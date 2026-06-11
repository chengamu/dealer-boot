import type { ActivityStatus } from "../types";

export function fmt(value: number | undefined | null, digits = 2) {
  return Number(value || 0).toFixed(digits);
}

export function pct(value: number | undefined | null) {
  return `${fmt((value || 0) * 100)}%`;
}

export function statusLabel(status: ActivityStatus) {
  return {
    running: "进行中",
    done: "已完成",
    blocked: "等待补充",
    error: "失败",
  }[status];
}
