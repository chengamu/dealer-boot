<template>
  <div class="sales-editor" v-loading="loading">
    <header class="editor-bar">
      <div><el-button link icon="ArrowLeft" @click="router.back()" /><strong>{{ document.salesDocumentId ? t('dealer.sales.edit') : t('dealer.sales.add') }}</strong><el-tag>{{ statusText }}</el-tag></div>
      <div><el-button v-if="canSave" icon="DocumentChecked" @click="save">{{ t('dealer.sales.save') }}</el-button><el-button v-if="canCalculate" icon="Calculator" @click="calculateAll">{{ t('dealer.sales.calculateAll') }}</el-button><el-button v-if="canQuote" type="primary" plain @click="makeQuote">{{ t('dealer.sales.makeQuote') }}</el-button><el-button v-if="canSubmit" type="primary" @click="submit">{{ t('dealer.sales.submit') }}</el-button></div>
    </header>
    <el-form ref="formRef" :model="document" :rules="rules" label-width="92px" class="header-form">
      <el-row :gutter="16">
        <el-col :span="6"><el-form-item :label="t('dealer.sales.customer')" prop="customerId"><el-select v-model="document.customerId" filterable @change="selectCustomer"><el-option v-for="c in customers" :key="c.customerId" :label="c.customerName" :value="String(c.customerId)" /></el-select></el-form-item></el-col>
        <el-col :span="6"><el-form-item :label="t('dealer.sales.project')"><el-input v-model="document.projectName" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item :label="t('dealer.sales.customerPo')"><el-input v-model="document.customerPoNo" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item :label="t('dealer.sales.validUntil')"><el-date-picker v-model="document.validUntil" value-format="YYYY-MM-DD" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item :label="t('dealer.sales.recipient')"><el-input v-model="document.recipientName" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item :label="t('dealer.sales.phone')"><el-input v-model="document.recipientPhone" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item :label="t('dealer.sales.address')"><el-input v-model="document.shippingAddress" /></el-form-item></el-col>
      </el-row>
    </el-form>
    <section class="line-panel">
      <div class="line-toolbar"><div><el-button type="primary" plain icon="Plus" @click="addLine">{{ t('dealer.sales.addLine') }}</el-button><el-button icon="CopyDocument" :disabled="!current" @click="current && copyLine(current)">{{ t('dealer.sales.copyLine') }}</el-button><el-button icon="Document" @click="pasteVisible = true">{{ t('dealer.sales.batchPaste') }}</el-button></div><span>{{ rows.length }} {{ t('dealer.sales.quantity') }}</span></div>
      <vxe-table ref="tableRef" :data="rows" border show-overflow :row-config="{ keyField: 'clientId', isCurrent: true }" :expand-config="{ accordion: true }" @current-change="current = $event.row" @toggle-row-expand="expandRow">
        <vxe-column type="expand" width="42"><template #content="{ row }"><div class="options"><el-form-item v-for="option in setups[String(row.saleProductId)]?.options || []" :key="option.optionCode" :label="option.optionNameCn || option.optionNameEn"><el-select v-model="row.selectedOptionValues[option.optionCode!]" clearable @change="dirty(row)"><el-option v-for="value in values(row, option.optionCode)" :key="value.valueCode" :label="value.valueNameCn || value.valueNameEn" :value="value.valueCode" /></el-select></el-form-item><el-button type="primary" plain @click="calculateLine(row)">{{ t('dealer.sales.calculateLine') }}</el-button></div></template></vxe-column>
        <vxe-column type="seq" width="55" title="#" /><vxe-column field="roomLocation" :title="t('dealer.sales.room')" min-width="130"><template #default="{ row }"><el-input v-model="row.roomLocation" /></template></vxe-column>
        <vxe-column field="saleProductId" :title="t('dealer.sales.product')" min-width="190"><template #default="{ row }"><el-select v-model="row.saleProductId" filterable @change="productChanged(row)"><el-option v-for="p in products" :key="p.saleProductId" :label="p.saleProductName" :value="String(p.saleProductId)" /></el-select></template></vxe-column>
        <vxe-column :title="t('dealer.sales.width')" width="120"><template #default="{ row }"><el-input-number v-model="row.orderWidthInch" :min="0.01" :precision="2" :controls="false" @change="dirty(row)" /></template></vxe-column>
        <vxe-column :title="t('dealer.sales.height')" width="120"><template #default="{ row }"><el-input-number v-model="row.orderHeightInch" :min="0.01" :precision="2" :controls="false" @change="dirty(row)" /></template></vxe-column>
        <vxe-column :title="t('dealer.sales.quantity')" width="80"><template #default="{ row }"><el-input-number v-model="row.quantity" :min="1" :controls="false" @change="dirty(row)" /></template></vxe-column>
        <vxe-column field="configurationSummary" :title="t('dealer.sales.configuration')" min-width="220" /><vxe-column field="calculationStatus" :title="t('common.status')" width="90" />
        <vxe-column :title="t('dealer.sales.totalAmount')" width="120"><template #default="{ row }">{{ money(row.lineAmount) }}</template></vxe-column>
        <vxe-column :title="t('common.operate')" width="70"><template #default="{ row }"><el-button link type="danger" icon="Delete" @click="rows.splice(rows.indexOf(row), 1)" /></template></vxe-column>
      </vxe-table>
    </section>
    <footer class="totals"><span>{{ t('dealer.sales.listAmount') }} <b>{{ money(document.listAmount) }}</b></span><span>{{ t('dealer.sales.discountAmount') }} <b>{{ money(document.discountAmount) }}</b></span><span>{{ t('dealer.sales.shippingAmount') }} <b>{{ money(document.shippingAmount) }}</b></span><span class="grand">{{ t('dealer.sales.totalAmount') }} <b>{{ money(document.totalAmount) }}</b></span></footer>
    <el-dialog v-model="pasteVisible" :title="t('dealer.sales.batchPaste')" width="620px"><el-input v-model="pasteText" type="textarea" :rows="8" :placeholder="t('dealer.sales.batchPasteHint')" /><template #footer><el-button @click="pasteVisible=false">{{ t('common.cancel') }}</el-button><el-button type="primary" @click="applyPaste">{{ t('common.confirm') }}</el-button></template></el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'; import { useI18n } from 'vue-i18n'; import { useRoute, useRouter } from 'vue-router'; import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'; import type { VxeTableInstance } from 'vxe-table'
import { listCustomerOptions, type CustomerProfile } from '@/api/customer/profile'; import { salesApi, type CatalogOption, type ProductSetup, type SalesDocument, type SalesItem } from '@/api/dealer-sales'
import { documentStatusText } from './salesPresentation'
import auth from '@/plugins/auth'
const { t } = useI18n(); const route = useRoute(); const router = useRouter(); const formRef = ref<FormInstance>(); const tableRef = ref<VxeTableInstance>(); const loading = ref(false)
const future = new Date(Date.now()+14*86400000).toISOString().slice(0,10); const document = reactive<SalesDocument>({ validUntil: future, items: [] }); const rows = ref<Array<SalesItem & {clientId:string}>>([]); const customers=ref<CustomerProfile[]>([]); const products=ref<any[]>([]); const setups=reactive<Record<string,ProductSetup>>({}); const current=ref<any>(); const pasteVisible=ref(false); const pasteText=ref(''); const rules={customerId:[{required:true,message:'Required'}]}
const statusText=computed(()=>documentStatusText(t,document.documentStatus||'DRAFT'))
const canSave=computed(()=>document.salesDocumentId?auth.hasPermi('dealer:sales:edit'):auth.hasPermi('dealer:sales:add'))
const canCalculate=auth.hasPermi('dealer:sales:edit')
const canQuote=canCalculate&&auth.hasPermi('dealer:sales:quote')
const canSubmit=canCalculate&&auth.hasPermi('dealer:sales:submit')
function newLine(): SalesItem & {clientId:string} { return { clientId: crypto.randomUUID(), quantity:1, selectedOptionValues:{}, calculationStatus:'FAIL' } }
function addLine(){ rows.value.push(newLine()) } function copyLine(row:any){rows.value.push({...row,clientId:crypto.randomUUID(),salesItemId:undefined,selectedOptionValues:{...row.selectedOptionValues}})} function dirty(row:any){row.calculationStatus='FAIL'}
async function productChanged(row:any){row.selectedOptionValues={};dirty(row);await loadSetup(row.saleProductId)} async function loadSetup(id?:string){if(!id||setups[id])return;setups[id]=(await salesApi.setup(id)).data||{}}
function values(row:any,code?:string){return (setups[String(row.saleProductId)]?.optionValues||[]).filter(v=>v.optionCode===code)} async function expandRow({row,expanded}:any){if(expanded)await loadSetup(row.saleProductId)}
async function calculateLine(row:any){const res=await salesApi.calculateItem(row);Object.assign(row,res.data);ElMessage.success(t('common.operationSuccess'))}
async function save(){if(!await formRef.value?.validate())return;document.items=rows.value; if(document.salesDocumentId)await salesApi.update(document);else document.salesDocumentId=String((await salesApi.add(document)).data); await reload();ElMessage.success(t('common.operationSuccess'));return document.salesDocumentId}
async function calculateAll(){const id=await save();if(!id)return;Object.assign(document,(await salesApi.calculateAll(id)).data);rows.value=(document.items||[]).map(r=>({...r,clientId:crypto.randomUUID()}))}
async function makeQuote(){await calculateAll();await salesApi.quote(document.salesDocumentId!);await reload()} async function submit(){await calculateAll();await ElMessageBox.confirm(t('dealer.sales.submitConfirm'),t('common.prompt'),{type:'warning'});await salesApi.submit(document.salesDocumentId!);void router.replace({name:'SalesDocumentDetail',params:{id:document.salesDocumentId}})}
function selectCustomer(id:any){const c=customers.value.find(v=>String(v.customerId)===String(id));if(c){document.recipientName=c.customerName;document.recipientPhone=c.phone;document.shippingAddress=[c.addressLine1,c.addressLine2,c.city,c.state,c.postalCode,c.country].filter(Boolean).join(', ')}}
function money(v?:number){return new Intl.NumberFormat('en-US',{style:'currency',currency:document.currencyCode||'USD'}).format(v||0)}
function applyPaste(){for(const line of pasteText.value.split(/\r?\n/)){const [room,code,w,h,q]=line.split('\t');const p=products.value.find(v=>v.saleProductCode===code);if(p)rows.value.push({...newLine(),roomLocation:room,saleProductId:String(p.saleProductId),orderWidthInch:Number(w),orderHeightInch:Number(h),quantity:Number(q)||1})}pasteVisible.value=false;pasteText.value=''}
async function reload(){if(!document.salesDocumentId)return;const data=(await salesApi.get(document.salesDocumentId)).data;Object.assign(document,data);rows.value=(data?.items||[]).map(r=>({...r,clientId:crypto.randomUUID()}))}
onMounted(async()=>{loading.value=true;try{const [c,p]=await Promise.all([listCustomerOptions({status:'ENABLED',pageNum:1,pageSize:500}),salesApi.products()]);customers.value=c.data||[];products.value=p.data||[];const id=String(route.query.id||'');if(id){document.salesDocumentId=id;await reload()}if(!rows.value.length)addLine()}finally{loading.value=false}})
</script>
<style scoped>
.sales-editor{min-height:calc(100vh - 88px);padding:14px 18px 92px;background:#f4f6f9}.editor-bar,.editor-bar>div,.line-toolbar,.totals{display:flex;align-items:center;justify-content:space-between;gap:10px}.header-form,.line-panel{margin-top:12px;padding:14px;border:1px solid #dfe5ed;background:#fff}.header-form :deep(.el-select),.header-form :deep(.el-date-editor){width:100%}.options{display:grid;grid-template-columns:repeat(4,minmax(180px,1fr));gap:8px 16px;padding:14px;background:#f8fafc}.options .el-form-item{margin:0}.line-toolbar{padding-bottom:10px}.totals{position:fixed;right:0;bottom:0;left:var(--sidebar-width,200px);justify-content:flex-end;padding:18px 28px;background:#fff;border-top:1px solid #dfe5ed;z-index:10}.grand{font-size:18px;color:#1677ff}.line-panel :deep(.el-input-number),.line-panel :deep(.el-select){width:100%}
</style>
