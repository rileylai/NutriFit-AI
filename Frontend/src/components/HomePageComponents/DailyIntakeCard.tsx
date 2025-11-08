import React, { useState } from 'react';
import { Card, Row, Typography, Divider } from 'antd';
import { NavLink } from 'react-router-dom';
import { PlusOutlined } from '@ant-design/icons';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';
import HoverProgress from '../common/HoverProgress';
import type { DailyIntake, DailyTargets } from '../../types/HomePage';

const { Text, Title } = Typography;

interface DailyIntakeCardProps {
  currentIntake: DailyIntake;
  dailyTargets: DailyTargets;
  onAddMeal: () => void;
}

const DailyIntakeCard: React.FC<DailyIntakeCardProps> = ({
  currentIntake,
  dailyTargets,
  onAddMeal,
}) => {
  const [hoveringDonut, setHoveringDonut] = useState(false);
  // Consistent palette across the app
  const macroPalette = {
    Protein: { base: '#22c55e', light: '#86efac' }, // green
    Carbs: { base: '#3b82f6', light: '#93c5fd' },   // blue
    Fat: { base: '#f59e0b', light: '#fcd34d' },     // amber
  } as const;

  const macroData = [
    { name: 'Protein', value: currentIntake.protein, target: dailyTargets.protein, color: macroPalette.Protein.base },
    { name: 'Carbs', value: currentIntake.carbs, target: dailyTargets.carbs, color: macroPalette.Carbs.base },
    { name: 'Fat', value: currentIntake.fat, target: dailyTargets.fat, color: macroPalette.Fat.base },
  ];

  const calculateProgress = (current: number, target: number): number => {
    return Math.min((current / Math.max(target, 1)) * 100, 100);
  };

  const totalMacros = macroData.reduce((sum, m) => sum + m.value, 0);

  // Customized tooltip for macro donut
  const MacroTooltip: React.FC<{ active?: boolean; payload?: any[] }> = ({ active, payload }) => {
    if (active && payload && payload.length) {
      const p = payload[0];
      const name: 'Protein' | 'Carbs' | 'Fat' = p?.name;
      const value: number = p?.value ?? 0;
      const color = macroPalette[name]?.base ?? '#8884d8';
      const percent = totalMacros > 0 ? Math.round((value / totalMacros) * 100) : 0;
      return (
        <div style={{ background: '#fff', border: '1px solid #eee', padding: 10, borderRadius: 8 }}>
          <div style={{ fontWeight: 600, color }}>{name}</div>
          <div style={{ marginTop: 4 }}>{value.toFixed(0)} g ({percent}%)</div>
        </div>
      );
    }
    return null;
  };

  return (
    <Card
      title="Daily Intake Progress"
      extra={
        <NavLink
          to="/meal"
          onClick={onAddMeal}
          style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}
        >
          <PlusOutlined />
          Add Meal
        </NavLink>
      }
      style={{ borderRadius: 12, boxShadow: '0 4px 14px rgba(0,0,0,0.06)' }}
      headStyle={{ borderBottom: '1px solid #f0f0f0' }}
      bodyStyle={{ paddingTop: 16 }}
    >
      <div style={{ marginBottom: '24px' }}>
        {macroData.map((macro) => {
          const progress = calculateProgress(macro.value, macro.target);
          return (
            <div key={macro.name} style={{ marginBottom: '16px' }}>
              <Row justify="space-between" align="middle" style={{ marginBottom: '8px' }}>
                <Text strong>{macro.name}</Text>
                <Text>
                  {macro.value}g / {macro.target}g
                </Text>
              </Row>
              <HoverProgress
                percent={progress}
                colors={{
                  from:
                    macro.name === 'Protein'
                      ? '#b7eb8f'
                      : macro.name === 'Carbs'
                        ? '#91caff'
                        : '#ffe58f',
                  to: macro.color,
                }}
                decimals={2}
                size="default"
                showInfo={false}
              />
              <Row justify="end">
                <Text type="secondary">{progress.toFixed(2)}%</Text>
              </Row>
            </div>
          );
        })}
      </div>
      
      <Divider />
      
      <div style={{ textAlign: 'center', position: 'relative' }}>
        <Title level={5}>Macro Distribution</Title>
        <ResponsiveContainer width="100%" height={240}>
          <PieChart>
            <defs>
              <linearGradient id="gradProtein" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor={macroPalette.Protein.light} />
                <stop offset="100%" stopColor={macroPalette.Protein.base} />
              </linearGradient>
              <linearGradient id="gradCarbs" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor={macroPalette.Carbs.light} />
                <stop offset="100%" stopColor={macroPalette.Carbs.base} />
              </linearGradient>
              <linearGradient id="gradFat" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor={macroPalette.Fat.light} />
                <stop offset="100%" stopColor={macroPalette.Fat.base} />
              </linearGradient>
            </defs>

            <Pie
              data={macroData}
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={90}
              cornerRadius={8}
              paddingAngle={2}
              startAngle={90}
              endAngle={-270}
              dataKey="value"
              labelLine={false}
              // Show name + percentage on the ring
              label={({ name, percent }) => `${name} ${((percent as number) * 100).toFixed(0)}%`}
              onMouseEnter={() => setHoveringDonut(true)}
              onMouseLeave={() => setHoveringDonut(false)}
            >
              <Cell key="protein" fill="url(#gradProtein)" />
              <Cell key="carbs" fill="url(#gradCarbs)" />
              <Cell key="fat" fill="url(#gradFat)" />
            </Pie>
            <Tooltip content={<MacroTooltip />} />
          </PieChart>
        </ResponsiveContainer>
        {/* Center label for donut */}
        <div
          style={{
            position: 'absolute',
            left: '50%',
            top: '50%',
            transform: 'translate(-50%, -50%)',
            pointerEvents: 'none',
            textAlign: 'center',
            opacity: hoveringDonut ? 0 : 1,
            transition: 'opacity 120ms ease',
          }}
        >
          <div style={{ fontSize: 12, color: '#999' }}>Total macros</div>
          <div style={{ fontWeight: 600 }}>{totalMacros} g</div>
        </div>
      </div>

      {/* Calories intake summary below donut */}
      <div
        style={{
          marginTop: 16,
          background: '#fafafa',
          border: '1px solid #f0f0f0',
          borderRadius: 10,
          padding: '12px 14px',
        }}
      >
        <Row justify="space-between" align="middle" style={{ marginBottom: 8 }}>
          <Text strong>Calories Intake</Text>
          <Text type="secondary">{currentIntake.calories} / {dailyTargets.calories} kcal</Text>
        </Row>
        <HoverProgress
          percent={calculateProgress(currentIntake.calories, dailyTargets.calories)}
          colors={{ from: '#d1fae5', to: '#10b981' }}
          decimals={0}
          size="default"
          showInfo={false}
        />
      </div>
    </Card>
  );
};

export default DailyIntakeCard;
