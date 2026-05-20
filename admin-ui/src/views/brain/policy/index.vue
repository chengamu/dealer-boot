<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="类别" prop="type">
                <el-select v-model="queryParams.type" placeholder="类别" clearable style="width: 200px">
                    <el-option
                        v-for="dict in brain_policy_type"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="政策名称" prop="name">
                <el-input
                    v-model="queryParams.name"
                    placeholder="请输入政策名称"
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
                    v-hasPermi="['healthbrain:policy:add']"
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
                    v-hasPermi="['healthbrain:policy:edit']"
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
                    v-hasPermi="['healthbrain:policy:remove']"
                >删除
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="policyList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="类别" align="center" prop="typeObj.label"/>
            <el-table-column label="政策名称" align="center" prop="name"/>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['healthbrain:policy:edit']">
                        修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['healthbrain:policy:remove']">删除
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

        <!-- 添加或修改政策对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="policyRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="类别" prop="type">
                    <el-select v-model="form.type" placeholder="请选择">
                        <el-option
                            v-for="dict in brain_policy_type"
                            :key="dict.value"
                            :label="dict.label"
                            :value="dict.value"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="政策名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入政策名称"/>
                </el-form-item>
                <el-form-item label="政策内容" prop="content">
                    <el-input v-model="form.content" type="textarea" placeholder="请输入政策内容" :rows="4"/>
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

<script setup name="Policy">

import {listPolicy, getPolicy, addPolicy, delPolicy, updatePolicy} from "@/api/brain/policy";
import {onMounted} from "vue";

const {proxy} = getCurrentInstance();
const {
    brain_policy_type,
} = proxy.useDict("brain_policy_type");

const policyList = ref([]);
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
        type: undefined,
        name: undefined,
    },
    rules: {
        type: [{required: true, message: "类别不能为空", trigger: "change"}],
        name: [{required: true, message: "政策名称不能为空", trigger: "blur"}],
        content: [{required: true, message: "政策内容不能为空", trigger: "blur"}],
    }
});

const {queryParams, form, rules} = toRefs(data);

/** 查询政策列表 */
function getList() {
    loading.value = true;
    listPolicy(queryParams.value).then(response => {
        policyList.value = response.rows.map(it => {
            let typeObj = brain_policy_type.value.find(itt => itt.value === it.type)
            return {
                ...it,
                typeObj: typeObj,
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
        type: undefined,
        name: undefined,
        content: undefined,
    };
    proxy.resetForm("policyRef");
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
    title.value = "添加政策";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const policyId = row.id || ids.value;
    getPolicy(policyId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改政策";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["policyRef"].validate(valid => {
        if (valid) {
            if (form.value.id != undefined) {
                updatePolicy(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addPolicy(form.value).then(response => {
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
    const policyIds = row.id || ids.value;
    const policyNames = row.name || names.value;
    proxy.$modal.confirm('是否确认删除政策名称为"' + policyNames + '"的数据项？').then(function () {
        return delPolicy(policyIds);
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
