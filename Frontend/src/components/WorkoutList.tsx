import { Button, Card, DatePicker, Empty, Popconfirm, Select, Space, Table, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { DeleteOutlined, EditOutlined } from '@ant-design/icons';
import dayjs, { Dayjs } from 'dayjs';
import type { Workout, WorkoutSortOption } from '../types/Workout';

const { Title, Text } = Typography;

type WorkoutListProps = {
  labelForCurrentDay: string;
  currentDayDisplayDate: string;
  todayWorkouts: Workout[];
  historyWorkouts: Workout[];
  loadingToday: boolean;
  loadingHistory: boolean;
  sortBy: WorkoutSortOption;
  onSortChange: (sort: WorkoutSortOption) => void;
  onEdit: (workout: Workout) => void;
  onDelete: (workoutId: number) => Promise<void>;
  deleteInProgressId: number | null;

  //
  selectedDate: Dayjs | null;
  onDateChange: (date: Dayjs | null) => void;
  //
};

const actionsColumn = (
  onEdit: (workout: Workout) => void,
  onDelete: (workoutId: number) => Promise<void>,
  deleteInProgressId: number | null,
): ColumnsType<Workout>[number] => ({
  title: 'Actions',
  key: 'actions',
  align: 'center',
  render: (_: unknown, workout: Workout) => (
    <Space size="middle">
      <Button type="text" icon={<EditOutlined />} onClick={() => onEdit(workout)} aria-label="Edit workout" />
      <Popconfirm
        title="Delete workout"
        description="Are you sure you want to delete this workout?"
        onConfirm={async () => {
          await onDelete(workout.workoutId);
        }}
        okButtonProps={{ loading: deleteInProgressId === workout.workoutId }}
      >
        <Button
          type="text"
          danger
          icon={<DeleteOutlined />}
          aria-label="Delete workout"
          loading={deleteInProgressId === workout.workoutId}
        />
      </Popconfirm>
    </Space>
  ),
});

const todayColumns = (
  onEdit: (workout: Workout) => void,
  onDelete: (workoutId: number) => Promise<void>,
  deleteInProgressId: number | null,
): ColumnsType<Workout> => [
  {
    title: 'Duration (minutes)',
    dataIndex: 'durationMinutes',
    key: 'durationMinutes',
    align: 'center',
  },
  {
    title: 'Workout Type',
    dataIndex: 'workoutType',
    key: 'workoutType',
    align: 'center',
  },
  {
    title: 'Calories Burned',
    dataIndex: 'caloriesBurned',
    key: 'caloriesBurned',
    align: 'center',
  },
  actionsColumn(onEdit, onDelete, deleteInProgressId),
];

const historyColumns = (
  onEdit: (workout: Workout) => void,
  onDelete: (workoutId: number) => Promise<void>,
  deleteInProgressId: number | null,
): ColumnsType<Workout> => [
  {
    title: 'Date',
    dataIndex: 'workoutDate',
    key: 'workoutDate',
    render: (value: string) => dayjs(value).format('MMM D, YYYY'),
  },
  {
    title: 'Workout Type',
    dataIndex: 'workoutType',
    key: 'workoutType',
  },
  {
    title: 'Duration (minutes)',
    dataIndex: 'durationMinutes',
    key: 'durationMinutes',
  },
  {
    title: 'Calories Burned',
    dataIndex: 'caloriesBurned',
    key: 'caloriesBurned',
  },
  {
    title: 'Notes',
    dataIndex: 'notes',
    key: 'notes',
    render: (value: string | null | undefined) => value ?? '—',
  },
  actionsColumn(onEdit, onDelete, deleteInProgressId),
];

export default function WorkoutList({
  labelForCurrentDay,
  currentDayDisplayDate,
  todayWorkouts,
  historyWorkouts,
  loadingToday,
  loadingHistory,
  sortBy,
  onSortChange,
  onEdit,
  onDelete,
  deleteInProgressId,
  selectedDate,
  onDateChange,
}: WorkoutListProps) {
  return (
    <Space direction="vertical" size={24} style={{ width: '100%' }}>
      <Card>
        <Space direction="vertical" size={16} style={{ width: '100%' }}>
          {/* <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 16 }}>
              <div>
                <Title level={4} style={{ margin: 0 }}>
                  {labelForCurrentDay}
                </Title>
                <Text type="secondary">{currentDayDisplayDate}</Text>
              </div> */}
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'flex-start',
                  flexWrap: 'wrap',
                  gap: 16,
                }}
              >
                <div>
                  <Title level={4} style={{ margin: 0 }}>
                    {labelForCurrentDay}
                  </Title>
                  <Text type="secondary">{currentDayDisplayDate}</Text>
                </div>
                <div>
                  <Text type="secondary">Select a day to review</Text>
                  <br />
                  <DatePicker
                    allowClear
                    format="YYYY-MM-DD"
                    value={selectedDate}
                    onChange={onDateChange}
                  />
                </div>
              </div>
              {/* <div>
                <Text type="secondary">Select a day to review</Text>
                <br />
                <DatePicker
                  allowClear
                  format="YYYY-MM-DD"
                  value={selectedDate}
                  onChange={onDateChange}
                />
              </div> */}
            {/* </div> */}


          {todayWorkouts.length === 0 ? (
            <Empty description={`No workouts logged for ${labelForCurrentDay.toLowerCase()}.`} />
          ) : (
            <Table<Workout>
              rowKey="workoutId"
              columns={todayColumns(onEdit, onDelete, deleteInProgressId)}
              dataSource={todayWorkouts}
              pagination={false}
              loading={loadingToday}
            />
          )}
        </Space>
      </Card>

      <Card>
        <Space direction="vertical" size={16} style={{ width: '100%' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <Title level={4} style={{ margin: 0 }}>
              History
            </Title>
            <Select<WorkoutSortOption>
              value={sortBy}
              onChange={onSortChange}
              style={{ width: 220 }}
              options={[
                { value: 'date_desc', label: 'Most recent' },
                { value: 'date_asc', label: 'Oldest first' },
                { value: 'type', label: 'Workout type (A-Z)' },
                { value: 'calories_desc', label: 'Calories burned (high-low)' },
              ]}
            />
          </div>
          {historyWorkouts.length === 0 ? (
            <Empty description="No workout history yet." />
          ) : (
            <Table<Workout>
              rowKey="workoutId"
              columns={historyColumns(onEdit, onDelete, deleteInProgressId)}
              dataSource={historyWorkouts}
              pagination={{ pageSize: 8 }}
              loading={loadingHistory}
            />
          )}
        </Space>
      </Card>
    </Space>
  );
}
