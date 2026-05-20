<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="行为" prop="action">
        <el-select v-model="queryParams.action" placeholder="行为" clearable style="width: 200px">
          <el-option
            v-for="dict in iiot_acl_action"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备权限" prop="permission">
        <el-select v-model="queryParams.permission" placeholder="设备权限" clearable style="width: 200px">
          <el-option
            v-for="dict in iiot_acl_permission"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery" v-hasPermi="['iiot:acl:list']">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['iiot:acl:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['iiot:acl:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['iiot:acl:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Edit"
          @click="handleInit"
          v-hasPermi="['iiot:acl:init']"
          :disabled="!props.userName"
        >初始化</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="aclList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="关联用户名" align="center" prop="username" />
      <el-table-column label="指令" align="center" prop="topic" width="450"/>
      <el-table-column label="行为" align="center" prop="action">
        <template #default="scope">
          <dict-tag :options="iiot_acl_action" :value="scope.row.action" />
        </template>
      </el-table-column>
      <el-table-column label="设备权限" align="center" prop="permission">
        <template #default="scope">
          <dict-tag :options="iiot_acl_permission" :value="scope.row.permission" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['iiot:acl:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['iiot:acl:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加或修改订阅主题对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="aclRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入关联用户名" disabled />
        </el-form-item>
        <el-form-item label="指令" prop="topic">
          <el-input v-model="form.topic" placeholder="请输入指令" />
        </el-form-item>
        <el-form-item label="行为" prop="action">
          <el-radio-group v-model="form.action">
            <el-radio-button
              v-for="dict in iiot_acl_action"
              :key="dict.value"
              :label="dict.value"
            >{{ dict.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设备权限" prop="permission">
          <el-radio-group v-model="form.permission">
            <el-radio-button
              v-for="dict in iiot_acl_permission"
              :key="dict.value"
              :label="dict.value"
            >{{ dict.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SubscriptionTheme">
import {addAcl, delAcl, getAcl, initAcl, listAcl, updateAcl} from "@/api/iiot/acl";

const { proxy } = getCurrentInstance();
const { iiot_acl_action, iiot_acl_permission } = proxy.useDict("iiot_acl_action", "iiot_acl_permission");

const props = defineProps({
  productId: {
    type: [String, Number],
    default: 0
  },
    code: {
        type: String,
        default: ''
    },
  userName: {
    type: String,
    default: ''
  },
})

const aclList = ref([]);
const open = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref([]);
const topics = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    action: undefined,
    permission: undefined
  },
  rules: {
    username: [{ required: true, message: "关联用户名不能为空", trigger: "blur" }],
    topic: [{ required: true, message: "指令不能为空", trigger: "blur" }],
    action: [{ required: true, message: "行为不能为空", trigger: "change" }],
    permission: [{ required: true, message: "设备权限不能为空", trigger: "change" }],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 监听 userName 参数变化 */
watch(() => props.userName, (newVal) => {
  getList();
}, { immediate: false });

/** 查询主题列表 */
function getList() {
  if(props.userName) {
    loading.value = true;
    listAcl({ ...queryParams.value, username: props.userName }).then(response => {
      aclList.value = response;
      total.value = response.total;
      loading.value = false;
    });
  }
}
/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}
/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    username: props.userName,
    topic: undefined,
    action: undefined,
    permission: undefined,
  };
  proxy.resetForm("aclRef");
}
/** 搜索按钮操作 */
function handleQuery() {
  getList();
}
/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}
/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  topics.value = selection.map(item => item.topic);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}
/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加订阅主题";
}
/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const aclId = row.id || ids.value;
  getAcl(aclId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改订阅主题";
  });
}
/** 提交按钮 */
function submitForm() {
  proxy.$refs["aclRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateAcl(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addAcl(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}
/** 删除按钮操作 */
function handleDelete(row) {
  const aclIds = row.id || ids.value;
  const aclTopics = row.topic || topics.value;
  proxy.$modal.confirm('是否确认删除指令为"' + aclTopics + '"的数据项？').then(function() {
    return delAcl(aclIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}
/** 删除按钮操作 */
function handleInit() {
  if(props.userName) {
    proxy.$modal.confirm('是否确认初始化订阅主题？').then(function () {
      return initAcl(props.code, props.userName);
    }).then(() => {
      getList();
      proxy.$modal.msgSuccess("初始化成功");
    }).catch(() => {
    });
  }
}

onMounted(() => {
  getList();
});
</script>

<style scoped lang="scss">

</style>
