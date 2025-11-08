import { LockOutlined, MailOutlined } from '@ant-design/icons';
import { Button, Card, Form, Input, Space, Typography, message } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import type { Location } from 'react-router-dom';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { loginUser } from '../services/authService';
import { setCredentials } from '../store/auth/authSlice';
import { useAppDispatch, useAppSelector } from '../store/hooks';
import type { LoginRequest } from '../types/Auth';

const { Title, Text } = Typography;

type LoginFormValues = LoginRequest;

export default function LoginPage() {
  const [form] = Form.useForm<LoginFormValues>();
  const [submitting, setSubmitting] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const dispatch = useAppDispatch();
  const token = useAppSelector((state) => state.auth.token);
  const navigate = useNavigate();
  const location = useLocation();

  const redirectPath = useMemo(() => {
    const state = location.state as { from?: Location } | null;
    return state?.from?.pathname ?? '/';
  }, [location.state]);

  useEffect(() => {
    if (token) {
      navigate(redirectPath, { replace: true });
    }
  }, [token, navigate, redirectPath]);

  const handleFinish = async (values: LoginFormValues) => {
    setSubmitting(true);
    try {
      const response = await loginUser(values);

      if (!response.success || !response.user) {
        throw new Error(response.message ?? 'Unable to login.');
      }

      const tokenPayload = response.tokenType && response.token
        ? `${response.tokenType} ${response.token}`
        : response.token ?? '';

      if (!tokenPayload) {
        throw new Error('No token returned from login.');
      }

      dispatch(
        setCredentials({
          token: tokenPayload,
          user: {
            uuid: response.user.uuid,
            email: response.user.email,
            userName: response.user.userName,
            emailVerified: response.user.emailVerified,
          },
        }),
      );

      messageApi.success(response.message || 'Logged in successfully.');
      navigate(redirectPath, { replace: true });
      return;
    } catch (error) {
      const msg = error instanceof Error ? error.message : 'Unable to login.';
      messageApi.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '24px',
        background: 'linear-gradient(135deg, #f3f4f8 0%, #e3e7ff 100%)',
      }}
    >
      {contextHolder}
      <Card
        style={{ maxWidth: 420, width: '100%', boxShadow: '0 16px 40px rgba(102, 126, 234, 0.1)' }}
        bordered={false}
      >
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div style={{ textAlign: 'center' }}>
            <Title level={3} style={{ marginBottom: 0 }}>
              Welcome Back
            </Title>
            <Text type="secondary">Log in to continue to NutriFit AI</Text>
          </div>

          <Form<LoginFormValues>
            form={form}
            layout="vertical"
            requiredMark={false}
            onFinish={handleFinish}
            initialValues={{ email: '', password: '' }}
          >
            <Form.Item
              name="email"
              label="Email Address"
              rules={[
                { required: true, message: 'Please enter your email.' },
                { type: 'email', message: 'Please enter a valid email.' },
              ]}
            >
              <Input
                prefix={<MailOutlined />}
                placeholder="you@example.com"
                autoComplete="email"
                size="large"
              />
            </Form.Item>

            <Form.Item
              name="password"
              label="Password"
              rules={[{ required: true, message: 'Please enter your password.' }]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="Enter your password"
                autoComplete="current-password"
                size="large"
              />
            </Form.Item>

            <Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                size="large"
                block
                loading={submitting}
              >
                Sign In
              </Button>
            </Form.Item>
          </Form>

          <Space direction="vertical" style={{ width: '100%' }}>
            <Text type="secondary">
              Don&apos;t have an account? <Link to="/register">Create one</Link>
            </Text>
            <Text type="secondary">
              Need to verify your email? <Link to="/verify-email">Verify here</Link>
            </Text>
            <Text type="secondary">
              Forgot your password? <Link to="/forgot-password">Reset it</Link>
            </Text>
          </Space>
        </Space>
      </Card>
    </div>
  );
}
