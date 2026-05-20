<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="	功能名称" prop="modelName">
                <el-input
                    v-model="queryParams.modelName"
                    placeholder="请输入产品名称"
                    clearable
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="模型类型" prop="modelType">
                <el-select v-model="queryParams.modelType" placeholder="模型类型" clearable style="width: 200px">
                    <el-option
                        v-for="dict in iiot_model_definition_type"
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
                    v-hasPermi="['iiot:modelDefinition:add']"
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
                    v-hasPermi="['iiot:modelDefinition:edit']"
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
                    v-hasPermi="['iiot:modelDefinition:remove']"
                >删除
                </el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                    type="primary"
                    plain
                    icon="Edit"
                    @click="handleRefresh"
                    v-hasPermi="['iiot:modelDefinition:refresh']"
                    :disabled="!props.code"
                >刷新缓存
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="modelList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="产品代码" align="center" prop="code"/>
            <el-table-column label="唯一标识符" align="center" prop="identifier"/>
            <el-table-column label="功能名称" align="center" prop="modelName"/>
            <el-table-column label="模型类型" align="center" prop="modelType">
                <template #default="scope">
                    <dict-tag :options="iiot_model_definition_type" :value="scope.row.modelType"/>
                </template>
            </el-table-column>
            <el-table-column label="指令Topic后缀" align="center" prop="topicSuffix"/>
            <!--      <el-table-column label="指令数据规格" align="center" prop="dataSpec" />-->
            <el-table-column label="应答Topic后缀" align="center" prop="responseTopicSuffix"/>
            <!--      <el-table-column label="应答数据规格" align="center" prop="responseDataSpec" />-->
            <el-table-column label="上行内容判别键" align="center" prop="discriminatorKey"/>
            <el-table-column label="功能描述" align="center" prop="description"/>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                               v-hasPermi="['iiot:acl:edit']">修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['iiot:acl:remove']">删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 添加或修改订阅主题对话框 -->
        <el-dialog :title="title" v-model="open" width="900px" append-to-body>
            <el-form ref="modelRef" :model="form" :rules="rules" label-width="120px">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="产品代码" prop="code">
                            <el-input v-model="form.code" placeholder="请输入产品代码" disabled/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="唯一标识符" prop="identifier">
                            <el-input v-model="form.identifier" placeholder="请输入唯一标识符"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="功能名称" prop="modelName">
                            <el-input v-model="form.modelName" placeholder="请输入功能名称"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="模型类型" prop="modelType">
                            <el-radio-group v-model="form.modelType">
                                <el-radio-button
                                    v-for="dict in iiot_model_definition_type"
                                    :key="dict.value"
                                    :label="dict.value"
                                >{{ dict.label }}
                                </el-radio-button>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="指令Topic后缀" prop="topicSuffix">
                            <el-input v-model="form.topicSuffix"
                                      placeholder="请输入指令Topic后缀 (上行时为接收,下行时为发送)"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="应答Topic后缀" prop="responseTopicSuffix">
                            <el-input v-model="form.responseTopicSuffix"
                                      placeholder="请输入应答Topic后缀 (上行时为发送,下行时为接收)"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="指令数据规格" prop="dataSpec">
                            <el-input v-model="form.dataSpec" type="textarea" :rows="5"
                                      placeholder="请输入指令数据规格 (上行时为校验规则,下行时为输入参数)" disabled
                                      class="mb8"/>
                            <el-button type="primary" icon="Edit" @click="handleEditJSON('dataSpec', '指令数据规格')">编辑
                            </el-button>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="应答数据规格" prop="responseDataSpec">
                            <el-input v-model="form.responseDataSpec" type="textarea" :rows="5"
                                      placeholder="请输入应答数据规格 (定义应答消息的data结构和值的来源)" disabled
                                      class="mb8"/>
                            <el-button type="primary" icon="Edit"
                                       @click="handleEditJSON('responseDataSpec', '应答数据规格')">
                                编辑
                            </el-button>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="上行内容判别键" prop="discriminatorKey">
                            <el-input v-model="form.discriminatorKey"
                                      placeholder="请输入上行内容判别键 (当同Topic有多种data结构时使用)"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="功能描述" prop="description">
                            <el-input v-model="form.description" placeholder="请输入功能描述"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="数据持久化配置" prop="persistenceConfig">
                            <el-input v-model="form.persistenceConfig" type="textarea" :rows="5"
                                      placeholder="请输入数据持久化配置" disabled
                                      class="mb8"/>
                            <el-button type="primary" icon="Edit"
                                       @click="handleEditConfig('persistenceConfig', '数据持久化配置')">编辑
                            </el-button>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="是否展示" prop="showUi">
                            <el-radio-group v-model="form.showUi">
                                <el-radio
                                    v-for="dict in iiot_model_definition_show"
                                    :key="Number(dict.value)"
                                    :label="Number(dict.value)"
                                >{{ dict.label }}
                                </el-radio>
                            </el-radio-group>
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

        <!-- 编辑指令数据规格/应答数据规格 -->
        <el-dialog :title="titleJSON" v-model="openJSON" width="1300px" append-to-body>
            <field-schema-dialog :json="json" ref="fieldSchemaRef" v-if="openJSON"></field-schema-dialog>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="submitJSON(strJSON)">确 定</el-button>
                    <el-button @click="cancelJson">取 消</el-button>
                </div>
            </template>
        </el-dialog>

        <persistence-config ref="persistenceConfigRef" v-model="config" v-if="openConfig" @save="submitConfig"
                            @close="cancelConfig"></persistence-config>
    </div>
</template>

<script setup name="FunctionalModel">
import {
    addModelDefinition,
    delModelDefinition,
    getModelDefinition,
    listModelDefinition, refreshModelDefinition,
    updateModelDefinition
} from "@/api/iiot/modelDefinition";
import FieldSchemaDialog from "@/views/iiot/product/components/fieldSchemaDialog.vue";
import PersistenceConfig from "@/views/iiot/product/components/persistenceConfig.vue";

const {proxy} = getCurrentInstance();
const {iiot_model_definition_type, iiot_model_definition_show} = proxy.useDict("iiot_model_definition_type", "iiot_model_definition_show");

const props = defineProps({
    productId: {
        type: [String, Number],
        default: 0
    },
    code: {
        type: String,
        default: ''
    },
})

const modelList = ref([]);
const open = ref(false);
const openJSON = ref(false)
const openConfig = ref(false)
const loading = ref(false);
const showSearch = ref(true);
const ids = ref([]);
const modelNames = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
// 编辑数据规格
const titleJSON = ref("");
const strJSON = ref("");
const json = ref("");
const fieldSchemaRef = ref(null)
// 编辑数据持久化配置
const titleConfig = ref("");
const strConfig = ref("");
const config = ref("");
const persistenceConfigRef = ref(null)

const data = reactive({
    form: {},
    queryParams: {
        modelName: undefined,
        modelType: undefined
    },
    rules: {
        identifier: [{required: true, message: "唯一标识符不能为空", trigger: "blur"}],
        modelName: [{required: true, message: "功能名称不能为空", trigger: "blur"}],
        modelType: [{required: true, message: "模型类型不能为空", trigger: "change"}],
        topicSuffix: [{required: true, message: "指令Topic后缀不能为空", trigger: "blur"}],
        dataSpec: [
            {required: true, message: "指令数据规格不能为空", trigger: "blur"},
            {validator: validateJson, trigger: "blur"}
        ],
        responseDataSpec: [
            {validator: validateJson, trigger: "blur"}
        ],
    }
});

const {queryParams, form, rules} = toRefs(data);

/** JSON格式校验 */
function validateJson(rule, value, callback) {
    if (value === "" || value === undefined || value === null) {
        callback();
    } else {
        try {
            JSON.parse(value);
            callback();
        } catch (e) {
            callback(new Error("请输入有效的JSON格式字符串"));
        }
    }
}

/** 监听 code 参数变化 */
watch(() => props.code, (newVal) => {
    getList();
}, {immediate: false});

/** 查询模型列表 */
function getList() {
    if (props.code) {
        loading.value = true;
        listModelDefinition({...queryParams.value, code: props.code}).then(response => {
            modelList.value = response;
            total.value = response.total;
            loading.value = false;
        });
    }
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
        code: props.code,
        identifier: undefined,
        modelName: undefined,
        modelType: undefined,
        topicSuffix: undefined,
        discriminatorKey: undefined,
        dataSpec: undefined,
        responseTopicSuffix: undefined,
        responseDataSpec: undefined,
        description: undefined,
        persistenceConfig: undefined,
        showUi: 1,
    };
    proxy.resetForm("modelRef");
}

/** 搜索按钮操作 */
function handleQuery() {
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
    modelNames.value = selection.map(item => item.modelName);
    single.value = selection.length !== 1;
    multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
    reset();
    open.value = true;
    title.value = "添加功能模型";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const modelId = row.id || ids.value;
    getModelDefinition(modelId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改功能模型";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["modelRef"].validate(valid => {
        if (valid) {
            if (form.value.id != undefined) {
                updateModelDefinition(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addModelDefinition(form.value).then(response => {
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
    const modelIds = row.id || ids.value;
    const modelNames = row.modelName || modelNames.value;
    proxy.$modal.confirm('是否确认删除功能名称为"' + modelNames + '"的数据项？').then(function () {
        return delModelDefinition(modelIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {
    });
}

/** 删除按钮操作 */
function handleRefresh() {
    if (props.code) {
        proxy.$modal.confirm('是否确认刷新功能模型缓存？').then(function () {
            return refreshModelDefinition(props.code);
        }).then(() => {
            getList();
            proxy.$modal.msgSuccess("刷新缓存成功");
        }).catch(() => {
        });
    }
}

onMounted(() => {
    getList();
});

function handleEditJSON(str, titleStr) {
    json.value = form.value[str]
    openJSON.value = true
    strJSON.value = str
    titleJSON.value = '编辑' + titleStr
}

function submitJSON() {
    form.value[strJSON.value] = JSON.stringify(fieldSchemaRef.value?.outputModel)
    cancelJson()
}

function cancelJson() {
    json.value = ''
    openJSON.value = false
    strJSON.value = ''
    titleJSON.value = ''
}

function handleEditConfig(str, titleStr) {
    config.value = form.value[str]
    openConfig.value = true
    strConfig.value = str
    titleConfig.value = '编辑' + titleStr
}

function submitConfig(e) {
    form.value[strConfig.value] = JSON.stringify(e)
    cancelConfig()
}

function cancelConfig() {
    config.value = ''
    openConfig.value = false
    strConfig.value = ''
    titleConfig.value = ''
}
</script>

<style scoped lang="scss">

</style>
