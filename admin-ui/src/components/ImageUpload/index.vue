<template>
  <div class="component-upload-image">
    <el-upload
      multiple
      :action="uploadImgUrl"
      list-type="picture-card"
      :on-success="handleUploadSuccess"
      :before-upload="handleBeforeUpload"
      :limit="limit"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      ref="imageUpload"
      :before-remove="handleDelete"
      :show-file-list="true"
      :http-request="uploadImage"
      :file-list="fileList"
      :on-preview="handlePictureCardPreview"
      :class="{ hide: fileList.length >= limit }"
    >
      <el-icon class="avatar-uploader-icon"><plus /></el-icon>
    </el-upload>
    <!-- 上传提示 -->
    <div class="el-upload__tip" v-if="showTip">
      {{ t('upload.tipPrefix') }}
      <template v-if="fileSize">
        {{ t('upload.maxSize') }} <b style="color: #f56c6c">{{ fileSize }}MB</b>
      </template>
      <span v-if="fileSize && fileType">&nbsp;</span>
      <template v-if="fileType">
        {{ t('upload.allowedTypes') }} <b style="color: #f56c6c">{{ fileType.join("/") }}</b>
      </template>
      {{ t('upload.fileSuffix') }}
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="t('common.preview')"
      width="800px"
      class="image-upload-preview-dialog"
      append-to-body
    >
      <img
        :src="dialogImageUrl"
        class="image-upload-preview-dialog__image"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { listByIds, delOss } from "@/api/system/oss";
import { OSS_UPLOAD_URL, uploadOssFile, type OssUploadResponse } from "@/api/system/ossUpload";
import { getMessage } from "@/locales";
import { useLocaleStore } from "@/stores/locale";
import { runUiAction } from "@/utils/action";
import type { PropType } from "vue";
import type { UploadFile, UploadInstance, UploadRawFile, UploadRequestOptions, UploadUserFile } from "element-plus";

type UploadModelValue = string | Record<string, unknown> | unknown[];

type UploadItem = UploadUserFile & {
  ossId?: number | string
  url?: string
}

type ImageUploadProxy = {
  $modal: {
    loading: (message: string) => void
    closeLoading: () => void
    msgError: (message: string) => void
  }
  $refs: {
    imageUpload?: UploadInstance
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
  // 图片数量限制
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
    default: () => ["png", "jpg", "jpeg"],
  },
  // 是否显示提示
  isShowTip: {
    type: Boolean,
    default: true
  },
});

const { proxy } = getCurrentInstance() as unknown as { proxy: ImageUploadProxy };
const emit = defineEmits<{
  (event: "update:modelValue", value: string): void
}>();
const number = ref(0);
const uploadList = ref<UploadItem[]>([]);
const dialogImageUrl = ref("");
const dialogVisible = ref(false);
const uploadImgUrl = ref(OSS_UPLOAD_URL); // 上传的图片服务器地址
const fileList = ref<UploadItem[]>([]);
const showTip = computed(
  () => props.isShowTip && (props.fileType || props.fileSize)
);

watch(() => props.modelValue, async val => {
  if (val) {
    // 首先将值转为数组
    let list: UploadItem[] = [];
    if (Array.isArray(val)) {
      list = val as UploadItem[];
    } else if (typeof val === 'object') {
      list = [val as UploadItem];
    } else {
      const res = await runUiAction(() => listByIds(val));
      if (!res) return;
      {
        list = res.data as UploadItem[];
      }
    }
    // 然后将数组转为对象数组
    fileList.value = list.map(item => {
      // 字符串回显处理 如果此处存的是url可直接回显 如果存的是id需要调用接口查出来
      if (typeof item === "string") {
        item = { name: item, url: item };
      } else {
        // 此处name使用ossId 防止删除出现重名
        item = { name: String(item.ossId || ''), url: item.url, ossId: item.ossId };
      }
      return item;
    });
  } else {
    fileList.value = [];
    return [];
  }
},{ deep: true, immediate: true });

// 上传前loading加载
function handleBeforeUpload(file: UploadRawFile) {
  let isImg = false;
  if (props.fileType.length) {
    let fileExtension = "";
    if (file.name.lastIndexOf(".") > -1) {
      fileExtension = file.name.slice(file.name.lastIndexOf(".") + 1);
    }
    isImg = props.fileType.some((type) => {
      if (file.type.indexOf(type) > -1) return true;
      if (fileExtension && fileExtension.indexOf(type) > -1) return true;
      return false;
    });
  } else {
    isImg = file.type.indexOf("image") > -1;
  }
  if (!isImg) {
    proxy.$modal.msgError(
      t('upload.invalidImageType', { types: props.fileType.join("/") })
    );
    return false;
  }
  if (props.fileSize) {
    const isLt = file.size / 1024 / 1024 < props.fileSize;
    if (!isLt) {
      proxy.$modal.msgError(t('upload.imageTooLarge', { size: props.fileSize }));
      return false;
    }
  }
  proxy.$modal.loading(t('upload.uploadingImage'));
  number.value++;
}

// 文件个数超出
function uploadImage(options: UploadRequestOptions) {
  return uploadOssFile(options.file, options.filename);
}

function handleExceed() {
  proxy.$modal.msgError(t('upload.limitExceeded', { limit: props.limit }));
}

// 上传成功回调
function handleUploadSuccess(res: OssUploadResponse, file: UploadFile) {
  if (res.code === 200) {
    uploadList.value.push({ name: res.data.fileName, url: res.data.url, ossId: res.data.ossId });
    uploadedSuccessfully();
  } else {
    number.value--;
    proxy.$modal.closeLoading();
    proxy.$modal.msgError(res.msg || t('upload.imageUploadFailed'));
    proxy.$refs.imageUpload?.handleRemove(file);
    uploadedSuccessfully();
  }
}

// 删除图片
async function handleDelete(file: UploadFile) {
  const findex = fileList.value.map(f => f.name).indexOf(file.name);
  if (findex > -1 && uploadList.value.length === number.value) {
    const ossId = fileList.value[findex].ossId;
    if (ossId !== undefined) await runUiAction(() => delOss(ossId));
    fileList.value.splice(findex, 1);
    emit("update:modelValue", listToString(fileList.value));
    return false;
  }
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

// 上传失败
function handleUploadError() {
  proxy.$modal.msgError(t('upload.imageUploadFailed'));
  proxy.$modal.closeLoading();
}

// 预览
function handlePictureCardPreview(file: UploadFile) {
  dialogImageUrl.value = file.url || "";
  dialogVisible.value = true;
}

// 对象转成指定字符串分隔
function listToString(list: UploadItem[], separator = ",") {
  let strs = "";
  for (let i in list) {
    if(undefined !== list[i].ossId && (list[i].url || '').indexOf("blob:") !== 0) {
      strs += list[i].ossId + separator;
    }
  }
  return strs != "" ? strs.substr(0, strs.length - 1) : "";
}
</script>

<style scoped lang="scss">
// .el-upload--picture-card 控制加号部分
:deep(.hide .el-upload--picture-card) {
    display: none;
}
</style>
