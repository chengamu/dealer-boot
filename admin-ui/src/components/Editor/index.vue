<template>
  <div :style="editorLocaleVars">
    <el-upload
        :action="uploadUrl"
        :before-upload="handleBeforeUpload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        class="editor-img-uploader"
        name="file"
        accept=".jpg,.jpeg,.png"
        multiple
        :show-file-list="false"
        :http-request="uploadImage"
        ref="uploadRef"
        v-if="type == 'url'"
    >
    </el-upload>
    <div class="editor" @paste.capture="handleEditorPaste" @drop.capture="handleEditorDrop" @dragover.capture="handleEditorDragOver">
      <quill-editor
          ref="quillEditorRef"
          v-model:content="content"
          contentType="html"
          @textChange="handleTextChange"
          :options="options"
          :style="styles"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { QuillEditor, Quill } from '@vueup/vue-quill';
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import { onBeforeUnmount, type CSSProperties } from 'vue';
import type { UploadInstance, UploadRawFile, UploadRequestOptions } from 'element-plus';
import { getMessage } from '@/locales'
import useLocaleStore from '@/stores/locale'
import { OSS_UPLOAD_URL, uploadOssFile, type OssUploadResponse } from '@/api/system/ossUpload'
import { enqueueUploadFiles, getClipboardFiles, getDroppedFiles, selectUploadFiles, validateUploadFile, type UploadValidationIssue } from '@/composables/uploadIntake'
import { registerOssImageBlot } from './ossImageBlot'
import { usePendingOssImages } from './pendingOssImages'

registerOssImageBlot(Quill)

type ModalProxy = {
  $modal: {
    loading: (message: string) => void
    closeLoading: () => void
    msgError: (message: string) => void
  }
}

type QuillInstance = {
  selection?: {
    savedRange?: {
      index: number
    }
  }
  getSelection?: () => { index: number } | null
  insertEmbed: (index: number, type: string, value: unknown) => void
  setSelection: (index: number) => void
}

type QuillEditorExpose = {
  getQuill: () => QuillInstance
}

type QuillStatic = {
  format: (name: string, value: boolean) => void
}

const props = defineProps({
  /* 编辑器的内容 */
  modelValue: {
    type: String,
  },
  /* 高度 */
  height: {
    type: Number,
    default: null,
  },
  /* 最小高度 */
  minHeight: {
    type: Number,
    default: null,
  },
  /* 只读 */
  readOnly: {
    type: Boolean,
    default: false,
  },
  /* 上传文件大小限制(MB) */
  fileSize: {
    type: Number,
    default: 10,
  },
  /* 类型（base64格式、url格式） */
  type: {
    type: String,
    default: "url",
  }
});

const { proxy } = getCurrentInstance() as unknown as { proxy: ModalProxy };
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const emit = defineEmits<{
  (event: 'update:modelValue', value: string): void
}>();
// 上传的图片服务器地址
const uploadUrl = ref(OSS_UPLOAD_URL);
const uploadRef = ref<UploadInstance>();
const quillEditorRef = ref<QuillEditorExpose | null>(null);
const editorImageTypes = ['jpg', 'jpeg', 'png'];
const pendingImages = usePendingOssImages();

const options = computed(() => ({
  theme: "snow",
  bounds: document.body,
  debug: "warn",
  modules: {
    // 工具栏配置
    toolbar: {
      container: [
        ["bold", "italic", "underline", "strike"],       // 加粗 斜体 下划线 删除线
        ["blockquote", "code-block"],                    // 引用  代码块
        [{ list: "ordered" }, { list: "bullet"} ],       // 有序、无序列表
        [{ indent: "-1" }, { indent: "+1" }],            // 缩进
        [{ size: ["small", false, "large", "huge"] }],   // 字体大小
        [{ header: [1, 2, 3, 4, 5, 6, false] }],         // 标题
        [{ color: [] }, { background: [] }],             // 字体颜色、字体背景颜色
        [{ align: [] }],                                 // 对齐方式
        ["clean"],                                       // 清除文本格式
        ["link", "image", "video"]                       // 链接、图片、视频
      ],
      handlers: {
        image: function (value: boolean) {
          if (value) {
            // 调用element图片上传
            document.querySelector<HTMLElement>(".editor-img-uploader>.el-upload")?.click();
          } else {
            (Quill as unknown as QuillStatic).format("image", true);
          }
        },
      },
    }
  },
  placeholder: t('editor.placeholder'),
  readOnly: props.readOnly,
}));

const editorLocaleVars = computed<CSSProperties & Record<string, string>>(() => ({
  '--editor-link-prompt': JSON.stringify(t('editor.linkPrompt')),
  '--editor-save': JSON.stringify(t('editor.save')),
  '--editor-video-prompt': JSON.stringify(t('editor.videoPrompt')),
  '--editor-text': JSON.stringify(t('editor.text')),
  '--editor-heading-1': JSON.stringify(t('editor.heading1')),
  '--editor-heading-2': JSON.stringify(t('editor.heading2')),
  '--editor-heading-3': JSON.stringify(t('editor.heading3')),
  '--editor-heading-4': JSON.stringify(t('editor.heading4')),
  '--editor-heading-5': JSON.stringify(t('editor.heading5')),
  '--editor-heading-6': JSON.stringify(t('editor.heading6')),
  '--editor-standard-font': JSON.stringify(t('editor.standardFont')),
  '--editor-serif-font': JSON.stringify(t('editor.serifFont')),
  '--editor-monospace-font': JSON.stringify(t('editor.monospaceFont'))
}))

const styles = computed<CSSProperties>(() => {
  const style: CSSProperties = {};
  if (props.minHeight) {
    style.minHeight = `${props.minHeight}px`;
  }
  if (props.height) {
    style.height = `${props.height}px`;
  }
  return style;
});

const content = ref("");
watch(() => props.modelValue, (v) => {
  if (v !== content.value) {
    content.value = v === undefined ? "<p></p>" : v;
  }
}, { immediate: true });

// 图片上传成功返回图片地址
function uploadImage(options: UploadRequestOptions) {
  return uploadOssFile(options.file, options.filename);
}

function handleUploadSuccess(res: OssUploadResponse) {
  // 如果上传成功
  if (res.code == 200) {
    // 获取富文本实例
    const quill = toRaw(quillEditorRef.value)?.getQuill();
    if (!quill) return
    // 获取光标位置
    const length = quill.getSelection?.()?.index ?? quill.selection?.savedRange?.index ?? 0;
    // 插入图片，res为服务器返回的图片链接地址
    pendingImages.track(res.data.ossId);
    quill.insertEmbed(length, "image", { url: res.data.url, ossId: res.data.ossId });
    // 调整光标到最后
    quill.setSelection(length + 1);
    proxy.$modal.closeLoading();
  } else {
    proxy.$modal.msgError(res.msg || t('upload.imageUploadFailed'));
    proxy.$modal.closeLoading();
  }
}

function handleTextChange() {
  emit('update:modelValue', content.value);
  queueMicrotask(() => { void pendingImages.cleanupMissing(content.value) });
}

// 图片上传前拦截
function handleBeforeUpload(file: UploadRawFile) {
  const issue = validateUploadFile(file, { allowedExtensions: editorImageTypes, maxSizeMb: props.fileSize });
  if (issue) return showValidationIssue(issue);
  proxy.$modal.loading(t('upload.uploadingImage'));
  return true;
}

function handleEditorPaste(event: ClipboardEvent) {
  handleEditorFiles(getClipboardFiles(event), event);
}

function handleEditorDrop(event: DragEvent) {
  handleEditorFiles(getDroppedFiles(event), event);
}

function handleEditorDragOver(event: DragEvent) {
  if (Array.from(event.dataTransfer?.types || []).includes('Files')) event.preventDefault();
}

function handleEditorFiles(files: File[], event: ClipboardEvent | DragEvent) {
  if (!files.length) return;
  event.preventDefault();
  event.stopPropagation();
  const result = selectUploadFiles(files, { allowedExtensions: editorImageTypes, maxSizeMb: props.fileSize });
  if (result.issue) showValidationIssue(result.issue);
  enqueueUploadFiles(uploadRef.value, result.files);
}

function showValidationIssue(issue: UploadValidationIssue) {
  proxy.$modal.msgError(issue === 'size'
    ? t('upload.imageTooLarge').replace('{size}', String(props.fileSize))
    : t('upload.invalidImageType').replace('{types}', 'JPG, JPEG, PNG'));
  return false;
}

// 图片失败拦截
function handleUploadError() {
  proxy.$modal.msgError(t('upload.imageUploadFailed'));
  proxy.$modal.closeLoading();
}

onBeforeUnmount(() => { void pendingImages.cleanup() });
defineExpose({ cleanup: pendingImages.cleanup, commit: pendingImages.commit });

</script>

<style src="./editor.css"></style>
