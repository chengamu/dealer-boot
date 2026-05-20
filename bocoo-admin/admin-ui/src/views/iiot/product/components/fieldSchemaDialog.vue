<template>
    <div class="dialog__body">
        <div class="body-grid">
            <!-- 左侧：树结构（上下级明显） -->
            <div class="tree-panel">
                <div class="tree-header">
                    <div>结构树</div>
                </div>
                <div class="tree">
                    <!-- 如果顶层是字段（非STRUCT）也能展示 -->
                    <ul class="tree-root">
                        <!-- 显示根节点及其子节点 -->
                        <tree-item
                            :node="model"
                            :active="currentNode===model"
                            :is-root="true"
                            @select="selectNode"
                            @addChild="addField"
                            @addPeer="addPeer"
                            @remove="removeNode"
                        />
                    </ul>
                </div>
            </div>
            <!-- 左侧：表单（页面风格贴近截图） -->
            <div class="form-panel">
                <!-- 顶部操作 -->
                <div class="row-actions">
                    <button class="btn" @click="newModel">新建结构</button>
                    <button class="btn" @click="openImport">导入JSON</button>
                    <button class="btn" @click="doExport">导出JSON</button>
                    <button class="btn" @click="addField(null)">➕ 添加顶级字段</button>
                </div>

                <!-- 当前字段编辑器 -->
                <div class="editor" v-if="currentNode">
                    <div class="form-row">
                        <label>Identifier</label>
                        <input v-model="currentNode.identifier" placeholder="英文字段标识" @input="refresh"/>
                    </div>
                    <div class="form-row">
                        <label>名称</label>
                        <input v-model="currentNode.name" placeholder="中文名称" @input="refresh"/>
                    </div>
                    <div class="form-row">
                        <label>数据类型</label>
                        <select v-model="currentNode.dataType" @change="onTypeChange(currentNode)">
                            <option v-for="t in TYPE_OPTIONS" :key="t" :value="t">{{ typeLabel(t) }}</option>
                        </select>
                    </div>

                    <!-- 单位：仅非 STRUCT/ENUM 时显示 -->
                    <div class="form-row" v-if="currentNode.dataType!=='STRUCT' && currentNode.dataType!=='ENUM'">
                        <label>单位</label>
                        <input v-model="currentNode.unit" placeholder="单位（如：°C、%RH、V）" @input="refresh"/>
                    </div>

                    <div class="form-row">
                        <label>描述</label>
                        <textarea v-model="currentNode.description" placeholder="字段用途/说明"
                                  @input="refresh"></textarea>
                    </div>

                    <!-- 枚举值编辑器 -->
                    <div v-if="currentNode.dataType === 'ENUM'" class="enum-editor">
                        <div class="sub-title">枚举值设置</div>
                        <div class="enum-items">
                            <div v-for="(value, key, index) in currentNode.specs" :key="index" class="enum-item">
                                <input v-model="editingEnumKeys[index]" :placeholder="'枚举键 ' + (index+1)" class="enum-key" @input="updateEnumKey(key, index)"/>
                                <input v-model="currentNode.specs[key]" :placeholder="'枚举值 ' + (index+1)" class="enum-value" @input="refresh"/>
                                <button class="link" @click="removeEnumItem(key)" style="color:#ef4444">删除</button>
                            </div>
                        </div>
                        <button class="btn add-enum-btn" @click="addEnumItem">＋ 添加枚举项</button>
                    </div>

                    <div class="hint" v-if="currentNode.dataType==='STRUCT'">STRUCT 可在右侧树上添加子字段</div>
                    <div class="hint" v-else-if="currentNode.dataType==='ENUM'">ENUM 可以添加枚举键值对</div>
                </div>
                <div class="editor empty" v-else>请选择右侧树中的字段进行编辑</div>

                <div class="split"></div>
                <div class="sub-title">实时 JSON 预览</div>
                <pre class="preview">{{ prettyJSON(outputModel) }}</pre>
            </div>
        </div>
    </div>

    <!-- 导入 JSON 弹窗 -->
    <div class="mask" v-if="importing">
        <div class="dialog small">
            <div class="dialog__header">
                <div class="dialog__title">导入 JSON</div>
                <button class="icon-btn" title="关闭" @click="importing=false">✕</button>
            </div>
            <div class="dialog__body">
        <textarea v-model="importText" rows="14" class="import-area"
                  placeholder='粘贴 JSON，如 {"dataType":"INTEGER","description":"..."} 或 {"dataType":"STRUCT","specs":[...]}'></textarea>
            </div>
            <div class="dialog__footer">
                <button class="btn" @click="importing=false">取消</button>
                <button class="btn primary" @click="doImport">导入</button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import {reactive, ref, computed, watch, onMounted} from 'vue';
import TreeItem from "@/views/iiot/product/components/treeItem.vue";

const props = defineProps<{
    json: String;
}>();

/** —— 类型选项 —— */
const TYPE_OPTIONS = [
    'STRUCT', 'ENUM', 'TEXT', 'INTEGER', 'LONG', 'DECIMAL', 'BOOLEAN', 'DATE', 'DATETIME', 'TIME'
];

/** —— 导入弹窗 —— */
const importing = ref(false);
const importText = ref('');

// 枚举编辑相关
const editingEnumKeys = ref<{[key: number]: string}>({});

function openImport() {
    importing.value = true;
}

/** —— 模型数据 —— */
type Dict = Record<string, any>;
type Node = {
    identifier?: string;
    name?: string;
    description?: string;
    dataType: string;
    unit?: string;
    specs?: any; // STRUCT: Node[]; ENUM: Dict
};

const model = reactive<Node>(makeRoot());
const currentNode = ref<Node | null>(null);

/** —— 工具与渲染 —— */
function makeRoot(): Node {
    return {dataType: 'STRUCT', description: '模型根（可为任意类型）', specs: []};
}

function clone<T>(v: T): T {
    return JSON.parse(JSON.stringify(v));
}

function ensureShape(node: Node) {
    if (node.dataType === 'STRUCT') {
        if (!Array.isArray(node.specs)) node.specs = [];
    } else if (node.dataType === 'ENUM') {
        if (!node.specs || typeof node.specs !== 'object' || Array.isArray(node.specs)) node.specs = {};
        // 初始化编辑中的枚举键
        editingEnumKeys.value = {};
        Object.keys(node.specs).forEach((key, index) => {
            editingEnumKeys.value[index] = key;
        });
    } else {
        delete node.specs;
    }
}

function typeLabel(t: string) {
    switch (t) {
        case 'STRUCT':
            return '结构 (STRUCT)';
        case 'ENUM':
            return '枚举 (ENUM)';
        case 'TEXT':
            return '文本 (TEXT)';
        case 'INTEGER':
            return '整数 (INTEGER)';
        case 'LONG':
            return '长整型 (LONG)';
        case 'DECIMAL':
            return '小数 (DECIMAL)';
        case 'BOOLEAN':
            return '布尔 (BOOLEAN)';
        case 'DATE':
            return '日期 (DATE)';
        case 'DATETIME':
            return '日期时间 (DATETIME)';
        case 'TIME':
            return '时间 (TIME)';
        default:
            return t || '未设类型';
    }
}

function onTypeChange(n: Node) {
    ensureShape(n);
    refresh();
}

function nodeKey(n: Node) {
    return (n.identifier || '') + '_' + (n.name || '') + '_' + (n.dataType || '');
}

function selectNode(n: Node) {
    currentNode.value = n;
    // 当选择一个ENUM节点时，初始化editingEnumKeys
    if (n.dataType === 'ENUM' && n.specs) {
        editingEnumKeys.value = {};
        Object.keys(n.specs).forEach((key, index) => {
            editingEnumKeys.value[index] = key;
        });
    }
}

// 枚举处理相关函数
function addEnumItem() {
    if (!currentNode.value || currentNode.value.dataType !== 'ENUM') return;
    
    const newKey = `enum_key_${Date.now()}`;
    const newValue = '枚举值';
    
    if (!currentNode.value.specs) {
        currentNode.value.specs = {};
    }
    
    // 添加新的枚举项
    currentNode.value.specs[newKey] = newValue;
    
    // 更新编辑中的键
    const index = Object.keys(currentNode.value.specs).length - 1;
    editingEnumKeys.value[index] = newKey;
    
    refresh();
}

function removeEnumItem(key: string) {
    if (!currentNode.value || currentNode.value.dataType !== 'ENUM' || !currentNode.value.specs) return;
    
    // 删除枚举项
    delete currentNode.value.specs[key];
    
    // 更新编辑中的键映射
    const keys = Object.keys(currentNode.value.specs);
    editingEnumKeys.value = {};
    keys.forEach((k, index) => {
        editingEnumKeys.value[index] = k;
    });
    
    refresh();
}

function updateEnumKey(oldKey: string, index: number) {
    if (!currentNode.value || currentNode.value.dataType !== 'ENUM' || !currentNode.value.specs) return;
    
    const newKey = editingEnumKeys.value[index] || oldKey;
    
    // 如果键名没有改变，则不执行任何操作
    if (newKey === oldKey) return;
    
    // 更新specs对象中的键
    const value = currentNode.value.specs[oldKey];
    delete currentNode.value.specs[oldKey];
    currentNode.value.specs[newKey] = value;
    
    refresh();
}

function addField(parent: Node | null) {
    const nf: Node = {identifier: '', name: '', description: '', dataType: 'TEXT', unit: ''};
    if (parent) {
        if (parent.dataType !== 'STRUCT') {
            toast('只能在 STRUCT 下新增子字段');
            return;
        }
        ensureShape(parent);
        (parent.specs as Node[]).push(nf);
    } else {
        if (Array.isArray(model.specs)) (model.specs as Node[]).push(nf);
        else Object.assign(model, makeRoot()), (model.specs as Node[]).push(nf);
    }
    currentNode.value = nf;
    refresh();
}

function findParent(cur: Node, target: Node): Node | null {
    if (!cur || !Array.isArray(cur.specs)) return null;
    for (const c of cur.specs as Node[]) {
        if (c === target) return cur;
        const deeper = findParent(c, target);
        if (deeper) return deeper;
    }
    return null;
}

function addPeer(n: Node) {
    const p = findParent(model, n);
    if (!p) {
        if (Array.isArray(model.specs)) {
            (model.specs as Node[]).push({identifier: '', name: '', dataType: 'TEXT', description: '', unit: ''});
            currentNode.value = (model.specs as Node[])[(model.specs as Node[]).length - 1];
        }
    } else {
        (p.specs as Node[]).push({identifier: '', name: '', dataType: 'TEXT', description: '', unit: ''});
        currentNode.value = (p.specs as Node[])[(p.specs as Node[]).length - 1];
    }
    refresh();
}

function removeNode(n: Node) {
    const p = findParent(model, n);
    if (!p && Array.isArray(model.specs)) {
        const i = (model.specs as Node[]).indexOf(n);
        if (i >= 0) (model.specs as Node[]).splice(i, 1);
    } else if (p) {
        const i = (p.specs as Node[]).indexOf(n);
        if (i >= 0) (p.specs as Node[]).splice(i, 1);
    } else if (model === n) {
        Object.assign(model, makeRoot());
    }
    if (currentNode.value === n) currentNode.value = null;
    refresh();
}

function newModel() {
    Object.assign(model, makeRoot());
    currentNode.value = null;
    refresh();
}

/** —— 导入/导出 —— */
function toOutput(n: Node): Node {
    const o = clone(n);
    if (o.dataType === 'STRUCT') o.specs = (o.specs || []).map((c: Node) => toOutput(c));
    else if (o.dataType !== 'ENUM') delete o.specs; // 非 ENUM/STRUCT 去掉 specs
    return o;
}

const outputModel = computed(() => toOutput(model));

function prettyJSON(v: any) {
    return JSON.stringify(v, null, 2);
}

function doExport() {
    const data = prettyJSON(outputModel.value);
    const blob = new Blob([data], {type: 'application/json'});
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'model.json';
    document.body.appendChild(a);
    a.click();
    a.remove();
}

function normalizeImport(obj: any): Node {
    const root = clone(obj);

    // 顶层是单字段对象（非 STRUCT 且无 specs 数组） -> 直接返回
    if (!Array.isArray(root.specs) && root.dataType && root.dataType !== 'STRUCT') {
        if (!('unit' in root)) root.unit = '';
        return root;
    }
    // specs 数组但无 dataType -> 设为 STRUCT
    if (Array.isArray(root.specs) && !root.dataType) root.dataType = 'STRUCT';

    function walk(n: any): Node {
        const x = clone(n);
        x.dataType = x.dataType || 'TEXT';
        if (!('unit' in x)) x.unit = '';
        if (x.dataType === 'STRUCT') {
            x.specs = Array.isArray(x.specs) ? x.specs.map((c: any) => walk(c)) : [];
        } else if (x.dataType === 'ENUM') {
            if (!x.specs || typeof x.specs !== 'object' || Array.isArray(x.specs)) x.specs = {};
        } else {
            delete x.specs;
        }
        return x;
    }

    return walk(root);
}

function doImport(str: string) {
    const text = str === 'init' ? '初始化' : '导入'
    try {
        const jsonStr = str === 'init' ? props.json : importText.value
        const obj = JSON.parse(jsonStr);
        const normalized = normalizeImport(obj);
        Object.keys(model).forEach(k => delete (model as any)[k]);
        Object.assign(model, normalized);
        currentNode.value = null;
        importing.value = false;
        toast(text + '成功');
    } catch (e: any) {
        toast(text + '失败：' + e.message);
    }
}

/** —— 交互细节 —— */
function refresh() { /* 触发依赖更新即可 */
    touched.value = Date.now();
}

const touched = ref(0);

/** —— 轻量提示 —— */
let toastTimer: any = null;

function toast(msg: string) {
    clearTimeout(toastTimer);
    const id = '__toast__';
    let t = document.getElementById(id) as HTMLDivElement | null;
    if (!t) {
        t = document.createElement('div');
        t.id = id;
        document.body.appendChild(t);
    }
    t.textContent = msg;
    t.style.cssText = 'position:fixed;left:50%;top:18px;transform:translateX(-50%);background:#0e1627;color:#dce3ff;border:1px solid #2b3f6b;padding:10px 14px;border-radius:10px;z-index:9999;box-shadow:0 10px 30px rgba(0,0,0,.35)';
    toastTimer = setTimeout(() => t?.remove(), 1600);
}

/** —— 初始数据（演示） —— */
Object.assign(model, normalizeImport({
    dataType: 'STRUCT',
    specs: [{identifier: 'name', name: '名称', dataType: 'TEXT', unit: ''}]
}));

onMounted(() => {
    doImport('init')
})

defineExpose( {
    outputModel,
})
</script>

<style scoped>

/* —— 弹窗骨架 —— */
.mask {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, .45);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
}

.dialog {
    width: min(1200px, 92vw);
    background: #fff;
    color: #111;
    border-radius: 8px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, .35);
    display: flex;
    flex-direction: column;
}

.dialog.small {
    width: min(840px, 92vw);
}

.dialog__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid #e5e7eb;
}

.dialog__title {
    font-weight: 600;
}

.icon-btn {
    border: none;
    background: transparent;
    cursor: pointer;
    font-size: 16px;
    color: #6b7280;
}

.dialog__body {
    padding: 14px 16px;
}

.dialog__footer {
    padding: 12px 16px;
    border-top: 1px solid #e5e7eb;
    display: flex;
    gap: 8px;
    justify-content: flex-end;
}

/* —— 布局 —— */
.body-grid {
    display: grid;
    grid-template-columns: 1.2fr 1fr;
    gap: 16px;
    min-height: 540px;
}

.form-panel {
    padding-right: 8px;
}

.tree-panel {
    background: #f8fafc;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    min-height: 540px;
}

.tree-header {
    padding: 10px 12px;
    border-bottom: 1px solid #e5e7eb;
    font-weight: 600;
    background: #fff;
    border-radius: 8px 8px 0 0;
}

/* —— 表单样式 —— */
.form-row {
    display: grid;
    grid-template-columns: 130px 1fr;
    gap: 10px;
    align-items: center;
    margin-bottom: 10px;
}

.form-row > label {
    color: #374151;
}

input, select, textarea {
    width: 100%;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    padding: 8px 10px;
    outline: none;
    background: #fff;
    color: #111;
}

textarea {
    min-height: 74px;
    resize: vertical;
}

.hint {
    font-size: 12px;
    color: #6b7280;
    margin-top: 4px;
}

.split {
    height: 1px;
    background: #e5e7eb;
    margin: 12px 0;
}

.sub-title {
    font-weight: 600;
    margin-bottom: 8px;
    color: #111827;
}

.preview {
    background: #0b1220;
    color: #e6e9ef;
    border-radius: 8px;
    padding: 10px 12px;
    max-height: 220px;
    overflow: auto;
    border: 1px solid #111827;
}

.row-actions {
    display: flex;
    gap: 8px;
    margin-bottom: 10px;
    flex-wrap: wrap;
}

.editor {
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    padding: 10px;
    background: #fff;
}

.editor.empty {
    color: #9ca3af;
    font-style: italic;
}

.seg {
    display: flex;
    gap: 8px;
}

.seg__btn {
    border: 1px solid #e5e7eb;
    background: #fff;
    padding: 6px 10px;
    border-radius: 6px;
    cursor: pointer;
    color: #374151;
}

.seg__btn.active {
    background: #2563eb;
    color: #fff;
    border-color: #2563eb;
}

/* —— 按钮 —— */
.btn {
    border: 1px solid #d1d5db;
    background: #fff;
    border-radius: 6px;
    padding: 8px 12px;
    cursor: pointer;
}

.btn.primary {
    background: #2563eb;
    color: #fff;
    border-color: #2563eb;
}

/* —— 树结构 —— */
.tree {
    padding: 12px;
    overflow: auto;
    flex: 1;
}

.tree-root {
    list-style: none;
    margin: 0;
    padding: 0 0 0 8px;
}

.tree ul {
    list-style: none;
    margin: 0;
    padding-left: 18px;
    position: relative;
}

.tree ul:before {
    content: "";
    position: absolute;
    left: 8px;
    top: 0;
    bottom: 0;
    width: 1px;
    background: #e5e7eb;
}

/* —— 导入框 —— */
.import-area {
    width: 100%;
    border: 1px solid #d1d5db;
    border-radius: 8px;
    padding: 10px;
}

/* —— 枚举编辑器样式 —— */
.enum-editor {
    margin-top: 15px;
    padding: 10px;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
    background-color: #f9fafb;
}

.enum-items {
    margin-bottom: 10px;
}

.enum-item {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;
    align-items: center;
}

.enum-key, .enum-value {
    flex: 1;
    border: 1px solid #d1d5db;
    border-radius: 4px;
    padding: 6px 8px;
    outline: none;
}

.enum-key {
    flex: 0.8;
}

.remove-btn {
    border: 1px solid #f87171;
    background: #fef2f2;
    color: #ef4444;
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
}

.add-enum-btn {
    background: #f3f4f6;
    border: 1px dashed #d1d5db;
    color: #4b5563;
    width: 100%;
}

.link {
    border: none;
    background: transparent;
    color: #2563eb;
    cursor: pointer;
    font-size: 12px;
    padding: 2px 6px;
}
</style>
