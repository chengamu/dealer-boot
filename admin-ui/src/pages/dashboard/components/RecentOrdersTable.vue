<template>
  <div class="portal-orders">
    <div class="portal-orders__head">
      <span>{{ text.orderNo }}</span>
      <span>{{ text.customer }}</span>
      <span>{{ text.status }}</span>
      <span>{{ text.orderDate }}</span>
      <span>{{ text.total }}</span>
      <span>{{ text.action }}</span>
    </div>
    <div v-for="order in orders" :key="order.no" class="portal-orders__row">
      <a href="javascript:void(0)" @click="emit('view', order)">{{ order.no }}</a>
      <div>
        <strong>{{ order.customer }}</strong>
        <small>{{ order.country }}</small>
      </div>
      <span class="portal-pill" :class="`is-${order.tone}`">{{ order.status }}</span>
      <span>{{ order.date }}</span>
      <strong>{{ order.total }}</strong>
      <button type="button" :aria-label="text.view" @click="emit('view', order)">
        <el-icon><View /></el-icon>
      </button>
    </div>
    <footer class="portal-orders__footer">
      <span>{{ text.showingOrders }}</span>
      <div>
        <button
          v-for="page in pages"
          :key="page"
          type="button"
          :class="{ 'is-active': page === currentPage }"
          @click="setPage(page)"
        >
          {{ page }}
        </button>
        <button type="button" @click="setPage(nextPage)">
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ArrowRight, View } from '@element-plus/icons-vue'
import type { OrderItem, OrderTableText } from './types'

const props = withDefaults(defineProps<{
  orders: OrderItem[]
  text: OrderTableText
  totalPages?: number
}>(), {
  totalPages: 3
})

const emit = defineEmits<{
  view: [order: OrderItem]
  pageChange: [page: number]
}>()

const currentPage = ref(1)
const pages = computed(() => Array.from({ length: props.totalPages }, (_, index) => index + 1))
const nextPage = computed(() => currentPage.value === props.totalPages ? 1 : currentPage.value + 1)

function setPage(page: number) {
  currentPage.value = page
  emit('pageChange', page)
}
</script>
