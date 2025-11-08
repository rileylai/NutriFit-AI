import React from 'react';
import { Modal, Select, Typography, Space } from 'antd';

const { Text } = Typography;

interface AIInsightModalProps {
  visible: boolean;
  selectedType: 'exercise' | 'nutrition';
  onTypeChange: (type: 'exercise' | 'nutrition') => void;
  onGenerate: () => void;
  onCancel: () => void;
}

const AIInsightModal: React.FC<AIInsightModalProps> = ({
  visible,
  selectedType,
  onTypeChange,
  onGenerate,
  onCancel,
}) => {
  return (
    <Modal
      title="Generate AI Insight"
      visible={visible}
      onOk={onGenerate}
      onCancel={onCancel}
      okText="Generate Insight"
    >
      <Space direction="vertical" style={{ width: '100%' }}>
        <Text>What type of insight would you like to generate?</Text>
        <Select
          value={selectedType}
          onChange={onTypeChange}
          style={{ width: '100%' }}
        >
          <Select.Option value="nutrition">Nutrition Recommendations</Select.Option>
          <Select.Option value="exercise">Exercise Plan Optimization</Select.Option>
        </Select>
        <Text type="secondary">
          AI will analyze your recent data and generate personalized recommendations based on your goals.
        </Text>
      </Space>
    </Modal>
  );
};

export default AIInsightModal;