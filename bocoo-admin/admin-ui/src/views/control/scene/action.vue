<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="102px">
            <el-form-item label="产品名称" prop="productName">
                <el-input
                    v-model="queryParams.productName"
                    placeholder="请输入产品名称"
                    clearable
                    style="width: 240px"
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="产品编码" prop="productCode">
                <el-input
                    v-model="queryParams.productCode"
                    placeholder="请输入产品编码"
                    clearable
                    style="width: 240px"
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="点位编码" prop="pointId">
                <el-input
                    v-model="queryParams.pointId"
                    placeholder="请输入点位编码"
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
                    @click="handleAddBatch"
                    v-hasPermi="['iiot:sceneAction:add']"
                >新增</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="success"
                    plain
                    icon="Edit"
                    :disabled="selectSceneActionList.length === 0"
                    @click="handleUpdateBatch"
                    v-hasPermi="['iiot:sceneAction:edit']"
                >修改</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="danger"
                    plain
                    icon="Delete"
                    :disabled="multiple"
                    @click="handleDelete"
                    v-hasPermi="['iiot:sceneAction:remove']"
                >删除</el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="sceneActionList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="产品名称" align="center" prop="productName" :show-overflow-tooltip="true" />
            <el-table-column label="产品编码" align="center" prop="productCode" />
            <el-table-column label="点位编码" align="center" prop="pointId" :show-overflow-tooltip="true" />
            <el-table-column label="动作描述" align="center" prop="description" :show-overflow-tooltip="true" />
            <el-table-column label="目标值" align="center" prop="targetValue" width="100" />
            <el-table-column label="动作执行顺序" align="center" prop="actionOrder" width="120" />
            <el-table-column label="动作状态" align="center" prop="status">
                <template #default="scope">
                    <el-switch
                        v-model="scope.row.status"
                        :active-value="1"
                        :inactive-value="0"
                        @change="handleUpdateStatus(scope.row)"
                    ></el-switch>
                </template>
            </el-table-column>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['iiot:sceneAction:edit']">修改</el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['iiot:sceneAction:remove']">删除</el-button>
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

        <!-- 批量添加场景联动对话框 -->
        <el-dialog :title="titleAddBatch" v-model="openAddBatch" width="1500px" append-to-body>
            <el-form :model="queryParamsPPLT" ref="queryPPLTRef" :inline="true" v-show="showSearch" label-width="102px">
                <el-form-item label="产品名称" prop="productName">
                    <el-input
                        v-model="queryParamsPPLT.productName"
                        placeholder="请输入产品名称"
                        clearable
                        style="width: 240px"
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="产品编码" prop="productCode">
                    <el-input
                        v-model="queryParamsPPLT.productCode"
                        placeholder="请输入产品编码"
                        clearable
                        style="width: 240px"
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="点位编码" prop="pointId">
                    <el-input
                        v-model="queryParamsPPLT.pointId"
                        placeholder="请输入点位编码"
                        clearable
                        style="width: 240px"
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" icon="Search" @click="handleQueryPPLT">搜索</el-button>
                    <el-button icon="Refresh" @click="resetQueryPPLT">重置</el-button>
                </el-form-item>
            </el-form>
            <el-table :data="productPointList" @selection-change="handleSelectionChangePPLT" ref="productPointListTableRef">
                <el-table-column type="selection" width="55" align="center" />
                <el-table-column label="产品名称" align="center" prop="productName" :show-overflow-tooltip="true" width="200" />
                <el-table-column label="产品编码" align="center" prop="productCode" width="200" />
                <el-table-column label="点位编码" align="center" prop="pointId" :show-overflow-tooltip="true" width="200" />
                <el-table-column label="动作描述" align="center" prop="description" :show-overflow-tooltip="true" />
                <el-table-column label="目标值" align="center" prop="targetValue" width="100">
                    <template #default="scope">
                        <el-input v-model="scope.row.targetValue" placeholder="目标值" />
                    </template>
                </el-table-column>
                <el-table-column label="动作执行顺序" align="center" prop="actionOrder" width="180">
                    <template #default="scope">
                        <el-input-number v-model="scope.row.actionOrder" placeholder="执行顺序" controls-position="right" :min="0"></el-input-number>
                    </template>
                </el-table-column>
            </el-table>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="submitFormAddBatch">确 定</el-button>
                    <el-button @click="cancel">取 消</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 批量修改场景联动对话框 -->
        <el-dialog :title="titleEditBatch" v-model="openEditBatch" width="1500px" append-to-body>
            <el-table :data="selectSceneActionList" ref="selectSceneActionListTableRef">
                <el-table-column label="产品名称" align="center" prop="productName" :show-overflow-tooltip="true" width="200" />
                <el-table-column label="产品编码" align="center" prop="productCode" width="200" />
                <el-table-column label="点位编码" align="center" prop="pointId" :show-overflow-tooltip="true" width="200" />
                <el-table-column label="动作描述" align="center" prop="description" :show-overflow-tooltip="true" />
                <el-table-column label="目标值" align="center" prop="targetValue" width="100">
                    <template #default="scope">
                        <el-input v-model="scope.row.targetValue" placeholder="目标值" />
                    </template>
                </el-table-column>
                <el-table-column label="动作执行顺序" align="center" prop="actionOrder" width="180">
                    <template #default="scope">
                        <el-input-number v-model="scope.row.actionOrder" placeholder="执行顺序" controls-position="right" :min="0"></el-input-number>
                    </template>
                </el-table-column>
            </el-table>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="submitFormEditBatch">确 定</el-button>
                    <el-button @click="cancel">取 消</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 修改场景联动对话框 -->
        <el-dialog :title="titleEdit" v-model="openEdit" width="500px" append-to-body>
            <el-form ref="sceneActionRef" :model="form" :rules="rules" label-width="120px">
                <el-form-item label="产品名称" prop="productName">
                    <el-input v-model="form.productName" placeholder="请输入产品名称" disabled />
                </el-form-item>
                <el-form-item label="产品编码" prop="productCode">
                    <el-input v-model="form.productCode" placeholder="请输入产品编码" disabled />
                </el-form-item>
                <el-form-item label="点位编码" prop="pointId">
                    <el-input v-model="form.pointId" placeholder="请输入点位编码" disabled />
                </el-form-item>
                <el-form-item label="动作描述" prop="description">
                    <el-input v-model="form.description" placeholder="请输入动作描述" disabled />
                </el-form-item>
                <el-form-item label="目标值" prop="targetValue">
                    <el-input v-model="form.targetValue" placeholder="请输入目标值"></el-input>
                </el-form-item>
                <el-form-item label="动作执行顺序" prop="actionOrder">
                    <el-input-number v-model="form.actionOrder" placeholder="请输入动作执行顺序" controls-position="right" :min="0"></el-input-number>
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="submitFormEdit">确 定</el-button>
                    <el-button @click="cancel">取 消</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup name="SceneAction">
import {
    delSceneAction,
    getSceneAction,
    listSceneAction,
    updateSceneAction,
    changeStatusSceneAction,
    getProductPointListAction, addSceneActionBatch, updateSceneActionBatch
} from "@/api/control/scene";
import {onMounted} from "vue";

const {proxy} = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict( "sys_normal_disable");

const productPointList = ref([])
const sceneActionList = ref([
    // { id: 1, sceneActionCode: 'cygm', name: '产业规模', unit: '亿元', remark: '无' },
    // { id: 2, sceneActionCode: 'jhztz', name: '计划总投资', unit: '亿元', remark: '无' },
    // { id: 3, sceneActionCode: 'yftr', name: '研发投入（R&D）', unit: '亿元', remark: '无' },
]);
const selectSceneActionList = ref([])
const openAddBatch = ref(false);
const openEditBatch = ref(false);
const openEdit = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref([]);
const names = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const titleAddBatch = ref("");
const titleEditBatch = ref("");
const titleEdit = ref("");

const productPointListTableRef = ref(null)
const selectProductPointList = ref([])

const route = useRoute();

const data = reactive({
    form: {},
    queryParams: {
        // pageNum: 1,
        // pageSize: 10,
        sceneId: route.params.sceneId,
        productName: undefined,
        productCode: undefined,
        pointId: undefined,
    },
    queryParamsPPLT: {
        pageNum: 1,
        pageSize: 10,
        productName: undefined,
        productCode: undefined,
        pointId: undefined,
    },
    rules: {
        targetValue: [{ required: true, message: "目标值不能为空", trigger: "blur" }],
        actionOrder: [{ required: true, message: "动作执行顺序不能为空", trigger: "blur" }],
    },
});

const { queryParams, queryParamsPPLT, form, rules } = toRefs(data);

/** 查询场景联动列表 */
function getProductPointListFunc() {
    return getProductPointListAction({ ...queryParamsPPLT.value, sceneId: route.params.sceneId }).then(response => {
        productPointList.value = response.data.map(it => {
            return {
                ...it,
                sceneId: route.params.sceneId,
                targetValue: undefined,
                actionOrder: undefined,
                status: 0,
            };
        });
    });
}

/** 查询场景联动列表 */
function getList() {
    loading.value = true;
    return listSceneAction(queryParams.value).then(response => {
        sceneActionList.value = response.rows.sort((a, b) => a.actionOrder - b.actionOrder);
        total.value = response.total;
        loading.value = false;
    });
}
/** 取消按钮 */
function cancel() {
    openAddBatch.value = false;
    openEditBatch.value = false;
    openEdit.value = false;
    reset();
}
/** 表单重置 */
function reset() {
    form.value = {
        id: undefined,
        sceneId: undefined,
        groupId: undefined,
        productName: undefined,
        productCode: undefined,
        pointId: undefined,
        description: undefined,
        targetValue: undefined,
        actionOrder: undefined,
        status: 1,
    };
    proxy.resetForm("sceneActionRef");
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
/** 多选框选中数据 */
function handleSelectionChange(selection) {
    ids.value = selection.map(item => item.id);
    names.value = selection.map(item => item.name);
    selectSceneActionList.value = selection;
    single.value = selection.length !== 1;
    multiple.value = !selection.length;
}

/** 新增按钮操作 */
async function handleAddBatch() {
    await getProductPointListFunc()
    openAddBatch.value = true;
    titleAddBatch.value = "添加场景联动";
}
/** 多选框选中数据-弹窗表格 */
function handleSelectionChangePPLT(selection) {
    selectProductPointList.value = selection;
}
/** 搜索按钮操作 */
function handleQueryPPLT() {
    getProductPointListFunc();
}
/** 重置按钮操作 */
function resetQueryPPLT() {
    proxy.resetForm("queryPPLTRef");
    handleQueryPPLT();
}
/** 修改按钮操作-多条 */
function handleUpdateBatch() {
    if(selectSceneActionList.value.length === 1) {
        handleUpdate({})
    } else {
        openEditBatch.value = true;
        titleEditBatch.value = "添加场景联动";
    }
}
/** 修改按钮操作-单条 */
function handleUpdate(row) {
    reset();
    const sceneActionId = row.id || ids.value;
    getSceneAction(sceneActionId).then(response => {
        form.value = response.data;
        openEdit.value = true;
        titleEdit.value = "修改场景联动";
    });
}

/** 提交按钮-批量新增 */
function submitFormAddBatch() {
    console.log(selectProductPointList.value)
    if(selectProductPointList.value.length === 0) {
        proxy.$modal.msgError("请选择要添加的场景联动");
        return
    }
    const completeData = selectProductPointList.value.filter(it => it.targetValue && it.actionOrder)
    if(completeData.length === selectProductPointList.value.length) {
        addSceneActionBatch(selectProductPointList.value).then(response => {
            proxy.$modal.msgSuccess("新增成功");
            openAddBatch.value = false;
            getList();
            selectProductPointList.value = []
        });
    } else {
        proxy.$modal.msgError("请完善选中数据的“目标值”和“动作执行顺序”字段");
    }
}

/** 提交按钮-批量修改 */
function submitFormEditBatch() {
    const completeData = selectSceneActionList.value.filter(it => it.targetValue && it.actionOrder)
    if(completeData.length === selectSceneActionList.value.length) {
        updateSceneActionBatch(selectSceneActionList.value).then(response => {
            proxy.$modal.msgSuccess("修改成功");
            openEditBatch.value = false;
            getList();
            selectSceneActionList.value = []
        });
    } else {
        proxy.$modal.msgError("请完善选中数据的“目标值”和“动作执行顺序”字段");
    }
}

/** 提交按钮-修改 */
function submitFormEdit() {
    proxy.$refs["sceneActionRef"].validate(valid => {
        if (valid) {
            updateSceneAction(form.value).then(response => {
                proxy.$modal.msgSuccess("修改成功");
                openEdit.value = false;
                getList();
            });
        }
    });
}

/** 启用禁用按钮操作 */
function handleUpdateStatus(row) {
    const sceneActionIds = row.id || ids.value;
    const sceneActionNames = row.name || names.value;
    proxy.$modal.confirm('是否确认' + (row.status === 1 ? "启用" : "禁用") + '场景联动名称为"' + sceneActionNames + '"的数据项？').then(async function () {
        return changeStatusSceneAction(sceneActionIds, row.status);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess((row.status === 1 ? "启用" : "禁用") + "成功");
    }).catch(() => {
        row.status = row.status === 0 ? 1 : 0;
    });
}
/** 删除按钮操作 */
function handleDelete(row) {
    const sceneActionIds = row.id || ids.value;
    const sceneActionNames = row.name || names.value;
    proxy.$modal.confirm('是否确认删除场景联动名称为"' + sceneActionNames + '"的数据项？').then(function() {
        return delSceneAction(sceneActionIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {});
}

onMounted(async () => {
    await getProductPointListFunc()
    await getList();
})

</script>
