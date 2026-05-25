<template>
  <div class="app-container">
    <el-card>
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
              <el-form-item :label="fieldLabel('sampleName')" prop="sampleName">
                <el-input
                  v-model="queryParams.sampleName"
                  :placeholder="fieldLabel('sampleName')"
                  clearable
                  @keyup.enter="handleQuery"
                />
              </el-form-item>
              <el-form-item :label="fieldLabel('status')" prop="status">
                <el-select v-model="queryParams.status" :placeholder="t('common.selectPlaceholder')" clearable>
                  <el-option
                    v-for="dict in sys_normal_disable"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item :label="fieldLabel('createdAt')" style="width: 308px">
                <el-date-picker
                  v-model="daterangeCreatedAt"
                  type="daterange"
                  range-separator="-"
                  :start-placeholder="t('common.startDate')"
                  :end-placeholder="t('common.endDate')"
                  :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
                ></el-date-picker>
              </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
            <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="mt20">

      <el-row :gutter="10" class="mb8" type="flex" justify="space-between">
        <el-col :span="6"><span style="font-size: large">{{ t('demo.sample.title') }}</span></el-col>
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['demo:sample:add']"
          >{{ t('common.add') }}</el-button>
          <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['demo:sample:export']"
          >{{ t('common.export') }}</el-button>
        </el-col>
      </el-row>

      <el-table v-loading="loading" :data="sampleList" border class="mt20">
            <el-table-column :label="fieldLabel('id')" prop="id" v-if="false"/>
            <el-table-column :label="fieldLabel('sampleName')" prop="sampleName" />
            <el-table-column :label="fieldLabel('status')" prop="status">
              <template #default="scope">
                  <dict-tag :options="sys_normal_disable" :value="scope.row.status"/>
              </template>
            </el-table-column>
            <el-table-column :label="fieldLabel('createdAt')" align="center" prop="createdAt" width="180">
              <template #default="scope">
                <span>{{ formatUtc(scope.row.createdAt) }}</span>
              </template>
            </el-table-column>
        <el-table-column :label="t('common.operate')" align="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['demo:sample:edit']">{{ t('common.edit') }}</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['demo:sample:remove']">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-row>
        <pagination
          v-show="total>0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </el-row>

    </el-card>
    <!-- 添加或修改样本对话框 -->
    <el-drawer :title="title" v-model="open" size="50%" append-to-body>
      <el-form ref="sampleRef" :model="form" :rules="rules" label-width="80px">
              <el-form-item :label="fieldLabel('sampleName')" prop="sampleName">
                <el-input v-model="form.sampleName" :placeholder="fieldLabel('sampleName')" />
              </el-form-item>
              <el-form-item :label="fieldLabel('status')" prop="status">
                <el-select v-model="form.status" :placeholder="t('common.selectPlaceholder')">
                  <el-option
                    v-for="dict in sys_normal_disable"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item :label="fieldLabel('remark')" prop="remark">
                <el-input v-model="form.remark" type="textarea" :placeholder="t('common.required')" />
              </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">{{ t('common.confirm') }}</el-button>
          <el-button @click="cancel">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup name="Sample">
  import { listSample, getSample, delSample, addSample, updateSample } from "@/api/demo/sample";
  import { formatUtc, withUtcDateRangeParams } from "@/utils/datetime";
  import { getMessage } from "@/locales";
  import { useLocaleStore } from "@/stores/locale";

const { proxy } = getCurrentInstance();
const localeStore = useLocaleStore();
const t = (key, params) => {
  const message = getMessage(key, localeStore.language);
  if (!params) return message;
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message);
};
const fieldLabel = (field) => t('demo.sample.field.' + field);
    const { sys_normal_disable } = proxy.useDict('sys_normal_disable');

  const sampleList = ref([]);
  const open = ref(false);
  const buttonLoading = ref(false);
  const loading = ref(true);
  const ids = ref([]);
  const total = ref(0);
  const title = ref("");
    const daterangeCreatedAt = ref([]);

  const data = reactive({
    form: {},
    queryParams: {
      pageNum: 1,
      pageSize: 10,
    sampleName: undefined,
    status: undefined,
    createdAt: undefined,
  },
  rules: {
    sampleName: [
      { required: true, message: t('common.required'), trigger: "blur" }
    ],
    status: [
      { required: true, message: t('common.required'), trigger: "change" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询样本列表 */
function getList() {
  loading.value = true;
  let params = { ...queryParams.value };
  params = withUtcDateRangeParams(params, daterangeCreatedAt.value, 'beginCreatedAt', 'endCreatedAt');
  listSample(params).then(response => {
  sampleList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    sampleName: null,
    status: null,
    createdAt: null,
    remark: null
  };
  proxy.resetForm("sampleRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

    /** 重置按钮操作 */
    function resetQuery() {
      daterangeCreatedAt.value = [];
    proxy.resetForm("queryRef");
    handleQuery();
  }

  /** 新增按钮操作 */
  function handleAdd() {
    reset();
    open.value = true;
    title.value = t('demo.sample.addTitle');
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getSample(_id).then(response => {
    form.value = response.data;
  open.value = true;
  title.value = t('demo.sample.editTitle');
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["sampleRef"].validate(valid => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id != null) {
        updateSample(form.value).then(response => {
          proxy.$modal.msgSuccess(t('common.editSuccess'));
          open.value = false;
          getList();
        }).finally(() => {
          buttonLoading.value = false;
        });
      } else {
        addSample(form.value).then(response => {
          proxy.$modal.msgSuccess(t('common.addSuccess'));
          open.value = false;
          getList();
        }).finally(() => {
          buttonLoading.value = false;
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value;
  proxy.$modal.confirm(t('demo.sample.deleteConfirm', { ids: _ids })).then(function() {
    loading.value = true;
    return delSample(_ids);
  }).then(() => {
    loading.value = true;
    getList();
    proxy.$modal.msgSuccess(t('common.deleteSuccess'));
  }).catch(() => {
  }).finally(() => {
    loading.value = false;
  });
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('demo/sample/export', {
    ...queryParams.value
  }, `sample_${Date.now()}.xlsx`)
}

getList();
</script>
