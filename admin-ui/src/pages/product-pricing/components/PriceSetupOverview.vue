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
        <span>{{ t('productCenter.pricing.materialPrices') }}</span>
        <strong>{{ materialRuleCount }}</strong>
      </div>
      <div class="price-overview__card">
        <span class="price-overview__icon price-overview__icon--warning"><WarningFilled /></span>
        <span>{{ t('productCenter.pricing.exceptions') }}</span>
        <strong class="price-overview__warning">{{ issueCount }}</strong>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Money, WarningFilled } from '@element-plus/icons-vue'
import fabricIcon from '@/assets/product-formula/icons/group-fabric.png'
import aluminumIcon from '@/assets/product-formula/icons/group-aluminum.png'
import systemIcon from '@/assets/product-formula/icons/group-system.png'
import accessoryIcon from '@/assets/product-formula/icons/group-accessory.png'
import packagingIcon from '@/assets/product-formula/icons/group-packaging.png'
import partPackIcon from '@/assets/product-formula/icons/group-part-pack.png'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import type { ProductFormulaMaterialVO } from '@/api/product-capability/types'

const props = defineProps<{
  materialRuleCount: number
  issueCount: number
  materialGroupCounts?: Record<string, number>
  formulaMaterials?: ProductFormulaMaterialVO[]
}>()

const localeStore = useLocaleStore()
const t = (key: string) => getMessage(key, localeStore.language)
const baseCards = computed(() => [
  { code: 'FABRIC', name: t('productCenter.pricing.groupFabric'), icon: fabricIcon },
  { code: 'ALUMINUM', name: t('productCenter.pricing.groupAluminum'), icon: aluminumIcon },
  { code: 'SYSTEM', name: t('productCenter.pricing.groupSystem'), icon: systemIcon },
  { code: 'ACCESSORY', name: t('productCenter.pricing.groupAccessory'), icon: accessoryIcon },
  { code: 'PART_PACK', name: t('productCenter.pricing.groupPartPack'), icon: partPackIcon },
  { code: 'PACKAGING', name: t('productCenter.pricing.groupPackaging'), icon: packagingIcon }
])
const cards = computed(() => {
  const knownCodes = new Set(baseCards.value.map((item) => item.code))
  const extras = Object.keys(props.materialGroupCounts || {})
    .filter((code) => !knownCodes.has(code))
    .map((code) => ({ code, name: groupName(code), icon: accessoryIcon }))
  return [...baseCards.value, ...extras].map((item) => ({ ...item, count: count(item.code) }))
})

function count(code: string) {
  return props.materialGroupCounts?.[code] || 0
}

function groupName(code: string) {
  const material = props.formulaMaterials?.find((item) =>
    (item.attributeGroupCode || item.materialTypeCode || 'UNCLASSIFIED') === code)
  return material?.attributeGroupNameCn || material?.materialTypeNameCn || code
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
  grid-auto-flow: column;
  grid-auto-columns: minmax(112px, 1fr);
  gap: 8px;
  overflow-x: auto;
}

.price-overview__card {
  display: grid;
  grid-template-columns: 30px minmax(34px, 1fr) minmax(24px, auto);
  gap: 6px;
  min-height: 52px;
  padding: 8px 12px;
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

.price-overview__card > span:not(.price-overview__group-icon):not(.price-overview__icon) {
  color: #1f2937;
  font-weight: 600;
  white-space: nowrap;
}

.price-overview strong {
  color: #111827;
  font-size: 20px;
  line-height: 1;
}

.price-overview__icon--warning,
.price-overview__warning {
  color: #f59e0b !important;
}
</style>
