<template>
  <div class="app-container product-master-guide">
    <section class="guide-hero">
      <div>
        <p class="guide-hero__eyebrow">{{ t('productCenter.masterGuide.eyebrow') }}</p>
        <h1>{{ t('productCenter.masterGuide.title') }}</h1>
        <p class="guide-hero__description">{{ t('productCenter.masterGuide.description') }}</p>
      </div>
      <div class="guide-hero__actions">
        <el-button type="primary" :icon="Plus" @click="go('/product-master/fabric-profiles')">
          {{ t('productCenter.masterGuide.newFabric') }}
        </el-button>
        <el-button :icon="Box" @click="go('/product-master/components')">
          {{ t('productCenter.masterGuide.newComponent') }}
        </el-button>
        <el-button :icon="Files" @click="go('/product-master/materials')">
          {{ t('productCenter.masterGuide.openMaterials') }}
        </el-button>
      </div>
    </section>

    <section class="guide-metrics" v-loading="loading">
      <article v-for="metric in metrics" :key="metric.key" class="guide-metric" @click="go(metric.path)">
        <el-icon class="guide-metric__icon"><component :is="metric.icon" /></el-icon>
        <div>
          <span>{{ t(metric.labelKey) }}</span>
          <strong>{{ metric.value }}</strong>
        </div>
      </article>
    </section>

    <section class="guide-workbench">
      <aside class="guide-rail">
        <article class="guide-panel">
          <header>
            <h2>{{ t('productCenter.masterGuide.modeTitle') }}</h2>
          </header>
          <div class="guide-mode-list">
            <button
              v-for="flow in flows"
              :key="flow.key"
              type="button"
              :class="{ 'is-active': activeFlow.key === flow.key }"
              @click="activeMode = flow.key"
            >
              <el-icon><component :is="flow.icon" /></el-icon>
              <span>
                <strong>{{ t(flow.titleKey) }}</strong>
                <small>{{ t(flow.summaryKey) }}</small>
              </span>
            </button>
          </div>
        </article>

        <article class="guide-panel">
          <header>
            <h2>{{ t('productCenter.masterGuide.recentTitle') }}</h2>
          </header>
          <div class="guide-recent-list">
            <button v-for="item in recentItems" :key="item.key" type="button" @click="go(item.path)">
              <strong>{{ item.title }}</strong>
              <span>{{ item.meta }}</span>
            </button>
            <el-empty v-if="!recentItems.length" :image-size="72" :description="t('productCenter.masterGuide.noRecent')" />
          </div>
        </article>
      </aside>

      <article class="guide-card guide-flow-card">
        <header class="guide-card__head">
          <div>
            <p>{{ t(activeFlow.badgeKey) }}</p>
            <h2>{{ t(activeFlow.titleKey) }}</h2>
          </div>
          <el-button type="primary" :icon="ArrowRight" @click="go(activeFlow.primaryPath)">
            {{ t('productCenter.masterGuide.start') }}
          </el-button>
        </header>
        <ol class="guide-steps">
          <li v-for="step in activeFlow.steps" :key="step.key" :class="{ 'is-ready': step.ready }">
            <span class="guide-steps__index">{{ step.index }}</span>
            <div>
              <div class="guide-steps__title">
                <h3>{{ t(step.titleKey) }}</h3>
                <el-tag size="small" :type="step.ready ? 'success' : 'warning'" effect="light">
                  {{ t(step.ready ? 'productCenter.masterGuide.ready' : 'productCenter.masterGuide.needAction') }}
                </el-tag>
              </div>
              <p>{{ t(step.descriptionKey) }}</p>
            </div>
            <div class="guide-steps__actions">
              <el-button :icon="step.icon" @click="go(step.path)">
                {{ t(step.actionKey) }}
              </el-button>
            </div>
          </li>
        </ol>
      </article>

      <aside class="guide-side">
        <article class="guide-panel">
          <header>
            <h2>{{ t('productCenter.masterGuide.checkTitle') }}</h2>
            <el-button text :icon="Refresh" :loading="loading" @click="loadOverview">
              {{ t('common.refresh') }}
            </el-button>
          </header>
          <div v-if="gaps.length" class="guide-gap-list">
            <button v-for="gap in gaps" :key="gap.key" type="button" @click="go(gap.path)">
              <el-icon><Warning /></el-icon>
              <span>{{ t(gap.labelKey) }}</span>
            </button>
          </div>
          <div v-else class="guide-ready">
            <el-icon><Finished /></el-icon>
            <span>{{ t('productCenter.masterGuide.noGaps') }}</span>
          </div>
        </article>

        <article class="guide-panel">
          <header>
            <h2>{{ t('productCenter.masterGuide.quickTitle') }}</h2>
          </header>
          <div class="guide-quick">
            <el-button v-for="action in quickActions" :key="action.key" :icon="action.icon" @click="go(action.path)">
              {{ t(action.labelKey) }}
            </el-button>
          </div>
        </article>

        <article class="guide-panel">
          <header>
            <h2>{{ t('productCenter.masterGuide.impactTitle') }}</h2>
          </header>
          <div class="guide-impact">
            <div>
              <span>{{ t('productCenter.masterGuide.impactConfig') }}</span>
              <strong>{{ counts.fabricProfiles + counts.components }}</strong>
            </div>
            <p>{{ t('productCenter.masterGuide.impactDesc') }}</p>
          </div>
        </article>
      </aside>
    </section>
  </div>
</template>

<script setup lang="ts" name="ProductMasterGuidePage">
import { computed, onMounted, reactive, ref, type Component } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRight,
  Box,
  Collection,
  Connection,
  Files,
  Finished,
  List,
  Plus,
  Refresh,
  SetUp,
  Tickets,
  Warning
} from '@element-plus/icons-vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productBaseAttributeApi, productUnitApi } from '@/api/product-capability/base'
import { productComponentApi, productComponentItemApi } from '@/api/product-capability/component'
import { fabricProfileApi, fabricSeriesApi } from '@/api/product-capability/fabric'
import { productMaterialApi, productMaterialAttributeApi } from '@/api/product-capability/material'
import type { FabricProfileVO, ProductComponentVO, ProductMaterialVO, ProductPageQuery } from '@/api/product-capability/types'

interface MetricConfig {
  key: keyof OverviewCounts
  labelKey: string
  path: string
  icon: Component
}

interface GuideStep {
  key: string
  index: number
  titleKey: string
  descriptionKey: string
  actionKey: string
  path: string
  icon: Component
  ready: boolean
}

interface GuideFlow {
  key: string
  icon: Component
  badgeKey: string
  titleKey: string
  summaryKey: string
  primaryPath: string
  steps: GuideStep[]
}

interface GuideGap {
  key: string
  labelKey: string
  path: string
}

interface QuickAction {
  key: string
  labelKey: string
  path: string
  icon: Component
}

interface OverviewCounts {
  units: number
  attributes: number
  materials: number
  materialAttributes: number
  fabricSeries: number
  fabricProfiles: number
  components: number
  componentItems: number
}

interface RecentItem {
  key: string
  title: string
  meta: string
  path: string
}

const localeStore = useLocaleStore()
const router = useRouter()
const t = (key: string) => getMessage(key, localeStore.language)
const loading = ref(false)
const activeMode = ref('fabric')
const recentMaterials = ref<ProductMaterialVO[]>([])
const recentFabrics = ref<FabricProfileVO[]>([])
const recentComponents = ref<ProductComponentVO[]>([])
const counts = reactive<OverviewCounts>({
  units: 0,
  attributes: 0,
  materials: 0,
  materialAttributes: 0,
  fabricSeries: 0,
  fabricProfiles: 0,
  components: 0,
  componentItems: 0
})

const countQuery: ProductPageQuery = { pageNum: 1, pageSize: 1 }
const recentQuery: ProductPageQuery = { pageNum: 1, pageSize: 4 }

const metricConfigs: MetricConfig[] = [
  { key: 'units', labelKey: 'productCenter.masterGuide.metricUnits', path: '/product-master/units', icon: SetUp },
  { key: 'attributes', labelKey: 'productCenter.masterGuide.metricAttributes', path: '/product-master/base-attributes', icon: Collection },
  { key: 'materials', labelKey: 'productCenter.masterGuide.metricMaterials', path: '/product-master/materials', icon: Files },
  { key: 'fabricProfiles', labelKey: 'productCenter.masterGuide.metricFabrics', path: '/product-master/fabric-profiles', icon: Tickets },
  { key: 'components', labelKey: 'productCenter.masterGuide.metricComponents', path: '/product-master/components', icon: Box },
  { key: 'componentItems', labelKey: 'productCenter.masterGuide.metricComponentItems', path: '/product-master/components?tab=componentItem', icon: Connection }
]

const metrics = computed(() => metricConfigs.map((item) => ({
  ...item,
  value: counts[item.key]
})))

const flows = computed<GuideFlow[]>(() => [
  {
    key: 'fabric',
    icon: Tickets,
    badgeKey: 'productCenter.masterGuide.fabricBadge',
    titleKey: 'productCenter.masterGuide.fabricFlowTitle',
    summaryKey: 'productCenter.masterGuide.fabricFlowSummary',
    primaryPath: '/product-master/fabric-profiles',
    steps: [
      step('fabric-units', 1, 'productCenter.masterGuide.stepUnits', 'productCenter.masterGuide.stepUnitsDesc', 'productCenter.masterGuide.openUnits', '/product-master/units', SetUp, counts.units > 0),
      step('fabric-material', 2, 'productCenter.masterGuide.stepMaterial', 'productCenter.masterGuide.stepMaterialDesc', 'productCenter.masterGuide.openMaterials', '/product-master/materials', Files, counts.materials > 0),
      step('fabric-series', 3, 'productCenter.masterGuide.stepFabricSeries', 'productCenter.masterGuide.stepFabricSeriesDesc', 'productCenter.masterGuide.openFabricSeries', '/product-master/fabric-series', Collection, counts.fabricSeries > 0),
      step('fabric-profile', 4, 'productCenter.masterGuide.stepFabricProfile', 'productCenter.masterGuide.stepFabricProfileDesc', 'productCenter.masterGuide.openFabricProfiles', '/product-master/fabric-profiles', Tickets, counts.fabricProfiles > 0)
    ]
  },
  {
    key: 'component',
    icon: Box,
    badgeKey: 'productCenter.masterGuide.componentBadge',
    titleKey: 'productCenter.masterGuide.componentFlowTitle',
    summaryKey: 'productCenter.masterGuide.componentFlowSummary',
    primaryPath: '/product-master/components',
    steps: [
      step('component-materials', 1, 'productCenter.masterGuide.stepComponentMaterial', 'productCenter.masterGuide.stepComponentMaterialDesc', 'productCenter.masterGuide.openMaterials', '/product-master/materials', Files, counts.materials > 0),
      step('component-attributes', 2, 'productCenter.masterGuide.stepMaterialAttributes', 'productCenter.masterGuide.stepMaterialAttributesDesc', 'productCenter.masterGuide.openMaterialAttributes', '/product-master/materials?tab=materialAttribute', List, counts.materialAttributes > 0),
      step('component-pack', 3, 'productCenter.masterGuide.stepComponentPack', 'productCenter.masterGuide.stepComponentPackDesc', 'productCenter.masterGuide.openComponents', '/product-master/components', Box, counts.components > 0),
      step('component-items', 4, 'productCenter.masterGuide.stepComponentItems', 'productCenter.masterGuide.stepComponentItemsDesc', 'productCenter.masterGuide.openComponentItems', '/product-master/components?tab=componentItem', Connection, counts.componentItems > 0)
    ]
  }
])

const activeFlow = computed(() => flows.value.find((flow) => flow.key === activeMode.value) || flows.value[0])

const recentItems = computed<RecentItem[]>(() => {
  const source = activeMode.value === 'fabric'
    ? [
        ...recentFabrics.value.map((item) => ({
          key: `fabric-${item.fabricId || item.fabricCode}`,
          title: item.fabricCode || item.colorName || '-',
          meta: [item.seriesNameCn || item.seriesCode, item.widthValue ? `${item.widthValue}${item.widthUnit || ''}` : item.materialCode].filter(Boolean).join(' / '),
          path: '/product-master/fabric-profiles'
        })),
        ...recentMaterials.value.map((item) => ({
          key: `material-${item.materialId || item.materialCode}`,
          title: item.materialCode || item.materialNameCn || '-',
          meta: [item.materialNameCn, item.materialType, item.unitCode].filter(Boolean).join(' / '),
          path: '/product-master/materials'
        }))
      ]
    : [
        ...recentComponents.value.map((item) => ({
          key: `component-${item.componentId || item.componentCode}`,
          title: item.componentCode || item.componentNameCn || '-',
          meta: [item.componentNameCn, item.componentType, item.unitCode].filter(Boolean).join(' / '),
          path: '/product-master/components'
        })),
        ...recentMaterials.value.map((item) => ({
          key: `material-${item.materialId || item.materialCode}`,
          title: item.materialCode || item.materialNameCn || '-',
          meta: [item.materialNameCn, item.materialType, item.unitCode].filter(Boolean).join(' / '),
          path: '/product-master/materials'
        }))
      ]
  return source.slice(0, 5)
})

const gaps = computed<GuideGap[]>(() => {
  const items: GuideGap[] = []
  if (counts.units === 0) items.push({ key: 'units', labelKey: 'productCenter.masterGuide.gapUnits', path: '/product-master/units' })
  if (counts.attributes === 0) items.push({ key: 'attributes', labelKey: 'productCenter.masterGuide.gapAttributes', path: '/product-master/base-attributes' })
  if (counts.materials === 0) items.push({ key: 'materials', labelKey: 'productCenter.masterGuide.gapMaterials', path: '/product-master/materials' })
  if (counts.fabricSeries === 0) items.push({ key: 'fabricSeries', labelKey: 'productCenter.masterGuide.gapFabricSeries', path: '/product-master/fabric-series' })
  if (counts.fabricProfiles === 0) items.push({ key: 'fabricProfiles', labelKey: 'productCenter.masterGuide.gapFabricProfiles', path: '/product-master/fabric-profiles' })
  if (counts.components === 0) items.push({ key: 'components', labelKey: 'productCenter.masterGuide.gapComponents', path: '/product-master/components' })
  if (counts.componentItems === 0) items.push({ key: 'componentItems', labelKey: 'productCenter.masterGuide.gapComponentItems', path: '/product-master/components?tab=componentItem' })
  return items
})

const quickActions: QuickAction[] = [
  { key: 'material', labelKey: 'productCenter.masterGuide.openMaterials', path: '/product-master/materials', icon: Files },
  { key: 'fabric', labelKey: 'productCenter.masterGuide.openFabricProfiles', path: '/product-master/fabric-profiles', icon: Tickets },
  { key: 'component', labelKey: 'productCenter.masterGuide.openComponents', path: '/product-master/components', icon: Box },
  { key: 'dictionary', labelKey: 'productCenter.masterGuide.openProductDicts', path: '/product-master/product-dicts', icon: Collection },
  { key: 'baseAttributes', labelKey: 'productCenter.masterGuide.openAttributes', path: '/product-master/base-attributes', icon: Collection },
  { key: 'attributes', labelKey: 'productCenter.masterGuide.openMaterialAttributes', path: '/product-master/materials?tab=materialAttribute', icon: List },
  { key: 'items', labelKey: 'productCenter.masterGuide.openComponentItems', path: '/product-master/components?tab=componentItem', icon: Connection }
]

function step(
  key: string,
  index: number,
  titleKey: string,
  descriptionKey: string,
  actionKey: string,
  path: string,
  icon: Component,
  ready: boolean
): GuideStep {
  return { key, index, titleKey, descriptionKey, actionKey, path, icon, ready }
}

async function loadOverview() {
  loading.value = true
  try {
    const [
      units,
      attributes,
      materials,
      materialAttributes,
      fabricSeries,
      fabricProfiles,
      components,
      componentItems
    ] = await Promise.all([
      productUnitApi.list(countQuery),
      productBaseAttributeApi.list(countQuery),
      productMaterialApi.list(countQuery),
      productMaterialAttributeApi.list(countQuery),
      fabricSeriesApi.list(countQuery),
      fabricProfileApi.list(countQuery),
      productComponentApi.list(countQuery),
      productComponentItemApi.list(countQuery)
    ])
    counts.units = units.total || 0
    counts.attributes = attributes.total || 0
    counts.materials = materials.total || 0
    counts.materialAttributes = materialAttributes.total || 0
    counts.fabricSeries = fabricSeries.total || 0
    counts.fabricProfiles = fabricProfiles.total || 0
    counts.components = components.total || 0
    counts.componentItems = componentItems.total || 0
    const [recentMaterialRows, recentFabricRows, recentComponentRows] = await Promise.all([
      productMaterialApi.list(recentQuery),
      fabricProfileApi.list(recentQuery),
      productComponentApi.list(recentQuery)
    ])
    recentMaterials.value = recentMaterialRows.rows || []
    recentFabrics.value = recentFabricRows.rows || []
    recentComponents.value = recentComponentRows.rows || []
  } finally {
    loading.value = false
  }
}

function go(path: string) {
  router.push(path)
}

onMounted(() => {
  loadOverview()
})
</script>

<style scoped lang="scss">
.product-master-guide {
  color: #1f2937;
}

.guide-hero,
.guide-card,
.guide-panel,
.guide-metric {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.guide-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 14px;
  padding: 20px 22px;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.04);
}

.guide-hero__eyebrow {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 650;
}

.guide-hero h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.guide-hero__description {
  max-width: 760px;
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.guide-hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.guide-metrics {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.guide-metric {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 72px;
  padding: 12px 14px;
  cursor: pointer;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;

  &:hover {
    border-color: #93c5fd;
    box-shadow: 0 8px 20px rgba(37, 99, 235, 0.08);
    transform: translateY(-1px);
  }

  span {
    color: #64748b;
    font-size: 13px;
  }

  strong {
    display: block;
    margin-top: 2px;
    font-size: 22px;
    line-height: 1;
  }
}

.guide-metric__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 8px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 18px;
}

.guide-workbench {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr) 340px;
  gap: 14px;
}

.guide-rail,
.guide-side {
  display: grid;
  align-content: start;
  gap: 14px;
}

.guide-card {
  padding: 16px;
}

.guide-flow-card {
  min-width: 0;
}

.guide-card__head,
.guide-panel header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.guide-card__head p {
  margin: 0 0 4px;
  color: #64748b;
  font-size: 13px;
}

.guide-card__head h2,
.guide-panel h2 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
}

.guide-mode-list {
  display: grid;
  gap: 8px;
}

.guide-mode-list button {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  gap: 10px;
  width: 100%;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  color: #334155;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s ease, background 0.18s ease;

  .el-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 34px;
    height: 34px;
    border-radius: 8px;
    background: #f1f5f9;
    color: #475569;
    font-size: 17px;
  }

  strong,
  small {
    display: block;
  }

  strong {
    font-size: 14px;
  }

  small {
    margin-top: 3px;
    color: #64748b;
    line-height: 1.45;
  }

  &.is-active {
    border-color: #2563eb;
    background: #eff6ff;

    .el-icon {
      background: #2563eb;
      color: #fff;
    }
  }
}

.guide-recent-list {
  display: grid;
  gap: 8px;
}

.guide-recent-list button {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  color: #334155;
  text-align: left;
  cursor: pointer;

  strong,
  span {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    font-size: 13px;
  }

  span {
    margin-top: 4px;
    color: #64748b;
    font-size: 12px;
  }
}

.guide-steps {
  display: grid;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.guide-steps li {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) 148px;
  align-items: center;
  gap: 10px 12px;
  min-height: 96px;
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;

  &.is-ready {
    border-color: #bfdbfe;

    .guide-steps__index {
      border-color: #2563eb;
      background: #eff6ff;
      color: #2563eb;
    }
  }

  p {
    margin: 4px 0 0;
    color: #64748b;
    font-size: 13px;
    line-height: 1.5;
  }
}

.guide-steps__title {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;

  h3 {
    margin: 0;
    font-size: 15px;
  }
}

.guide-steps__index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background: #f8fafc;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
}

.guide-steps__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  min-width: 0;

  :deep(.el-button) {
    width: 100%;
    margin: 0;
  }
}

.guide-panel {
  padding: 16px;
}

.guide-gap-list,
.guide-quick {
  display: grid;
  gap: 8px;
}

.guide-gap-list button {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #fed7aa;
  border-radius: 8px;
  background: #fff7ed;
  color: #9a3412;
  text-align: left;
  cursor: pointer;
}

.guide-ready {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 86px;
  padding: 16px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background: #f8fafc;
  color: #64748b;
  line-height: 1.5;

  .el-icon {
    flex: 0 0 auto;
    color: #2563eb;
    font-size: 22px;
  }
}

.guide-impact {
  display: grid;
  gap: 10px;
  padding: 14px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background: #f8fafc;

  div {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 12px;
  }

  span {
    color: #64748b;
  }

  strong {
    color: #2563eb;
    font-size: 24px;
    line-height: 1;
  }

  p {
    margin: 0;
    color: #64748b;
    line-height: 1.55;
  }
}

.guide-quick {
  grid-template-columns: repeat(2, minmax(0, 1fr));

  :deep(.el-button) {
    width: 100%;
    margin: 0;
  }
}

@media (max-width: 1200px) {
  .guide-metrics {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .guide-workbench {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .guide-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .guide-metrics,
  .guide-quick {
    grid-template-columns: 1fr;
  }

  .guide-steps li {
    grid-template-columns: 34px minmax(0, 1fr);

    .guide-steps__actions {
      grid-column: 2;
      justify-content: flex-start;
    }
  }
}
</style>
