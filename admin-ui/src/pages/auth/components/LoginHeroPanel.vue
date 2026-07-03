<template>
  <section class="hero-panel">
    <div class="brand">
      <div class="sky-logo">skyspf</div>
      <p>{{ t('login.portalName') }}</p>
    </div>

    <div class="portal-pill">
      <el-icon><Connection /></el-icon>
      <span>{{ t('login.dealerPortal') }}</span>
    </div>

    <div class="hero-copy">
      <h1>{{ t('login.heroLine1') }}<br />{{ t('login.heroLine2') }}<br />{{ t('login.heroLine3') }}</h1>
      <p>{{ t('login.heroDescription') }}</p>
    </div>

    <div class="room-scene" aria-hidden="true">
      <div class="curtain" />
      <div class="window-frame" />
      <div class="blind" />
    </div>

    <article class="float-card order-card">
      <span class="check-dot">✓</span>
      <strong>{{ t('login.newOrder') }}</strong>
      <small>{{ t('login.orderProduct') }}</small>
    </article>

    <article class="float-card chart-card">
      <strong>{{ t('login.orderOverview') }}</strong>
      <div class="mini-chart">
        <span v-for="bar in 7" :key="bar" :style="{ height: `${18 + (bar % 4) * 13}px` }" />
      </div>
      <small>{{ t('login.thisMonth') }} <b>↑ 18.6%</b></small>
    </article>

    <article class="float-card material-card">
      <strong>{{ t('login.topFabricCollections') }}</strong>
      <div class="swatches">
        <i v-for="swatch in swatches" :key="swatch" :style="{ background: swatch }" />
      </div>
    </article>

    <article class="trust-card">
      <span><el-icon><Connection /></el-icon></span>
      <div>
        <strong>{{ t('login.trustTitle') }}</strong>
        <small>{{ t('login.trustText') }}</small>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { Connection } from '@element-plus/icons-vue'

const { t } = useI18n()
const swatches = ['#d9c7aa', '#e8e1d2', '#c8c5ba', '#8995a1']
</script>

<style scoped lang="scss" src="./LoginHeroPanel.scss"></style>
<style scoped lang="scss" src="./LoginHeroPanel.responsive.scss"></style>
