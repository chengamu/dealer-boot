<template>
    <el-dialog
        :model-value="true"
        title="超级表存储配置"
        width="700px"
        :close-on-click-modal="false"
        @close="handleClose"
    >
        <el-form label-width="80px" :model="config">
            <el-form-item label="启用">
                <el-switch v-model="config.enabled" />
            </el-form-item>

            <el-form-item label="策略">
                <el-select v-model="config.strategy" placeholder="请选择策略">
                    <el-option label="TDengine 超级表" value="TDENGINE_SUPER_TABLE" />
                    <el-option label="时间序列" value="TIMESERIES" />
                    <el-option label="关系型表" value="RELATIONAL" />
                </el-select>
            </el-form-item>

            <el-form-item label="超级表名">
                <el-input v-model="config.targetStable" placeholder="请输入超级表名" />
            </el-form-item>

            <el-form-item label="标签映射">
                <el-table :data="tagMappingList" border size="small" style="width:100%">
                    <el-table-column label="源字段" prop="key">
                        <template #default="{ row }">
                            <el-input v-model="row.key" placeholder="源字段" />
                        </template>
                    </el-table-column>
                    <el-table-column label="目标字段" prop="value">
                        <template #default="{ row }">
                            <el-input v-model="row.value" placeholder="目标字段" />
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="80">
                        <template #default="{ $index }">
                            <el-button link size="small" type="danger" @click="removeTag($index)">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <el-button type="primary" @click="addTag" class="mt10">新增标签</el-button>
            </el-form-item>

            <el-form-item label="字段映射">
                <el-table :data="fieldMappingList" border size="small" style="width:100%">
                    <el-table-column label="源字段" prop="key">
                        <template #default="{ row }">
                            <el-input v-model="row.key" placeholder="源字段" />
                        </template>
                    </el-table-column>
                    <el-table-column label="目标字段" prop="value">
                        <template #default="{ row }">
                            <el-input v-model="row.value" placeholder="目标字段" />
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="80">
                        <template #default="{ $index }">
                            <el-button link size="small" type="danger" @click="removeField($index)">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <el-button type="primary" @click="addField" class="mt10">新增字段</el-button>
            </el-form-item>
        </el-form>

        <template #footer>
            <el-button @click="doExport">导出 JSON</el-button>
            <el-upload :show-file-list="false" accept=".json" :on-change="onImport" class="el-upload-self">
                <el-button>导入 JSON</el-button>
            </el-upload>
            <el-button @click="handleClose">取消</el-button>
            <el-button type="primary" @click="handleSave">确定</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue';

const props = defineProps({
    modelValue: {
        type: [Object, String],
        default: () => ({})
    }
});

const emit = defineEmits(['update:modelValue', 'save', 'close']);

const config = reactive({
    enabled: true,
    strategy: 'TDENGINE_SUPER_TABLE',
    targetStable: '',
    tagMapping: {} as Record<string,string>,
    fieldMapping: {} as Record<string,string>
});

// --- tagMapping ---
const tagMappingList = ref<Array<{key: string, value: string}>>([]);

function updateTagMappingList() {
    tagMappingList.value = Object.entries(config.tagMapping || {}).map(([key, value]) => ({ key, value }));
}

function syncTagMapping() {
    const obj: Record<string,string> = {};
    tagMappingList.value.forEach(item => {
        if(item.key) obj[item.key] = item.value;
    });
    config.tagMapping = obj;
}

function addTag(){ 
    tagMappingList.value.push({ key: '', value: '' });
    syncTagMapping();
}

function removeTag(index: number){ 
    tagMappingList.value.splice(index, 1);
    syncTagMapping();
}

// --- fieldMapping ---
const fieldMappingList = ref<Array<{key: string, value: string}>>([]);

function updateFieldMappingList() {
    fieldMappingList.value = Object.entries(config.fieldMapping || {}).map(([key, value]) => ({ key, value }));
}

function syncFieldMapping() {
    const obj: Record<string,string> = {};
    fieldMappingList.value.forEach(item => {
        if(item.key) obj[item.key] = item.value;
    });
    config.fieldMapping = obj;
}

function addField(){ 
    fieldMappingList.value.push({ key: '', value: '' });
    syncFieldMapping();
}

function removeField(index: number){ 
    fieldMappingList.value.splice(index, 1);
    syncFieldMapping();
}

// 监听父组件传入的modelValue变化
watch(() => props.modelValue, (newVal) => {
    if (newVal) {
        let parsedVal = newVal;
        // 如果是字符串，则尝试解析为JSON对象
        if (typeof newVal === 'string') {
            try {
                parsedVal = JSON.parse(newVal);
            } catch (e) {
                console.error('解析modelValue失败', e);
                return;
            }
        }
        
        // 更新config对象
        Object.assign(config, parsedVal);
        
        // 更新列表数据
        updateTagMappingList();
        updateFieldMappingList();
    }
}, { immediate: true, deep: true });

// --- 通用操作 ---
function handleSave() {
    // 确保数据同步
    syncTagMapping();
    syncFieldMapping();
    
    // 构造要传递给父组件的数据
    const result = {
        enabled: config.enabled,
        strategy: config.strategy,
        targetStable: config.targetStable,
        tagMapping: config.tagMapping,
        fieldMapping: config.fieldMapping
    };
    
    emit('save', result);
    handleClose();
}

function handleClose() {
    emit('close');
}

function doExport(){
    // 确保导出的是最新数据
    syncTagMapping();
    syncFieldMapping();
    
    const result = {
        enabled: config.enabled,
        strategy: config.strategy,
        targetStable: config.targetStable,
        tagMapping: config.tagMapping,
        fieldMapping: config.fieldMapping
    };
    
    const blob = new Blob([JSON.stringify(result, null, 2)], {type:'application/json'});
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'super_table_config.json';
    a.click();
}

function onImport(file:any){
    const reader = new FileReader();
    reader.onload = (e)=>{
        try{
            const obj = JSON.parse(e.target?.result as string);
            Object.assign(config, obj);
            // 导入后更新列表
            updateTagMappingList();
            updateFieldMappingList();
            console.log('导入成功');
        }catch(err){
            console.error('导入失败', err);
        }
    };
    reader.readAsText(file.raw);
}
</script>

<style scoped lang="scss">
.el-upload-self+.el-button {
    margin-left: 12px;
}
.el-button+.el-upload-self {
    margin-left: 12px;
}
.el-upload-self {
    display: inline-block;
}
</style>