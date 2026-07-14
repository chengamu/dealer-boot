-- 销售业务最终初始化结构：快速下单、正式订单、履约和权限。

CREATE TABLE IF NOT EXISTS dealer_quick_order (
    quick_order_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    business_origin varchar(20) NOT NULL CHECK (business_origin IN ('INTERNAL', 'MERCHANT')),
    sales_store_id bigint,
    dept_id bigint,
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
ALTER TABLE dealer_quick_order ADD COLUMN IF NOT EXISTS business_origin varchar(20);
ALTER TABLE dealer_quick_order ADD COLUMN IF NOT EXISTS sales_store_id bigint;
ALTER TABLE dealer_quick_order ADD COLUMN IF NOT EXISTS dept_id bigint;
UPDATE dealer_quick_order SET business_origin = CASE WHEN tenant_id = 1 THEN 'INTERNAL' ELSE 'MERCHANT' END
WHERE business_origin IS NULL;
ALTER TABLE dealer_quick_order ALTER COLUMN business_origin SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_dealer_quick_order_customer ON dealer_quick_order (tenant_id, customer_id, status) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_quick_order_scope ON dealer_quick_order (tenant_id, business_origin, dept_id, owner_user_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_quick_order_store ON dealer_quick_order (sales_store_id, status) WHERE del_flag = '0' AND sales_store_id IS NOT NULL;
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
    business_origin varchar(20) NOT NULL CHECK (business_origin IN ('INTERNAL', 'MERCHANT')),
    sales_store_id bigint,
    dept_id bigint,
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
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS business_origin varchar(20);
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS sales_store_id bigint;
ALTER TABLE dealer_sales_document ADD COLUMN IF NOT EXISTS dept_id bigint;
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
UPDATE dealer_sales_document SET business_origin = CASE WHEN tenant_id = 1 THEN 'INTERNAL' ELSE 'MERCHANT' END
WHERE business_origin IS NULL;
UPDATE dealer_sales_document
SET source_no = COALESCE(quote_no, order_no, sales_document_id::varchar)
WHERE source_no IS NULL;
UPDATE dealer_sales_document
SET order_no = COALESCE(order_no, quote_no, 'SO-' || sales_document_id::varchar)
WHERE order_no IS NULL;
ALTER TABLE dealer_sales_document ALTER COLUMN source_type SET NOT NULL;
ALTER TABLE dealer_sales_document ALTER COLUMN business_origin SET NOT NULL;
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
CREATE INDEX IF NOT EXISTS idx_dealer_sales_scope ON dealer_sales_document (tenant_id, business_origin, dept_id, owner_user_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_sales_store ON dealer_sales_document (sales_store_id, document_status) WHERE del_flag = '0' AND sales_store_id IS NOT NULL;
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
DROP INDEX IF EXISTS uk_dealer_tracking_provider_event;
DROP INDEX IF EXISTS uk_dealer_tracking_fallback_event;
CREATE UNIQUE INDEX IF NOT EXISTS uk_dealer_tracking_provider_event
    ON dealer_tracking_event (shipment_id, carrier_code, provider_event_id)
    WHERE provider_event_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_dealer_tracking_fallback_event
    ON dealer_tracking_event (shipment_id, carrier_code, tracking_no, event_code, occurred_time)
    WHERE provider_event_id IS NULL;


-- =====================================================
-- 阶段 12 最终菜单：销售、订单、跟踪、平台运营与工厂作业
-- =====================================================

DELETE FROM sys_role_menu
WHERE menu_id IN (26023, 26024, 26025)
   OR menu_id BETWEEN 26101 AND 26215;
DELETE FROM sys_menu
WHERE menu_id BETWEEN 26101 AND 26215
   OR menu_id IN (26023, 26024, 26025);

-- 角色和菜单由平台统一维护，商家租户不保留菜单副本。
DELETE FROM sys_role_menu rm
USING sys_menu m
WHERE rm.menu_id = m.menu_id
  AND m.tenant_id <> 1;
DELETE FROM sys_menu WHERE tenant_id <> 1;

INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (26000, 1, '销售业务', 'sales.menu', 0, 30, 'sales', NULL, '1', '0', 'M', '1', '1', NULL, 'sales', 'system', now(), '内部销售业务'),
    (26020, 1, '销售首页', 'sales.dashboard.menu', 26000, 1, 'dashboard', 'sales/dashboard', '1', '0', 'C', '1', '1', 'sales:dashboard:view', 'dashboard', 'system', now(), ''),
    (20230, 1, '工程测算', 'customer.quote.estimate.menu', 26000, 2, 'estimates', 'customer/quote-workbench', '1', '0', 'C', '1', '1', 'customer:quote:list', 'calculator', 'system', now(), ''),
    (26021, 1, '报价管理', 'customer.quote.management.menu', 26000, 3, 'quotes', 'customer/quotes', '1', '0', 'C', '1', '1', 'customer:quote:list', 'quote', 'system', now(), ''),
    (26022, 1, '快速下单', 'dealer.quickOrder.menu', 26000, 4, 'quickOrders', 'dealer-quick-order/list', '1', '0', 'C', '1', '1', 'dealer:quick-order:list', 'cart', 'system', now(), ''),
    (26024, 1, '下单配置', 'dealer.quickOrder.workbenchTitle', 26000, 41, 'quickOrders/workbench', 'dealer-quick-order/workbench', '1', '0', 'C', '0', '1', 'dealer:quick-order:add', '#', 'system', now(), '快速下单隐藏工作台'),
    (26025, 1, '订单复核', 'dealer.quickOrder.reviewTitle', 26000, 42, 'quickOrders/review', 'dealer-quick-order/review', '1', '0', 'C', '0', '1', 'dealer:quick-order:edit', '#', 'system', now(), '快速下单隐藏复核页'),
    (26300, 1, '订单管理', 'dealer.order.menu', 0, 40, 'orders', NULL, '1', '0', 'M', '1', '1', NULL, 'order', 'system', now(), '内部销售订单'),
    (26001, 1, '销售订单', 'dealer.sales.list', 26300, 1, 'salesDocuments', 'dealer-sales/list', '1', '0', 'C', '1', '1', 'dealer:sales:list', 'order', 'system', now(), ''),
    (26400, 1, '订单跟踪', 'dealer.tracking.menu', 0, 60, 'tracking', NULL, '1', '0', 'M', '1', '1', NULL, 'delivery', 'system', now(), '内部销售履约只读跟踪'),
    (26401, 1, '生产进度', 'dealer.tracking.productionMenu', 26400, 1, 'production', 'dealer-fulfillment/business-production', '1', '0', 'C', '1', '1', 'dealer:fulfillment:progress:list', 'build', 'system', now(), '阶段 12 薄壳'),
    (26402, 1, '发货包裹', 'dealer.tracking.shipments', 26400, 2, 'shipments', 'dealer-fulfillment/business-shipments', '1', '0', 'C', '1', '1', 'dealer:fulfillment:progress:list', 'delivery', 'system', now(), '阶段 12 薄壳'),
    (26403, 1, '物流跟踪', 'dealer.tracking.logisticsMenu', 26400, 3, 'logistics', 'dealer-fulfillment/business-tracking', '1', '0', 'C', '1', '1', 'dealer:fulfillment:progress:list', 'tracking', 'system', now(), '阶段 12 薄壳'),
    (26500, 1, '生产作业', 'factory.production.menu', 0, 200, 'factoryProduction', NULL, '1', '0', 'M', '1', '1', NULL, 'build', 'system', now(), '工厂生产'),
    (26501, 1, '生产队列', 'factory.production.queue', 26500, 1, 'queue', 'dealer-fulfillment/factory-production', '1', '0', 'C', '1', '1', 'dealer:fulfillment:factory:production:list', 'build', 'system', now(), '阶段 12 薄壳'),
    (26502, 1, '生产清单', 'factory.production.documents', 26500, 2, 'documents', 'dealer-fulfillment/factory-production-documents', '1', '0', 'C', '1', '1', 'dealer:fulfillment:production:document', 'clipboard', 'system', now(), '阶段 12 薄壳'),
    (26600, 1, '发货作业', 'factory.shipping.menu', 0, 210, 'factoryShipping', NULL, '1', '0', 'M', '1', '1', NULL, 'delivery', 'system', now(), '工厂发货'),
    (26601, 1, '发货队列', 'factory.shipping.queue', 26600, 1, 'queue', 'dealer-fulfillment/factory-shipping', '1', '0', 'C', '1', '1', 'dealer:fulfillment:factory:shipment:list', 'delivery', 'system', now(), '阶段 12 薄壳'),
    (26602, 1, '包裹管理', 'factory.shipping.packages', 26600, 2, 'packages', 'dealer-fulfillment/factory-packages', '1', '0', 'C', '1', '1', 'dealer:fulfillment:factory:shipment:list', 'order', 'system', now(), '阶段 12 薄壳'),
    (26603, 1, '物流跟踪', 'factory.shipping.tracking', 26600, 3, 'tracking', 'dealer-fulfillment/factory-tracking', '1', '0', 'C', '1', '1', 'dealer:fulfillment:factory:tracking:list', 'tracking', 'system', now(), '阶段 12 薄壳'),
    (26700, 1, '销售运营', 'platform.sales.menu', 0, 420, 'salesOperations', NULL, '1', '0', 'M', '1', '1', NULL, 'report', 'system', now(), '平台销售运营'),
    (26701, 1, '运营总览', 'platform.sales.dashboard', 26700, 1, 'dashboard', 'platform-sales/dashboard', '1', '0', 'C', '1', '1', 'platform:sales:dashboard:view', 'dashboard', 'system', now(), '阶段 12 薄壳'),
    (26702, 1, '报价查询', 'platform.sales.quotes', 26700, 2, 'quotes', 'platform-sales/quotes', '1', '0', 'C', '1', '1', 'platform:sales:quote:list', 'quote', 'system', now(), '阶段 12 薄壳'),
    (26703, 1, '下单查询', 'platform.sales.quickOrders', 26700, 3, 'quickOrders', 'platform-sales/quick-orders', '1', '0', 'C', '1', '1', 'platform:sales:quick-order:list', 'cart', 'system', now(), '阶段 12 薄壳'),
    (26704, 1, '订单查询', 'platform.sales.orders', 26700, 4, 'orders', 'platform-sales/orders', '1', '0', 'C', '1', '1', 'platform:sales:order:list', 'order', 'system', now(), '阶段 12 薄壳'),
    (26800, 1, '履约中心', 'platform.fulfillment.menu', 0, 430, 'fulfillment', NULL, '1', '0', 'M', '1', '1', NULL, 'delivery', 'system', now(), '平台履约'),
    (26801, 1, '生产管理', 'platform.fulfillment.productionMenu', 26800, 1, 'production', 'dealer-fulfillment/platform-production', '1', '0', 'C', '1', '1', 'dealer:fulfillment:admin:list', 'build', 'system', now(), '阶段 12 薄壳'),
    (26802, 1, '发货管理', 'platform.fulfillment.shipping', 26800, 2, 'shipping', 'dealer-fulfillment/platform-shipping', '1', '0', 'C', '1', '1', 'dealer:fulfillment:admin:list', 'delivery', 'system', now(), '阶段 12 薄壳'),
    (26803, 1, '包裹查询', 'platform.fulfillment.packages', 26800, 3, 'packages', 'dealer-fulfillment/platform-packages', '1', '0', 'C', '1', '1', 'dealer:fulfillment:admin:list', 'order', 'system', now(), '阶段 12 薄壳'),
    (26804, 1, '物流跟踪', 'platform.fulfillment.trackingMenu', 26800, 4, 'tracking', 'dealer-fulfillment/platform-tracking', '1', '0', 'C', '1', '1', 'dealer:fulfillment:admin:list', 'tracking', 'system', now(), '阶段 12 薄壳')
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
    (20231, '报价查询', 'customer.quote.permission.query', 20230, 1, 'customer:quote:query'),
    (20232, '报价新增', 'customer.quote.permission.add', 20230, 2, 'customer:quote:add'),
    (20233, '报价修改', 'customer.quote.permission.edit', 20230, 3, 'customer:quote:edit'),
    (20234, '报价删除', 'customer.quote.permission.remove', 20230, 4, 'customer:quote:remove'),
    (20235, '报价导出', 'customer.quote.permission.export', 20230, 5, 'customer:quote:export'),
    (20236, '报价单据', 'customer.quote.permission.document', 20230, 6, 'customer:quote:document'),
    (20237, '报价邮件', 'customer.quote.permission.email', 20230, 7, 'customer:quote:email'),
    (20238, '转销售单', 'customer.quote.permission.convert', 20230, 8, 'customer:quote:convert'),
    (26031, '下单查询', 'dealer.quickOrder.query', 26022, 1, 'dealer:quick-order:query'),
    (26032, '下单新增', 'dealer.quickOrder.add', 26022, 2, 'dealer:quick-order:add'),
    (26033, '下单修改', 'dealer.quickOrder.edit', 26022, 3, 'dealer:quick-order:edit'),
    (26034, '下单删除', 'dealer.quickOrder.remove', 26022, 4, 'dealer:quick-order:remove'),
    (26035, '下单提交', 'dealer.quickOrder.submit', 26022, 5, 'dealer:quick-order:submit'),
    (26002, '订单查询', 'dealer.sales.permission.query', 26001, 1, 'dealer:sales:query'),
    (26008, '订单取消', 'dealer.sales.permission.cancel', 26001, 2, 'dealer:sales:cancel'),
    (26013, '单据输出', 'dealer.sales.permission.document', 26001, 3, 'dealer:sales:document'),
    (26014, '邮件发送', 'dealer.sales.permission.email', 26001, 4, 'dealer:sales:email'),
    (26411, '进度查询', 'dealer.tracking.production.query', 26401, 1, 'dealer:fulfillment:progress:query'),
    (26421, '包裹查询', 'dealer.tracking.shipment.query', 26402, 1, 'dealer:fulfillment:progress:query'),
    (26431, '物流查询', 'dealer.tracking.logistics.query', 26403, 1, 'dealer:fulfillment:progress:tracking'),
    (26432, '确认收货', 'dealer.tracking.receipt', 26403, 2, 'dealer:fulfillment:progress:receipt:confirm'),
    (26511, '生产查询', 'factory.production.query', 26501, 1, 'dealer:fulfillment:factory:production:query'),
    (26512, '开始生产', 'factory.production.start', 26501, 2, 'dealer:fulfillment:factory:production:start'),
    (26513, '完成生产', 'factory.production.complete', 26501, 3, 'dealer:fulfillment:factory:production:complete'),
    (26610, '发货查询', 'factory.shipping.query', 26601, 1, 'dealer:fulfillment:factory:shipment:query'),
    (26611, '包裹新增', 'factory.shipping.add', 26602, 1, 'dealer:fulfillment:factory:shipment:add'),
    (26612, '包裹修改', 'factory.shipping.edit', 26602, 2, 'dealer:fulfillment:factory:shipment:edit'),
    (26613, '包裹删除', 'factory.shipping.remove', 26602, 3, 'dealer:fulfillment:factory:shipment:remove'),
    (26614, '确认发货', 'factory.shipping.dispatch', 26602, 4, 'dealer:fulfillment:factory:shipment:dispatch'),
    (26621, '物流查询', 'factory.tracking.query', 26603, 1, 'dealer:fulfillment:factory:tracking:list'),
    (26622, '同步物流', 'factory.tracking.sync', 26603, 2, 'dealer:fulfillment:factory:tracking:sync'),
    (26711, '报价详情', 'platform.sales.quote.query', 26702, 1, 'platform:sales:quote:query'),
    (26712, '报价单据', 'platform.sales.quote.document', 26702, 2, 'platform:sales:quote:document'),
    (26713, '报价导出', 'platform.sales.quote.export', 26702, 3, 'platform:sales:quote:export'),
    (26721, '下单详情', 'platform.sales.quickOrder.query', 26703, 1, 'platform:sales:quick-order:query'),
    (26722, '下单导出', 'platform.sales.quickOrder.export', 26703, 2, 'platform:sales:quick-order:export'),
    (26731, '订单详情', 'platform.sales.order.query', 26704, 1, 'platform:sales:order:query'),
    (26732, '订单单据', 'platform.sales.order.document', 26704, 2, 'platform:sales:order:document'),
    (26733, '订单导出', 'platform.sales.order.export', 26704, 3, 'platform:sales:order:export'),
    (26705, '商家选项', 'platform.sales.merchant.options', 26700, 90, 'system:merchant:profile:options'),
    (26706, '门店选项', 'platform.sales.store.options', 26700, 91, 'system:sales-store:options'),
    (26811, '生产查询', 'platform.fulfillment.production.query', 26801, 1, 'dealer:fulfillment:admin:query'),
    (26814, '生产单据', 'platform.fulfillment.production.document', 26801, 4, 'dealer:fulfillment:production:document'),
    (26831, '物流查询', 'platform.fulfillment.tracking.query', 26804, 1, 'dealer:fulfillment:admin:tracking'),
    (26832, '同步物流', 'platform.fulfillment.tracking.sync', 26804, 2, 'dealer:fulfillment:admin:tracking:sync'),
    (26833, '异常签收', 'platform.fulfillment.receipt.override', 26804, 3, 'dealer:fulfillment:admin:receipt:override'),
    (26834, '商家选项', 'platform.fulfillment.merchant.options', 26800, 90, 'system:merchant:profile:options'),
    (26835, '门店选项', 'platform.fulfillment.store.options', 26800, 91, 'system:sales-store:options')
) AS x(menu_id, menu_name, i18n_key, parent_id, order_num, perms)
ON CONFLICT (menu_id) DO UPDATE
SET tenant_id = EXCLUDED.tenant_id, menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id, order_num = EXCLUDED.order_num, perms = EXCLUDED.perms,
    status = EXCLUDED.status, update_by = 'system', update_time = now();

INSERT INTO sys_role (role_id, tenant_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES
    (260901, 1, '业务主管', 'platform_sales_manager', 20, '1', true, true, '1', '0', 'system', now(), '平台商家与销售业务管理'),
    (260902, 1, '平台销售', 'platform_sales', 21, '5', true, true, '1', '0', 'system', now(), '平台内部销售本人范围'),
    (260904, 1, '工厂生产', 'factory_production', 40, '1', true, true, '1', '0', 'system', now(), '平台财务、履约与工厂生产作业')
ON CONFLICT (role_id) DO UPDATE
SET role_name = EXCLUDED.role_name, role_key = EXCLUDED.role_key, role_sort = EXCLUDED.role_sort,
    data_scope = EXCLUDED.data_scope, status = EXCLUDED.status, del_flag = EXCLUDED.del_flag,
    update_by = 'system', update_time = now(), remark = EXCLUDED.remark;

DELETE FROM sys_user_role ur
USING sys_role r
WHERE ur.role_id = r.role_id
  AND r.tenant_id = 1
  AND r.role_key IN ('platform_merchant_operations', 'platform_sales_operations',
                     'platform_fulfillment', 'factory_shipping');

DELETE FROM sys_role_menu rm
USING sys_role r
WHERE rm.role_id = r.role_id
  AND r.tenant_id = 1
  AND r.role_key IN ('platform_merchant_operations', 'platform_sales_operations',
                     'platform_fulfillment', 'factory_shipping');

DELETE FROM sys_role_dept rd
USING sys_role r
WHERE rd.role_id = r.role_id
  AND r.tenant_id = 1
  AND r.role_key IN ('platform_merchant_operations', 'platform_sales_operations',
                     'platform_fulfillment', 'factory_shipping');

DELETE FROM sys_role
WHERE tenant_id = 1
  AND role_key IN ('platform_merchant_operations', 'platform_sales_operations',
                   'platform_fulfillment', 'factory_shipping');

DELETE FROM sys_user_role ur
USING sys_role r
WHERE ur.role_id = r.role_id
  AND (r.tenant_id <> 1
       OR r.role_key IN ('merchant_store', 'merchant_sales', 'merchant_finance', 'merchant_fulfillment'));

DELETE FROM sys_role_menu rm
USING sys_role r
WHERE rm.role_id = r.role_id
  AND (r.tenant_id <> 1
       OR r.role_key IN ('merchant_store', 'merchant_sales', 'merchant_finance', 'merchant_fulfillment'));

DELETE FROM sys_role_dept rd
USING sys_role r
WHERE rd.role_id = r.role_id
  AND (r.tenant_id <> 1
       OR r.role_key IN ('merchant_store', 'merchant_sales', 'merchant_finance', 'merchant_fulfillment'));

DELETE FROM sys_role
WHERE tenant_id <> 1
   OR role_key IN ('merchant_store', 'merchant_sales', 'merchant_finance', 'merchant_fulfillment');

DELETE FROM sys_role_menu rm
USING sys_role r
WHERE rm.role_id = r.role_id
  AND r.tenant_id = 1
  AND r.role_key IN ('merchant_admin', 'merchant_employee');

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT r.role_id, m.menu_id, r.tenant_id
FROM sys_role r
JOIN sys_menu m ON m.tenant_id = 1
WHERE (r.role_key IN ('platform_sales_manager', 'platform_sales')
       AND (m.menu_id IN (20200, 26000, 26300) OR m.parent_id IN (20200, 20201, 26000, 20230, 26021, 26022, 26300, 26001)))
   OR (r.role_key = 'platform_sales_manager'
       AND (m.menu_id IN (19500, 26700)
            OR m.parent_id IN (19500, 107, 108, 20009, 20100, 20120,
                               26700, 26702, 26703, 26704)))
   OR (r.role_key = 'factory_production'
       AND (m.menu_id IN (26500, 26600, 26800)
            OR m.parent_id IN (26500, 26501, 26502, 26600, 26601, 26602, 26603,
                               26800, 26801, 26802, 26803, 26804)))
   OR (r.role_key IN ('merchant_admin', 'merchant_employee')
       AND r.tenant_id = 1
       AND (m.menu_id IN (20200, 26000, 26300, 26400)
            OR m.parent_id IN (20200, 20201, 26000, 20230, 26021, 26022,
                               26300, 26001, 26400, 26401, 26402, 26403)))
   OR (r.role_key = 'merchant_admin'
       AND r.tenant_id = 1
       AND m.menu_id BETWEEN 28000 AND 28015)
   OR (r.role_key = 'merchant_employee'
       AND r.tenant_id = 1
       AND m.menu_id IN (28000, 28001, 28002))
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT 1, menu_id, 1
FROM sys_menu
WHERE tenant_id = 1
  AND (menu_id IN (26000, 26300, 26400, 26500, 26600, 26700, 26800)
       OR parent_id BETWEEN 26000 AND 26899)
ON CONFLICT DO NOTHING;

UPDATE sys_role r
SET default_menu_id = m.menu_id, update_by = 'system', update_time = now()
FROM sys_menu m
WHERE m.tenant_id = r.tenant_id
  AND m.menu_type = 'C'
  AND m.status = '1'
  AND m.visible = '1'
  AND (
    (r.role_key IN ('platform_sales_manager', 'platform_sales', 'merchant_admin', 'merchant_employee')
      AND m.component = 'sales/dashboard')
    OR (r.role_key = 'factory_production' AND m.component = 'dealer-fulfillment/factory-production')
  );
