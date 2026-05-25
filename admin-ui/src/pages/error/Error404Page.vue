<template>
  <div class="wscn-http404-container">
    <div class="wscn-http404">
      <div class="pic-404">
        <img class="pic-404__parent" src="@/assets/404_images/404.png" alt="404" />
        <img class="pic-404__child left" src="@/assets/404_images/404_cloud.png" alt="404" />
        <img class="pic-404__child mid" src="@/assets/404_images/404_cloud.png" alt="404" />
        <img class="pic-404__child right" src="@/assets/404_images/404_cloud.png" alt="404" />
      </div>
      <div class="bullshit">
        <div class="bullshit__oops">{{ t('error.notFoundTitle') }}</div>
        <div class="bullshit__headline">{{ t('error.notFoundHeadline') }}</div>
        <div class="bullshit__info">{{ t('error.notFoundInfo') }}</div>
        <div class="bullshit__bilingual">
          <strong>{{ altT('error.notFoundHeadline') }}</strong>
          <span>{{ altT('error.notFoundInfo') }}</span>
        </div>
        <div class="bullshit__actions">
          <router-link to="/index" class="bullshit__return-home">{{ t('error.goHome') }}</router-link>
          <button
            v-for="item in languages"
            :key="item.value"
            type="button"
            :class="{ active: localeStore.language === item.value }"
            @click="localeStore.setLanguage(item.value)"
          >
            {{ item.label }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="Error404Page">
import { computed } from 'vue'
import { localeOptions, type AppLocale } from '@/i18n'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const altT = (key: string) => getMessage(key, localeStore.language === 'en_US' ? 'zh_CN' : 'en_US')
const languages = computed<Array<{ value: AppLocale; label: string }>>(() => localeOptions.map(item => ({
  value: item.value,
  label: getMessage(item.labelKey, localeStore.language)
})))
</script>

<style lang="scss" scoped>
.wscn-http404-container {
  transform: translate(-50%, -50%);
  position: absolute;
  top: 40%;
  left: 50%;
}
.wscn-http404 {
  position: relative;
  width: 1200px;
  padding: 0 50px;
  overflow: hidden;
  .pic-404 {
    position: relative;
    float: left;
    width: 600px;
    overflow: hidden;
    &__parent {
      width: 100%;
    }
    &__child {
      position: absolute;
      &.left {
        width: 80px;
        top: 17px;
        left: 220px;
        opacity: 0;
        animation: cloudLeft 2s linear 1s forwards;
      }
      &.mid {
        width: 46px;
        top: 10px;
        left: 420px;
        opacity: 0;
        animation: cloudMid 2s linear 1.2s forwards;
      }
      &.right {
        width: 62px;
        top: 100px;
        left: 500px;
        opacity: 0;
        animation: cloudRight 2s linear 1s forwards;
      }
    }
  }
  .bullshit {
    position: relative;
    float: left;
    width: 300px;
    padding: 30px 0;
    overflow: hidden;
    &__oops {
      font-size: 32px;
      font-weight: bold;
      line-height: 40px;
      color: #1482f0;
      opacity: 0;
      margin-bottom: 20px;
      animation: slideUp 0.5s forwards;
    }
    &__headline {
      font-size: 20px;
      line-height: 24px;
      color: #222;
      font-weight: bold;
      opacity: 0;
      margin-bottom: 10px;
      animation: slideUp 0.5s 0.1s forwards;
    }
    &__info {
      font-size: 13px;
      line-height: 21px;
      color: grey;
      opacity: 0;
      margin-bottom: 14px;
      animation: slideUp 0.5s 0.2s forwards;
    }
    &__bilingual {
      display: flex;
      flex-direction: column;
      gap: 6px;
      margin-bottom: 24px;
      color: #606266;
      font-size: 13px;
      line-height: 20px;
      opacity: 0;
      animation: slideUp 0.5s 0.25s forwards;

      strong {
        color: #303133;
      }
    }
    &__actions {
      display: flex;
      align-items: center;
      gap: 10px;
      opacity: 0;
      animation: slideUp 0.5s 0.3s forwards;

      button {
        height: 32px;
        padding: 0 12px;
        border: 1px solid #dcdfe6;
        border-radius: 16px;
        background: #fff;
        color: #606266;
        cursor: pointer;

        &.active {
          border-color: #1482f0;
          color: #1482f0;
        }
      }
    }
    &__return-home {
      display: block;
      float: left;
      width: 110px;
      height: 36px;
      background: #1482f0;
      border-radius: 100px;
      text-align: center;
      color: #ffffff;
      opacity: 0;
      font-size: 14px;
      line-height: 36px;
      cursor: pointer;
    }
  }
}

@keyframes cloudLeft {
  0% { top: 17px; left: 220px; opacity: 0; }
  20% { top: 33px; left: 188px; opacity: 1; }
  80% { top: 81px; left: 92px; opacity: 1; }
  100% { top: 97px; left: 60px; opacity: 0; }
}
@keyframes cloudMid {
  0% { top: 10px; left: 420px; opacity: 0; }
  20% { top: 40px; left: 360px; opacity: 1; }
  70% { top: 130px; left: 180px; opacity: 1; }
  100% { top: 160px; left: 120px; opacity: 0; }
}
@keyframes cloudRight {
  0% { top: 100px; left: 500px; opacity: 0; }
  20% { top: 120px; left: 460px; opacity: 1; }
  80% { top: 180px; left: 340px; opacity: 1; }
  100% { top: 200px; left: 300px; opacity: 0; }
}
@keyframes slideUp {
  0% { transform: translateY(60px); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}
</style>
