<template>
  <div class="base-dashboard">
    <div class="base-dashboard__header">
      <div>
        <span class="base-dashboard__eyebrow">{{ t('dashboard.home') }}</span>
        <h1>{{ t('dashboard.title') }}</h1>
        <p>{{ t('dashboard.subtitle') }}</p>
      </div>
      <div class="base-dashboard__status">
        <span>工作台</span>
        <strong>运行平稳</strong>
      </div>
    </div>

    <div class="base-dashboard__summary">
      <section class="base-dashboard__item is-primary">
        <span>{{ t('dashboard.system') }}</span>
        <strong>Users / Roles / Menus</strong>
        <em>权限基础能力</em>
      </section>
      <section class="base-dashboard__item">
        <span>{{ t('dashboard.tenant') }}</span>
        <strong>tenant</strong>
        <em>业务隔离</em>
      </section>
      <section class="base-dashboard__item">
        <span>{{ t('dashboard.database') }}</span>
        <strong>MySQL / PostgreSQL</strong>
        <em>运行配置</em>
      </section>
      <section class="base-dashboard__item">
        <span>访问控制</span>
        <strong>Routes / APIs</strong>
        <em>安全策略</em>
      </section>
    </div>

    <div class="base-dashboard__panels">
      <section class="base-dashboard__panel">
        <div class="base-dashboard__panel-title">
          <h2>平台概览</h2>
          <span>今日</span>
        </div>
        <ul class="base-dashboard__list">
          <li>
            <span>用户中心</span>
            <strong>就绪</strong>
          </li>
          <li>
            <span>角色权限</span>
            <strong>就绪</strong>
          </li>
          <li>
            <span>操作审计</span>
            <strong>启用</strong>
          </li>
        </ul>
      </section>

      <section class="base-dashboard__panel">
        <div class="base-dashboard__panel-title">
          <h2>快速指标</h2>
          <span>静态样板</span>
        </div>
        <div class="base-dashboard__metrics">
          <div>
            <span>模块</span>
            <strong>12</strong>
          </div>
          <div>
            <span>菜单</span>
            <strong>48</strong>
          </div>
          <div>
            <span>日志</span>
            <strong>100%</strong>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts" name="DashboardPage">
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
</script>

<style scoped>
.base-dashboard {
  color: var(--admin-text, #333333);
}

.base-dashboard__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
}

.base-dashboard__header h1 {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 0;
  color: var(--admin-title, #1a1a1a);
}

.base-dashboard__header p {
  margin: 0;
  max-width: 720px;
  color: var(--admin-muted, #666666);
  line-height: 1.7;
}

.base-dashboard__eyebrow {
  display: inline-flex;
  align-items: center;
  margin-bottom: 8px;
  color: var(--admin-primary, #0066ff);
  font-size: 13px;
  font-weight: 600;
}

.base-dashboard__status {
  min-width: 168px;
  padding: 14px 16px;
  border: 1px solid var(--admin-border-soft, #edf2f7);
  border-radius: var(--admin-radius, 8px);
  background: #ffffff;
}

.base-dashboard__status span,
.base-dashboard__item span,
.base-dashboard__item em,
.base-dashboard__panel-title span,
.base-dashboard__list span,
.base-dashboard__metrics span {
  color: var(--admin-muted, #666666);
  font-style: normal;
}

.base-dashboard__status strong {
  display: block;
  margin-top: 6px;
  color: var(--admin-success, #00c18d);
  font-size: 20px;
  font-weight: 700;
}

.base-dashboard__summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.base-dashboard__item {
  min-height: 132px;
  padding: 18px;
  border: 1px solid var(--admin-border-soft, #edf2f7);
  border-radius: var(--admin-radius, 8px);
  background: #fff;
  box-shadow: var(--admin-shadow, 0 8px 24px rgba(15, 23, 42, 0.05));
}

.base-dashboard__item.is-primary {
  border-color: #b8d2ff;
  background: linear-gradient(135deg, #f7faff 0%, #ffffff 78%);
}

.base-dashboard__item span {
  display: block;
  margin-bottom: 8px;
}

.base-dashboard__item strong {
  display: block;
  margin-bottom: 18px;
  color: var(--admin-title, #1a1a1a);
  font-size: 18px;
  font-weight: 700;
}

.base-dashboard__item em {
  display: block;
  font-size: 13px;
}

.base-dashboard__panels {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.65fr);
  gap: 16px;
}

.base-dashboard__panel {
  padding: 18px;
  border: 1px solid var(--admin-border-soft, #edf2f7);
  border-radius: var(--admin-radius, 8px);
  background: #fff;
}

.base-dashboard__panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.base-dashboard__panel-title h2 {
  margin: 0;
  color: var(--admin-title, #1a1a1a);
  font-size: 16px;
  font-weight: 600;
}

.base-dashboard__list {
  display: grid;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.base-dashboard__list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 6px;
  background: #f9fafb;
}

.base-dashboard__list strong {
  color: var(--admin-primary, #0066ff);
  font-size: 13px;
}

.base-dashboard__metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.base-dashboard__metrics div {
  min-height: 84px;
  padding: 14px;
  border-radius: 6px;
  background: #f9fafb;
}

.base-dashboard__metrics strong {
  display: block;
  margin-top: 10px;
  color: var(--admin-title, #1a1a1a);
  font-size: 22px;
}

@media (max-width: 1200px) {
  .base-dashboard__summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .base-dashboard__panels {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .base-dashboard__header {
    align-items: stretch;
    flex-direction: column;
  }

  .base-dashboard__summary,
  .base-dashboard__metrics {
    grid-template-columns: 1fr;
  }
}
</style>
