<template>
  <div class="portal-dashboard">
    <section class="portal-dashboard__stats">
      <DashboardStatCard v-for="item in statCards" :key="item.title" :item="item" @select="handleStatSelect" />
    </section>

    <section class="portal-dashboard__main">
      <DashboardPanel :title="text.recentOrders" :action-label="text.viewAllOrders" panel-class="portal-panel--orders" @action="handlePanelAction(text.viewAllOrders)">
        <RecentOrdersTable :orders="orders" :text="orderTableText" @view="handleOrderView" @page-change="handlePageChange" />
      </DashboardPanel>

      <DashboardPanel :title="text.activeUsers" :action-label="text.viewAllUsers" panel-class="portal-panel--users" @action="handlePanelAction(text.viewAllUsers)">
        <DashboardUserList :users="activeUsers" />
      </DashboardPanel>
    </section>

    <section class="portal-dashboard__bottom">
      <DashboardPanel :title="text.alerts" :action-label="text.viewAllAlerts" @action="handlePanelAction(text.viewAllAlerts)">
        <DashboardAlertList :alerts="alerts" />
      </DashboardPanel>

      <DashboardPanel :title="text.timeline" :action-label="text.viewAllActivity" :show-arrow="false" @action="handlePanelAction(text.viewAllActivity)">
        <DashboardTimeline :timeline="timeline" />
      </DashboardPanel>

      <DashboardPanel :title="text.orderHealth" :subtitle="text.lastSevenDays" :action-label="text.viewReport" panel-class="portal-panel--health" @action="handlePanelAction(text.viewReport)">
        <OrderHealthChart :items="healthItems" total="1,248" :total-label="text.totalOrders" />
      </DashboardPanel>
    </section>
  </div>
</template>

<script setup lang="ts" name="DashboardPage">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, DataAnalysis, Document, InfoFilled, ShoppingBag, User, Warning } from '@element-plus/icons-vue'
import useLocaleStore from '@/stores/locale'
import DashboardAlertList from './components/DashboardAlertList.vue'
import DashboardPanel from './components/DashboardPanel.vue'
import DashboardStatCard from './components/DashboardStatCard.vue'
import DashboardTimeline from './components/DashboardTimeline.vue'
import DashboardUserList from './components/DashboardUserList.vue'
import OrderHealthChart from './components/OrderHealthChart.vue'
import RecentOrdersTable from './components/RecentOrdersTable.vue'
import type { ActiveUserItem, AlertItem, HealthItem, OrderItem, OrderTableText, StatCardItem, TimelineItem } from './components/types'

const localeStore = useLocaleStore()
const isZh = computed(() => localeStore.language.startsWith('zh'))

const text = computed(() => isZh.value
  ? {
      activeOrders: '活跃订单',
      quotesPending: '待处理报价',
      dealersOnline: '在线经销商',
      fulfillmentRate: '准时履约率',
      recentOrders: '最近订单',
      activeUsers: '最近活跃用户',
      alerts: '系统告警',
      timeline: '活动时间线',
      orderHealth: '订单健康度',
      lastSevenDays: '最近 7 天',
      viewAllOrders: '查看全部订单',
      viewAllUsers: '查看全部用户',
      viewAllAlerts: '查看全部告警',
      viewAllActivity: '查看全部动态',
      viewReport: '查看报告',
      orderNo: '订单号',
      customer: '客户',
      status: '状态',
      orderDate: '订单日期',
      total: '金额',
      action: '操作',
      view: '查看',
      showingOrders: '显示 1 到 5 条，共 20 条订单',
      totalOrders: '总计'
    }
  : {
      activeOrders: 'Active Orders',
      quotesPending: 'Quotes Pending',
      dealersOnline: 'Dealers Online',
      fulfillmentRate: 'On-time Fulfillment',
      recentOrders: 'Recent Orders',
      activeUsers: 'Recently Active Users',
      alerts: 'System Alerts',
      timeline: 'Activity Timeline',
      orderHealth: 'Order Health',
      lastSevenDays: 'Last 7 Days',
      viewAllOrders: 'View all orders',
      viewAllUsers: 'View all users',
      viewAllAlerts: 'View all alerts',
      viewAllActivity: 'View all activity',
      viewReport: 'View report',
      orderNo: 'Order No.',
      customer: 'Customer',
      status: 'Status',
      orderDate: 'Order Date',
      total: 'Total',
      action: 'Action',
      view: 'View',
      showingOrders: 'Showing 1 to 5 of 20 orders',
      totalOrders: 'Total'
    })

const orderTableText = computed<OrderTableText>(() => ({
  orderNo: text.value.orderNo,
  customer: text.value.customer,
  status: text.value.status,
  orderDate: text.value.orderDate,
  total: text.value.total,
  action: text.value.action,
  view: text.value.view,
  showingOrders: text.value.showingOrders
}))

const statCards = computed<StatCardItem[]>(() => [
  {
    title: text.value.activeOrders,
    value: '1,248',
    trend: '↑ 18.6%',
    caption: isZh.value ? '较近 7 天' : 'vs last 7 days',
    icon: ShoppingBag,
    tone: 'blue',
    sparkline: '2,30 18,24 31,27 44,15 58,20 72,9 86,18 100,7 116,12'
  },
  {
    title: text.value.quotesPending,
    value: '342',
    trend: '↑ 9.4%',
    caption: isZh.value ? '较近 7 天' : 'vs last 7 days',
    icon: Document,
    tone: 'teal',
    sparkline: '2,26 16,18 29,23 42,16 55,22 70,12 84,19 98,7 116,14'
  },
  {
    title: text.value.dealersOnline,
    value: '128',
    trend: '↑ 12.3%',
    caption: isZh.value ? '较昨天' : 'vs yesterday',
    icon: User,
    tone: 'green',
    sparkline: '2,28 18,20 33,24 48,16 62,21 78,8 94,15 110,10'
  },
  {
    title: text.value.fulfillmentRate,
    value: '96.8%',
    trend: '',
    caption: '',
    icon: DataAnalysis,
    tone: 'orange',
    progress: '96.8%',
    progressLabel: isZh.value ? '本周 604 / 624 单准时完成' : '604 of 624 orders on time'
  }
])

const orders = computed<OrderItem[]>(() => [
  { no: 'O-2024-0532', customer: 'Bright View Homes', country: isZh.value ? '美国' : 'United States', status: isZh.value ? '生产中' : 'In Production', tone: 'blue', date: 'May 28, 2024', total: '$6,245.00' },
  { no: 'O-2024-0531', customer: 'Maple Leaf Living', country: isZh.value ? '加拿大' : 'Canada', status: isZh.value ? '已提交' : 'Submitted', tone: 'cyan', date: 'May 27, 2024', total: '$3,890.00' },
  { no: 'O-2024-0530', customer: 'Sunset Blinds GmbH', country: isZh.value ? '德国' : 'Germany', status: isZh.value ? '已发货' : 'Shipped', tone: 'green', date: 'May 26, 2024', total: '$9,780.00' },
  { no: 'O-2024-0529', customer: 'Coastal Interiors', country: isZh.value ? '澳大利亚' : 'Australia', status: isZh.value ? '待付款' : 'Pending Payment', tone: 'orange', date: 'May 25, 2024', total: '$2,156.00' },
  { no: 'O-2024-0528', customer: 'Nordic Light AB', country: isZh.value ? '瑞典' : 'Sweden', status: isZh.value ? '已交付' : 'Delivered', tone: 'green', date: 'May 24, 2024', total: '$4,321.00' }
])

const activeUsers = computed<ActiveUserItem[]>(() => [
  { name: 'Sophia Johnson', location: 'New York, US', role: 'Admin', time: '2 min ago', initials: 'SJ', tone: 'pink' },
  { name: 'Liam Chen', location: 'Toronto, CA', role: 'Manager', time: '8 min ago', initials: 'LC', tone: 'blue' },
  { name: 'Emma Williams', location: 'London, UK', role: 'Editor', time: '15 min ago', initials: 'EW', tone: 'purple' },
  { name: 'Noah Martinez', location: 'Sydney, AU', role: 'Viewer', time: '28 min ago', initials: 'NM', tone: 'orange' },
  { name: 'Isabella Rossi', location: 'Milan, IT', role: 'Admin', time: '1 hr ago', initials: 'IR', tone: 'green', away: true }
])

const alerts = computed<AlertItem[]>(() => [
  { title: isZh.value ? '订单积压偏高' : 'High order backlog', description: isZh.value ? '当前有 87 个订单等待生产。' : 'You have 87 orders pending production.', time: '2h ago', tone: 'danger', icon: Warning },
  { title: isZh.value ? '报价审批积压' : 'Quote approval backlog', description: isZh.value ? '当前有 14 个报价等待确认。' : '14 quotes are waiting for approval.', time: '3h ago', tone: 'warning', icon: DataAnalysis },
  { title: isZh.value ? '支付网关通知' : 'Payment gateway notice', description: isZh.value ? '计划维护窗口已确认。' : 'Scheduled maintenance window has been confirmed.', time: '6h ago', tone: 'info', icon: InfoFilled },
  { title: isZh.value ? '系统新版本' : 'New system update', description: isZh.value ? 'v2.4.0 将在本周发布。' : 'v2.4.0 will be released this week.', time: '1d ago', tone: 'info', icon: Bell }
])

const timeline = computed<TimelineItem[]>(() => [
  { title: isZh.value ? '订单 O-2024-0532 更新为生产中' : 'Order O-2024-0532 status updated to In Production', by: 'by Sophia Johnson', time: '2 min ago', color: '#1677ff' },
  { title: isZh.value ? '报价 Q-2024-0781 已发送给 Bright View Homes' : 'Quote Q-2024-0781 was sent to Bright View Homes', by: 'by Liam Chen', time: '15 min ago', color: '#14b8c4' },
  { title: isZh.value ? '订单 O-2024-0529 已收到付款' : 'Payment received for Order O-2024-0529', by: 'by System', time: '1 hr ago', color: '#22c55e' },
  { title: isZh.value ? '新增用户 Emma Williams' : 'New user Emma Williams was added', by: 'by Noah Martinez', time: '2 hrs ago', color: '#7c3aed' },
  { title: isZh.value ? '文件 Window Specs.pdf 已上传' : 'File Window Specs.pdf uploaded to O-2024-0530', by: 'by Isabella Rossi', time: '3 hrs ago', color: '#f59e0b' }
])

const healthItems = computed<HealthItem[]>(() => [
  { label: isZh.value ? '生产中' : 'In Production', value: '482 (38.6%)', color: '#1677ff' },
  { label: isZh.value ? '已提交' : 'Submitted', value: '286 (22.9%)', color: '#14b8c4' },
  { label: isZh.value ? '已发货' : 'Shipped', value: '231 (18.5%)', color: '#35c767' },
  { label: isZh.value ? '待付款' : 'Pending Payment', value: '142 (11.4%)', color: '#f59e0b' },
  { label: isZh.value ? '已取消' : 'Cancelled', value: '107 (8.6%)', color: '#ff4d4f' }
])

function handleStatSelect(item: StatCardItem) {
  ElMessage.info(isZh.value ? `已选择：${item.title}` : `Selected: ${item.title}`)
}

function handlePanelAction(label: string) {
  ElMessage.info(isZh.value ? `即将打开：${label}` : `Open: ${label}`)
}

function handleOrderView(order: OrderItem) {
  ElMessage.info(isZh.value ? `查看订单：${order.no}` : `View order: ${order.no}`)
}

function handlePageChange(page: number) {
  ElMessage.info(isZh.value ? `切换到第 ${page} 页` : `Page ${page}`)
}
</script>

<style>
.portal-dashboard {
  --portal-title: #07143d;
  --portal-text: #1b2b57;
  --portal-muted: #647294;
  --portal-line: #dfe7f3;
  --portal-soft: #f7faff;
  --portal-blue: #1677ff;
  display: grid;
  gap: 18px;
  padding-bottom: 32px;
  color: var(--portal-text);
  font-size: 13px;
}

.portal-dashboard__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.portal-stat-card,
.portal-panel {
  border: 1px solid var(--portal-line);
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 10px 26px rgba(15, 35, 80, 0.06);
}

.portal-stat-card {
  position: relative;
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr);
  min-height: 154px;
  overflow: hidden;
  padding: 26px 24px 22px;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.portal-stat-card:hover,
.portal-stat-card:focus-visible {
  border-color: #b8d2ff;
  box-shadow: 0 14px 32px rgba(15, 35, 80, 0.1);
  outline: none;
  transform: translateY(-2px);
}

.portal-stat-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: #eaf2ff;
  color: #1677ff;
}

.portal-stat-card__icon svg {
  width: 29px;
  height: 29px;
}

.portal-stat-card__content {
  min-width: 0;
}

.portal-stat-card__content > span {
  display: block;
  margin-bottom: 8px;
  color: var(--portal-title);
  font-size: 14px;
  font-weight: 700;
}

.portal-stat-card__content > strong {
  display: block;
  color: #07133b;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.05;
}

.portal-stat-card__meta {
  position: absolute;
  left: 24px;
  bottom: 24px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.portal-stat-card__meta em {
  color: #05a63f;
  font-size: 13px;
  font-style: normal;
  font-weight: 800;
}

.portal-stat-card__meta small,
.portal-stat-card__progress span {
  color: var(--portal-muted);
  font-size: 12px;
  font-weight: 600;
}

.portal-stat-card__sparkline {
  position: absolute;
  right: 22px;
  bottom: 26px;
  width: 98px;
  height: 36px;
  color: #1677ff;
}

.portal-stat-card.is-teal .portal-stat-card__icon {
  background: #dff9f7;
  color: #13b8b5;
}

.portal-stat-card.is-teal .portal-stat-card__sparkline {
  color: #13b8b5;
}

.portal-stat-card.is-green .portal-stat-card__icon {
  background: #e7f9eb;
  color: #16b345;
}

.portal-stat-card.is-green .portal-stat-card__sparkline {
  color: #16b345;
}

.portal-stat-card.is-orange .portal-stat-card__icon {
  background: #fff0df;
  color: #f97316;
}

.portal-stat-card__progress {
  position: absolute;
  right: 24px;
  bottom: 24px;
  left: 24px;
}

.portal-stat-card__progress i {
  display: block;
  height: 10px;
  margin-top: 12px;
  overflow: hidden;
  border-radius: 999px;
  background: #e4e9f2;
}

.portal-stat-card__progress b {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #ff7a00;
}

.portal-dashboard__main,
.portal-dashboard__bottom {
  display: grid;
  gap: 18px;
}

.portal-dashboard__main {
  grid-template-columns: minmax(0, 1.55fr) minmax(360px, 0.95fr);
}

.portal-dashboard__bottom {
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.08fr) minmax(360px, 1.22fr);
}

.portal-panel {
  overflow: hidden;
}

.portal-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 54px;
  padding: 0 24px;
  border-bottom: 1px solid var(--portal-line);
}

.portal-panel__header h2 {
  margin: 0;
  color: var(--portal-title);
  font-size: 16px;
  font-weight: 800;
}

.portal-panel__header h2 small {
  color: var(--portal-muted);
  font-size: 12px;
  font-weight: 700;
}

.portal-panel__header button,
.portal-orders__footer button,
.portal-orders__row button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  background: transparent;
  color: var(--portal-blue);
  font: inherit;
  font-weight: 800;
  cursor: pointer;
}

.portal-panel__header button {
  gap: 6px;
  font-size: 13px;
}

.portal-orders {
  overflow: auto;
}

.portal-orders__head,
.portal-orders__row {
  display: grid;
  grid-template-columns: 132px minmax(180px, 1.15fr) 140px 128px 120px 72px;
  align-items: center;
  min-width: 760px;
  column-gap: 16px;
  padding: 0 24px;
}

.portal-orders__head {
  min-height: 44px;
  border-bottom: 1px solid var(--portal-line);
  background: #fbfdff;
  color: #314163;
  font-size: 12px;
  font-weight: 800;
}

.portal-orders__row {
  min-height: 58px;
  border-bottom: 1px solid var(--portal-line);
}

.portal-orders__row a {
  color: var(--portal-blue);
  font-weight: 800;
  text-decoration: none;
}

.portal-orders__row strong,
.portal-users__item strong,
.portal-alerts__item strong,
.portal-timeline__item strong {
  color: var(--portal-title);
  font-weight: 800;
}

.portal-orders__row small,
.portal-users__item small,
.portal-alerts__item small,
.portal-timeline__item small {
  display: block;
  margin-top: 4px;
  color: var(--portal-muted);
  font-size: 12px;
  font-weight: 600;
}

.portal-orders__row button {
  width: 36px;
  height: 36px;
  border: 1px solid var(--portal-line);
  border-radius: 9px;
  color: #26365f;
}

.portal-pill {
  justify-self: start;
  padding: 5px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 800;
}

.portal-pill.is-blue {
  background: #eaf2ff;
  color: #005eff;
}

.portal-pill.is-cyan {
  background: #e5f7ff;
  color: #0072bc;
}

.portal-pill.is-green {
  background: #e6f8ec;
  color: #09a344;
}

.portal-pill.is-orange {
  background: #fff1df;
  color: #f97316;
}

.portal-orders__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 760px;
  min-height: 48px;
  padding: 0 24px;
  color: var(--portal-muted);
  font-weight: 700;
}

.portal-orders__footer div {
  display: flex;
  gap: 10px;
}

.portal-orders__footer button {
  min-width: 28px;
  height: 28px;
  border-radius: 7px;
  color: #44537a;
}

.portal-orders__footer button.is-active {
  background: #eaf2ff;
  color: #1677ff;
}

.portal-users {
  display: grid;
}

.portal-users__item {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) auto 72px 8px;
  align-items: center;
  gap: 12px;
  min-height: 72px;
  padding: 0 24px;
  border-bottom: 1px solid var(--portal-line);
}

.portal-users__item:last-child {
  border-bottom: 0;
}

.portal-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffb1c7, #8a4fff);
  color: #fff;
  font-weight: 800;
}

.portal-avatar.is-blue {
  background: linear-gradient(135deg, #7dd3fc, #1677ff);
}

.portal-avatar.is-purple {
  background: linear-gradient(135deg, #c4b5fd, #7c3aed);
}

.portal-avatar.is-orange {
  background: linear-gradient(135deg, #fdba74, #ef4444);
}

.portal-avatar.is-green {
  background: linear-gradient(135deg, #86efac, #14b8a6);
}

.portal-users__item em {
  padding: 5px 9px;
  border-radius: 6px;
  background: #edf4ff;
  color: #1677ff;
  font-size: 12px;
  font-style: normal;
  font-weight: 800;
}

.portal-users__item time,
.portal-alerts__item time,
.portal-timeline__item time {
  color: var(--portal-muted);
  font-size: 12px;
  font-weight: 700;
}

.portal-users__item > i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #18b83f;
}

.portal-users__item > i.is-away {
  background: #fb8c00;
}

.portal-alerts,
.portal-timeline {
  padding: 14px 24px 18px;
}

.portal-alerts__item {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) 52px;
  align-items: center;
  gap: 10px;
  min-height: 54px;
}

.portal-alerts__item > span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #edf4ff;
  color: #1677ff;
}

.portal-alerts__item.is-danger > span {
  background: #fff1f1;
  color: #ff4d4f;
}

.portal-alerts__item.is-warning > span {
  background: #fff4e5;
  color: #ff9900;
}

.portal-timeline__item {
  position: relative;
  display: grid;
  grid-template-columns: 20px minmax(0, 1fr) 70px;
  gap: 10px;
  min-height: 52px;
}

.portal-timeline__item::before {
  position: absolute;
  top: 18px;
  bottom: -18px;
  left: 5px;
  width: 1px;
  background: #d8e3f2;
  content: "";
}

.portal-timeline__item:last-child::before {
  display: none;
}

.portal-timeline__item > span {
  position: relative;
  z-index: 1;
  width: 10px;
  height: 10px;
  margin-top: 5px;
  border-radius: 50%;
  background: var(--dot-color);
}

.portal-health {
  display: grid;
  grid-template-columns: 190px minmax(0, 1fr);
  gap: 22px;
  align-items: center;
  padding: 26px 24px 24px;
}

.portal-health__chart {
  display: grid;
  place-items: center;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: conic-gradient(#1677ff 0 38.6%, #14b8c4 38.6% 61.5%, #35c767 61.5% 80%, #f59e0b 80% 91.4%, #ff4d4f 91.4% 100%);
}

.portal-health__chart span {
  display: grid;
  place-items: center;
  width: 94px;
  height: 94px;
  border-radius: 50%;
  background: #ffffff;
}

.portal-health__chart strong {
  color: var(--portal-title);
  font-size: 24px;
  font-weight: 800;
}

.portal-health__chart small {
  color: var(--portal-muted);
  font-size: 12px;
  font-weight: 700;
}

.portal-health__legend {
  display: grid;
  gap: 16px;
}

.portal-health__legend div {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
}

.portal-health__legend i {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.portal-health__legend span {
  color: var(--portal-text);
  font-weight: 700;
}

.portal-health__legend strong {
  color: var(--portal-title);
  font-weight: 800;
}

@media (max-width: 1400px) {
  .portal-dashboard__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .portal-dashboard__main,
  .portal-dashboard__bottom {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .portal-dashboard {
    gap: 14px;
  }

  .portal-dashboard__stats,
  .portal-health {
    grid-template-columns: 1fr;
  }

  .portal-stat-card {
    min-height: 146px;
    padding: 20px;
  }

  .portal-health__chart {
    justify-self: center;
  }
}
</style>
