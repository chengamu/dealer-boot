<template>
  <div class="app-container engineering-check-page">
    <section class="engineering-check-page__header">
      <div>
        <p>{{ t('productCenter.engineering.eyebrow') }}</p>
        <h2>{{ t('productCenter.engineering.checkTitle') }}</h2>
        <span>{{ t('productCenter.engineering.checkDescription') }}</span>
      </div>
      <el-button type="primary" :icon="CircleCheck" @click="runCheck">{{ t('productCenter.engineering.check') }}</el-button>
    </section>

    <section class="engineering-check-page__filters">
      <el-form :model="form" inline label-width="132px">
        <el-form-item :label="t('productCenter.engineering.planTitle')">
          <el-select v-model="selectedPlanId" filterable :placeholder="t('productCenter.engineering.selectPlan')" @change="loadVersions">
            <el-option v-for="plan in plans" :key="plan.planId" :label="`${plan.planCode} ${plan.planNameCn}`" :value="plan.planId" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('productCenter.engineering.versionTitle')">
          <el-select v-model="form.versionId" filterable :placeholder="t('productCenter.engineering.selectVersion')">
            <el-option v-for="version in versions" :key="version.versionId" :label="`${version.versionNo} ${version.versionName || ''}`" :value="version.versionId" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-tag :type="statusType" size="large">{{ result.status || '-' }}</el-tag>
    </section>

    <section class="engineering-check-page__issues">
      <el-empty v-if="!issues.length" :description="t('productCenter.engineering.noIssues')" />
      <el-table v-else :data="issues" border>
        <el-table-column prop="severity" :label="t('productCenter.engineering.severity')" width="130" />
        <el-table-column prop="code" :label="t('productCenter.engineering.ruleCode')" width="180" show-overflow-tooltip />
        <el-table-column prop="message" :label="t('productCenter.engineering.messageCn')" min-width="260" show-overflow-tooltip />
      </el-table>
    </section>
  </div>
</template>

<script setup lang="ts" name="EngineeringCheckPage">
import { computed, onMounted, reactive, ref } from 'vue'
import { CircleCheck } from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { engineeringApi, engineeringPlanApi } from '@/api/product-capability/engineering'
import type { EngineeringPlanVO, EngineeringPlanVersionVO } from '@/api/product-capability/types'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const plans = ref<EngineeringPlanVO[]>([])
const versions = ref<EngineeringPlanVersionVO[]>([])
const selectedPlanId = ref<number>()
const result = ref<Record<string, any>>({})
const form = reactive({ versionId: undefined as number | undefined })

const issues = computed(() => (Array.isArray(result.value.issues) ? result.value.issues : []))
const statusType = computed(() => {
  if (result.value.status === 'BLOCKER') return 'danger'
  if (result.value.status === 'WARNING') return 'warning'
  return 'success'
})

onMounted(async () => {
  const response = await engineeringPlanApi.options({ status: 'ENABLED' })
  plans.value = Array.isArray(response) ? response : response?.data || []
  selectedPlanId.value = plans.value[0]?.planId
  await loadVersions()
})

async function loadVersions() {
  const response = await engineeringApi.versions.list({ pageNum: 1, pageSize: 50, planId: selectedPlanId.value })
  versions.value = response.rows || []
  form.versionId = versions.value[0]?.versionId
  await runCheck()
}

async function runCheck() {
  if (!form.versionId) return
  const response = await engineeringApi.check(form.versionId)
  result.value = response?.data || response || {}
}
</script>

<style scoped lang="scss">
.engineering-check-page {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.engineering-check-page__header,
.engineering-check-page__filters,
.engineering-check-page__issues {
  border: 1px solid #e5edf7;
  border-radius: 6px;
  background: #fff;
}

.engineering-check-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;

  p {
    margin: 0 0 4px;
    color: #1f78ff;
    font-weight: 700;
  }

  h2 {
    margin: 0 0 4px;
  }

  span {
    color: #607086;
  }
}

.engineering-check-page__filters {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px;
}

.engineering-check-page__issues {
  min-height: 460px;
  padding: 14px;
}
</style>
