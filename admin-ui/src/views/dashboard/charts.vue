<template>
  <div class="base-dashboard">
    <div class="base-dashboard__header">
      <h1>{{ t('dashboard.title') }}</h1>
      <p>{{ t('dashboard.subtitle') }}</p>
    </div>
    <div class="base-dashboard__grid">
      <section class="base-dashboard__item">
        <span>{{ t('dashboard.system') }}</span>
        <strong>Users / Roles / Menus</strong>
      </section>
      <section class="base-dashboard__item">
        <span>{{ t('dashboard.tenant') }}</span>
        <strong>tenant</strong>
      </section>
      <section class="base-dashboard__item">
        <span>{{ t('dashboard.database') }}</span>
        <strong>MySQL / PostgreSQL</strong>
      </section>
    </div>
  </div>
</template>

<script setup>
import { getMessage } from '@/locales'
import useLocaleStore from '@/store/modules/locale'

const localeStore = useLocaleStore()
const t = (key) => getMessage(key, localeStore.language)
</script>

<style scoped>
.base-dashboard {
  padding: 24px;
}

.base-dashboard__header {
  margin-bottom: 20px;
}

.base-dashboard__header h1 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.base-dashboard__header p {
  margin: 0;
  color: #606266;
}

.base-dashboard__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.base-dashboard__item {
  padding: 18px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
}

.base-dashboard__item span {
  display: block;
  margin-bottom: 8px;
  color: #606266;
}

.base-dashboard__item strong {
  color: #111827;
  font-size: 16px;
}
</style>
