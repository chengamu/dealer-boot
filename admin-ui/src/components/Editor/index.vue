<template>
  <div :style="editorLocaleVars">
    <el-upload
        :action="uploadUrl"
        :before-upload="handleBeforeUpload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        class="editor-img-uploader"
        name="file"
        :show-file-list="false"
        :headers="headers"
        ref="uploadRef"
        v-if="type == 'url'"
    >
    </el-upload>
    <div class="editor">
      <quill-editor
          ref="quillEditorRef"
          v-model:content="content"
          contentType="html"
          @textChange="emit('update:modelValue', content)"
          :options="options"
          :style="styles"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { QuillEditor, Quill } from '@vueup/vue-quill';
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import { getToken } from "@/utils/auth";
import type { CSSProperties } from 'vue';
import type { UploadRawFile } from 'element-plus';
import { getMessage } from '@/locales'
import useLocaleStore from '@/stores/locale'

type UploadResponse = {
  code: number
  msg?: string
  data: {
    url: string
  }
}

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
  insertEmbed: (index: number, type: string, value: string) => void
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
    default: 5,
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
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + "/system/oss/upload");
const headers = ref({ Authorization: "Bearer " + getToken() });
const quillEditorRef = ref<QuillEditorExpose | null>(null);

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
function handleUploadSuccess(res: UploadResponse) {
  // 如果上传成功
  if (res.code == 200) {
    // 获取富文本实例
    const quill = toRaw(quillEditorRef.value)?.getQuill();
    if (!quill) return
    // 获取光标位置
    const length = quill.getSelection?.()?.index ?? quill.selection?.savedRange?.index ?? 0;
    // 插入图片，res为服务器返回的图片链接地址
    quill.insertEmbed(length, "image", res.data.url);
    // 调整光标到最后
    quill.setSelection(length + 1);
    proxy.$modal.closeLoading();
  } else {
    proxy.$modal.loading(res.msg || t('upload.imageUploadFailed'));
    proxy.$modal.closeLoading();
  }
}

// 图片上传前拦截
function handleBeforeUpload(file: UploadRawFile) {
  const type = ["image/jpeg", "image/jpg", "image/png", "image/svg"];
  const isJPG = type.includes(file.type);
  //检验文件格式
  if (!isJPG) {
    proxy.$modal.msgError(t('upload.invalidImageType').replace('{types}', 'JPG, JPEG, PNG, SVG'));
    return false;
  }
  // 校检文件大小
  if (props.fileSize) {
    const isLt = file.size / 1024 / 1024 < props.fileSize;
    if (!isLt) {
      proxy.$modal.msgError(t('upload.imageTooLarge').replace('{size}', String(props.fileSize)));
      return false;
    }
  }
  proxy.$modal.loading(t('upload.uploadingImage'));
  return true;
}

// 图片失败拦截
function handleUploadError() {
  proxy.$modal.msgError(t('upload.imageUploadFailed'));
}

</script>

<style>
.editor-img-uploader {
  display: none;
}
.editor, .ql-toolbar {
  white-space: pre-wrap !important;
  line-height: normal !important;
}
.quill-img {
  display: none;
}
.ql-snow .ql-tooltip[data-mode="link"]::before {
  content: var(--editor-link-prompt, "Enter link URL:");
}
.ql-snow .ql-tooltip.ql-editing a.ql-action::after {
  border-right: 0px;
  content: var(--editor-save, "Save");
  padding-right: 0px;
}

.ql-snow .ql-tooltip[data-mode="video"]::before {
  content: var(--editor-video-prompt, "Enter video URL:");
}

.ql-snow .ql-picker.ql-size .ql-picker-label::before,
.ql-snow .ql-picker.ql-size .ql-picker-item::before {
  content: "14px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="small"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="small"]::before {
  content: "10px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="large"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="large"]::before {
  content: "18px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="huge"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="huge"]::before {
  content: "32px";
}

.ql-snow .ql-picker.ql-header .ql-picker-label::before,
.ql-snow .ql-picker.ql-header .ql-picker-item::before {
  content: var(--editor-text, "Text");
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="1"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="1"]::before {
  content: var(--editor-heading-1, "Heading 1");
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="2"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="2"]::before {
  content: var(--editor-heading-2, "Heading 2");
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="3"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="3"]::before {
  content: var(--editor-heading-3, "Heading 3");
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="4"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="4"]::before {
  content: var(--editor-heading-4, "Heading 4");
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="5"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="5"]::before {
  content: var(--editor-heading-5, "Heading 5");
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="6"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="6"]::before {
  content: var(--editor-heading-6, "Heading 6");
}

.ql-snow .ql-picker.ql-font .ql-picker-label::before,
.ql-snow .ql-picker.ql-font .ql-picker-item::before {
  content: var(--editor-standard-font, "Standard font");
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="serif"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="serif"]::before {
  content: var(--editor-serif-font, "Serif font");
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="monospace"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="monospace"]::before {
  content: var(--editor-monospace-font, "Monospace font");
}
</style>
