import React from 'react';
import { Card, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import type { ProgressData } from '../../types/HomePage';

interface ProgressChartsProps {
  data: ProgressData[];
}

const ProgressCharts: React.FC<ProgressChartsProps> = ({ data }) => {
  const navigate = useNavigate();
  const formatDate = (iso: string) =>
    new Date(iso).toLocaleDateString(undefined, { month: 'short', day: '2-digit' });

  const cardStyle = { borderRadius: 12, boxShadow: '0 4px 14px rgba(0,0,0,0.06)' } as const;
  const headStyle = { borderBottom: '1px solid #f0f0f0' } as const;
  const { Text } = Typography;

  type ChartTooltipPayload = { dataKey?: string; value?: number | string };
  type ChartTooltipProps = { active?: boolean; payload?: ChartTooltipPayload[]; label?: string | number };

  const CustomTooltip: React.FC<ChartTooltipProps> = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      const findByKey = (key: string) =>
        payload.find((p) => p.dataKey === key);

      const cal = findByKey('calories');
      const kg = findByKey('weight');
      const wk = findByKey('workouts');
      const labelText = typeof label === 'string' ? formatDate(label) : undefined;

      return (
        <div style={{ background: '#fff', border: '1px solid #eee', padding: 12, borderRadius: 8 }}>
          {labelText && <div style={{ fontWeight: 600, marginBottom: 8 }}>{labelText}</div>}
          {kg && (
            <div style={{ color: '#667eea' }}>Weight: <b>{kg.value}</b> kg</div>
          )}
          {cal && (
            <div style={{ color: '#52c41a' }}>Calories: <b>{cal.value}</b></div>
          )}
          {wk && (
            <div style={{ color: '#1890ff' }}>Workouts: <b>{wk.value}</b></div>
          )}
        </div>
      );
    }
    return null;
  };

  return (
    <>
      <Card title="Weight & Calorie Trends" style={cardStyle} headStyle={headStyle}>
        <ResponsiveContainer width="100%" height={320}>
          <LineChart
            data={data}
            onClick={(state: { activeLabel?: string } | undefined) => {
              const date = state?.activeLabel;
              if (date) {
                navigate('/workout', { state: { date, focus: 'manual' } });
              }
            }}
          >
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="date" />
            <YAxis yAxisId="left" />
            <YAxis yAxisId="right" orientation="right" />
            <Tooltip />
            <Legend />
            <Line yAxisId="left" type="monotone" dataKey="weight" stroke="#8884d8" name="Weight (kg)" />
            <Line yAxisId="right" type="monotone" dataKey="calories" stroke="#22c55e" strokeWidth={2} dot={false} name="Calories (kcal)" />
          </LineChart>
        </ResponsiveContainer>
        <div style={{ marginTop: 8 }}>
          <Text type="secondary">Daily weight line (left axis) with calorie line (right axis)</Text>
        </div>
      </Card>

      <Card title="Weekly Workout Frequency" style={{ ...cardStyle, marginTop: 16 }} headStyle={headStyle}>
        <ResponsiveContainer width="100%" height={320}>
          <BarChart data={data}>
            <defs>
              <linearGradient id="barBlue" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#91caff" />
                <stop offset="100%" stopColor="#1677ff" />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" vertical={false} />
            <XAxis dataKey="date" tickFormatter={formatDate} />
            <YAxis allowDecimals={false} />
            <Tooltip content={<CustomTooltip />} />
            <Bar
              dataKey="workouts"
              fill="url(#barBlue)"
              radius={[6, 6, 0, 0]}
              onClick={(entry: { payload?: ProgressData } | undefined) => {
                const date = entry?.payload?.date;
                if (date) {
                  navigate('/workout', { state: { date, focus: 'ai' } });
                }
              }}
            />
          </BarChart>
        </ResponsiveContainer>
      </Card>
    </>
  );
};

// No helper component — use the simple, reliable dual-line chart above.

export default ProgressCharts;
