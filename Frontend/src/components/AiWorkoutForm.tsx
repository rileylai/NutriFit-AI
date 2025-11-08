import { Button, DatePicker, Form, Input, message } from 'antd';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { useState } from 'react';
import { estimateWorkoutWithAi } from '../services/workoutService';
import { useAppSelector } from '../store/hooks';
import type { AiWorkoutSummary } from '../types/AiWorkout';

type AiWorkoutFormValues = {
  description: string;
  workoutDate?: Dayjs;
  notes?: string;
  // autoSave: boolean;
};

type AiWorkoutFormProps = {
  onWorkoutLogged?: () => void;
  onAiSummary?: (summary: AiWorkoutSummary) => void;
};

const DATE_FORMAT = 'YYYY-MM-DD';

export default function AiWorkoutForm({ onWorkoutLogged: _onWorkoutLogged, onAiSummary }: AiWorkoutFormProps) {
  const [form] = Form.useForm<AiWorkoutFormValues>();
  const [submitting, setSubmitting] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const descriptionValue = Form.useWatch('description', form);
  void _onWorkoutLogged;
  const hasAuthenticatedUser = useAppSelector((state) => Boolean(state.auth.user));
  // The presence of a user (with uuid) is the only check we need before hitting the AI endpoint.

  const handleFinish = async (values: AiWorkoutFormValues) => {
    const trimmedDescription = values.description.trim();
    if (!trimmedDescription) {
      return;
    }
    if (!hasAuthenticatedUser) {
      messageApi.error('Please sign in again before requesting an AI estimate.');
      return;
    }

    setSubmitting(true);

    try {
      // send request to AI estimation service
      const response = await estimateWorkoutWithAi({
        description: trimmedDescription,
        workoutDate: values.workoutDate ? values.workoutDate.format(DATE_FORMAT) : undefined,
        notes: values.notes && values.notes.trim() ? values.notes.trim() : null,
        // save: values.autoSave,
      });

      if (!response.success) {
        throw new Error(response.message || 'Failed to estimate workout.');
      }

      if (response.data) {
        onAiSummary?.(response.data);
      }

      messageApi.success(response.message);

      // if (values.autoSave) {
      //   onWorkoutLogged?.();
      // }

      form.resetFields();
      form.setFieldsValue({
        // autoSave: values.autoSave,
        workoutDate: values.workoutDate ?? dayjs(),
      });
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to estimate workout.';
      messageApi.error(errorMessage);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      {contextHolder}
      <Form<AiWorkoutFormValues>
        layout="vertical"
        form={form}
        initialValues={{
          description: '',
          // autoSave: false,
          workoutDate: dayjs(), // default to today
        }}
        onFinish={handleFinish}
      >
        <Form.Item
          label="Describe your workout today"
          name="description"
          rules={[{ required: true, message: 'Please describe your workout.' }]}
        >
          <Input.TextArea rows={5} placeholder='e.g. "I ran 40 minutes in the park"' />
        </Form.Item>

        <Form.Item label="Workout Date" name="workoutDate">
          <DatePicker style={{ width: '100%' }} format={DATE_FORMAT} />
        </Form.Item>

        <Form.Item label="Notes" name="notes">
          <Input.TextArea rows={3} placeholder="Optional context for the AI estimator" />
        </Form.Item>

        {/* <Form.Item label="Automatically save workout" name="autoSave" valuePropName="checked">
          <Switch />
        </Form.Item> */}

        <Form.Item>
          <Button
            type="primary"
            htmlType="submit"
            block
            loading={submitting}
            disabled={submitting || !descriptionValue || !descriptionValue.trim()}
          >
            Estimate Calories with AI
          </Button>
        </Form.Item>
      </Form>
    </>
  );
}
