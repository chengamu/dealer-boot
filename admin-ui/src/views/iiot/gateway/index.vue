<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="网关名称" prop="name" style="width: calc(25% - 32px)">
                <el-input
                    v-model="queryParams.name"
                    placeholder="请输入网关名称"
                    clearable
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="设备编号" prop="deviceSn" style="width: calc(25% - 32px)">
                <el-input
                    v-model="queryParams.deviceSn"
                    placeholder="请输入设备编号"
                    clearable
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="所属分组" prop="deviceGroupId" style="width: calc(25% - 32px)">
                <el-tree-select
                    v-model="queryParams.deviceGroupId"
                    :data="deviceGroupOptions"
                    :props="{ value: 'id', label: 'name', children: 'children' }"
                    value-key="id"
                    check-strictly
                    @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="设备状态" prop="status" style="width: calc(25% - 32px)">
                <el-select v-model="queryParams.status" placeholder="设备状态" clearable>
                    <el-option
                        v-for="dict in iiot_gateway_status"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="设备类型" prop="status" style="width: calc(25% - 32px)">
                <el-select v-model="queryParams.type" placeholder="设备类型" clearable>
                    <el-option
                        v-for="dict in iiot_protocol_gateway_device_type"
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
                    v-hasPermi="['iiot:gateway:add']"
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
                    v-hasPermi="['iiot:gateway:edit']"
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
                    v-hasPermi="['iiot:gateway:remove']"
                >删除
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="gatewayList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="网关名称" align="center" prop="name"/>
            <el-table-column label="设备编号" align="center" prop="deviceSn"/>
            <el-table-column label="设备地址" align="center" prop="address"/>
            <el-table-column label="所属分组" align="center" prop="deviceGroup.name"/>
            <el-table-column label="设备类型" align="center" prop="typeObj.label"/>
            <el-table-column label="设备状态" align="center" prop="status">
<!--                <template #default="scope">-->
<!--                    <dict-tag :options="iiot_gateway_status" :value="scope.row.status"/>-->
<!--                </template>-->
                <template #default="scope">
                    <el-switch
                        v-model="scope.row.status"
                        active-value="online"
                        inactive-value="offline"
                        @change="handleOnOff(scope.row)"
                    ></el-switch>
                </template>
            </el-table-column>
            <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="270" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                               v-hasPermi="['iiot:gateway:edit']">修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['iiot:gateway:remove']">删除
                    </el-button>
<!--                    <el-button link type="primary" icon="Switch" @click="handleOnOff(scope.row)"-->
<!--                               v-hasPermi="['iiot:gateway:edit']">{{ scope.row.status === 'online' ? '断开' : '连接' }}-->
<!--                    </el-button>-->
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

        <!-- 添加或修改网关对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="gatewayRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="网关名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入网关名称"/>
                </el-form-item>
                <el-form-item label="设备编号" prop="deviceSn">
                    <el-input v-model="form.deviceSn" placeholder="请输入设备编号"/>
                </el-form-item>
                <el-form-item label="设备地址" prop="address">
                    <el-input v-model="form.address" placeholder="请输入设备地址"/>
                </el-form-item>
                <el-form-item label="所属分组" prop="deviceGroupId">
                    <el-tree-select
                        v-model="form.deviceGroupId"
                        :data="deviceGroupOptions"
                        :props="{ value: 'id', label: 'name', children: 'children' }"
                        value-key="id"
                        placeholder="选择所属分组"
                        check-strictly
                    />
                </el-form-item>
                <el-form-item label="设备类型" prop="type">
                    <el-radio-group v-model="form.type">
                        <el-radio-button
                            v-for="dict in iiot_protocol_gateway_device_type"
                            :key="dict.value"
                            :label="dict.value"
                        >{{ dict.label }}
                        </el-radio-button>
                    </el-radio-group>
                </el-form-item>


                <el-form-item label="网关IP" prop="ip">
                    <el-input v-model="form.ip" placeholder="请输入网关IP"/>
                </el-form-item>
                <el-form-item label="网关端口" prop="port">
                    <el-input-number v-model="form.port" controls-position="right" :min="0"/>
                </el-form-item>
                <el-form-item label="账号" prop="account">
                    <el-input v-model="form.account" placeholder="请输入账号"/>
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input v-model="form.password" placeholder="请输入密码"/>
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

<script setup name="Gateway">
import {listGroup} from "@/api/iiot/deviceGroup";
import {
    addDevice,
    delDevice,
    disconnectDevice, disconnectDeviceModbus,
    getDevice,
    initializeDevice, initializeDeviceModbus,
    listDevice,
    updateDevice,
    changeStatusDeviceWebSocket
} from "@/api/iiot/gateway";
import {onMounted} from "vue";

const {proxy} = getCurrentInstance();
const {iiot_gateway_status, iiot_protocol_gateway_device_type} = proxy.useDict("iiot_gateway_status", "iiot_protocol_gateway_device_type");

const gatewayList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const names = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const deviceGroupList = ref([])
const deviceGroupOptions = ref([])

const data = reactive({
    form: {},
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        deviceSn: undefined,
        deviceGroupId: undefined,
        status: undefined
    },
    rules: {
        name: [{required: true, message: "网关名称不能为空", trigger: "blur"}],
        deviceSn: [{required: true, message: "设备编号不能为空", trigger: "blur"}],
        deviceGroupId: [{required: true, message: "所属分组不能为空", trigger: "change"}],
        ip: [
            {required: true, message: "IP不能为空", trigger: "blur"},
            {
                pattern: /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/,
                message: "请输入合法的IP地址",
                trigger: "blur"
            }],
        port: [{required: true, message: "端口不能为空", trigger: "blur"}],
        type: [{required: true, message: "设备类型未选择", trigger: "change"}],
    }
});

const {queryParams, form, rules} = toRefs(data);

/** 查询网关列表 */
function getList() {
    loading.value = true;
    return listDevice(queryParams.value).then(response => {
        gatewayList.value = response.rows.map(it => {
            return {
                ...it,
                deviceGroup: deviceGroupList.value.filter(itt => itt.id === it.deviceGroupId)[0],
                typeObj: iiot_protocol_gateway_device_type.value.filter(itt => itt.value === it.type)[0],
            }
        });
        total.value = response.total;
        loading.value = false;
    });
}

/** 查询分组下拉树结构 */
function getTreeSelect() {
    deviceGroupList.value = [];
    deviceGroupOptions.value = [];
    return listGroup().then(response => {
        deviceGroupList.value = response.rows;
        deviceGroupOptions.value = proxy.handleTree(response.rows, "id", "pid");
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
        deviceSn: undefined,
        address: undefined,
        deviceGroupId: undefined,
        ip: undefined,
        port: undefined,
        account: undefined,
        password: undefined,
    };
    proxy.resetForm("gatewayRef");
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
    title.value = "添加网关";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const gatewayId = row.id || ids.value;
    getDevice(gatewayId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改网关";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["gatewayRef"].validate(valid => {
        if (valid) {
            if (form.value.id != undefined) {
                updateDevice(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addDevice(form.value).then(response => {
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
    const gatewayIds = row.id || ids.value;
    const gatewayNames = row.name || names.value;
    proxy.$modal.confirm('是否确认删除网关名称为"' + gatewayNames + '"的数据项？').then(function () {
        return delDevice(gatewayIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {
    });
}

/** 连接、断开按钮操作 */
function handleOnOff(row) {
    const gatewayId = row.id;
    const deviceSn = row.deviceSn;
    const gatewayName = row.name;
    let text = row.status === 'online' ? '连接' : '断开'

    proxy.$modal.confirm('是否确认' + text + '网关名称为"' + gatewayName + '"的数据项？').then(function () {
        proxy.$modal.loading("操作执行中，请稍候...");
        if (row.status === 'online') {
            if(row.type === '1') { // modbus网关
                return initializeDeviceModbus(deviceSn)
            } else if(row.type === '0') { // MQTT网关
                return initializeDevice(gatewayId);
            } else { // WebSocket网关
                return changeStatusDeviceWebSocket(gatewayId, 'online');
            }
        } else {
            if(row.type === '1') { // modbus网关
                return disconnectDeviceModbus(deviceSn)
            } else if(row.type === '0') { // MQTT网关
                return disconnectDevice(gatewayId);
            } else { // WebSocket网关
                return changeStatusDeviceWebSocket(gatewayId, 'offline');
            }
        }
    }).then(() => {
        // 等待5秒后再执行后续操作
        setTimeout(() => {
            proxy.$modal.closeLoading();
            getList();
            proxy.$modal.msgSuccess(text + "成功");
        }, 1000);
    }).catch(() => {
        row.status = row.status === "offline" ? "online" : "offline";
    });
}

onMounted(async () => {
    await Promise.all([getTreeSelect()])
    await getList()
})
</script>

<style scoped lang="scss">
:deep(.el-select) {
    width: 100%;
}
</style>
