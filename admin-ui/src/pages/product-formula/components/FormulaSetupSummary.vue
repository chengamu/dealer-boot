<template>
  <div class="setup-summary">
    <div v-for="card in materialGroupCards" :key="card.code" class="summary-card" :class="`summary-card--${card.code.toLowerCase()}`">
      <span class="summary-card__icon">
        <img :src="groupIcon(card.code)" :alt="displayName(card)" />
      </span>
      <span class="summary-card__text">
        <span class="summary-card__label">{{ displayName(card) }}</span>
      </span>
      <strong>{{ card.count }}</strong>
    </div>
    <div class="summary-metrics">
      <div class="summary-metric summary-metric--total">
        <span class="summary-metric__icon"><DataAnalysis /></span>
        <span class="summary-metric__text">
          <span>{{ t('productCenter.formulaSetup.materialTotal') }}</span>
        </span>
        <strong>{{ materialCount }}</strong>
      </div>
      <div class="summary-metric summary-metric--warning">
        <span class="summary-metric__icon"><Warning /></span>
        <span class="summary-metric__text">
          <span>{{ t('productCenter.formulaSetup.unsetUsageCount') }}</span>
        </span>
        <strong class="summary-metric__warning">{{ unsetUsageCount }}</strong>
      </div>
      <div class="summary-metric summary-metric--danger">
        <span class="summary-metric__icon"><CircleCloseFilled /></span>
        <span class="summary-metric__text">
          <span>{{ t('productCenter.formulaSetup.exceptionCount') }}</span>
        </span>
        <strong class="summary-metric__danger">{{ exceptionCount }}</strong>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { CircleCloseFilled, DataAnalysis, Warning } from '@element-plus/icons-vue'
import fabricIcon from '@/assets/product-formula/icons/group-fabric.png'
import aluminumIcon from '@/assets/product-formula/icons/group-aluminum.png'
import systemIcon from '@/assets/product-formula/icons/group-system.png'
import accessoryIcon from '@/assets/product-formula/icons/group-accessory.png'
import partPackIcon from '@/assets/product-formula/icons/group-part-pack.png'
import packagingIcon from '@/assets/product-formula/icons/group-packaging.png'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

interface MaterialGroupCard {
  code: string
  name: string
  nameEn?: string
  shortName: string
  count: number
}

defineProps<{
  materialGroupCards: MaterialGroupCard[]
  materialCount: number
  unsetUsageCount: number
  optionCount: number
  ruleCount: number
  exceptionCount: number
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)

const groupIconMap: Record<string, string> = {
  FABRIC: fabricIcon,
  ALUMINUM: aluminumIcon,
  SYSTEM: systemIcon,
  ACCESSORY: accessoryIcon,
  PART_PACK: partPackIcon,
  PACKAGING: packagingIcon
}

function groupIcon(code: string) {
  return groupIconMap[code] || packagingIcon
}

function displayName(card: MaterialGroupCard) {
  return localeStore.language === 'zh_CN' ? card.name : card.nameEn || card.code || card.name
}
</script>

<style scoped>
.setup-summary {
  display: flex;
  flex-wrap: wrap;
  align-items: stretch;
  gap: 8px;
  margin-top: 12px;
  padding: 10px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 0 1 150px;
  min-width: 0;
  min-height: 58px;
  border: 1px solid #e5ecf6;
  border-radius: 8px;
  padding: 9px 10px;
  background: linear-gradient(180deg, #fff, #f8fbff);
  box-shadow: 0 4px 14px rgb(15 23 42 / 3%);
}

.summary-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 32px;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: #f9fbff;
  overflow: hidden;
}

.summary-card__icon img {
  width: 30px;
  height: 30px;
  object-fit: contain;
  display: block;
}

.summary-card__label {
  color: #1f2937;
  font-weight: 600;
  white-space: nowrap;
}

.summary-card__text {
  display: grid;
  min-width: 0;
  gap: 2px;
}

.summary-card strong {
  margin-left: auto;
  color: #111827;
  font-size: 21px;
  line-height: 1;
}

.summary-metrics {
  display: contents;
}

.summary-metric {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 0 1 154px;
  min-width: 0;
  min-height: 58px;
  padding: 9px 10px;
  color: #1f2937;
  background: linear-gradient(180deg, #fff, #f8fbff);
  border: 1px solid #e5ecf6;
  border-radius: 8px;
  box-shadow: 0 4px 14px rgb(15 23 42 / 3%);
}

.summary-metric__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 32px;
  width: 32px;
  height: 32px;
  color: #1677ff;
  font-size: 21px;
  background: #f1f7ff;
  border-radius: 10px;
}

.summary-metric--warning .summary-metric__icon {
  color: #f59e0b;
  background: #fff8eb;
}

.summary-metric--danger .summary-metric__icon {
  color: #dc2626;
  background: #fff1f1;
}

.summary-metric__text {
  display: grid;
  min-width: 0;
  gap: 2px;
}

.summary-metric__text span {
  overflow: hidden;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.summary-metric strong {
  margin-left: auto;
  color: #111827;
  font-size: 21px;
  line-height: 1;
}

.summary-metric__warning {
  color: #f59e0b !important;
}

.summary-metric__danger {
  color: #dc2626 !important;
}

@media (max-width: 1440px) {
  .summary-card,
  .summary-metric {
    flex-basis: 138px;
  }
}
</style>
