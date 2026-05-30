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
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_order_no ON pay_order (no);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_order_app_merchant ON pay_order (app_id, merchant_order_id);
CREATE INDEX IF NOT EXISTS idx_pay_order_payer_status_time ON pay_order (payer_tenant_id, status, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_pay_order_payee_status_time ON pay_order (payee_tenant_id, status, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_pay_order_channel_order_no ON pay_order (channel_order_no) WHERE channel_order_no IS NOT NULL;

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
    channel_error_code varchar(128),
    channel_error_msg varchar(512),
    channel_notify_data text,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_order_extension_no ON pay_order_extension (no);
CREATE INDEX IF NOT EXISTS idx_pay_order_extension_order ON pay_order_extension (order_id, status, create_time DESC);

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
    (260530002001, 1, '待支付', '0', 'pay_order_status', 'warning', 'N', '1', 'admin', now(), NULL),
    (260530002002, 2, '支付成功', '10', 'pay_order_status', 'success', 'N', '1', 'admin', now(), NULL),
    (260530002003, 3, '支付关闭', '20', 'pay_order_status', 'info', 'N', '1', 'admin', now(), NULL),
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
    (260530100002, 1, 'mock', '1', 0, 260530100001, '{}'::jsonb, '本地 Mock smoke 默认渠道，不代表真实支付联调通过', 'admin', now())
ON CONFLICT (id) DO NOTHING;
