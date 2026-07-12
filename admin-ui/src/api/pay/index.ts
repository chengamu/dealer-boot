import { requestData, requestPage } from '@/utils/request'
import type { BankCollectionAccount, BankSubmitRequest, CreditAccount, CreditTransaction, EnabledPayChannel, PayAttempt, PayOrder, PayOrderDetail, PayOrderQuery, PayPalCheckout, Receivable, SalesPayment, SupplementRequest } from './types'
import type { DecimalValue } from '@/types/api'

const adminRoot = '/pay/admin'
const salesRoot = '/dealer/sales-documents'

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

export * from './types'
