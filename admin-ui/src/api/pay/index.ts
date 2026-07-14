import { requestData, requestPage } from '@/utils/request'
import type {
  BankCollectionAccount,
  BankSubmitRequest,
  BankTransferQuery,
  BankTransferRecord,
  CreditAccount,
  CreditAccountQuery,
  CreditRepayRequest,
  CreditTransaction,
  CreditTransactionQuery,
  EnabledPayChannel,
  PayAttempt,
  PayOrder,
  PayOrderDetail,
  PayOrderQuery,
  PayPalCheckout,
  Receivable,
  ReceivableQuery,
  ReconciliationCase,
  ReconciliationCaseDetail,
  ReconciliationQuery,
  SalesPayment,
  SupplementRequest
} from './types'
import type { DecimalValue } from '@/types/api'

const adminRoot = '/pay/admin'
const salesRoot = '/dealer/sales-documents'
const businessRoot = '/dealer/payments'
const businessCreditRoot = '/dealer/credit-account'
const businessReceivableRoot = '/dealer/receivables'
const platformPaymentRoot = '/platform-finance/payment-orders'
const platformBankRoot = '/platform-finance/bank-transfers'
const platformCreditRoot = '/platform-finance/credit-accounts'
const platformReceivableRoot = '/platform-finance/receivables'
const platformReconciliationRoot = '/platform-finance/reconciliation-cases'

export const payApi = {
  orderList: (params: PayOrderQuery) => requestPage<PayOrder>({ url: `${adminRoot}/order/list`, method: 'get', params }),
  orderDetail: (payOrderId: string | number) => requestData<PayOrderDetail>({ url: `${adminRoot}/order/${payOrderId}`, method: 'get' }),
  creditAccounts: (params: Record<string, unknown>) => requestPage<CreditAccount>({ url: `${adminRoot}/credit/account/list`, method: 'get', params }),
  creditTransactions: (params: Record<string, unknown>) => requestPage<CreditTransaction>({ url: `${adminRoot}/credit/transaction/list`, method: 'get', params }),
  receivables: (params: Record<string, unknown>) => requestPage<Receivable>({ url: `${adminRoot}/credit/receivable/list`, method: 'get', params }),
  enabledChannels: (params: { payOrderId?: string | number; appId?: string | number }) => requestData<EnabledPayChannel[]>({
    url: `${adminRoot}/channel/enabled`, method: 'get', params
  }),
  bankAccounts: (params: { payOrderId?: string | number; appId?: string | number; currency?: string }) => requestData<BankCollectionAccount[]>({
    url: `${adminRoot}/channel/bank-accounts`, method: 'get', params
  }),
  salesPayment: (salesDocumentId: string | number) => requestData<SalesPayment>({ url: `${salesRoot}/${salesDocumentId}/payment`, method: 'get' }),
  createPayPal: (salesDocumentId: string | number) => requestData<PayPalCheckout>({ url: `${salesRoot}/${salesDocumentId}/payment/paypal/create`, method: 'post' }),
  capturePayPal: (salesDocumentId: string | number, paypalOrderId: string) => requestData<PayPalCheckout>({
    url: `${salesRoot}/${salesDocumentId}/payment/paypal/capture`, method: 'post', data: { paypalOrderId }
  }),
  submitBank: (salesDocumentId: string | number, data: BankSubmitRequest) => requestData<PayAttempt>({
    url: `${salesRoot}/${salesDocumentId}/payment/bank-transfer`, method: 'post', data
  }),
  useCredit: (salesDocumentId: string | number) => requestData<Receivable>({ url: `${salesRoot}/${salesDocumentId}/payment/credit`, method: 'post' }),
  reviewBank: (extensionId: string | number, approved: boolean, reason?: string) => requestData<PayAttempt>({
    url: `${adminRoot}/bank/${extensionId}/review`, method: 'post', data: { approved, reason }
  }),
  supplement: (payOrderId: string | number, data: SupplementRequest) => requestData<PayAttempt>({
    url: `${adminRoot}/order/${payOrderId}/supplement`, method: 'post', data
  }),
  repair: (payOrderId: string | number) => requestData<void>({ url: `${adminRoot}/order/${payOrderId}/repair`, method: 'post' }),
  reconcilePayPal: (payOrderId: string | number) => requestData<PayPalCheckout>({ url: `${adminRoot}/order/${payOrderId}/paypal/reconcile`, method: 'post' }),
  adjustCredit: (accountId: string | number, amount: DecimalValue, reason: string) => requestData({
    url: `${adminRoot}/credit/${accountId}/adjust`, method: 'post', data: { amount, reason }
  }),
  freezeCredit: (accountId: string | number, frozen: boolean, reason: string) => requestData({
    url: `${adminRoot}/credit/${accountId}/freeze`, method: 'post', data: { frozen, reason }
  }),
  repayReceivable: (receivableId: string | number, reason: string) => requestData({
    url: `${adminRoot}/credit/receivable/${receivableId}/repay`, method: 'post', params: { reason }
  })
}

export const businessPaymentApi = {
  list: (params: PayOrderQuery) => requestPage<PayOrder>({ url: `${businessRoot}/list`, method: 'get', params }),
  detail: (payOrderId: string | number) => requestData<PayOrderDetail>({ url: `${businessRoot}/${payOrderId}`, method: 'get' }),
  creditAccount: () => requestData<CreditAccount | null>({ url: businessCreditRoot, method: 'get' }),
  creditTransactions: (params: CreditTransactionQuery) => requestPage<CreditTransaction>({ url: `${businessCreditRoot}/transactions`, method: 'get', params }),
  receivables: (params: ReceivableQuery) => requestPage<Receivable>({ url: `${businessReceivableRoot}/list`, method: 'get', params }),
  receivableDetail: (receivableId: string | number) => requestData<Receivable>({ url: `${businessReceivableRoot}/${receivableId}`, method: 'get' })
}

export const platformPaymentApi = {
  list: (params: PayOrderQuery) => requestPage<PayOrder>({ url: `${platformPaymentRoot}/list`, method: 'get', params }),
  detail: (payOrderId: string | number) => requestData<PayOrderDetail>({ url: `${platformPaymentRoot}/${payOrderId}`, method: 'get' }),
  supplement: (payOrderId: string | number, data: SupplementRequest) => requestData<PayAttempt>({
    url: `${platformPaymentRoot}/${payOrderId}/supplement`, method: 'post', data
  })
}

export const platformBankApi = {
  list: (params: BankTransferQuery) => requestPage<BankTransferRecord>({ url: `${platformBankRoot}/list`, method: 'get', params }),
  detail: (extensionId: string | number) => requestData<BankTransferRecord>({ url: `${platformBankRoot}/${extensionId}`, method: 'get' }),
  review: (extensionId: string | number, approved: boolean, reason?: string) => requestData<PayAttempt>({
    url: `${platformBankRoot}/${extensionId}/review`, method: 'post', data: { approved, reason }
  })
}

export const platformCreditApi = {
  list: (params: CreditAccountQuery) => requestPage<CreditAccount>({ url: `${platformCreditRoot}/list`, method: 'get', params }),
  detail: (accountId: string | number) => requestData<CreditAccount>({ url: `${platformCreditRoot}/${accountId}`, method: 'get' }),
  transactions: (accountId: string | number, params: CreditTransactionQuery) => requestPage<CreditTransaction>({
    url: `${platformCreditRoot}/${accountId}/transactions`, method: 'get', params
  }),
  adjust: (accountId: string | number, amount: DecimalValue, reason: string) => requestData<CreditAccount>({
    url: `${platformCreditRoot}/${accountId}/adjust`, method: 'post', data: { amount, reason }
  }),
  freeze: (accountId: string | number, reason: string) => requestData<CreditAccount>({
    url: `${platformCreditRoot}/${accountId}/freeze`, method: 'post', data: { frozen: true, reason }
  }),
  unfreeze: (accountId: string | number, reason: string) => requestData<CreditAccount>({
    url: `${platformCreditRoot}/${accountId}/unfreeze`, method: 'post', data: { frozen: false, reason }
  })
}

export const platformReceivableApi = {
  list: (params: ReceivableQuery) => requestPage<Receivable>({ url: `${platformReceivableRoot}/list`, method: 'get', params }),
  detail: (receivableId: string | number) => requestData<Receivable>({ url: `${platformReceivableRoot}/${receivableId}`, method: 'get' }),
  repay: (receivableId: string | number, data: CreditRepayRequest) => requestData<Receivable>({
    url: `${platformReceivableRoot}/${receivableId}/repay`, method: 'post', data
  })
}

export const platformReconciliationApi = {
  list: (params: ReconciliationQuery) => requestPage<ReconciliationCase>({ url: `${platformReconciliationRoot}/list`, method: 'get', params }),
  detail: (caseId: string | number) => requestData<ReconciliationCaseDetail>({ url: `${platformReconciliationRoot}/${caseId}`, method: 'get' }),
  scan: (payOrderId: string | number) => requestData<ReconciliationCase>({ url: `${platformReconciliationRoot}/scan/${payOrderId}`, method: 'post' }),
  rescan: (caseId: string | number, reason: string) => requestData<ReconciliationCase>({
    url: `${platformReconciliationRoot}/${caseId}/rescan`, method: 'post', data: { reason }
  }),
  reconcileChannel: (caseId: string | number, reason: string) => requestData<ReconciliationCase>({
    url: `${platformReconciliationRoot}/${caseId}/reconcile-channel`, method: 'post', data: { reason }
  }),
  repairOrder: (caseId: string | number, reason: string) => requestData<ReconciliationCase>({
    url: `${platformReconciliationRoot}/${caseId}/repair-order`, method: 'post', data: { reason }
  }),
  ignore: (caseId: string | number, reason: string) => requestData<ReconciliationCase>({
    url: `${platformReconciliationRoot}/${caseId}/ignore`, method: 'post', data: { reason }
  })
}

export * from './types'
