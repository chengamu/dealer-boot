<template>
  <div class="top-right-btn" :style="style">
    <el-row>
      <el-tooltip class="item" effect="dark" :content="showSearch ? t('common.hideSearch') : t('common.showSearch')" placement="top" v-if="search">
        <el-button circle icon="Search" :aria-label="showSearch ? t('common.hideSearch') : t('common.showSearch')" :title="showSearch ? t('common.hideSearch') : t('common.showSearch')" @click="toggleSearch()" />
      </el-tooltip>
      <el-tooltip class="item" effect="dark" :content="t('common.refresh')" placement="top">
        <el-button circle icon="Refresh" :aria-label="t('common.refresh')" :title="t('common.refresh')" @click="refresh()" />
      </el-tooltip>
      <el-tooltip class="item" effect="dark" :content="t('common.columnVisibility')" placement="top" v-if="columns">
        <el-button circle icon="Menu" :aria-label="t('common.columnVisibility')" :title="t('common.columnVisibility')" @click="showColumn()" />
      </el-tooltip>
    </el-row>
    <el-dialog :title="title" v-model="open" append-to-body>
      <el-transfer
        :titles="[t('common.show'), t('common.hide')]"
        v-model="value"
        :data="columns"
        @change="dataChange"
      ></el-transfer>
    </el-dialog>
  </div>
</template>

<script setup>
import { getMessage } from '@/locales'
import useLocaleStore from '@/store/modules/locale'

const props = defineProps({
  showSearch: {
    type: Boolean,
    default: true,
  },
  columns: {
    type: Array,
  },
  search: {
    type: Boolean,
    default: true,
  },
  gutter: {
    type: Number,
    default: 10,
  },
})

const emits = defineEmits(['update:showSearch', 'queryTable']);
const localeStore = useLocaleStore();
const t = (key) => getMessage(key, localeStore.language);

// 显隐数据
const value = ref([]);
// 弹出层标题
const title = ref(t("common.columnVisibility"));
// 是否显示弹出层
const open = ref(false);

const style = computed(() => {
  const ret = {};
  if (props.gutter) {
    ret.marginRight = `${props.gutter / 2}px`;
  }
  return ret;
});

// 搜索
function toggleSearch() {
  emits("update:showSearch", !props.showSearch);
}

// 刷新
function refresh() {
  emits("queryTable");
}

// 右侧列表元素变化
function dataChange(data) {
  for (let item in props.columns) {
    const key = props.columns[item].key;
    props.columns[item].visible = !data.includes(key);
  }
}

// 打开显隐列dialog
function showColumn() {
  open.value = true;
}

// 显隐列初始默认隐藏列
for (let item in props.columns) {
  if (props.columns[item].visible === false) {
    value.value.push(parseInt(item));
  }
}
</script>

<style lang='scss' scoped>
:deep(.el-transfer__button) {
  border-radius: 50%;
  display: block;
  margin-left: 0px;
}
:deep(.el-transfer__button:first-child) {
  margin-bottom: 10px;
}

.my-el-transfer {
  text-align: center;
}
</style>
