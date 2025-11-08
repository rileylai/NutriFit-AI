import React from 'react';
import { Row, Col, Card, Typography, Space, Button, Dropdown } from 'antd';
import { FireOutlined, ExportOutlined, DownOutlined } from '@ant-design/icons';
import type { MenuProps } from 'antd';
import type { DashboardPeriod } from '../../types/HomePage';

const { Title, Text } = Typography;

interface HeaderProps {
  timeRange: DashboardPeriod;
  onTimeRangeChange: (range: DashboardPeriod) => void;
  onExportReport: (format: 'json' | 'pdf') => void;
}

const Header: React.FC<HeaderProps> = ({
  timeRange,
  onTimeRangeChange,
  onExportReport,
}) => {
  const exportMenuItems: MenuProps['items'] = [
    {
      key: 'json',
      label: 'Export as JSON',
      onClick: () => onExportReport('json'),
    },
    {
      key: 'pdf',
      label: 'Export as PDF',
      onClick: () => onExportReport('pdf'),
    },
  ];

  return (
    <Card>
      <Row justify="space-between" align="middle">
        <Col>
          <Title level={2} style={{ margin: 0 }}>
            <FireOutlined style={{ color: '#ff7875', marginRight: '12px' }} />
            NutriFit AI Dashboard
          </Title>
          <Text type="secondary">Welcome back! Here's your personalized health overview</Text>
        </Col>
        <Col>
          <Space>
            <Button.Group>
              <Button
                type={timeRange === 'weekly' ? 'primary' : 'default'}
                onClick={() => onTimeRangeChange('weekly')}
              >
                Last 7 days
              </Button>
              <Button
                type={timeRange === 'monthly' ? 'primary' : 'default'}
                onClick={() => onTimeRangeChange('monthly')}
              >
                Last 30 days
              </Button>
            </Button.Group>
            <Dropdown menu={{ items: exportMenuItems }} placement="bottomRight">
              <Button type="primary" icon={<ExportOutlined />}>
                Export Report <DownOutlined />
              </Button>
            </Dropdown>
          </Space>
        </Col>
      </Row>
    </Card>
  );
};

export default Header;