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
            <el-form-item label="点位任务状态" prop="status">
                <el-select v-model="queryParams.status" placeholder="点位任务状态" clearable style="width: 200px">
                    <el-option
                        v-for="dict in iiot_modbus_model_task_status"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery" v-hasPermi="['iiot:task:list']">搜索</el-button>
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
                    v-hasPermi="['iiot:task:add']"
                >新增</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="success"
                    plain
                    icon="Edit"
                    :disabled="single"
                    @click="handleUpdate"
                    v-hasPermi="['iiot:task:edit']"
                >修改</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="danger"
                    plain
                    icon="Delete"
                    :disabled="multiple"
                    @click="handleDelete"
                    v-hasPermi="['iiot:task:remove']"
                >删除</el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="唯一标识符" align="center" prop="pointId" />
            <el-table-column label="解析器名称" align="center" prop="parserName" />
            <el-table-column label="寄存器地址" align="center" prop="startAddress" width="100"/>
            <el-table-column label="寄存器数量" align="center" prop="quantity" width="100"/>
            <el-table-column label="数据采集间隔" align="center" prop="collectIntervalSeconds" width="120"/>
            <el-table-column label="点位任务状态" align="center" prop="status" width="120">
<!--                <template #default="scope">-->
<!--                    <dict-tag :options="iiot_modbus_model_task_status" :value="scope.row.status" />-->
<!--                </template>-->
                <template #default="scope">
                    <el-switch
                        v-model="scope.row.statusValue"
                        active-value="1"
                        inactive-value="0"
                        @change="handleUpdateStatus(scope.row)"
                    ></el-switch>
                </template>
            </el-table-column>
            <el-table-column label="描述信息" align="center" prop="description"/>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['iiot:task:edit']">修改</el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['iiot:task:remove']">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 添加或修改采集属性对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="taskRef" :model="form" :rules="rules" label-width="100px">
                <el-form-item label="唯一标识符" prop="pointId">
                    <el-input v-model="form.pointId" placeholder="请输入唯一标识符" />
                </el-form-item>
                <el-form-item label="解析器名称" prop="parserName">
                    <el-input v-model="form.parserName" placeholder="请输入解析器名称" />
                </el-form-item>
                <el-form-item label="寄存器地址" prop="startAddress">
                    <el-input-number v-model="form.startAddress" placeholder="" :controls="false" :precision="0" />
                </el-form-item>
                <el-form-item label="寄存器数量" prop="quantity">
                    <el-input-number v-model="form.quantity" placeholder="" :controls="false" :precision="0" />
                </el-form-item>
                <el-form-item label="数据采集间隔" prop="collectIntervalSeconds">
                    <el-input-number v-model="form.collectIntervalSeconds" placeholder="" :controls="false" :precision="0">

                    </el-input-number>
                    <div class="ml10">秒</div>
                </el-form-item>
<!--                <el-form-item label="点位任务状态" prop="status">-->
<!--                    <el-radio-group v-model="form.status">-->
<!--                        <el-radio-button-->
<!--                            v-for="dict in iiot_modbus_model_task_status"-->
<!--                            :key="dict.value"-->
<!--                            :label="dict.value"-->
<!--                        >{{ dict.label }}</el-radio-button>-->
<!--                    </el-radio-group>-->
<!--                </el-form-item>-->
                <el-form-item label="描述信息" prop="description">
                    <el-input v-model="form.description" placeholder="请输入描述信息" />
                </el-form-item>
                <el-form-item label="启用存储" prop="persistenceConfig.enabled">
                    <el-switch v-model="form.persistenceConfig.enabled" />
                </el-form-item>
                <el-form-item label="存储表名" prop="description">
                    <el-input v-model="form.persistenceConfig.targetStable" placeholder="请输入存储表名" />
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
import {
    addTask,
    delTask,
    getTask,
    listTask,
    updateTask,
    startModbusTask,
    stopModbusTask,
    changeStatusTask
} from "@/api/iiot/task";

const { proxy } = getCurrentInstance();
const { iiot_modbus_model_task_status } = proxy.useDict("iiot_modbus_model_task_status");

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

const taskList = ref([]);
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
        startAddress: [{ required: true, message: "寄存器地址不能为空", trigger: "blur" }],
    }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询主题列表 */
function getList() {
    loading.value = true;
    return listTask({ ...queryParams.value, productId: props.productId }).then(response => {
        taskList.value = response.map(item => {
            return {
                ...item,
                statusValue: item.status === 'ENABLED' ? '1' : '0'
            }
        });
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
        parserName: undefined,
        startAddress: undefined,
        quantity: undefined,
        collectIntervalSeconds: undefined,
        status: 'DISABLED',
        description: undefined,
        persistenceConfig: {
            enabled: false,
            targetStable: ''
        }
    };
    proxy.resetForm("taskRef");
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
    title.value = "添加采集属性";
}
/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const taskId = row.id || ids.value;
    getTask(taskId).then(response => {
        form.value = {
            ...response.data,
            persistenceConfig: JSON.parse(response.data.persistenceConfig) || { enabled: false, targetStable: '' }
        };
        open.value = true;
        title.value = "修改采集属性";
    });
}
/** 提交按钮 */
function submitForm() {
    proxy.$refs["taskRef"].validate(valid => {
        if (valid) {
            let data = {
                ...form.value,
                persistenceConfig: JSON.stringify(form.value.persistenceConfig)
            };
            if (form.value.id != undefined) {
                updateTask(data).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addTask(data).then(response => {
                    proxy.$modal.msgSuccess("新增成功");
                    open.value = false;
                    getList();
                });
            }
        }
    });
}
/** 启用禁用按钮操作 */
function handleUpdateStatus(row) {
    const taskIds = row.id || ids.value;
    const taskPointIds = row.pointId || pointIds.value;
    proxy.$modal.confirm('是否确认' + (row.statusValue === '1' ? "启用" : "禁用") + '唯一标识符为"' + taskPointIds + '"的数据项？').then(async function () {
        if(row.statusValue === '1') {
            await changeStatusTask(taskIds, row.statusValue === '1' ? "ENABLED" : "DISABLED");
            return startModbusTask(taskIds);
        } else {
            await stopModbusTask(taskIds);
            return changeStatusTask(taskIds, row.statusValue === '1' ? "ENABLED" : "DISABLED");
        }
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess((row.statusValue === '1' ? "启用" : "禁用") + "成功");
    }).catch(() => {
        row.statusValue = row.statusValue === "0" ? "1" : "0";
        row.status = row.statusValue === "0" ? "ENABLED" : "DISABLED";
    });
}
/** 删除按钮操作 */
function handleDelete(row) {
    const taskIds = row.id || ids.value;
    const taskPointIds = row.pointId || pointIds.value;
    proxy.$modal.confirm('是否确认删除唯一标识符为"' + taskPointIds + '"的数据项？').then(function() {
        return delTask(taskIds);
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

</style>
