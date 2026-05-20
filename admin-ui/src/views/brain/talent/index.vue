<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="人才类别" prop="type">
                <el-select v-model="queryParams.type" placeholder="人才类别" clearable style="width: 200px">
                    <el-option
                        v-for="dict in brain_talent_type"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="人才姓名" prop="name">
                <el-input
                    v-model="queryParams.name"
                    placeholder="请输入人才姓名"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="工作单位(联盟内)" prop="enterpriseId">
                <el-select v-model="queryParams.enterpriseId" placeholder="工作单位" clearable filterable style="width: 200px">
                    <el-option
                        v-for="dict in enterpriseList"
                        :key="dict.id"
                        :label="dict.name"
                        :value="dict.id"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="工作单位(联盟外)" prop="enterprise">
                <el-input
                    v-model="queryParams.enterprise"
                    placeholder="请输入单位名称"
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
                    v-hasPermi="['healthbrain:talent:add']"
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
                    v-hasPermi="['healthbrain:talent:edit']"
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
                    v-hasPermi="['healthbrain:talent:remove']"
                >删除
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="talentList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="人才类别" align="center" prop="type">
                <template #default="scope">
                    <dict-tag :options="brain_talent_type" :value="scope.row.type" />
                </template>
            </el-table-column>
            <el-table-column label="人才姓名" align="center" prop="name"/>
            <el-table-column label="擅长领域" align="center" prop="development" show-overflow-tooltip/>
            <el-table-column label="工作单位(联盟内)" align="center" prop="enterpriseObj.name" show-overflow-tooltip/>
            <el-table-column label="工作单位(联盟外)" align="center" prop="enterprise" show-overflow-tooltip/>
            <el-table-column label="职务" align="center" prop="position" show-overflow-tooltip/>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                               v-hasPermi="['healthbrain:talent:edit']">
                        修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['healthbrain:talent:remove']">删除
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

        <!-- 添加或修改人才对话框 -->
        <el-dialog :title="title" v-model="open" width="1000px" append-to-body>
            <el-form ref="talentRef" :model="form" :rules="rules" label-width="120px">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="人才类别" prop="type">
                            <el-select v-model="form.type" placeholder="请选择">
                                <el-option
                                    v-for="dict in brain_talent_type"
                                    :key="dict.value"
                                    :label="dict.label"
                                    :value="dict.value"
                                ></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="人才姓名" prop="name">
                            <el-input v-model="form.name" placeholder="请输入人才姓名"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="工作单位(联盟内)" prop="enterpriseId">
                            <el-select v-model="form.enterpriseId" placeholder="请选择工作单位(联盟内)" filterable>
                                <el-option
                                    v-for="dict in enterpriseList"
                                    :key="dict.id"
                                    :label="dict.name"
                                    :value="dict.id"
                                ></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="工作单位(联盟外)" prop="enterprise">
                            <el-input v-model="form.enterprise" placeholder="请输入工作单位(联盟外)"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="职务" prop="position">
                            <el-input v-model="form.position" placeholder="请输入职务"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="擅长领域" prop="development">
                            <el-input v-model="form.development" placeholder="请输入擅长领域"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="个人荣誉" prop="honor">
                            <el-input v-model="form.honor" type="textarea" placeholder="请输入个人荣誉" :rows="4"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="工作/科研经历/重要成果" prop="experience">
                            <el-input v-model="form.experience" type="textarea" placeholder="请输入工作/科研经历/重要成果"
                                      :rows="4"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="备注" prop="remark">
                            <el-input v-model="form.remark" type="textarea" placeholder="请输入备注"
                                      :rows="4"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="头像" prop="avatar">
                            <ProductImageUpload v-model="form.avatar"/>
                        </el-form-item>
                    </el-col>
                </el-row>
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

<script setup name="Talent">

import {
    listTalent,
    getTalent,
    addTalent,
    delTalent,
    updateTalent
} from "@/api/brain/talent";
import {onMounted} from "vue";
import ProductImageUpload from "@/views/iiot/product/components/ProductImageUpload.vue";
import {listEnterpriseAll} from "@/api/brain/enterprise";

const {proxy} = getCurrentInstance();
const {
    brain_talent_type,
} = proxy.useDict("brain_talent_type");

const enterpriseList = ref([]);
const talentList = ref([]);
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
        enterpriseId: undefined,
        enterprise: undefined,
    },
    rules: {
        type: [{required: true, message: "人才类别不能为空", trigger: "change"}],
        name: [{required: true, message: "人才姓名不能为空", trigger: "blur"}],
        development: [{required: true, message: "擅长领域不能为空", trigger: "blur"}],
    }
});

const {queryParams, form, rules} = toRefs(data);

/** 查询企业列表 */
function getEnterpriseList() {
    return listEnterpriseAll().then(response => {
        enterpriseList.value = response
    });
}

/** 查询人才列表 */
function getList() {
    loading.value = true;
    listTalent(queryParams.value).then(response => {
        talentList.value = response.rows.map(it => {
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
        development: undefined,
        enterpriseId: undefined,
        enterprise: undefined,
        position: undefined,
        honor: undefined,
        experience: undefined,
        remark: undefined,
        avatar: undefined,
    };
    proxy.resetForm("talentRef");
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
    title.value = "添加人才";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const talentId = row.id || ids.value;
    getTalent(talentId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改人才";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["talentRef"].validate(valid => {
        if (valid) {
            if (form.value.id != undefined) {
                updateTalent({...form.value}).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addTalent({...form.value}).then(response => {
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
    const talentIds = row.id || ids.value;
    const talentNames = row.name || names.value;
    proxy.$modal.confirm('是否确认删除人才名称为"' + talentNames + '"的数据项？').then(function () {
        return delTalent(talentIds);
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
