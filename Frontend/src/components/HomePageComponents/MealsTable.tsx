import React, { useMemo, useState } from 'react';
import { Card, Space, Avatar, Button, Typography, List, Tag, Segmented, Tooltip } from 'antd';
import type { SegmentedValue } from 'antd/es/segmented';
import { DeleteOutlined } from '@ant-design/icons';
import type { Meal } from '../../types/HomePage';

const { Text } = Typography;

interface MealsTableProps {
  meals: Meal[];
  onDeleteMeal: (mealId: string) => void;
}

const MealsTable: React.FC<MealsTableProps> = ({
  meals,
  onDeleteMeal,
}) => {
  const [filter, setFilter] = useState<'all' | Meal['type']>('all');
  const handleFilterChange = (value: SegmentedValue) => {
    setFilter(value as 'all' | Meal['type']);
  };
  const filteredMeals = useMemo(() => {
    if (filter === 'all') return meals;
    return meals.filter((m) => m.type === filter);
  }, [meals, filter]);

  return (
    <Card
      title="Today's Meals"
      style={{ borderRadius: 12, boxShadow: '0 4px 14px rgba(0,0,0,0.06)' }}
      headStyle={{ borderBottom: '1px solid #f0f0f0' }}
      extra={
        <Segmented
          options={[
            { label: 'All', value: 'all' },
            { label: 'Breakfast', value: 'breakfast' },
            { label: 'Lunch', value: 'lunch' },
            { label: 'Dinner', value: 'dinner' },
            { label: 'Snack', value: 'snack' },
          ]}
          value={filter}
          onChange={handleFilterChange}
          size="small"
        />
      }
    >
      <List
        dataSource={filteredMeals}
        itemLayout="horizontal"
        renderItem={(item: Meal) => {
          const color =
            item.type === 'breakfast' ? '#52c41a' :
            item.type === 'lunch' ? '#1890ff' :
            item.type === 'dinner' ? '#722ed1' : '#faad14';
          return (
            <List.Item
              style={{ padding: '10px 8px' }}
              actions={[
                <Tag color="blue" key={`cal-${item.id}`} style={{ fontSize: 12, padding: '2px 8px', borderRadius: 8 }}>{item.calories} kcal</Tag>,
                <Tag color="green" key={`prot-${item.id}`} style={{ fontSize: 12, padding: '2px 8px', borderRadius: 8 }}>{item.protein}g</Tag>,
                <Space key={`act-${item.id}`}>
                  <Tooltip title="Delete meal">
                    <Button size="small" danger icon={<DeleteOutlined />} onClick={() => onDeleteMeal(item.id)} aria-label="Delete meal" />
                  </Tooltip>
                </Space>
              ]}
            >
              <Space size="middle">
                <Avatar size={32} style={{ backgroundColor: color }}>
                  {item.type.charAt(0).toUpperCase()}
                </Avatar>
                <div>
                  <Text strong>{item.name}</Text>
                  <div>
                    <Text type="secondary" style={{ fontSize: 12 }}>{item.time}</Text>
                  </div>
                </div>
              </Space>
            </List.Item>
          );
        }}
      />
    </Card>
  );
};

export default MealsTable;
