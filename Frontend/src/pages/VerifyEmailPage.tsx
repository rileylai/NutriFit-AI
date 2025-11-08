import { MailOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Form, Input, Result, Space, Typography, message } from 'antd';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { resendVerificationEmail, verifyEmail } from '../services/authService';
import type { EmailVerificationRequest, ResendVerificationRequest } from '../types/Auth';

const { Title, Text } = Typography;

type VerifyFormValues = EmailVerificationRequest;
type ResendFormValues = ResendVerificationRequest;
type VerificationResponse = Awaited<ReturnType<typeof verifyEmail>>;

function normalizeToken(rawToken: string): string {
  const trimmed = rawToken.trim();
  if (!trimmed) {
    return '';
  }

  try {
    const parsed = new URL(trimmed);
    const nestedToken = parsed.searchParams.get('token');
    if (nestedToken) {
      return nestedToken.trim();
    }
  } catch {
    // Ignore parsing errors; the token might already be the raw value.
  }

  return trimmed;
}

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const [verifyForm] = Form.useForm<VerifyFormValues>();
  const [resendForm] = Form.useForm<ResendFormValues>();
  const [verifying, setVerifying] = useState(false);
  const [resending, setResending] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const [verificationResult, setVerificationResult] = useState<VerificationResponse | null>(null);
  const navigate = useNavigate();
  const autoVerifyTokenRef = useRef<string | null>(null);

  const rawTokenFromQuery = useMemo(() => searchParams.get('token') ?? '', [searchParams]);
  const tokenFromQuery = useMemo(
    () => normalizeToken(rawTokenFromQuery),
    [rawTokenFromQuery],
  );

  const handleVerify = useCallback(async (values: VerifyFormValues) => {
    const sanitizedToken = normalizeToken(values.token);
    if (!sanitizedToken) {
      messageApi.error('The provided verification token is invalid.');
      setVerificationResult({ success: false, message: 'Invalid verification token.', emailVerified: false });
      return;
    }

    if (sanitizedToken !== values.token) {
      verifyForm.setFieldsValue({ token: sanitizedToken });
    }

    setVerifying(true);
    setVerificationResult(null);
    try {
      const response = await verifyEmail({ token: sanitizedToken });
      setVerificationResult(response);
      if (!response.success) {
        const feedback = response.message ?? 'Email verification failed.';
        messageApi.error(feedback);
        return;
      }

      const feedback = response.message || 'Email verified successfully. You can now sign in.';
      messageApi.success(feedback);
    } catch (error) {
      const msg = error instanceof Error ? error.message : 'Unable to verify email.';
      messageApi.error(msg);
      setVerificationResult({ success: false, message: msg, emailVerified: false });
    } finally {
      setVerifying(false);
    }
  }, [messageApi, verifyForm]);

  const handleResend = useCallback(async (values: ResendFormValues) => {
    setResending(true);
    try {
      const response = await resendVerificationEmail(values);
      if (!response.success) {
        const feedback = response.message ?? 'Failed to resend verification email.';
        messageApi.error(feedback);
        return;
      }

      messageApi.success(
        response.message || 'Verification email has been resent. Please check your inbox.',
      );
    } catch (error) {
      const msg =
        error instanceof Error ? error.message : 'Unable to resend verification email right now.';
      messageApi.error(msg);
    } finally {
      setResending(false);
    }
  }, [messageApi]);

  useEffect(() => {
    if (!tokenFromQuery) {
      autoVerifyTokenRef.current = null;
      return;
    }

    verifyForm.setFieldsValue({ token: tokenFromQuery });

    if (autoVerifyTokenRef.current === tokenFromQuery) {
      return;
    }

    autoVerifyTokenRef.current = tokenFromQuery;
    void handleVerify({ token: tokenFromQuery });
  }, [tokenFromQuery, verifyForm, handleVerify]);

  const showSuccess =
    verificationResult?.success === true && verificationResult?.emailVerified === true;
  const showFailure = verificationResult?.success === false;

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
        style={{ maxWidth: 520, width: '100%', boxShadow: '0 16px 40px rgba(102, 126, 234, 0.1)' }}
        variant="borderless"
      >
        {showSuccess ? (
          <Result
            status="success"
            title="Email verified!"
            subTitle={
              verificationResult?.message ||
              'Your email has been verified successfully. You can now sign in.'
            }
            extra={
              <Space direction="vertical" style={{ width: '100%' }}>
                <Button key="home" size="large" onClick={() => navigate('/')} block>
                  Go to Home
                </Button>
                <Button
                  type="primary"
                  key="login"
                  size="large"
                  onClick={() => navigate('/login', { replace: true })}
                  block
                >
                  Go to Login
                </Button>
              </Space>
            }
          />
        ) : (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <div style={{ textAlign: 'center' }}>
              <Title level={3} style={{ marginBottom: 0 }}>
                Verify Your Email
              </Title>
              <Text type="secondary">
                Enter the verification token sent to your inbox to activate your account.
              </Text>
            </div>

            <Alert
              type="info"
              showIcon
              message="Check your inbox"
              description="Paste the verification token from the email we sent you to complete verification."
            />

            {showFailure && (
              <Alert
                type="error"
                showIcon
                message="Verification failed"
                description={
                  verificationResult?.message ||
                  'The provided verification token is invalid or has expired.'
                }
              />
            )}

            {showFailure && (
              <Button size="large" onClick={() => navigate('/')} block>
                Go to Home
              </Button>
            )}

            <Card type="inner" title="Enter Verification Token" variant="borderless">
              <Form<VerifyFormValues>
                form={verifyForm}
                layout="vertical"
                requiredMark={false}
                initialValues={{ token: tokenFromQuery }}
                onFinish={handleVerify}
              >
                <Form.Item
                  name="token"
                  label="Verification Token"
                  rules={[{ required: true, message: 'Enter the verification token from your email.' }]}
                >
                  <Input
                    prefix={<SafetyCertificateOutlined />}
                    placeholder="Enter your verification token"
                    size="large"
                  />
                </Form.Item>

                <Form.Item>
                  <Button type="primary" htmlType="submit" size="large" block loading={verifying}>
                    Verify Email
                  </Button>
                </Form.Item>
              </Form>
            </Card>

            <Card type="inner" title="Need a new email?" variant="borderless">
              <Form<ResendFormValues>
                form={resendForm}
                layout="vertical"
                requiredMark={false}
                onFinish={handleResend}
              >
                <Form.Item
                  name="email"
                  label="Account Email"
                  rules={[
                    { required: true, message: 'Please provide the email associated with your account.' },
                    { type: 'email', message: 'Enter a valid email address.' },
                  ]}
                >
                  <Input
                    prefix={<MailOutlined />}
                    placeholder="Enter your email address"
                    size="large"
                  />
                </Form.Item>

                <Form.Item>
                  <Button type="default" htmlType="submit" size="large" block loading={resending}>
                    Resend Verification Email
                  </Button>
                </Form.Item>
              </Form>
            </Card>

            <Text type="secondary" style={{ textAlign: 'center' }}>
              Ready to go back? <Link to="/login">Return to login</Link>
            </Text>
          </Space>
        )}
      </Card>
    </div>
  );
}
