<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
<!--            <el-form-item label="企业名称" prop="enterpriseId">-->
<!--                <el-select v-model="queryParams.enterpriseId" placeholder="企业名称" clearable style="width: 200px">-->
<!--                    <el-option-->
<!--                        v-for="dict in enterpriseList"-->
<!--                        :key="dict.id"-->
<!--                        :label="dict.name"-->
<!--                        :value="dict.id"-->
<!--                    />-->
<!--                </el-select>-->
<!--            </el-form-item>-->
            <el-form-item label="企业名称" prop="enterpriseId">
                <el-input
                    v-model="queryParams.enterpriseId"
                    placeholder="请输入企业名称"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="医疗器械名称" prop="name">
                <el-input
                    v-model="queryParams.name"
                    placeholder="请输入医疗器械名称"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="注册编号" prop="number">
                <el-input
                    v-model="queryParams.number"
                    placeholder="请输入注册编号"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="等级" prop="level">
                <el-select v-model="queryParams.level" placeholder="等级" clearable style="width: 200px">
                    <el-option
                        v-for="dict in brain_instrument_level"
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
                    v-hasPermi="['healthbrain:instrument:add']"
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
                    v-hasPermi="['healthbrain:instrument:edit']"
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
                    v-hasPermi="['healthbrain:instrument:remove']"
                >删除
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="instrumentList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
<!--            <el-table-column label="企业名称" align="center" prop="enterpriseObj.name"/>-->
            <el-table-column label="企业名称" align="center" prop="enterpriseId"/>
            <el-table-column label="医疗器械名称" align="center" prop="name"/>
            <el-table-column label="注册编号" align="center" prop="number"/>
            <el-table-column label="等级" align="center" prop="level">
                <template #default="scope">
                    <dict-tag :options="brain_instrument_level" :value="scope.row.level" />
                </template>
            </el-table-column>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['healthbrain:instrument:edit']">
                        修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['healthbrain:instrument:remove']">删除
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

        <!-- 添加或修改医疗器械对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="instrumentRef" :model="form" :rules="rules" label-width="120px">
<!--                <el-form-item label="企业名称" prop="enterpriseId">-->
<!--                    <el-select v-model="form.enterpriseId" placeholder="请选择企业名称">-->
<!--                        <el-option-->
<!--                            v-for="dict in enterpriseList"-->
<!--                            :key="dict.id"-->
<!--                            :label="dict.name"-->
<!--                            :value="dict.id"-->
<!--                        ></el-option>-->
<!--                    </el-select>-->
<!--                </el-form-item>-->
                <el-form-item label="企业名称" prop="enterpriseId">
                    <el-input v-model="form.enterpriseId" placeholder="请输入企业名称"/>
                </el-form-item>
                <el-form-item label="医疗器械名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入医疗器械名称"/>
                </el-form-item>
                <el-form-item label="注册编号" prop="number">
                    <el-input v-model="form.number" placeholder="请输入注册编号"/>
                </el-form-item>
                <el-form-item label="等级" prop="level">
                    <el-select v-model="form.level" placeholder="请选择等级">
                        <el-option
                            v-for="dict in brain_instrument_level"
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

<script setup name="Instrument">

import {listInstrument, getInstrument, addInstrument, delInstrument, updateInstrument} from "@/api/brain/instrument";
import {listEnterpriseAll} from "@/api/brain/enterprise";
import {onMounted} from "vue";

const {proxy} = getCurrentInstance();
const {
    brain_instrument_level,
} = proxy.useDict("brain_instrument_level");

const enterpriseList = ref([]);
const instrumentList = ref([]);
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
        number: undefined,
        level: undefined,
    },
    rules: {
        enterpriseId: [{required: true, message: "企业名称不能为空", trigger: "change"}],
        name: [{required: true, message: "医疗器械名称不能为空", trigger: "blur"}],
        number: [{required: true, message: "注册编号不能为空", trigger: "blur"}],
        level: [{required: true, message: "等级不能为空", trigger: "change"}],
    }
});

const {queryParams, form, rules} = toRefs(data);

/** 查询企业列表 */
function getEnterpriseList() {
    return listEnterpriseAll().then(response => {
        enterpriseList.value = response
    });
}

/** 查询医疗器械列表 */
function getList() {
    loading.value = true;
    listInstrument(queryParams.value).then(response => {
        instrumentList.value = response.rows.map(it => {
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
        type: undefined,
        name: undefined,
        description: undefined,
    };
    proxy.resetForm("instrumentRef");
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
    title.value = "添加医疗器械";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const instrumentId = row.id || ids.value;
    getInstrument(instrumentId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改医疗器械";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["instrumentRef"].validate(valid => {
        if (valid) {
            if (form.value.id != undefined) {
                updateInstrument(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addInstrument(form.value).then(response => {
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
    const instrumentIds = row.id || ids.value;
    const instrumentNames = row.name || names.value;
    proxy.$modal.confirm('是否确认删除医疗器械名称为"' + instrumentNames + '"的数据项？').then(function () {
        return delInstrument(instrumentIds);
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
