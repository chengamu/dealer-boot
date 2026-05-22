<template>
  <div class="upload-file">
    <el-upload
      multiple
      :action="uploadFileUrl"
      :before-upload="handleBeforeUpload"
      :file-list="fileList"
      :limit="limit"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      :on-success="handleUploadSuccess"
      :show-file-list="false"
      :headers="headers"
      class="upload-file-uploader"
      ref="fileUpload"
    >
      <!-- 上传按钮 -->
      <el-button type="primary">{{ t('upload.selectFile') }}</el-button>
    </el-upload>
    <!-- 上传提示 -->
    <div class="el-upload__tip" v-if="showTip">
      {{ t('upload.tipPrefix') }}
      <template v-if="fileSize"> {{ t('upload.maxSize') }} <b style="color: #f56c6c">{{ fileSize }}MB</b> </template>
      <span v-if="fileSize && fileType">&nbsp;</span>
      <template v-if="fileType"> {{ t('upload.allowedTypes') }} <b style="color: #f56c6c">{{ fileType.join("/") }}</b> </template>
      {{ t('upload.fileSuffix') }}
    </div>
    <!-- 文件列表 -->
    <transition-group class="upload-file-list el-upload-list el-upload-list--text" name="el-fade-in-linear" tag="ul">
      <li :key="file.uid" class="el-upload-list__item ele-upload-list__item-content" v-for="(file, index) in fileList">
        <el-link :href="`${file.url}`" :underline="false" target="_blank">
          <span class="el-icon-document"> {{ getFileName(file.name) }} </span>
        </el-link>
        <div class="ele-upload-list__item-content-action">
          <el-link :underline="false" @click="handleDelete(index)" type="danger">{{ t('common.delete') }}</el-link>
        </div>
      </li>
    </transition-group>
  </div>
</template>

<script setup lang="ts">
import { getToken } from "@/utils/auth";
import { listByIds, delOss } from "@/api/system/oss";
import { getMessage } from "@/locales";
import { useLocaleStore } from "@/stores/locale";
import type { PropType } from "vue";
import type { UploadFile, UploadInstance, UploadRawFile, UploadUserFile } from "element-plus";

type UploadModelValue = string | Record<string, unknown> | unknown[];

type UploadItem = UploadUserFile & {
  ossId?: number | string
  originalName?: string
  url?: string
}

type UploadResponse = {
  code: number
  msg?: string
  data: {
    fileName: string
    url: string
    ossId: number | string
  }
}

type FileUploadProxy = {
  $modal: {
    loading: (message: string) => void
    closeLoading: () => void
    msgError: (message: string) => void
  }
  $refs: {
    fileUpload?: UploadInstance
  }
}

const localeStore = useLocaleStore();
const t = (key: string, params?: Record<string, unknown>) => {
  const message = getMessage(key, localeStore.language);
  if (!params) return message;
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message);
};

const props = defineProps({
  modelValue: [String, Object, Array] as PropType<UploadModelValue>,
  // 数量限制
  limit: {
    type: Number,
    default: 5,
  },
  // 大小限制(MB)
  fileSize: {
    type: Number,
    default: 5,
  },
  // 文件类型, 例如['png', 'jpg', 'jpeg']
  fileType: {
    type: Array as PropType<string[]>,
    default: () => ["doc", "xls", "ppt", "txt", "pdf"],
  },
  // 是否显示提示
  isShowTip: {
    type: Boolean,
    default: true
  }
});

const { proxy } = getCurrentInstance() as unknown as { proxy: FileUploadProxy };
const emit = defineEmits<{
  (event: "update:modelValue", value: string): void
}>();
const number = ref(0);
const uploadList = ref<UploadItem[]>([]);
const baseUrl = import.meta.env.VITE_APP_BASE_API;
const uploadFileUrl = ref(baseUrl + "/system/oss/upload"); // 上传文件服务器地址
const headers = ref({ Authorization: "Bearer " + getToken() });
const fileList = ref<UploadItem[]>([]);
const showTip = computed(
  () => props.isShowTip && (props.fileType || props.fileSize)
);

watch(() => props.modelValue, async val => {
  if (val) {
    let temp = 1;
    // 首先将值转为数组
    let list: UploadItem[] = [];
    if (Array.isArray(val)) {
      list = val as UploadItem[];
    } else if (typeof val === 'object') {
      list = [val as UploadItem];
    } else {
      await listByIds(val).then(res => {
        list = res.data.map(oss => {
          return { name: oss.originalName || oss.fileName || '', url: oss.url, ossId: oss.ossId };
        });
      })
    }
    // 然后将数组转为对象数组
    fileList.value = list.map(item => {
      item = {name: item.name, url: item.url, ossId: item.ossId};
      item.uid = item.uid || new Date().getTime() + temp++;
      return item;
    });
  } else {
    fileList.value = [];
    return [];
  }
},{ deep: true, immediate: true });

// 上传前校检格式和大小
function handleBeforeUpload(file: UploadRawFile) {
  // 校检文件类型
  if (props.fileType.length) {
    const fileName = file.name.split('.');
    const fileExt = fileName[fileName.length - 1];
    const isTypeOk = props.fileType.indexOf(fileExt) >= 0;
    if (!isTypeOk) {
      proxy.$modal.msgError(t('upload.invalidFileType', { types: props.fileType.join("/") }));
      return false;
    }
  }
  // 校检文件大小
  if (props.fileSize) {
    const isLt = file.size / 1024 / 1024 < props.fileSize;
    if (!isLt) {
      proxy.$modal.msgError(t('upload.fileTooLarge', { size: props.fileSize }));
      return false;
    }
  }
  proxy.$modal.loading(t('upload.uploadingFile'));
  number.value++;
  return true;
}

// 文件个数超出
function handleExceed() {
  proxy.$modal.msgError(t('upload.limitExceeded', { limit: props.limit }));
}

// 上传失败
function handleUploadError() {
  proxy.$modal.msgError(t('upload.fileUploadFailed'));
}

// 上传成功回调
function handleUploadSuccess(res: UploadResponse, file: UploadFile) {
  if (res.code === 200) {
    uploadList.value.push({ name: res.data.fileName, url: res.data.url, ossId: res.data.ossId });
    uploadedSuccessfully();
  } else {
    number.value--;
    proxy.$modal.closeLoading();
    proxy.$modal.msgError(res.msg || t('upload.fileUploadFailed'));
    proxy.$refs.fileUpload?.handleRemove(file);
    uploadedSuccessfully();
  }
}

// 删除文件
function handleDelete(index: number) {
  const ossId = fileList.value[index].ossId;
  if (ossId !== undefined) delOss(ossId);
  fileList.value.splice(index, 1);
  emit("update:modelValue", listToString(fileList.value));
}

// 上传结束处理
function uploadedSuccessfully() {
  if (number.value > 0 && uploadList.value.length === number.value) {
    fileList.value = fileList.value.filter(f => f.url !== undefined).concat(uploadList.value);
    uploadList.value = [];
    number.value = 0;
    emit("update:modelValue", listToString(fileList.value));
    proxy.$modal.closeLoading();
  }
}

// 获取文件名称
function getFileName(name = '') {
  // 如果是url那么取最后的名字 如果不是直接返回
  if (name.lastIndexOf("/") > -1) {
    return name.slice(name.lastIndexOf("/") + 1);
  } else {
    return name;
  }
}

// 对象转成指定字符串分隔
function listToString(list: UploadItem[], separator = ",") {
  let strs = "";
  for (let i in list) {
    if(list[i].ossId) {
      strs += list[i].ossId + separator;
    }
  }
  return strs != "" ? strs.substr(0, strs.length - 1) : "";
}
</script>

<style scoped lang="scss">
.upload-file-uploader {
  margin-bottom: 5px;
}
.upload-file-list .el-upload-list__item {
  border: 1px solid #e4e7ed;
  line-height: 2;
  margin-bottom: 10px;
  position: relative;
}
.upload-file-list .ele-upload-list__item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: inherit;
}
.ele-upload-list__item-content-action .el-link {
  margin-right: 10px;
}
</style>
