<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="姓名" prop="cardName">
                <el-input
                    v-model="queryParams.cardName"
                    placeholder="请输入姓名"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="状态" prop="cardStatus">
                <el-select v-model="queryParams.cardStatus" placeholder="状态" clearable style="width: 200px">
                    <el-option
                        v-for="dict in iiot_access_card_status.filter(it => it.status === '1')"
                        :key="Number(dict.value)"
                        :label="dict.label"
                        :value="Number(dict.value)"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="类型" prop="cardType">
                <el-select v-model="queryParams.cardType" placeholder="类型" clearable style="width: 200px">
                    <el-option
                        v-for="dict in iiot_access_card_type.filter(it => it.status === '1')"
                        :key="Number(dict.value)"
                        :label="dict.label"
                        :value="Number(dict.value)"
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
                    v-hasPermi="['dhsdk:card:add']"
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
                    v-hasPermi="['dhsdk:card:edit']"
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
                    v-hasPermi="['dhsdk:card:remove']"
                >删除
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="accessCardList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="姓名" align="center" prop="cardName"/>
<!--            <el-table-column label="卡密码" align="center" prop="cardPasswd"/>-->
            <el-table-column label="状态" align="center" prop="cardStatus">
                <template #default="scope">
                    <dict-tag :options="iiot_access_card_status.filter(it => it.status === '1')" :value="scope.row.cardStatus"/>
                </template>
            </el-table-column>
            <el-table-column label="类型" align="center" prop="cardType">
                <template #default="scope">
                    <dict-tag :options="iiot_access_card_type.filter(it => it.status === '1')" :value="scope.row.cardType"/>
                </template>
            </el-table-column>
<!--            <el-table-column label="使用次数" align="center" prop="useTimes"/>-->
<!--            <el-table-column label="是否首卡" align="center" prop="firstEnter">-->
<!--                <template #default="scope">-->
<!--                    <dict-tag :options="iiot_access_card_first_enter" :value="scope.row.firstEnter"/>-->
<!--                </template>-->
<!--            </el-table-column>-->
<!--            <el-table-column label="是否有效" align="center" prop="enable">-->
<!--                <template #default="scope">-->
<!--                    <dict-tag :options="iiot_access_card_enable" :value="scope.row.enable"/>-->
<!--                </template>-->
<!--            </el-table-column>-->
<!--            <el-table-column label="有效期开始时间" align="center" prop="startTime" width="180">-->
<!--                <template #default="scope">-->
<!--                    <span>{{ parseTime(scope.row.startTime) }}</span>-->
<!--                </template>-->
<!--            </el-table-column>-->
            <el-table-column label="有效期" align="center" prop="endTime" width="180">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.endTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="250" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dhsdk:card:edit']">
                        修改
                    </el-button>
                    <el-button link type="primary" icon="Edit" @click="handleSync(scope.row)" v-hasPermi="['dhsdk:card:sync']">
                        同步
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['dhsdk:card:remove']">删除
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

        <!-- 添加或修改门禁卡对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="accessCardRef" :model="form" :rules="rules" label-width="120px">
                <el-form-item label="姓名" prop="cardName">
                    <el-input v-model="form.cardName" placeholder="请输入姓名"/>
                </el-form-item>
<!--                <el-form-item label="卡密码" prop="cardPasswd">-->
<!--                    <el-input v-model="form.cardPasswd" placeholder="请输入卡密码"/>-->
<!--                </el-form-item>-->
                <el-form-item label="状态" prop="cardStatus">
                    <el-select v-model="form.cardStatus" placeholder="请选择">
                        <el-option
                            v-for="dict in iiot_access_card_status.filter(it => it.status === '1')"
                            :key="Number(dict.value)"
                            :label="dict.label"
                            :value="Number(dict.value)"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="类型" prop="cardType">
                    <el-select v-model="form.cardType" placeholder="请选择">
                        <el-option
                            v-for="dict in iiot_access_card_type.filter(it => it.status === '1')"
                            :key="Number(dict.value)"
                            :label="dict.label"
                            :value="Number(dict.value)"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="使用次数" prop="useTimes" :required="form.cardType === 2">
                    <el-input-number v-model="form.useTimes" :controls="false" :precision="0" :min="0"></el-input-number>
                </el-form-item>
<!--                <el-form-item label="是否首卡" prop="firstEnter">-->
<!--                    <el-radio-group v-model="form.firstEnter">-->
<!--                        <el-radio-->
<!--                            v-for="dict in iiot_access_card_first_enter"-->
<!--                            :key="Number(dict.value)"-->
<!--                            :label="Number(dict.value)"-->
<!--                        >{{ dict.label }}-->
<!--                        </el-radio>-->
<!--                    </el-radio-group>-->
<!--                </el-form-item>-->
<!--                <el-form-item label="是否有效" prop="enable">-->
<!--                    <el-radio-group v-model="form.enable">-->
<!--                        <el-radio-->
<!--                            v-for="dict in iiot_access_card_enable"-->
<!--                            :key="Number(dict.value)"-->
<!--                            :label="Number(dict.value)"-->
<!--                        >{{ dict.label }}-->
<!--                        </el-radio>-->
<!--                    </el-radio-group>-->
<!--                </el-form-item>-->
<!--                <el-form-item label="有效期开始时间" prop="startTime">-->
<!--                    <el-date-picker-->
<!--                        v-model="form.startTime"-->
<!--                        value-format="YYYY-MM-DD HH:mm:ss"-->
<!--                        type="datetime"-->
<!--                        placeholder="请选择有效期开始时间"-->
<!--                    />-->
<!--                </el-form-item>-->
                <el-form-item label="有效期" prop="endTime">
                    <el-date-picker
                        v-model="form.endTime"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        type="datetime"
                        placeholder="请选择有效期"
                    />
                </el-form-item>
                <el-form-item label="人脸照片" prop="face">
                    <ProductImageUpload v-model="form.face" />
                </el-form-item>
                <el-form-item label="同步设备" prop="deviceId">
                    <el-select v-model="form.deviceId" placeholder="请选择同步设备" multiple>
                        <el-option
                            v-for="dict in productList"
                            :key="dict.id"
                            :label="dict.name"
                            :value="dict.id"
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

<script setup name="AccessCard">
import { v4 as uuidv4 } from 'uuid';
import { listProduct } from "@/api/iiot/product";
import {listCard, getCard, addCard, delCard, updateCard, syncCard} from "@/api/iiot/accessCard";
import ProductImageUpload from "@/views/iiot/product/components/ProductImageUpload.vue";
import {onMounted} from "vue";

const {proxy} = getCurrentInstance();
const {
    iiot_access_card_status,
    iiot_access_card_type,
    iiot_access_card_first_enter,
    iiot_access_card_enable,
} = proxy.useDict("iiot_access_card_status", "iiot_access_card_type", "iiot_access_card_first_enter", "iiot_access_card_enable");

const productList = ref([]);
const accessCardList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const cardNames = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
    form: {},
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        cardName: undefined,
        cardStatus: undefined,
        cardType: undefined
    },
    rules: {
        cardName: [{required: true, message: "姓名不能为空", trigger: "blur"}],
        // cardPasswd: [{required: true, message: "卡密码不能为空", trigger: "blur"}],
        cardStatus: [{required: true, message: "状态不能为空", trigger: "change"}],
        cardType: [{required: true, message: "类型不能为空", trigger: "change"}],
        // startTime: [{required: true, message: "有效期开始时间不能为空", trigger: "blur"}],
        endTime: [{required: true, message: "有效期不能为空", trigger: "blur"}],
        face: [{required: true, message: "人脸照片不能为空", trigger: "change"}],
        deviceId: [{required: true, message: "同步设备不能为空", trigger: "change"}],
    }
});

const {queryParams, form, rules} = toRefs(data);

// 监听cardType变化，动态设置useTimes的验证规则并仅校验useTimes字段
watch(() => form.value.cardType, (newVal) => {
    // 动态设置useTimes的验证规则
    if (newVal === 2) {
        rules.value.useTimes = [{ required: true, message: "使用次数不能为空", trigger: "blur" }];
    } else {
        rules.value.useTimes = [];
    }
    
    // 使用nextTick确保在rules更新后再校验字段
    nextTick(() => {
        proxy.$refs.accessCardRef?.validateField("useTimes");
    });
});

/** 查询产品列表 */
function getProductList() {
    loading.value = true;
    return listProduct({ productTypeId: '1959168239269883905' }).then(response => {
        productList.value = response.rows
    });
}

/** 查询门禁卡列表 */
function getList() {
    loading.value = true;
    return listCard(queryParams.value).then(response => {
        accessCardList.value = response.rows.map(it => {
            return {
                ...it,
                deviceId: it.deviceId.slice(','),
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
    const uuid = uuidv4().replace(/-/g, '');
    // 优化默认值获取方式，避免直接访问数组[0]可能引起的错误
    const defaultStatus = iiot_access_card_status.value.find(it => it.status === '1');
    const defaultType = iiot_access_card_type.value.find(it => it.status === '1');
    
    form.value = {
        id: undefined,
        cardNo: uuid,
        userId: uuid,
        cardName: undefined,
        // cardPasswd: undefined,
        cardStatus: defaultStatus ? Number(defaultStatus.value) : undefined,
        cardType: defaultType ? Number(defaultType.value) : undefined,
        useTimes: undefined,
        // firstEnter: 0,
        // enable: 0,
        startTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
        endTime: undefined,
        face: undefined,
        deviceId: [],
    };
    
    // 根据默认的cardType设置useTimes的验证规则
    if (form.value.cardType === 2) {
        rules.value.useTimes = [{ required: true, message: "使用次数不能为空", trigger: "blur" }];
    } else {
        rules.value.useTimes = [];
    }
    
    proxy.resetForm("accessCardRef");
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
    cardNames.value = selection.map(item => item.cardName);
    single.value = selection.length !== 1;
    multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
    reset();
    open.value = true;
    title.value = "添加门禁卡";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const accessCardId = row.id || ids.value;
    getCard(accessCardId).then(response => {
        form.value = response.data;
        form.value.deviceId = response.data.deviceId.split(',');
        open.value = true;
        title.value = "修改门禁卡";
    });
}

/** 同步按钮操作 */
function handleSync(row) {
    const accessCardIds = row.id || ids.value;
    const accessCardNames = row.cardName || cardNames.value;
    proxy.$modal.confirm('是否确认同步姓名为"' + accessCardNames + '"的数据项？').then(function () {
        return syncCard(accessCardIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("同步成功");
    }).catch(() => {
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["accessCardRef"].validate(valid => {
        if (valid) {
            let deviceId = form.value.deviceId.join(',');
            if (form.value.id != undefined) {
                updateCard({ ...form.value, deviceId: deviceId }).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addCard({ ...form.value, deviceId: deviceId }).then(response => {
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
    const accessCardIds = row.id || ids.value;
    const accessCardNames = row.cardName || cardNames.value;
    proxy.$modal.confirm('是否确认删除姓名为"' + accessCardNames + '"的数据项？').then(function () {
        return delCard(accessCardIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {
    });
}

onMounted(async () => {
    await Promise.all([getProductList()])
    await getList()
})
</script>

<style scoped lang="scss">
:deep(.el-select) {
    width: 100%;
}
</style>