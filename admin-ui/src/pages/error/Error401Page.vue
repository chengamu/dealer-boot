<template>
  <div class="errPage-container">
    <el-button icon="ArrowLeft" class="pan-back-btn" @click="back">{{ t('common.back') }}</el-button>
    <el-row>
      <el-col :span="12">
        <h1 class="text-jumbo text-ginormous">{{ t('error.unauthorizedTitle') }}</h1>
        <h2>{{ t('error.unauthorizedHeadline') }}</h2>
        <h6>{{ t('error.unauthorizedInfo') }}</h6>
        <ul class="list-unstyled">
          <li class="link-type">
            <router-link to="/">{{ t('error.goHome') }}</router-link>
          </li>
        </ul>
      </el-col>
      <el-col :span="12">
        <img :src="errGif" width="313" height="428" :alt="t('error.unauthorizedAlt')" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="Error401Page">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import errImage from '@/assets/401_images/401.gif'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const errGif = ref(`${errImage}?${Date.now()}`)

function back() {
  if (route.query.noGoBack) {
    router.push({ path: '/' })
  } else {
    router.go(-1)
  }
}
</script>

<style lang="scss" scoped>
.errPage-container {
  width: 800px;
  max-width: 100%;
  margin: 100px auto;
  .pan-back-btn {
    background: #008489;
    color: #fff;
    border: none !important;
  }
  .text-jumbo {
    font-size: 60px;
    font-weight: 700;
    color: #484848;
  }
  .list-unstyled {
    font-size: 14px;
    li {
      padding-bottom: 5px;
    }
    a {
      color: #008489;
      text-decoration: none;
      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style>
