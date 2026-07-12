-- 销售业务最终初始化结构：快速下单、正式订单、履约和权限。

CREATE TABLE IF NOT EXISTS dealer_quick_order (
    quick_order_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    quick_order_no varchar(64) NOT NULL,
    merchant_id bigint,
    merchant_name varchar(200),
    customer_id bigint NOT NULL,
    customer_name varchar(200) NOT NULL,
    company_name varchar(200),
    customer_email varchar(200),
    customer_phone varchar(64),
    owner_user_id bigint,
    owner_name varchar(100),
    customer_po_no varchar(100),
    recipient_name varchar(100),
    recipient_phone varchar(64),
    shipping_address varchar(1000),
    currency_code varchar(16) NOT NULL DEFAULT 'USD',
    list_amount numeric(18,2) NOT NULL DEFAULT 0,
    product_amount numeric(18,2) NOT NULL DEFAULT 0,
    discount_amount numeric(18,2) NOT NULL DEFAULT 0,
    shipping_amount numeric(18,2) NOT NULL DEFAULT 0,
    tax_amount numeric(18,2) NOT NULL DEFAULT 0,
    total_amount numeric(18,2) NOT NULL DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ORDERED')),
    sales_document_id bigint,
    order_no varchar(64),
    submitted_by_id bigint,
    submitted_by varchar(100),
    submitted_time timestamptz,
    del_flag char(1) NOT NULL DEFAULT '0',
    create_dept bigint,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(1000)
);
CREATE INDEX IF NOT EXISTS idx_dealer_quick_order_no ON dealer_quick_order (tenant_id, quick_order_no) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_quick_order_customer ON dealer_quick_order (tenant_id, customer_id, status) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_quick_order_sales_document ON dealer_quick_order (sales_document_id) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS dealer_quick_order_item (
    quick_order_item_id bigint PRIMARY KEY,
    quick_order_id bigint NOT NULL,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    line_no integer NOT NULL,
    room_location varchar(200),
    sale_product_id bigint NOT NULL,
    sale_product_code varchar(100),
    sale_product_name varchar(200),
    category_id bigint,
    category_code varchar(100),
    category_name_cn varchar(200),
    product_type_code varchar(100),
    product_type_name_cn varchar(200),
    formula_id bigint,
    formula_version_id bigint,
    formula_version_label varchar(50),
    order_width_inch numeric(18,4),
    order_height_inch numeric(18,4),
    quantity integer NOT NULL DEFAULT 1 CHECK (quantity > 0),
    selected_options_json text,
    configuration_summary_cn text,
    configuration_summary_en text,
    calculation_status varchar(32) NOT NULL DEFAULT 'PENDING',
    calculation_message varchar(1000),
    bom_snapshot_json text,
    pricing_snapshot_json text,
    shipping_snapshot_json text,
    list_unit_amount numeric(18,2) NOT NULL DEFAULT 0,
    list_amount numeric(18,2) NOT NULL DEFAULT 0,
    discount_rate numeric(10,4) NOT NULL DEFAULT 1,
    discount_amount numeric(18,2) NOT NULL DEFAULT 0,
    unit_amount numeric(18,2) NOT NULL DEFAULT 0,
    product_amount numeric(18,2) NOT NULL DEFAULT 0,
    shipping_template_id bigint,
    unit_shipping_amount numeric(18,2) NOT NULL DEFAULT 0,
    shipping_amount numeric(18,2) NOT NULL DEFAULT 0,
    line_amount numeric(18,2) NOT NULL DEFAULT 0,
    sort_order integer NOT NULL DEFAULT 0,
    del_flag char(1) NOT NULL DEFAULT '0',
    create_dept bigint,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(1000)
);
CREATE INDEX IF NOT EXISTS idx_dealer_quick_order_item_order ON dealer_quick_order_item (tenant_id, quick_order_id, line_no) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_quick_order_item_product ON dealer_quick_order_item (sale_product_id, formula_version_id) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS dealer_sales_document (
    sales_document_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    merchant_id bigint,
    merchant_name varchar(200),
    source_type varchar(20) NOT NULL CHECK (source_type IN ('QUOTE', 'QUICK_ORDER')),
    source_quote_id bigint,
    source_quick_order_id bigint,
    source_no varchar(64) NOT NULL,
    quote_no varchar(64),
    order_no varchar(64) NOT NULL,
    customer_id bigint NOT NULL,
    customer_name varchar(200) NOT NULL,
    company_name varchar(200),
    customer_email varchar(200),
    customer_phone varchar(64),
    owner_user_id bigint,
    owner_name varchar(100),
    project_name varchar(200),
    customer_po_no varchar(100),
    valid_until date,
    recipient_name varchar(100),
    recipient_phone varchar(64),
    shipping_address varchar(1000),
    currency_code varchar(16) NOT NULL DEFAULT 'USD',
    list_amount numeric(18,2) NOT NULL DEFAULT 0,
    discount_rate numeric(10,4) NOT NULL DEFAULT 1,
    discount_amount numeric(18,2) NOT NULL DEFAULT 0,
    product_amount numeric(18,2) NOT NULL DEFAULT 0,
    shipping_amount numeric(18,2) NOT NULL DEFAULT 0,
    tax_amount numeric(18,2) NOT NULL DEFAULT 0,
    total_amount numeric(18,2) NOT NULL DEFAULT 0,
    document_status varchar(32) NOT NULL DEFAULT 'SUBMITTED' CHECK (document_status IN ('SUBMITTED', 'CANCELLED', 'COMPLETED')),
    payment_status varchar(32) NOT NULL DEFAULT 'UNPAID' CHECK (payment_status IN ('UNPAID', 'PAID')),
    pay_order_id bigint,
    pay_order_no varchar(64),
    payment_method varchar(32),
    paid_amount numeric(18,2),
    payment_reference varchar(128),
    payment_proof_media_id bigint,
    payment_confirmed_by_id bigint,
    payment_confirmed_by varchar(100),
    paid_time timestamptz,
    production_status varchar(32) NOT NULL DEFAULT 'PENDING' CHECK (production_status IN ('PENDING', 'IN_PRODUCTION', 'COMPLETED')),
    production_started_by_id bigint,
    production_started_by varchar(100),
    production_start_time timestamptz,
    production_completed_by_id bigint,
    production_completed_by varchar(100),
    production_complete_time timestamptz,
    production_remark varchar(1000),
    shipment_status varchar(32) NOT NULL DEFAULT 'UNSHIPPED' CHECK (shipment_status IN ('UNSHIPPED', 'PARTIALLY_SHIPPED', 'SHIPPED', 'DELIVERED')),
    shipment_count integer NOT NULL DEFAULT 0 CHECK (shipment_count >= 0),
    shipped_quantity integer NOT NULL DEFAULT 0 CHECK (shipped_quantity >= 0),
    first_shipped_time timestamptz,
    latest_shipped_time timestamptz,
    latest_carrier_name varchar(100),
    latest_tracking_no varchar(128),
    shipped_time timestamptz,
    carrier_name varchar(100),
    tracking_no varchar(128),
    delivered_time timestamptz,
    submitted_time timestamptz NOT NULL,
    completed_time timestamptz,
    cancelled_by_id bigint,
    cancelled_by varchar(100),
    cancelled_time timestamptz,
    cancel_reason varchar(1000),
    del_flag char(1) NOT NULL DEFAULT '0',
    create_dept bigint,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(1000),
    CONSTRAINT ck_dealer_sales_document_source CHECK ((source_type = 'QUOTE' AND source_quote_id IS NOT NULL AND source_quick_order_id IS NULL)
        OR (source_type = 'QUICK_ORDER' AND source_quote_id IS NULL AND source_quick_order_id IS NOT NULL))
);
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS source_type varchar(20);
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS source_quick_order_id bigint;
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS source_no varchar(64);
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS pay_order_id bigint;
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS pay_order_no varchar(64);
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS production_started_by_id bigint;
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS production_started_by varchar(100);
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS production_completed_by_id bigint;
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS production_completed_by varchar(100);
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS production_remark varchar(1000);
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS shipment_count integer NOT NULL DEFAULT 0;
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS shipped_quantity integer NOT NULL DEFAULT 0;
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS first_shipped_time timestamptz;
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS latest_shipped_time timestamptz;
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS latest_carrier_name varchar(100);
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS latest_tracking_no varchar(128);
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS completed_time timestamptz;
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS cancelled_by_id bigint;
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS cancelled_by varchar(100);
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS cancelled_time timestamptz;
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS cancel_reason varchar(1000);
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS create_by_id bigint;
ALTER TABLE dealer_sales_document ALTER COLUMN source_quote_id DROP NOT NULL;
ALTER TABLE dealer_sales_document ALTER COLUMN quote_no DROP NOT NULL;
UPDATE dealer_sales_document SET source_type = 'QUOTE' WHERE source_type IS NULL;
UPDATE dealer_sales_document
SET source_no = COALESCE(quote_no, order_no, sales_document_id::varchar)
WHERE source_no IS NULL;
UPDATE dealer_sales_document
SET order_no = COALESCE(order_no, quote_no, 'SO-' || sales_document_id::varchar)
WHERE order_no IS NULL;
ALTER TABLE dealer_sales_document ALTER COLUMN source_type SET NOT NULL;
ALTER TABLE dealer_sales_document ALTER COLUMN source_no SET NOT NULL;
ALTER TABLE dealer_sales_document ALTER COLUMN order_no SET NOT NULL;
ALTER TABLE dealer_sales_document DROP CONSTRAINT IF EXISTS ck_dealer_sales_document_source;
ALTER TABLE dealer_sales_document ADD CONSTRAINT ck_dealer_sales_document_source
    CHECK ((source_type = 'QUOTE' AND source_quote_id IS NOT NULL AND source_quick_order_id IS NULL)
        OR (source_type = 'QUICK_ORDER' AND source_quote_id IS NULL AND source_quick_order_id IS NOT NULL));
CREATE INDEX IF NOT EXISTS idx_dealer_sales_source_quote ON dealer_sales_document (source_quote_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_sales_source_quick_order ON dealer_sales_document (source_quick_order_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_sales_order ON dealer_sales_document (tenant_id, order_no) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_sales_customer ON dealer_sales_document (tenant_id, customer_id, document_status) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_sales_pay_order ON dealer_sales_document (pay_order_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_sales_progress ON dealer_sales_document (document_status, payment_status, production_status, shipment_status) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS dealer_sales_document_item (
    sales_item_id bigint PRIMARY KEY,
    sales_document_id bigint NOT NULL,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    source_quote_item_id bigint,
    source_quick_order_item_id bigint,
    line_no integer NOT NULL,
    item_code varchar(64),
    room_location varchar(200),
    sale_product_id bigint NOT NULL,
    sale_product_code varchar(100),
    sale_product_name varchar(200),
    category_id bigint,
    category_code varchar(100),
    category_name_cn varchar(200),
    product_type_code varchar(100),
    product_type_name_cn varchar(200),
    formula_id bigint,
    formula_version_id bigint,
    formula_version_label varchar(50),
    order_width_inch numeric(18,4),
    order_height_inch numeric(18,4),
    quantity integer NOT NULL DEFAULT 1 CHECK (quantity > 0),
    selected_options_json text,
    configuration_summary text,
    configuration_summary_cn text,
    configuration_summary_en text,
    calculation_status varchar(32),
    calculation_message varchar(1000),
    bom_snapshot_json text,
    pricing_snapshot_json text,
    shipping_snapshot_json text,
    list_unit_amount numeric(18,2) NOT NULL DEFAULT 0,
    list_amount numeric(18,2) NOT NULL DEFAULT 0,
    discount_rate numeric(10,4) NOT NULL DEFAULT 1,
    discount_amount numeric(18,2) NOT NULL DEFAULT 0,
    unit_amount numeric(18,2) NOT NULL DEFAULT 0,
    product_amount numeric(18,2) NOT NULL DEFAULT 0,
    shipping_template_id bigint,
    unit_shipping_amount numeric(18,2) NOT NULL DEFAULT 0,
    shipping_amount numeric(18,2) NOT NULL DEFAULT 0,
    line_amount numeric(18,2) NOT NULL DEFAULT 0,
    sort_order integer NOT NULL DEFAULT 0,
    del_flag char(1) NOT NULL DEFAULT '0',
    create_dept bigint,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(1000),
    CONSTRAINT ck_dealer_sales_document_item_source CHECK ((source_quote_item_id IS NOT NULL AND source_quick_order_item_id IS NULL)
        OR (source_quote_item_id IS NULL AND source_quick_order_item_id IS NOT NULL))
);
ALTER TABLE dealer_sales_document_item ADD COLUMN IF NOT EXISTS source_quick_order_item_id bigint;
ALTER TABLE dealer_sales_document_item ADD COLUMN IF NOT EXISTS configuration_summary_cn text;
ALTER TABLE dealer_sales_document_item ADD COLUMN IF NOT EXISTS configuration_summary_en text;
ALTER TABLE dealer_sales_document_item ADD COLUMN IF NOT EXISTS discount_amount numeric(18,2) NOT NULL DEFAULT 0;
ALTER TABLE dealer_sales_document_item ADD COLUMN IF NOT EXISTS unit_shipping_amount numeric(18,2) NOT NULL DEFAULT 0;
ALTER TABLE dealer_sales_document_item ADD COLUMN IF NOT EXISTS create_by_id bigint;
ALTER TABLE dealer_sales_document_item ALTER COLUMN source_quote_item_id DROP NOT NULL;
ALTER TABLE dealer_sales_document_item DROP CONSTRAINT IF EXISTS ck_dealer_sales_document_item_source;
ALTER TABLE dealer_sales_document_item ADD CONSTRAINT ck_dealer_sales_document_item_source
    CHECK ((source_quote_item_id IS NOT NULL AND source_quick_order_item_id IS NULL)
        OR (source_quote_item_id IS NULL AND source_quick_order_item_id IS NOT NULL));
CREATE INDEX IF NOT EXISTS idx_dealer_sales_item_document ON dealer_sales_document_item (tenant_id, sales_document_id, line_no) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_sales_item_source_quote ON dealer_sales_document_item (source_quote_item_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_sales_item_source_quick ON dealer_sales_document_item (source_quick_order_item_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_sales_item_product ON dealer_sales_document_item (sale_product_id, formula_version_id) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS dealer_sales_document_event (
    sales_event_id bigint PRIMARY KEY,
    sales_document_id bigint NOT NULL,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    event_type varchar(64) NOT NULL,
    from_status varchar(64),
    to_status varchar(64),
    operator_id bigint,
    operator_name varchar(100),
    event_note varchar(1000),
    before_value_json text,
    after_value_json text,
    occurred_time timestamptz NOT NULL,
    create_dept bigint,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
ALTER TABLE dealer_sales_document_event ADD COLUMN IF NOT EXISTS before_value_json text;
ALTER TABLE dealer_sales_document_event ADD COLUMN IF NOT EXISTS after_value_json text;
ALTER TABLE dealer_sales_document_event ADD COLUMN IF NOT EXISTS create_by_id bigint;
CREATE INDEX IF NOT EXISTS idx_dealer_sales_event_document ON dealer_sales_document_event (tenant_id, sales_document_id, occurred_time DESC);

CREATE TABLE IF NOT EXISTS dealer_shipment (
    shipment_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    merchant_id bigint,
    merchant_name varchar(200),
    sales_document_id bigint NOT NULL,
    shipment_no varchar(64) NOT NULL,
    package_no varchar(64),
    carrier_code varchar(64),
    carrier_name varchar(100),
    tracking_no varchar(128),
    status varchar(32) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'DISPATCHED', 'IN_TRANSIT', 'CARRIER_DELIVERED', 'EXCEPTION', 'CANCELLED')),
    tracking_status varchar(32),
    item_quantity integer NOT NULL DEFAULT 0 CHECK (item_quantity >= 0),
    weight numeric(18,6),
    weight_unit varchar(32),
    length numeric(18,6),
    width numeric(18,6),
    height numeric(18,6),
    dimension_unit varchar(32),
    label_media_id bigint,
    packing_list_media_id bigint,
    shipped_by_id bigint,
    shipped_by varchar(100),
    shipped_time timestamptz,
    receipt_status varchar(20) NOT NULL DEFAULT 'PENDING' CHECK (receipt_status IN ('PENDING', 'CONFIRMED')),
    received_by_id bigint,
    received_by varchar(100),
    received_time timestamptz,
    receipt_override_reason varchar(1000),
    last_tracking_time timestamptz,
    tracking_error_code varchar(128),
    tracking_error_message varchar(1000),
    del_flag char(1) NOT NULL DEFAULT '0',
    create_dept bigint,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(1000)
);
CREATE INDEX IF NOT EXISTS idx_dealer_shipment_no ON dealer_shipment (tenant_id, shipment_no) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_shipment_document ON dealer_shipment (tenant_id, sales_document_id, status) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_shipment_tracking ON dealer_shipment (carrier_code, tracking_no) WHERE del_flag = '0' AND tracking_no IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_dealer_shipment_exception ON dealer_shipment (status, last_tracking_time) WHERE del_flag = '0' AND status = 'EXCEPTION';

CREATE TABLE IF NOT EXISTS dealer_shipment_item (
    shipment_item_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    shipment_id bigint NOT NULL,
    sales_document_id bigint NOT NULL,
    sales_item_id bigint NOT NULL,
    line_no integer NOT NULL,
    sale_product_code varchar(100),
    sale_product_name varchar(200),
    quantity integer NOT NULL CHECK (quantity > 0),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(1000)
);
CREATE INDEX IF NOT EXISTS idx_dealer_shipment_item_shipment ON dealer_shipment_item (tenant_id, shipment_id, line_no);
CREATE INDEX IF NOT EXISTS idx_dealer_shipment_item_sales_item ON dealer_shipment_item (sales_document_id, sales_item_id);

CREATE TABLE IF NOT EXISTS dealer_tracking_event (
    tracking_event_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    shipment_id bigint NOT NULL,
    carrier_code varchar(64) NOT NULL,
    tracking_no varchar(128) NOT NULL,
    provider_event_id varchar(128),
    event_code varchar(64) NOT NULL,
    event_status varchar(32),
    description_original text,
    description_cn text,
    description_en text,
    location varchar(255),
    occurred_time timestamptz NOT NULL,
    source varchar(20) NOT NULL CHECK (source IN ('API', 'WEBHOOK', 'MANUAL')),
    received_time timestamptz NOT NULL DEFAULT now(),
    raw_data_ref varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE INDEX IF NOT EXISTS idx_dealer_tracking_shipment ON dealer_tracking_event (tenant_id, shipment_id, occurred_time DESC);
CREATE UNIQUE INDEX IF NOT EXISTS uk_dealer_tracking_provider_event ON dealer_tracking_event (carrier_code, provider_event_id) WHERE provider_event_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_dealer_tracking_fallback_event ON dealer_tracking_event (carrier_code, tracking_no, event_code, occurred_time) WHERE provider_event_id IS NULL;

-- 清理已被销售业务七个入口替代的旧菜单。
DELETE FROM sys_role_menu WHERE menu_id IN (26009, 26010, 26011, 26012, 300130, 300001212);
DELETE FROM sys_menu WHERE menu_id IN (26009, 26010, 26011, 26012, 300130, 300001212);

-- 平台租户销售业务七个菜单及按钮权限。
INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (26000, 1, '销售业务', 'sales.menu', 0, 40, 'sales', NULL, '1', '0', 'M', '1', '1', NULL, 'shopping', 'system', now(), '销售全链路业务'),
    (26020, 1, '销售仪表盘', 'sales.dashboard.menu', 26000, 1, 'dashboard', 'sales/dashboard', '1', '0', 'C', '1', '1', 'sales:dashboard:view', 'dashboard', 'system', now(), ''),
    (20230, 1, '工程测算', 'customer.quote.estimate.menu', 26000, 2, 'estimates', 'customer/quote-workbench', '1', '0', 'C', '1', '1', 'customer:quote:list', 'calculator', 'system', now(), ''),
    (26021, 1, '报价管理', 'customer.quote.management.menu', 26000, 3, 'quotes', 'customer/quotes', '1', '0', 'C', '1', '1', 'customer:quote:list', 'documentation', 'system', now(), ''),
    (26022, 1, '快速下单', 'dealer.quickOrder.menu', 26000, 4, 'quickOrders', 'dealer-quick-order/list', '1', '0', 'C', '1', '1', 'dealer:quick-order:list', 'shopping', 'system', now(), ''),
    (26001, 1, '销售订单', 'dealer.sales.list', 26000, 5, 'salesDocuments', 'dealer-sales/list', '1', '0', 'C', '1', '1', 'dealer:sales:list', 'list', 'system', now(), ''),
    (26023, 1, '支付结算', 'pay.settlement.menu', 26000, 6, 'payments', 'pay/order/list', '1', '0', 'C', '1', '1', 'pay:order:list', 'money', 'system', now(), ''),
    (26024, 1, '履约管理', 'dealer.fulfillment.menu', 26000, 7, 'fulfillment', 'dealer-fulfillment/index', '1', '0', 'C', '1', '1', 'dealer:fulfillment:production:list', 'logistics', 'system', now(), ''),
    (20231, 1, '报价查询', 'customer.quote.permission.query', 20230, 1, '#', '', '1', '0', 'F', '1', '1', 'customer:quote:query', '#', 'system', now(), ''),
    (20232, 1, '报价新增', 'customer.quote.permission.add', 20230, 2, '#', '', '1', '0', 'F', '1', '1', 'customer:quote:add', '#', 'system', now(), ''),
    (20233, 1, '报价修改', 'customer.quote.permission.edit', 20230, 3, '#', '', '1', '0', 'F', '1', '1', 'customer:quote:edit', '#', 'system', now(), ''),
    (20234, 1, '报价删除', 'customer.quote.permission.remove', 20230, 4, '#', '', '1', '0', 'F', '1', '1', 'customer:quote:remove', '#', 'system', now(), ''),
    (20235, 1, '报价导出', 'customer.quote.permission.export', 20230, 5, '#', '', '1', '0', 'F', '1', '1', 'customer:quote:export', '#', 'system', now(), ''),
    (20236, 1, '报价单据', 'customer.quote.permission.document', 20230, 6, '#', '', '1', '0', 'F', '1', '1', 'customer:quote:document', '#', 'system', now(), ''),
    (20237, 1, '报价邮件', 'customer.quote.permission.email', 20230, 7, '#', '', '1', '0', 'F', '1', '1', 'customer:quote:email', '#', 'system', now(), ''),
    (20238, 1, '转销售订单', 'customer.quote.permission.convert', 20230, 8, '#', '', '1', '0', 'F', '1', '1', 'customer:quote:convert', '#', 'system', now(), ''),
    (26031, 1, '快速下单查询', 'dealer.quickOrder.query', 26022, 1, '#', '', '1', '0', 'F', '1', '1', 'dealer:quick-order:query', '#', 'system', now(), ''),
    (26032, 1, '快速下单新增', 'dealer.quickOrder.add', 26022, 2, '#', '', '1', '0', 'F', '1', '1', 'dealer:quick-order:add', '#', 'system', now(), ''),
    (26033, 1, '快速下单修改', 'dealer.quickOrder.edit', 26022, 3, '#', '', '1', '0', 'F', '1', '1', 'dealer:quick-order:edit', '#', 'system', now(), ''),
    (26034, 1, '快速下单删除', 'dealer.quickOrder.remove', 26022, 4, '#', '', '1', '0', 'F', '1', '1', 'dealer:quick-order:remove', '#', 'system', now(), ''),
    (26035, 1, '快速下单提交', 'dealer.quickOrder.submit', 26022, 5, '#', '', '1', '0', 'F', '1', '1', 'dealer:quick-order:submit', '#', 'system', now(), ''),
    (26002, 1, '销售单查询', 'dealer.sales.permission.query', 26001, 1, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:query', '#', 'system', now(), ''),
    (26008, 1, '订单取消', 'dealer.sales.permission.cancel', 26001, 2, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:cancel', '#', 'system', now(), ''),
    (26013, 1, '单据输出', 'dealer.sales.permission.document', 26001, 3, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:document', '#', 'system', now(), ''),
    (26014, 1, '邮件发送', 'dealer.sales.permission.email', 26001, 4, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:email', '#', 'system', now(), '')
ON CONFLICT (menu_id) DO UPDATE SET menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id, order_num = EXCLUDED.order_num, path = EXCLUDED.path,
    component = EXCLUDED.component, menu_type = EXCLUDED.menu_type, visible = EXCLUDED.visible,
    status = EXCLUDED.status, perms = EXCLUDED.perms, icon = EXCLUDED.icon,
    tenant_id = EXCLUDED.tenant_id, update_by = 'system', update_time = now();

-- 其余业务按钮用固定权限编码挂到所属页面。
INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT x.menu_id, 1, x.menu_name, x.i18n_key, x.parent_id, x.order_num, '#', '', '1', '0', 'F', '1', '1', x.perms, '#', 'system', now(), ''
FROM (VALUES
    (26101, '支付单查询', 'pay.order.query', 26023, 1, 'pay:order:query'),
    (26102, '提交支付', 'pay.order.submit', 26023, 2, 'pay:order:submit'),
    (26103, '提交银行转账', 'pay.bank.submit', 26023, 3, 'pay:bank:submit'),
    (26104, '审核银行转账', 'pay.bank.review', 26023, 4, 'pay:bank:review'),
    (26105, '使用信用额度', 'pay.credit.use', 26023, 5, 'pay:credit:use'),
    (26106, '查询信用账户', 'pay.credit.query', 26023, 6, 'pay:credit:query'),
    (26107, '调整信用额度', 'pay.credit.adjust', 26023, 7, 'pay:credit:adjust'),
    (26108, '冻结信用账户', 'pay.credit.freeze', 26023, 8, 'pay:credit:freeze'),
    (26109, '信用回款', 'pay.credit.repay', 26023, 9, 'pay:credit:repay'),
    (26110, '付款补录', 'pay.order.supplement', 26023, 10, 'pay:order:supplement'),
    (26111, '支付修复', 'pay.order.repair', 26023, 11, 'pay:order:repair'),
    (26112, '对账查询', 'pay.reconcile.query', 26023, 12, 'pay:reconcile:query'),
    (26113, '执行对账', 'pay.reconcile.execute', 26023, 13, 'pay:reconcile:execute'),
    (26114, '渠道查询', 'pay.channel.list', 26023, 14, 'pay:channel:list'),
    (26115, '渠道配置', 'pay.channel.config', 26023, 15, 'pay:channel:config'),
    (26201, '生产查询', 'dealer.fulfillment.production.query', 26024, 1, 'dealer:fulfillment:production:query'),
    (26202, '开始生产', 'dealer.fulfillment.production.start', 26024, 2, 'dealer:fulfillment:production:start'),
    (26203, '完成生产', 'dealer.fulfillment.production.complete', 26024, 3, 'dealer:fulfillment:production:complete'),
    (26204, '生产单据', 'dealer.fulfillment.production.document', 26024, 4, 'dealer:fulfillment:production:document'),
    (26205, '发货列表', 'dealer.fulfillment.shipment.list', 26024, 5, 'dealer:fulfillment:shipment:list'),
    (26206, '包裹查询', 'dealer.fulfillment.shipment.query', 26024, 6, 'dealer:fulfillment:shipment:query'),
    (26207, '包裹新增', 'dealer.fulfillment.shipment.add', 26024, 7, 'dealer:fulfillment:shipment:add'),
    (26208, '包裹修改', 'dealer.fulfillment.shipment.edit', 26024, 8, 'dealer:fulfillment:shipment:edit'),
    (26209, '包裹删除', 'dealer.fulfillment.shipment.remove', 26024, 9, 'dealer:fulfillment:shipment:remove'),
    (26210, '确认发货', 'dealer.fulfillment.shipment.dispatch', 26024, 10, 'dealer:fulfillment:shipment:dispatch'),
    (26211, '发货纠错', 'dealer.fulfillment.shipment.correct', 26024, 11, 'dealer:fulfillment:shipment:correct'),
    (26212, '同步物流', 'dealer.fulfillment.tracking.sync', 26024, 12, 'dealer:fulfillment:tracking:sync'),
    (26213, '物流轨迹', 'dealer.fulfillment.tracking.list', 26024, 13, 'dealer:fulfillment:tracking:list'),
    (26214, '确认收货', 'dealer.fulfillment.receipt.confirm', 26024, 14, 'dealer:fulfillment:receipt:confirm'),
    (26215, '异常代签收', 'dealer.fulfillment.receipt.override', 26024, 15, 'dealer:fulfillment:receipt:override')
) AS x(menu_id, menu_name, i18n_key, parent_id, order_num, perms)
ON CONFLICT (menu_id) DO UPDATE SET menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id, order_num = EXCLUDED.order_num, perms = EXCLUDED.perms,
    tenant_id = EXCLUDED.tenant_id, status = EXCLUDED.status, update_by = 'system', update_time = now();

-- 平台销售、财务和工厂能力角色。
INSERT INTO sys_role (role_id, tenant_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES
    (260901, 1, '平台销售负责人', 'platform_sales_manager', 20, '2', true, true, '1', '0', 'system', now(), '授权商家销售数据范围'),
    (260902, 1, '平台销售', 'platform_sales', 21, '4', true, true, '1', '0', 'system', now(), '本人或被分配商家销售数据范围'),
    (260903, 1, '平台财务', 'platform_finance', 22, '2', true, true, '1', '0', 'system', now(), '平台支付结算能力'),
    (260904, 1, '工厂生产', 'factory_production', 23, '2', true, true, '1', '0', 'system', now(), '已付款订单生产能力'),
    (260905, 1, '工厂发货', 'factory_shipping', 24, '2', true, true, '1', '0', 'system', now(), '已完工订单发货能力')
ON CONFLICT (role_id) DO UPDATE SET role_name = EXCLUDED.role_name, role_key = EXCLUDED.role_key,
    role_sort = EXCLUDED.role_sort, data_scope = EXCLUDED.data_scope, status = EXCLUDED.status,
    del_flag = EXCLUDED.del_flag, update_by = 'system', update_time = now(), remark = EXCLUDED.remark;

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT 1, menu_id, 1 FROM sys_menu WHERE tenant_id = 1 AND (menu_id = 26000 OR parent_id IN (26000, 20230, 26021, 26022, 26001, 26023, 26024))
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT r.role_id, m.menu_id, 1
FROM sys_role r
JOIN sys_menu m ON m.tenant_id = 1
WHERE r.role_key IN ('platform_sales_manager', 'platform_sales')
  AND (m.menu_id = 26000 OR m.perms IN ('sales:dashboard:view', 'customer:quote:list', 'customer:quote:query',
      'customer:quote:export', 'customer:quote:document', 'dealer:sales:list', 'dealer:sales:query',
      'dealer:sales:document', 'dealer:sales:email'))
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT r.role_id, m.menu_id, 1
FROM sys_role r
JOIN sys_menu m ON m.tenant_id = 1
WHERE r.role_key = 'platform_finance'
  AND (m.menu_id = 26000 OR m.perms IN ('sales:dashboard:view', 'dealer:sales:list', 'dealer:sales:query',
      'pay:order:list', 'pay:order:query', 'pay:bank:review', 'pay:credit:query', 'pay:credit:adjust',
      'pay:credit:freeze', 'pay:credit:repay', 'pay:order:supplement', 'pay:order:repair',
      'pay:reconcile:query', 'pay:reconcile:execute'))
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT r.role_id, m.menu_id, 1
FROM sys_role r
JOIN sys_menu m ON m.tenant_id = 1
WHERE (r.role_key = 'factory_production' AND (m.menu_id = 26000 OR m.perms IN ('sales:dashboard:view',
          'dealer:fulfillment:production:list', 'dealer:fulfillment:production:query',
          'dealer:fulfillment:production:start', 'dealer:fulfillment:production:complete',
          'dealer:fulfillment:production:document')))
   OR (r.role_key = 'factory_shipping' AND (m.menu_id = 26000 OR m.perms IN ('sales:dashboard:view',
          'dealer:fulfillment:production:list', 'dealer:fulfillment:shipment:list',
          'dealer:fulfillment:shipment:query', 'dealer:fulfillment:shipment:add',
          'dealer:fulfillment:shipment:edit', 'dealer:fulfillment:shipment:remove',
          'dealer:fulfillment:shipment:dispatch', 'dealer:fulfillment:shipment:correct',
          'dealer:fulfillment:tracking:sync', 'dealer:fulfillment:tracking:list')))
ON CONFLICT DO NOTHING;

-- 为每个商家租户建立同一组七菜单，履约入口使用商家跟踪权限。
INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT t.tenant_id * 1000 + x.offset_id, t.tenant_id, x.menu_name, x.i18n_key,
       CASE WHEN x.offset_id = 200 THEN 0 ELSE t.tenant_id * 1000 + 200 END,
       x.order_num, x.path, x.component, '1', '0', x.menu_type, '1', '1', x.perms, x.icon, 'system', now(), '销售业务'
FROM sys_tenant t
CROSS JOIN (VALUES
    (200, '销售业务', 'sales.menu', 40, 'sales', NULL, 'M', NULL, 'shopping'),
    (220, '销售仪表盘', 'sales.dashboard.menu', 1, 'dashboard', 'sales/dashboard', 'C', 'sales:dashboard:view', 'dashboard'),
    (230, '工程测算', 'customer.quote.estimate.menu', 2, 'estimates', 'customer/quote-workbench', 'C', 'customer:quote:list', 'calculator'),
    (221, '报价管理', 'customer.quote.management.menu', 3, 'quotes', 'customer/quotes', 'C', 'customer:quote:list', 'documentation'),
    (222, '快速下单', 'dealer.quickOrder.menu', 4, 'quickOrders', 'dealer-quick-order/list', 'C', 'dealer:quick-order:list', 'shopping'),
    (201, '销售订单', 'dealer.sales.list', 5, 'salesDocuments', 'dealer-sales/list', 'C', 'dealer:sales:list', 'list'),
    (223, '支付结算', 'pay.settlement.menu', 6, 'payments', 'pay/order/list', 'C', 'pay:order:list', 'money'),
    (224, '履约管理', 'dealer.fulfillment.menu', 7, 'fulfillment', 'dealer-fulfillment/index', 'C', 'dealer:fulfillment:tracking:list', 'logistics')
) AS x(offset_id, menu_name, i18n_key, order_num, path, component, menu_type, perms, icon)
WHERE t.tenant_type = 'MERCHANT'
ON CONFLICT (menu_id) DO UPDATE SET menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id, order_num = EXCLUDED.order_num, path = EXCLUDED.path,
    component = EXCLUDED.component, menu_type = EXCLUDED.menu_type, visible = EXCLUDED.visible,
    status = EXCLUDED.status, perms = EXCLUDED.perms, icon = EXCLUDED.icon,
    tenant_id = EXCLUDED.tenant_id, update_by = 'system', update_time = now();

INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT t.tenant_id * 1000 + x.offset_id, t.tenant_id, x.menu_name, x.i18n_key,
       t.tenant_id * 1000 + x.parent_offset, x.order_num, '#', '', '1', '0', 'F', '1', '1', x.perms, '#', 'system', now(), ''
FROM sys_tenant t
CROSS JOIN (VALUES
    (231, 230, 1, '报价查询', 'customer.quote.permission.query', 'customer:quote:query'),
    (232, 230, 2, '报价新增', 'customer.quote.permission.add', 'customer:quote:add'),
    (233, 230, 3, '报价修改', 'customer.quote.permission.edit', 'customer:quote:edit'),
    (234, 230, 4, '报价删除', 'customer.quote.permission.remove', 'customer:quote:remove'),
    (235, 230, 5, '报价导出', 'customer.quote.permission.export', 'customer:quote:export'),
    (236, 230, 6, '报价单据', 'customer.quote.permission.document', 'customer:quote:document'),
    (237, 230, 7, '报价邮件', 'customer.quote.permission.email', 'customer:quote:email'),
    (238, 230, 8, '转销售订单', 'customer.quote.permission.convert', 'customer:quote:convert'),
    (241, 222, 1, '快速下单查询', 'dealer.quickOrder.query', 'dealer:quick-order:query'),
    (242, 222, 2, '快速下单新增', 'dealer.quickOrder.add', 'dealer:quick-order:add'),
    (243, 222, 3, '快速下单修改', 'dealer.quickOrder.edit', 'dealer:quick-order:edit'),
    (244, 222, 4, '快速下单删除', 'dealer.quickOrder.remove', 'dealer:quick-order:remove'),
    (245, 222, 5, '快速下单提交', 'dealer.quickOrder.submit', 'dealer:quick-order:submit'),
    (202, 201, 1, '销售单查询', 'dealer.sales.permission.query', 'dealer:sales:query'),
    (208, 201, 2, '订单取消', 'dealer.sales.permission.cancel', 'dealer:sales:cancel'),
    (213, 201, 3, '单据输出', 'dealer.sales.permission.document', 'dealer:sales:document'),
    (214, 201, 4, '邮件发送', 'dealer.sales.permission.email', 'dealer:sales:email'),
    (251, 223, 1, '支付单查询', 'pay.order.query', 'pay:order:query'),
    (252, 223, 2, '提交支付', 'pay.order.submit', 'pay:order:submit'),
    (253, 223, 3, '提交银行转账', 'pay.bank.submit', 'pay:bank:submit'),
    (254, 223, 4, '使用信用额度', 'pay.credit.use', 'pay:credit:use'),
    (255, 223, 5, '查询信用账户', 'pay.credit.query', 'pay:credit:query'),
    (261, 224, 1, '包裹查询', 'dealer.fulfillment.shipment.query', 'dealer:fulfillment:shipment:query'),
    (262, 224, 2, '物流轨迹', 'dealer.fulfillment.tracking.list', 'dealer:fulfillment:tracking:list'),
    (263, 224, 3, '确认收货', 'dealer.fulfillment.receipt.confirm', 'dealer:fulfillment:receipt:confirm')
) AS x(offset_id, parent_offset, order_num, menu_name, i18n_key, perms)
WHERE t.tenant_type = 'MERCHANT'
ON CONFLICT (menu_id) DO UPDATE SET menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id, order_num = EXCLUDED.order_num, perms = EXCLUDED.perms,
    tenant_id = EXCLUDED.tenant_id, status = EXCLUDED.status, update_by = 'system', update_time = now();

-- 商家能力角色按租户生成；负责人、销售、财务、跟单不再共享同一权限集合。
INSERT INTO sys_role (role_id, tenant_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT t.tenant_id * 1000 + x.offset_id, t.tenant_id, x.role_name, x.role_key, x.role_sort, x.data_scope,
       true, true, '1', '0', 'system', now(), x.remark
FROM sys_tenant t
CROSS JOIN (VALUES
    (901, '商家销售', 'merchant_sales', 20, '4', '本人或团队销售能力'),
    (902, '商家财务', 'merchant_finance', 21, '2', '本商家支付结算能力'),
    (903, '商家跟单', 'merchant_fulfillment', 22, '2', '本商家履约跟踪能力')
) AS x(offset_id, role_name, role_key, role_sort, data_scope, remark)
WHERE t.tenant_type = 'MERCHANT'
ON CONFLICT (role_id) DO UPDATE SET role_name = EXCLUDED.role_name, role_key = EXCLUDED.role_key,
    role_sort = EXCLUDED.role_sort, data_scope = EXCLUDED.data_scope, status = EXCLUDED.status,
    del_flag = EXCLUDED.del_flag, update_by = 'system', update_time = now(), remark = EXCLUDED.remark;

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT r.role_id, m.menu_id, r.tenant_id
FROM sys_role r
JOIN sys_menu m ON m.tenant_id = r.tenant_id
WHERE r.role_key = 'merchant_admin'
  AND (m.i18n_key = 'sales.menu' OR m.parent_id IN (
      r.tenant_id * 1000 + 200, r.tenant_id * 1000 + 230, r.tenant_id * 1000 + 221,
      r.tenant_id * 1000 + 222, r.tenant_id * 1000 + 201, r.tenant_id * 1000 + 223,
      r.tenant_id * 1000 + 224))
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT r.role_id, m.menu_id, r.tenant_id
FROM sys_role r
JOIN sys_menu m ON m.tenant_id = r.tenant_id
WHERE (r.role_key = 'merchant_sales' AND (m.i18n_key = 'sales.menu' OR m.perms IN ('sales:dashboard:view',
          'customer:quote:list', 'customer:quote:query', 'customer:quote:add', 'customer:quote:edit',
          'customer:quote:remove', 'customer:quote:export', 'customer:quote:document', 'customer:quote:email',
          'customer:quote:convert', 'dealer:quick-order:list', 'dealer:quick-order:query', 'dealer:quick-order:add',
          'dealer:quick-order:edit', 'dealer:quick-order:remove', 'dealer:quick-order:submit',
          'dealer:sales:list', 'dealer:sales:query', 'dealer:sales:cancel', 'dealer:sales:document', 'dealer:sales:email')))
   OR (r.role_key = 'merchant_finance' AND (m.i18n_key = 'sales.menu' OR m.perms IN ('sales:dashboard:view',
          'dealer:sales:list', 'dealer:sales:query', 'pay:order:list', 'pay:order:query', 'pay:order:submit',
          'pay:bank:submit', 'pay:credit:use', 'pay:credit:query')))
   OR (r.role_key = 'merchant_fulfillment' AND (m.i18n_key = 'sales.menu' OR m.perms IN ('sales:dashboard:view',
          'dealer:sales:list', 'dealer:sales:query', 'dealer:fulfillment:tracking:list',
          'dealer:fulfillment:shipment:query', 'dealer:fulfillment:receipt:confirm')))
ON CONFLICT DO NOTHING;
