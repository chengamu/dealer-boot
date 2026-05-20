<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="类别" prop="type">
                <el-select v-model="queryParams.type" placeholder="类别" clearable style="width: 200px">
                    <el-option
                        v-for="dict in brain_enterprise_type"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="单位名称" prop="name">
                <el-input
                    v-model="queryParams.name"
                    placeholder="请输入单位名称"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <!--            <el-form-item label="发展方向" prop="development">-->
            <!--                <el-select v-model="queryParams.development" placeholder="发展方向" clearable style="width: 200px">-->
            <!--                    <el-option-->
            <!--                        v-for="dict in brain_enterprise_development"-->
            <!--                        :key="dict.value"-->
            <!--                        :label="dict.label"-->
            <!--                        :value="dict.value"-->
            <!--                    />-->
            <!--                </el-select>-->
            <!--            </el-form-item>-->
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
                    v-hasPermi="['healthbrain:enterprise:add']"
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
                    v-hasPermi="['healthbrain:enterprise:edit']"
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
                    v-hasPermi="['healthbrain:enterprise:remove']"
                >删除
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="enterpriseList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="类别" align="center" prop="typeObj.label" width="160"/>
            <el-table-column label="单位名称" align="center" prop="name" show-overflow-tooltip/>
            <el-table-column label="发展方向" align="center" prop="developmentLabels"/>
            <el-table-column label="单位地址" align="center" prop="address" show-overflow-tooltip/>
            <el-table-column label="联系人" align="center" prop="contact" width="70"/>
            <el-table-column label="联系方式" align="center" prop="contactInfo" width="130"/>
            <el-table-column label="对接人" align="center" prop="dock" width="70"/>
            <el-table-column label="是否重点" align="center" prop="important" width="80">
                <template #default="scope">
                    <el-switch
                        v-model="scope.row.important"
                        active-value="1"
                        inactive-value="0"
                        @change="handleUpdateStatus(scope.row)"
                    ></el-switch>
                </template>
            </el-table-column>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                               v-hasPermi="['healthbrain:enterprise:edit']">
                        修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['healthbrain:enterprise:remove']">删除
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

        <!-- 添加或修改企业对话框 -->
        <el-dialog :title="title" v-model="open" width="1000px" append-to-body>
            <el-form ref="enterpriseRef" :model="form" :rules="rules" label-width="110px">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="类别" prop="type">
                            <el-select v-model="form.type" placeholder="请选择">
                                <el-option
                                    v-for="dict in brain_enterprise_type"
                                    :key="dict.value"
                                    :label="dict.label"
                                    :value="dict.value"
                                ></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="单位名称" prop="name">
                            <el-input v-model="form.name" placeholder="请输入单位名称"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="发展方向" prop="development">
                            <el-select v-model="form.development" placeholder="请选择" multiple>
                                <el-option
                                    v-for="dict in brain_enterprise_development"
                                    :key="dict.value"
                                    :label="dict.label"
                                    :value="dict.value"
                                ></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="单位地址" prop="address">
                            <el-input v-model="form.address" placeholder="请输入单位地址"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="主营" prop="mainBusiness">
                            <el-input v-model="form.mainBusiness" type="textarea" placeholder="请输入主营" :rows="4"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="单位描述" prop="description">
                            <el-input v-model="form.description" type="textarea" placeholder="请输入单位描述"
                                      :rows="4"/>
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
                        <el-form-item label="对接人" prop="dock">
                            <el-input v-model="form.dock" placeholder="请输入对接人"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="是否重点" prop="important">
                            <el-radio-group v-model="form.important">
                                <el-radio
                                    v-for="dict in brain_enterprise_important"
                                    :key="dict.value"
                                    :label="dict.value"
                                >{{ dict.label }}
                                </el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="logo" prop="logo">
                            <ProductImageUpload v-model="form.logo"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="是否银发经济" prop="silverEconomy">
                            <el-radio-group v-model="form.silverEconomy">
                                <el-radio
                                    v-for="dict in brain_enterprise_silver_economy"
                                    :key="dict.value"
                                    :label="dict.value"
                                >{{ dict.label }}
                                </el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="企业地点" prop="location">
                            <el-select v-model="form.location" placeholder="请选择">
                                <el-option
                                    v-for="dict in brain_enterprise_location"
                                    :key="dict.value"
                                    :label="dict.label"
                                    :value="dict.value"
                                ></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="是否生命健康类" prop="lifeHealth">
                            <el-radio-group v-model="form.lifeHealth">
                                <el-radio
                                    v-for="dict in brain_enterprise_life_health"
                                    :key="dict.value"
                                    :label="dict.value"
                                >{{ dict.label }}
                                </el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="企业类型" prop="enterpriseCategory">
                            <el-select v-model="form.enterpriseCategory" placeholder="请选择">
                                <el-option
                                    v-for="dict in brain_enterprise_category"
                                    :key="dict.value"
                                    :label="dict.label"
                                    :value="dict.value"
                                ></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="项目进展" prop="projectProgress">
                            <el-select v-model="form.projectProgress" placeholder="请选择">
                                <el-option
                                    v-for="dict in brain_enterprise_progress"
                                    :key="dict.value"
                                    :label="dict.label"
                                    :value="dict.value"
                                ></el-option>
                            </el-select>
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

<script setup name="Enterprise">

import {
    listEnterprise,
    getEnterprise,
    addEnterprise,
    delEnterprise,
    updateEnterprise,
    changeStatusEnterprise
} from "@/api/brain/enterprise";
import {onMounted} from "vue";
import ProductImageUpload from "@/views/iiot/product/components/ProductImageUpload.vue";

const {proxy} = getCurrentInstance();
const {
    brain_enterprise_type,
    brain_enterprise_development,
    brain_enterprise_important,
    brain_enterprise_silver_economy,
    brain_enterprise_location,
    brain_enterprise_life_health,
    brain_enterprise_category,
    brain_enterprise_progress,
} = proxy.useDict(
    "brain_enterprise_type",
    "brain_enterprise_development",
    "brain_enterprise_important",
    "brain_enterprise_silver_economy",
    "brain_enterprise_location",
    "brain_enterprise_life_health",
    "brain_enterprise_category",
    "brain_enterprise_progress",
);

const enterpriseList = ref([]);
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
        development: undefined,
    },
    rules: {
        // type: [{required: true, message: "类别不能为空", trigger: "change"}],
        name: [{required: true, message: "单位名称不能为空", trigger: "blur"}],
        // logo: [{required: true, message: "logo不能为空", trigger: "change"}],
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
function getList() {
    loading.value = true;
    listEnterprise(queryParams.value).then(response => {
        enterpriseList.value = response.rows.map(it => {
            let typeObj = brain_enterprise_type.value.find(itt => itt.value === it.type)
            let developmentValueArr = it.development.split(',')
            let developmentArr = brain_enterprise_development.value.filter(itt => developmentValueArr.includes(itt.value))
            let developmentLabels = developmentArr.map(itt => itt.label).join(',')
            return {
                ...it,
                typeObj: typeObj,
                developmentArr: developmentArr,
                developmentLabels: developmentLabels,
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
        typeName: undefined,
        name: undefined,
        logo: undefined,
        development: [],
        developmentName: undefined,
        address: undefined,
        mainBusiness: undefined,
        description: undefined,
        contact: undefined,
        contactInfo: undefined,
        dock: undefined,
        important: '0',
        silverEconomy: '0',
        location: undefined,
        lifeHealth: '0',
        enterpriseCategory: undefined,
        projectProgress: undefined,
    };
    proxy.resetForm("enterpriseRef");
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
    title.value = "添加企业";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const enterpriseId = row.id || ids.value;
    getEnterprise(enterpriseId).then(response => {
        form.value = response.data;
        form.value.development = response.data.development.split(',');
        open.value = true;
        title.value = "修改企业";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["enterpriseRef"].validate(valid => {
        if (valid) {
            let development = form.value.development.join(',');
            let developmentArr = brain_enterprise_development.value.filter(itt => form.value.development.includes(itt.value))
            let developmentName = developmentArr.map(itt => itt.label).join(',')
            if (form.value.id != undefined) {
                updateEnterprise({...form.value, development: development, developmentName: developmentName}).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addEnterprise({...form.value, development: development, developmentName: developmentName}).then(response => {
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
    const eventIds = row.id || ids.value;
    const eventNames = row.name || names.value;
    proxy.$modal.confirm('是否' + (row.important === '1' ? "" : "取消") + '标记企业名称为"' + eventNames + '"的数据项为重点企业？').then(function () {
        return changeStatusEnterprise(eventIds, row.important);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess((row.important === '1' ? "" : "取消") + "标记成功");
    }).catch(() => {
        row.important = row.important === "0" ? "1" : "0";
    });
}

/** 删除按钮操作 */
function handleDelete(row) {
    const enterpriseIds = row.id || ids.value;
    const enterpriseNames = row.name || names.value;
    proxy.$modal.confirm('是否确认删除企业名称为"' + enterpriseNames + '"的数据项？').then(function () {
        return delEnterprise(enterpriseIds);
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
