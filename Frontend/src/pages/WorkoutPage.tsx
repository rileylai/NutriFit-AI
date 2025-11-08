import { Alert, Card, Col, Row, Space, Typography, message } from 'antd';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useLocation } from 'react-router-dom';
import AiWorkoutForm from '../components/AiWorkoutForm';
import EditWorkoutModal from '../components/EditWorkoutModal';
import type { EditWorkoutFormValues } from '../components/EditWorkoutModal';
import WorkoutForm from '../components/WorkoutForm';
import WorkoutList from '../components/WorkoutList';
import { deleteWorkout, fetchWorkouts, updateWorkout } from '../services/workoutService';
import { useAppSelector } from '../store/hooks';
import type { AiWorkoutSummary } from '../types/AiWorkout';
import type { Workout, WorkoutRequest, WorkoutResponseMessage, WorkoutSortOption } from '../types/Workout';

const { Title, Paragraph } = Typography;

export default function WorkoutPage() {
  const location = useLocation() as { state?: { date?: string; focus?: 'manual' | 'ai' } };
  const user = useAppSelector((state) => state.auth.user);
  // We simply check for a user object; the uuid contained within is our canonical identifier.
  const isAuthenticated = Boolean(user);
  const [activeDate, setActiveDate] = useState<Dayjs | null>(null);
  const [historySort, setHistorySort] = useState<WorkoutSortOption>('date_desc');
  const [todayWorkouts, setTodayWorkouts] = useState<Workout[]>([]);
  const [historyWorkouts, setHistoryWorkouts] = useState<Workout[]>([]);
  const [loadingToday, setLoadingToday] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const [refreshToken, setRefreshToken] = useState(0);
  const [messageApi, contextHolder] = message.useMessage();
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [workoutBeingEdited, setWorkoutBeingEdited] = useState<Workout | null>(null);
  const [updatingWorkout, setUpdatingWorkout] = useState(false);
  const [deletingWorkoutId, setDeletingWorkoutId] = useState<number | null>(null);
  const [aiPrefill, setAiPrefill] = useState<AiWorkoutSummary | null>(null);
  const manualRef = useRef<HTMLDivElement | null>(null);
  const aiRef = useRef<HTMLDivElement | null>(null);

  const effectiveDate = useMemo(() => activeDate ?? dayjs(), [activeDate]);
  const effectiveDateString = useMemo(() => effectiveDate.format('YYYY-MM-DD'), [effectiveDate]);

  const currentDayLabel = useMemo(
    () => (effectiveDate.isSame(dayjs(), 'day') ? 'Today' : effectiveDate.format('MMMM D, YYYY')),
    [effectiveDate],
  );

  // const currentDayDisplayDate = useMemo(
  //   () => effectiveDate.format('dddd, MMM D, YYYY'),
  //   [effectiveDate],
  // );
  const currentDayDisplayDate = useMemo(() => {
    if (effectiveDate.isSame(dayjs(), 'day')) {
      return effectiveDate.format('dddd, MMM D, YYYY'); // e.g., Friday, Sep 26, 2025
    }
    return effectiveDate.format('dddd'); // e.g., Wednesday
  }, [effectiveDate]);
  //

  const triggerRefresh = useCallback(() => {
    setRefreshToken((value) => value + 1);
  }, []);

  // Apply navigation state (date + focus) once on mount
  useEffect(() => {
    const navDate = location.state?.date;
    if (navDate) {
      const parsed = dayjs(navDate);
      if (parsed.isValid()) {
        setActiveDate(parsed);
      }
    }

    const focus = location.state?.focus;
    const el = focus === 'ai' ? aiRef.current : manualRef.current;
    if (el) {
      // Delay to ensure layout is ready
      setTimeout(() => el.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    let active = true;

    const loadData = async () => {
      if (!isAuthenticated) {
        // Show a gentle message instead of firing failing requests when the session is not ready.
        setFetchError('Sign in to view and manage your workouts.');
        setTodayWorkouts([]);
        setHistoryWorkouts([]);
        setLoadingToday(false);
        setLoadingHistory(false);
        return;
      }

      setFetchError(null);
      setLoadingToday(true);
      setLoadingHistory(true);

      try {
        const [currentDayData, historyData] = await Promise.all([
          fetchWorkouts({ workoutDate: effectiveDateString }),
          fetchWorkouts({ sortBy: historySort }),
        ]);

        if (!active) {
          return;
        }

        setTodayWorkouts(currentDayData);
        setHistoryWorkouts(historyData.filter((entry) => entry.workoutDate !== effectiveDateString));
      } catch (error) {
        if (!active) {
          return;
        }

        const messageText = error instanceof Error ? error.message : 'Unable to load workouts.';
        setFetchError(messageText);
        setTodayWorkouts([]);
        setHistoryWorkouts([]);
      } finally {
        if (active) {
          setLoadingToday(false);
          setLoadingHistory(false);
        }
      }
    };

    loadData().catch((error) => {
      if (!active) {
        return;
      }
      console.error('Unexpected error while loading workouts:', error);
      setFetchError('An unexpected error occurred while loading workouts.');
      setLoadingToday(false);
      setLoadingHistory(false);
    });

    return () => {
      active = false;
    };
  }, [effectiveDateString, historySort, isAuthenticated, refreshToken]);

  const handleDateChange = (value: Dayjs | null) => {
    setActiveDate(value);
  };

  const handleSortChange = (sort: WorkoutSortOption) => {
    setHistorySort(sort);
  };

  const handleWorkoutCreated = () => {
    setAiPrefill(null);
    triggerRefresh();
  };

  const handleEditRequest = (workout: Workout) => {
    setWorkoutBeingEdited(workout);
    setEditModalOpen(true);
  };

  const handleDeleteRequest = async (workoutId: number) => {
    setDeletingWorkoutId(workoutId);
    try {
      const response = await deleteWorkout(workoutId);
      if (!response.success) {
        throw new Error(response.message);
      }
      messageApi.success(response.message);
      triggerRefresh();
    } catch (error) {
      const messageText = error instanceof Error ? error.message : 'Failed to delete workout.';
      messageApi.error(messageText);
    } finally {
      setDeletingWorkoutId(null);
    }
  };

  const handleModalClose = () => {
    setEditModalOpen(false);
    setWorkoutBeingEdited(null);
  };

  const handleModalSubmit = async (values: EditWorkoutFormValues) => {
    if (!workoutBeingEdited) {
      return;
    }

    setUpdatingWorkout(true);

    const payload: WorkoutRequest = {
      workoutType: values.workoutType,
      workoutDate: values.workoutDate.format('YYYY-MM-DD'),
      durationMinutes: values.durationMinutes,
      caloriesBurned: values.caloriesBurned,
      notes: values.notes?.trim() ? values.notes.trim() : null,
    };

    try {
      const response: WorkoutResponseMessage = await updateWorkout(workoutBeingEdited.workoutId, payload);
      if (!response.success) {
        throw new Error(response.message);
      }
      messageApi.success(response.message);
      handleModalClose();
      triggerRefresh();
    } catch (error) {
      const messageText = error instanceof Error ? error.message : 'Failed to update workout.';
      messageApi.error(messageText);
    } finally {
      setUpdatingWorkout(false);
    }
  };

  return (
    <>
      {contextHolder}
      <Space direction="vertical" size={24} style={{ width: '100%' }}>
        <Card>
          <Space direction="vertical" size={24} style={{ width: '100%' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: 16 }}>
              <Space direction="vertical" size={8}>
                <Title level={2} style={{ margin: 0 }}>
                  Workout
                </Title>
                <Paragraph style={{ margin: 0 }}>
                  Log workouts manually or let the AI assistant capture them for you.
                </Paragraph>
              </Space>
              {/* <Space direction="vertical" size={4}>
                <Text type="secondary">Select a day to review</Text>
                <DatePicker allowClear format="YYYY-MM-DD" value={activeDate} onChange={handleDateChange} />
              </Space> */}
            </div>

            <Row gutter={[24, 24]}>
              <Col xs={24} md={12}>
                <Card type="inner" title="Manual Input" ref={manualRef}>
                  <Paragraph type="secondary">
                    Enter the workout details below to add a new log entry.
                  </Paragraph>
                  <WorkoutForm onWorkoutCreated={handleWorkoutCreated} aiPrefill={aiPrefill} />
                </Card>
              </Col>
              <Col xs={24} md={12}>
                <Card type="inner" title="AI Auto Input" ref={aiRef}>
                  <Paragraph type="secondary">
                    Describe your workout (e.g. “I ran 40 minutes”) and we&apos;ll create the log.
                  </Paragraph>
                  <AiWorkoutForm
                    onWorkoutLogged={handleWorkoutCreated}
                    onAiSummary={(summary) => {
                      setAiPrefill(summary);
                    }}
                  />
                </Card>
              </Col>
            </Row>

            {fetchError && <Alert type="error" message={fetchError} showIcon />}

            <WorkoutList
              labelForCurrentDay={currentDayLabel}
              currentDayDisplayDate={currentDayDisplayDate}
              todayWorkouts={todayWorkouts}
              historyWorkouts={historyWorkouts}
              loadingToday={loadingToday}
              loadingHistory={loadingHistory}
              sortBy={historySort}
              onSortChange={handleSortChange}
              onEdit={handleEditRequest}
              onDelete={handleDeleteRequest}
              deleteInProgressId={deletingWorkoutId}
              //
              selectedDate={activeDate}
              onDateChange={handleDateChange}
              //
            />
          </Space>
        </Card>
      </Space>

      <EditWorkoutModal
        open={editModalOpen}
        workout={workoutBeingEdited}
        onCancel={handleModalClose}
        onSubmit={handleModalSubmit}
        confirmLoading={updatingWorkout}
      />
    </>
  );
}
