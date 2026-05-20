<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="102px">
            <el-form-item label="场景编号" prop="sceneCode">
                <el-input
                    v-model="queryParams.sceneCode"
                    placeholder="请输入场景编号"
                    clearable
                    style="width: 240px"
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="场景名称" prop="name">
                <el-input
                    v-model="queryParams.name"
                    placeholder="请输入场景名称"
                    clearable
                    style="width: 240px"
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
                    v-hasPermi="['iiot:scene:add']"
                >新增</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="success"
                    plain
                    icon="Edit"
                    :disabled="single"
                    @click="handleUpdate"
                    v-hasPermi="['iiot:scene:edit']"
                >修改</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="danger"
                    plain
                    icon="Delete"
                    :disabled="multiple"
                    @click="handleDelete"
                    v-hasPermi="['iiot:scene:remove']"
                >删除</el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="sceneList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="场景编号" align="center" prop="sceneCode" />
            <el-table-column label="场景名称" align="center" prop="name" :show-overflow-tooltip="true" />
            <el-table-column label="数据状态" align="center" prop="status" width="80">
                <template #default="scope">
                    <el-switch
                        v-model="scope.row.status"
                        :active-value="1"
                        :inactive-value="0"
                        @change="handleUpdateStatus(scope.row)"
                    ></el-switch>
                </template>
            </el-table-column>
            <el-table-column label="是否为当前场景" align="center" prop="isCurrent">
                <template #default="scope">
                    <dict-tag :options="control_scene_current" :value="scope.row.isCurrent"/>
                </template>
            </el-table-column>
            <el-table-column label="排序序号" align="center" prop="sortOrder" width="80" />
            <el-table-column label="场景描述" align="center" prop="description" :show-overflow-tooltip="true" />
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="340" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['iiot:scene:edit']">修改</el-button>
                    <el-button link type="primary" icon="Edit" @click="handleUpdateAction(scope.row)" v-hasPermi="['iiot:sceneAction:list']">场景联动</el-button>
                    <el-button link type="primary" icon="Edit" @click="handleUpdateCondition(scope.row)" v-hasPermi="['iiot:sceneCondition:list']">规则引擎</el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['iiot:scene:remove']">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

<!--        <pagination-->
<!--            v-show="total > 0"-->
<!--            :total="total"-->
<!--            v-model:page="queryParams.pageNum"-->
<!--            v-model:limit="queryParams.pageSize"-->
<!--            @pagination="getList"-->
<!--        />-->

        <!-- 添加或修改参数配置对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="sceneRef" :model="form" :rules="rules" label-width="120px">
                <el-form-item label="场景编号" prop="sceneCode">
                    <el-input v-model="form.sceneCode" placeholder="请输入场景编号" />
                </el-form-item>
                <el-form-item label="场景名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入场景名称" />
                </el-form-item>
                <el-form-item label="数据状态" prop="status">
                    <el-radio-group v-model="form.status">
                        <el-radio
                            v-for="dict in sys_normal_disable"
                            :key="Number(dict.value)"
                            :label="Number(dict.value)"
                        >{{ dict.label }}</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="显示排序" prop="sortOrder">
                    <el-input-number v-model="form.sortOrder" controls-position="right" :min="0" />
                </el-form-item>
                <el-form-item label="场景描述" prop="description">
                    <el-input v-model="form.description" type="textarea" placeholder="请输入场景描述"></el-input>
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

<script setup name="Scene">
import {addScene, delScene, getScene, listScene, updateScene, changeStatusScene} from "@/api/control/scene";
import {onMounted} from "vue";

const {proxy} = getCurrentInstance();
const { control_scene_current, sys_normal_disable } = proxy.useDict("control_scene_current", "sys_normal_disable");

const sceneList = ref([
    // { id: 1, sceneCode: 'cygm', name: '产业规模', unit: '亿元', remark: '无' },
    // { id: 2, sceneCode: 'jhztz', name: '计划总投资', unit: '亿元', remark: '无' },
    // { id: 3, sceneCode: 'yftr', name: '研发投入（R&D）', unit: '亿元', remark: '无' },
]);
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
        // pageNum: 1,
        // pageSize: 10,
        sort: 'sortOrder',
        sceneCode: undefined,
        name: undefined,
    },
    rules: {
        sceneCode: [{ required: true, message: "场景编号不能为空", trigger: "blur" }],
        name: [{ required: true, message: "场景名称不能为空", trigger: "blur" }],
        status: [{ required: true, message: "单位不能为空", trigger: "change" }],
        sortOrder: [{ required: true, message: "显示排序不能为空", trigger: "change" }],
    },
});

const { queryParams, form, rules } = toRefs(data);

/** 查询场景列表 */
function getList() {
    loading.value = true;
    listScene(queryParams.value).then(response => {
        sceneList.value = response.rows.sort((a, b) => a.sortOrder - b.sortOrder);
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
        sceneCode: undefined,
        name: undefined,
        status: undefined,
        isCurrent: '0',
        sortOrder: undefined,
        description: undefined,
    };
    proxy.resetForm("sceneRef");
}
/** 搜索按钮操作 */
function handleQuery() {
    // queryParams.value.pageNum = 1;
    getList();
}
/** 重置按钮操作 */
function resetQuery() {
    proxy.resetForm("queryRef");
    handleQuery();
}
/** 新增按钮操作 */
function handleAdd() {
    reset();
    open.value = true;
    title.value = "添加场景";
}
/** 多选框选中数据 */
function handleSelectionChange(selection) {
    ids.value = selection.map(item => item.id);
    names.value = selection.map(item => item.name);
    single.value = selection.length !== 1;
    multiple.value = !selection.length;
}
/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const sceneId = row.id || ids.value;
    getScene(sceneId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改场景";
    });
}
/** 提交按钮 */
function submitForm() {
    proxy.$refs["sceneRef"].validate(valid => {
        if (valid) {
            if (form.value.id != undefined) {
                updateScene(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addScene(form.value).then(response => {
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
    const sceneIds = row.id || ids.value;
    const sceneNames = row.name || names.value;
    proxy.$modal.confirm('是否确认' + (row.status === '1' ? "启用" : "禁用") + '场景名称为"' + sceneNames + '"的数据项？').then(async function () {
        return changeStatusScene(sceneIds, row.status);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess((row.status === '1' ? "启用" : "禁用") + "成功");
    }).catch(() => {
        row.status = row.status === "0" ? "1" : "0";
    });
}
/** 删除按钮操作 */
function handleDelete(row) {
    const sceneIds = row.id || ids.value;
    const sceneNames = row.name || names.value;
    proxy.$modal.confirm('是否确认删除场景名称为"' + sceneNames + '"的数据项？').then(function() {
        return delScene(sceneIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {});
}

function handleUpdateAction(row) {
    proxy.$router.push({path: "/control/scene-action/index/" + row.id});
}

function handleUpdateCondition(row) {
    proxy.$router.push({path: "/control/scene-condition/index/" + row.id});
}

onMounted(() => {
    getList();
})

</script>
