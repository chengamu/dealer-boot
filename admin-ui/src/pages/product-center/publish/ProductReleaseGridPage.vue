<template>
  <div class="app-container product-center-page">
    <product-entity-grid-page v-if="activeConfig" :config="activeConfig" />
  </div>
</template>

<script setup lang="ts" name="ProductReleaseGridPage">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { gapTaskApi, publishApprovalApi, publishPackageApi, syncOutboxApi } from '@/api/product-capability/publish'
import type { ProductRecord } from '@/api/product-capability/types'
import ProductEntityGridPage, { type ProductGridConfig } from '@/pages/product-center/components/ProductEntityGridPage.vue'

const route = useRoute()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const editablePermissions = {
  add: 'product:publish:approve',
  edit: 'product:publish:approve',
  remove: 'product:publish:approve',
  reference: 'product:publish:list'
}
const readonlyPermissions = {
  add: 'product:publish:disabled',
  edit: 'product:publish:disabled',
  remove: 'product:publish:disabled',
  reference: 'product:publish:list'
}

const configs = computed<ProductGridConfig[]>(() => [
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
    rowActions: [
      {
        labelKey: 'productCenter.publish.approve',
        icon: 'CircleCheck',
        type: 'success',
        permission: 'product:publish:approve',
        disabled: (row) => row.approvalStatus !== 'SUBMITTED',
        handler: approveRow
      },
      {
        labelKey: 'productCenter.publish.reject',
        icon: 'CircleClose',
        type: 'danger',
        permission: 'product:publish:reject',
        disabled: (row) => row.approvalStatus !== 'SUBMITTED',
        handler: rejectRow
      }
    ],
    fields: [
      { prop: 'targetType', labelKey: 'productCenter.workbench.targetType', search: true },
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
    key: 'gap-tasks',
    titleKey: 'productCenter.publish.gapTasks',
    descriptionKey: 'productCenter.publish.gapTasksDescription',
    idKey: 'checkId',
    permissions: readonlyPermissions,
    readonly: true,
    api: gapTaskApi,
    hideReference: true,
    showDetail: true,
    rowActions: [
      {
        labelKey: 'productCenter.publish.fixBlocker',
        icon: 'CircleCheck',
        type: 'success',
        permission: 'product:publish:resolve',
        disabled: (row) => row.resolvedFlag === '1',
        handler: resolveGapTask
      }
    ],
    fields: [
      { prop: 'targetCode', labelKey: 'productCenter.workbench.targetCode', search: true },
      { prop: 'checkCode', labelKey: 'productCenter.publish.checkCode', search: true },
      { prop: 'checkNameCn', labelKey: 'productCenter.publish.checkNameCn', search: true },
      { prop: 'checkLevel', labelKey: 'productCenter.publish.checkLevel', search: true },
      { prop: 'checkStatus', labelKey: 'productCenter.publish.checkStatus', search: true },
      { prop: 'messageCn', labelKey: 'productCenter.publish.messageCn', type: 'textarea' },
      { prop: 'resolvedFlag', labelKey: 'productCenter.publish.resolvedFlag', search: true },
      { prop: 'status', labelKey: 'productCenter.common.status', type: 'status', search: true }
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
    key: 'sync-outbox',
    titleKey: 'productCenter.publish.outbox',
    descriptionKey: 'productCenter.publish.outboxDescription',
    idKey: 'outboxId',
    permissions: readonlyPermissions,
    readonly: true,
    api: syncOutboxApi,
    hideReference: true,
    showDetail: true,
    rowActions: [
      {
        labelKey: 'productCenter.publish.retrySync',
        icon: 'Refresh',
        type: 'warning',
        permission: 'product:publish:retrySync',
        disabled: (row) => row.syncStatus === 'SUCCESS',
        handler: retrySync
      }
    ],
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

const routeKey = computed(() => String(route.path).split('/').pop() || 'approvals')
const activeConfig = computed(() => configs.value.find((item) => item.key === routeKey.value) || configs.value[0])

async function approveRow(row: ProductRecord) {
  if (!row.approvalId) return
  await ElMessageBox.confirm(t('productCenter.publish.approveConfirm'), t('common.prompt'), { type: 'warning' })
  await publishApprovalApi.approve(row.approvalId as string | number)
  ElMessage.success(t('productCenter.publish.approveSuccess'))
}

async function rejectRow(row: ProductRecord) {
  if (!row.approvalId) return
  const result = await ElMessageBox.prompt(t('productCenter.publish.rejectReason'), t('productCenter.publish.reject'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    inputType: 'textarea'
  }).catch(() => undefined)
  if (!result) return
  await publishApprovalApi.reject(row.approvalId as string | number, { approvalComment: result.value })
  ElMessage.success(t('productCenter.publish.rejectSuccess'))
}

async function retrySync(row: ProductRecord) {
  if (!row.outboxId) return
  await syncOutboxApi.retry(row.outboxId as string | number)
  ElMessage.success(t('productCenter.publish.retrySuccess'))
}

async function resolveGapTask(row: ProductRecord) {
  if (!row.checkId) return
  await gapTaskApi.resolve(row.checkId as string | number)
  ElMessage.success(t('productCenter.publish.resolveSuccess'))
}
</script>
