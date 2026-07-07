<template>
  <div class="app-container merchant-profile-page merchant-table-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" class="merchant-table-page__search">
      <el-form-item :label="t('merchantProfile.merchantName')" prop="merchantName">
        <el-input v-model="queryParams.merchantName" :placeholder="t('merchantProfile.merchantNamePlaceholder')" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('merchantProfile.primaryEmail')" prop="primaryEmail">
        <el-input v-model="queryParams.primaryEmail" :placeholder="t('merchantProfile.primaryEmailPlaceholder')" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="t('merchantLevel.name')" prop="levelId">
        <el-select v-model="queryParams.levelId" clearable filterable style="width: 170px">
          <el-option v-for="item in levelOptions" :key="item.levelId" :label="item.levelName" :value="item.levelId" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 merchant-table-page__toolbar">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="rows" border class="merchant-table-page__table">
      <el-table-column :label="t('merchantProfile.merchantName')" prop="merchantName" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column :label="t('merchantProfile.companyName')" prop="companyName" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column :label="t('merchantProfile.primaryEmail')" prop="primaryEmail" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column :label="t('merchantLevel.name')" prop="levelName" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column :label="t('merchantLevel.discount')" width="120" align="right">
        <template #default="{ row }">{{ formatDiscountRate(row.discountRate) }}</template>
      </el-table-column>
      <el-table-column :label="t('merchantLevel.credit')" width="140" align="right">
        <template #default="{ row }">{{ formatCreditLimit(row.creditLimit) }}</template>
      </el-table-column>
      <el-table-column :label="t('merchantProfile.country')" prop="country" width="120" :show-overflow-tooltip="true" />
      <el-table-column :label="t('merchantProfile.status')" prop="status" width="110" align="center">
        <template #default="{ row }">
          <dict-tag :options="sys_normal_disable" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="t('common.createTime')" width="180" align="center">
        <template #default="{ row }">{{ formatUtc(row.createTime) }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="150" fixed="right" align="center">
        <template #default="{ row }">
          <AdminTableActions :actions="[
            { label: t('common.detail'), icon: 'View', permission: 'system:merchant:profile:query', onClick: () => openDetail(row.merchantId) },
            { label: t('common.edit'), icon: 'Edit', permission: 'system:merchant:profile:edit', onClick: () => openEdit(row.merchantId) }
          ]" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" class="merchant-table-page__pagination" @pagination="getList" />

    <AdminDrawer v-model="detailOpen" :title="t('merchantProfile.detailTitle')" size="520px" variant="detail">
      <MerchantProfileDescriptions v-if="detail" :profile="detail" />
    </AdminDrawer>

    <AdminDrawer
      v-model="editOpen"
      :title="t('merchantProfile.editTitle')"
      size="560px"
      :close-on-click-modal="false"
      :before-close="editFormCloseGuard.beforeClose"
      @closed="editFormCloseGuard.handleClosed"
    >
      <el-alert :title="t('merchantProfile.lockedHint')" type="info" show-icon :closable="false" class="mb16" />
      <el-form :model="editForm" label-width="120px">
        <el-form-item :label="t('merchantProfile.merchantName')"><el-input v-model="editForm.merchantName" disabled /></el-form-item>
        <el-form-item :label="t('merchantProfile.companyName')"><el-input v-model="editForm.companyName" disabled /></el-form-item>
        <el-form-item :label="t('merchantProfile.primaryEmail')"><el-input v-model="editForm.primaryEmail" disabled /></el-form-item>
        <el-form-item :label="t('merchantLevel.name')">
          <el-select v-model="editForm.levelId" filterable @change="handleLevelChange">
            <el-option v-for="item in levelOptions" :key="item.levelId" :label="item.levelName" :value="item.levelId" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('merchantLevel.discount')"><el-input-number v-model="editForm.discountRate" :precision="2" :min="0" :max="1" controls-position="right" /></el-form-item>
        <el-form-item :label="t('merchantLevel.credit')"><el-input-number v-model="editForm.creditLimit" :precision="2" :min="0" controls-position="right" /></el-form-item>
        <el-form-item :label="t('apply.firstName')"><el-input v-model="editForm.contactFirstName" /></el-form-item>
        <el-form-item :label="t('apply.lastName')"><el-input v-model="editForm.contactLastName" /></el-form-item>
        <el-form-item :label="t('merchantProfile.officePhone')"><el-input v-model="editForm.officePhone" /></el-form-item>
        <el-form-item :label="t('merchantProfile.mobilePhone')"><el-input v-model="editForm.mobilePhone" /></el-form-item>
        <el-form-item :label="t('apply.state')"><el-input v-model="editForm.state" /></el-form-item>
        <el-form-item :label="t('apply.city')"><el-input v-model="editForm.city" /></el-form-item>
        <el-form-item :label="t('apply.addressLine1')"><el-input v-model="editForm.addressLine1" /></el-form-item>
        <el-form-item :label="t('apply.addressLine2')"><el-input v-model="editForm.addressLine2" /></el-form-item>
        <el-form-item :label="t('apply.postalCode')"><el-input v-model="editForm.postalCode" /></el-form-item>
        <el-form-item :label="t('merchantProfile.remark')"><el-input v-model="editForm.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <div class="merchant-profile-page__drawer-footer">
          <el-button @click="editFormCloseGuard.closeWithGuard">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="submitEdit">{{ t('common.confirm') }}</el-button>
        </div>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getMerchantProfile, listMerchantProfiles, updateMerchantProfile, type MerchantProfile, type MerchantProfileQuery } from '@/api/merchant/profile'
import { optionsMerchantLevels, type MerchantLevel } from '@/api/merchant/level'
import { formatCreditLimit, formatDiscountRate } from '@/api/merchant/format'
import { formatUtc } from '@/utils/datetime'
import { useDict } from '@/utils/dict'
import MerchantProfileDescriptions from '@/pages/merchant/MerchantProfileDescriptions.vue'
import { useFormCloseGuard } from '@/composables/useFormCloseGuard'

const { t } = useI18n()
const { sys_normal_disable } = useDict('sys_normal_disable')
const queryRef = ref<FormInstance>()
const rows = ref<MerchantProfile[]>([])
const levelOptions = ref<MerchantLevel[]>([])
const detail = ref<MerchantProfile>()
const loading = ref(false)
const detailOpen = ref(false)
const editOpen = ref(false)
const showSearch = ref(true)
const total = ref(0)
const queryParams = reactive<MerchantProfileQuery>({ pageNum: 1, pageSize: 10 })
const editForm = reactive<MerchantProfile>({})
const editFormCloseGuard = useFormCloseGuard({
  enabled: () => editOpen.value,
  getSnapshot: () => JSON.stringify(editForm),
  close: () => {
    editOpen.value = false
  },
  reset: resetEditForm,
  t
})

async function getList() {
  loading.value = true
  try {
    const response = await listMerchantProfiles(queryParams)
    rows.value = response.rows || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

async function loadLevels() {
  const response = await optionsMerchantLevels({ status: 'ENABLED' })
  levelOptions.value = response.data || []
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

async function openDetail(merchantId?: number) {
  if (!merchantId) return
  const response = await getMerchantProfile(merchantId)
  detail.value = response.data
  detailOpen.value = true
}

async function openEdit(merchantId?: number) {
  if (!merchantId) return
  const response = await getMerchantProfile(merchantId)
  resetEditForm()
  Object.assign(editForm, response.data)
  editFormCloseGuard.markPristine()
  editOpen.value = true
}

function resetEditForm() {
  Object.keys(editForm).forEach((key) => delete editForm[key as keyof MerchantProfile])
}

function handleLevelChange(levelId?: number) {
  const level = levelOptions.value.find((item) => item.levelId === levelId)
  if (!level) return
  editForm.levelCode = level.levelCode
  editForm.levelName = level.levelName
  editForm.discountRate = level.defaultDiscountRate
  editForm.creditLimit = level.defaultCreditLimit
}

async function submitEdit() {
  await updateMerchantProfile({
    merchantId: editForm.merchantId,
    levelId: editForm.levelId,
    discountRate: editForm.discountRate,
    creditLimit: editForm.creditLimit,
    contactFirstName: editForm.contactFirstName,
    contactLastName: editForm.contactLastName,
    contactName: [editForm.contactFirstName, editForm.contactLastName].filter(Boolean).join(' '),
    officePhone: editForm.officePhone,
    mobilePhone: editForm.mobilePhone,
    state: editForm.state,
    city: editForm.city,
    addressLine1: editForm.addressLine1,
    addressLine2: editForm.addressLine2,
    postalCode: editForm.postalCode,
    remark: editForm.remark
  })
  ElMessage.success(t('common.editSuccess'))
  editFormCloseGuard.markPristine()
  editOpen.value = false
  await getList()
}

getList()
loadLevels()
</script>

<style scoped>
.merchant-profile-page__drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
