import React from 'react';
import { Row, Col, Card, Statistic, Typography, Tag, Popover } from 'antd';
import {
  UserOutlined,
  FireOutlined,
  TrophyOutlined,
  ThunderboltOutlined,
  RiseOutlined,
  FallOutlined,
  LineOutlined,
  HeartOutlined,
  DashboardOutlined
} from '@ant-design/icons';
import type {
  UserMetrics,
  DailyIntake,
  DailyTargets,
  WeeklyAverages,
  MonthlyAverages,
  WorkoutFrequency,
  Streaks,
  DashboardPeriod
} from '../../types/HomePage';

const { Text } = Typography;

interface QuickStatsProps {
  userMetrics: UserMetrics;
  currentIntake: DailyIntake;
  dailyTargets: DailyTargets;
  selectedPeriod: DashboardPeriod;
  activeStreak: number;
  weeklyWorkouts: number;
  weeklyWorkoutGoal: number;
  weeklyAverages?: WeeklyAverages;
  monthlyAverages?: MonthlyAverages;
  workoutFrequency?: WorkoutFrequency;
  streaks?: Streaks;
}

const QuickStats: React.FC<QuickStatsProps> = ({
  userMetrics,
  selectedPeriod,
  activeStreak,
  weeklyWorkouts: periodWorkouts,
  weeklyWorkoutGoal,
  weeklyAverages,
  monthlyAverages,
  workoutFrequency,
  streaks,
}) => {
  const periodLabel = selectedPeriod === 'weekly' ? 'Week' : 'Month';
  const averagesLabel = selectedPeriod === 'weekly' ? 'Weekly' : 'Monthly';
  const selectedAverages = selectedPeriod === 'weekly' ? weeklyAverages : monthlyAverages;
  const totalCaloriesBurned = selectedAverages?.totalCaloriesBurned ?? 0;
  const avgCaloriesBurned = selectedAverages?.avgCaloriesBurned;

  const getWeightTrendIcon = (trend: string) => {
    if (trend === 'up') return <RiseOutlined style={{ color: '#ff4d4f' }} />;
    if (trend === 'down') return <FallOutlined style={{ color: '#52c41a' }} />;
    return <LineOutlined style={{ color: '#8c8c8c' }} />;
  };

  const getStreakColor = (status: string): string => {
    if (status === 'excellent' || status === 'great') return 'success';
    if (status === 'good') return 'warning';
    return 'default';
  };

  const weightDetails = (
    <div style={{ maxWidth: 220 }}>
      <Text type="secondary">BMI: {userMetrics.bmi.toFixed(2)}</Text>
      <br />
      <Text type="secondary">BMR: {Math.round(userMetrics.bmr)} kcal</Text>
      <br />
      <Text type="secondary">Height: {userMetrics.height} cm</Text>
      {userMetrics.weightChange !== 0 && (
        <div style={{ marginTop: '4px', display: 'flex', alignItems: 'center' }}>
          {getWeightTrendIcon(userMetrics.weightTrend)}
          <Text type="secondary" style={{ marginLeft: '4px' }}>
            {userMetrics.weightChange > 0 ? '+' : ''}
            {userMetrics.weightChange.toFixed(1)} kg
          </Text>
        </div>
      )}
    </div>
  );

  return (
    <>
      {/* Primary Stats Row */}
      <Row gutter={[16, 16]} style={{ marginBottom: '16px' }}>
        <Col xs={24} sm={12} md={6}>
          <Popover content={weightDetails} placement="right">
            <Card hoverable>
              <Statistic
                title="Current Weight"
                value={userMetrics.weight}
                suffix="kg"
                prefix={<UserOutlined />}
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Popover>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Popover
            placement="right"
            content={
              avgCaloriesBurned !== undefined ? (
                <div style={{ maxWidth: 220 }}>
                  <Text type="secondary">
                    ~{avgCaloriesBurned.toFixed(0)} avg kcal burned
                  </Text>
                </div>
              ) : null
            }
          >
            <Card hoverable>
              <Statistic
                title="Total Calories Burned"
                value={totalCaloriesBurned}
                suffix="kcal"
                prefix={<FireOutlined />}
                valueStyle={{ color: '#cf1322' }}
              />
            </Card>
          </Popover>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="Workout Streak"
              value={streaks?.workoutStreak || activeStreak}
              suffix="days"
              prefix={<TrophyOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
            {streaks && (
              <div style={{ marginTop: '8px' }}>
                <Tag color={getStreakColor(streaks.workoutStreakStatus)}>
                  {streaks.workoutStreakStatus || 'Keep going!'}
                </Tag>
              </div>
            )}
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title={`This ${periodLabel}`}
              value={periodWorkouts}
              suffix="workouts"
              prefix={<ThunderboltOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
            <Text type="secondary">Goal: {weeklyWorkoutGoal} per week</Text>
          </Card>
        </Col>
      </Row>

      {/* Additional Stats Row */}
      {(selectedAverages || workoutFrequency || streaks) && (
        <Row gutter={[16, 16]}>
          {selectedAverages && (
            <Col xs={24} sm={12} md={6}>
              <Popover
                placement="right"
                content={
                  <div style={{ maxWidth: 220 }}>
                    <Text type="secondary" style={{ fontSize: '12px' }}>
                      P: {selectedAverages.avgMacros.protein.toFixed(0)}g | C: {selectedAverages.avgMacros.carbs.toFixed(0)}g | F: {selectedAverages.avgMacros.fats.toFixed(0)}g
                    </Text>
                    {(selectedAverages.totalCaloriesIntake !== undefined ||
                      selectedAverages.workoutCount !== undefined) && (
                        <div style={{ marginTop: '4px' }}>
                          {selectedAverages.totalCaloriesIntake !== undefined && (
                            <Text type="secondary" style={{ fontSize: '12px' }}>
                              Total intake: {selectedAverages.totalCaloriesIntake.toLocaleString()} kcal
                            </Text>
                          )}
                          {selectedAverages.workoutCount !== undefined && (
                            <div>
                              <Text type="secondary" style={{ fontSize: '12px' }}>
                                Logged workouts: {selectedAverages.workoutCount}
                              </Text>
                            </div>
                          )}
                        </div>
                    )}
                  </div>
                }
              >
                <Card hoverable>
                  <Statistic
                    title={`${averagesLabel} Avg Calories`}
                    value={selectedAverages.avgCaloriesIntake}
                    suffix="kcal"
                    prefix={<DashboardOutlined />}
                    valueStyle={{ color: '#fa8c16' }}
                    precision={0}
                  />
                </Card>
              </Popover>
            </Col>
          )}

          {selectedAverages && (
            <Col xs={24} sm={12} md={6}>
              <Popover
                placement="right"
                content={
                  <div style={{ maxWidth: 220 }}>
                    {selectedAverages.totalWorkoutDuration !== undefined && (
                      <div>
                        <Text type="secondary" style={{ fontSize: '12px' }}>
                          Total duration: {selectedAverages.totalWorkoutDuration} min
                        </Text>
                      </div>
                    )}
                  </div>
                }
              >
                <Card hoverable>
                  <Statistic
                    title={`${averagesLabel} Avg Workout`}
                    value={selectedAverages.avgWorkoutDuration}
                    suffix="min"
                    prefix={<HeartOutlined />}
                    valueStyle={{ color: '#eb2f96' }}
                    precision={0}
                  />
                </Card>
              </Popover>
            </Col>
          )}

          {workoutFrequency && (
            <Col xs={24} sm={12} md={6}>
              <Card>
                <Statistic
                  title="Workout Frequency"
                  value={workoutFrequency.frequencyPercentage}
                  suffix="%"
                  prefix={<ThunderboltOutlined />}
                  valueStyle={{ color: '#13c2c2' }}
                  precision={1}
                />
                <div style={{ marginTop: '8px' }}>
                  <Text type="secondary">
                    {workoutFrequency.workoutDays}/{workoutFrequency.totalDays} days
                  </Text>
                </div>
              </Card>
            </Col>
          )}

          {streaks && (
            <Col xs={24} sm={12} md={6}>
              <Card>
                <Statistic
                  title="Nutrition Streak"
                  value={streaks.nutritionStreak}
                  suffix="days"
                  prefix={<FireOutlined />}
                  valueStyle={{ color: '#52c41a' }}
                />
                <div style={{ marginTop: '8px' }}>
                  <Tag color={getStreakColor(streaks.nutritionStreakStatus)}>
                    {streaks.nutritionStreakStatus || 'Start tracking!'}
                  </Tag>
                </div>
              </Card>
            </Col>
          )}
        </Row>
      )}
    </>
  );
};

export default QuickStats;
