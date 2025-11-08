import { MailOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Form, Input, Result, Space, Typography, message } from 'antd';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { requestPasswordReset } from '../services/authService';
import type { ForgotPasswordRequest } from '../types/Auth';

const { Title, Text } = Typography;

type ForgotPasswordFormValues = ForgotPasswordRequest;

export default function ForgotPasswordPage() {
  const [form] = Form.useForm<ForgotPasswordFormValues>();
  const [submitting, setSubmitting] = useState(false);
  const [completed, setCompleted] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const handleSubmit = async (values: ForgotPasswordFormValues) => {
    setSubmitting(true);
    try {
      const response = await requestPasswordReset(values);
      if (!response.success) {
        const feedback = response.message ?? 'Failed to send password reset email.';
        messageApi.error(feedback);
        return;
      }

      messageApi.success(
        response.message ||
          'If an account exists for this email, a password reset link has been sent. Please check your inbox.',
      );
      setCompleted(true);
    } catch (error) {
      const msg = error instanceof Error ? error.message : 'Unable to send password reset email.';
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
        style={{ maxWidth: 480, width: '100%', boxShadow: '0 16px 40px rgba(102, 126, 234, 0.1)' }}
        variant="borderless"
      >
        {completed ? (
          <Result
            status="success"
            title="Password reset email sent"
            subTitle="If an account exists for this email address, you will receive a link to reset your password shortly."
            extra={
              <Space direction="vertical" style={{ width: '100%' }}>
                <Button type="primary" size="large" block>
                  <Link to="/login">Return to Login</Link>
                </Button>
                <Button size="large" block>
                  <Link to="/">Go to Home</Link>
                </Button>
              </Space>
            }
          />
        ) : (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <div style={{ textAlign: 'center' }}>
              <Title level={3} style={{ marginBottom: 0 }}>
                Forgot your password?
              </Title>
              <Text type="secondary">
                Enter the email tied to your account. We&apos;ll send you a link to reset your password.
              </Text>
            </div>

            <Alert
              type="info"
              showIcon
              message="We&apos;ll send you reset instructions"
              description="If your email is associated with an account, you&apos;ll receive a password reset link."
            />

            <Form<ForgotPasswordFormValues>
              form={form}
              layout="vertical"
              requiredMark={false}
              onFinish={handleSubmit}
              initialValues={{ email: '' }}
            >
              <Form.Item
                name="email"
                label="Email Address"
                rules={[
                  { required: true, message: 'Please enter the email associated with your account.' },
                  { type: 'email', message: 'Please enter a valid email address.' },
                ]}
              >
                <Input
                  prefix={<MailOutlined />}
                  placeholder="you@example.com"
                  autoComplete="email"
                  size="large"
                />
              </Form.Item>

              <Form.Item>
                <Button type="primary" htmlType="submit" size="large" block loading={submitting}>
                  Send Reset Link
                </Button>
              </Form.Item>
            </Form>

            <Text type="secondary" style={{ textAlign: 'center' }}>
              Remembered your password? <Link to="/login">Back to login</Link>
            </Text>
          </Space>
        )}
      </Card>
    </div>
  );
}

