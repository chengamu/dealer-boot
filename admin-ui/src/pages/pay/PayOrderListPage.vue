<template>
  <PaymentCheckoutPage v-if="checkoutId && pageMode === 'business-orders'" :sales-document-id="checkoutId" @back="closeCheckout" />
  <BusinessPaymentGrid v-else-if="pageMode === 'business-orders'" @checkout="openCheckout" />
  <BusinessCreditGrid v-else-if="pageMode === 'business-credit'" mode="credit" />
  <BusinessCreditGrid v-else-if="pageMode === 'business-receivable'" mode="receivable" />
  <PlatformPaymentGrid v-else-if="pageMode === 'platform-payments'" />
  <PlatformBankTransferGrid v-else-if="pageMode === 'platform-bank'" />
  <PlatformCreditGrid v-else-if="pageMode === 'platform-credit'" mode="credit" />
  <PlatformCreditGrid v-else-if="pageMode === 'platform-receivable'" mode="receivable" />
  <PlatformReconciliationDetailPage
    v-else-if="reconciliationCaseId"
    :case-id="reconciliationCaseId"
    @back="closeReconciliation"
  />
  <PlatformReconciliationGrid v-else @detail="openReconciliation" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import PaymentCheckoutPage from './PaymentCheckoutPage.vue'
import BusinessCreditGrid from './components/BusinessCreditGrid.vue'
import BusinessPaymentGrid from './components/BusinessPaymentGrid.vue'
import PlatformBankTransferGrid from './components/PlatformBankTransferGrid.vue'
import PlatformCreditGrid from './components/PlatformCreditGrid.vue'
import PlatformPaymentGrid from './components/PlatformPaymentGrid.vue'
import PlatformReconciliationGrid from './components/PlatformReconciliationGrid.vue'
import PlatformReconciliationDetailPage from './PlatformReconciliationDetailPage.vue'
import { resolvePayPageMode } from './payPageModes'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const checkoutId = computed(() => String(route.query.salesDocumentId || ''))
const reconciliationCaseId = computed(() => String(route.query.caseId || ''))
const pageMode = computed(() => resolvePayPageMode(route, userStore.permissions))

function openCheckout(salesDocumentId: string) {
  void router.replace({ query: { ...route.query, salesDocumentId } })
}

function closeCheckout() {
  const next = { ...route.query }
  delete next.salesDocumentId
  void router.replace({ query: next })
}

function openReconciliation(caseId: string) {
  void router.replace({ query: { ...route.query, caseId } })
}

function closeReconciliation() {
  const next = { ...route.query }
  delete next.caseId
  void router.replace({ query: next })
}
</script>
