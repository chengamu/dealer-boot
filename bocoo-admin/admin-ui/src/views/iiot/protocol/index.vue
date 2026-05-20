<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="协议名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入协议名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="协议代码" prop="code">
        <el-input
          v-model="queryParams.code"
          placeholder="请输入协议代码"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="传输协议" prop="type">
        <el-select v-model="queryParams.type" placeholder="传输协议" clearable style="width: 200px">
          <el-option
            v-for="dict in iiot_protocol_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
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
          v-hasPermi="['iiot:protocol:add']"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['iiot:protocol:edit']"
        >修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['iiot:protocol:remove']"
        >删除
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="protocolList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="协议名称" align="center" prop="name"/>
      <el-table-column label="协议代码" align="center" prop="code"/>
      <el-table-column label="协议版本" align="center" prop="version"/>
      <el-table-column label="传输协议" align="center" prop="type">
        <template #default="scope">
          <dict-tag :options="iiot_protocol_type" :value="scope.row.type"/>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['iiot:protocol:edit']">
            修改
          </el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['iiot:protocol:remove']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改协议对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="protocolRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="协议名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入协议名称"/>
        </el-form-item>
        <el-form-item label="协议代码" prop="code">
          <el-input v-model="form.code" placeholder="请输入协议代码"/>
        </el-form-item>
        <el-form-item label="协议版本" prop="version">
          <el-input v-model="form.version" placeholder="请输入协议版本"/>
        </el-form-item>
        <el-form-item label="传输协议" prop="type">
          <el-select v-model="form.type" placeholder="请选择">
            <el-option
              v-for="dict in iiot_protocol_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="网关类型" prop="gatewayType">
          <el-radio-group v-model="form.gatewayType">
            <el-radio-button
              v-for="item in iiot_protocol_gateway_type"
              :key="item.value"
              :label="item.value"
            >{{ item.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="连接类型" prop="connectionType">
          <el-radio-group v-model="form.connectionType">
            <el-radio-button
              v-for="item in iiot_protocol_connection_type"
              :key="item.value"
              :label="item.value"
            >{{ item.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"/>
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

<script setup name="Protocol">

import {listProtocol, getProtocol, addProtocol, delProtocol, updateProtocol} from "@/api/iiot/protocol";

const {proxy} = getCurrentInstance();
const {
  iiot_protocol_type,
  iiot_protocol_gateway_type,
  iiot_protocol_connection_type
} = proxy.useDict("iiot_protocol_type", "iiot_protocol_gateway_type", "iiot_protocol_connection_type");

const protocolList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const names = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: undefined,
    code: undefined,
    type: undefined
  },
  rules: {
    name: [{required: true, message: "协议名称不能为空", trigger: "blur"}],
    code: [{required: true, message: "协议代码不能为空", trigger: "blur"}],
    version: [{required: true, message: "协议版本不能为空", trigger: "blur"}],
    type: [{required: true, message: "传输协议不能为空", trigger: "change"}],
    gatewayType: [{required: true, message: "网关类型不能为空", trigger: "change"}],
    connectionType: [{required: true, message: "连接类型不能为空", trigger: "change"}],
  }
});

const {queryParams, form, rules} = toRefs(data);

/** 查询协议列表 */
function getList() {
  loading.value = true;
  listProtocol(queryParams.value).then(response => {
    protocolList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
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
    name: undefined,
    code: undefined,
    version: undefined,
    type: undefined,
    gatewayType: undefined,
    connectionType: undefined,
    remark: undefined,
  };
  proxy.resetForm("protocolRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
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
  names.value = selection.map(item => item.name);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加协议";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const protocolId = row.id || ids.value;
  getProtocol(protocolId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改协议";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["protocolRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateProtocol(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addProtocol(form.value).then(response => {
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
  const protocolIds = row.id || ids.value;
  const protocolNames = row.name || names.value;
  proxy.$modal.confirm('是否确认删除协议名称为"' + protocolNames + '"的数据项？').then(function () {
    return delProtocol(protocolIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {
  });
}

getList();
</script>

<style scoped lang="scss">
:deep(.el-select) {
  width: 100%;
}
</style>
