import type { Component } from 'vue'

export type PortalTone = 'blue' | 'teal' | 'green' | 'orange' | 'cyan' | 'purple' | 'pink' | 'danger' | 'warning' | 'info'

export interface StatCardItem {
  title: string
  value: string
  trend?: string
  caption?: string
  icon: Component
  tone: PortalTone
  sparkline?: string
  progress?: string
  progressLabel?: string
}

export interface OrderItem {
  no: string
  customer: string
  country: string
  status: string
  tone: PortalTone
  date: string
  total: string
}

export interface OrderTableText {
  orderNo: string
  customer: string
  status: string
  orderDate: string
  total: string
  action: string
  view: string
  showingOrders: string
}

export interface ActiveUserItem {
  name: string
  location: string
  role: string
  time: string
  initials: string
  tone: PortalTone
  away?: boolean
}

export interface AlertItem {
  title: string
  description: string
  time: string
  tone: PortalTone
  icon: Component
}

export interface TimelineItem {
  title: string
  by: string
  time: string
  color: string
}

export interface HealthItem {
  label: string
  value: string
  color: string
}
