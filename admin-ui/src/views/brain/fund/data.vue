<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="年份" prop="year">
                <el-date-picker
                    v-model="queryParams.year"
                    type="year"
                    placeholder="请选择年份"
                    value-format="YYYY"
                    style="width: 200px"
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
                    v-hasPermi="['healthbrain:fundData:add']"
                >新增</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="success"
                    plain
                    icon="Edit"
                    :disabled="single"
                    @click="handleUpdate"
                    v-hasPermi="['healthbrain:fundData:edit']"
                >修改</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="danger"
                    plain
                    icon="Delete"
                    :disabled="multiple"
                    @click="handleDelete"
                    v-hasPermi="['healthbrain:fundData:remove']"
                >删除</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="warning"
                    plain
                    icon="Close"
                    @click="handleClose"
                >关闭</el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="年份" align="center" prop="year" />
            <el-table-column label="数据" align="center" prop="value" />
            <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['healthbrain:fundData:edit']">修改</el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['healthbrain:fundData:remove']">删除</el-button>
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
            <el-form ref="dataRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="年份" prop="year">
                    <el-date-picker
                        v-model="form.year"
                        type="year"
                        placeholder="请选择年份"
                        value-format="YYYY"
                    />
                </el-form-item>
                <el-form-item label="数据" prop="value">
                    <el-input-number v-model="form.value" controls-position="right" :min="0" />
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

<script setup name="FundData">
import {addFundData, delFundData, getFundData, listFundData, updateFundData} from "@/api/brain/fund";
import {onMounted} from "vue";

const { proxy } = getCurrentInstance();

const dataList = ref([
    // { id: 1, fundTypeId: 1, year: '2024', value: 18.6, remark: '无' },
]);
const open = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref([]);
const years = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const route = useRoute();

const data = reactive({
    form: {},
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        year: undefined,
    },
    rules: {
        year: [{ required: true, message: "年份不能为空", trigger: "blur" }],
        value: [{ required: true, message: "数据不能为空", trigger: "blur" }],
    }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询字典数据列表 */
function getList() {
    loading.value = true;
    console.log(queryParams.value)
    listFundData({...queryParams.value, fundTypeId: route.params.fundTypeId}).then(response => {
        dataList.value = response.rows;
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
        fundTypeId: undefined,
        id: undefined,
        year: undefined,
        value: undefined,
        remark: undefined
    };
    proxy.resetForm("dataRef");
}
/** 搜索按钮操作 */
function handleQuery() {
    queryParams.value.pageNum = 1;
    getList();
}
/** 返回按钮操作 */
function handleClose() {
    const obj = { path: "/brain/fund" };
    proxy.$tab.closeOpenPage(obj);
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
    title.value = "添加资金数据";
    form.value.fundTypeId = route.params.fundTypeId;
}
/** 多选框选中数据 */
function handleSelectionChange(selection) {
    ids.value = selection.map(item => item.fundTypeId);
    years.value = selection.map(item => item.year);
    single.value = selection.length !== 1;
    multiple.value = !selection.length;
}
/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const fundDataId = row.id || ids.value;
    getFundData(fundDataId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改资金数据";
    });
}
/** 提交按钮 */
function submitForm() {
    proxy.$refs["dataRef"].validate(valid => {
        if (valid) {
            if (form.value.id != undefined) {
                updateFundData(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addFundData(form.value).then(response => {
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
    const fundDataIds = row.id || ids.value;
    const years = row.year || years.value;
    proxy.$modal.confirm('是否确认删除年份为"' + years + '"的数据项？').then(function() {
        return delFundData(fundDataIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {});
}

onMounted(() => {
    getList();
})
</script>
