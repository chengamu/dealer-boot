<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="企业名称" prop="enterpriseId">
                <el-select v-model="queryParams.enterpriseId" placeholder="企业名称" clearable style="width: 200px">
                    <el-option
                        v-for="dict in enterpriseList"
                        :key="dict.id"
                        :label="dict.name"
                        :value="dict.id"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="研发管线名称" prop="name">
                <el-input
                    v-model="queryParams.name"
                    placeholder="请输入研发管线名称"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="适应症" prop="indication">
                <el-input
                    v-model="queryParams.indication"
                    placeholder="请输入适应症"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="所处阶段" prop="phase">
                <el-select v-model="queryParams.phase" placeholder="所处阶段" clearable style="width: 200px">
                    <el-option
                        v-for="dict in brain_drug_phase"
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
                    v-hasPermi="['healthbrain:drug:add']"
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
                    v-hasPermi="['healthbrain:drug:edit']"
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
                    v-hasPermi="['healthbrain:drug:remove']"
                >删除
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="drugList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="企业名称" align="center" prop="enterpriseObj.name" show-overflow-tooltip/>
            <el-table-column label="研发管线名称" align="center" prop="name" width="320"/>
            <el-table-column label="适应症" align="center" prop="indication" show-overflow-tooltip/>
            <el-table-column label="所处阶段" align="center" prop="phase" width="80">
                <template #default="scope">
                    <dict-tag :options="brain_drug_phase" :value="scope.row.phase" />
                </template>
            </el-table-column>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['healthbrain:drug:edit']">
                        修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['healthbrain:drug:remove']">删除
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

        <!-- 添加或修改创新药对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="drugRef" :model="form" :rules="rules" label-width="120px">
                <el-form-item label="企业名称" prop="enterpriseId">
                    <el-select v-model="form.enterpriseId" placeholder="请选择企业名称">
                        <el-option
                            v-for="dict in enterpriseList"
                            :key="dict.id"
                            :label="dict.name"
                            :value="dict.id"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="研发管线名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入研发管线名称"/>
                </el-form-item>
                <el-form-item label="适应症" prop="indication">
                    <el-input v-model="form.indication" placeholder="请输入适应症"/>
                </el-form-item>
                <el-form-item label="所处阶段" prop="phase">
                    <el-select v-model="form.phase" placeholder="请选择所处阶段">
                        <el-option
                            v-for="dict in brain_drug_phase"
                            :key="dict.value"
                            :label="dict.label"
                            :value="dict.value"
                        ></el-option>
                    </el-select>
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

<script setup name="Drug">

import {listDrug, getDrug, addDrug, delDrug, updateDrug} from "@/api/brain/drug";
import {listEnterpriseAll} from "@/api/brain/enterprise";
import {onMounted} from "vue";

const {proxy} = getCurrentInstance();
const {
    brain_drug_phase,
} = proxy.useDict("brain_drug_phase");

const enterpriseList = ref([]);
const drugList = ref([]);
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
        enterpriseId: undefined,
        name: undefined,
        indication: undefined,
        phase: undefined,
    },
    rules: {
        enterpriseId: [{required: true, message: "企业名称不能为空", trigger: "change"}],
        name: [{required: true, message: "研发管线名称不能为空", trigger: "blur"}],
        indication: [{required: true, message: "适应症不能为空", trigger: "blur"}],
        phase: [{required: true, message: "所处阶段不能为空", trigger: "change"}],
    }
});

const {queryParams, form, rules} = toRefs(data);

/** 查询企业列表 */
function getEnterpriseList() {
    return listEnterpriseAll().then(response => {
        enterpriseList.value = response
    });
}

/** 查询创新药列表 */
function getList() {
    loading.value = true;
    listDrug(queryParams.value).then(response => {
        drugList.value = response.rows.map(it => {
            let enterpriseObj = enterpriseList.value.find(itt => itt.id === it.enterpriseId)
            return {
                ...it,
                enterpriseObj: enterpriseObj,
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
        enterpriseId: undefined,
        name: undefined,
        indication: undefined,
        phase: undefined,
    };
    proxy.resetForm("drugRef");
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
    title.value = "添加创新药";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const drugId = row.id || ids.value;
    getDrug(drugId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改创新药";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["drugRef"].validate(valid => {
        if (valid) {
            if (form.value.id != undefined) {
                updateDrug(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addDrug(form.value).then(response => {
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
    const drugIds = row.id || ids.value;
    const drugNames = row.name || names.value;
    proxy.$modal.confirm('是否确认删除研发管线名称为"' + drugNames + '"的数据项？').then(function () {
        return delDrug(drugIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {
    });
}

onMounted(async () => {
    await Promise.all([getEnterpriseList()])
    await getList();
})

</script>

<style scoped lang="scss">
:deep(.el-select) {
    width: 100%;
}
</style>
