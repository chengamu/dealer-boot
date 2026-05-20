<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="设备名称" prop="name">
                <el-input
                    v-model="queryParams.name"
                    placeholder="请输入设备名称"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="所属企业" prop="enterpriseId">
                <el-select v-model="queryParams.enterpriseId" placeholder="所属企业" clearable filterable style="width: 200px">
                    <el-option
                        v-for="dict in enterpriseList"
                        :key="dict.id"
                        :label="dict.name"
                        :value="dict.id"
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
                    v-hasPermi="['healthbrain:equipment:add']"
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
                    v-hasPermi="['healthbrain:equipment:edit']"
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
                    v-hasPermi="['healthbrain:equipment:remove']"
                >删除
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="equipmentList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="设备名称" align="center" prop="name"/>
            <el-table-column label="品牌" align="center" prop="brand"/>
            <el-table-column label="规格型号" align="center" prop="specification"/>
            <el-table-column label="数量" align="center" prop="number" width="80"/>
            <el-table-column label="服务内容" align="center" prop="service"/>
            <el-table-column label="所属企业" align="center" prop="enterpriseObj.name"/>
            <el-table-column label="使用率(%)" align="center" prop="usageRate" width="100"/>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['healthbrain:equipment:edit']">
                        修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['healthbrain:equipment:remove']">删除
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

        <!-- 添加或修改设备对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="equipmentRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="设备名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入设备名称" />
                </el-form-item>
                <el-form-item label="品牌" prop="brand">
                    <el-input v-model="form.brand" placeholder="请输入设备品牌" />
                </el-form-item>
                <el-form-item label="规格型号" prop="specification">
                    <el-input v-model="form.specification" placeholder="请输入规格型号" />
                </el-form-item>
                <el-form-item label="数量" prop="number">
                    <el-input-number v-model="form.number" placeholder="" :controls="false" :precision="0"  />
                    <span class="ml10">台</span>
                </el-form-item>
                <el-form-item label="服务内容" prop="service">
                    <el-input v-model="form.service" placeholder="请输入服务内容" />
                </el-form-item>
                <el-form-item label="所属企业" prop="enterpriseId">
                    <el-select v-model="form.enterpriseId" placeholder="请选择所属企业" filterable>
                        <el-option
                            v-for="dict in enterpriseList"
                            :key="dict.id"
                            :label="dict.name"
                            :value="dict.id"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="使用率" prop="usageRate">
                    <el-input-number v-model="form.usageRate" placeholder="" :controls="false" :precision="2" :min="0" :max="100" />
                    <span class="ml10">%</span>
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

<script setup name="Equipment">

import {listEquipment, getEquipment, addEquipment, delEquipment, updateEquipment, changeStatusEquipment} from "@/api/brain/equipment";
import {onMounted} from "vue";
import {listEnterpriseAll} from "@/api/brain/enterprise";

const {proxy} = getCurrentInstance();
const {
    sys_show_hide,
} = proxy.useDict("sys_show_hide");

const enterpriseList = ref([]);
const equipmentList = ref([]);
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
        name: [{required: true, message: "设备名称不能为空", trigger: "blur"}],
        brand: [{required: true, message: "品牌不规格型号能为空", trigger: "blur"}],
        specification: [{required: true, message: "规格型号不能为空", trigger: "blur"}],
        number: [{required: true, message: "数量不能为空", trigger: "blur"}],
        service: [{required: true, message: "服务内容不能为空", trigger: "blur"}],
        enterpriseId: [{required: true, message: "所属企业不能为空", trigger: "change"}],
    }
});

const {queryParams, form, rules} = toRefs(data);

/** 查询企业列表 */
function getEnterpriseList() {
    return listEnterpriseAll({ type: 'yjy' }).then(response => {
        enterpriseList.value = response
    });
}

/** 查询设备列表 */
function getList() {
    loading.value = true;
    listEquipment(queryParams.value).then(response => {
        equipmentList.value = response.rows.map(it => {
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
        name: undefined,
        brand: undefined,
        specification: undefined,
        number: undefined,
        service: undefined,
        enterpriseId: undefined,
    };
    proxy.resetForm("equipmentRef");
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
    title.value = "添加设备";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const equipmentId = row.id || ids.value;
    getEquipment(equipmentId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改设备";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["equipmentRef"].validate(valid => {
        if (valid) {
            if (form.value.id != undefined) {
                updateEquipment(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addEquipment(form.value).then(response => {
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
    const equipmentIds = row.id || ids.value;
    const equipmentNames = row.name || names.value;
    proxy.$modal.confirm('是否确认' + (row.visible === '1' ? "显示" : "隐藏") + '设备名称为"' + equipmentNames + '"的数据项？').then(function () {
        return changeStatusEquipment(equipmentIds, row.visible);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess((row.visible === '1' ? "显示" : "隐藏") + "成功");
    }).catch(() => {
        row.visible = row.visible === "0" ? "1" : "0";
    });
}

/** 删除按钮操作 */
function handleDelete(row) {
    const equipmentIds = row.id || ids.value;
    const equipmentNames = row.name || names.value;
    proxy.$modal.confirm('是否确认删除设备名称为"' + equipmentNames + '"的数据项？').then(function () {
        return delEquipment(equipmentIds);
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
