<!-- 小型树节点组件（内联，便于拷贝） -->
<script setup lang="ts" name="TreeItem">
import {computed} from 'vue';

const props = defineProps({
  node: {type: Object, required: true},
  active: {type: Boolean, default: false},
  isRoot: {type: Boolean, default: false}
});

const emit = defineEmits(['select', 'addChild', 'addPeer', 'remove']);

const isStruct = computed(() => props.node.dataType === 'STRUCT');
const isEnum = computed(() => props.node.dataType === 'ENUM');

const label = computed(() => {
  switch (props.node.dataType) {
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
      return props.node.dataType || '未设类型';
  }
});

const tagClass = computed(() => isStruct.value ? 'tag struct' : (isEnum.value ? 'tag enum' : 'tag'));

const handleAddPeer = (e) => {
  e.stopPropagation();
  emit('addPeer', props.node);
};

const handleRemove = (e) => {
  e.stopPropagation();
  emit('remove', props.node);
};

const handleAddChild = (e) => {
  e.stopPropagation();
  emit('addChild', props.node);
};

const handleSelect = () => {
  emit('select', props.node);
};

const hasChildren = computed(() => {
  return isStruct.value && Array.isArray(props.node.specs) && props.node.specs.length;
});
</script>

<template>
  <li class="tree-li">
    <div
      :class="['tree-row', active && 'active']"
      @click="handleSelect"
    >
      <span :class="tagClass">{{ label }}</span>
      <span class="node-text">{{ (node.identifier || '(未命名)') + (node.name ? ' · ' + node.name : '') }}</span>
      <div class="ops">
          <button v-if="isStruct" class="link" @click="handleAddChild">子字段</button>
        <button v-if="!isRoot" class="link" @click="handleAddPeer">同级</button>
        <button class="link" @click="handleRemove" style="color:#ef4444">删除</button>
      </div>
    </div>
    <ul v-if="hasChildren">
      <li v-for="(child, i) in node.specs" :key="i" class="tree-li">
        <TreeItem
          :node="child"
          :active="false"
          @select="(n) => $emit('select', n)"
          @add-child="(n) => $emit('addChild', n)"
          @add-peer="(n) => $emit('addPeer', n)"
          @remove="(n) => $emit('remove', n)"
        />
      </li>
    </ul>
  </li>
</template>

<style scoped lang="scss">
.tree-li {
  position: relative;
  margin: 2px 0;
}

.tree-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
}

.tree-row:hover {
  background: #eef2ff;
}

.tree-row.active {
  background: #e0e7ff;
  border: 1px solid #c7d2fe;
}

.tree-row:before {
  content: "";
  position: absolute;
  left: -10px;
  top: 14px;
  width: 10px;
  height: 1px;
  background: #e5e7eb;
}

.tag {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #fff;
  color: #374151;
}

.tag.struct {
  color: #92400e;
  background: #fff7ed;
  border-color: #fed7aa;
}

.tag.enum {
  color: #065f46;
  background: #ecfdf5;
  border-color: #a7f3d0;
}

.node-text {
  color: #111827;
}

.ops {
  margin-left: auto;
  display: flex;
  gap: 6px;
}

.link {
  border: none;
  background: transparent;
  color: #2563eb;
  cursor: pointer;
  font-size: 12px;
  padding: 2px 6px;
}


ul {
    list-style: none;
    margin: 0;
    padding-left: 18px;
    position: relative;
}

ul:before {
    content: "";
    position: absolute;
    left: 8px;
    top: 0;
    bottom: 0;
    width: 1px;
    background: #e5e7eb;
}
</style>