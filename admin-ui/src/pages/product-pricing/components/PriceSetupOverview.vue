<template>
  <div class="price-overview">
    <div class="price-overview__cards">
      <div v-for="card in cards" :key="card.code" class="price-overview__card">
        <span class="price-overview__group-icon"><img :src="card.icon" :alt="card.name" /></span>
        <span>{{ card.name }}</span>
        <strong>{{ card.count }}</strong>
      </div>
      <div class="price-overview__card">
        <span class="price-overview__icon"><Money /></span>
        <span>{{ t('productCenter.pricing.fabricRules') }}</span>
        <strong>{{ fabricRuleCount }}</strong>
      </div>
      <div class="price-overview__card">
        <span class="price-overview__icon price-overview__icon--fee"><Van /></span>
        <span>{{ t('productCenter.pricing.extraFeeRules') }}</span>
        <strong>{{ feeRuleCount }}</strong>
      </div>
      <div class="price-overview__card">
        <span class="price-overview__icon price-overview__icon--warning"><WarningFilled /></span>
        <span>{{ t('productCenter.pricing.pendingIssues') }}</span>
        <strong class="price-overview__warning">{{ issueCount }}</strong>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Money, Van, WarningFilled } from '@element-plus/icons-vue'
import fabricIcon from '@/assets/product-formula/icons/group-fabric.png'
import aluminumIcon from '@/assets/product-formula/icons/group-aluminum.png'
import systemIcon from '@/assets/product-formula/icons/group-system.png'
import accessoryIcon from '@/assets/product-formula/icons/group-accessory.png'
import packagingIcon from '@/assets/product-formula/icons/group-packaging.png'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const props = defineProps<{
  fabricRuleCount: number
  feeRuleCount: number
  issueCount: number
  materialGroupCounts?: Record<string, number>
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const cards = computed(() => [
  { code: 'FABRIC', name: t('productCenter.pricing.groupFabric'), count: count('FABRIC'), icon: fabricIcon },
  { code: 'ALUMINUM', name: t('productCenter.pricing.groupAluminum'), count: count('ALUMINUM'), icon: aluminumIcon },
  { code: 'SYSTEM', name: t('productCenter.pricing.groupSystem'), count: count('SYSTEM'), icon: systemIcon },
  { code: 'ACCESSORY', name: t('productCenter.pricing.groupAccessory'), count: count('ACCESSORY'), icon: accessoryIcon },
  { code: 'PACKAGING', name: t('productCenter.pricing.groupPackaging'), count: count('PACKAGING'), icon: packagingIcon }
])

function count(code: string) {
  return props.materialGroupCounts?.[code] || 0
}
</script>

<style scoped>
.price-overview {
  display: grid;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
}

.price-overview__cards,
.price-overview__card {
  display: flex;
  align-items: center;
}

.price-overview__cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(168px, 1fr));
  gap: 10px;
}

.price-overview__card {
  display: grid;
  grid-template-columns: 32px minmax(70px, 1fr) minmax(34px, auto);
  gap: 10px;
  min-height: 58px;
  padding: 10px 14px;
  background: linear-gradient(180deg, #fff, #f8fbff);
  border: 1px solid #e5ecf6;
  border-radius: 8px;
}

.price-overview__group-icon,
.price-overview__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  color: #1677ff;
  font-size: 21px;
  background: #f1f7ff;
  border-radius: 10px;
}

.price-overview__group-icon img {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.price-overview__card span {
  color: #1f2937;
  font-weight: 600;
}

.price-overview strong {
  color: #111827;
  font-size: 22px;
  line-height: 1;
}

.price-overview__icon--fee {
  color: #22c55e;
  background: #f0fdf4;
}

.price-overview__icon--warning,
.price-overview__warning {
  color: #f59e0b !important;
}
</style>
