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
                    v-hasPermi="['iiot:sceneCondition:add']"
                >新增</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="success"
                    plain
                    icon="Edit"
                    :disabled="selectSceneConditionList.length === 0"
                    @click="handleUpdateBatch"
                    v-hasPermi="['iiot:sceneCondition:edit']"
                >修改</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="danger"
                    plain
                    icon="Delete"
                    :disabled="multiple"
                    @click="handleDelete"
                    v-hasPermi="['iiot:sceneCondition:remove']"
                >删除</el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="sceneConditionList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="产品名称" align="center" prop="productName" :show-overflow-tooltip="true" />
            <el-table-column label="产品编码" align="center" prop="productCode" />
            <el-table-column label="点位编码" align="center" prop="pointId" :show-overflow-tooltip="true" />
            <el-table-column label="点位描述" align="center" prop="description" />
            <el-table-column label="条件类型" align="center" prop="conditionType">
                <template #default="scope">
                    <dict-tag :options="control_scene_condition_type" :value="scope.row.conditionType" />
                </template>
            </el-table-column>
            <el-table-column label="运算符" align="center" prop="operator">
                <template #default="scope">
                    <dict-tag :options="control_scene_condition_operator" :value="scope.row.operator" />
                </template>
            </el-table-column>
            <el-table-column label="条件值" align="center" prop="conditionValue" />
            <el-table-column label="写入值" align="center" prop="targetValue" width="100" />
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
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['iiot:sceneCondition:edit']">修改</el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['iiot:sceneCondition:remove']">删除</el-button>
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

        <!-- 批量添加规则引擎对话框 -->
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
                <el-table-column label="点位描述" align="center" prop="description" :show-overflow-tooltip="true" />
                <el-table-column label="条件类型" align="center" prop="conditionType" width="150">
                    <template #default="scope">
                        <el-select v-model="scope.row.conditionType">
                            <el-option
                                v-for="item in control_scene_condition_type"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value"
                            ></el-option>
                        </el-select>
                    </template>
                </el-table-column>
                <el-table-column label="运算符" align="center" prop="operator" width="100">
                    <template #default="scope">
                        <el-select v-model="scope.row.operator">
                            <el-option
                                v-for="item in control_scene_condition_operator"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value"
                            ></el-option>
                        </el-select>
                    </template>
                </el-table-column>
                <el-table-column label="条件值" align="center" prop="conditionValue" width="100">
                    <template #default="scope">
                        <el-input v-model="scope.row.conditionValue" placeholder="条件值" />
                    </template>
                </el-table-column>
                <el-table-column label="写入值" align="center" prop="targetValue" width="100">
                    <template #default="scope">
                        <el-input v-model="scope.row.targetValue" placeholder="写入值" />
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

        <!-- 批量修改规则引擎对话框 -->
        <el-dialog :title="titleEditBatch" v-model="openEditBatch" width="1500px" append-to-body>
            <el-table :data="selectSceneConditionList" ref="selectSceneConditionListTableRef">
                <el-table-column label="产品名称" align="center" prop="productName" :show-overflow-tooltip="true" width="200" />
                <el-table-column label="产品编码" align="center" prop="productCode" width="200" />
                <el-table-column label="点位编码" align="center" prop="pointId" :show-overflow-tooltip="true" width="200" />
                <el-table-column label="点位描述" align="center" prop="description" :show-overflow-tooltip="true" />
                <el-table-column label="条件类型" align="center" prop="conditionType" width="150">
                    <template #default="scope">
                        <el-select v-model="scope.row.conditionType">
                            <el-option
                                v-for="item in control_scene_condition_type"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value"
                            ></el-option>
                        </el-select>
                    </template>
                </el-table-column>
                <el-table-column label="运算符" align="center" prop="operator" width="100">
                    <template #default="scope">
                        <el-select v-model="scope.row.operator">
                            <el-option
                                v-for="item in control_scene_condition_operator"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value"
                            ></el-option>
                        </el-select>
                    </template>
                </el-table-column>
                <el-table-column label="条件值" align="center" prop="conditionValue" width="100">
                    <template #default="scope">
                        <el-input v-model="scope.row.conditionValue" placeholder="条件值" />
                    </template>
                </el-table-column>
                <el-table-column label="写入值" align="center" prop="targetValue" width="100">
                    <template #default="scope">
                        <el-input v-model="scope.row.targetValue" placeholder="写入值" />
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

        <!-- 修改规则引擎对话框 -->
        <el-dialog :title="titleEdit" v-model="openEdit" width="500px" append-to-body>
            <el-form ref="sceneConditionRef" :model="form" :rules="rules" label-width="120px">
                <el-form-item label="产品名称" prop="productName">
                    <el-input v-model="form.productName" placeholder="请输入产品名称" disabled />
                </el-form-item>
                <el-form-item label="产品编码" prop="productCode">
                    <el-input v-model="form.productCode" placeholder="请输入产品编码" disabled />
                </el-form-item>
                <el-form-item label="点位编码" prop="pointId">
                    <el-input v-model="form.pointId" placeholder="请输入点位编码" disabled />
                </el-form-item>
                <el-form-item label="点位描述" prop="description">
                    <el-input v-model="form.description" placeholder="请输入点位描述" disabled />
                </el-form-item>
                <el-form-item label="条件类型" prop="conditionType">
                    <el-select v-model="form.conditionType">
                        <el-option
                            v-for="item in control_scene_condition_type"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="运算符" prop="operator">
                    <el-select v-model="form.operator">
                        <el-option
                            v-for="item in control_scene_condition_operator"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="条件值" prop="conditionValue">
                    <el-input v-model="form.conditionValue" placeholder="请输入条件值"></el-input>
                </el-form-item>
                <el-form-item label="写入值" prop="targetValue">
                    <el-input v-model="form.targetValue" placeholder="请输入写入值"></el-input>
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

<script setup name="SceneCondition">
import {
    delSceneCondition,
    getSceneCondition,
    listSceneCondition,
    updateSceneCondition,
    changeStatusSceneCondition,
    getProductPointListCondition, addSceneConditionBatch, updateSceneConditionBatch
} from "@/api/control/scene";
import {onMounted} from "vue";

const {proxy} = getCurrentInstance();
const { control_scene_condition_type, control_scene_condition_operator, sys_normal_disable } = proxy.useDict("control_scene_condition_type", "control_scene_condition_operator", "sys_normal_disable");

const productPointList = ref([])
const sceneConditionList = ref([
    // { id: 1, sceneConditionCode: 'cygm', name: '产业规模', unit: '亿元', remark: '无' },
    // { id: 2, sceneConditionCode: 'jhztz', name: '计划总投资', unit: '亿元', remark: '无' },
    // { id: 3, sceneConditionCode: 'yftr', name: '研发投入（R&D）', unit: '亿元', remark: '无' },
]);
const selectSceneConditionList = ref([])
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
        pageNum: 1,
        pageSize: 10,
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
        conditionType: [{ required: true, message: "条件类型不能为空", trigger: "change" }],
        operator: [{ required: true, message: "运算符不能为空", trigger: "change" }],
        conditionValue: [{ required: true, message: "条件值不能为空", trigger: "blur" }],
        targetValue: [{ required: true, message: "写入值不能为空", trigger: "blur" }],
    },
});

const { queryParams, queryParamsPPLT, form, rules } = toRefs(data);

/** 查询规则引擎列表 */
function getProductPointListFunc() {
    return getProductPointListCondition({ ...queryParamsPPLT.value, sceneId: route.params.sceneId }).then(response => {
        productPointList.value = response.data.map(it => {
            return {
                ...it,
                sceneId: route.params.sceneId,
                conditionType: 'presence',
                operator: '=',
                targetValue: undefined,
                status: 0,
            };
        });
    });
}

/** 查询规则引擎列表 */
function getList() {
    loading.value = true;
    return listSceneCondition(queryParams.value).then(response => {
        sceneConditionList.value = response.rows;
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
        conditionType: undefined,
        operator: undefined,
        conditionValue: undefined,
        targetValue: undefined,
        status: 1,
    };
    proxy.resetForm("sceneConditionRef");
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
    selectSceneConditionList.value = selection;
    single.value = selection.length !== 1;
    multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAddBatch() {
    openAddBatch.value = true;
    titleAddBatch.value = "添加规则引擎";
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
    if(selectSceneConditionList.value.length === 1) {
        handleUpdate({})
    } else {
        openEditBatch.value = true;
        titleEditBatch.value = "添加规则引擎";
    }
}
/** 修改按钮操作-单条 */
function handleUpdate(row) {
    reset();
    const sceneConditionId = row.id || ids.value;
    getSceneCondition(sceneConditionId).then(response => {
        form.value = response.data;
        openEdit.value = true;
        titleEdit.value = "修改规则引擎";
    });
}

/** 提交按钮-批量新增 */
function submitFormAddBatch() {
    console.log(selectProductPointList.value)
    if(selectProductPointList.value.length === 0) {
        proxy.$modal.msgError("请选择要添加的规则引擎");
        return
    }
    const completeData = selectProductPointList.value.filter(it => it.conditionType && it.operator && it.conditionValue && it.targetValue)
    if(completeData.length === selectProductPointList.value.length) {
        addSceneConditionBatch(selectProductPointList.value).then(response => {
            proxy.$modal.msgSuccess("新增成功");
            openAddBatch.value = false;
            getList();
            selectProductPointList.value = []
        });
    } else {
        proxy.$modal.msgError("请完善选中数据的“条件类型”,“运算符”,“条件值”和“写入值”字段");
    }
}

/** 提交按钮-批量修改 */
function submitFormEditBatch() {
    const completeData = selectSceneConditionList.value.filter(it => it.conditionType && it.operator && it.conditionValue && it.targetValue)
    if(completeData.length === selectSceneConditionList.value.length) {
        updateSceneConditionBatch(selectSceneConditionList.value).then(response => {
            proxy.$modal.msgSuccess("修改成功");
            openEditBatch.value = false;
            getList();
            selectSceneConditionList.value = []
        });
    } else {
        proxy.$modal.msgError("请完善选中数据的“条件类型”,“运算符”,“条件值”和“写入值”字段");
    }
}

/** 提交按钮-修改 */
function submitFormEdit() {
    proxy.$refs["sceneConditionRef"].validate(valid => {
        if (valid) {
            updateSceneCondition(form.value).then(response => {
                proxy.$modal.msgSuccess("修改成功");
                openEdit.value = false;
                getList();
            });
        }
    });
}

/** 启用禁用按钮操作 */
function handleUpdateStatus(row) {
    const sceneConditionIds = row.id || ids.value;
    const sceneConditionNames = row.name || names.value;
    proxy.$modal.confirm('是否确认' + (row.status === 1 ? "启用" : "禁用") + '规则引擎名称为"' + sceneConditionNames + '"的数据项？').then(async function () {
        return changeStatusSceneCondition(sceneConditionIds, row.status);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess((row.status === 1 ? "启用" : "禁用") + "成功");
    }).catch(() => {
        row.status = row.status === 0 ? 1 : 0;
    });
}
/** 删除按钮操作 */
function handleDelete(row) {
    const sceneConditionIds = row.id || ids.value;
    const sceneConditionNames = row.name || names.value;
    proxy.$modal.confirm('是否确认删除规则引擎名称为"' + sceneConditionNames + '"的数据项？').then(function() {
        return delSceneCondition(sceneConditionIds);
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
