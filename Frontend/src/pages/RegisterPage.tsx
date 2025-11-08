import { LockOutlined, MailOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Card, Form, Input, Space, Typography, message } from 'antd';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { registerUser } from '../services/authService';
import type { RegisterRequest } from '../types/Auth';

const { Title, Text } = Typography;

interface RegisterFormValues extends RegisterRequest {
  confirmPassword: string;
}

export default function RegisterPage() {
  const [form] = Form.useForm<RegisterFormValues>();
  const [submitting, setSubmitting] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const navigate = useNavigate();

  const handleFinish = async (values: RegisterFormValues) => {
    setSubmitting(true);
    try {
      const response = await registerUser({
        email: values.email,
        userName: values.userName,
        password: values.password,
      });

      if (!response.success) {
        throw new Error(response.message ?? 'Registration failed.');
      }

      messageApi.success(
        response.message ?? 'Registration successful. Please verify your email to continue.',
      );
      navigate(`/verify-email?email=${encodeURIComponent(values.email)}`);
    } catch (error) {
      const msg = error instanceof Error ? error.message : 'Unable to register.';
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
        bordered={false}
      >
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div style={{ textAlign: 'center' }}>
            <Title level={3} style={{ marginBottom: 0 }}>
              Create Your Account
            </Title>
            <Text type="secondary">Join NutriFit AI to track your wellness journey.</Text>
          </div>

          <Form<RegisterFormValues>
            form={form}
            layout="vertical"
            requiredMark={false}
            onFinish={handleFinish}
            initialValues={{ email: '', userName: '', password: '', confirmPassword: '' }}
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
              name="userName"
              label="Username"
              rules={[
                { required: true, message: 'Please choose a username.' },
                {
                  min: 3,
                  max: 50,
                  message: 'Username must be between 3 and 50 characters.',
                },
              ]}
            >
              <Input
                prefix={<UserOutlined />}
                placeholder="Your display name"
                autoComplete="username"
                size="large"
              />
            </Form.Item>

            <Form.Item
              name="password"
              label="Password"
              rules={[
                { required: true, message: 'Please create a password.' },
                {
                  min: 8,
                  message: 'Password must be at least 8 characters.',
                },
              ]}
              hasFeedback
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="Create a secure password"
                autoComplete="new-password"
                size="large"
              />
            </Form.Item>

            <Form.Item
              name="confirmPassword"
              label="Confirm Password"
              dependencies={['password']}
              hasFeedback
              rules={[
                { required: true, message: 'Please confirm your password.' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('password') === value) {
                      return Promise.resolve();
                    }
                    return Promise.reject(new Error('The two passwords do not match.'));
                  },
                }),
              ]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="Re-enter your password"
                autoComplete="new-password"
                size="large"
              />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" size="large" block loading={submitting}>
                Create Account
              </Button>
            </Form.Item>
          </Form>

          <Text type="secondary">
            Already have an account? <Link to="/login">Sign in instead</Link>
          </Text>
        </Space>
      </Card>
    </div>
  );
}
