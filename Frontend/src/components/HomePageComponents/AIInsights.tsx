import React from 'react';
import { Card, List, Space, Avatar, Tag, Typography, Button, Badge, Modal } from 'antd';
import { BulbOutlined, ClockCircleOutlined } from '@ant-design/icons';
import type { AIInsightDTO, SuggestionContent } from '../../services/aiinsightService';

const { Text, Paragraph } = Typography;

interface AISuggestionItem {
  content: SuggestionContent;
  requestId?: string;
  suggestionId?: string;
  timestamp?: string;
  source: 'quick' | 'custom' | 'latest';
}

interface AIInsightsProps {
  insights: AIInsightDTO[];
  suggestion?: AISuggestionItem | null;
  onClearSuggestion?: () => void;
  onGenerateInsight: () => void;
  onDismissInsight: (insightId: number) => void;
  onQuickSuggestion: () => void;
  onCustomSuggestion: () => void;
  quickSuggestionLoading?: boolean;
  customSuggestionLoading?: boolean;
  clearingSuggestion?: boolean;
}

const AIInsights: React.FC<AIInsightsProps> = ({
  insights,
  suggestion,
  onClearSuggestion,
  onGenerateInsight,
  onDismissInsight,
  onQuickSuggestion,
  onCustomSuggestion,
  quickSuggestionLoading = false,
  customSuggestionLoading = false,
  clearingSuggestion = false,
}) => {
  const [selectedInsight, setSelectedInsight] = React.useState<AIInsightDTO | null>(null);
  const suggestionContent = suggestion?.content;
  const recommendations = Array.isArray(suggestionContent?.recommendations)
    ? suggestionContent.recommendations
    : [];
  const hasRecommendations = recommendations.length > 0;
  const metricsEntries = suggestionContent?.specificMetrics
    ? Object.entries(suggestionContent.specificMetrics)
    : [];
  const hasMetrics = metricsEntries.length > 0;
  const suggestionTimestamp = suggestion?.timestamp
    ? new Date(suggestion.timestamp).toLocaleString()
    : undefined;
  let suggestionSourceLabel = 'Quick Suggestion';
  let suggestionSourceColor: string = 'geekblue';

  if (suggestion?.source === 'custom') {
    suggestionSourceLabel = 'Custom Suggestion';
    suggestionSourceColor = 'purple';
  } else if (suggestion?.source === 'latest') {
    suggestionSourceLabel = 'Latest Suggestion';
    suggestionSourceColor = 'blue';
  }
  const confidenceValue = typeof suggestionContent?.confidenceScore === 'number'
    ? Math.round(Number(suggestionContent.confidenceScore))
    : undefined;
  const getCategoryLabel = (category: string) => {
    if (category === 'nutrition') {
      return 'Nutrition Insight';
    }
    if (category === 'exercise') {
      return 'Exercise Insight';
    }
    return 'General Insight';
  };
  const getCategoryAvatarColor = (category: string) => {
    if (category === 'nutrition') {
      return '#52c41a';
    }
    if (category === 'exercise') {
      return '#1890ff';
    }
    return '#722ed1';
  };
  const getCategoryInitial = (category: string) => {
    if (category === 'nutrition') {
      return 'N';
    }
    if (category === 'exercise') {
      return 'E';
    }
    return 'G';
  };
  const getPriorityBadgeColor = (priority: number) => {
    if (priority === 1) {
      return '#ff4d4f';
    }
    if (priority === 2) {
      return '#faad14';
    }
    return '#52c41a';
  };
  const getPriorityTagColor = (priority: number) => {
    if (priority === 1) {
      return 'red';
    }
    if (priority === 2) {
      return 'orange';
    }
    return 'green';
  };
  const getPriorityLabel = (priority: number) => {
    if (priority === 1) {
      return 'HIGH';
    }
    if (priority === 2) {
      return 'MEDIUM';
    }
    return 'LOW';
  };
  const formatDateTime = (value?: string) => {
    if (!value) {
      return null;
    }
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return value;
    }
    return parsed.toLocaleString();
  };
  const handleOpenInsightDetail = (insight: AIInsightDTO) => {
    setSelectedInsight(insight);
  };
  const handleCloseInsightDetail = () => {
    setSelectedInsight(null);
  };
  const handleDismissInsight = (insightId: number) => {
    if (selectedInsight?.insightId === insightId) {
      setSelectedInsight(null);
    }
    onDismissInsight(insightId);
  };

  return (
    <Card 
      title={
        <Space>
          <BulbOutlined style={{ color: '#faad14' }} />
          AI Insights & Recommendations
        </Space>
      }
      extra={
        <Space>
          <Button onClick={onQuickSuggestion} loading={quickSuggestionLoading}>
            Quick Suggestion
          </Button>
          <Button type="dashed" onClick={onCustomSuggestion} loading={customSuggestionLoading}>
            Custom Suggestion
          </Button>
          <Button type="primary" onClick={onGenerateInsight}>
            Generate Insight
          </Button>
        </Space>
      }
    >
      {suggestionContent && (
        <div
          style={{
            marginBottom: 16,
            padding: '12px 16px',
            background: '#fafafa',
            border: '1px solid #f0f0f0',
            borderRadius: 8,
          }}
        >
          <Space direction="vertical" size="small" style={{ width: '100%' }}>
            <Space
              align="baseline"
              style={{ width: '100%', justifyContent: 'space-between' }}
            >
              <Space size={[8, 4]} wrap>
                <Text strong>Latest AI Suggestion</Text>
                <Tag color={suggestionSourceColor}>{suggestionSourceLabel}</Tag>
                {suggestionContent.suggestionType && (
                  <Tag>Type: {suggestionContent.suggestionType}</Tag>
                )}
                {suggestionContent.userGoal && (
                  <Tag color="cyan">Goal: {suggestionContent.userGoal}</Tag>
                )}
                {suggestionContent.timeFrame && (
                  <Tag color="green">Time Frame: {suggestionContent.timeFrame}</Tag>
                )}
                {confidenceValue !== undefined && (
                  <Tag color="gold">Confidence: {confidenceValue}%</Tag>
                )}
              </Space>
              <Space size="small">
                {suggestionTimestamp && (
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {suggestionTimestamp}
                  </Text>
                )}
                {suggestion?.requestId && (
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    ID: {suggestion.requestId}
                  </Text>
                )}
                {onClearSuggestion && (
                  <Button type="link" size="small" onClick={onClearSuggestion} loading={clearingSuggestion}>
                    Clear
                  </Button>
                )}
              </Space>
            </Space>

            {suggestionContent.rationale && (
              <Paragraph style={{ marginBottom: 0 }}>
                {suggestionContent.rationale}
              </Paragraph>
            )}

            {suggestionContent.summary && (
              <Paragraph style={{ marginBottom: 0 }}>
                {suggestionContent.summary}
              </Paragraph>
            )}

            {suggestionContent.plan && (
              <Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>
                {suggestionContent.plan}
              </Paragraph>
            )}

            {hasRecommendations && (
              <div>
                <Text strong>Recommendations</Text>
                <ul style={{ margin: '8px 0 0 16px', padding: 0 }}>
                  {recommendations.map((item, index) => (
                    <li key={index}>{item}</li>
                  ))}
                </ul>
              </div>
            )}

            {hasMetrics && (
              <div>
                <Text strong>Specific Metrics</Text>
                <ul style={{ margin: '8px 0 0 16px', padding: 0 }}>
                  {metricsEntries.map(([key, value]) => (
                    <li key={key}>
                      <Text strong>{key}:</Text> <Text>{value}</Text>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </Space>
        </div>
      )}

      <List
        dataSource={insights.filter(insight => insight.isActive)}
        renderItem={(insight) => (
          <List.Item
            actions={[
              <Button
                type="link"
                size="small"
                onClick={() => handleOpenInsightDetail(insight)}
              >
                Details
              </Button>,
              <Button
                type="link"
                size="small"
                danger
                onClick={() => handleDismissInsight(insight.insightId)}
              >
                Dismiss
              </Button>
            ]}
          >
            <List.Item.Meta
              avatar={
                <Badge
                  color={getPriorityBadgeColor(insight.priority)}
                >
                  <Avatar
                    style={{
                      backgroundColor: getCategoryAvatarColor(insight.category)
                    }}
                  >
                    {getCategoryInitial(insight.category)}
                  </Avatar>
                </Badge>
              }
              title={
                <Space>
                  <Text strong>
                    {getCategoryLabel(insight.category)}
                  </Text>
                  <Tag color={getPriorityTagColor(insight.priority)}>
                    {getPriorityLabel(insight.priority)}
                  </Tag>
                  {insight.status === 'new' && <Tag color="blue">NEW</Tag>}
                </Space>
              }
              description={
                <div>
                  <Paragraph style={{ margin: '8px 0' }}>{insight.content}</Paragraph>
                  <Text type="secondary" style={{ fontSize: '12px' }}>
                    <ClockCircleOutlined /> {formatDateTime(insight.createdAt) ?? 'Unknown'}
                  </Text>
                </div>
              }
            />
          </List.Item>
        )}
      />
      <Modal
        title="AI Insight Details"
        visible={Boolean(selectedInsight)}
        onCancel={handleCloseInsightDetail}
        destroyOnClose
        footer={[
          <Button key="close" onClick={handleCloseInsightDetail}>
            Close
          </Button>,
          selectedInsight ? (
            <Button
              key="dismiss"
              type="primary"
              danger
              onClick={() => handleDismissInsight(selectedInsight.insightId)}
            >
              Dismiss Insight
            </Button>
          ) : null,
        ]}
      >
        {selectedInsight && (
          <Space direction="vertical" size="small" style={{ width: '100%' }}>
            <Space size={[8, 8]} wrap>
              <Tag color={getPriorityTagColor(selectedInsight.priority)}>
                {getPriorityLabel(selectedInsight.priority)}
              </Tag>
              <Tag>
                {getCategoryLabel(selectedInsight.category)}
              </Tag>
              {selectedInsight.status && <Tag color="blue">{selectedInsight.status.toUpperCase()}</Tag>}
              {selectedInsight.suggestionFormat && (
                <Tag color="geekblue">Format: {selectedInsight.suggestionFormat}</Tag>
              )}
            </Space>
            <Paragraph style={{ whiteSpace: 'pre-wrap' }}>
              {selectedInsight.content}
            </Paragraph>
            <Space direction="vertical" size={2}>
              <Text type="secondary">
                Created At: {formatDateTime(selectedInsight.createdAt) ?? 'Unknown'}
              </Text>
              <Text type="secondary">
                Expires At: {formatDateTime(selectedInsight.expiresAt) ?? 'Unknown'}
              </Text>
              <Text type="secondary">
                Updated At: {formatDateTime(selectedInsight.updatedAt) ?? 'Unknown'}
              </Text>
            </Space>
          </Space>
        )}
      </Modal>
    </Card>
  );
};

export default AIInsights;
