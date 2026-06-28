<template>
  <div
    class="user-info-head"
    role="button"
    tabindex="0"
    :aria-label="t('user.uploadAvatar')"
    @click="editCropper"
    @keydown.enter.prevent="editCropper"
    @keydown.space.prevent="editCropper"
  >
    <img :src="options.img" :title="t('user.uploadAvatar')" class="img-circle img-lg" :alt="t('user.avatar')" />
    <AdminDialog
      v-model="open"
      :title="t('user.editAvatar')"
      width="800px"
      class="admin-media-dialog avatar-cropper-dialog"
      append-to-body
      @opened="modalOpened"
      @close="closeDialog"
    >
      <el-row class="avatar-cropper-dialog__workspace">
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <vue-cropper
            v-if="visible"
            ref="cropper"
            :img="options.img"
            :info="true"
            :autoCrop="options.autoCrop"
            :autoCropWidth="options.autoCropWidth"
            :autoCropHeight="options.autoCropHeight"
            :fixedBox="options.fixedBox"
            :outputType="options.outputType"
            @realTime="realTime"
          />
        </el-col>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <div class="avatar-upload-preview">
            <img v-if="options.previews.url" :src="options.previews.url" :style="options.previews.img" :alt="t('user.avatarPreview')" />
          </div>
        </el-col>
      </el-row>
      <template #footer>
        <AdminDialogFooter class="avatar-cropper-dialog__actions">
          <el-upload action="#" :http-request="requestUpload" :show-file-list="false" :before-upload="beforeUpload">
            <el-button>
              {{ t('common.selectPlaceholder') }}
              <el-icon class="el-icon--right"><Upload /></el-icon>
            </el-button>
          </el-upload>
          <el-button class="avatar-cropper-dialog__icon-button" icon="Plus" :aria-label="t('user.zoomInAvatar')" :title="t('user.zoomInAvatar')" @click.stop="changeScale(1)" />
          <el-button class="avatar-cropper-dialog__icon-button" icon="Minus" :aria-label="t('user.zoomOutAvatar')" :title="t('user.zoomOutAvatar')" @click.stop="changeScale(-1)" />
          <el-button class="avatar-cropper-dialog__icon-button" icon="RefreshLeft" :aria-label="t('user.rotateAvatarLeft')" :title="t('user.rotateAvatarLeft')" @click.stop="rotateLeft" />
          <el-button class="avatar-cropper-dialog__icon-button" icon="RefreshRight" :aria-label="t('user.rotateAvatarRight')" :title="t('user.rotateAvatarRight')" @click.stop="rotateRight" />
          <el-button type="primary" @click.stop="uploadImg">{{ t('common.submit') }}</el-button>
        </AdminDialogFooter>
      </template>
    </AdminDialog>
  </div>
</template>

<script setup lang="ts">
import 'vue-cropper/dist/index.css'
import { reactive, ref } from 'vue'
import { VueCropper } from 'vue-cropper'
import { ElMessage } from 'element-plus'
import { uploadAvatar } from '@/api/system/user'
import { useUserStore } from '@/stores/user'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

interface CropPreview {
  url?: string
  img?: Record<string, string>
}

const userStore = useUserStore()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll(`{${name}}`, String(value)), message)
}

const open = ref(false)
const visible = ref(false)
const cropper = ref<any>()
const options = reactive({
  img: userStore.avatar,
  autoCrop: true,
  autoCropWidth: 200,
  autoCropHeight: 200,
  fixedBox: true,
  outputType: 'png',
  filename: '',
  previews: {} as CropPreview
})

function editCropper() {
  open.value = true
}

function modalOpened() {
  visible.value = true
}

function requestUpload() {}

function rotateLeft() {
  cropper.value?.rotateLeft()
}

function rotateRight() {
  cropper.value?.rotateRight()
}

function changeScale(num: number) {
  cropper.value?.changeScale(num || 1)
}

function beforeUpload(file: File) {
  if (!file.type.includes('image/')) {
    ElMessage.error(t('user.avatarImageOnly'))
    return false
  }
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = () => {
    options.img = String(reader.result || '')
    options.filename = file.name
  }
  return false
}

function getAvatarFilename(blob: Blob) {
  const type = blob.type || 'image/png'
  const extensionMap: Record<string, string> = {
    'image/bmp': 'bmp',
    'image/gif': 'gif',
    'image/jpeg': 'jpg',
    'image/jpg': 'jpg',
    'image/png': 'png'
  }
  return `avatar.${extensionMap[type] || 'png'}`
}

function uploadImg() {
  cropper.value?.getCropBlob(async (data: Blob) => {
    const formData = new FormData()
    const file = new File([data], getAvatarFilename(data), { type: data.type || 'image/png' })
    formData.append('avatarfile', file, file.name)
    const response = await uploadAvatar(formData)
    open.value = false
    options.img = response.data.imgUrl
    userStore.avatar = options.img
    ElMessage.success(t('common.editSuccess'))
    visible.value = false
  })
}

function realTime(data: CropPreview) {
  options.previews = data
}

function closeDialog() {
  options.img = userStore.avatar
  visible.value = false
}
</script>

<style lang="scss" scoped>
.user-info-head {
  position: relative;
  display: inline-block;
  height: 120px;
  cursor: pointer;
}

.user-info-head:hover::after,
.user-info-head:focus-visible::after {
  content: "+";
  position: absolute;
  inset: 0;
  color: #eee;
  background: rgba(0, 0, 0, 0.5);
  font-size: 24px;
  line-height: 110px;
  border-radius: 50%;
}
</style>
