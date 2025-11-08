import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Col,
  DatePicker,
  Empty,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { TablePaginationConfig } from 'antd/es/table/interface';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { EditOutlined, ReloadOutlined } from '@ant-design/icons';
import {
  createMetrics,
  fetchLatestMetrics,
  fetchMetricsHistory,
  fetchUserProfile,
  updateMetrics,
  upsertUserProfile,
} from '../services/profileService';
import type { PageResponse, UserMetrics, UserProfile } from '../types/Profile';
import { useAppSelector } from '../store/hooks';

type ProfileFormValues = {
  heightCm?: number | null;
  weightKg?: number | null;
  userGoal?: string | null;
  recordAt?: Dayjs | null;
};

type HistoryParams = {
  page: number;
  size: number;
  startDate?: string;
  endDate?: string;
};

type UserProfileFormValues = {
  birthDate?: Dayjs | null;
  gender?: string | null;
};

const { Title, Paragraph, Text } = Typography;
const { RangePicker } = DatePicker;
const genderOptions = [
  { label: 'Male', value: 'MALE' },
  { label: 'Female', value: 'FEMALE' },
  { label: 'Other / Prefer not to say', value: 'OTHER' },
];

const formatNumber = (value: number | null | undefined, digits: number) => {
  if (typeof value !== 'number' || !Number.isFinite(value)) {
    return '—';
  }
  return value.toFixed(digits);
};

const formatInteger = (value: number | null | undefined) => {
  if (typeof value !== 'number' || !Number.isFinite(value)) {
    return '—';
  }
  return Math.round(value).toString();
};

const formatDateTime = (value: string | null | undefined) => {
  if (!value) {
    return '—';
  }
  const parsed = dayjs(value);
  if (!parsed.isValid()) {
    return '—';
  }
  return parsed.format('MMM D, YYYY HH:mm');
};

const normalizeGenderValue = (value: string | null | undefined): string | undefined => {
  if (!value) {
    return undefined;
  }
  const normalized = value.trim().toUpperCase();
  switch (normalized) {
    case 'M':
    case 'MALE':
      return 'MALE';
    case 'F':
    case 'FEMALE':
      return 'FEMALE';
    case 'OTHER':
      return 'OTHER';
    default:
      return normalized;
  }
};

// const formatGenderLabel = (value: string | null | undefined) => {
//   if (!value) {
//     return '—';
//   }
//   const normalized = value.trim().toUpperCase();
//   switch (normalized) {
//     case 'M':
//     case 'MALE':
//       return 'Male';
//     case 'F':
//     case 'FEMALE':
//       return 'Female';
//     case 'OTHER':
//       return 'Other';
//     default:
//       return normalized.charAt(0) + normalized.slice(1).toLowerCase();
//   }
// };

const getRecordMoment = (metrics: UserMetrics | null): Dayjs => {
  if (!metrics) {
    return dayjs();
  }
  const recordSource = metrics.recordAt ?? metrics.createdAt;
  const parsed = recordSource ? dayjs(recordSource) : dayjs();
  return parsed.isValid() ? parsed : dayjs();
};

const ProfilePage = () => {
  const [form] = Form.useForm<ProfileFormValues>();
  const [profileForm] = Form.useForm<UserProfileFormValues>();
  const [messageApi, contextHolder] = message.useMessage();
  const token = useAppSelector((state) => state.auth.token);
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [latestMetrics, setLatestMetrics] = useState<UserMetrics | null>(null);
  const [loadingLatest, setLoadingLatest] = useState(false);
  const [latestError, setLatestError] = useState<string | null>(null);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyError, setHistoryError] = useState<string | null>(null);
  const [historyParams, setHistoryParams] = useState<HistoryParams>({ page: 1, size: 10 });
  const [history, setHistory] = useState<PageResponse<UserMetrics> | null>(null);
  const [saving, setSaving] = useState(false);
  const [editingMetricId, setEditingMetricId] = useState<number | null>(null);
  const [editingMetricSnapshot, setEditingMetricSnapshot] = useState<UserMetrics | null>(null);
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [profileSaving, setProfileSaving] = useState(false);

  const loadLatestMetrics = useCallback(async () => {
    setLoadingLatest(true);
    setLatestError(null);
    try {
      const metrics = await fetchLatestMetrics();
      setLatestMetrics(metrics);

      if (metrics) {
        setEditingMetricId(metrics.metricId);
        setEditingMetricSnapshot(metrics);
        form.setFieldsValue({
          heightCm: metrics.heightCm ?? undefined,
          weightKg: metrics.weightKg ?? undefined,
          userGoal: metrics.userGoal ?? undefined,
          recordAt: getRecordMoment(metrics),
        });
      } else {
        setEditingMetricId(null);
        setEditingMetricSnapshot(null);
        form.resetFields();
        form.setFieldsValue({ recordAt: dayjs() });
      }
    } catch (error) {
      const messageText = error instanceof Error ? error.message : 'Failed to load profile metrics.';
      setLatestError(messageText);
      messageApi.error(messageText);
    } finally {
      setLoadingLatest(false);
    }
  }, [form, messageApi]);

  const loadUserProfile = useCallback(async () => {
    setProfileLoading(true);
    setProfileError(null);
    try {
      const profile = await fetchUserProfile();
      setUserProfile(profile);

      if (profile) {
        profileForm.setFieldsValue({
          birthDate: profile.birthDate ? dayjs(profile.birthDate) : null,
          gender: normalizeGenderValue(profile.gender),
        });
      } else {
        profileForm.resetFields();
      }
    } catch (error) {
      const messageText = error instanceof Error ? error.message : 'Failed to load user profile.';
      setProfileError(messageText);
      messageApi.error(messageText);
    } finally {
      setProfileLoading(false);
    }
  }, [messageApi, profileForm]);

  const handleProfileRefresh = useCallback(() => {
    loadUserProfile().catch((error) => {
      console.error('Unexpected error loading user profile:', error);
    });
  }, [loadUserProfile]);

  const handleProfileSubmit = async (values: UserProfileFormValues) => {
    const payload = {
      birthDate: values.birthDate && values.birthDate.isValid() ? values.birthDate.format('YYYY-MM-DD') : null,
      gender: values.gender ? values.gender.toUpperCase() : null,
    };

    setProfileSaving(true);
    setProfileError(null);
    try {
      const updatedProfile = await upsertUserProfile(payload);
      setUserProfile(updatedProfile);
      profileForm.setFieldsValue({
        birthDate: updatedProfile.birthDate ? dayjs(updatedProfile.birthDate) : null,
        gender: normalizeGenderValue(updatedProfile.gender),
      });
      messageApi.success('Profile updated successfully.');
    } catch (error) {
      const messageText = error instanceof Error ? error.message : 'Failed to update user profile.';
      setProfileError(messageText);
      messageApi.error(messageText);
    } finally {
      setProfileSaving(false);
    }
  };

  const loadHistory = useCallback(async () => {
    setHistoryLoading(true);
    setHistoryError(null);
    try {
      const response = await fetchMetricsHistory({
        page: historyParams.page,
        size: historyParams.size,
        startDate: historyParams.startDate,
        endDate: historyParams.endDate,
        sortBy: 'createdAt',
        sortDirection: 'DESC',
      });
      setHistory(response);
    } catch (error) {
      const messageText = error instanceof Error ? error.message : 'Failed to load metrics history.';
      setHistoryError(messageText);
      messageApi.error(messageText);
    } finally {
      setHistoryLoading(false);
    }
  }, [historyParams, messageApi]);

  useEffect(() => {
    if (!token) {
      return;
    }
    loadUserProfile().catch((error) => {
      console.error('Unexpected error loading user profile:', error);
    });
  }, [loadUserProfile, token]);

  useEffect(() => {
    if (!token) {
      return;
    }
    loadLatestMetrics().catch((error) => {
      console.error('Unexpected error loading latest metrics:', error);
    });
  }, [loadLatestMetrics, token]);

  useEffect(() => {
    if (!token) {
      return;
    }
    loadHistory().catch((error) => {
      console.error('Unexpected error loading history:', error);
    });
  }, [loadHistory, token]);

  const rangeValue = useMemo<[Dayjs | null, Dayjs | null]>(() => {
    const start = historyParams.startDate ? dayjs(historyParams.startDate) : null;
    const end = historyParams.endDate ? dayjs(historyParams.endDate) : null;
    return [start && start.isValid() ? start : null, end && end.isValid() ? end : null];
  }, [historyParams]);

  const handleTableChange = (pagination: TablePaginationConfig) => {
    if (!pagination) {
      return;
    }
    setHistoryParams((prev) => ({
      ...prev,
      page: pagination.current ?? prev.page,
      size: pagination.pageSize ?? prev.size,
    }));
  };

  const handleRangeChange = (dates: [Dayjs | null, Dayjs | null] | null) => {
    setHistoryParams((prev) => ({
      ...prev,
      page: 1,
      startDate: dates && dates[0] ? dates[0].format('YYYY-MM-DD') : undefined,
      endDate: dates && dates[1] ? dates[1].format('YYYY-MM-DD') : undefined,
    }));
  };

  const handleResetFilters = () => {
    setHistoryParams((prev) => ({
      ...prev,
      page: 1,
      startDate: undefined,
      endDate: undefined,
    }));
  };

  const handleStartNewEntry = useCallback(() => {
    setEditingMetricId(null);
    setEditingMetricSnapshot(null);
    form.resetFields();
    form.setFieldsValue({ recordAt: dayjs() });
  }, [form]);

  const handleLoadLatestEntry = useCallback(() => {
    if (!latestMetrics) {
      return;
    }
    setEditingMetricId(latestMetrics.metricId);
    setEditingMetricSnapshot(latestMetrics);
    form.setFieldsValue({
      heightCm: latestMetrics.heightCm ?? undefined,
      weightKg: latestMetrics.weightKg ?? undefined,
      userGoal: latestMetrics.userGoal ?? undefined,
      recordAt: getRecordMoment(latestMetrics),
    });
    messageApi.info('Loaded the most recent metrics entry.');
  }, [form, latestMetrics, messageApi]);

  const handleEditEntry = useCallback(
    (entry: UserMetrics) => {
      setEditingMetricId(entry.metricId);
      setEditingMetricSnapshot(entry);
      form.setFieldsValue({
        heightCm: entry.heightCm ?? undefined,
        weightKg: entry.weightKg ?? undefined,
        userGoal: entry.userGoal ?? undefined,
        recordAt: getRecordMoment(entry),
      });
      messageApi.success('Metrics entry loaded into the form.');
    },
    [form, messageApi],
  );

  const handleSubmit = async (values: ProfileFormValues) => {
    const payload = {
      heightCm: values.heightCm ?? null,
      weightKg: values.weightKg ?? null,
      age: editingMetricSnapshot?.age ?? null,
      gender: normalizeGenderValue(editingMetricSnapshot?.gender) ?? null,
      userGoal: values.userGoal && values.userGoal.trim().length > 0 ? values.userGoal.trim() : null,
      recordAt: values.recordAt ? values.recordAt.toISOString() : null,
    };

    setSaving(true);
    try {
      if (editingMetricId) {
        await updateMetrics(editingMetricId, payload);
        messageApi.success('Metrics updated successfully.');
      } else {
        await createMetrics(payload);
        messageApi.success('Metrics recorded successfully.');
      }

      await loadLatestMetrics();
      setHistoryParams((prev) => ({
        ...prev,
        page: 1,
      }));
    } catch (error) {
      const messageText = error instanceof Error ? error.message : 'Failed to save metrics.';
      messageApi.error(messageText);
    } finally {
      setSaving(false);
    }
  };

  const historyColumns: ColumnsType<UserMetrics> = useMemo(
    () => [
      {
        title: 'Recorded At',
        dataIndex: 'recordAt',
        key: 'recordAt',
        render: (value: string | null | undefined, record) => (
          <Space size={8}>
            <span>{formatDateTime(value ?? record.createdAt)}</span>
            {latestMetrics?.metricId === record.metricId && <Tag color="blue">Latest</Tag>}
          </Space>
        ),
      },
      {
        title: 'Height (cm)',
        dataIndex: 'heightCm',
        key: 'heightCm',
        align: 'right',
        render: (value: number | null | undefined) => (
          <Text>{formatNumber(value, 1)}</Text>
        ),
      },
      {
        title: 'Weight (kg)',
        dataIndex: 'weightKg',
        key: 'weightKg',
        align: 'right',
        render: (value: number | null | undefined) => (
          <Text>{formatNumber(value, 1)}</Text>
        ),
      },
      {
        title: 'BMI',
        dataIndex: 'bmi',
        key: 'bmi',
        align: 'right',
        render: (value: number | null | undefined) => (
          <Text>{formatNumber(value, 1)}</Text>
        ),
      },
      {
        title: 'BMR',
        dataIndex: 'bmr',
        key: 'bmr',
        align: 'right',
        render: (value: number | null | undefined) => (
          <Text>{formatInteger(value)}</Text>
        ),
      },
      {
        title: 'Goal',
        dataIndex: 'userGoal',
        key: 'userGoal',
        ellipsis: true,
        render: (value: string | null | undefined) => (value && value.trim().length > 0 ? value : '—'),
      },
      {
        title: 'Actions',
        key: 'actions',
        render: (_: unknown, record) => (
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEditEntry(record)}>
            Use Entry
          </Button>
        ),
      },
    ],
    [handleEditEntry, latestMetrics],
  );

  return (
    <>
      {contextHolder}
      <Space direction="vertical" size={24} style={{ width: '100%' }}>
        <Card>
          <Space direction="vertical" size={24} style={{ width: '100%' }}>
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'flex-start',
                flexWrap: 'wrap',
                gap: 16,
              }}
            >
              <Space direction="vertical" size={4}>
                <Title level={2} style={{ margin: 0 }}>
                  Profile
                </Title>
                <Paragraph style={{ margin: 0 }}>
                  Record your physical metrics and track how they change over time.
                </Paragraph>
              </Space>
              <Space>
                <Button icon={<ReloadOutlined />} onClick={() => loadLatestMetrics()}>
                  Refresh Latest
                </Button>
                <Button onClick={handleStartNewEntry}>Start New Entry</Button>
                <Button type="primary" icon={<EditOutlined />} onClick={handleLoadLatestEntry} disabled={!latestMetrics}>
                  Load Latest Entry
                </Button>
              </Space>
            </div>

            <Card
              type="inner"
              title="User Profile"
              loading={profileLoading}
              extra={
                <Button icon={<ReloadOutlined />} onClick={handleProfileRefresh} disabled={profileLoading || profileSaving}>
                  Refresh Profile
                </Button>
              }
            >
              <Space direction="vertical" size={16} style={{ width: '100%' }}>
                {profileError && <Alert type="error" message={profileError} showIcon />}
                {!profileError && !profileLoading && !userProfile && (
                  <Alert type="info" message="No profile information saved yet." showIcon />
                )}

                <Form form={profileForm} layout="vertical" onFinish={handleProfileSubmit}>
                  <Row gutter={16}>
                    <Col xs={24} sm={12}>
                      <Form.Item
                        name="birthDate"
                        label="Birth Date"
                        rules={[
                          {
                            validator: async (_, value: Dayjs | null | undefined) => {
                              if (!value) {
                                return Promise.resolve();
                              }
                              if (!value.isValid()) {
                                return Promise.reject(new Error('Please select a valid date.'));
                              }
                              if (value.isAfter(dayjs(), 'day')) {
                                return Promise.reject(new Error('Birth date must be in the past.'));
                              }
                              return Promise.resolve();
                            },
                          },
                        ]}
                      >
                        <DatePicker
                          allowClear
                          format="YYYY-MM-DD"
                          style={{ width: '100%' }}
                          disabledDate={(current) => !!current && current.isAfter(dayjs(), 'day')}
                        />
                      </Form.Item>
                    </Col>
                    <Col xs={24} sm={12}>
                      <Form.Item name="gender" label="Gender">
                        <Select
                          placeholder="Select gender"
                          allowClear
                          options={genderOptions}
                          showSearch
                          optionFilterProp="label"
                        />
                      </Form.Item>
                    </Col>
                  </Row>

                  <Form.Item>
                    <Space>
                      <Button type="primary" htmlType="submit" loading={profileSaving}>
                        Update Profile
                      </Button>
                      <Button
                        onClick={() => {
                          profileForm.resetFields();
                          setProfileError(null);
                        }}
                        disabled={profileSaving || profileLoading}
                      >
                        Clear
                      </Button>
                    </Space>
                  </Form.Item>
                </Form>
              </Space>
            </Card>

            <Row gutter={[24, 24]}>
              <Col xs={24} lg={12}>
                <Card type="inner" title={editingMetricId ? 'Update Metrics' : 'Create Metrics'}>
                  <Form
                    form={form}
                    layout="vertical"
                    onFinish={handleSubmit}
                    initialValues={{ recordAt: dayjs() }}
                  >
                    <Row gutter={16}>
                      <Col span={12}>
                        <Form.Item
                          name="heightCm"
                          label="Height (cm)"
                          rules={[
                            { required: true, message: 'Please enter your height.' },
                            { type: 'number', min: 1, message: 'Height must be greater than zero.' },
                          ]}
                        >
                          <InputNumber
                            placeholder="e.g. 170"
                            min={1}
                            step={0.5}
                            style={{ width: '100%' }}
                          />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item
                          name="weightKg"
                          label="Weight (kg)"
                          rules={[
                            { required: true, message: 'Please enter your weight.' },
                            { type: 'number', min: 1, message: 'Weight must be greater than zero.' },
                          ]}
                        >
                          <InputNumber
                            placeholder="e.g. 65"
                            min={1}
                            step={0.5}
                            style={{ width: '100%' }}
                          />
                        </Form.Item>
                      </Col>
                    </Row>

                    <Form.Item name="userGoal" label="Goal">
                      <Input.TextArea
                        placeholder="Describe your current goal (e.g. lose weight, build muscle)"
                        autoSize={{ minRows: 3, maxRows: 4 }}
                        maxLength={200}
                        showCount
                      />
                    </Form.Item>

                    <Form.Item name="recordAt" label="Record Time">
                      <DatePicker
                        style={{ width: '100%' }}
                        showTime
                        allowClear
                        format="YYYY-MM-DD HH:mm"
                      />
                    </Form.Item>

                    <Form.Item>
                      <Space>
                        <Button type="primary" htmlType="submit" loading={saving}>
                          {editingMetricId ? 'Update Metrics' : 'Save Metrics'}
                        </Button>
                        <Button onClick={handleStartNewEntry} disabled={saving}>
                          Clear Form
                        </Button>
                      </Space>
                    </Form.Item>
                  </Form>
                </Card>
              </Col>

              <Col xs={24} lg={12}>
                <Card type="inner" title="Latest Metrics" loading={loadingLatest}>
                  {latestError && (
                    <Alert
                      type="error"
                      message={latestError}
                      showIcon
                      style={{ marginBottom: 16 }}
                    />
                  )}

                  {!loadingLatest && !latestMetrics && !latestError && (
                    <Empty description="No metrics recorded yet." />
                  )}

                  {!loadingLatest && latestMetrics && (
                    <Space direction="vertical" size={16} style={{ width: '100%' }}>
                      <div>
                        <Text type="secondary">Recorded</Text>
                        <div style={{ fontSize: 16, fontWeight: 500 }}>
                          {formatDateTime(latestMetrics.recordAt ?? latestMetrics.createdAt)}
                        </div>
                      </div>

                      <Row gutter={[16, 16]}>
                        <Col span={12}>
                          <Space direction="vertical" size={4}>
                            <Text type="secondary">Height</Text>
                            <Text style={{ fontSize: 16 }}>{formatNumber(latestMetrics.heightCm, 1)} cm</Text>
                          </Space>
                        </Col>
                        <Col span={12}>
                          <Space direction="vertical" size={4}>
                            <Text type="secondary">Weight</Text>
                            <Text style={{ fontSize: 16 }}>{formatNumber(latestMetrics.weightKg, 1)} kg</Text>
                          </Space>
                        </Col>

                      </Row>

                      <Space size={[8, 8]} wrap>
                        {typeof latestMetrics.bmi === 'number' && Number.isFinite(latestMetrics.bmi) && (
                          <Tag color="blue">BMI: {formatNumber(latestMetrics.bmi, 1)}</Tag>
                        )}
                        {typeof latestMetrics.bmr === 'number' && Number.isFinite(latestMetrics.bmr) && (
                          <Tag color="purple">BMR: {formatInteger(latestMetrics.bmr)} kcal</Tag>
                        )}
                        {latestMetrics.userGoal && latestMetrics.userGoal.trim().length > 0 && (
                          <Tag color="green">{latestMetrics.userGoal}</Tag>
                        )}
                      </Space>
                    </Space>
                  )}
                </Card>
              </Col>
            </Row>
          </Space>
        </Card>

        <Card title="Metrics History">
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <Space wrap>
              <RangePicker
                value={rangeValue}
                onChange={handleRangeChange}
                allowClear
                format="YYYY-MM-DD"
              />
              <Button onClick={handleResetFilters}>Reset Filters</Button>
              <Button icon={<ReloadOutlined />} onClick={() => loadHistory()}>
                Refresh History
              </Button>
            </Space>

            {historyError && <Alert type="error" message={historyError} showIcon />}

            <Table<UserMetrics>
              dataSource={history?.data ?? []}
              columns={historyColumns}
              loading={historyLoading}
              rowKey={(record) => String(record.metricId)}
              pagination={{
                current: history?.currentPage ?? historyParams.page,
                pageSize: history?.pageSize ?? historyParams.size,
                total: history?.totalItems ?? 0,
                showSizeChanger: true,
                pageSizeOptions: ['5', '10', '20', '50'],
              }}
              onChange={handleTableChange}
              locale={{
                emptyText: historyLoading ? 'Loading...' : 'No history records found.',
              }}
            />
          </Space>
        </Card>
      </Space>
    </>
  );
};

export default ProfilePage;
