<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="产品名称" prop="name" style="width: calc(25% - 32px)">
                <el-input
                    v-model="queryParams.name"
                    placeholder="请输入产品名称"
                    clearable
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="产品代码" prop="code" style="width: calc(25% - 32px)">
                <el-input
                    v-model="queryParams.code"
                    placeholder="请输入产品代码"
                    clearable
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="产品类型" prop="productTypeId" style="width: calc(25% - 32px)">
                <el-tree-select
                    v-model="queryParams.productTypeId"
                    :data="productTypeOptions"
                    :props="{ value: 'id', label: 'name', children: 'children' }"
                    value-key="id"
                    check-strictly
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="设备类型" prop="deviceType" style="width: calc(25% - 32px)">
                <el-select v-model="queryParams.deviceType" clearable>
                    <el-option
                        v-for="dict in iiot_protocol_gateway_type"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="产品状态" prop="status" style="width: calc(25% - 32px)">
                <el-select v-model="queryParams.status" clearable>
                    <el-option
                        v-for="dict in sys_normal_disable"
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
                    v-hasPermi="['iiot:product:add']"
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
                    v-hasPermi="['iiot:product:edit']"
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
                    v-hasPermi="['iiot:product:remove']"
                >删除
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="产品名称" align="center" prop="name"/>
            <el-table-column label="产品代码" align="center" prop="code"/>
            <el-table-column label="产品类型" align="center" prop="productType.name"/>
            <el-table-column label="设备类型" align="center" prop="deviceType" width="80">
                <template #default="scope">
                    <dict-tag :options="iiot_protocol_gateway_type" :value="scope.row.deviceType"/>
                </template>
            </el-table-column>
            <el-table-column label="所属网关" align="center" prop="gateway.name"/>
            <el-table-column label="所属协议" align="center" prop="protocol.name"/>
            <el-table-column label="所属分组" align="center" prop="deviceGroup.name"/>
            <el-table-column label="启用状态" align="center" prop="status" width="80">
                <!--        <template #default="scope">-->
                <!--          <dict-tag :options="sys_normal_disable" :value="scope.row.status" />-->
                <!--        </template>-->
                <template #default="scope">
                    <el-switch
                        v-model="scope.row.status"
                        active-value="1"
                        inactive-value="0"
                        @change="handleUpdateStatus(scope.row)"
                    ></el-switch>
                </template>
            </el-table-column>
<!--            <el-table-column label="连接状态" align="center" prop="gatewayStatus" width="80">-->
<!--                <template #default="scope">-->
<!--                    <dict-tag :options="iiot_product_status" :value="scope.row.gatewayStatus"/>-->
<!--                </template>-->
<!--            </el-table-column>-->
            <!--      <el-table-column label="产品说明" align="center" prop="remark"></el-table-column>-->
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="250" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                               v-hasPermi="['iiot:product:edit']">修改
                    </el-button>
                    <el-button link type="primary" icon="Edit" @click="handleUpdateModel(scope.row)"
                               v-hasPermi="['iiot:product:edit']">物模型
                    </el-button>
                    <!--          <el-button link :type="scope.row.status === '0' ? 'primary' : 'warning'" icon="Edit" @click="handleUpdateStatus(scope.row)" v-hasPermi="['iiot:product:edit']">{{ scope.row.status === "0" ? "启用" : "禁用" }}</el-button>-->
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['iiot:product:remove']">删除
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

        <!-- 添加或修改岗位对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="productRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="产品名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入产品名称"/>
                </el-form-item>
                <el-form-item label="产品代码" prop="code">
                    <el-input v-model="form.code" placeholder="请输入产品代码"/>
                </el-form-item>
                <el-form-item label="产品类型" prop="productTypeId">
                    <el-tree-select
                        v-model="form.productTypeId"
                        :data="productTypeOptions"
                        :props="{ value: 'id', label: 'name', children: 'children' }"
                        value-key="id"
                        placeholder="选择产品类型"
                        check-strictly
                    />
                </el-form-item>
                <el-form-item label="设备类型" prop="deviceType">
                    <el-radio-group v-model="form.deviceType">
                        <el-radio-button
                            v-for="item in iiot_protocol_gateway_type"
                            :key="item.value"
                            :label="item.value"
                        >{{ item.label }}
                        </el-radio-button>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="所属网关" prop="gatewayId">
                    <el-select v-model="form.gatewayId" placeholder="请选择所属网关">
                        <el-option
                            v-for="dict in gatewayList"
                            :key="dict.id"
                            :label="dict.name"
                            :value="dict.id"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="所属协议" prop="protocolId">
                    <el-select v-model="form.protocolId" placeholder="请选择所属协议" disabled>
                        <el-option
                            v-for="dict in protocolList"
                            :key="dict.id"
                            :label="dict.name"
                            :value="dict.id"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="所属分组" prop="groupId">
                    <el-tree-select
                        v-model="form.groupId"
                        :data="deviceGroupOptions"
                        :props="{ value: 'id', label: 'name', children: 'children' }"
                        value-key="id"
                        placeholder="选择所属分组"
                        check-strictly
                    />
                </el-form-item>
                <el-form-item label="产品说明" prop="remark">
                    <el-input v-model="form.remark" type="textarea" placeholder="请输入产品说明"/>
                </el-form-item>
                <el-form-item label="产品图片" prop="logo">
                    <ProductImageUpload v-model="form.logo" />
                </el-form-item>
<!--                <el-form-item label="主机" prop="config.host">-->
<!--                    <el-input v-model="form.config.host" placeholder="请输入主机"/>-->
<!--                </el-form-item>-->
<!--                <el-form-item label="端口" prop="config.port">-->
<!--                    <el-input-number v-model="form.config.port" controls-position="right" :min="0"/>-->
<!--                </el-form-item>-->
<!--                <el-form-item label="用户名" prop="config.userName">-->
<!--                    <el-input v-model="form.config.userName" placeholder="请输入用户名"/>-->
<!--                </el-form-item>-->
<!--                <el-form-item label="密码" prop="config.passdWord">-->
<!--                    <el-input v-model="form.config.passdWord" placeholder="请输入密码"/>-->
<!--                </el-form-item>-->
                <el-form-item label="从站地址" prop="unitId">
                    <el-input v-model="form.unitId" placeholder="请输入从站地址"/>
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

<script setup name="Product">
import {listGroup} from "@/api/iiot/deviceGroup";
import {addProduct, changeStatusProduct, delProduct, getProduct, listProduct, updateProduct, startModbusDeviceTask, stopModbusDeviceTask } from "@/api/iiot/product";
import {listProductType} from "@/api/iiot/productType";
import {listDeviceAll} from "@/api/iiot/gateway";
import {listProtocolAll} from "@/api/iiot/protocol";
import {onMounted} from "vue";
import ProductImageUpload from './components/ProductImageUpload.vue'; // 引入图片上传组件（将被移除）

const {proxy} = getCurrentInstance();
const {
    sys_normal_disable,
    iiot_protocol_gateway_type,
    iiot_product_status
} = proxy.useDict("sys_normal_disable", "iiot_protocol_gateway_type", "iiot_product_status");

const productList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const names = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const productTypeList = ref([])
const productTypeOptions = ref([]);
const gatewayList = ref([]);
const protocolList = ref([]);
const deviceGroupList = ref([])
const deviceGroupOptions = ref([])

const data = reactive({
    form: {},
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        code: undefined,
        productTypeId: undefined,
        deviceType: undefined,
        status: undefined,
        protocolId: '1962754791346536450',
    },
    rules: {
        name: [{required: true, message: "产品名称不能为空", trigger: "blur"}],
        code: [{required: true, message: "产品代码不能为空", trigger: "blur"}],
        productTypeId: [{required: true, message: "产品类型不能为空", trigger: "change"}],
        deviceType: [{required: true, message: "设备类型不能为空", trigger: "change"}],
        // 'config.host': [{required: true, message: "主机不能为空", trigger: "blur"}],
        // 'config.port': [{required: true, message: "端口不能为空", trigger: "blur"}],
        // 'config.userName': [{required: true, message: "用户名不能为空", trigger: "blur"}],
    }
});

const {queryParams, form, rules} = toRefs(data);

/** 查询产品列表 */
function getList() {
    loading.value = true;
    return listProduct(queryParams.value).then(response => {
        productList.value = response.rows.map(it => {
            return {
                ...it,
                productType: productTypeList.value.filter(itt => itt.id === it.productTypeId)[0],
                gateway: gatewayList.value.filter(itt => itt.id === it.gatewayId)[0],
                protocol: protocolList.value.filter(itt => itt.id === it.protocolId)[0],
                deviceGroup: deviceGroupList.value.filter(itt => itt.id === it.groupId)[0]
            }
        });
        total.value = response.total;
        loading.value = false;
    });
}

/** 查询设备分组下拉树结构 */
function getTreeSelectDeviceGroup() {
    deviceGroupList.value = [];
    deviceGroupOptions.value = [];
    return listGroup().then(response => {
        deviceGroupList.value = response.rows;
        deviceGroupOptions.value = proxy.handleTree(response.rows, "id", "pid");
    });
}

/** 查询产品类型下拉树结构 */
function getTreeSelect() {
    productTypeList.value = [];
    productTypeOptions.value = [];
    return listProductType().then(response => {
        productTypeList.value = response.rows;
        productTypeOptions.value = proxy.handleTree(response.rows, "id", "pid");
    });
}

/** 查询网关列表 */
function getGatewayList() {
    return listDeviceAll().then(response => {
        gatewayList.value = response
    });
}

/** 查询协议列表 */
function getProtocolList() {
    return listProtocolAll().then(response => {
        protocolList.value = response
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
        code: undefined,
        productTypeId: undefined,
        deviceType: undefined,
        gatewayId: undefined,
        protocolId: '1962754791346536450',
        status: '0',
        remark: undefined,
        logo: undefined,
        // config: {
        //     host: undefined,
        //     port: undefined,
        //     userName: undefined,
        //     passdWord: undefined,
        // },
        unitId: undefined,
    };
    proxy.resetForm("productRef");
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
    title.value = "添加产品";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const productId = row.id || ids.value;
    getProduct(productId).then(response => {
        form.value = {
            ...response.data,
            // config: {
            //     host: response.data.config?.host || undefined,
            //     port: response.data.config?.port || undefined,
            //     userName: response.data.config?.userName || undefined,
            //     passdWord: response.data.config?.passdWord || undefined,
            // }
        };

        open.value = true;
        title.value = "修改产品";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["productRef"].validate(valid => {
        if (valid) {
            let data = {
                ...form.value,
            };
            if (data.id != undefined) {
                updateProduct(data).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addProduct(data).then(response => {
                    proxy.$modal.msgSuccess("新增成功");
                    open.value = false;
                    getList();
                });
            }
        }
    });
}

/** 启用禁用按钮操作 */
function handleUpdateStatus(row) {
    const productIds = row.id || ids.value;
    const productNames = row.name || names.value;
    proxy.$modal.confirm('是否确认' + (row.status === '1' ? "启用" : "禁用") + '产品名称为"' + productNames + '"的数据项？').then(async function () {
        if(row.status === '1') {
            await startModbusDeviceTask(productIds);
        } else {
            await stopModbusDeviceTask(productIds);
        }
        return changeStatusProduct(productIds, row.status);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess((row.status === '1' ? "启用" : "禁用") + "成功");
    }).catch(() => {
        row.status = row.status === "0" ? "1" : "0";
    });
}

/** 删除按钮操作 */
function handleDelete(row) {
    const productIds = row.id || ids.value;
    const productNames = row.name || names.value;
    proxy.$modal.confirm('是否确认删除产品名称为"' + productNames + '"的数据项？').then(function () {
        return delProduct(productIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {
    });
}

function handleUpdateModel(row) {
    proxy.$router.push({path: "/iiot/physical-model-modbus/index/" + row.id});
}

onMounted(async () => {
    await Promise.all([getTreeSelectDeviceGroup(), getTreeSelect(), getGatewayList(), getProtocolList()])
    await getList()
})

</script>

<style scoped lang="scss">
:deep(.el-select) {
    width: 100%;
}
:deep(.hide .el-upload--picture-card) {
    display: none;
}
</style>
