import { Progress } from 'antd';
import type { ProgressProps } from 'antd';

interface HoverProgressProps {
  percent: number;
  colors: { from: string; to: string };
  decimals?: number; // how many decimals to show
  size?: ProgressProps['size'];
  showInfo?: boolean;
}

export default function HoverProgress({
  percent,
  colors,
  decimals = 2,
  size = 'default',
  showInfo = true,
}: HoverProgressProps) {
  const clamp = (v: number) => Math.max(0, Math.min(v, 100));
  const displayPercent = clamp(percent);

  return (
    <div style={{ width: '100%' }}>
      <Progress
        percent={Number(displayPercent.toFixed(decimals))}
        showInfo={showInfo}
        format={(p) => `${(p ?? 0).toFixed(decimals)}%`}
        strokeColor={{ '0%': colors.from, '100%': colors.to }}
        strokeLinecap="round"
        size={size}
      />
    </div>
  );
}
