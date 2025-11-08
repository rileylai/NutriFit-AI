import { DatePicker, Form, Input, InputNumber, Modal } from 'antd';
import { useEffect } from 'react';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import type { Workout } from '../types/Workout';

export interface EditWorkoutFormValues {
  workoutType: string;
  workoutDate: Dayjs;
  durationMinutes: number;
  caloriesBurned: number;
  notes?: string;
}

type EditWorkoutModalProps = {
  workout: Workout | null;
  open: boolean;
  confirmLoading?: boolean;
  onCancel: () => void;
  onSubmit: (values: EditWorkoutFormValues) => Promise<void>;
};

const DATE_FORMAT = 'YYYY-MM-DD';

export default function EditWorkoutModal({
  workout,
  open,
  confirmLoading = false,
  onCancel,
  onSubmit,
}: EditWorkoutModalProps) {
  const [form] = Form.useForm<EditWorkoutFormValues>();

  const handleCancel = () => {
    form.resetFields();
    onCancel();
  };

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      await onSubmit(values);
      form.resetFields();
    } catch {
      // Validation errors are handled by Ant Design; other errors bubble up.
    }
  };

  useEffect(() => {
    if (open && workout) {
      form.setFieldsValue({
        workoutType: workout.workoutType,
        workoutDate: dayjs(workout.workoutDate, DATE_FORMAT),
        durationMinutes: workout.durationMinutes,
        caloriesBurned: workout.caloriesBurned,
        notes: workout.notes ?? undefined,
      });
    }

    if (!open) {
      form.resetFields();
    }
  }, [form, open, workout]);

  return (
    <Modal
      title="Edit Workout"
      open={open}
      onCancel={handleCancel}
      onOk={handleOk}
      okText="Save"
      confirmLoading={confirmLoading}
    >
      <Form<EditWorkoutFormValues> form={form} layout="vertical">
        <Form.Item
          label="Workout Type"
          name="workoutType"
          rules={[{ required: true, message: 'Please enter the workout type.' }]}
        >
          <Input placeholder="e.g. Running" />
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
          rules={[{ required: true, message: 'Please enter the duration.' }]}
        >
          <InputNumber style={{ width: '100%' }} min={1} max={600} placeholder="30" />
        </Form.Item>

        <Form.Item
          label="Calories Burned"
          name="caloriesBurned"
          rules={[{ required: true, message: 'Please enter the calories burned.' }]}
        >
          <InputNumber style={{ width: '100%' }} min={0} max={2000} placeholder="250" />
        </Form.Item>

        <Form.Item label="Notes" name="notes">
          <Input.TextArea rows={3} placeholder="Optional notes about the session" />
        </Form.Item>
      </Form>
    </Modal>
  );
}
