import React, { useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  Layout,
  Menu,
  Avatar,
  Typography,
  Button,
  Space,
  Tooltip,
} from 'antd';
import {
  HomeOutlined,
  FireOutlined,
  StarOutlined,
  LogoutOutlined,
  HomeFilled,
  FireFilled,
  StarFilled,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { useAppDispatch, useAppSelector } from '../store/hooks';
import { clearCredentials } from '../store/auth/authSlice';

const { Sider, Content } = Layout;
const { Text, Title } = Typography;

interface NavItem {
  key: string;
  to: string;
  icon: React.ReactNode;
  iconActive: React.ReactNode;
  label: string;
  color: string;
}

const navItems: NavItem[] = [
  {
    key: 'home',
    to: '/',
    icon: <HomeOutlined />,
    iconActive: <HomeFilled />,
    label: 'Home',
    color: '#1890ff'
  },
  {
    key: 'profile',
    to: '/profile',
    icon: <UserOutlined />,
    iconActive: <UserOutlined />,
    label: 'Profile',
    color: '#13c2c2'
  },
  {
    key: 'workout',
    to: '/workout',
    icon: <FireOutlined />,
    iconActive: <FireFilled />,
    label: 'Workout',
    color: '#ff4d4f'
  },
  {
    key: 'meal',
    to: '/meal',
    icon: <StarOutlined />,
    iconActive: <StarFilled />,
    label: 'Meal',
    color: '#52c41a'
  },
  // Message page removed pending future feature rollout.
];

export default function MainLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const token = useAppSelector((state) => state.auth.token);

  // Get current active key based on pathname
  const getCurrentKey = () => {
    const currentItem = navItems.find(item => 
      item.to === '/' ? location.pathname === '/' : location.pathname.startsWith(item.to)
    );
    return currentItem?.key || 'home';
  };

  const menuItems = navItems.map(item => {
    const isActive = getCurrentKey() === item.key;
    return {
      key: item.key,
      icon: isActive ? item.iconActive : item.icon,
      label: (
        <NavLink 
          to={item.to} 
          style={{ 
            textDecoration: 'none',
            color: 'inherit'
          }}
        >
          {item.label}
        </NavLink>
      ),
      style: {
        margin: '6px 12px',
        height: 44,
        borderRadius: 10,
        display: 'flex',
        alignItems: 'center',
        transition: 'all 0.2s ease',
        ...(isActive
          ? {
              backgroundColor: `${item.color}14`,
              borderLeft: `4px solid ${item.color}`,
              fontWeight: 600,
              color: item.color,
              boxShadow: 'inset 0 0 0 1px rgba(0,0,0,0.02)'
            }
          : {
              backgroundColor: 'transparent'
            }),
      }
    };
  });

  const handleSignOut = () => {
    dispatch(clearCredentials());
    navigate('/login');
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* Sidebar */}
      <Sider 
        collapsed={collapsed}
        onCollapse={setCollapsed}
        width={280}
        style={{
          background: 'linear-gradient(180deg, #ffffff 0%, #fafafa 100%)',
          boxShadow: '2px 0 8px 0 rgba(29,35,41,0.05)',
          borderRight: '1px solid #f0f0f0'
        }}
      >
        <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
          {/* Logo Section */}
          <div style={{
            padding: collapsed ? '16px 12px' : '24px 20px',
            textAlign: 'center',
            borderBottom: '1px solid #f0f0f0',
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            margin: '0 0 16px 0',
            position: 'relative'
          }}>
            <Space style={{ position: 'absolute', top: 8, right: 8 }} size={4}>
              <Tooltip title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'} placement="bottomRight">
                <Button
                  type="text"
                  size="large"
                  icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                  onClick={() => setCollapsed(!collapsed)}
                  aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
                  style={{ color: '#ffffff' }}
                />
              </Tooltip>
            </Space>
            {!collapsed ? (
              <Space direction="vertical" size="small" style={{ width: '100%' }}>
                <Avatar 
                  size={60}
                  style={{
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    border: '2px solid rgba(255,255,255,0.35)',
                    fontSize: '22px',
                    fontWeight: 'bold',
                    color: 'white'
                  }}
                >
                  N+F
                </Avatar>
                <div>
                  <Title 
                    level={4} 
                    style={{ 
                      margin: '6px 0 2px 0', 
                      color: '#ffffff',
                      letterSpacing: 0.2,
                      textShadow: '0 1px 2px rgba(0,0,0,0.25)'
                    }}
                  >
                    NutriFit AI
                  </Title>
                  <Text style={{ fontSize: 12, color: 'rgba(255,255,255,0.85)' }}>
                    Wellness Platform
                  </Text>
                </div>
              </Space>
            ) : (
              <Avatar 
                size={40}
                style={{
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  fontSize: '16px',
                  fontWeight: 'bold',
                  color: 'white'
                }}
              >
                N+F
              </Avatar>
            )}
          </div>

          {/* Navigation Menu */}
          <Menu
            mode="inline"
            selectedKeys={[getCurrentKey()]}
            items={menuItems}
            style={{ 
              border: 'none',
              background: 'transparent'
            }}
          />

          {/* Sign out directly under menu (with divider line) */}
          <div
            style={{
              marginTop: 8,
              padding: collapsed ? '12px 12px 12px 12px' : '12px 20px 12px 20px',
              borderTop: '1px solid #f0f0f0',
            }}
          >
            <Tooltip title="Sign out" placement={collapsed ? 'right' : 'top'}>
              <Button
                type="text"
                icon={<LogoutOutlined />}
                aria-label="Sign out"
                onClick={handleSignOut}
                disabled={!token}
                style={{
                  width: '100%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: collapsed ? 'center' : 'flex-start',
                  color: token ? '#a78bfa' : 'rgba(0,0,0,0.25)',
                  fontWeight: 500,
                  padding: collapsed ? '8px 0' : '8px 12px',
                }}
              >
                {!collapsed && 'Sign out'}
              </Button>
            </Tooltip>
          </div>
        </div>
      </Sider>

      {/* Main Content */}
      <Layout>
        <Content style={{
          margin: '24px',
          padding: '32px',
          background: '#ffffff',
          borderRadius: '12px',
          boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06)',
          minHeight: 'calc(100vh - 48px)',
          overflow: 'auto'
        }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
