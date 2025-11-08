import { Button, Card, Empty, Popconfirm, Select, Space, Table, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { DeleteOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import type { MealDetail } from '../types/Meal';

const { Title } = Typography;

type MealSortOption = 'date_desc' | 'date_asc' | 'calories_desc' | 'calories_asc';

type MealHistoryListProps = {
  meals: MealDetail[];
  loading: boolean;
  sortBy: MealSortOption;
  onSortChange: (sort: MealSortOption) => void;
  onDelete: (mealId: number) => Promise<void>;
  deleteInProgressId: number | null;
};

const historyColumns = (
  onDelete: (mealId: number) => Promise<void>,
  deleteInProgressId: number | null,
): ColumnsType<MealDetail> => [
  {
    title: 'Date',
    dataIndex: 'mealTime',
    key: 'mealTime',
    render: (value: string | null) => {
      if (!value) return '—';
      return dayjs(value).format('MMM D, YYYY HH:mm');
    },
  },
  {
    title: 'Meal',
    dataIndex: 'mealDescription',
    key: 'mealDescription',
    ellipsis: true,
  },
  {
    title: 'Type',
    dataIndex: 'role',
    key: 'role',
    render: (value: string | null) => {
      if (!value) return '—';
      return value.charAt(0).toUpperCase() + value.slice(1);
    },
  },
  {
    title: 'Calories',
    dataIndex: 'totalCalories',
    key: 'totalCalories',
    align: 'center',
    render: (value: number) => Math.round(value),
  },
  {
    title: 'Protein (g)',
    dataIndex: 'proteinG',
    key: 'proteinG',
    align: 'center',
    render: (value: number) => value?.toFixed(1) ?? '—',
  },
  {
    title: 'Carbs (g)',
    dataIndex: 'carbsG',
    key: 'carbsG',
    align: 'center',
    render: (value: number) => value?.toFixed(1) ?? '—',
  },
  {
    title: 'Fat (g)',
    dataIndex: 'fatG',
    key: 'fatG',
    align: 'center',
    render: (value: number) => value?.toFixed(1) ?? '—',
  },
  {
    title: 'Actions',
    key: 'actions',
    align: 'center',
    render: (_: unknown, meal: MealDetail) => (
      <Popconfirm
        title="Delete meal"
        description="Are you sure you want to delete this meal?"
        onConfirm={async () => {
          await onDelete(meal.mealId);
        }}
        okButtonProps={{ loading: deleteInProgressId === meal.mealId }}
      >
        <Button
          type="text"
          danger
          icon={<DeleteOutlined />}
          aria-label="Delete meal"
          loading={deleteInProgressId === meal.mealId}
        />
      </Popconfirm>
    ),
  },
];

export default function MealHistoryList({
  meals,
  loading,
  sortBy,
  onSortChange,
  onDelete,
  deleteInProgressId,
}: MealHistoryListProps) {
  return (
    <Card>
      <Space direction="vertical" size={16} style={{ width: '100%' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
          <Title level={4} style={{ margin: 0 }}>
            Meal History
          </Title>
          <Select<MealSortOption>
            value={sortBy}
            onChange={onSortChange}
            style={{ width: 220 }}
            options={[
              { value: 'date_desc', label: 'Most recent' },
              { value: 'date_asc', label: 'Oldest first' },
              { value: 'calories_desc', label: 'Calories (high-low)' },
              { value: 'calories_asc', label: 'Calories (low-high)' },
            ]}
          />
        </div>
        {meals.length === 0 ? (
          <Empty description="No meal history yet." />
        ) : (
          <Table<MealDetail>
            rowKey="mealId"
            columns={historyColumns(onDelete, deleteInProgressId)}
            dataSource={meals}
            pagination={{ pageSize: 10, showSizeChanger: true, pageSizeOptions: ['10', '20', '50'] }}
            loading={loading}
            scroll={{ x: 'max-content' }}
          />
        )}
      </Space>
    </Card>
  );
}
