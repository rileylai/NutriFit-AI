import { LockOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Form, Input, Result, Space, Typography, message } from 'antd';
import { useCallback, useMemo, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { resetPassword } from '../services/authService';
import type { PasswordResetRequest } from '../types/Auth';

const { Title, Text } = Typography;

type ResetPasswordFormValues = Pick<PasswordResetRequest, 'newPassword'>;

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const [form] = Form.useForm<ResetPasswordFormValues>();
  const [submitting, setSubmitting] = useState(false);
  const [completed, setCompleted] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const navigate = useNavigate();

  const tokenFromQuery = useMemo(() => searchParams.get('token') ?? '', [searchParams]);

  const handleSubmit = useCallback(
    async (values: ResetPasswordFormValues) => {
      if (!tokenFromQuery) {
        messageApi.error('Reset token is missing. Please use the link from your email.');
        return;
      }
      setSubmitting(true);
      try {
        const response = await resetPassword({
          token: tokenFromQuery,
          newPassword: values.newPassword,
        });
        if (!response.success) {
          const feedback = response.message ?? 'Failed to reset password.';
          messageApi.error(feedback);
          return;
        }

        messageApi.success(response.message || 'Your password has been reset successfully.');
        setCompleted(true);
      } catch (error) {
        const msg = error instanceof Error ? error.message : 'Unable to reset password.';
        messageApi.error(msg);
      } finally {
        setSubmitting(false);
      }
    },
    [messageApi, tokenFromQuery],
  );

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
        style={{ maxWidth: 480, width: '100%', boxShadow: '0 16px 40px rgba(102, 126, 234, 0.1)' }}
        variant="borderless"
      >
        {completed ? (
          <Result
            status="success"
            title="Password reset successfully"
            subTitle="You can now sign in with your new password."
            extra={
              <Space direction="vertical" style={{ width: '100%' }}>
                <Button type="primary" size="large" block onClick={() => navigate('/login', { replace: true })}>
                  Go to Login
                </Button>
                <Button size="large" block onClick={() => navigate('/', { replace: true })}>
                  Go to Home
                </Button>
              </Space>
            }
          />
        ) : (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <div style={{ textAlign: 'center' }}>
              <Title level={3} style={{ marginBottom: 0 }}>
                Reset your password
              </Title>
              <Text type="secondary">
                Choose a new password for your account. The reset token will expire shortly, so complete this step now.
              </Text>
            </div>

            {!tokenFromQuery && (
              <Alert
                type="warning"
                showIcon
                message="Missing reset token"
                description="This page requires a valid reset token from the email link. Please request a new password reset email if needed."
              />
            )}

            <Form<ResetPasswordFormValues>
              form={form}
              layout="vertical"
              requiredMark={false}
              onFinish={handleSubmit}
              initialValues={{ newPassword: '' }}
            >
              <Form.Item
                name="newPassword"
                label="New Password"
                rules={[
                  { required: true, message: 'Please enter a new password.' },
                  { min: 8, message: 'Password must be at least 8 characters long.' },
                ]}
              >
                <Input.Password
                  prefix={<LockOutlined />}
                  placeholder="Enter your new password"
                  autoComplete="new-password"
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
                  disabled={!tokenFromQuery}
                >
                  Reset Password
                </Button>
              </Form.Item>
            </Form>

            <Text type="secondary" style={{ textAlign: 'center' }}>
              Need to request a new link? <Link to="/forgot-password">Send reset email</Link>
            </Text>
          </Space>
        )}
      </Card>
    </div>
  );
}

