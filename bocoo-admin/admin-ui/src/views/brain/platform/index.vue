<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="类别" prop="type">
                <el-select v-model="queryParams.type" placeholder="类别" clearable style="width: 200px">
                    <el-option
                        v-for="dict in brain_platform_type"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="平台名称" prop="name">
                <el-input
                    v-model="queryParams.name"
                    placeholder="请输入平台名称"
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
                    v-hasPermi="['healthbrain:platform:add']"
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
                    v-hasPermi="['healthbrain:platform:edit']"
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
                    v-hasPermi="['healthbrain:platform:remove']"
                >删除
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="platformList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="类别" align="center" prop="type">
                <template #default="scope">
                    <dict-tag :options="brain_platform_type" :value="scope.row.type" />
                </template>
            </el-table-column>
            <el-table-column label="平台名称" align="center" prop="name"/>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['healthbrain:platform:edit']">
                        修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['healthbrain:platform:remove']">删除
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

        <!-- 添加或修改平台对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="platformRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="类别" prop="type">
                    <el-select v-model="form.type" placeholder="请选择">
                        <el-option
                            v-for="dict in brain_platform_type"
                            :key="dict.value"
                            :label="dict.label"
                            :value="dict.value"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="平台名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入平台名称"/>
                </el-form-item>
                <el-form-item label="平台描述" prop="description">
                    <el-input v-model="form.description" type="textarea" placeholder="请输入平台描述" :rows="4"/>
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

<script setup name="Platform">

import {listPlatform, getPlatform, addPlatform, delPlatform, updatePlatform} from "@/api/brain/platform";
import {onMounted} from "vue";

const {proxy} = getCurrentInstance();
const {
    brain_platform_type,
} = proxy.useDict("brain_platform_type");

const platformList = ref([]);
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
        name: [{required: true, message: "平台名称不能为空", trigger: "blur"}],
        description: [{required: true, message: "平台描述不能为空", trigger: "blur"}],
    }
});

const {queryParams, form, rules} = toRefs(data);

/** 查询平台列表 */
function getList() {
    loading.value = true;
    return listPlatform(queryParams.value).then(response => {
        platformList.value = response.rows
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
        description: undefined,
    };
    proxy.resetForm("platformRef");
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
    title.value = "添加平台";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const platformId = row.id || ids.value;
    getPlatform(platformId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改平台";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["platformRef"].validate(valid => {
        if (valid) {
            if (form.value.id != undefined) {
                updatePlatform(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addPlatform(form.value).then(response => {
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
    const platformIds = row.id || ids.value;
    const platformNames = row.name || names.value;
    proxy.$modal.confirm('是否确认删除平台名称为"' + platformNames + '"的数据项？').then(function () {
        return delPlatform(platformIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {
    });
}

onMounted(async () => {
    await getList();
})

</script>

<style scoped lang="scss">
:deep(.el-select) {
    width: 100%;
}
</style>
