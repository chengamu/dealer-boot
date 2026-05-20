<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="标题" prop="title">
                <el-input
                    v-model="queryParams.title"
                    placeholder="请输入标题"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="标签" prop="label">
                <el-select v-model="queryParams.label" placeholder="标签" clearable style="width: 200px">
                    <el-option
                        v-for="dict in brain_technology_label"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="发布有效期" prop="deadline">
                <el-select v-model="queryParams.deadline" placeholder="发布有效期" clearable style="width: 200px">
                    <el-option
                        v-for="dict in brain_technology_deadline"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                    />
                </el-select>
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
                    v-hasPermi="['healthbrain:technology:add']"
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
                    v-hasPermi="['healthbrain:technology:edit']"
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
                    v-hasPermi="['healthbrain:technology:remove']"
                >删除
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="technologyList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="标题" align="center" prop="title" show-overflow-tooltip/>
            <el-table-column label="标签" align="center" prop="label">
                <template #default="scope">
                    <dict-tag :options="brain_technology_label" :value="scope.row.label" />
                </template>
            </el-table-column>
            <el-table-column label="发布有效期" align="center" prop="deadline">
                <template #default="scope">
                    <dict-tag :options="brain_technology_deadline" :value="scope.row.deadline" />
                </template>
            </el-table-column>
            <el-table-column label="单位名称" align="center" prop="enterprise.name"/>
            <el-table-column label="联系人" align="center" prop="contact" width="70"/>
            <el-table-column label="联系方式" align="center" prop="contactInfo" width="130"/>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                               v-hasPermi="['healthbrain:technology:edit']">
                        修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['healthbrain:technology:remove']">删除
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

        <!-- 添加或修改技术对话框 -->
        <el-dialog :title="title" v-model="open" width="800px" append-to-body>
            <el-form ref="technologyRef" :model="form" :rules="rules" label-width="100px">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="标题" prop="title">
                            <el-input v-model="form.title" placeholder="请输入标题"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="标签" prop="label">
                            <el-select v-model="form.label" placeholder="请选择">
                                <el-option
                                    v-for="dict in brain_technology_label"
                                    :key="dict.value"
                                    :label="dict.label"
                                    :value="dict.value"
                                ></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="自定义标签" prop="customLabel">
                            <el-input v-model="form.customLabel" placeholder="请输入自定义标签"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="详细描述" prop="description">
                            <el-input v-model="form.description" type="textarea" placeholder="请输入详细描述"
                                      :rows="6"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="单位" prop="enterpriseId">
                            <el-select v-model="form.enterpriseId" placeholder="请选择单位">
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
                        <el-form-item label="发布有效期" prop="deadline">
                            <el-select v-model="form.deadline" placeholder="请选择">
                                <el-option
                                    v-for="dict in brain_technology_deadline"
                                    :key="dict.value"
                                    :label="dict.label"
                                    :value="dict.value"
                                ></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="联系人" prop="contact">
                            <el-input v-model="form.contact" placeholder="请输入联系人"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="联系方式" prop="contactInfo">
                            <el-input v-model="form.contactInfo" placeholder="请输入联系方式"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="图片" prop="img">
                            <ProductImageUpload v-model="form.img"/>
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

<script setup name="Technology">

import {
    listTechnology,
    getTechnology,
    addTechnology,
    delTechnology,
    updateTechnology,
    changeStatusTechnology
} from "@/api/brain/technology";
import {onMounted} from "vue";
import ProductImageUpload from "@/views/iiot/product/components/ProductImageUpload.vue";
import {listEnterpriseAll} from "@/api/brain/enterprise";

const {proxy} = getCurrentInstance();
const {
    brain_technology_label,
    brain_technology_deadline,
} = proxy.useDict("brain_technology_label", "brain_technology_deadline");

const enterpriseList = ref([])
const technologyList = ref([]);
const open = ref(false);
const loading = ref(false);
const showSearch = ref(true);
const ids = ref([]);
const titles = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
    form: {},
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        label: undefined,
        deadline: undefined,
        enterpriseId: undefined,
    },
    rules: {
        title: [{required: true, message: "标题不能为空", trigger: "blur"}],
        label: [{required: true, message: "标签不能为空", trigger: "change"}],
        deadline: [{required: true, message: "发布有效期不能为空", trigger: "change"}],
        enterpriseId: [{required: true, message: "单位不能为空", trigger: "change"}],
        contactInfo: [
            {
                validator: (rule, value, callback) => {
                    if (value) {
                        // 验证手机号或座机号格式
                        const mobileReg = /^1[3-9]\d{9}$/;
                        const phoneReg = /^0\d{2,3}-?\d{7,8}$/;
                        if (!mobileReg.test(value) && !phoneReg.test(value)) {
                            callback(new Error('请输入正确的手机号或座机号格式'));
                        } else {
                            callback();
                        }
                    } else {
                        callback();
                    }
                },
                trigger: 'blur'
            }
        ]
    }
});

const {queryParams, form, rules} = toRefs(data);

/** 查询企业列表 */
function getEnterpriseList() {
    return listEnterpriseAll().then(response => {
        enterpriseList.value = response
    });
}

/** 查询技术列表 */
function getList() {
    loading.value = true;
    listTechnology(queryParams.value).then(response => {
        technologyList.value = response.rows.map(it => {
            return {
                ...it,
                enterprise: enterpriseList.value.find(itt => itt.id === it.enterpriseId)
            }
        })
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
        title: undefined,
        label: undefined,
        customLabel: undefined,
        deadline: undefined,
        description: undefined,
        enterpriseId: undefined,
        contact: undefined,
        contactInfo: undefined,
        img: undefined,
    };
    proxy.resetForm("technologyRef");
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
    titles.value = selection.map(item => item.title);
    single.value = selection.length !== 1;
    multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
    reset();
    open.value = true;
    title.value = "添加技术";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const technologyId = row.id || ids.value;
    getTechnology(technologyId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改技术";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["technologyRef"].validate(valid => {
        if (valid) {
            if (form.value.id != undefined) {
                updateTechnology({...form.value}).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addTechnology({...form.value}).then(response => {
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
    const technologyIds = row.id || ids.value;
    const technologyNames = row.title || titles.value;
    proxy.$modal.confirm('是否确认删除标题为"' + technologyNames + '"的数据项？').then(function () {
        return delTechnology(technologyIds);
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
