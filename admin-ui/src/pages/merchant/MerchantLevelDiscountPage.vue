<template>
  <div class="app-container merchant-table-page">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" class="merchant-table-page__search">
      <el-form-item :label="t('merchantLevel.name')" prop="levelId">
        <el-select v-model="queryParams.levelId" clearable filterable style="width: 180px">
          <el-option v-for="item in levelOptions" :key="item.levelId" :label="item.levelName" :value="item.levelId" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('merchantDiscount.category')" prop="categoryId">
        <el-select v-model="queryParams.categoryId" clearable filterable style="width: 180px">
          <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryNameCn" :value="item.categoryId" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('merchantDiscount.productType')" prop="productTypeCode">
        <el-select v-model="queryParams.productTypeCode" clearable filterable style="width: 180px">
          <el-option v-for="item in productTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select v-model="queryParams.status" clearable style="width: 150px">
          <el-option :label="t('common.enabled')" value="ENABLED" />
          <el-option :label="t('common.disabled')" value="DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 merchant-table-page__toolbar">
      <el-button type="primary" plain icon="Plus" @click="openAdd" v-hasPermi="['merchant:levelDiscount:add']">{{ t('common.add') }}</el-button>
      <el-button type="success" plain icon="Edit" :disabled="single" @click="openEdit()" v-hasPermi="['merchant:levelDiscount:edit']">{{ t('common.edit') }}</el-button>
      <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['merchant:levelDiscount:remove']">{{ t('common.delete') }}</el-button>
      <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['merchant:levelDiscount:export']">{{ t('common.export') }}</el-button>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="rows" border class="merchant-table-page__table" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column type="index" :label="t('common.index')" width="70" align="center" />
      <el-table-column :label="t('merchantLevel.name')" prop="levelName" min-width="130" />
      <el-table-column :label="t('merchantDiscount.category')" prop="categoryNameCn" min-width="150" />
      <el-table-column :label="t('merchantDiscount.productType')" prop="productTypeNameCn" min-width="150" />
      <el-table-column :label="t('merchantDiscount.rate')" prop="discountRate" width="120" align="right" />
      <el-table-column :label="t('common.status')" width="110" align="center">
        <template #default="{ row }"><el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">{{ row.status === 'ENABLED' ? t('common.enabled') : t('common.disabled') }}</el-tag></template>
      </el-table-column>
      <el-table-column :label="t('common.updateTime')" width="170" align="center">
        <template #default="{ row }">{{ formatUtc(row.updateTime || row.createTime, 'YYYY-MM-DD HH:mm') }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operate')" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <AdminTableActions :actions="[
            { label: t('common.edit'), icon: 'Edit', permission: 'merchant:levelDiscount:edit', hidden: row.status === 'ENABLED', onClick: () => openEdit(row) },
            { label: row.status === 'ENABLED' ? t('common.disable') : t('common.enable'), icon: 'Switch', permission: 'merchant:levelDiscount:edit', onClick: () => toggleStatus(row) },
            { label: t('common.delete'), icon: 'Delete', type: 'danger', permission: 'merchant:levelDiscount:remove', hidden: row.status === 'ENABLED', onClick: () => handleDelete(row) }
          ]" />
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" class="merchant-table-page__pagination" @pagination="getList" />

    <AdminDrawer v-model="open" :title="title" size="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="t('merchantLevel.name')" prop="levelId">
          <el-select v-model="form.levelId" filterable>
            <el-option v-for="item in levelOptions" :key="item.levelId" :label="item.levelName" :value="item.levelId" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('merchantDiscount.category')" prop="categoryId">
          <el-select v-model="form.categoryId" filterable>
            <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryNameCn" :value="item.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('merchantDiscount.productType')" prop="productTypeCode">
          <el-select v-model="form.productTypeCode" filterable>
            <el-option v-for="item in productTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('merchantDiscount.rate')" prop="discountRate"><el-input-number v-model="form.discountRate" :precision="4" :min="0" :max="1" controls-position="right" /></el-form-item>
        <el-form-item :label="t('common.sort')"><el-input-number v-model="form.sortOrder" :min="0" controls-position="right" /></el-form-item>
        <el-form-item :label="t('common.status')">
          <el-radio-group v-model="form.status">
            <el-radio value="ENABLED">{{ t('common.enabled') }}</el-radio>
            <el-radio value="DISABLED">{{ t('common.disabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('common.remark')"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submit">{{ t('common.confirm') }}</el-button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { changeMerchantLevelDiscountStatus, addMerchantLevelDiscount, deleteMerchantLevelDiscount, getMerchantLevelDiscount, listMerchantLevelDiscounts, updateMerchantLevelDiscount, type MerchantLevelDiscount, type MerchantLevelDiscountQuery } from '@/api/merchant/level-discount'
import { optionsMerchantLevels, type MerchantLevel } from '@/api/merchant/level'
import { productCategoryApi } from '@/api/product-capability/base'
import { getProductDictItems } from '@/api/product-capability/product-dict'
import type { ProductCategoryVO } from '@/api/product-capability/types'
import { download } from '@/utils/request'
import { formatUtc } from '@/utils/datetime'

const { t } = useI18n()
const rows = ref<MerchantLevelDiscount[]>([])
const levelOptions = ref<MerchantLevel[]>([])
const categoryOptions = ref<ProductCategoryVO[]>([])
const productTypeOptions = ref<Array<{ label: string; value: string }>>([])
const ids = ref<Array<number | string>>([])
const loading = ref(false)
const open = ref(false)
const showSearch = ref(true)
const total = ref(0)
const queryRef = ref<FormInstance>()
const formRef = ref<FormInstance>()
const queryParams = reactive<MerchantLevelDiscountQuery>({ pageNum: 1, pageSize: 10 })
const form = ref<MerchantLevelDiscount>({})
const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const title = computed(() => (form.value.discountId ? t('merchantDiscount.editTitle') : t('merchantDiscount.addTitle')))
const rules = computed<FormRules<MerchantLevelDiscount>>(() => ({
  levelId: [{ required: true, message: t('merchantDiscount.levelRequired'), trigger: 'change' }],
  categoryId: [{ required: true, message: t('merchantDiscount.categoryRequired'), trigger: 'change' }],
  productTypeCode: [{ required: true, message: t('merchantDiscount.productTypeRequired'), trigger: 'change' }],
  discountRate: [{ required: true, message: t('merchantDiscount.rateRequired'), trigger: 'blur' }]
}))

async function loadOptions() {
  const [levels, categories, productTypes] = await Promise.all([
    optionsMerchantLevels({ status: 'ENABLED' }),
    productCategoryApi.options?.({ status: 'ENABLED' }),
    getProductDictItems('product_type')
  ])
  levelOptions.value = levels.data || []
  categoryOptions.value = Array.isArray(categories) ? categories : categories?.data || []
  productTypeOptions.value = (productTypes.data || []).map((item) => ({ label: item.labelCn || item.label || item.value || '', value: item.value || '' }))
}
async function getList() {
  loading.value = true
  try {
    const res = await listMerchantLevelDiscounts(queryParams)
    rows.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}
function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}
function handleSelectionChange(selection: MerchantLevelDiscount[]) {
  ids.value = selection.map((item) => item.discountId).filter(Boolean) as number[]
}
function openAdd() {
  form.value = { discountRate: 1, sortOrder: 0, status: 'DISABLED' }
  open.value = true
}
async function openEdit(row?: MerchantLevelDiscount) {
  const id = row?.discountId || ids.value[0]
  if (!id) return
  const res = await getMerchantLevelDiscount(id)
  form.value = { ...res.data }
  open.value = true
}
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.value.discountId) await updateMerchantLevelDiscount(form.value)
  else await addMerchantLevelDiscount(form.value)
  ElMessage.success(form.value.discountId ? t('common.editSuccess') : t('common.addSuccess'))
  open.value = false
  await getList()
}
async function toggleStatus(row: MerchantLevelDiscount) {
  if (!row.discountId) return
  await changeMerchantLevelDiscountStatus(row.discountId, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED')
  ElMessage.success(t('common.operationSuccess'))
  await getList()
}
async function handleDelete(row?: MerchantLevelDiscount) {
  const target = row?.discountId || ids.value
  if (!target || (Array.isArray(target) && !target.length)) return
  await ElMessageBox.confirm(t('common.deleteConfirm'), t('common.prompt'), { type: 'warning' })
  await deleteMerchantLevelDiscount(target)
  ElMessage.success(t('common.deleteSuccess'))
  await getList()
}
function handleExport() {
  download('merchant/level-discounts/export', { ...queryParams }, `merchant_level_discount_${Date.now()}.xlsx`)
}
loadOptions()
getList()
</script>
