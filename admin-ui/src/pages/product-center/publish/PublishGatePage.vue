<template>
  <div class="app-container publish-gate">
    <section class="publish-gate__header">
      <div>
        <p>{{ t('productCenter.menu.title') }}</p>
        <h1>{{ t('productCenter.publish.title') }}</h1>
        <span>{{ t('productCenter.publish.workbenchHint') }}</span>
      </div>
      <el-space wrap>
        <el-button icon="CircleCheck" :loading="checkLoading" :disabled="actionLoading" @click="runCheck" v-hasPermi="['product:publish:check']">
          {{ t('productCenter.publish.check') }}
        </el-button>
        <el-button type="warning" icon="Promotion" :loading="approvalLoading" :disabled="summary.resultStatus === 'BLOCKER' || actionLoading" @click="submitApproval" v-hasPermi="['product:publish:submit']">
          {{ t('productCenter.publish.submit') }}
        </el-button>
        <el-button type="primary" icon="UploadFilled" :loading="publishLoading" :disabled="summary.resultStatus === 'BLOCKER' || actionLoading" @click="runPublish" v-hasPermi="['product:publish:publish']">
          {{ t('productCenter.publish.publish') }}
        </el-button>
      </el-space>
    </section>

    <publish-status-cards :items="gateCards" />

    <el-row :gutter="12">
      <el-col :xs="24" :xl="17">
        <div class="publish-gate__panel">
          <div class="publish-gate__panel-head">
            <h2>{{ t('productCenter.publish.input') }}</h2>
            <el-tag :type="summaryTag">{{ summary.resultStatus || 'PASS' }}</el-tag>
          </div>
          <el-form inline class="publish-gate__input-form">
            <el-form-item :label="t('productCenter.model.id')">
              <el-input-number v-model="form.productModelId" :min="0" controls-position="right" />
            </el-form-item>
            <el-form-item :label="t('productCenter.model.code')">
              <el-input v-model="form.productModelCode" />
            </el-form-item>
            <el-form-item :label="t('productCenter.variant.code')">
              <el-input v-model="form.salesVariantCode" />
            </el-form-item>
            <el-form-item :label="t('productCenter.template.templateVersionId')">
              <el-input-number v-model="form.templateVersionId" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
            <el-form-item :label="t('productCenter.template.versionNo')">
              <el-input v-model="form.templateVersionNo" />
            </el-form-item>
            <el-form-item :label="t('productCenter.price.planVersionId')">
              <el-input-number v-model="form.pricePlanVersionId" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
            <el-form-item :label="t('productCenter.price.planCode')">
              <el-input v-model="form.pricePlanCode" />
            </el-form-item>
          </el-form>
        </div>
        <div class="publish-gate__panel">
          <el-tabs v-model="activeTab">
            <el-tab-pane :label="t('productCenter.publish.latestCheck')" name="latest">
              <check-item-table :rows="latestResults" :status-tag="statusTag" @open-item="openCheckItem" />
            </el-tab-pane>
            <el-tab-pane v-for="item in gridConfigs" :key="item.key" :label="t(item.titleKey)" :name="item.key">
              <product-entity-grid-page v-if="activeTab === item.key" :config="item" />
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>
      <el-col :xs="24" :xl="7">
        <publish-package-preview :form="form" :execution="execution" :approval="approval">
          <template #actions>
            <el-button type="warning" icon="Promotion" :loading="approvalLoading" :disabled="summary.resultStatus === 'BLOCKER' || actionLoading" @click="submitApproval" v-hasPermi="['product:publish:submit']">
              {{ t('productCenter.publish.submit') }}
            </el-button>
            <el-button type="primary" icon="UploadFilled" :loading="publishLoading" :disabled="summary.resultStatus === 'BLOCKER' || actionLoading" @click="runPublish" v-hasPermi="['product:publish:publish']">
              {{ t('productCenter.publish.publish') }}
            </el-button>
            <el-button icon="Calendar" disabled>{{ t('productCenter.publish.createSchedule') }}</el-button>
          </template>
        </publish-package-preview>
        <sync-status-card :execution="execution" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="PublishGatePage">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { createPublishPackage, publishApprovalApi, publishCheckResultApi, publishPackageApi, runPublishCheck, syncOutboxApi } from '@/api/product-capability/publish'
import type { ProductRecord } from '@/api/product-capability/types'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'
import PublishStatusCards, { type PublishStatusCard } from '@/pages/product-center/components/PublishStatusCards.vue'
import CheckItemTable from '@/pages/product-center/components/CheckItemTable.vue'
import PublishPackagePreview from '@/pages/product-center/components/PublishPackagePreview.vue'
import SyncStatusCard from '@/pages/product-center/components/SyncStatusCard.vue'

const localeStore = useLocaleStore()
const router = useRouter()
const t = (key: string) => getMessage(key, localeStore.language)

const activeTab = ref('latest')
const form = reactive<{ productModelId?: number; productModelCode?: string; salesVariantCode?: string; templateVersionId?: number; templateVersionNo?: string; pricePlanVersionId?: number; pricePlanCode?: string }>({})
const summary = ref<ProductRecord>({})
const execution = ref<ProductRecord>({})
const approval = ref<ProductRecord>({})
const checkLoading = ref(false)
const approvalLoading = ref(false)
const publishLoading = ref(false)

const latestResults = computed(() => (summary.value.results as ProductRecord[] | undefined) || [])
const summaryTag = computed(() => statusTag(String(summary.value.resultStatus || 'PASS')))
const actionLoading = computed(() => checkLoading.value || approvalLoading.value || publishLoading.value)
const gateCards = computed<PublishStatusCard[]>(() => [
  { key: 'pass', labelKey: 'productCenter.publish.pass', hintKey: 'productCenter.publish.passHint', value: summary.value.passCount || 0, tone: 'success' },
  { key: 'warning', labelKey: 'productCenter.template.warnings', hintKey: 'productCenter.publish.warningHint', value: summary.value.warningCount || 0, tone: 'warning' },
  { key: 'blocker', labelKey: 'productCenter.template.blockers', hintKey: 'productCenter.publish.blockerHint', value: summary.value.blockerCount || 0, tone: 'danger' },
  { key: 'pending', labelKey: 'productCenter.publish.pending', hintKey: 'productCenter.publish.pendingHint', value: summary.value.pendingCount || 0, tone: 'info' }
])
const editablePermissions = { add: 'product:publish:approve', edit: 'product:publish:approve', remove: 'product:publish:approve', reference: 'product:publish:list' }
const readonlyPermissions = { add: 'product:publish:disabled', edit: 'product:publish:disabled', remove: 'product:publish:disabled', reference: 'product:publish:list' }

const gridConfigs = computed<ProductGridConfig[]>(() => [
  {
    key: 'checks',
    titleKey: 'productCenter.publish.checkResults',
    descriptionKey: 'productCenter.publish.checkResultsDescription',
    idKey: 'checkId',
    permissions: editablePermissions,
    readonly: true,
    api: publishCheckResultApi,
    hideReference: true,
    showDetail: true,
    fields: [
      { prop: 'targetCode', labelKey: 'productCenter.workbench.targetCode', search: true },
      { prop: 'checkCode', labelKey: 'productCenter.publish.checkCode', search: true },
      { prop: 'checkNameCn', labelKey: 'productCenter.publish.checkNameCn', search: true },
      { prop: 'checkStatus', labelKey: 'productCenter.publish.checkStatus', search: true },
      { prop: 'messageCn', labelKey: 'productCenter.publish.messageCn', type: 'textarea' },
      { prop: 'resolvedFlag', labelKey: 'productCenter.publish.resolvedFlag', search: true },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true }
    ]
  },
  {
    key: 'approvals',
    titleKey: 'productCenter.publish.approvals',
    descriptionKey: 'productCenter.publish.approvalsDescription',
    idKey: 'approvalId',
    permissions: editablePermissions,
    readonly: true,
    api: publishApprovalApi,
    hideReference: true,
    showDetail: true,
    fields: [
      { prop: 'targetCode', labelKey: 'productCenter.workbench.targetCode', search: true },
      { prop: 'approvalStatus', labelKey: 'productCenter.publish.approvalStatus', search: true },
      { prop: 'submitterName', labelKey: 'productCenter.publish.submitterName', search: true },
      { prop: 'approverName', labelKey: 'productCenter.publish.approverName', search: true },
      { prop: 'approvedTime', labelKey: 'productCenter.publish.approvedTime', type: 'datetime' },
      { prop: 'approvalComment', labelKey: 'productCenter.publish.approvalComment', type: 'textarea' },
      { prop: 'snapshotHash', labelKey: 'productCenter.publish.snapshotHash' }
    ]
  },
  {
    key: 'packages',
    titleKey: 'productCenter.publish.packages',
    descriptionKey: 'productCenter.publish.packagesDescription',
    idKey: 'packageId',
    permissions: readonlyPermissions,
    readonly: true,
    api: publishPackageApi,
    hideReference: true,
    showDetail: true,
    fields: [
      { prop: 'packageCode', labelKey: 'productCenter.publish.packageCode', search: true },
      { prop: 'productModelCode', labelKey: 'productCenter.model.code', search: true },
      { prop: 'templateVersionNo', labelKey: 'productCenter.template.versionNo' },
      { prop: 'pricePlanCode', labelKey: 'productCenter.price.planCode', search: true },
      { prop: 'packageStatus', labelKey: 'productCenter.publish.packageStatus', search: true },
      { prop: 'packageHash', labelKey: 'productCenter.publish.packageHash' },
      { prop: 'publishedTime', labelKey: 'productCenter.publish.publishedTime', type: 'datetime' }
    ]
  },
  {
    key: 'outbox',
    titleKey: 'productCenter.publish.outbox',
    descriptionKey: 'productCenter.publish.outboxDescription',
    idKey: 'outboxId',
    permissions: readonlyPermissions,
    readonly: true,
    api: syncOutboxApi,
    hideReference: true,
    showDetail: true,
    fields: [
      { prop: 'packageCode', labelKey: 'productCenter.publish.packageCode', search: true },
      { prop: 'targetSystem', labelKey: 'productCenter.publish.targetSystem', search: true },
      { prop: 'eventType', labelKey: 'productCenter.workbench.eventType', search: true },
      { prop: 'syncStatus', labelKey: 'productCenter.publish.syncStatus', search: true },
      { prop: 'retryCount', labelKey: 'productCenter.workbench.retryCount', type: 'number' },
      { prop: 'nextRetryTime', labelKey: 'productCenter.publish.nextRetryTime', type: 'datetime' },
      { prop: 'lastErrorMessage', labelKey: 'productCenter.publish.lastErrorMessage', type: 'textarea' }
    ]
  }
])

function statusTag(status: string): 'success' | 'warning' | 'danger' {
  if (status === 'BLOCKER' || status === 'REJECTED' || status === 'FAILED') return 'danger'
  if (status === 'WARNING' || status === 'SUBMITTED' || status === 'PENDING') return 'warning'
  return 'success'
}

function openCheckItem(row: ProductRecord) {
  if (statusTag(String(row.checkStatus || '')) === 'danger') {
    router.push({
      path: '/product-release/gap-tasks',
      query: {
        targetCode: String(row.targetCode || form.productModelCode || ''),
        checkCode: String(row.checkCode || '')
      }
    })
    return
  }
  activeTab.value = 'checks'
}

async function runCheck() {
  if (actionLoading.value) return
  checkLoading.value = true
  try {
    const response = await runPublishCheck({ ...form })
    summary.value = response.data || {}
    execution.value = {}
    approval.value = {}
  } finally {
    checkLoading.value = false
  }
}

async function submitApproval() {
  if (actionLoading.value) return
  approvalLoading.value = true
  try {
    const response = await publishApprovalApi.submit({ ...form })
    approval.value = response.data || {}
    ElMessage.success(t('productCenter.publish.submitSuccess'))
    activeTab.value = 'approvals'
  } finally {
    approvalLoading.value = false
  }
}

async function runPublish() {
  if (actionLoading.value) return
  publishLoading.value = true
  try {
    const response = await createPublishPackage({ ...form })
    execution.value = response.data || {}
    summary.value = (execution.value.checkSummary as ProductRecord | undefined) || summary.value
  } finally {
    publishLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.publish-gate {
  display: flex;
  flex-direction: column;
  gap: 12px;
  color: #111827;
}

.publish-gate__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.publish-gate__header p {
  margin: 0 0 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.publish-gate__header h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
}

.publish-gate__header span {
  display: block;
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}

.publish-gate__card,
.publish-gate__panel {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 14px;
  background: var(--el-bg-color);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.04);
}

.publish-gate__card {
  min-height: 104px;

  span,
  p {
    color: #64748b;
    font-size: 12px;
  }

  strong {
    display: block;
    margin: 10px 0 6px;
    font-size: 30px;
    line-height: 1;
  }

  p {
    margin: 0;
  }

  &.is-success strong { color: #16a34a; }
  &.is-warning strong { color: #d97706; }
  &.is-danger strong { color: #dc2626; }
  &.is-info strong { color: #475569; }
}

.publish-gate__panel {
  margin-bottom: 12px;
}

.publish-gate__panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.publish-gate__panel-head h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 650;
}

.publish-gate__input-form {
  margin-bottom: -12px;
}

.publish-gate__package {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  border: 1px solid #e5efff;
  border-radius: 8px;
  background: #f8fbff;

  strong {
    color: #111827;
    font-size: 16px;
  }

  span {
    color: #64748b;
    font-size: 12px;
  }
}

.publish-gate__side-actions {
  width: 100%;
  margin: 12px 0;

  :deep(.el-button) {
    width: 100%;
  }
}

.publish-gate__sync {
  display: flex;
  flex-direction: column;
  gap: 10px;

  div {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    padding-bottom: 10px;
    border-bottom: 1px solid #eef2f7;
  }

  span {
    color: #64748b;
    font-size: 12px;
  }

  strong {
    color: #111827;
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .publish-gate__header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
