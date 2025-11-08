import React, { useEffect } from 'react';
import { Modal, Form, Select, Input, InputNumber } from 'antd';
import type { SuggestionRequestDTO } from '../../services/aiinsightService';

interface SuggestionFormModalProps {
  visible: boolean;
  loading?: boolean;
  initialType: SuggestionRequestDTO['suggestionType'];
  onSubmit: (values: SuggestionRequestDTO) => void;
  onCancel: () => void;
}

const { TextArea } = Input;

const timeFrameOptions: Array<{ label: string; value: SuggestionRequestDTO['timeFrame'] }> = [
  { label: 'Day', value: 'day' },
  { label: 'Week', value: 'week' },
  { label: 'Month', value: 'month' },
];

const intensityOptions: Array<{ label: string; value: SuggestionRequestDTO['intensity'] }> = [
  { label: 'Low', value: 'low' },
  { label: 'Moderate', value: 'moderate' },
  { label: 'High', value: 'high' },
];

const experienceOptions: Array<{ label: string; value: SuggestionRequestDTO['experienceLevel'] }> = [
  { label: 'Beginner', value: 'beginner' },
  { label: 'Intermediate', value: 'intermediate' },
  { label: 'Advanced', value: 'advanced' },
];

const SuggestionFormModal: React.FC<SuggestionFormModalProps> = ({
  visible,
  loading,
  initialType,
  onSubmit,
  onCancel,
}) => {
  const [form] = Form.useForm<SuggestionRequestDTO>();

  useEffect(() => {
    if (visible) {
      form.setFieldsValue({
        suggestionType: initialType,
        timeFrame: 'week',
        intensity: 'moderate',
        schedule: {
          daysPerWeek: 3,
          minutesPerSession: 45,
        },
      });
    } else {
      form.resetFields();
    }
  }, [visible, initialType, form]);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      onSubmit(values);
    } catch {
      // validation errors handled by antd Form
    }
  };

  return (
    <Modal
      title="Custom AI Suggestion"
      visible={visible}
      onOk={handleOk}
      onCancel={onCancel}
      okText="Generate Suggestion"
      confirmLoading={loading}
      destroyOnClose
    >
      <Form<SuggestionRequestDTO> form={form} layout="vertical">
        <Form.Item<SuggestionRequestDTO> name="suggestionType" label="Suggestion Type" rules={[{ required: true, message: 'Please choose a suggestion type.' }]}>
          <Select>
            <Select.Option value="diet">Diet / Nutrition</Select.Option>
            <Select.Option value="exercise">Exercise</Select.Option>
          </Select>
        </Form.Item>

        <Form.Item<SuggestionRequestDTO> name="userGoal" label="Primary Goal" tooltip="e.g., weight_loss, maintenance, muscle_gain">
          <Input placeholder="Describe your main goal (snake_case if matching backend enums)" />
        </Form.Item>

        <Form.Item<SuggestionRequestDTO> name="timeFrame" label="Time Frame">
          <Select options={timeFrameOptions} />
        </Form.Item>

        <Form.Item<SuggestionRequestDTO> name="intensity" label="Preferred Intensity">
          <Select options={intensityOptions} />
        </Form.Item>

        <Form.Item<SuggestionRequestDTO> name="experienceLevel" label="Experience Level">
          <Select options={experienceOptions} />
        </Form.Item>

        <Form.Item name="focusAreas" label="Focus Areas" tooltip="Add body parts or nutritional focus areas">
          <Select mode="tags" placeholder="e.g., core, endurance, macros" />
        </Form.Item>

        <Form.Item name="availableEquipment" label="Available Equipment">
          <Select mode="tags" placeholder="e.g., dumbbells, treadmill, blender" />
        </Form.Item>

        <Form.Item name="dietaryPreferences" label="Dietary Preferences">
          <Select mode="tags" placeholder="e.g., vegetarian, high protein" />
        </Form.Item>

        <Form.Item name="restrictions" label="Restrictions / Allergies">
          <Select mode="tags" placeholder="e.g., gluten-free, low sodium" />
        </Form.Item>

        <Form.Item label="Weekly Schedule">
          <Input.Group compact>
            <Form.Item name={['schedule', 'daysPerWeek']} noStyle>
              <InputNumber min={1} max={7} placeholder="Days per week" style={{ width: '50%' }} />
            </Form.Item>
            <Form.Item name={['schedule', 'minutesPerSession']} noStyle>
              <InputNumber min={10} max={180} placeholder="Minutes per session" style={{ width: '50%' }} />
            </Form.Item>
          </Input.Group>
        </Form.Item>

        <Form.Item name={['schedule', 'preferredTimes']} label="Preferred Times">
          <Select mode="tags" placeholder="e.g., mornings, evenings" />
        </Form.Item>

        <Form.Item<SuggestionRequestDTO> name="notes" label="Additional Notes">
          <TextArea rows={3} placeholder="Anything else the AI should consider?" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default SuggestionFormModal;
