<template>
    <!--产品配置信息-->
    <el-descriptions
        class="margin-20"
        :column="4"
        border
    >
        <el-descriptions-item>
            <template #label>
                <div class="cell-item">
                    产品名称
                </div>
            </template>
            {{ productInfo.name }}
        </el-descriptions-item>
        <el-descriptions-item>
            <template #label>
                <div class="cell-item">
                    产品代码
                </div>
            </template>
            {{ productInfo.code }}
        </el-descriptions-item>
        <el-descriptions-item>
            <template #label>
                <div class="cell-item">
                    产品类型
                </div>
            </template>
            {{ productInfo.productType?.name }}
        </el-descriptions-item>
        <el-descriptions-item>
            <template #label>
                <div class="cell-item">
                    设备类型
                </div>
            </template>
            <dict-tag :options="iiot_protocol_gateway_type" :value="productInfo.deviceType"/>
        </el-descriptions-item>
        <el-descriptions-item>
            <template #label>
                <div class="cell-item">
                    所属网关
                </div>
            </template>
            {{ productInfo.gateway?.name }}
        </el-descriptions-item>
        <el-descriptions-item>
            <template #label>
                <div class="cell-item">
                    所属协议
                </div>
            </template>
            {{ productInfo.protocol?.name }}
        </el-descriptions-item>
        <el-descriptions-item>
            <template #label>
                <div class="cell-item">
                    状态
                </div>
            </template>
            <dict-tag :options="sys_normal_disable" :value="productInfo.status"/>
        </el-descriptions-item>
        <el-descriptions-item>
            <template #label>
                <div class="cell-item">
                    产品说明
                </div>
            </template>
            {{ productInfo.remark }}
        </el-descriptions-item>
    </el-descriptions>

    <!--标签页-->
    <el-tabs v-model="activeName" class="margin-20" @tab-click="handleClick">
        <el-tab-pane label="订阅主题" name="订阅主题">
            <subscription-theme :productId="route.params.productId" :code="productInfo.code"
                                :userName="productInfo.config?.userName"></subscription-theme>
        </el-tab-pane>
        <el-tab-pane label="功能模型" name="功能模型">
            <functional-model :productId="route.params.productId" :code="productInfo.code"></functional-model>
        </el-tab-pane>
    </el-tabs>
</template>

<script setup name="PhysicalModel">

import {getProduct} from "@/api/iiot/product";
import {listProductType} from "@/api/iiot/productType";
import {listDeviceAll} from "@/api/iiot/gateway";
import {listProtocolAll} from "@/api/iiot/protocol";
import SubscriptionTheme from "@/views/iiot/product/components/subscriptionTheme.vue";
import FunctionalModel from "@/views/iiot/product/components/functionalModel.vue";

const {proxy} = getCurrentInstance();
const {
    sys_normal_disable,
    iiot_protocol_gateway_type
} = proxy.useDict("sys_normal_disable", "iiot_protocol_gateway_type");

const productTypeList = ref([])
const gatewayList = ref([]);
const protocolList = ref([]);
const productInfo = ref({})

const activeName = ref('订阅主题')

const route = useRoute();


/** 查询设备分组下拉树结构 */
function getTreeSelect() {
    productTypeList.value = [];
    return listProductType().then(response => {
        productTypeList.value = response.rows;
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

/** 查询产品配置详细 */
function getProductInfo(productId) {
    return getProduct(productId).then(response => {
        productInfo.value = {
            ...response.data,
            productType: productTypeList.value.filter(itt => itt.id === response.data.productTypeId)[0],
            gateway: gatewayList.value.filter(itt => itt.id === response.data.gatewayId)[0],
            protocol: protocolList.value.filter(itt => itt.id === response.data.protocolId)[0],
            config: {
                host: response.data.config?.host || undefined,
                port: response.data.config?.port || undefined,
                userName: response.data.config?.userName || undefined,
                passdWord: response.data.config?.passdWord || undefined,
            }
        };
        console.log(productInfo.value)
    });
}

const handleClick = (tab) => {
    activeName.value = tab.props.name
    console.log(activeName)
}

onMounted(async () => {
    await Promise.all([getTreeSelect(), getGatewayList(), getProtocolList()])
    await getProductInfo(route.params && route.params.productId);
});
</script>

<style scoped lang="scss">
.el-descriptions {
    margin-top: 20px;
}

.cell-item {
    display: flex;
    align-items: center;
}

.margin-20 {
    margin: 20px;
}
</style>
