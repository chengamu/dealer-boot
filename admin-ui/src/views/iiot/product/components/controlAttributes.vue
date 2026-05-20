<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="唯一标识符" prop="pointId" style="width: calc(25% - 32px)">
                <el-input
                    v-model="queryParams.pointId"
                    placeholder="请输入唯一标识符"
                    clearable
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="写入数据类型" prop="dataType">
                <el-select v-model="queryParams.dataType" placeholder="写入数据类型" clearable style="width: 200px">
                    <el-option
                        v-for="dict in iiot_modbus_model_point_data_type"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery" v-hasPermi="['iiot:point:list']">搜索</el-button>
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
                    v-hasPermi="['iiot:point:add']"
                >新增</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="success"
                    plain
                    icon="Edit"
                    :disabled="single"
                    @click="handleUpdate"
                    v-hasPermi="['iiot:point:edit']"
                >修改</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="danger"
                    plain
                    icon="Delete"
                    :disabled="multiple"
                    @click="handleDelete"
                    v-hasPermi="['iiot:point:remove']"
                >删除</el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="pointList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="唯一标识符" align="center" prop="pointId" />
            <el-table-column label="寄存器地址" align="center" prop="registerAddress"/>
            <el-table-column label="写入数据类型" align="center" prop="dataType">
                <template #default="scope">
                    <dict-tag :options="iiot_modbus_model_point_data_type" :value="scope.row.dataType" />
                </template>
            </el-table-column>
            <el-table-column label="写入的最小值" align="center" prop="minValue"/>
            <el-table-column label="写入的最大值" align="center" prop="maxValue"/>
            <el-table-column label="描述信息" align="center" prop="description"/>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['iiot:point:edit']">修改</el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['iiot:point:remove']">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 添加或修改控制属性对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="pointRef" :model="form" :rules="rules" label-width="120px">
                <el-form-item label="唯一标识符" prop="pointId">
                    <el-input v-model="form.pointId" placeholder="请输入唯一标识符" />
                </el-form-item>
                <el-form-item label="寄存器地址" prop="registerAddress">
                    <el-input-number v-model="form.registerAddress" placeholder="" :controls="false" :precision="0" />
                </el-form-item>
                <el-form-item label="写入数据类型" prop="dataType">
                    <el-select v-model="form.dataType" clearable>
                        <el-option
                            v-for="dict in iiot_modbus_model_point_data_type"
                            :key="dict.value"
                            :label="dict.label"
                            :value="dict.value"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="写入的最小值" prop="minValue">
                    <el-input-number v-model="form.minValue" placeholder="" :controls="false" :precision="0" />
                </el-form-item>
                <el-form-item label="写入的最大值" prop="maxValue">
                    <el-input-number v-model="form.maxValue" placeholder="" :controls="false" :precision="0" />
                </el-form-item>
                <el-form-item label="描述信息" prop="description">
                    <el-input v-model="form.description" placeholder="请输入描述信息" />
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

<script setup name="CollectAttributes">
import {addPoint, delPoint, getPoint, listPoint, updatePoint} from "@/api/iiot/point";

const { proxy } = getCurrentInstance();
const { iiot_modbus_model_point_data_type } = proxy.useDict("iiot_modbus_model_point_data_type");

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

const pointList = ref([]);
const open = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref([]);
const pointIds = ref([]);
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
        pointId: [{ required: true, message: "唯一标识符不能为空", trigger: "blur" }],
        dataType: [{ required: true, message: "写入数据类型不能为空", trigger: "change" }],
        registerAddress: [{ required: true, message: "寄存器地址不能为空", trigger: "blur" }],
    }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询主题列表 */
function getList() {
    loading.value = true;
    return listPoint({ ...queryParams.value, productId: props.productId }).then(response => {
        pointList.value = response;
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
        productId: props.productId,
        pointId: undefined,
        registerAddress: undefined,
        dataType: undefined,
        minValue: undefined,
        maxValue: undefined,
        description: undefined,
    };
    proxy.resetForm("pointRef");
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
    pointIds.value = selection.map(item => item.pointId);
    single.value = selection.length !== 1;
    multiple.value = !selection.length;
}
/** 新增按钮操作 */
function handleAdd() {
    reset();
    open.value = true;
    title.value = "添加控制属性";
}
/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const pointId = row.id || ids.value;
    getPoint(pointId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改控制属性";
    });
}
/** 提交按钮 */
function submitForm() {
    proxy.$refs["pointRef"].validate(valid => {
        if (valid) {
            if (form.value.id != undefined) {
                updatePoint(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addPoint(form.value).then(response => {
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
    const pointIds = row.id || ids.value;
    const pointPointIds = row.pointId || pointIds.value;
    proxy.$modal.confirm('是否确认删除唯一标识符为"' + pointPointIds + '"的数据项？').then(function() {
        return delPoint(pointIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {});
}

onMounted(async () => {
    await getList();
});
</script>

<style scoped lang="scss">
:deep(.el-select) {
    width: 100%;
}
</style>
