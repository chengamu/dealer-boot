-- 正式销售订单模块：销售单、冻结明细和操作流水。

CREATE TABLE IF NOT EXISTS dealer_sales_document (
    sales_document_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL, merchant_id bigint, merchant_name varchar(200),
    quote_no varchar(64) NOT NULL, order_no varchar(64),
    customer_id bigint NOT NULL, customer_name varchar(200) NOT NULL, company_name varchar(200),
    customer_email varchar(200), customer_phone varchar(64), owner_user_id bigint, owner_name varchar(100),
    project_name varchar(200), customer_po_no varchar(100), valid_until date,
    recipient_name varchar(100), recipient_phone varchar(64), shipping_address varchar(1000),
    currency_code varchar(16) NOT NULL DEFAULT 'USD',
    list_amount numeric(18,2) NOT NULL DEFAULT 0, discount_rate numeric(10,4) NOT NULL DEFAULT 1,
    discount_amount numeric(18,2) NOT NULL DEFAULT 0, product_amount numeric(18,2) NOT NULL DEFAULT 0,
    shipping_amount numeric(18,2) NOT NULL DEFAULT 0, tax_amount numeric(18,2) NOT NULL DEFAULT 0,
    total_amount numeric(18,2) NOT NULL DEFAULT 0,
    document_status varchar(32) NOT NULL DEFAULT 'DRAFT', payment_status varchar(32) NOT NULL DEFAULT 'UNPAID',
    payment_method varchar(32), paid_amount numeric(18,2), payment_reference varchar(100),
    payment_proof_media_id bigint, payment_confirmed_by_id bigint, payment_confirmed_by varchar(100),
    production_status varchar(32) NOT NULL DEFAULT 'PENDING', shipment_status varchar(32) NOT NULL DEFAULT 'UNSHIPPED',
    quoted_time timestamptz, submitted_time timestamptz, paid_time timestamptz, production_start_time timestamptz,
    production_complete_time timestamptz, shipped_time timestamptz, delivered_time timestamptz,
    carrier_name varchar(100), tracking_no varchar(100),
    del_flag char(1) NOT NULL DEFAULT '0', remark varchar(1000),
    create_dept bigint, create_by varchar(64), create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64), update_time timestamptz
);

CREATE INDEX IF NOT EXISTS idx_dealer_sales_quote ON dealer_sales_document (tenant_id, quote_no) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_sales_order ON dealer_sales_document (tenant_id, order_no) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_sales_customer ON dealer_sales_document (tenant_id, customer_id, document_status) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_sales_progress ON dealer_sales_document (document_status, payment_status, production_status, shipment_status) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS dealer_sales_document_item (
    sales_item_id bigint PRIMARY KEY, sales_document_id bigint NOT NULL, tenant_id bigint NOT NULL,
    line_no integer NOT NULL, item_code varchar(64), room_location varchar(200),
    sale_product_id bigint NOT NULL, sale_product_code varchar(100), sale_product_name varchar(200),
    category_id bigint, category_code varchar(100), category_name_cn varchar(200),
    product_type_code varchar(100), product_type_name_cn varchar(200),
    formula_id bigint, formula_version_id bigint, formula_version_label varchar(50),
    order_width_inch numeric(18,4), order_height_inch numeric(18,4), quantity integer NOT NULL DEFAULT 1,
    selected_options_json text, configuration_summary text,
    calculation_status varchar(32), calculation_message varchar(1000),
    list_unit_amount numeric(18,2) NOT NULL DEFAULT 0, list_amount numeric(18,2) NOT NULL DEFAULT 0,
    discount_rate numeric(10,4) NOT NULL DEFAULT 1, unit_amount numeric(18,2) NOT NULL DEFAULT 0,
    product_amount numeric(18,2) NOT NULL DEFAULT 0, shipping_template_id bigint,
    shipping_amount numeric(18,2) NOT NULL DEFAULT 0,
    line_amount numeric(18,2) NOT NULL DEFAULT 0,
    bom_snapshot_json text, pricing_snapshot_json text, shipping_snapshot_json text,
    sort_order integer NOT NULL DEFAULT 0, del_flag char(1) NOT NULL DEFAULT '0', remark varchar(1000),
    create_dept bigint, create_by varchar(64), create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64), update_time timestamptz
);

ALTER TABLE dealer_sales_document_item ADD COLUMN IF NOT EXISTS shipping_template_id bigint;
CREATE INDEX IF NOT EXISTS idx_dealer_sales_item_document ON dealer_sales_document_item (tenant_id, sales_document_id, line_no) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_sales_item_product ON dealer_sales_document_item (sale_product_id, formula_version_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_dealer_sales_item_shipping_template ON dealer_sales_document_item (shipping_template_id) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS dealer_sales_document_event (
    sales_event_id bigint PRIMARY KEY, sales_document_id bigint NOT NULL, tenant_id bigint NOT NULL,
    event_type varchar(64) NOT NULL, from_status varchar(64), to_status varchar(64),
    operator_id bigint, operator_name varchar(100), event_note varchar(1000), occurred_time timestamptz NOT NULL,
    create_dept bigint, create_by varchar(64), create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64), update_time timestamptz
);

CREATE INDEX IF NOT EXISTS idx_dealer_sales_event_document ON dealer_sales_document_event (tenant_id, sales_document_id, occurred_time DESC);

-- 平台销售订单菜单和权限。
INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (26000, 1, '销售订单', 'dealer.sales.menu', 0, 40, 'salesOrders', NULL, '1', '0', 'M', '1', '1', NULL, 'shopping', 'system', now(), '正式销售订单'),
    (26001, 1, '销售单列表', 'dealer.sales.list', 26000, 1, 'salesDocuments', 'dealer-sales/list', '1', '0', 'C', '1', '1', 'dealer:sales:list', 'list', 'system', now(), ''),
    (26002, 1, '销售单查询', 'dealer.sales.permission.query', 26001, 1, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:query', '#', 'system', now(), ''),
    (26003, 1, '销售单新增', 'dealer.sales.permission.add', 26001, 2, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:add', '#', 'system', now(), ''),
    (26004, 1, '销售单修改', 'dealer.sales.permission.edit', 26001, 3, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:edit', '#', 'system', now(), ''),
    (26005, 1, '销售单删除', 'dealer.sales.permission.remove', 26001, 4, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:remove', '#', 'system', now(), ''),
    (26006, 1, '销售单报价', 'dealer.sales.permission.quote', 26001, 5, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:quote', '#', 'system', now(), ''),
    (26007, 1, '销售单提交', 'dealer.sales.permission.submit', 26001, 6, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:submit', '#', 'system', now(), ''),
    (26008, 1, '订单取消', 'dealer.sales.permission.cancel', 26001, 7, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:cancel', '#', 'system', now(), ''),
    (26009, 1, '付款确认', 'dealer.sales.permission.payment', 26001, 8, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:payment', '#', 'system', now(), ''),
    (26010, 1, '生产处理', 'dealer.sales.permission.production', 26001, 9, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:production', '#', 'system', now(), ''),
    (26011, 1, '发货处理', 'dealer.sales.permission.shipment', 26001, 10, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:shipment', '#', 'system', now(), ''),
    (26012, 1, '确认签收', 'dealer.sales.permission.deliver', 26001, 11, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:deliver', '#', 'system', now(), ''),
    (26013, 1, '单据输出', 'dealer.sales.permission.document', 26001, 12, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:document', '#', 'system', now(), ''),
    (26014, 1, '邮件发送', 'dealer.sales.permission.email', 26001, 13, '#', '', '1', '0', 'F', '1', '1', 'dealer:sales:email', '#', 'system', now(), '')
ON CONFLICT (menu_id) DO UPDATE SET menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id, path = EXCLUDED.path, component = EXCLUDED.component,
    perms = EXCLUDED.perms, status = EXCLUDED.status, update_by = 'system', update_time = now();

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT 1, menu_id, 1 FROM sys_menu WHERE menu_id BETWEEN 26000 AND 26014
ON CONFLICT DO NOTHING;

-- 为现有商家租户创建销售入口；后续新租户初始化复用相同权限集合。
INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT t.tenant_id * 1000 + x.offset_id, t.tenant_id, x.menu_name, x.i18n_key,
       CASE WHEN x.offset_id = 200 THEN 0 ELSE t.tenant_id * 1000 + 200 END,
       x.order_num, x.path, x.component, '1', '0', x.menu_type, '1', '1', x.perms, x.icon, 'system', now(), '正式销售订单'
FROM sys_tenant t
CROSS JOIN (VALUES
    (200, '销售订单', 'dealer.sales.menu', 40, 'salesOrders', NULL, 'M', NULL, 'shopping'),
    (201, '销售单列表', 'dealer.sales.list', 1, 'salesDocuments', 'dealer-sales/list', 'C', 'dealer:sales:list', 'list'),
    (202, '销售单查询', 'dealer.sales.permission.query', 1, '#', '', 'F', 'dealer:sales:query', '#'),
    (203, '销售单新增', 'dealer.sales.permission.add', 2, '#', '', 'F', 'dealer:sales:add', '#'),
    (204, '销售单修改', 'dealer.sales.permission.edit', 3, '#', '', 'F', 'dealer:sales:edit', '#'),
    (205, '销售单删除', 'dealer.sales.permission.remove', 4, '#', '', 'F', 'dealer:sales:remove', '#'),
    (206, '销售单报价', 'dealer.sales.permission.quote', 5, '#', '', 'F', 'dealer:sales:quote', '#'),
    (207, '销售单提交', 'dealer.sales.permission.submit', 6, '#', '', 'F', 'dealer:sales:submit', '#'),
    (208, '订单取消', 'dealer.sales.permission.cancel', 7, '#', '', 'F', 'dealer:sales:cancel', '#'),
    (212, '确认签收', 'dealer.sales.permission.deliver', 11, '#', '', 'F', 'dealer:sales:deliver', '#'),
    (213, '单据输出', 'dealer.sales.permission.document', 12, '#', '', 'F', 'dealer:sales:document', '#'),
    (214, '邮件发送', 'dealer.sales.permission.email', 13, '#', '', 'F', 'dealer:sales:email', '#')
) AS x(offset_id, menu_name, i18n_key, order_num, path, component, menu_type, perms, icon)
WHERE t.tenant_type = 'MERCHANT'
ON CONFLICT (menu_id) DO UPDATE SET menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id, component = EXCLUDED.component, perms = EXCLUDED.perms,
    status = EXCLUDED.status, update_by = 'system', update_time = now();

WITH RECURSIVE sales_menus AS (
    SELECT r.role_id, r.tenant_id, m.menu_id, m.parent_id
    FROM sys_role r JOIN sys_menu m ON m.tenant_id = r.tenant_id
    WHERE r.role_key IN ('merchant_admin', 'merchant_store', 'merchant_employee') AND r.del_flag = '0'
      AND (m.perms LIKE 'dealer:sales:%' OR m.i18n_key = 'dealer.sales.menu')
    UNION
    SELECT s.role_id, s.tenant_id, p.menu_id, p.parent_id FROM sales_menus s
    JOIN sys_menu p ON p.menu_id = s.parent_id AND p.tenant_id = s.tenant_id
)
INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT DISTINCT role_id, menu_id, tenant_id FROM sales_menus ON CONFLICT DO NOTHING;
