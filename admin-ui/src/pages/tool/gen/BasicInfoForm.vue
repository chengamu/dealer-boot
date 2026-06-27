<template>
  <el-form ref="basicInfoForm" :model="info" :rules="rules" label-width="150px">
    <el-row>
      <el-col :span="12">
        <el-form-item :label="t('gen.tableName')" prop="tableName">
          <el-input v-model="info.tableName" :placeholder="t('gen.tableNamePlaceholder')" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="t('gen.tableComment')" prop="tableComment">
          <el-input v-model="info.tableComment" :placeholder="t('gen.tableCommentPlaceholder')" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="t('gen.className')" prop="className">
          <el-input v-model="info.className" :placeholder="t('gen.className')" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="t('gen.author')" prop="functionAuthor">
          <el-input v-model="info.functionAuthor" :placeholder="t('gen.author')" />
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item :label="t('gen.remark')" prop="remark">
          <el-input v-model="info.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { GenTable } from '@/api/tool/gen'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

defineProps<{
  info: GenTable
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const basicInfoForm = ref<FormInstance>()
const rules = computed<FormRules<GenTable>>(() => ({
  tableName: [{ required: true, message: t('gen.tableNameRequired'), trigger: 'blur' }],
  tableComment: [{ required: true, message: t('gen.tableCommentRequired'), trigger: 'blur' }],
  className: [{ required: true, message: t('gen.classNameRequired'), trigger: 'blur' }],
  functionAuthor: [{ required: true, message: t('gen.authorRequired'), trigger: 'blur' }]
}))

async function validate() {
  return await basicInfoForm.value?.validate().catch(() => false)
}

defineExpose({ validate })
</script>
