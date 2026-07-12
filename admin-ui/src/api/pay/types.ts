import type { DecimalValue, PageQuery } from '@/types/api'

export type PayOrderStatus = 0 | 5 | 10 | 20
export type PayMethod = 'paypal' | 'bank_transfer' | 'credit_limit' | string

export interface PayOrder {
  id?: string
  payerTenantId?: string
  payeeTenantId?: string
  channelCode?: PayMethod
  no?: string
  merchantOrderId?: string
  salesDocumentId?: string
  salesOrderNo?: string
  merchantId?: string
  merchantName?: string
  customerId?: string
  customerName?: string
  subject?: string
  price?: string
  currency?: string
  status?: PayOrderStatus
  extensionId?: string
  channelOrderNo?: string
  expireTime?: string
  successTime?: string
  createTime?: string
}

export interface PayAttempt {
  id?: string
  no?: string
  orderId?: string
  channelCode?: PayMethod
  channelOrderNo?: string
  channelCaptureNo?: string
  bankTransferStatus?: string
  bankPayerName?: string
  bankReferenceNo?: string
  bankTransferTime?: string
  bankDeclaredPrice?: string
  bankCurrency?: string
  bankProofMediaId?: string
  bankSubmittedTime?: string
  bankReviewedBy?: string
  bankReviewedTime?: string
  bankRejectReason?: string
  channelErrorMsg?: string
  status?: number
  successTime?: string
  createTime?: string
}

export interface CreditAccount {
  creditAccountId?: string
  merchantId?: string
  merchantName?: string
  creditLimit?: DecimalValue
  usedCredit?: DecimalValue
  availableCredit?: DecimalValue
  currency?: string
  status?: string
  frozenReason?: string
  updateTime?: string
}

export interface CreditTransaction {
  creditTransactionId?: string
  creditAccountId?: string
  transactionNo?: string
  transactionType?: string
  businessType?: string
  businessNo?: string
  amount?: DecimalValue
  beforeCreditLimit?: DecimalValue
  afterCreditLimit?: DecimalValue
  beforeUsedCredit?: DecimalValue
  afterUsedCredit?: DecimalValue
  currency?: string
  operatorName?: string
  occurredTime?: string
  remark?: string
}

export interface BankCollectionAccount {
  bankAccountId?: string
  bankName?: string
  accountName?: string
  accountNumber?: string
  accountNumberMasked?: string
  swiftCode?: string
  routingNumber?: string
  currency?: string
  remark?: string
}

export interface EnabledPayChannel {
  channelId?: string
  appId?: string
  channelCode?: string
  feeRate?: DecimalValue
  remark?: string
}

export interface Receivable {
  receivableId?: string
  merchantName?: string
  salesOrderNo?: string
  payOrderNo?: string
  receivableAmount?: DecimalValue
  repaidAmount?: DecimalValue
  outstandingAmount?: DecimalValue
  currency?: string
  status?: string
  formedTime?: string
  dueDate?: string
  settledTime?: string
}

export interface PayWebhookSummary {
  channelEventId?: string
  eventType?: string
  channelOrderNo?: string
  channelCaptureNo?: string
  signatureStatus?: string
  processStatus?: string
  errorCode?: string
  errorMessage?: string
  receivedTime?: string
  processedTime?: string
}

export interface PayOrderDetail {
  payOrderId: string
  payOrderNo: string
  salesDocumentId?: string
  salesOrderNo?: string
  payerTenantId?: string
  merchantName?: string
  customerName?: string
  subject?: string
  price?: string
  currency?: string
  channelCode?: string
  status?: PayOrderStatus
  channelOrderNo?: string
  successTime?: string
  attempts: PayAttempt[]
  webhooks: PayWebhookSummary[]
  receivable?: Receivable
}

export interface SalesPayment {
  salesDocumentId: string
  orderNo: string
  tenantId: string
  customerName?: string
  projectName?: string
  totalAmount: DecimalValue
  currency: string
  documentStatus: string
  paymentStatus: string
  payOrder: PayOrder
  attempts: PayAttempt[]
  creditAccount?: CreditAccount
}

export interface PayOrderQuery extends PageQuery {
  no?: string
  merchantOrderId?: string
  channelCode?: string
  status?: PayOrderStatus | ''
}

export interface PayPalCheckout {
  payOrderId: string
  extensionId: string
  paypalOrderId: string
  captureId?: string
  status: string
  approvalUrl?: string
}

export interface BankSubmitRequest {
  payerName: string
  bankReference: string
  transferredTime: string
  declaredPrice: string
  currency: string
  proofMediaId: string
  remark?: string
}

export interface SupplementRequest {
  method: string
  price: string
  currency: string
  reference: string
  paidTime: string
  proofMediaId: string
  reason: string
}
