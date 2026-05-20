<template>
  <div class="product-image-upload">
    <!-- 上传区域 -->
    <el-upload
      v-if="!hasImage"
      action="#"
      list-type="picture-card"
      :http-request="requestUpload"
      :before-upload="beforeUpload"
      :limit="1"
      :on-exceed="handleExceed"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>
    
    <!-- 已上传图片展示区域 -->
    <div v-else class="uploaded-image-container">
      <div class="image-preview">
        <img class="image-thumbnail" :src="modelValue" alt="产品图片" />
        <div class="image-actions">
          <span class="action-button preview" @click="handlePreview">
            <el-icon><zoom-in /></el-icon>
          </span>
          <span class="action-button delete" @click="handleRemove">
            <el-icon><Delete /></el-icon>
          </span>
        </div>
      </div>
    </div>
    
    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="800px" append-to-body>
      <el-image
        :src="modelValue"
        :preview-src-list="[modelValue]"
        fit="contain"
        style="width: 100%; height: 60vh"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { uploadLogo } from "@/api/iiot/product";
import { ElMessage } from 'element-plus';

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['update:modelValue']);

// 预览对话框可见性
const previewVisible = ref(false);

// 计算属性：是否有图片
const hasImage = computed(() => {
  return props.modelValue && props.modelValue.length > 0;
});

// 上传预处理
function beforeUpload(file) {
  const isImage = file.type.indexOf("image/") !== -1;
  const isLt4M = file.size / 1024 / 1024 < 4;
  
  if (!isImage) {
    ElMessage.error("文件格式错误，请上传图片类型,如：JPG，PNG后缀的文件。");
    return false;
  }
  if (!isLt4M) {
    ElMessage.error("上传图片大小不能超过 4MB!");
    return false;
  }
  
      // 如果文件超过1M，进行压缩处理
  if (file.size > 1024 * 1024) {
    return compressImage(file);
  }
  
  return true;
}

// 超出限制处理
function handleExceed() {
  ElMessage.warning("只能上传一张图片！");
}

// 覆盖默认上传行为
function requestUpload(options) {
  let formData = new FormData();
  formData.append("avatarfile", options.file);
  uploadLogo(formData).then(response => {
    emit('update:modelValue', response.data.imgUrl);
    options.onSuccess(response);
  }).catch(error => {
    ElMessage.error("图片上传失败");
    options.onError(error);
  });
}

// 图片预览处理
function handlePreview() {
  previewVisible.value = true;
}

// 删除图片
function handleRemove() {
  emit('update:modelValue', '');
}

// 压缩图片
function compressImage(file) {
  return new Promise((resolve) => {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const img = new Image();
    
    img.src = URL.createObjectURL(file);
    img.onload = function() {
      // 计算压缩比例
      let quality = 0.9;
      let maxWidth = img.width;
      let maxHeight = img.height;
      
      // 如果图片尺寸很大，先等比缩放
      if (maxWidth > 1920 || maxHeight > 1080) {
        const ratio = Math.min(1920 / maxWidth, 1080 / maxHeight);
        maxWidth = Math.floor(maxWidth * ratio);
        maxHeight = Math.floor(maxHeight * ratio);
      }
      
      canvas.width = maxWidth;
      canvas.height = maxHeight;
      
      // 绘制图片
      ctx.drawImage(img, 0, 0, maxWidth, maxHeight);
      
      // 尝试压缩到目标大小以下
      let compressedFile = null;
      const targetSize = 900 * 1024; // 900KB
      
      const compress = () => {
        canvas.toBlob((blob) => {
          if (!blob) {
            resolve(true); // 如果压缩失败，直接上传原图
            return;
          }
          
          if (blob.size <= targetSize || quality <= 0.1) {
            // 压缩后的文件
            compressedFile = new File([blob], file.name, {
              type: file.type,
              lastModified: Date.now()
            });
            
            // 创建新的File对象替换原文件
            const newFile = compressedFile;
            resolve(newFile);
          } else {
            // 如果压缩后仍然太大，继续降低质量压缩
            quality -= 0.1;
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            ctx.drawImage(img, 0, 0, maxWidth, maxHeight);
            compress();
          }
        }, file.type, quality);
      };
      
      compress();
    };
    
    img.onerror = function() {
      // 图片加载失败，直接上传原图
      resolve(true);
    };
  });
}
</script>

<style scoped lang="scss">
.product-image-upload {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    
    &:hover {
      border-color: var(--el-color-primary);
    }
  }
  
  .uploaded-image-container {
    display: inline-block;
  }
  
  .image-preview {
    position: relative;
    width: 148px;
    height: 148px;
    border: 1px solid #c0ccda;
    border-radius: 6px;
    overflow: hidden;
    
    .image-thumbnail {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .image-actions {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.5);
      opacity: 0;
      transition: opacity 0.3s;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &:hover {
        opacity: 1;
      }
      
      .action-button {
        display: inline-block;
        width: 32px;
        height: 32px;
        border-radius: 50%;
        cursor: pointer;
        text-align: center;
        line-height: 32px;
        margin: 0 8px;
        color: #ffffff;
          font-size: 28px;
        
        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }
}
</style>