-- Payment module schema for PostgreSQL.
-- Time columns use timestamptz and follow project UTC storage semantics.
-- Platform/factory receiver is currently tenant_id = 1.

CREATE TABLE IF NOT EXISTS pay_app (
    id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    app_key varchar(64) NOT NULL,
    name varchar(64) NOT NULL,
    status char(1) DEFAULT '1',
    order_notify_url varchar(512),
    refund_notify_url varchar(512),
    transfer_notify_url varchar(512),
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_app_tenant_key ON pay_app (tenant_id, app_key);
CREATE INDEX IF NOT EXISTS idx_pay_app_tenant_status ON pay_app (tenant_id, status);

CREATE TABLE IF NOT EXISTS pay_channel (
    id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    code varchar(64) NOT NULL,
    status char(1) DEFAULT '1',
    fee_rate integer DEFAULT 0,
    app_id bigint NOT NULL,
    config jsonb NOT NULL DEFAULT '{}'::jsonb,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_channel_app_code ON pay_channel (app_id, code);
CREATE INDEX IF NOT EXISTS idx_pay_channel_tenant_status ON pay_channel (tenant_id, status, code);

CREATE TABLE IF NOT EXISTS pay_order (
    id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    payer_tenant_id bigint NOT NULL CHECK (payer_tenant_id <> 0),
    payee_tenant_id bigint NOT NULL CHECK (payee_tenant_id <> 0),
    app_id bigint NOT NULL,
    channel_id bigint,
    channel_code varchar(64),
    no varchar(64) NOT NULL,
    merchant_order_id varchar(128) NOT NULL,
    sales_document_id bigint,
    sales_order_no varchar(64),
    merchant_id bigint,
    merchant_name varchar(200),
    customer_id bigint,
    customer_name varchar(200),
    user_id bigint,
    user_type varchar(32),
    subject varchar(128) NOT NULL,
    body varchar(512),
    notify_url varchar(512),
    price bigint NOT NULL CHECK (price >= 0),
    refund_price bigint DEFAULT 0 CHECK (refund_price >= 0),
    currency varchar(3) NOT NULL DEFAULT 'CNY',
    channel_fee_rate integer DEFAULT 0,
    channel_fee_price bigint DEFAULT 0,
    status integer NOT NULL,
    user_ip varchar(64),
    expire_time timestamptz,
    success_time timestamptz,
    extension_id bigint,
    channel_user_id varchar(128),
    channel_order_no varchar(128),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
ALTER TABLE pay_order ADD COLUMN IF NOT EXISTS sales_document_id bigint;
ALTER TABLE pay_order ADD COLUMN IF NOT EXISTS sales_order_no varchar(64);
ALTER TABLE pay_order ADD COLUMN IF NOT EXISTS merchant_id bigint;
ALTER TABLE pay_order ADD COLUMN IF NOT EXISTS merchant_name varchar(200);
ALTER TABLE pay_order ADD COLUMN IF NOT EXISTS customer_id bigint;
ALTER TABLE pay_order ADD COLUMN IF NOT EXISTS customer_name varchar(200);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_order_no ON pay_order (no);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_order_app_merchant ON pay_order (app_id, merchant_order_id);
CREATE INDEX IF NOT EXISTS idx_pay_order_payer_status_time ON pay_order (payer_tenant_id, status, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_pay_order_payee_status_time ON pay_order (payee_tenant_id, status, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_pay_order_channel_order_no ON pay_order (channel_order_no) WHERE channel_order_no IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_pay_order_sales_document ON pay_order (sales_document_id) WHERE sales_document_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_pay_order_sales_order_no ON pay_order (payer_tenant_id, sales_order_no) WHERE sales_order_no IS NOT NULL;

CREATE TABLE IF NOT EXISTS pay_order_extension (
    id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    payer_tenant_id bigint NOT NULL CHECK (payer_tenant_id <> 0),
    payee_tenant_id bigint NOT NULL CHECK (payee_tenant_id <> 0),
    no varchar(64) NOT NULL,
    order_id bigint NOT NULL,
    channel_id bigint NOT NULL,
    channel_code varchar(64) NOT NULL,
    user_ip varchar(64),
    status integer NOT NULL,
    channel_extras jsonb NOT NULL DEFAULT '{}'::jsonb,
    request_id varchar(128),
    channel_order_no varchar(128),
    channel_capture_no varchar(128),
    bank_transfer_status varchar(32),
    bank_payer_name varchar(200),
    bank_reference_no varchar(128),
    bank_transfer_time timestamptz,
    bank_declared_price bigint CHECK (bank_declared_price IS NULL OR bank_declared_price >= 0),
    bank_currency varchar(3),
    bank_proof_media_id bigint,
    bank_submitted_time timestamptz,
    bank_reviewed_by_id bigint,
    bank_reviewed_by varchar(100),
    bank_reviewed_time timestamptz,
    bank_reject_reason varchar(1000),
    channel_error_code varchar(128),
    channel_error_msg varchar(512),
    success_time timestamptz,
    channel_notify_data text,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS request_id varchar(128);
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS channel_order_no varchar(128);
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS channel_capture_no varchar(128);
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS bank_transfer_status varchar(32);
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS bank_payer_name varchar(200);
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS bank_reference_no varchar(128);
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS bank_transfer_time timestamptz;
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS bank_declared_price bigint;
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS bank_currency varchar(3);
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS bank_proof_media_id bigint;
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS bank_submitted_time timestamptz;
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS bank_reviewed_by_id bigint;
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS bank_reviewed_by varchar(100);
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS bank_reviewed_time timestamptz;
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS bank_reject_reason varchar(1000);
ALTER TABLE pay_order_extension ADD COLUMN IF NOT EXISTS success_time timestamptz;
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_order_extension_no ON pay_order_extension (no);
CREATE INDEX IF NOT EXISTS idx_pay_order_extension_order ON pay_order_extension (order_id, status, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_pay_order_extension_channel_order ON pay_order_extension (channel_code, channel_order_no) WHERE channel_order_no IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_pay_order_extension_bank_review ON pay_order_extension (bank_transfer_status, bank_submitted_time) WHERE bank_transfer_status = 'PENDING_REVIEW';

CREATE TABLE IF NOT EXISTS pay_webhook_event (
    id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    app_id bigint NOT NULL,
    channel_id bigint,
    channel_code varchar(64) NOT NULL,
    channel_event_id varchar(128) NOT NULL,
    event_type varchar(128) NOT NULL,
    channel_order_no varchar(128),
    channel_capture_no varchar(128),
    order_id bigint,
    extension_id bigint,
    price bigint CHECK (price IS NULL OR price >= 0),
    currency varchar(3),
    signature_status varchar(20) NOT NULL DEFAULT 'PENDING' CHECK (signature_status IN ('PENDING', 'VERIFIED', 'FAILED')),
    process_status varchar(20) NOT NULL DEFAULT 'PENDING' CHECK (process_status IN ('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED', 'IGNORED')),
    received_time timestamptz NOT NULL DEFAULT now(),
    processed_time timestamptz,
    next_retry_time timestamptz,
    retry_count integer NOT NULL DEFAULT 0 CHECK (retry_count >= 0),
    raw_data_ref varchar(500),
    event_summary text,
    error_code varchar(128),
    error_message varchar(1000),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_webhook_channel_event ON pay_webhook_event (channel_code, channel_event_id);
CREATE INDEX IF NOT EXISTS idx_pay_webhook_order ON pay_webhook_event (order_id, received_time DESC) WHERE order_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_pay_webhook_retry ON pay_webhook_event (process_status, next_retry_time) WHERE process_status IN ('PENDING', 'FAILED');

CREATE TABLE IF NOT EXISTS merchant_credit_account (
    credit_account_id bigint PRIMARY KEY,
    business_origin varchar(20) NOT NULL CHECK (business_origin IN ('INTERNAL', 'MERCHANT')),
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    sales_store_id bigint,
    merchant_id bigint,
    merchant_name varchar(200),
    currency varchar(3) NOT NULL DEFAULT 'USD',
    credit_limit numeric(18,2) NOT NULL DEFAULT 0 CHECK (credit_limit >= 0),
    used_credit numeric(18,2) NOT NULL DEFAULT 0 CHECK (used_credit >= 0),
    status varchar(20) NOT NULL DEFAULT 'NORMAL' CHECK (status IN ('NORMAL', 'FROZEN', 'DISABLED')),
    version integer NOT NULL DEFAULT 0 CHECK (version >= 0),
    frozen_by_id bigint,
    frozen_by varchar(100),
    frozen_time timestamptz,
    frozen_reason varchar(1000),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(1000),
    CHECK (used_credit <= credit_limit)
);
DROP INDEX IF EXISTS uk_merchant_credit_account_tenant;
ALTER TABLE merchant_credit_account ADD COLUMN IF NOT EXISTS business_origin varchar(20);
ALTER TABLE merchant_credit_account ADD COLUMN IF NOT EXISTS sales_store_id bigint;
ALTER TABLE merchant_credit_account ALTER COLUMN merchant_id DROP NOT NULL;
UPDATE merchant_credit_account SET business_origin = CASE WHEN tenant_id = 1 THEN 'INTERNAL' ELSE 'MERCHANT' END
WHERE business_origin IS NULL;
ALTER TABLE merchant_credit_account ALTER COLUMN business_origin SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_merchant_credit_account_subject
    ON merchant_credit_account (business_origin, tenant_id, sales_store_id, currency, status);
CREATE UNIQUE INDEX IF NOT EXISTS uk_merchant_credit_account_merchant_subject
    ON merchant_credit_account (tenant_id, currency)
    WHERE business_origin = 'MERCHANT' AND sales_store_id IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_merchant_credit_account_internal_subject
    ON merchant_credit_account (sales_store_id, currency)
    WHERE business_origin = 'INTERNAL' AND sales_store_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_merchant_credit_account_merchant ON merchant_credit_account (merchant_id, status);

CREATE TABLE IF NOT EXISTS merchant_credit_transaction (
    credit_transaction_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    credit_account_id bigint NOT NULL,
    transaction_no varchar(64) NOT NULL,
    transaction_type varchar(20) NOT NULL CHECK (transaction_type IN ('OCCUPY', 'REPAY', 'RELEASE', 'ADJUST', 'FREEZE', 'UNFREEZE')),
    business_type varchar(32) NOT NULL,
    business_id bigint,
    business_no varchar(128),
    amount numeric(18,2) NOT NULL CHECK (amount >= 0),
    before_credit_limit numeric(18,2) NOT NULL,
    after_credit_limit numeric(18,2) NOT NULL,
    before_used_credit numeric(18,2) NOT NULL,
    after_used_credit numeric(18,2) NOT NULL,
    currency varchar(3) NOT NULL DEFAULT 'USD',
    operator_id bigint,
    operator_name varchar(100),
    occurred_time timestamptz NOT NULL,
    payment_method varchar(32),
    payment_reference varchar(128),
    paid_time timestamptz,
    proof_media_id bigint,
    idempotency_key varchar(128),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(1000)
);
ALTER TABLE merchant_credit_transaction ADD COLUMN IF NOT EXISTS update_by varchar(64);
ALTER TABLE merchant_credit_transaction ADD COLUMN IF NOT EXISTS update_time timestamptz;
ALTER TABLE merchant_credit_transaction ADD COLUMN IF NOT EXISTS payment_method varchar(32);
ALTER TABLE merchant_credit_transaction ADD COLUMN IF NOT EXISTS payment_reference varchar(128);
ALTER TABLE merchant_credit_transaction ADD COLUMN IF NOT EXISTS paid_time timestamptz;
ALTER TABLE merchant_credit_transaction ADD COLUMN IF NOT EXISTS proof_media_id bigint;
ALTER TABLE merchant_credit_transaction ADD COLUMN IF NOT EXISTS idempotency_key varchar(128);
CREATE UNIQUE INDEX IF NOT EXISTS uk_merchant_credit_transaction_no ON merchant_credit_transaction (transaction_no);
CREATE INDEX IF NOT EXISTS idx_merchant_credit_transaction_account ON merchant_credit_transaction (tenant_id, credit_account_id, occurred_time DESC);
CREATE INDEX IF NOT EXISTS idx_merchant_credit_transaction_business ON merchant_credit_transaction (business_type, business_id);
CREATE INDEX IF NOT EXISTS idx_merchant_credit_transaction_idempotency
    ON merchant_credit_transaction (business_type, business_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE TABLE IF NOT EXISTS merchant_receivable (
    receivable_id bigint PRIMARY KEY,
    business_origin varchar(20) NOT NULL CHECK (business_origin IN ('INTERNAL', 'MERCHANT')),
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    sales_store_id bigint,
    merchant_id bigint,
    merchant_name varchar(200),
    receivable_no varchar(64) NOT NULL,
    sales_document_id bigint NOT NULL,
    sales_order_no varchar(64) NOT NULL,
    pay_order_id bigint NOT NULL,
    pay_order_no varchar(64) NOT NULL,
    credit_account_id bigint NOT NULL,
    receivable_amount numeric(18,2) NOT NULL CHECK (receivable_amount > 0),
    repaid_amount numeric(18,2) NOT NULL DEFAULT 0 CHECK (repaid_amount >= 0),
    outstanding_amount numeric(18,2) NOT NULL CHECK (outstanding_amount >= 0),
    currency varchar(3) NOT NULL DEFAULT 'USD',
    formed_time timestamptz NOT NULL,
    due_date date NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'UNPAID' CHECK (status IN ('UNPAID', 'PARTIALLY_PAID', 'SETTLED', 'OVERDUE', 'CLOSED')),
    settled_time timestamptz,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(1000),
    CHECK (repaid_amount + outstanding_amount = receivable_amount)
);
ALTER TABLE merchant_receivable ADD COLUMN IF NOT EXISTS business_origin varchar(20);
ALTER TABLE merchant_receivable ADD COLUMN IF NOT EXISTS sales_store_id bigint;
ALTER TABLE merchant_receivable ALTER COLUMN merchant_id DROP NOT NULL;
UPDATE merchant_receivable SET business_origin = CASE WHEN tenant_id = 1 THEN 'INTERNAL' ELSE 'MERCHANT' END
WHERE business_origin IS NULL;
ALTER TABLE merchant_receivable ALTER COLUMN business_origin SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_merchant_receivable_no ON merchant_receivable (receivable_no);
CREATE INDEX IF NOT EXISTS idx_merchant_receivable_sales ON merchant_receivable (sales_document_id, pay_order_id);
CREATE INDEX IF NOT EXISTS idx_merchant_receivable_tenant_status ON merchant_receivable (tenant_id, status, due_date);
CREATE INDEX IF NOT EXISTS idx_merchant_receivable_subject
    ON merchant_receivable (business_origin, tenant_id, sales_store_id, status, due_date);

CREATE TABLE IF NOT EXISTS pay_reconciliation_case (
    case_id bigint PRIMARY KEY,
    case_no varchar(64) NOT NULL,
    business_origin varchar(20) NOT NULL CHECK (business_origin IN ('INTERNAL', 'MERCHANT')),
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    sales_store_id bigint,
    pay_order_id bigint NOT NULL,
    extension_id bigint,
    webhook_event_id bigint,
    sales_document_id bigint,
    anomaly_type varchar(64) NOT NULL,
    severity varchar(20) NOT NULL CHECK (severity IN ('WARNING', 'CRITICAL')),
    status varchar(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'RESOLVED', 'IGNORED')),
    detected_time timestamptz NOT NULL,
    last_checked_time timestamptz,
    diagnosis_code varchar(128),
    diagnosis_message varchar(1000),
    expected_snapshot_json text,
    actual_snapshot_json text,
    resolved_by_id bigint,
    resolved_by varchar(100),
    resolved_time timestamptz,
    resolution_code varchar(128),
    version integer NOT NULL DEFAULT 0 CHECK (version >= 0),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE INDEX IF NOT EXISTS idx_pay_reconciliation_case_status
    ON pay_reconciliation_case (status, severity, detected_time DESC);
CREATE INDEX IF NOT EXISTS idx_pay_reconciliation_case_order
    ON pay_reconciliation_case (pay_order_id, anomaly_type, status);
CREATE INDEX IF NOT EXISTS idx_pay_reconciliation_case_scope
    ON pay_reconciliation_case (business_origin, tenant_id, sales_store_id, status);

CREATE TABLE IF NOT EXISTS pay_reconciliation_action (
    action_id bigint PRIMARY KEY,
    case_id bigint NOT NULL,
    action_type varchar(32) NOT NULL,
    before_snapshot_json text,
    after_snapshot_json text,
    result varchar(32) NOT NULL,
    result_code varchar(128),
    result_message varchar(1000),
    operator_id bigint,
    operator_name varchar(100),
    occurred_time timestamptz NOT NULL,
    reason varchar(1000),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE INDEX IF NOT EXISTS idx_pay_reconciliation_action_case
    ON pay_reconciliation_action (case_id, occurred_time DESC);

-- =====================================================
-- 阶段 12 最终菜单：业务财务中心与平台财务
-- =====================================================
DELETE FROM sys_role_menu WHERE menu_id IN (26023) OR menu_id BETWEEN 26101 AND 26115;
DELETE FROM sys_menu WHERE menu_id BETWEEN 26101 AND 26115 OR menu_id = 26023;

INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (27000, 1, '财务中心', 'finance.business.menu', 0, 50, 'finance', NULL, '1', '0', 'M', '1', '1', NULL, 'finance', 'system', now(), '内部销售财务查询'),
    (27001, 1, '付款记录', 'finance.business.payments', 27000, 1, 'payments', 'pay/business-payments', '1', '0', 'C', '1', '1', 'pay:order:list', 'money', 'system', now(), '阶段 12 薄壳'),
    (27002, 1, '信用账户', 'finance.business.creditAccounts', 27000, 2, 'creditAccounts', 'pay/business-credit-accounts', '1', '0', 'C', '1', '1', 'pay:credit:query', 'finance', 'system', now(), '阶段 12 薄壳'),
    (27003, 1, '应付账单', 'finance.business.receivables', 27000, 3, 'receivables', 'pay/business-receivables', '1', '0', 'C', '1', '1', 'pay:receivable:list', 'invoice', 'system', now(), '阶段 12 薄壳'),
    (27100, 1, '平台财务', 'platform.finance.menu', 0, 300, 'platformFinance', NULL, '1', '0', 'M', '1', '1', NULL, 'finance', 'system', now(), '平台财务'),
    (27101, 1, '支付单据', 'platform.finance.payments', 27100, 1, 'payments', 'pay/platform-payments', '1', '0', 'C', '1', '1', 'platform:finance:payment:list', 'money', 'system', now(), '阶段 12 薄壳'),
    (27102, 1, '转账审核', 'platform.finance.bankTransfers', 27100, 2, 'bankTransfers', 'pay/platform-bank-transfers', '1', '0', 'C', '1', '1', 'platform:finance:bank:list', 'check', 'system', now(), '阶段 12 薄壳'),
    (27103, 1, '信用账户', 'platform.finance.creditAccounts', 27100, 3, 'creditAccounts', 'pay/platform-credit-accounts', '1', '0', 'C', '1', '1', 'platform:finance:credit:list', 'finance', 'system', now(), '阶段 12 薄壳'),
    (27104, 1, '信用应收', 'platform.finance.receivables', 27100, 4, 'receivables', 'pay/platform-receivables', '1', '0', 'C', '1', '1', 'platform:finance:receivable:list', 'invoice', 'system', now(), '阶段 12 薄壳'),
    (27105, 1, '异常对账', 'platform.finance.reconciliationMenu', 27100, 5, 'reconciliation', 'pay/platform-reconciliation', '1', '0', 'C', '1', '1', 'platform:finance:reconciliation:list', 'report', 'system', now(), '阶段 12 薄壳')
ON CONFLICT (menu_id) DO UPDATE
SET tenant_id = EXCLUDED.tenant_id, menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id, order_num = EXCLUDED.order_num, path = EXCLUDED.path,
    component = EXCLUDED.component, menu_type = EXCLUDED.menu_type, visible = EXCLUDED.visible,
    status = EXCLUDED.status, perms = EXCLUDED.perms, icon = EXCLUDED.icon,
    update_by = 'system', update_time = now(), remark = EXCLUDED.remark;

INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT x.menu_id, 1, x.menu_name, x.i18n_key, x.parent_id, x.order_num, '#', '',
       '1', '0', 'F', '1', '1', x.perms, '#', 'system', now(), ''
FROM (VALUES
    (27011, '付款查询', 'finance.business.payment.query', 27001, 1, 'pay:order:query'),
    (27012, 'PayPal支付', 'finance.business.payment.paypal', 27001, 2, 'pay:order:submit'),
    (27013, '银行转账', 'finance.business.payment.bank', 27001, 3, 'pay:bank:submit'),
    (27014, '信用支付', 'finance.business.payment.credit', 27001, 4, 'pay:credit:use'),
    (27021, '信用查询', 'finance.business.credit.query', 27002, 1, 'pay:credit:query'),
    (27031, '账单查询', 'finance.business.receivable.query', 27003, 1, 'pay:receivable:query'),
    (27111, '支付查询', 'platform.finance.payment.query', 27101, 1, 'platform:finance:payment:query'),
    (27112, '付款补录', 'platform.finance.payment.supplement', 27101, 2, 'platform:finance:payment:supplement'),
    (27121, '转账查询', 'platform.finance.bank.query', 27102, 1, 'platform:finance:bank:query'),
    (27122, '转账审核', 'platform.finance.bank.review', 27102, 2, 'platform:finance:bank:review'),
    (27131, '信用查询', 'platform.finance.credit.query', 27103, 1, 'platform:finance:credit:query'),
    (27132, '额度调整', 'platform.finance.credit.adjust', 27103, 2, 'platform:finance:credit:adjust'),
    (27133, '账户冻结', 'platform.finance.credit.freeze', 27103, 3, 'platform:finance:credit:freeze'),
    (27134, '账户同步', 'platform.finance.credit.sync', 27103, 4, 'platform:finance:credit:sync'),
    (27141, '应收查询', 'platform.finance.receivable.query', 27104, 1, 'platform:finance:receivable:query'),
    (27142, '信用回款', 'platform.finance.receivable.repay', 27104, 2, 'platform:finance:receivable:repay'),
    (27151, '对账查询', 'platform.finance.reconciliation.query', 27105, 1, 'platform:finance:reconciliation:query'),
    (27152, '重新扫描', 'platform.finance.reconciliation.rescan', 27105, 2, 'platform:finance:reconciliation:rescan'),
    (27153, '渠道事件', 'platform.finance.reconciliation.channel', 27105, 3, 'platform:finance:reconciliation:channel'),
    (27154, '支付修复', 'platform.finance.reconciliation.repair', 27105, 4, 'platform:finance:reconciliation:repair'),
    (27155, '忽略异常', 'platform.finance.reconciliation.ignore', 27105, 5, 'platform:finance:reconciliation:ignore'),
    (27156, '商家选项', 'platform.finance.merchant.options', 27100, 90, 'system:merchant:profile:options'),
    (27157, '门店选项', 'platform.finance.store.options', 27100, 91, 'system:sales-store:options')
) AS x(menu_id, menu_name, i18n_key, parent_id, order_num, perms)
ON CONFLICT (menu_id) DO UPDATE
SET tenant_id = EXCLUDED.tenant_id, menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id, order_num = EXCLUDED.order_num, perms = EXCLUDED.perms,
    status = EXCLUDED.status, update_by = 'system', update_time = now();

INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT t.tenant_id * 1000 + x.offset_id, t.tenant_id, x.menu_name, x.i18n_key,
       CASE WHEN x.parent_offset = 0 THEN 0 ELSE t.tenant_id * 1000 + x.parent_offset END,
       x.order_num, x.path, x.component, '1', '0', x.menu_type, '1', '1', x.perms, x.icon,
       'system', now(), '商家财务菜单'
FROM sys_tenant t
CROSS JOIN (VALUES
    (500, 0, '财务中心', 'finance.business.menu', 50, 'finance', NULL, 'M', NULL, 'finance'),
    (510, 500, '付款记录', 'finance.business.payments', 1, 'payments', 'pay/business-payments', 'C', 'pay:order:list', 'money'),
    (520, 500, '信用账户', 'finance.business.creditAccounts', 2, 'creditAccounts', 'pay/business-credit-accounts', 'C', 'pay:credit:query', 'finance'),
    (530, 500, '应付账单', 'finance.business.receivables', 3, 'receivables', 'pay/business-receivables', 'C', 'pay:receivable:list', 'invoice')
) AS x(offset_id, parent_offset, menu_name, i18n_key, order_num, path, component, menu_type, perms, icon)
WHERE t.tenant_type = 'MERCHANT'
ON CONFLICT (menu_id) DO UPDATE
SET tenant_id = EXCLUDED.tenant_id, menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id, order_num = EXCLUDED.order_num, path = EXCLUDED.path,
    component = EXCLUDED.component, menu_type = EXCLUDED.menu_type, visible = EXCLUDED.visible,
    status = EXCLUDED.status, perms = EXCLUDED.perms, icon = EXCLUDED.icon,
    update_by = 'system', update_time = now(), remark = EXCLUDED.remark;

INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT t.tenant_id * 1000 + x.offset_id, t.tenant_id, x.menu_name, x.i18n_key,
       t.tenant_id * 1000 + x.parent_offset, x.order_num, '#', '', '1', '0', 'F', '1', '1',
       x.perms, '#', 'system', now(), ''
FROM sys_tenant t
CROSS JOIN (VALUES
    (511, 510, 1, '付款查询', 'finance.business.payment.query', 'pay:order:query'),
    (512, 510, 2, 'PayPal支付', 'finance.business.payment.paypal', 'pay:order:submit'),
    (513, 510, 3, '银行转账', 'finance.business.payment.bank', 'pay:bank:submit'),
    (514, 510, 4, '信用支付', 'finance.business.payment.credit', 'pay:credit:use'),
    (521, 520, 1, '信用查询', 'finance.business.credit.query', 'pay:credit:query'),
    (531, 530, 1, '账单查询', 'finance.business.receivable.query', 'pay:receivable:query')
) AS x(offset_id, parent_offset, order_num, menu_name, i18n_key, perms)
WHERE t.tenant_type = 'MERCHANT'
ON CONFLICT (menu_id) DO UPDATE
SET tenant_id = EXCLUDED.tenant_id, menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id, order_num = EXCLUDED.order_num, perms = EXCLUDED.perms,
    status = EXCLUDED.status, update_by = 'system', update_time = now();

INSERT INTO sys_role (role_id, tenant_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES (260903, 1, '平台财务', 'platform_finance', 33, '1', true, true, '1', '0', 'system', now(), '平台财务管理')
ON CONFLICT (role_id) DO UPDATE
SET role_name = EXCLUDED.role_name, role_key = EXCLUDED.role_key, role_sort = EXCLUDED.role_sort,
    data_scope = EXCLUDED.data_scope, status = EXCLUDED.status, del_flag = EXCLUDED.del_flag,
    update_by = 'system', update_time = now(), remark = EXCLUDED.remark;

DELETE FROM sys_role_menu rm
USING sys_role r
WHERE rm.role_id = r.role_id
  AND r.role_key = 'platform_finance';

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT r.role_id, m.menu_id, r.tenant_id
FROM sys_role r
JOIN sys_menu m ON m.tenant_id = r.tenant_id
WHERE (r.role_key = 'platform_finance'
       AND (m.menu_id = 27100 OR m.parent_id IN (27100, 27101, 27102, 27103, 27104, 27105)))
   OR (r.role_key IN ('merchant_admin', 'merchant_employee') AND r.tenant_id <> 1
       AND (m.menu_id = r.tenant_id * 1000 + 500
            OR m.parent_id IN (r.tenant_id * 1000 + 500, r.tenant_id * 1000 + 510,
                               r.tenant_id * 1000 + 520, r.tenant_id * 1000 + 530)))
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT 1, menu_id, 1
FROM sys_menu
WHERE tenant_id = 1
  AND (menu_id IN (27000, 27100) OR parent_id BETWEEN 27000 AND 27199)
ON CONFLICT DO NOTHING;

UPDATE sys_role r
SET default_menu_id = m.menu_id, update_by = 'system', update_time = now()
FROM sys_menu m
WHERE m.tenant_id = r.tenant_id
  AND m.menu_type = 'C'
  AND m.status = '1'
  AND m.visible = '1'
  AND r.role_key = 'platform_finance'
  AND m.component = 'pay/platform-payments';

CREATE TABLE IF NOT EXISTS pay_refund (
    id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    payer_tenant_id bigint NOT NULL CHECK (payer_tenant_id <> 0),
    payee_tenant_id bigint NOT NULL CHECK (payee_tenant_id <> 0),
    no varchar(64) NOT NULL,
    app_id bigint NOT NULL,
    channel_id bigint,
    channel_code varchar(64),
    order_id bigint NOT NULL,
    order_no varchar(64) NOT NULL,
    merchant_order_id varchar(128) NOT NULL,
    merchant_refund_id varchar(128) NOT NULL,
    notify_url varchar(512),
    status integer NOT NULL,
    pay_price bigint NOT NULL CHECK (pay_price >= 0),
    refund_price bigint NOT NULL CHECK (refund_price >= 0),
    currency varchar(3) NOT NULL DEFAULT 'CNY',
    reason varchar(256),
    user_ip varchar(64),
    channel_order_no varchar(128),
    channel_refund_no varchar(128),
    success_time timestamptz,
    channel_error_code varchar(128),
    channel_error_msg varchar(512),
    channel_notify_data text,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_refund_no ON pay_refund (no);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_refund_app_merchant ON pay_refund (app_id, merchant_refund_id);
CREATE INDEX IF NOT EXISTS idx_pay_refund_order ON pay_refund (order_id, status, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_pay_refund_payer_status_time ON pay_refund (payer_tenant_id, status, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_pay_refund_payee_status_time ON pay_refund (payee_tenant_id, status, create_time DESC);

CREATE TABLE IF NOT EXISTS pay_notify_task (
    id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    payer_tenant_id bigint CHECK (payer_tenant_id IS NULL OR payer_tenant_id <> 0),
    payee_tenant_id bigint NOT NULL CHECK (payee_tenant_id <> 0),
    app_id bigint NOT NULL,
    type integer NOT NULL,
    data_id bigint NOT NULL,
    merchant_order_id varchar(128),
    merchant_refund_id varchar(128),
    merchant_transfer_id varchar(128),
    status integer NOT NULL,
    next_notify_time timestamptz,
    last_execute_time timestamptz,
    notify_times integer DEFAULT 0,
    max_notify_times integer DEFAULT 9,
    notify_url varchar(512) NOT NULL,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE INDEX IF NOT EXISTS idx_pay_notify_task_status_next ON pay_notify_task (status, next_notify_time);
CREATE INDEX IF NOT EXISTS idx_pay_notify_task_data ON pay_notify_task (type, data_id);

CREATE TABLE IF NOT EXISTS pay_notify_log (
    id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    task_id bigint NOT NULL,
    notify_times integer NOT NULL,
    response text,
    status integer NOT NULL,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE INDEX IF NOT EXISTS idx_pay_notify_log_task_time ON pay_notify_log (task_id, create_time DESC);

CREATE TABLE IF NOT EXISTS pay_transfer (
    id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    payee_tenant_id bigint NOT NULL CHECK (payee_tenant_id <> 0),
    no varchar(64) NOT NULL,
    app_id bigint NOT NULL,
    channel_id bigint,
    channel_code varchar(64),
    merchant_transfer_id varchar(128) NOT NULL,
    subject varchar(128) NOT NULL,
    price bigint NOT NULL CHECK (price >= 0),
    user_account varchar(128) NOT NULL,
    user_name varchar(128),
    status integer NOT NULL,
    success_time timestamptz,
    notify_url varchar(512),
    user_ip varchar(64),
    channel_extras jsonb NOT NULL DEFAULT '{}'::jsonb,
    channel_transfer_no varchar(128),
    channel_error_code varchar(128),
    channel_error_msg varchar(512),
    channel_notify_data text,
    channel_package_info text,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_transfer_no ON pay_transfer (no);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_transfer_app_merchant ON pay_transfer (app_id, merchant_transfer_id);
CREATE INDEX IF NOT EXISTS idx_pay_transfer_payee_status_time ON pay_transfer (payee_tenant_id, status, create_time DESC);

CREATE TABLE IF NOT EXISTS pay_wallet (
    id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    user_id bigint NOT NULL,
    user_type varchar(32) NOT NULL,
    balance bigint DEFAULT 0 CHECK (balance >= 0),
    total_expense bigint DEFAULT 0 CHECK (total_expense >= 0),
    total_recharge bigint DEFAULT 0 CHECK (total_recharge >= 0),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_wallet_tenant_user ON pay_wallet (tenant_id, user_id, user_type);

CREATE TABLE IF NOT EXISTS pay_wallet_transaction (
    id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    wallet_id bigint NOT NULL,
    no varchar(64) NOT NULL,
    title varchar(128) NOT NULL,
    price bigint NOT NULL,
    balance bigint NOT NULL CHECK (balance >= 0),
    biz_type integer NOT NULL,
    biz_id varchar(128) NOT NULL,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_wallet_transaction_no ON pay_wallet_transaction (no);
CREATE INDEX IF NOT EXISTS idx_pay_wallet_transaction_wallet_time ON pay_wallet_transaction (wallet_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_pay_wallet_transaction_biz ON pay_wallet_transaction (biz_type, biz_id);

CREATE TABLE IF NOT EXISTS pay_wallet_recharge (
    id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    wallet_id bigint NOT NULL,
    no varchar(64) NOT NULL,
    pay_price bigint NOT NULL CHECK (pay_price >= 0),
    bonus_price bigint DEFAULT 0 CHECK (bonus_price >= 0),
    pay_order_id bigint,
    pay_status integer NOT NULL,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_wallet_recharge_no ON pay_wallet_recharge (no);
CREATE INDEX IF NOT EXISTS idx_pay_wallet_recharge_wallet_time ON pay_wallet_recharge (wallet_id, create_time DESC);

CREATE TABLE IF NOT EXISTS pay_wallet_recharge_package (
    id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    name varchar(64) NOT NULL,
    pay_price bigint NOT NULL CHECK (pay_price >= 0),
    bonus_price bigint DEFAULT 0 CHECK (bonus_price >= 0),
    status char(1) DEFAULT '1',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE INDEX IF NOT EXISTS idx_pay_wallet_recharge_package_status ON pay_wallet_recharge_package (tenant_id, status);

COMMENT ON TABLE pay_app IS '支付应用';
COMMENT ON COLUMN pay_app.id IS '支付应用ID';
COMMENT ON COLUMN pay_app.tenant_id IS '租户ID，当前为收款方平台/厂家租户';
COMMENT ON COLUMN pay_app.app_key IS '支付应用标识';
COMMENT ON COLUMN pay_app.name IS '支付应用名称';
COMMENT ON COLUMN pay_app.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pay_app.order_notify_url IS '支付订单通知地址';
COMMENT ON COLUMN pay_app.refund_notify_url IS '退款通知地址';
COMMENT ON COLUMN pay_app.transfer_notify_url IS '转账通知地址';
COMMENT ON COLUMN pay_app.remark IS '备注';
COMMENT ON COLUMN pay_app.create_by_id IS '创建者ID';
COMMENT ON COLUMN pay_app.create_by IS '创建者';
COMMENT ON COLUMN pay_app.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pay_app.update_by IS '更新者';
COMMENT ON COLUMN pay_app.update_time IS '更新时间，UTC timestamptz';

COMMENT ON TABLE pay_channel IS '支付渠道配置';
COMMENT ON COLUMN pay_channel.id IS '支付渠道ID';
COMMENT ON COLUMN pay_channel.tenant_id IS '租户ID，当前为收款方平台/厂家租户';
COMMENT ON COLUMN pay_channel.code IS '支付渠道代码';
COMMENT ON COLUMN pay_channel.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pay_channel.fee_rate IS '渠道手续费率，万分比';
COMMENT ON COLUMN pay_channel.app_id IS '支付应用ID';
COMMENT ON COLUMN pay_channel.config IS '渠道配置JSONB，接口和日志输出必须脱敏';
COMMENT ON COLUMN pay_channel.remark IS '备注';
COMMENT ON COLUMN pay_channel.create_by_id IS '创建者ID';
COMMENT ON COLUMN pay_channel.create_by IS '创建者';
COMMENT ON COLUMN pay_channel.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pay_channel.update_by IS '更新者';
COMMENT ON COLUMN pay_channel.update_time IS '更新时间，UTC timestamptz';

COMMENT ON TABLE pay_order IS '支付订单';
COMMENT ON COLUMN pay_order.id IS '支付订单ID';
COMMENT ON COLUMN pay_order.tenant_id IS '租户ID，默认等于付款方租户ID用于租户拦截';
COMMENT ON COLUMN pay_order.payer_tenant_id IS '付款方商户租户ID';
COMMENT ON COLUMN pay_order.payee_tenant_id IS '收款方平台/厂家租户ID';
COMMENT ON COLUMN pay_order.app_id IS '支付应用ID';
COMMENT ON COLUMN pay_order.channel_id IS '支付渠道ID';
COMMENT ON COLUMN pay_order.channel_code IS '支付渠道代码';
COMMENT ON COLUMN pay_order.no IS '支付订单号';
COMMENT ON COLUMN pay_order.merchant_order_id IS '商户订单号';
COMMENT ON COLUMN pay_order.sales_document_id IS '结构化关联销售订单ID';
COMMENT ON COLUMN pay_order.sales_order_no IS '销售订单号';
COMMENT ON COLUMN pay_order.user_id IS '付款用户ID';
COMMENT ON COLUMN pay_order.user_type IS '付款用户类型';
COMMENT ON COLUMN pay_order.subject IS '订单标题';
COMMENT ON COLUMN pay_order.body IS '订单描述';
COMMENT ON COLUMN pay_order.notify_url IS '业务通知地址';
COMMENT ON COLUMN pay_order.price IS '支付金额，单位分';
COMMENT ON COLUMN pay_order.refund_price IS '已退款金额，单位分';
COMMENT ON COLUMN pay_order.currency IS '币种，ISO 4217';
COMMENT ON COLUMN pay_order.channel_fee_rate IS '渠道手续费率，万分比';
COMMENT ON COLUMN pay_order.channel_fee_price IS '渠道手续费，单位分';
COMMENT ON COLUMN pay_order.status IS '支付订单状态';
COMMENT ON COLUMN pay_order.user_ip IS '付款用户IP';
COMMENT ON COLUMN pay_order.expire_time IS '过期时间，UTC timestamptz';
COMMENT ON COLUMN pay_order.success_time IS '支付成功时间，UTC timestamptz';
COMMENT ON COLUMN pay_order.extension_id IS '当前支付提交记录ID';
COMMENT ON COLUMN pay_order.channel_user_id IS '渠道用户ID';
COMMENT ON COLUMN pay_order.channel_order_no IS '渠道订单号';
COMMENT ON COLUMN pay_order.create_by_id IS '创建者ID';
COMMENT ON COLUMN pay_order.create_by IS '创建者';
COMMENT ON COLUMN pay_order.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pay_order.update_by IS '更新者';
COMMENT ON COLUMN pay_order.update_time IS '更新时间，UTC timestamptz';

COMMENT ON TABLE pay_order_extension IS '支付提交记录';
COMMENT ON COLUMN pay_order_extension.id IS '支付提交记录ID';
COMMENT ON COLUMN pay_order_extension.tenant_id IS '租户ID，默认等于付款方租户ID用于租户拦截';
COMMENT ON COLUMN pay_order_extension.payer_tenant_id IS '付款方商户租户ID';
COMMENT ON COLUMN pay_order_extension.payee_tenant_id IS '收款方平台/厂家租户ID';
COMMENT ON COLUMN pay_order_extension.no IS '支付提交单号';
COMMENT ON COLUMN pay_order_extension.order_id IS '支付订单ID';
COMMENT ON COLUMN pay_order_extension.channel_id IS '支付渠道ID';
COMMENT ON COLUMN pay_order_extension.channel_code IS '支付渠道代码';
COMMENT ON COLUMN pay_order_extension.user_ip IS '付款用户IP';
COMMENT ON COLUMN pay_order_extension.status IS '支付提交状态';
COMMENT ON COLUMN pay_order_extension.channel_extras IS '渠道扩展参数JSONB';
COMMENT ON COLUMN pay_order_extension.channel_error_code IS '渠道错误码';
COMMENT ON COLUMN pay_order_extension.channel_error_msg IS '渠道错误信息';
COMMENT ON COLUMN pay_order_extension.channel_notify_data IS '渠道回调原始数据';
COMMENT ON COLUMN pay_order_extension.create_by_id IS '创建者ID';
COMMENT ON COLUMN pay_order_extension.create_by IS '创建者';
COMMENT ON COLUMN pay_order_extension.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pay_order_extension.update_by IS '更新者';
COMMENT ON COLUMN pay_order_extension.update_time IS '更新时间，UTC timestamptz';

COMMENT ON TABLE pay_refund IS '支付退款';
COMMENT ON COLUMN pay_refund.id IS '退款ID';
COMMENT ON COLUMN pay_refund.tenant_id IS '租户ID，默认等于付款方租户ID用于租户拦截';
COMMENT ON COLUMN pay_refund.payer_tenant_id IS '付款方商户租户ID';
COMMENT ON COLUMN pay_refund.payee_tenant_id IS '收款方平台/厂家租户ID';
COMMENT ON COLUMN pay_refund.no IS '退款单号';
COMMENT ON COLUMN pay_refund.app_id IS '支付应用ID';
COMMENT ON COLUMN pay_refund.channel_id IS '支付渠道ID';
COMMENT ON COLUMN pay_refund.channel_code IS '支付渠道代码';
COMMENT ON COLUMN pay_refund.order_id IS '支付订单ID';
COMMENT ON COLUMN pay_refund.order_no IS '支付订单号';
COMMENT ON COLUMN pay_refund.merchant_order_id IS '商户订单号';
COMMENT ON COLUMN pay_refund.merchant_refund_id IS '商户退款单号';
COMMENT ON COLUMN pay_refund.notify_url IS '业务通知地址';
COMMENT ON COLUMN pay_refund.status IS '退款状态';
COMMENT ON COLUMN pay_refund.pay_price IS '原支付金额，单位分';
COMMENT ON COLUMN pay_refund.refund_price IS '退款金额，单位分';
COMMENT ON COLUMN pay_refund.currency IS '币种，ISO 4217';
COMMENT ON COLUMN pay_refund.reason IS '退款原因';
COMMENT ON COLUMN pay_refund.user_ip IS '操作用户IP';
COMMENT ON COLUMN pay_refund.channel_order_no IS '渠道订单号';
COMMENT ON COLUMN pay_refund.channel_refund_no IS '渠道退款单号';
COMMENT ON COLUMN pay_refund.success_time IS '退款成功时间，UTC timestamptz';
COMMENT ON COLUMN pay_refund.channel_error_code IS '渠道错误码';
COMMENT ON COLUMN pay_refund.channel_error_msg IS '渠道错误信息';
COMMENT ON COLUMN pay_refund.channel_notify_data IS '渠道回调原始数据';
COMMENT ON COLUMN pay_refund.create_by_id IS '创建者ID';
COMMENT ON COLUMN pay_refund.create_by IS '创建者';
COMMENT ON COLUMN pay_refund.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pay_refund.update_by IS '更新者';
COMMENT ON COLUMN pay_refund.update_time IS '更新时间，UTC timestamptz';

COMMENT ON TABLE pay_notify_task IS '支付通知任务';
COMMENT ON COLUMN pay_notify_task.id IS '通知任务ID';
COMMENT ON COLUMN pay_notify_task.tenant_id IS '租户ID';
COMMENT ON COLUMN pay_notify_task.payer_tenant_id IS '付款方商户租户ID';
COMMENT ON COLUMN pay_notify_task.payee_tenant_id IS '收款方平台/厂家租户ID';
COMMENT ON COLUMN pay_notify_task.app_id IS '支付应用ID';
COMMENT ON COLUMN pay_notify_task.type IS '通知类型';
COMMENT ON COLUMN pay_notify_task.data_id IS '通知数据ID';
COMMENT ON COLUMN pay_notify_task.merchant_order_id IS '商户订单号';
COMMENT ON COLUMN pay_notify_task.merchant_refund_id IS '商户退款单号';
COMMENT ON COLUMN pay_notify_task.merchant_transfer_id IS '商户转账单号';
COMMENT ON COLUMN pay_notify_task.status IS '通知状态';
COMMENT ON COLUMN pay_notify_task.next_notify_time IS '下次通知时间，UTC timestamptz';
COMMENT ON COLUMN pay_notify_task.last_execute_time IS '最后执行时间，UTC timestamptz';
COMMENT ON COLUMN pay_notify_task.notify_times IS '已通知次数';
COMMENT ON COLUMN pay_notify_task.max_notify_times IS '最大通知次数';
COMMENT ON COLUMN pay_notify_task.notify_url IS '通知地址';
COMMENT ON COLUMN pay_notify_task.create_by_id IS '创建者ID';
COMMENT ON COLUMN pay_notify_task.create_by IS '创建者';
COMMENT ON COLUMN pay_notify_task.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pay_notify_task.update_by IS '更新者';
COMMENT ON COLUMN pay_notify_task.update_time IS '更新时间，UTC timestamptz';

COMMENT ON TABLE pay_notify_log IS '支付通知日志';
COMMENT ON COLUMN pay_notify_log.id IS '通知日志ID';
COMMENT ON COLUMN pay_notify_log.tenant_id IS '租户ID';
COMMENT ON COLUMN pay_notify_log.task_id IS '通知任务ID';
COMMENT ON COLUMN pay_notify_log.notify_times IS '通知次数';
COMMENT ON COLUMN pay_notify_log.response IS '通知响应内容';
COMMENT ON COLUMN pay_notify_log.status IS '通知状态';
COMMENT ON COLUMN pay_notify_log.create_by_id IS '创建者ID';
COMMENT ON COLUMN pay_notify_log.create_by IS '创建者';
COMMENT ON COLUMN pay_notify_log.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pay_notify_log.update_by IS '更新者';
COMMENT ON COLUMN pay_notify_log.update_time IS '更新时间，UTC timestamptz';

COMMENT ON TABLE pay_transfer IS '支付转账';
COMMENT ON COLUMN pay_transfer.id IS '转账ID';
COMMENT ON COLUMN pay_transfer.tenant_id IS '租户ID';
COMMENT ON COLUMN pay_transfer.payee_tenant_id IS '收款方平台/厂家租户ID';
COMMENT ON COLUMN pay_transfer.no IS '转账单号';
COMMENT ON COLUMN pay_transfer.app_id IS '支付应用ID';
COMMENT ON COLUMN pay_transfer.channel_id IS '支付渠道ID';
COMMENT ON COLUMN pay_transfer.channel_code IS '支付渠道代码';
COMMENT ON COLUMN pay_transfer.merchant_transfer_id IS '商户转账单号';
COMMENT ON COLUMN pay_transfer.subject IS '转账标题';
COMMENT ON COLUMN pay_transfer.price IS '转账金额，单位分';
COMMENT ON COLUMN pay_transfer.user_account IS '收款账号';
COMMENT ON COLUMN pay_transfer.user_name IS '收款人姓名';
COMMENT ON COLUMN pay_transfer.status IS '转账状态';
COMMENT ON COLUMN pay_transfer.success_time IS '转账成功时间，UTC timestamptz';
COMMENT ON COLUMN pay_transfer.notify_url IS '业务通知地址';
COMMENT ON COLUMN pay_transfer.user_ip IS '操作用户IP';
COMMENT ON COLUMN pay_transfer.channel_extras IS '渠道扩展参数JSONB';
COMMENT ON COLUMN pay_transfer.channel_transfer_no IS '渠道转账单号';
COMMENT ON COLUMN pay_transfer.channel_error_code IS '渠道错误码';
COMMENT ON COLUMN pay_transfer.channel_error_msg IS '渠道错误信息';
COMMENT ON COLUMN pay_transfer.channel_notify_data IS '渠道回调原始数据';
COMMENT ON COLUMN pay_transfer.channel_package_info IS '渠道包信息';
COMMENT ON COLUMN pay_transfer.create_by_id IS '创建者ID';
COMMENT ON COLUMN pay_transfer.create_by IS '创建者';
COMMENT ON COLUMN pay_transfer.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pay_transfer.update_by IS '更新者';
COMMENT ON COLUMN pay_transfer.update_time IS '更新时间，UTC timestamptz';

COMMENT ON TABLE pay_wallet IS '支付钱包';
COMMENT ON COLUMN pay_wallet.id IS '钱包ID';
COMMENT ON COLUMN pay_wallet.tenant_id IS '租户ID';
COMMENT ON COLUMN pay_wallet.user_id IS '用户ID';
COMMENT ON COLUMN pay_wallet.user_type IS '用户类型';
COMMENT ON COLUMN pay_wallet.balance IS '钱包余额，单位分';
COMMENT ON COLUMN pay_wallet.total_expense IS '累计支出金额，单位分';
COMMENT ON COLUMN pay_wallet.total_recharge IS '累计充值金额，单位分';
COMMENT ON COLUMN pay_wallet.create_by_id IS '创建者ID';
COMMENT ON COLUMN pay_wallet.create_by IS '创建者';
COMMENT ON COLUMN pay_wallet.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pay_wallet.update_by IS '更新者';
COMMENT ON COLUMN pay_wallet.update_time IS '更新时间，UTC timestamptz';

COMMENT ON TABLE pay_wallet_transaction IS '钱包流水';
COMMENT ON COLUMN pay_wallet_transaction.id IS '钱包流水ID';
COMMENT ON COLUMN pay_wallet_transaction.tenant_id IS '租户ID';
COMMENT ON COLUMN pay_wallet_transaction.wallet_id IS '钱包ID';
COMMENT ON COLUMN pay_wallet_transaction.no IS '钱包流水号';
COMMENT ON COLUMN pay_wallet_transaction.title IS '流水标题';
COMMENT ON COLUMN pay_wallet_transaction.price IS '变动金额，单位分';
COMMENT ON COLUMN pay_wallet_transaction.balance IS '变动后余额，单位分';
COMMENT ON COLUMN pay_wallet_transaction.biz_type IS '业务类型';
COMMENT ON COLUMN pay_wallet_transaction.biz_id IS '业务ID';
COMMENT ON COLUMN pay_wallet_transaction.remark IS '备注';
COMMENT ON COLUMN pay_wallet_transaction.create_by_id IS '创建者ID';
COMMENT ON COLUMN pay_wallet_transaction.create_by IS '创建者';
COMMENT ON COLUMN pay_wallet_transaction.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pay_wallet_transaction.update_by IS '更新者';
COMMENT ON COLUMN pay_wallet_transaction.update_time IS '更新时间，UTC timestamptz';

COMMENT ON TABLE pay_wallet_recharge IS '钱包充值';
COMMENT ON COLUMN pay_wallet_recharge.id IS '钱包充值ID';
COMMENT ON COLUMN pay_wallet_recharge.tenant_id IS '租户ID';
COMMENT ON COLUMN pay_wallet_recharge.wallet_id IS '钱包ID';
COMMENT ON COLUMN pay_wallet_recharge.no IS '钱包充值单号';
COMMENT ON COLUMN pay_wallet_recharge.pay_price IS '支付金额，单位分';
COMMENT ON COLUMN pay_wallet_recharge.bonus_price IS '赠送金额，单位分';
COMMENT ON COLUMN pay_wallet_recharge.pay_order_id IS '支付订单ID';
COMMENT ON COLUMN pay_wallet_recharge.pay_status IS '支付状态';
COMMENT ON COLUMN pay_wallet_recharge.create_by_id IS '创建者ID';
COMMENT ON COLUMN pay_wallet_recharge.create_by IS '创建者';
COMMENT ON COLUMN pay_wallet_recharge.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pay_wallet_recharge.update_by IS '更新者';
COMMENT ON COLUMN pay_wallet_recharge.update_time IS '更新时间，UTC timestamptz';

COMMENT ON TABLE pay_wallet_recharge_package IS '钱包充值套餐';
COMMENT ON COLUMN pay_wallet_recharge_package.id IS '钱包充值套餐ID';
COMMENT ON COLUMN pay_wallet_recharge_package.tenant_id IS '租户ID';
COMMENT ON COLUMN pay_wallet_recharge_package.name IS '套餐名称';
COMMENT ON COLUMN pay_wallet_recharge_package.pay_price IS '支付金额，单位分';
COMMENT ON COLUMN pay_wallet_recharge_package.bonus_price IS '赠送金额，单位分';
COMMENT ON COLUMN pay_wallet_recharge_package.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pay_wallet_recharge_package.create_by_id IS '创建者ID';
COMMENT ON COLUMN pay_wallet_recharge_package.create_by IS '创建者';
COMMENT ON COLUMN pay_wallet_recharge_package.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pay_wallet_recharge_package.update_by IS '更新者';
COMMENT ON COLUMN pay_wallet_recharge_package.update_time IS '更新时间，UTC timestamptz';

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES
    (260530000001, '支付渠道', 'pay_channel_code', '1', 'admin', now(), '支付渠道字典'),
    (260530000002, '支付订单状态', 'pay_order_status', '1', 'admin', now(), '支付订单状态字典'),
    (260530000003, '支付通知状态', 'pay_notify_status', '1', 'admin', now(), '支付通知状态字典'),
    (260530000004, '支付退款状态', 'pay_refund_status', '1', 'admin', now(), '支付退款状态字典'),
    (260530000005, '支付转账状态', 'pay_transfer_status', '1', 'admin', now(), '支付转账状态字典'),
    (260530000006, '支付通知类型', 'pay_notify_type', '1', 'admin', now(), '支付通知类型字典')
ON CONFLICT (dict_type) DO NOTHING;

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time, remark)
VALUES
    (260530001001, 1, '支付宝 PC', 'alipay_pc', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001002, 2, '支付宝 WAP', 'alipay_wap', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001003, 3, '支付宝 App', 'alipay_app', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001004, 4, '支付宝扫码', 'alipay_qr', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001005, 5, '支付宝条码', 'alipay_bar', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001006, 6, '微信 JSAPI', 'wx_pub', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001007, 7, '微信小程序', 'wx_lite', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001008, 8, '微信 App', 'wx_app', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001009, 9, '微信 Native', 'wx_native', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001010, 10, '微信 WAP', 'wx_wap', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001011, 11, '微信条码', 'wx_bar', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001012, 12, 'PayPal', 'paypal', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001013, 13, 'Stripe', 'stripe', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001014, 14, 'Mock', 'mock', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001015, 15, '钱包', 'wallet', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001016, 16, '银行转账', 'bank_transfer', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530001017, 17, '信用额度', 'credit_limit', 'pay_channel_code', 'default', 'N', '1', 'admin', now(), NULL),
    (260530002001, 1, '待支付', '0', 'pay_order_status', 'warning', 'N', '1', 'admin', now(), NULL),
    (260530002004, 2, '处理中', '5', 'pay_order_status', 'primary', 'N', '1', 'admin', now(), NULL),
    (260530002002, 3, '支付成功', '10', 'pay_order_status', 'success', 'N', '1', 'admin', now(), NULL),
    (260530002003, 4, '支付关闭', '20', 'pay_order_status', 'info', 'N', '1', 'admin', now(), NULL),
    (260530003001, 1, '等待通知', '0', 'pay_notify_status', 'warning', 'N', '1', 'admin', now(), NULL),
    (260530003002, 2, '通知成功', '10', 'pay_notify_status', 'success', 'N', '1', 'admin', now(), NULL),
    (260530003003, 3, '通知失败', '20', 'pay_notify_status', 'danger', 'N', '1', 'admin', now(), NULL),
    (260530004001, 1, '退款创建', '0', 'pay_refund_status', 'warning', 'N', '1', 'admin', now(), NULL),
    (260530004002, 2, '退款成功', '10', 'pay_refund_status', 'success', 'N', '1', 'admin', now(), NULL),
    (260530004003, 3, '退款失败', '20', 'pay_refund_status', 'danger', 'N', '1', 'admin', now(), NULL),
    (260530005001, 1, '转账等待', '0', 'pay_transfer_status', 'warning', 'N', '1', 'admin', now(), NULL),
    (260530005002, 2, '转账成功', '10', 'pay_transfer_status', 'success', 'N', '1', 'admin', now(), NULL),
    (260530005003, 3, '转账失败', '20', 'pay_transfer_status', 'danger', 'N', '1', 'admin', now(), NULL),
    (260530006001, 1, '支付订单', '1', 'pay_notify_type', 'default', 'N', '1', 'admin', now(), NULL),
    (260530006002, 2, '退款订单', '2', 'pay_notify_type', 'default', 'N', '1', 'admin', now(), NULL),
    (260530006003, 3, '转账订单', '3', 'pay_notify_type', 'default', 'N', '1', 'admin', now(), NULL)
ON CONFLICT (dict_code) DO NOTHING;

INSERT INTO pay_app (id, tenant_id, app_key, name, status, order_notify_url, refund_notify_url, transfer_notify_url, remark, create_by, create_time)
VALUES
    (260530100001, 1, 'platform-default', '平台默认支付应用', '1', NULL, NULL, NULL, '本地 Mock smoke 默认应用，不包含真实支付密钥', 'admin', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO pay_channel (id, tenant_id, code, status, fee_rate, app_id, config, remark, create_by, create_time)
VALUES
    (260530100002, 1, 'mock', '1', 0, 260530100001, '{}'::jsonb, '本地 Mock smoke 默认渠道，不代表真实支付联调通过', 'admin', now()),
    (260530100003, 1, 'credit_limit', '1', 0, 260530100001, '{}'::jsonb, '商家授信支付；额度与账期来自已审核商家档案', 'admin', now())
ON CONFLICT (id) DO NOTHING;
