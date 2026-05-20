<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="事件名称" prop="name">
                <el-input
                    v-model="queryParams.name"
                    placeholder="请输入事件名称"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleQuery"
                />
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
                    v-hasPermi="['healthbrain:milestone:add']"
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
                    v-hasPermi="['healthbrain:milestone:edit']"
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
                    v-hasPermi="['healthbrain:milestone:remove']"
                >删除
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="eventList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="事件时间" align="center" prop="time" width="100"/>
            <el-table-column label="事件名称" align="center" prop="name"/>
<!--            <el-table-column label="备注" align="center" prop="remark"/>-->
            <el-table-column label="显示状态" align="center" prop="visible" width="80">
                <template #default="scope">
                    <el-switch
                        v-model="scope.row.visible"
                        active-value="1"
                        inactive-value="0"
                        @change="handleUpdateStatus(scope.row)"
                    ></el-switch>
                </template>
            </el-table-column>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['healthbrain:milestone:edit']">
                        修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['healthbrain:milestone:remove']">删除
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

        <!-- 添加或修改事件对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="eventRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="事件时间" prop="time">
                    <el-date-picker
                        v-model="form.time"
                        type="date"
                        placeholder="请选择事件时间"
                        value-format="YYYY-MM-DD HH:mm:ss"
                    />
                </el-form-item>
                <el-form-item label="事件名称" prop="name">
                    <el-input v-model="form.name" type="textarea" placeholder="请输入事件名称" :rows="3"/>
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" :rows="3"/>
                </el-form-item>
                <el-form-item label="显示状态" prop="visible">
                    <el-radio-group v-model="form.visible">
                        <el-radio
                            v-for="dict in sys_show_hide"
                            :key="dict.value"
                            :label="dict.value"
                        >{{ dict.label }}</el-radio>
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

<script setup name="Event">

import {listEvent, getEvent, addEvent, delEvent, updateEvent, changeStatusEvent} from "@/api/brain/event";
import {onMounted} from "vue";

const {proxy} = getCurrentInstance();
const {
    sys_show_hide,
} = proxy.useDict("sys_show_hide");

const eventList = ref([]);
const open = ref(false);
const loading = ref(false);
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
    },
    rules: {
        time: [{required: true, message: "事件时间不能为空", trigger: "blur"}],
        name: [{required: true, message: "事件名称不能为空", trigger: "blur"}],
    }
});

const {queryParams, form, rules} = toRefs(data);

/** 查询事件列表 */
function getList() {
    loading.value = true;
    listEvent(queryParams.value).then(response => {
        eventList.value = response.rows;
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
        time: undefined,
        name: undefined,
        remark: undefined,
        visible: '0',
    };
    proxy.resetForm("eventRef");
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
    title.value = "添加事件";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const eventId = row.id || ids.value;
    getEvent(eventId).then(response => {
        form.value = {
            ...response.data,
            time: response.data.time + ' 00:00:00',
        };
        open.value = true;
        title.value = "修改事件";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["eventRef"].validate(valid => {
        if (valid) {
            if (form.value.id != undefined) {
                updateEvent(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addEvent(form.value).then(response => {
                    proxy.$modal.msgSuccess("新增成功");
                    open.value = false;
                    getList();
                });
            }
        }
    });
}

/** 显示状态按钮操作 */
function handleUpdateStatus(row) {
    const eventIds = row.id || ids.value;
    const eventNames = row.name || names.value;
    proxy.$modal.confirm('是否确认' + (row.visible === '1' ? "显示" : "隐藏") + '事件名称为"' + eventNames + '"的数据项？').then(function () {
        return changeStatusEvent(eventIds, row.visible);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess((row.visible === '1' ? "显示" : "隐藏") + "成功");
    }).catch(() => {
        row.visible = row.visible === "0" ? "1" : "0";
    });
}

/** 删除按钮操作 */
function handleDelete(row) {
    const eventIds = row.id || ids.value;
    const eventNames = row.name || names.value;
    proxy.$modal.confirm('是否确认删除事件名称为"' + eventNames + '"的数据项？').then(function () {
        return delEvent(eventIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {
    });
}

onMounted(() => {
    getList();
})

</script>

<style scoped lang="scss">
:deep(.el-select) {
    width: 100%;
}
</style>
