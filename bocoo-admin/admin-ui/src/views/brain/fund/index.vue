<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="102px">
            <el-form-item label="数据类型编号" prop="code">
                <el-input
                    v-model="queryParams.code"
                    placeholder="请输入数据类型编号"
                    clearable
                    style="width: 240px"
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="数据类型名称" prop="name">
                <el-input
                    v-model="queryParams.name"
                    placeholder="请输入数据类型名称"
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
                    v-hasPermi="['healthbrain:fundType:add']"
                >新增</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="success"
                    plain
                    icon="Edit"
                    :disabled="single"
                    @click="handleUpdate"
                    v-hasPermi="['healthbrain:fundType:edit']"
                >修改</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="danger"
                    plain
                    icon="Delete"
                    :disabled="multiple"
                    @click="handleDelete"
                    v-hasPermi="['healthbrain:fundType:remove']"
                >删除</el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="fundTypeList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="数据类型编号" align="center" prop="code" />
            <el-table-column label="数据类型名称" align="center" :show-overflow-tooltip="true">
                <template #default="scope">
                    <router-link :to="'/brain/fundType-data/index/' + scope.row.id" class="link-type">
                        <span>{{ scope.row.name }}</span>
                    </router-link>
                </template>
            </el-table-column>
            <el-table-column label="单位" align="center" prop="unit" />
            <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['healthbrain:fundType:edit']">修改</el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['healthbrain:fundType:remove']">删除</el-button>
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

        <!-- 添加或修改参数配置对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="fundTypeRef" :model="form" :rules="rules" label-width="120px">
                <el-form-item label="数据类型编号" prop="code">
                    <el-input v-model="form.code" placeholder="请输入数据类型编号" />
                </el-form-item>
                <el-form-item label="数据类型名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入数据类型名称" />
                </el-form-item>
                <el-form-item label="单位" prop="unit">
                    <el-input v-model="form.unit" placeholder="请输入单位" />
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
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

<script setup name="FundType">
import {addFundType, delFundType, getFundType, listFundType, updateFundType} from "@/api/brain/fund";
import {onMounted} from "vue";

const {proxy} = getCurrentInstance();

const fundTypeList = ref([
    // { id: 1, code: 'cygm', name: '产业规模', unit: '亿元', remark: '无' },
    // { id: 2, code: 'jhztz', name: '计划总投资', unit: '亿元', remark: '无' },
    // { id: 3, code: 'yftr', name: '研发投入（R&D）', unit: '亿元', remark: '无' },
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
        pageNum: 1,
        pageSize: 10,
        code: undefined,
        name: undefined,
    },
    rules: {
        code: [{ required: true, message: "数据类型编号不能为空", trigger: "blur" }],
        name: [{ required: true, message: "数据类型名称不能为空", trigger: "blur" }],
        unit: [{ required: true, message: "单位不能为空", trigger: "blur" }]
    },
});

const { queryParams, form, rules } = toRefs(data);

/** 查询数据类型列表 */
function getList() {
    loading.value = true;
    listFundType(queryParams.value).then(response => {
        fundTypeList.value = response.rows;
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
        code: undefined,
        name: undefined,
        unit: undefined,
        remark: undefined
    };
    proxy.resetForm("fundTypeRef");
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
/** 新增按钮操作 */
function handleAdd() {
    reset();
    open.value = true;
    title.value = "添加数据类型";
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
    const fundTypeId = row.id || ids.value;
    getFundType(fundTypeId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改数据类型";
    });
}
/** 提交按钮 */
function submitForm() {
    proxy.$refs["fundTypeRef"].validate(valid => {
        if (valid) {
            if (form.value.id != undefined) {
                updateFundType(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addFundType(form.value).then(response => {
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
    const fundTypeIds = row.id || ids.value;
    const fundTypeNames = row.name || names.value;
    proxy.$modal.confirm('是否确认删除数据类型编号为"' + fundTypeNames + '"的数据项？').then(function() {
        return delFundType(fundTypeIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {});
}

onMounted(() => {
    getList();
})

</script>
