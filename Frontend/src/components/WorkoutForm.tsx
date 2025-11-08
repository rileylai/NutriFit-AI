import { Button, DatePicker, Form, Input, InputNumber, Select, message } from 'antd';
import { useEffect, useMemo, useRef, useState } from 'react';
import dayjs, { Dayjs } from 'dayjs';
import type { WorkoutRequest } from '../types/Workout';
import { createWorkout } from '../services/workoutService';
import { useAppSelector } from '../store/hooks';
import type { AiWorkoutSummary } from '../types/AiWorkout';

const DEFAULT_WORKOUT_TYPES = [
  'Running',
  'Cycling',
  'Swimming',
  'Strength Training',
  'Yoga',
  'Pilates',
  'HIIT',
  'Walking',
  'Hiking',
  'Rowing',
  'Dancing',
  'Boxing',
  'Martial Arts',
  'Stair Climbing',
  'Jump Rope',
  'CrossFit',
  'Elliptical',
  'Zumba',
  'Skating',
  'Skiing',
  'Snowboarding',
  'Basketball',
  'Soccer',
  'Tennis',
  'Badminton',
  'Volleyball',
  'Golf',
  'Baseball',
  'Rock Climbing',
];

const DATE_FORMAT = 'YYYY-MM-DD';

type WorkoutFormProps = {
  onWorkoutCreated: () => void;
  aiPrefill?: AiWorkoutSummary | null;
};

type WorkoutFormValues = {
  workoutType: string;
  durationMinutes: number;
  caloriesBurned: number;
  workoutDate: Dayjs;
  notes?: string;
};

export default function WorkoutForm({ onWorkoutCreated, aiPrefill = null }: WorkoutFormProps) {
  const [form] = Form.useForm<WorkoutFormValues>();
  const [submitting, setSubmitting] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const lastAppliedPrefillKey = useRef<string | null>(null);
  const hasAuthenticatedUser = useAppSelector((state) => Boolean(state.auth.user));
  // We only care that a user exists (identified by uuid); numeric ids are no longer tracked on the client.

  const selectOptions = useMemo(
    () =>
      {
        const uniqueTypes = new Set(DEFAULT_WORKOUT_TYPES);
        if (aiPrefill?.exerciseType && aiPrefill.exerciseType.trim().length > 0) {
          uniqueTypes.add(aiPrefill.exerciseType.trim());
        }

        return Array.from(uniqueTypes).map((label) => ({
          label,
          value: label,
        }));
      },
    [aiPrefill],
  );

  const handleFinish = async (values: WorkoutFormValues) => {
    if (!hasAuthenticatedUser) {
      messageApi.error('Please sign in again before logging a workout.');
      return;
    }

    setSubmitting(true);

    const payload: WorkoutRequest = {
      workoutType: values.workoutType,
      workoutDate: values.workoutDate.format(DATE_FORMAT),
      durationMinutes: values.durationMinutes,
      caloriesBurned: values.caloriesBurned,
      notes: values.notes?.trim() ? values.notes.trim() : null,
    };

    try {
      const response = await createWorkout(payload);

      if (!response.success) {
        throw new Error(response.message);
      }

      messageApi.success(response.message);
      form.resetFields();
      form.setFieldsValue({
        workoutType: DEFAULT_WORKOUT_TYPES[0],
        durationMinutes: 30,
        caloriesBurned: 200,
        workoutDate: dayjs(),
        notes: undefined,
      });
      onWorkoutCreated();
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Unable to save workout.';
      messageApi.error(errorMessage);
    } finally {
      setSubmitting(false);
    }
  };

  useEffect(() => {
    if (!aiPrefill) {
      lastAppliedPrefillKey.current = null;
      return;
    }

    const summaryKey = JSON.stringify(aiPrefill);
    if (summaryKey === lastAppliedPrefillKey.current) {
      return;
    }

    lastAppliedPrefillKey.current = summaryKey;

    const normalizedType = aiPrefill.exerciseType.trim();
    const workoutType = normalizedType.length > 0 ? normalizedType : DEFAULT_WORKOUT_TYPES[0];
    const roundedCalories = Math.max(0, Math.round(aiPrefill.caloriesBurned));
    const roundedDuration = Math.max(1, Math.round(aiPrefill.durationMinutes));

    form.setFieldsValue({
      workoutType,
      durationMinutes: roundedDuration,
      caloriesBurned: roundedCalories,
    });

    messageApi.success('AI estimation applied to the form fields.');
  }, [aiPrefill, form, messageApi]);

  return (
    <>
      {contextHolder}
      <Form<WorkoutFormValues>
        layout="vertical"
        form={form}
        initialValues={{
          workoutType: DEFAULT_WORKOUT_TYPES[0],
          durationMinutes: 30,
          caloriesBurned: 200,
          workoutDate: dayjs(),
        }}
        onFinish={handleFinish}
      >
        <Form.Item
          label="Workout Type"
          name="workoutType"
          rules={[{ required: true, message: 'Please select the workout type.' }]}
        >
          <Select options={selectOptions} placeholder="Select a workout" showSearch optionFilterProp="label" />
        </Form.Item>

        <Form.Item
          label="Workout Date"
          name="workoutDate"
          rules={[{ required: true, message: 'Please choose the workout date.' }]}
        >
          <DatePicker style={{ width: '100%' }} format={DATE_FORMAT} />
        </Form.Item>

        <Form.Item
          label="Duration (minutes)"
          name="durationMinutes"
          rules={[{ required: true, message: 'Please enter the duration in minutes.' }]}
        >
          <InputNumber style={{ width: '100%' }} min={1} max={600} placeholder="30" />
        </Form.Item>

        {/* <Form.Item
          label="Calories Burned"
          name="caloriesBurned"
          rules={[{ required: true, message: 'Please enter the estimated calories burned.' }]}
        >
          <InputNumber style={{ width: '100%' }} min={0} max={2000} placeholder="200" />
        </Form.Item> */}

        <Form.Item label="Notes" name="notes">
          <Input.TextArea rows={3} placeholder="Optional notes about the session" />
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit" block loading={submitting}>
            Add Workout
          </Button>
        </Form.Item>
      </Form>
    </>
  );
}
