-- base-boot PostgreSQL baseline for dealer.
-- Platform/factory tenant is tenant_id = 1.
-- tenant_id = 0 is not a valid business tenant and must not be used as fallback.

CREATE TABLE IF NOT EXISTS sys_tenant (
    tenant_id bigint PRIMARY KEY CHECK (tenant_id <> 0),
    tenant_name varchar(100) NOT NULL,
    tenant_type varchar(20) NOT NULL,
    contact_name varchar(50),
    contact_email varchar(100),
    country varchar(50),
    status char(1) NOT NULL DEFAULT '1',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE INDEX IF NOT EXISTS idx_sys_tenant_status_type ON sys_tenant (status, tenant_type);
CREATE INDEX IF NOT EXISTS idx_sys_tenant_contact_email ON sys_tenant (contact_email) WHERE contact_email IS NOT NULL;

CREATE TABLE IF NOT EXISTS sys_tenant_apply (
    apply_id bigint PRIMARY KEY,
    tenant_id bigint CHECK (tenant_id IS NULL OR tenant_id <> 0),
    merchant_name varchar(100) NOT NULL,
    company_name varchar(100),
    contact_first_name varchar(50),
    contact_last_name varchar(50),
    contact_name varchar(100),
    email varchar(100) NOT NULL,
    apply_locale varchar(20) NOT NULL DEFAULT 'en_US',
    office_phone varchar(50),
    mobile_phone varchar(50),
    country varchar(50) NOT NULL,
    state varchar(50),
    city varchar(50),
    address_line1 varchar(255),
    address_line2 varchar(255),
    postal_code varchar(20),
    remark varchar(500),
    status varchar(20) NOT NULL DEFAULT 'PENDING',
    audit_by varchar(64),
    audit_by_id bigint,
    audit_time timestamptz,
    reject_reason varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_tenant_apply_email_active
    ON sys_tenant_apply (email)
    WHERE status IN ('PENDING', 'APPROVED');
CREATE INDEX IF NOT EXISTS idx_sys_tenant_apply_status_time ON sys_tenant_apply (status, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_sys_tenant_apply_tenant ON sys_tenant_apply (tenant_id);

CREATE TABLE IF NOT EXISTS merchant_profile (
    merchant_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    merchant_name varchar(100) NOT NULL,
    company_name varchar(100),
    contact_first_name varchar(50),
    contact_last_name varchar(50),
    contact_name varchar(100),
    primary_email varchar(100) NOT NULL,
    office_phone varchar(50),
    mobile_phone varchar(50),
    country varchar(50) NOT NULL,
    state varchar(50),
    city varchar(50),
    address_line1 varchar(255),
    address_line2 varchar(255),
    postal_code varchar(20),
    level_id bigint,
    level_code varchar(50),
    level_name varchar(100),
    discount_rate numeric(10,4),
    credit_limit numeric(18,2),
    credit_term_days integer CHECK (credit_term_days IS NULL OR credit_term_days > 0),
    status char(1) NOT NULL DEFAULT '1',
    audit_status varchar(20) NOT NULL DEFAULT 'APPROVED',
    audit_by varchar(64),
    audit_by_id bigint,
    audit_time timestamptz,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
ALTER TABLE merchant_profile ADD COLUMN IF NOT EXISTS credit_term_days integer;
ALTER TABLE merchant_profile DROP CONSTRAINT IF EXISTS merchant_profile_credit_term_days_check;
ALTER TABLE merchant_profile ADD CONSTRAINT merchant_profile_credit_term_days_check
    CHECK (credit_term_days IS NULL OR credit_term_days > 0);
CREATE UNIQUE INDEX IF NOT EXISTS uk_merchant_profile_tenant ON merchant_profile (tenant_id);
CREATE INDEX IF NOT EXISTS idx_merchant_profile_primary_email ON merchant_profile (primary_email);
CREATE INDEX IF NOT EXISTS idx_merchant_profile_status ON merchant_profile (status, audit_status);
CREATE INDEX IF NOT EXISTS idx_merchant_profile_level ON merchant_profile (level_id, status);

CREATE TABLE IF NOT EXISTS merchant_level (
    level_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    level_code varchar(50) NOT NULL,
    level_name varchar(100) NOT NULL,
    default_discount_rate numeric(10,4) NOT NULL DEFAULT 1,
    default_credit_limit numeric(18,2) NOT NULL DEFAULT 0,
    default_flag boolean NOT NULL DEFAULT false,
    sort_order integer DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'DISABLED',
    del_flag char(1) NOT NULL DEFAULT '0',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE INDEX IF NOT EXISTS idx_merchant_level_code_status ON merchant_level (tenant_id, level_code, status);
CREATE INDEX IF NOT EXISTS idx_merchant_level_sort ON merchant_level (tenant_id, status, sort_order);

CREATE TABLE IF NOT EXISTS merchant_level_discount (
    discount_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    level_id bigint NOT NULL,
    level_code varchar(50) NOT NULL,
    level_name varchar(100) NOT NULL,
    category_id bigint NOT NULL,
    category_code varchar(80) NOT NULL,
    category_name_cn varchar(200) NOT NULL,
    product_type_code varchar(80) NOT NULL,
    product_type_name_cn varchar(200) NOT NULL,
    discount_rate numeric(10,4) NOT NULL,
    sort_order integer DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'DISABLED',
    del_flag char(1) NOT NULL DEFAULT '0',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE INDEX IF NOT EXISTS idx_merchant_level_discount_lookup ON merchant_level_discount (tenant_id, level_id, category_id, product_type_code, status);

CREATE TABLE IF NOT EXISTS customer_profile (
    customer_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    business_origin varchar(20) NOT NULL CHECK (business_origin IN ('INTERNAL', 'MERCHANT')),
    sales_store_id bigint,
    dept_id bigint,
    merchant_id bigint,
    merchant_name varchar(100),
    customer_name varchar(120) NOT NULL,
    company_name varchar(120),
    email varchar(120) NOT NULL,
    phone varchar(50),
    customer_type varchar(30),
    country varchar(50),
    state varchar(50),
    city varchar(50),
    address_line1 varchar(255),
    address_line2 varchar(255),
    postal_code varchar(20),
    owner_user_id bigint,
    owner_name varchar(100),
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag char(1) NOT NULL DEFAULT '0',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE INDEX IF NOT EXISTS idx_customer_profile_tenant_email ON customer_profile (tenant_id, email);
ALTER TABLE customer_profile ADD COLUMN IF NOT EXISTS business_origin varchar(20);
ALTER TABLE customer_profile ADD COLUMN IF NOT EXISTS sales_store_id bigint;
ALTER TABLE customer_profile ADD COLUMN IF NOT EXISTS dept_id bigint;
UPDATE customer_profile SET business_origin = CASE WHEN tenant_id = 1 THEN 'INTERNAL' ELSE 'MERCHANT' END
WHERE business_origin IS NULL;
ALTER TABLE customer_profile ALTER COLUMN business_origin SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_customer_profile_tenant_owner ON customer_profile (tenant_id, owner_user_id, status);
CREATE INDEX IF NOT EXISTS idx_customer_profile_scope ON customer_profile (tenant_id, business_origin, dept_id, owner_user_id);
CREATE INDEX IF NOT EXISTS idx_customer_profile_store ON customer_profile (sales_store_id, status) WHERE sales_store_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_customer_profile_merchant ON customer_profile (merchant_id, status);

CREATE TABLE IF NOT EXISTS customer_quote (
    quote_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    business_origin varchar(20) NOT NULL CHECK (business_origin IN ('INTERNAL', 'MERCHANT')),
    sales_store_id bigint,
    dept_id bigint,
    quote_no varchar(50) NOT NULL,
    customer_id bigint NOT NULL,
    customer_name varchar(120) NOT NULL,
    company_name varchar(120),
    customer_email varchar(120),
    customer_phone varchar(64),
    project_name varchar(160) NOT NULL,
    customer_po_no varchar(100),
    recipient_name varchar(100),
    recipient_phone varchar(64),
    shipping_address varchar(1000),
    quote_language varchar(20) NOT NULL DEFAULT 'EN_US',
    valid_until date,
    owner_user_id bigint,
    owner_name varchar(100),
    currency_code varchar(10),
    status varchar(20) NOT NULL DEFAULT 'DRAFT',
    product_amount numeric(18,2) NOT NULL DEFAULT 0,
    shipping_amount numeric(18,2) NOT NULL DEFAULT 0,
    discount_amount numeric(18,2) NOT NULL DEFAULT 0,
    total_amount numeric(18,2) NOT NULL DEFAULT 0,
    confirmed_by_id bigint,
    confirmed_by varchar(64),
    confirmed_time timestamptz,
    sales_document_id bigint,
    order_no varchar(64),
    converted_by_id bigint,
    converted_by varchar(64),
    converted_time timestamptz,
    del_flag char(1) NOT NULL DEFAULT '0',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE INDEX IF NOT EXISTS idx_customer_quote_no ON customer_quote (tenant_id, quote_no) WHERE del_flag = '0';
ALTER TABLE customer_quote ADD COLUMN IF NOT EXISTS business_origin varchar(20);
ALTER TABLE customer_quote ADD COLUMN IF NOT EXISTS sales_store_id bigint;
ALTER TABLE customer_quote ADD COLUMN IF NOT EXISTS dept_id bigint;
UPDATE customer_quote SET business_origin = CASE WHEN tenant_id = 1 THEN 'INTERNAL' ELSE 'MERCHANT' END
WHERE business_origin IS NULL;
ALTER TABLE customer_quote ALTER COLUMN business_origin SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_customer_quote_customer ON customer_quote (tenant_id, customer_id, status) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_customer_quote_scope ON customer_quote (tenant_id, business_origin, dept_id, owner_user_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_customer_quote_store ON customer_quote (sales_store_id, status) WHERE del_flag = '0' AND sales_store_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_customer_quote_updated ON customer_quote (tenant_id, update_time DESC, create_time DESC) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS customer_quote_item (
    quote_item_id bigint PRIMARY KEY,
    quote_id bigint NOT NULL,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    line_no integer NOT NULL,
    room_location varchar(120),
    sale_product_id bigint,
    sale_product_code varchar(80),
    sale_product_name varchar(200),
    category_id bigint,
    category_code varchar(100),
    category_name_cn varchar(200),
    product_type_code varchar(100),
    product_type_name_cn varchar(200),
    formula_id bigint,
    formula_version_id bigint,
    formula_version_label varchar(30),
    order_width_inch numeric(18,4),
    order_height_inch numeric(18,4),
    quantity integer NOT NULL DEFAULT 1,
    selected_options_json text,
    selected_options_summary_cn text,
    selected_options_summary_en text,
    calculation_status varchar(20) NOT NULL DEFAULT 'PENDING',
    calculation_message varchar(500),
    unit_amount numeric(18,2) NOT NULL DEFAULT 0,
    product_amount numeric(18,2) NOT NULL DEFAULT 0,
    unit_shipping_amount numeric(18,2) NOT NULL DEFAULT 0,
    shipping_template_id bigint,
    shipping_amount numeric(18,2) NOT NULL DEFAULT 0,
    discount_amount numeric(18,2) NOT NULL DEFAULT 0,
    line_amount numeric(18,2) NOT NULL DEFAULT 0,
    bom_snapshot_json text,
    pricing_snapshot_json text,
    shipping_snapshot_json text,
    sort_order integer NOT NULL DEFAULT 0,
    del_flag char(1) NOT NULL DEFAULT '0',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE INDEX IF NOT EXISTS idx_customer_quote_item_quote ON customer_quote_item (tenant_id, quote_id, line_no) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_customer_quote_item_sale_product ON customer_quote_item (sale_product_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_customer_quote_item_formula_version ON customer_quote_item (formula_version_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_customer_quote_item_shipping_template ON customer_quote_item (shipping_template_id) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    parent_id bigint DEFAULT 0,
    ancestors varchar(500) DEFAULT '',
    dept_name varchar(30) NOT NULL,
    order_num integer DEFAULT 0,
    leader varchar(20),
    phone varchar(11),
    email varchar(50),
    status char(1) DEFAULT '1',
    del_flag char(1) DEFAULT '0',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_dept_tenant_parent_name_active ON sys_dept (tenant_id, parent_id, dept_name) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_sys_dept_tenant_parent_status ON sys_dept (tenant_id, parent_id, del_flag, status, order_num);

CREATE TABLE IF NOT EXISTS sales_store (
    sales_store_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id = 1),
    store_code varchar(64) NOT NULL,
    store_name varchar(120) NOT NULL,
    dept_id bigint NOT NULL,
    contact_name varchar(100),
    contact_phone varchar(64),
    country varchar(64),
    state varchar(64),
    city varchar(64),
    address_line1 varchar(255),
    address_line2 varchar(255),
    postal_code varchar(32),
    currency_code varchar(3) NOT NULL DEFAULT 'USD',
    credit_limit numeric(18,2) NOT NULL DEFAULT 0 CHECK (credit_limit >= 0),
    payment_term_days integer NOT NULL DEFAULT 0 CHECK (payment_term_days >= 0),
    status char(1) NOT NULL DEFAULT '1',
    del_flag char(1) NOT NULL DEFAULT '0',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz NOT NULL DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE INDEX IF NOT EXISTS idx_sales_store_code ON sales_store (tenant_id, store_code, status) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_sales_store_dept ON sales_store (tenant_id, dept_id, status) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS sys_user (
    user_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    dept_id bigint,
    user_name varchar(100) NOT NULL,
    nick_name varchar(30) NOT NULL,
    user_type varchar(20) DEFAULT 'sys_user',
    email varchar(100),
    phonenumber varchar(20),
    sex char(1) DEFAULT '0',
    avatar varchar(500),
    password varchar(100),
    force_password_change char(1) DEFAULT '0',
    status char(1) DEFAULT '1',
    del_flag char(1) DEFAULT '0',
    login_ip varchar(128),
    login_date timestamptz,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500),
    file_url1 varchar(500),
    file_url2 varchar(500),
    file_url3 varchar(500),
    file_url4 varchar(500),
    oss_id1 varchar(64),
    oss_id2 varchar(64),
    oss_id3 varchar(64),
    oss_id4 varchar(64)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_tenant_username ON sys_user (tenant_id, user_name);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_email ON sys_user (email) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_username_active ON sys_user (user_name) WHERE del_flag = '0';
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_phone_active ON sys_user (phonenumber) WHERE del_flag = '0' AND phonenumber IS NOT NULL AND phonenumber <> '';
CREATE INDEX IF NOT EXISTS idx_sys_user_tenant_status ON sys_user (tenant_id, del_flag, status, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_sys_user_tenant_dept ON sys_user (tenant_id, dept_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_login_date ON sys_user (login_date DESC);

CREATE TABLE IF NOT EXISTS sys_role (
    role_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    role_name varchar(30) NOT NULL,
    role_key varchar(100) NOT NULL,
    role_sort integer NOT NULL,
    default_menu_id bigint,
    data_scope char(1) DEFAULT '1',
    menu_check_strictly boolean DEFAULT true,
    dept_check_strictly boolean DEFAULT true,
    status char(1) DEFAULT '1',
    del_flag char(1) DEFAULT '0',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
ALTER TABLE sys_role ADD COLUMN IF NOT EXISTS default_menu_id bigint;

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_tenant_key ON sys_role (tenant_id, role_key);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_tenant_name_active ON sys_role (tenant_id, role_name) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_sys_role_tenant_status ON sys_role (tenant_id, del_flag, status, role_sort);

CREATE TABLE IF NOT EXISTS sys_menu (
    menu_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    menu_name varchar(50) NOT NULL,
    i18n_key varchar(128),
    parent_id bigint DEFAULT 0,
    order_num integer DEFAULT 0,
    path varchar(200),
    component varchar(200),
    query_param varchar(255),
    is_frame char(1) DEFAULT '1',
    is_cache char(1) DEFAULT '0',
    menu_type char(1) NOT NULL,
    visible char(1) DEFAULT '1',
    status char(1) DEFAULT '1',
    perms varchar(100),
    icon varchar(100),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE INDEX IF NOT EXISTS idx_sys_menu_tenant_parent_order ON sys_menu (tenant_id, parent_id, order_num);
CREATE INDEX IF NOT EXISTS idx_sys_menu_tenant_status_type ON sys_menu (tenant_id, status, menu_type, order_num);
CREATE INDEX IF NOT EXISTS idx_sys_menu_perms ON sys_menu (perms) WHERE perms IS NOT NULL AND perms <> '';
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_menu_tenant_parent_name ON sys_menu (tenant_id, parent_id, menu_name);

CREATE TABLE IF NOT EXISTS sys_post (
    post_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    post_code varchar(64) NOT NULL,
    post_name varchar(50) NOT NULL,
    post_sort integer NOT NULL,
    status char(1) DEFAULT '1',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_post_tenant_code ON sys_post (tenant_id, post_code);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_post_tenant_name ON sys_post (tenant_id, post_name);
CREATE INDEX IF NOT EXISTS idx_sys_post_tenant_status ON sys_post (tenant_id, status, post_sort);

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    PRIMARY KEY (user_id, role_id)
);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_role_user ON sys_user_role (role_id, user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_tenant_user ON sys_user_role (tenant_id, user_id, role_id);

CREATE TABLE IF NOT EXISTS sys_user_post (
    user_id bigint NOT NULL,
    post_id bigint NOT NULL,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    PRIMARY KEY (user_id, post_id)
);
CREATE INDEX IF NOT EXISTS idx_sys_user_post_post_user ON sys_user_post (post_id, user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_post_tenant_user ON sys_user_post (tenant_id, user_id, post_id);

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id bigint NOT NULL,
    menu_id bigint NOT NULL,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    PRIMARY KEY (role_id, menu_id)
);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_menu_role ON sys_role_menu (menu_id, role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_tenant_role ON sys_role_menu (tenant_id, role_id, menu_id);

CREATE TABLE IF NOT EXISTS sys_role_dept (
    role_id bigint NOT NULL,
    dept_id bigint NOT NULL,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    PRIMARY KEY (role_id, dept_id)
);
CREATE INDEX IF NOT EXISTS idx_sys_role_dept_dept_role ON sys_role_dept (dept_id, role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_dept_tenant_role ON sys_role_dept (tenant_id, role_id, dept_id);

CREATE TABLE IF NOT EXISTS sys_config (
    config_id bigint PRIMARY KEY,
    config_name varchar(100),
    config_key varchar(100) NOT NULL,
    config_value varchar(500),
    config_type char(1) DEFAULT 'N',
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500),
    create_by_id bigint
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_config_key ON sys_config (config_key);
CREATE INDEX IF NOT EXISTS idx_sys_config_type ON sys_config (config_type);

CREATE TABLE IF NOT EXISTS sys_dict_type (
    dict_id bigint PRIMARY KEY,
    dict_name varchar(100),
    dict_type varchar(100) NOT NULL,
    status char(1) DEFAULT '1',
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500),
    create_by_id bigint
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_dict_type ON sys_dict_type (dict_type);
CREATE INDEX IF NOT EXISTS idx_sys_dict_type_status ON sys_dict_type (status);

CREATE TABLE IF NOT EXISTS sys_dict_data (
    dict_code bigint PRIMARY KEY,
    dict_sort integer DEFAULT 0,
    dict_label varchar(100),
    dict_value varchar(100),
    dict_type varchar(100),
    css_class varchar(100),
    list_class varchar(100),
    is_default char(1) DEFAULT 'N',
    status char(1) DEFAULT '1',
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500),
    create_by_id bigint
);
CREATE INDEX IF NOT EXISTS idx_sys_dict_data_type_status_sort ON sys_dict_data (dict_type, status, dict_sort);

CREATE TABLE IF NOT EXISTS sys_country (
    country_id bigint PRIMARY KEY,
    country_code varchar(2) NOT NULL,
    name_en varchar(128) NOT NULL,
    name_zh varchar(128) NOT NULL,
    status char(1) DEFAULT '1',
    sort integer DEFAULT 0,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500),
    create_by_id bigint
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_country_code ON sys_country (country_code);
CREATE INDEX IF NOT EXISTS idx_sys_country_status_sort ON sys_country (status, sort, country_code);

CREATE TABLE IF NOT EXISTS sys_currency (
    currency_id bigint PRIMARY KEY,
    currency_code varchar(3) NOT NULL,
    name_en varchar(128) NOT NULL,
    name_zh varchar(128) NOT NULL,
    symbol varchar(16),
    decimal_places integer DEFAULT 2,
    status char(1) DEFAULT '1',
    sort integer DEFAULT 0,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500),
    create_by_id bigint
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_currency_code ON sys_currency (currency_code);
CREATE INDEX IF NOT EXISTS idx_sys_currency_status_sort ON sys_currency (status, sort, currency_code);

CREATE TABLE IF NOT EXISTS sys_language (
    language_id bigint PRIMARY KEY,
    language_code varchar(20) NOT NULL,
    name_en varchar(128) NOT NULL,
    name_native varchar(128) NOT NULL,
    status char(1) DEFAULT '1',
    sort integer DEFAULT 0,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500),
    create_by_id bigint
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_language_code ON sys_language (language_code);
CREATE INDEX IF NOT EXISTS idx_sys_language_status_sort ON sys_language (status, sort, language_code);

COMMENT ON TABLE sys_country IS '国家表';
COMMENT ON COLUMN sys_country.country_id IS '国家ID';
COMMENT ON COLUMN sys_country.country_code IS '国家代码，ISO 3166-1 alpha-2';
COMMENT ON COLUMN sys_country.name_en IS '英文名称';
COMMENT ON COLUMN sys_country.name_zh IS '中文名称';
COMMENT ON COLUMN sys_country.status IS '状态：1正常，0停用';
COMMENT ON COLUMN sys_country.sort IS '排序';
COMMENT ON COLUMN sys_country.create_by IS '创建者';
COMMENT ON COLUMN sys_country.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_country.update_by IS '更新者';
COMMENT ON COLUMN sys_country.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_country.remark IS '备注';
COMMENT ON COLUMN sys_country.create_by_id IS '创建者ID';

COMMENT ON TABLE sys_currency IS '币种表';
COMMENT ON COLUMN sys_currency.currency_id IS '币种ID';
COMMENT ON COLUMN sys_currency.currency_code IS '币种代码，ISO 4217';
COMMENT ON COLUMN sys_currency.name_en IS '英文名称';
COMMENT ON COLUMN sys_currency.name_zh IS '中文名称';
COMMENT ON COLUMN sys_currency.symbol IS '货币符号';
COMMENT ON COLUMN sys_currency.decimal_places IS '小数位数';
COMMENT ON COLUMN sys_currency.status IS '状态：1正常，0停用';
COMMENT ON COLUMN sys_currency.sort IS '排序';
COMMENT ON COLUMN sys_currency.create_by IS '创建者';
COMMENT ON COLUMN sys_currency.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_currency.update_by IS '更新者';
COMMENT ON COLUMN sys_currency.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_currency.remark IS '备注';
COMMENT ON COLUMN sys_currency.create_by_id IS '创建者ID';

COMMENT ON TABLE sys_language IS '语言表';
COMMENT ON COLUMN sys_language.language_id IS '语言ID';
COMMENT ON COLUMN sys_language.language_code IS '语言代码';
COMMENT ON COLUMN sys_language.name_en IS '英文名称';
COMMENT ON COLUMN sys_language.name_native IS '本地语言名称';
COMMENT ON COLUMN sys_language.status IS '状态：1正常，0停用';
COMMENT ON COLUMN sys_language.sort IS '排序';
COMMENT ON COLUMN sys_language.create_by IS '创建者';
COMMENT ON COLUMN sys_language.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_language.update_by IS '更新者';
COMMENT ON COLUMN sys_language.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_language.remark IS '备注';
COMMENT ON COLUMN sys_language.create_by_id IS '创建者ID';

CREATE TABLE IF NOT EXISTS sys_notice (
    notice_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    notice_title varchar(50) NOT NULL,
    notice_type char(1) NOT NULL,
    notice_content text,
    status char(1) DEFAULT '1',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE INDEX IF NOT EXISTS idx_sys_notice_tenant_status_time ON sys_notice (tenant_id, status, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_sys_notice_type ON sys_notice (notice_type);

CREATE TABLE IF NOT EXISTS sys_notice_read (
    read_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    notice_id bigint NOT NULL REFERENCES sys_notice (notice_id) ON DELETE CASCADE,
    user_id bigint NOT NULL,
    read_time timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uk_sys_notice_read_user_notice UNIQUE (tenant_id, notice_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_sys_notice_read_user_time ON sys_notice_read (tenant_id, user_id, read_time DESC);
CREATE INDEX IF NOT EXISTS idx_sys_notice_read_notice ON sys_notice_read (tenant_id, notice_id);

CREATE TABLE IF NOT EXISTS sys_logininfor (
    info_id bigint PRIMARY KEY,
    user_name varchar(50),
    ipaddr varchar(128),
    login_location varchar(255),
    browser varchar(50),
    os varchar(50),
    status char(1),
    msg varchar(255),
    login_time timestamptz
);
CREATE INDEX IF NOT EXISTS idx_sys_logininfor_login_time ON sys_logininfor (login_time DESC);
CREATE INDEX IF NOT EXISTS idx_sys_logininfor_user_time ON sys_logininfor (user_name, login_time DESC);
CREATE INDEX IF NOT EXISTS idx_sys_logininfor_status_time ON sys_logininfor (status, login_time DESC);

CREATE TABLE IF NOT EXISTS sys_oper_log (
    oper_id bigint PRIMARY KEY,
    title varchar(50),
    business_type integer,
    method varchar(200),
    request_method varchar(10),
    operator_type integer,
    oper_name varchar(50),
    dept_name varchar(50),
    oper_url varchar(255),
    oper_ip varchar(128),
    oper_location varchar(255),
    oper_param text,
    json_result text,
    status integer,
    error_msg text,
    oper_time timestamptz,
    cost_time bigint
);
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_oper_time ON sys_oper_log (oper_time DESC);
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_status_time ON sys_oper_log (status, oper_time DESC);
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_business_time ON sys_oper_log (business_type, oper_time DESC);
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_oper_name_time ON sys_oper_log (oper_name, oper_time DESC);

CREATE TABLE IF NOT EXISTS sys_oss (
    oss_id bigint PRIMARY KEY,
    file_name varchar(255),
    original_name varchar(255),
    file_suffix varchar(20),
    url varchar(500),
    service varchar(20),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE INDEX IF NOT EXISTS idx_sys_oss_create_time ON sys_oss (create_time DESC);
CREATE INDEX IF NOT EXISTS idx_sys_oss_service_suffix ON sys_oss (service, file_suffix);

CREATE TABLE IF NOT EXISTS sys_oss_config (
    oss_config_id bigint PRIMARY KEY,
    config_key varchar(20),
    access_key varchar(255),
    secret_key varchar(255),
    bucket_name varchar(255),
    prefix varchar(255),
    endpoint varchar(255),
    domain varchar(255),
    is_https char(1),
    region varchar(50),
    access_policy char(1),
    status char(1),
    ext1 varchar(255),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_oss_config_key ON sys_oss_config (config_key) WHERE config_key IS NOT NULL AND config_key <> '';
CREATE INDEX IF NOT EXISTS idx_sys_oss_config_status ON sys_oss_config (status);

CREATE TABLE IF NOT EXISTS gen_table (
    table_id bigint PRIMARY KEY,
    table_name varchar(200),
    table_comment varchar(500),
    sub_table_name varchar(64),
    sub_table_fk_name varchar(64),
    class_name varchar(100),
    tpl_category varchar(200),
    package_name varchar(100),
    module_name varchar(30),
    business_name varchar(30),
    function_name varchar(50),
    function_author varchar(50),
    gen_type char(1),
    gen_path varchar(200),
    options varchar(1000),
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE INDEX IF NOT EXISTS idx_gen_table_name ON gen_table (table_name);

CREATE TABLE IF NOT EXISTS gen_table_column (
    column_id bigint PRIMARY KEY,
    table_id bigint,
    column_name varchar(200),
    column_comment varchar(500),
    column_type varchar(100),
    java_type varchar(500),
    java_field varchar(200),
    is_pk char(1),
    is_increment char(1),
    is_required char(1),
    is_insert char(1),
    is_edit char(1),
    is_list char(1),
    is_query char(1),
    query_type varchar(200),
    html_type varchar(200),
    dict_type varchar(200),
    sort integer,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);
CREATE INDEX IF NOT EXISTS idx_gen_table_column_table_sort ON gen_table_column (table_id, sort);

COMMENT ON TABLE sys_tenant IS '租户表';
COMMENT ON COLUMN sys_tenant.tenant_id IS '租户ID，平台/厂家固定为1，0不是合法业务租户';
COMMENT ON COLUMN sys_tenant.tenant_name IS '租户名称';
COMMENT ON COLUMN sys_tenant.tenant_type IS '租户类型：PLATFORM平台/厂家，MERCHANT商家';
COMMENT ON COLUMN sys_tenant.contact_name IS '联系人姓名';
COMMENT ON COLUMN sys_tenant.contact_email IS '联系人邮箱';
COMMENT ON COLUMN sys_tenant.country IS '国家或地区';
COMMENT ON COLUMN sys_tenant.status IS '状态：1正常，0停用';
COMMENT ON COLUMN sys_tenant.create_by_id IS '创建者ID';
COMMENT ON COLUMN sys_tenant.create_by IS '创建者';
COMMENT ON COLUMN sys_tenant.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_tenant.update_by IS '更新者';
COMMENT ON COLUMN sys_tenant.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_tenant.remark IS '备注';

COMMENT ON TABLE sys_tenant_apply IS '商家租户申请表';
COMMENT ON COLUMN sys_tenant_apply.apply_id IS '申请ID';
COMMENT ON COLUMN sys_tenant_apply.tenant_id IS '审核通过后生成的租户ID';
COMMENT ON COLUMN sys_tenant_apply.merchant_name IS '商家名称';
COMMENT ON COLUMN sys_tenant_apply.company_name IS '公司名称';
COMMENT ON COLUMN sys_tenant_apply.contact_first_name IS '联系人名';
COMMENT ON COLUMN sys_tenant_apply.contact_last_name IS '联系人姓';
COMMENT ON COLUMN sys_tenant_apply.contact_name IS '联系人姓名';
COMMENT ON COLUMN sys_tenant_apply.email IS '登录邮箱/联系邮箱';
COMMENT ON COLUMN sys_tenant_apply.apply_locale IS '申请人提交申请时的语言';
COMMENT ON COLUMN sys_tenant_apply.office_phone IS '办公电话';
COMMENT ON COLUMN sys_tenant_apply.mobile_phone IS '手机号码';
COMMENT ON COLUMN sys_tenant_apply.country IS '国家或地区';
COMMENT ON COLUMN sys_tenant_apply.state IS '州/省';
COMMENT ON COLUMN sys_tenant_apply.city IS '城市';
COMMENT ON COLUMN sys_tenant_apply.address_line1 IS '地址行1';
COMMENT ON COLUMN sys_tenant_apply.address_line2 IS '地址行2';
COMMENT ON COLUMN sys_tenant_apply.postal_code IS '邮政编码';
COMMENT ON COLUMN sys_tenant_apply.remark IS '申请备注';
COMMENT ON COLUMN sys_tenant_apply.status IS '申请状态：PENDING待审核，APPROVED已通过，REJECTED已拒绝';
COMMENT ON COLUMN sys_tenant_apply.audit_by IS '审核人';
COMMENT ON COLUMN sys_tenant_apply.audit_by_id IS '审核人ID';
COMMENT ON COLUMN sys_tenant_apply.audit_time IS '审核时间，UTC timestamptz';
COMMENT ON COLUMN sys_tenant_apply.reject_reason IS '拒绝原因';
COMMENT ON COLUMN sys_tenant_apply.create_by_id IS '创建者ID';
COMMENT ON COLUMN sys_tenant_apply.create_by IS '创建者';
COMMENT ON COLUMN sys_tenant_apply.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_tenant_apply.update_by IS '更新者';
COMMENT ON COLUMN sys_tenant_apply.update_time IS '更新时间，UTC timestamptz';

COMMENT ON TABLE merchant_profile IS '商家主体资料表';
COMMENT ON COLUMN merchant_profile.merchant_id IS '商家ID';
COMMENT ON COLUMN merchant_profile.tenant_id IS '租户ID，一对一关联sys_tenant';
COMMENT ON COLUMN merchant_profile.merchant_name IS '商家名称';
COMMENT ON COLUMN merchant_profile.company_name IS '公司名称';
COMMENT ON COLUMN merchant_profile.contact_first_name IS '联系人名';
COMMENT ON COLUMN merchant_profile.contact_last_name IS '联系人姓';
COMMENT ON COLUMN merchant_profile.contact_name IS '联系人姓名';
COMMENT ON COLUMN merchant_profile.primary_email IS '主邮箱';
COMMENT ON COLUMN merchant_profile.office_phone IS '办公电话';
COMMENT ON COLUMN merchant_profile.mobile_phone IS '手机号码';
COMMENT ON COLUMN merchant_profile.country IS '国家或地区';
COMMENT ON COLUMN merchant_profile.state IS '州/省';
COMMENT ON COLUMN merchant_profile.city IS '城市';
COMMENT ON COLUMN merchant_profile.address_line1 IS '地址行1';
COMMENT ON COLUMN merchant_profile.address_line2 IS '地址行2';
COMMENT ON COLUMN merchant_profile.postal_code IS '邮政编码';
COMMENT ON COLUMN merchant_profile.status IS '商家状态：1正常，0停用';
COMMENT ON COLUMN merchant_profile.audit_status IS '审核状态：PENDING待审核，APPROVED已通过，REJECTED已拒绝';
COMMENT ON COLUMN merchant_profile.audit_by IS '审核人';
COMMENT ON COLUMN merchant_profile.audit_by_id IS '审核人ID';
COMMENT ON COLUMN merchant_profile.audit_time IS '审核时间，UTC timestamptz';
COMMENT ON COLUMN merchant_profile.remark IS '备注';
COMMENT ON COLUMN merchant_profile.create_by_id IS '创建者ID';
COMMENT ON COLUMN merchant_profile.create_by IS '创建者';
COMMENT ON COLUMN merchant_profile.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN merchant_profile.update_by IS '更新者';
COMMENT ON COLUMN merchant_profile.update_time IS '更新时间，UTC timestamptz';

COMMENT ON TABLE sys_dept IS '部门表';
COMMENT ON COLUMN sys_dept.dept_id IS '部门ID';
COMMENT ON COLUMN sys_dept.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_dept.parent_id IS '父部门ID';
COMMENT ON COLUMN sys_dept.ancestors IS '祖级列表';
COMMENT ON COLUMN sys_dept.dept_name IS '部门名称';
COMMENT ON COLUMN sys_dept.order_num IS '显示顺序';
COMMENT ON COLUMN sys_dept.leader IS '负责人';
COMMENT ON COLUMN sys_dept.phone IS '联系电话';
COMMENT ON COLUMN sys_dept.email IS '邮箱';
COMMENT ON COLUMN sys_dept.status IS '部门状态：1正常，0停用';
COMMENT ON COLUMN sys_dept.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN sys_dept.create_by_id IS '创建者ID';
COMMENT ON COLUMN sys_dept.create_by IS '创建者';
COMMENT ON COLUMN sys_dept.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_dept.update_by IS '更新者';
COMMENT ON COLUMN sys_dept.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_dept.remark IS '备注';

COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON COLUMN sys_user.user_id IS '用户ID';
COMMENT ON COLUMN sys_user.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_user.dept_id IS '部门ID';
COMMENT ON COLUMN sys_user.user_name IS '用户名，商家账号使用邮箱';
COMMENT ON COLUMN sys_user.nick_name IS '用户昵称';
COMMENT ON COLUMN sys_user.user_type IS '用户类型';
COMMENT ON COLUMN sys_user.email IS '邮箱';
COMMENT ON COLUMN sys_user.phonenumber IS '手机号码';
COMMENT ON COLUMN sys_user.sex IS '性别';
COMMENT ON COLUMN sys_user.avatar IS '头像地址';
COMMENT ON COLUMN sys_user.password IS '密码哈希';
COMMENT ON COLUMN sys_user.force_password_change IS '是否强制下次登录修改密码：1是，0否';
COMMENT ON COLUMN sys_user.status IS '账号状态：1正常，0停用';
COMMENT ON COLUMN sys_user.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN sys_user.login_ip IS '最后登录IP';
COMMENT ON COLUMN sys_user.login_date IS '最后登录时间，UTC timestamptz';
COMMENT ON COLUMN sys_user.create_by_id IS '创建者ID';
COMMENT ON COLUMN sys_user.create_by IS '创建者';
COMMENT ON COLUMN sys_user.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_user.update_by IS '更新者';
COMMENT ON COLUMN sys_user.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_user.remark IS '备注';
COMMENT ON COLUMN sys_user.file_url1 IS '扩展文件地址1';
COMMENT ON COLUMN sys_user.file_url2 IS '扩展文件地址2';
COMMENT ON COLUMN sys_user.file_url3 IS '扩展文件地址3';
COMMENT ON COLUMN sys_user.file_url4 IS '扩展文件地址4';
COMMENT ON COLUMN sys_user.oss_id1 IS '扩展OSS文件ID1';
COMMENT ON COLUMN sys_user.oss_id2 IS '扩展OSS文件ID2';
COMMENT ON COLUMN sys_user.oss_id3 IS '扩展OSS文件ID3';
COMMENT ON COLUMN sys_user.oss_id4 IS '扩展OSS文件ID4';

COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.role_id IS '角色ID';
COMMENT ON COLUMN sys_role.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.role_key IS '角色权限字符串';
COMMENT ON COLUMN sys_role.role_sort IS '显示顺序';
COMMENT ON COLUMN sys_role.data_scope IS '数据范围';
COMMENT ON COLUMN sys_role.menu_check_strictly IS '菜单树选择项是否关联显示';
COMMENT ON COLUMN sys_role.dept_check_strictly IS '部门树选择项是否关联显示';
COMMENT ON COLUMN sys_role.status IS '角色状态：1正常，0停用';
COMMENT ON COLUMN sys_role.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN sys_role.create_by_id IS '创建者ID';
COMMENT ON COLUMN sys_role.create_by IS '创建者';
COMMENT ON COLUMN sys_role.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_role.update_by IS '更新者';
COMMENT ON COLUMN sys_role.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_role.remark IS '备注';

COMMENT ON TABLE sys_menu IS '菜单权限表';
COMMENT ON COLUMN sys_menu.menu_id IS '菜单ID';
COMMENT ON COLUMN sys_menu.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_menu.menu_name IS '菜单名称';
COMMENT ON COLUMN sys_menu.i18n_key IS '国际化消息键';
COMMENT ON COLUMN sys_menu.parent_id IS '父菜单ID';
COMMENT ON COLUMN sys_menu.order_num IS '显示顺序';
COMMENT ON COLUMN sys_menu.path IS '路由地址';
COMMENT ON COLUMN sys_menu.component IS '组件路径';
COMMENT ON COLUMN sys_menu.query_param IS '路由参数';
COMMENT ON COLUMN sys_menu.is_frame IS '是否外链：0是，1否';
COMMENT ON COLUMN sys_menu.is_cache IS '是否缓存：0缓存，1不缓存';
COMMENT ON COLUMN sys_menu.menu_type IS '菜单类型：M目录，C菜单，F按钮';
COMMENT ON COLUMN sys_menu.visible IS '显示状态：1显示，0隐藏';
COMMENT ON COLUMN sys_menu.status IS '菜单状态：1正常，0停用';
COMMENT ON COLUMN sys_menu.perms IS '权限标识';
COMMENT ON COLUMN sys_menu.icon IS '菜单图标';
COMMENT ON COLUMN sys_menu.create_by_id IS '创建者ID';
COMMENT ON COLUMN sys_menu.create_by IS '创建者';
COMMENT ON COLUMN sys_menu.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_menu.update_by IS '更新者';
COMMENT ON COLUMN sys_menu.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_menu.remark IS '备注';

COMMENT ON TABLE sys_post IS '岗位表';
COMMENT ON COLUMN sys_post.post_id IS '岗位ID';
COMMENT ON COLUMN sys_post.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_post.post_code IS '岗位编码';
COMMENT ON COLUMN sys_post.post_name IS '岗位名称';
COMMENT ON COLUMN sys_post.post_sort IS '显示顺序';
COMMENT ON COLUMN sys_post.status IS '岗位状态：1正常，0停用';
COMMENT ON COLUMN sys_post.create_by_id IS '创建者ID';
COMMENT ON COLUMN sys_post.create_by IS '创建者';
COMMENT ON COLUMN sys_post.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_post.update_by IS '更新者';
COMMENT ON COLUMN sys_post.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_post.remark IS '备注';

COMMENT ON TABLE sys_user_role IS '用户和角色关联表';
COMMENT ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID';
COMMENT ON COLUMN sys_user_role.tenant_id IS '租户ID';

COMMENT ON TABLE sys_user_post IS '用户和岗位关联表';
COMMENT ON COLUMN sys_user_post.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_post.post_id IS '岗位ID';
COMMENT ON COLUMN sys_user_post.tenant_id IS '租户ID';

COMMENT ON TABLE sys_role_menu IS '角色和菜单关联表';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单ID';
COMMENT ON COLUMN sys_role_menu.tenant_id IS '租户ID';

COMMENT ON TABLE sys_role_dept IS '角色和部门关联表';
COMMENT ON COLUMN sys_role_dept.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_dept.dept_id IS '部门ID';
COMMENT ON COLUMN sys_role_dept.tenant_id IS '租户ID';

COMMENT ON TABLE sys_config IS '参数配置表';
COMMENT ON COLUMN sys_config.config_id IS '参数ID';
COMMENT ON COLUMN sys_config.config_name IS '参数名称';
COMMENT ON COLUMN sys_config.config_key IS '参数键名';
COMMENT ON COLUMN sys_config.config_value IS '参数键值';
COMMENT ON COLUMN sys_config.config_type IS '系统内置：Y是，N否';
COMMENT ON COLUMN sys_config.create_by IS '创建者';
COMMENT ON COLUMN sys_config.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_config.update_by IS '更新者';
COMMENT ON COLUMN sys_config.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_config.remark IS '备注';
COMMENT ON COLUMN sys_config.create_by_id IS '创建者ID';

COMMENT ON TABLE sys_dict_type IS '字典类型表';
COMMENT ON COLUMN sys_dict_type.dict_id IS '字典主键';
COMMENT ON COLUMN sys_dict_type.dict_name IS '字典名称';
COMMENT ON COLUMN sys_dict_type.dict_type IS '字典类型';
COMMENT ON COLUMN sys_dict_type.status IS '状态：1正常，0停用';
COMMENT ON COLUMN sys_dict_type.create_by IS '创建者';
COMMENT ON COLUMN sys_dict_type.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_dict_type.update_by IS '更新者';
COMMENT ON COLUMN sys_dict_type.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_dict_type.remark IS '备注';
COMMENT ON COLUMN sys_dict_type.create_by_id IS '创建者ID';

COMMENT ON TABLE sys_dict_data IS '字典数据表';
COMMENT ON COLUMN sys_dict_data.dict_code IS '字典编码';
COMMENT ON COLUMN sys_dict_data.dict_sort IS '字典排序';
COMMENT ON COLUMN sys_dict_data.dict_label IS '字典标签';
COMMENT ON COLUMN sys_dict_data.dict_value IS '字典键值';
COMMENT ON COLUMN sys_dict_data.dict_type IS '字典类型';
COMMENT ON COLUMN sys_dict_data.css_class IS '样式属性';
COMMENT ON COLUMN sys_dict_data.list_class IS '回显样式';
COMMENT ON COLUMN sys_dict_data.is_default IS '是否默认：Y是，N否';
COMMENT ON COLUMN sys_dict_data.status IS '状态：1正常，0停用';
COMMENT ON COLUMN sys_dict_data.create_by IS '创建者';
COMMENT ON COLUMN sys_dict_data.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_dict_data.update_by IS '更新者';
COMMENT ON COLUMN sys_dict_data.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_dict_data.remark IS '备注';
COMMENT ON COLUMN sys_dict_data.create_by_id IS '创建者ID';


COMMENT ON TABLE sys_notice IS '通知公告表';
COMMENT ON COLUMN sys_notice.notice_id IS '公告ID';
COMMENT ON COLUMN sys_notice.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_notice.notice_title IS '公告标题';
COMMENT ON COLUMN sys_notice.notice_type IS '公告类型';
COMMENT ON COLUMN sys_notice.notice_content IS '公告内容';
COMMENT ON COLUMN sys_notice.status IS '公告状态：1正常，0关闭';
COMMENT ON COLUMN sys_notice.create_by_id IS '创建者ID';
COMMENT ON COLUMN sys_notice.create_by IS '创建者';
COMMENT ON COLUMN sys_notice.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_notice.update_by IS '更新者';
COMMENT ON COLUMN sys_notice.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_notice.remark IS '备注';

COMMENT ON TABLE sys_notice_read IS '用户公告阅读记录';
COMMENT ON COLUMN sys_notice_read.read_id IS '阅读记录ID';
COMMENT ON COLUMN sys_notice_read.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_notice_read.notice_id IS '公告ID';
COMMENT ON COLUMN sys_notice_read.user_id IS '用户ID';
COMMENT ON COLUMN sys_notice_read.read_time IS '阅读时间，UTC timestamptz';

COMMENT ON TABLE sys_logininfor IS '系统访问记录表';
COMMENT ON COLUMN sys_logininfor.info_id IS '访问ID';
COMMENT ON COLUMN sys_logininfor.user_name IS '用户账号';
COMMENT ON COLUMN sys_logininfor.ipaddr IS '登录IP地址';
COMMENT ON COLUMN sys_logininfor.login_location IS '登录地点';
COMMENT ON COLUMN sys_logininfor.browser IS '浏览器类型';
COMMENT ON COLUMN sys_logininfor.os IS '操作系统';
COMMENT ON COLUMN sys_logininfor.status IS '登录状态';
COMMENT ON COLUMN sys_logininfor.msg IS '提示消息';
COMMENT ON COLUMN sys_logininfor.login_time IS '访问时间，UTC timestamptz';

COMMENT ON TABLE sys_oper_log IS '操作日志记录表';
COMMENT ON COLUMN sys_oper_log.oper_id IS '日志主键';
COMMENT ON COLUMN sys_oper_log.title IS '模块标题';
COMMENT ON COLUMN sys_oper_log.business_type IS '业务类型';
COMMENT ON COLUMN sys_oper_log.method IS '方法名称';
COMMENT ON COLUMN sys_oper_log.request_method IS '请求方式';
COMMENT ON COLUMN sys_oper_log.operator_type IS '操作类别';
COMMENT ON COLUMN sys_oper_log.oper_name IS '操作人员';
COMMENT ON COLUMN sys_oper_log.dept_name IS '部门名称';
COMMENT ON COLUMN sys_oper_log.oper_url IS '请求URL';
COMMENT ON COLUMN sys_oper_log.oper_ip IS '主机地址';
COMMENT ON COLUMN sys_oper_log.oper_location IS '操作地点';
COMMENT ON COLUMN sys_oper_log.oper_param IS '请求参数';
COMMENT ON COLUMN sys_oper_log.json_result IS '返回参数';
COMMENT ON COLUMN sys_oper_log.status IS '操作状态';
COMMENT ON COLUMN sys_oper_log.error_msg IS '错误消息';
COMMENT ON COLUMN sys_oper_log.oper_time IS '操作时间，UTC timestamptz';
COMMENT ON COLUMN sys_oper_log.cost_time IS '消耗时间，毫秒';

COMMENT ON TABLE sys_oss IS '对象存储文件表';
COMMENT ON COLUMN sys_oss.oss_id IS '对象存储ID';
COMMENT ON COLUMN sys_oss.file_name IS '文件名';
COMMENT ON COLUMN sys_oss.original_name IS '原始文件名';
COMMENT ON COLUMN sys_oss.file_suffix IS '文件后缀';
COMMENT ON COLUMN sys_oss.url IS '访问地址';
COMMENT ON COLUMN sys_oss.service IS '服务商';
COMMENT ON COLUMN sys_oss.create_by_id IS '上传人ID';
COMMENT ON COLUMN sys_oss.create_by IS '上传人';
COMMENT ON COLUMN sys_oss.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_oss.update_by IS '更新者';
COMMENT ON COLUMN sys_oss.update_time IS '更新时间，UTC timestamptz';

COMMENT ON TABLE sys_oss_config IS '对象存储配置表';
COMMENT ON COLUMN sys_oss_config.oss_config_id IS '配置ID';
COMMENT ON COLUMN sys_oss_config.config_key IS '配置键';
COMMENT ON COLUMN sys_oss_config.access_key IS '访问密钥ID';
COMMENT ON COLUMN sys_oss_config.secret_key IS '访问密钥Secret';
COMMENT ON COLUMN sys_oss_config.bucket_name IS '桶名称';
COMMENT ON COLUMN sys_oss_config.prefix IS '前缀';
COMMENT ON COLUMN sys_oss_config.endpoint IS '访问站点';
COMMENT ON COLUMN sys_oss_config.domain IS '自定义域名';
COMMENT ON COLUMN sys_oss_config.is_https IS '是否HTTPS';
COMMENT ON COLUMN sys_oss_config.region IS '区域';
COMMENT ON COLUMN sys_oss_config.access_policy IS '桶权限';
COMMENT ON COLUMN sys_oss_config.status IS '状态';
COMMENT ON COLUMN sys_oss_config.ext1 IS '扩展字段';
COMMENT ON COLUMN sys_oss_config.create_by_id IS '创建者ID';
COMMENT ON COLUMN sys_oss_config.create_by IS '创建者';
COMMENT ON COLUMN sys_oss_config.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_oss_config.update_by IS '更新者';
COMMENT ON COLUMN sys_oss_config.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_oss_config.remark IS '备注';

COMMENT ON TABLE gen_table IS '代码生成业务表';
COMMENT ON COLUMN gen_table.table_id IS '编号';
COMMENT ON COLUMN gen_table.table_name IS '表名称';
COMMENT ON COLUMN gen_table.table_comment IS '表描述';
COMMENT ON COLUMN gen_table.sub_table_name IS '关联子表名称';
COMMENT ON COLUMN gen_table.sub_table_fk_name IS '子表外键名';
COMMENT ON COLUMN gen_table.class_name IS '实体类名称';
COMMENT ON COLUMN gen_table.tpl_category IS '使用模板';
COMMENT ON COLUMN gen_table.package_name IS '生成包路径';
COMMENT ON COLUMN gen_table.module_name IS '生成模块名';
COMMENT ON COLUMN gen_table.business_name IS '生成业务名';
COMMENT ON COLUMN gen_table.function_name IS '生成功能名';
COMMENT ON COLUMN gen_table.function_author IS '生成功能作者';
COMMENT ON COLUMN gen_table.gen_type IS '生成代码方式';
COMMENT ON COLUMN gen_table.gen_path IS '生成路径';
COMMENT ON COLUMN gen_table.options IS '其他生成选项';
COMMENT ON COLUMN gen_table.create_by IS '创建者';
COMMENT ON COLUMN gen_table.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN gen_table.update_by IS '更新者';
COMMENT ON COLUMN gen_table.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN gen_table.remark IS '备注';

COMMENT ON TABLE gen_table_column IS '代码生成业务字段表';
COMMENT ON COLUMN gen_table_column.column_id IS '编号';
COMMENT ON COLUMN gen_table_column.table_id IS '归属表编号';
COMMENT ON COLUMN gen_table_column.column_name IS '列名称';
COMMENT ON COLUMN gen_table_column.column_comment IS '列描述';
COMMENT ON COLUMN gen_table_column.column_type IS '列类型';
COMMENT ON COLUMN gen_table_column.java_type IS 'Java类型';
COMMENT ON COLUMN gen_table_column.java_field IS 'Java字段名';
COMMENT ON COLUMN gen_table_column.is_pk IS '是否主键';
COMMENT ON COLUMN gen_table_column.is_increment IS '是否自增';
COMMENT ON COLUMN gen_table_column.is_required IS '是否必填';
COMMENT ON COLUMN gen_table_column.is_insert IS '是否为插入字段';
COMMENT ON COLUMN gen_table_column.is_edit IS '是否编辑字段';
COMMENT ON COLUMN gen_table_column.is_list IS '是否列表字段';
COMMENT ON COLUMN gen_table_column.is_query IS '是否查询字段';
COMMENT ON COLUMN gen_table_column.query_type IS '查询方式';
COMMENT ON COLUMN gen_table_column.html_type IS '显示类型';
COMMENT ON COLUMN gen_table_column.dict_type IS '字典类型';
COMMENT ON COLUMN gen_table_column.sort IS '排序';
COMMENT ON COLUMN gen_table_column.create_by IS '创建者';
COMMENT ON COLUMN gen_table_column.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN gen_table_column.update_by IS '更新者';
COMMENT ON COLUMN gen_table_column.update_time IS '更新时间，UTC timestamptz';

INSERT INTO sys_tenant (tenant_id, tenant_name, tenant_type, contact_email, status, create_by, create_time, remark)
VALUES (1, 'Platform Factory', 'PLATFORM', 'admin@example.com', '1', 'system', now(), 'Factory/platform tenant')
ON CONFLICT (tenant_id) DO NOTHING;
UPDATE sys_tenant
SET tenant_id = 1
WHERE tenant_id = 0
  AND tenant_type = 'PLATFORM'
  AND NOT EXISTS (SELECT 1 FROM sys_tenant WHERE tenant_id = 1);

INSERT INTO sys_dept (dept_id, tenant_id, parent_id, ancestors, dept_name, order_num, status, del_flag, create_by, create_time)
VALUES (100, 1, 0, '0', 'Platform', 0, '1', '0', 'system', now())
ON CONFLICT (dept_id) DO NOTHING;
UPDATE sys_dept SET tenant_id = 1 WHERE dept_id = 100;

INSERT INTO sys_user (user_id, tenant_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, password, force_password_change, status, del_flag, create_by, create_time, remark)
VALUES (1, 1, 100, 'admin', 'System Admin', 'sys_user', 'admin@example.com', '18888888888', '0',
        '$2a$10$JKJTOxd4d2I4.ee73mbJEe8M4AIhABfTfwNHvAvjGg978hsiBxeV6', '1', '1', '0', 'system', now(), 'Platform administrator')
ON CONFLICT (user_id) DO NOTHING;
UPDATE sys_user SET tenant_id = 1 WHERE user_id = 1;

INSERT INTO sys_role (role_id, tenant_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES (1, 1, 'Super Admin', 'admin', 1, '1', true, true, '1', '0', 'system', now(), 'Platform super admin')
ON CONFLICT (role_id) DO NOTHING;
UPDATE sys_role SET tenant_id = 1 WHERE role_id = 1;

INSERT INTO sys_user_role (user_id, role_id, tenant_id)
VALUES (1, 1, 1)
ON CONFLICT DO NOTHING;
UPDATE sys_user_role SET tenant_id = 1 WHERE user_id = 1 AND role_id = 1;

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES
    (1, 'Captcha enabled', 'sys.account.captchaEnabled', 'true', 'Y', 'system', now(), 'Enable captcha'),
    (2, 'Registration enabled', 'sys.account.registerUser', 'false', 'Y', 'system', now(), 'Legacy direct user registration disabled'),
    (3, 'Initial password', 'sys.user.initPassword', '123456', 'Y', 'system', now(), 'Default password for manually created users'),
    (11, 'OSS preview list resource', 'sys.oss.previewListResource', 'true', 'Y', 'system', now(), 'Enable OSS preview in file list')
ON CONFLICT (config_id) DO NOTHING;

INSERT INTO sys_oss_config (oss_config_id, config_key, access_key, secret_key, bucket_name, prefix, endpoint, domain, is_https, region, access_policy, status, create_by, create_time, remark)
VALUES (1, 'minio', '02f8394e7dd656645cd4', 'Zd8FW2BsnOGCSv+LfNUb7fTe3P55J1k/', 'bocoo', '', 'localhost:9000', '', 'N', 'us-east-1', '1', '1', 'system', now(), 'Local MinIO for development')
ON CONFLICT (oss_config_id) DO UPDATE
SET config_key = EXCLUDED.config_key,
    access_key = EXCLUDED.access_key,
    secret_key = EXCLUDED.secret_key,
    bucket_name = EXCLUDED.bucket_name,
    prefix = EXCLUDED.prefix,
    endpoint = EXCLUDED.endpoint,
    domain = EXCLUDED.domain,
    is_https = EXCLUDED.is_https,
    region = EXCLUDED.region,
    access_policy = EXCLUDED.access_policy,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now(),
    remark = EXCLUDED.remark;

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES
    (1, 'Normal/Disabled', 'sys_normal_disable', '1', 'system', now(), 'Base status'),
    (2, 'Tenant application status', 'tenant_apply_status', '1', 'system', now(), 'Merchant application status'),
    (3, 'Show/Hide', 'sys_show_hide', '1', 'system', now(), 'Menu visibility')
ON CONFLICT (dict_id) DO NOTHING;

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES
    (1, 1, 'Normal', '1', 'sys_normal_disable', 'primary', 'Y', '1', 'system', now()),
    (2, 2, 'Disabled', '0', 'sys_normal_disable', 'danger', 'N', '1', 'system', now()),
    (3, 1, 'Pending', 'PENDING', 'tenant_apply_status', 'warning', 'Y', '1', 'system', now()),
    (4, 2, 'Approved', 'APPROVED', 'tenant_apply_status', 'success', 'N', '1', 'system', now()),
    (5, 3, 'Rejected', 'REJECTED', 'tenant_apply_status', 'danger', 'N', '1', 'system', now()),
    (6, 1, 'Show', '1', 'sys_show_hide', 'primary', 'Y', '1', 'system', now()),
    (7, 2, 'Hide', '0', 'sys_show_hide', 'danger', 'N', '1', 'system', now())
ON CONFLICT (dict_code) DO NOTHING;



INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (1, 1, '系统管理', 'sys.menu.system', 0, 910, 'system', NULL, '1', '0', 'M', '1', '1', NULL, 'system', 'system', now(), 'System menu'),
    (100, 1, 'User', 'sys.menu.system.user', 1, 1, 'user', 'system/user/index', '1', '0', 'C', '1', '1', 'system:user:list', 'user', 'system', now(), 'User management'),
    (101, 1, 'Role', 'sys.menu.system.role', 1, 2, 'role', 'system/role/index', '1', '0', 'C', '1', '1', 'system:role:list', 'peoples', 'system', now(), 'Role management'),
    (102, 1, 'Menu', 'sys.menu.system.menu', 1, 3, 'menu', 'system/menu/index', '1', '0', 'C', '1', '1', 'system:menu:list', 'tree-table', 'system', now(), 'Menu management'),
    (103, 1, 'Dept', 'sys.menu.system.dept', 1, 4, 'dept', 'system/dept/index', '1', '0', 'C', '1', '1', 'system:dept:list', 'tree', 'system', now(), 'Dept management'),
    (104, 1, 'Post', 'sys.menu.system.post', 1, 5, 'post', 'system/post/index', '1', '0', 'C', '1', '1', 'system:post:list', 'post', 'system', now(), 'Post management'),
    (105, 1, 'Dict', 'sys.menu.system.dict', 1, 6, 'dict', 'system/dict/index', '1', '0', 'C', '1', '1', 'system:dict:list', 'dict', 'system', now(), 'Dict management'),
    (106, 1, 'Config', 'sys.menu.system.config', 1, 7, 'config', 'system/config/index', '1', '0', 'C', '1', '1', 'system:config:list', 'edit', 'system', now(), 'Config management'),
    (107, 1, 'Merchant Audit', 'sys.menu.system.merchantAudit', 1, 8, 'tenantApplication', 'system/tenant/applications', '1', '0', 'C', '1', '1', 'system:tenant:application:list', 'shop', 'system', now(), 'Merchant tenant audit'),
    (108, 1, 'Merchant Profile', 'sys.menu.system.merchantProfile', 1, 9, 'merchantProfile', 'system/merchant/profile', '1', '0', 'C', '1', '1', 'system:merchant:profile:list', 'store', 'system', now(), 'Merchant profile management'),
    (1001, 1, 'Tenant Application Query', 'sys.menu.system.merchantAudit.query', 107, 1, '#', '', '1', '0', 'F', '1', '1', 'system:tenant:application:query', '#', 'system', now(), ''),
    (1002, 1, 'Tenant Application Approve', 'sys.menu.system.merchantAudit.approve', 107, 2, '#', '', '1', '0', 'F', '1', '1', 'system:tenant:application:approve', '#', 'system', now(), ''),
    (1004, 1, 'Tenant Application Reject', 'sys.menu.system.merchantAudit.reject', 107, 3, '#', '', '1', '0', 'F', '1', '1', 'system:tenant:application:reject', '#', 'system', now(), ''),
    (1003, 1, 'Merchant Profile Query', 'sys.menu.system.merchantProfile.query', 108, 1, '#', '', '1', '0', 'F', '1', '1', 'system:merchant:profile:query', '#', 'system', now(), ''),
    (20004, 1, 'Merchant Profile Edit', 'sys.menu.system.merchantProfile.edit', 108, 2, '#', '', '1', '0', 'F', '1', '1', 'system:merchant:profile:edit', '#', 'system', now(), '')
ON CONFLICT (menu_id) DO NOTHING;

UPDATE sys_menu SET i18n_key = 'sys.menu.system' WHERE menu_id = 1 AND i18n_key IS NULL;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.user' WHERE menu_id = 100 AND i18n_key IS NULL;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.role' WHERE menu_id = 101 AND i18n_key IS NULL;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.menu' WHERE menu_id = 102 AND i18n_key IS NULL;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.dept' WHERE menu_id = 103 AND i18n_key IS NULL;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.post' WHERE menu_id = 104 AND i18n_key IS NULL;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.dict' WHERE menu_id = 105 AND i18n_key IS NULL;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.config' WHERE menu_id = 106 AND i18n_key IS NULL;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.merchantAudit' WHERE menu_id = 107 AND i18n_key IS NULL;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.merchantProfile' WHERE menu_id = 108 AND i18n_key IS NULL;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.merchantAudit.query' WHERE menu_id = 1001 AND i18n_key IS NULL;
UPDATE sys_menu SET menu_name = 'Tenant Application Approve', i18n_key = 'sys.menu.system.merchantAudit.approve', perms = 'system:tenant:application:approve', order_num = 2 WHERE menu_id = 1002;
UPDATE sys_menu SET menu_name = 'Tenant Application Reject', i18n_key = 'sys.menu.system.merchantAudit.reject', perms = 'system:tenant:application:reject', order_num = 3 WHERE menu_id = 1004;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.merchantProfile.query' WHERE menu_id = 1003 AND i18n_key IS NULL;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.merchantProfile.edit' WHERE menu_id = 20004 AND i18n_key IS NULL;
UPDATE sys_menu SET tenant_id = 1 WHERE menu_id IN (1, 100, 101, 102, 103, 104, 105, 106, 107, 108, 1001, 1002, 1003, 1004, 20004);
UPDATE sys_menu
SET menu_name = '系统管理', order_num = 910, icon = 'system', update_by = 'system', update_time = now()
WHERE menu_id = 1 AND tenant_id = 1;


INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (120, 1, 'Notice', 'sys.menu.system.notice', 1, 9, 'notice', 'system/notice/index', '1', '0', 'C', '1', '1', 'system:notice:list', 'message', 'system', now(), 'Notice management'),
    (121, 1, 'File', 'sys.menu.system.oss', 1, 10, 'oss', 'system/oss/index', '1', '0', 'C', '1', '1', 'system:oss:list', 'upload', 'system', now(), 'File management'),
    (200, 1, '系统监控', 'sys.menu.monitor', 0, 920, 'monitor', NULL, '1', '0', 'M', '1', '1', NULL, 'monitor', 'system', now(), 'System monitor'),
    (201, 1, 'Online Users', 'sys.menu.monitor.online', 200, 1, 'online', 'monitor/online/index', '1', '0', 'C', '1', '1', 'monitor:online:list', 'online', 'system', now(), 'Online users'),
    (202, 1, 'Cache Monitor', 'sys.menu.monitor.cache', 200, 2, 'cache', 'monitor/cache/index', '1', '0', 'C', '1', '1', 'monitor:cache:list', 'redis', 'system', now(), 'Cache monitor'),
    (205, 1, 'Server Resources', 'sys.menu.monitor.server', 200, 3, 'server', 'monitor/server/index', '1', '0', 'C', '1', '1', 'monitor:server:list', 'server', 'system', now(), 'Server resource snapshot'),
    (203, 1, 'Cache List', 'sys.menu.monitor.cacheList', 200, 4, 'cacheList', 'monitor/cache/list', '1', '0', 'C', '1', '1', 'monitor:cache:list', 'redis-list', 'system', now(), 'Cache list'),
    (204, 1, 'Clear Cache', 'sys.menu.monitor.cacheList.clear', 203, 1, '#', '', '1', '0', 'F', '1', '1', 'monitor:cache:remove', '#', 'system', now(), 'Clear cache'),
    (300, 1, 'Code Generator', 'sys.menu.tool.gen', 1, 99, 'gen', 'tool/gen/index', '1', '0', 'C', '1', '1', 'tool:gen:list', 'code', 'system', now(), 'Code generator'),
    (400, 1, '日志管理', 'sys.menu.log', 0, 930, 'log', NULL, '1', '0', 'M', '1', '1', NULL, 'log', 'system', now(), 'Log management'),
    (401, 1, 'Operation Logs', 'sys.menu.log.operlog', 400, 1, 'operlog', 'monitor/operlog/index', '1', '0', 'C', '1', '1', 'monitor:operlog:list', 'form', 'system', now(), 'Operation logs'),
    (402, 1, 'Login Logs', 'sys.menu.log.logininfor', 400, 2, 'logininfor', 'monitor/logininfor/index', '1', '0', 'C', '1', '1', 'monitor:logininfor:list', 'logininfor', 'system', now(), 'Login logs')
ON CONFLICT (menu_id) DO UPDATE
SET menu_name = EXCLUDED.menu_name,
    i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id,
    order_num = EXCLUDED.order_num,
    path = EXCLUDED.path,
    component = EXCLUDED.component,
    is_frame = EXCLUDED.is_frame,
    is_cache = EXCLUDED.is_cache,
    menu_type = EXCLUDED.menu_type,
    visible = EXCLUDED.visible,
    status = EXCLUDED.status,
    perms = EXCLUDED.perms,
    icon = EXCLUDED.icon,
    tenant_id = EXCLUDED.tenant_id,
    update_by = 'system',
    update_time = now();
UPDATE sys_menu SET tenant_id = 1 WHERE menu_id IN (120, 121, 200, 201, 202, 203, 204, 205, 300, 400, 401, 402);

-- 阶段 12：角色默认首页、门店管理和角色管理按钮。
INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (130, 1, '门店管理', 'sys.menu.system.salesStoreMenu', 1, 8, 'salesStore', 'system/sales-store/index', '1', '0', 'C', '1', '1', 'system:sales-store:list', 'store', 'system', now(), '内部销售门店管理'),
    (131, 1, '门店查询', 'sys.menu.system.salesStore.query', 130, 1, '#', '', '1', '0', 'F', '1', '1', 'system:sales-store:query', '#', 'system', now(), ''),
    (132, 1, '门店新增', 'sys.menu.system.salesStore.add', 130, 2, '#', '', '1', '0', 'F', '1', '1', 'system:sales-store:add', '#', 'system', now(), ''),
    (133, 1, '门店修改', 'sys.menu.system.salesStore.edit', 130, 3, '#', '', '1', '0', 'F', '1', '1', 'system:sales-store:edit', '#', 'system', now(), ''),
    (134, 1, '门店状态', 'sys.menu.system.salesStore.status', 130, 4, '#', '', '1', '0', 'F', '1', '1', 'system:sales-store:status', '#', 'system', now(), ''),
    (1011, 1, '角色查询', 'sys.menu.system.role.query', 101, 1, '#', '', '1', '0', 'F', '1', '1', 'system:role:query', '#', 'system', now(), ''),
    (1012, 1, '角色新增', 'sys.menu.system.role.add', 101, 2, '#', '', '1', '0', 'F', '1', '1', 'system:role:add', '#', 'system', now(), ''),
    (1013, 1, '角色修改', 'sys.menu.system.role.edit', 101, 3, '#', '', '1', '0', 'F', '1', '1', 'system:role:edit', '#', 'system', now(), '包含默认首页维护'),
    (1014, 1, '角色删除', 'sys.menu.system.role.remove', 101, 4, '#', '', '1', '0', 'F', '1', '1', 'system:role:remove', '#', 'system', now(), ''),
    (1015, 1, '角色导出', 'sys.menu.system.role.export', 101, 5, '#', '', '1', '0', 'F', '1', '1', 'system:role:export', '#', 'system', now(), '')
ON CONFLICT (menu_id) DO UPDATE
SET tenant_id = EXCLUDED.tenant_id, menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id, order_num = EXCLUDED.order_num, path = EXCLUDED.path,
    component = EXCLUDED.component, menu_type = EXCLUDED.menu_type, visible = EXCLUDED.visible,
    status = EXCLUDED.status, perms = EXCLUDED.perms, icon = EXCLUDED.icon,
    update_by = 'system', update_time = now(), remark = EXCLUDED.remark;

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT 1, menu_id, 1
FROM sys_menu
WHERE tenant_id = 1 AND menu_id IN (130, 131, 132, 133, 134, 1011, 1012, 1013, 1014, 1015)
ON CONFLICT DO NOTHING;

UPDATE sys_role r
SET default_menu_id = m.menu_id,
    update_by = 'system',
    update_time = now()
FROM sys_menu m
WHERE r.tenant_id = 1
  AND r.role_key = 'admin'
  AND m.tenant_id = r.tenant_id
  AND m.perms = 'monitor:server:list'
  AND m.menu_type = 'C'
  AND m.visible = '1'
  AND m.status = '1';

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT 1, menu_id, 1 FROM sys_menu
WHERE tenant_id = 1
ON CONFLICT DO NOTHING;
UPDATE sys_role_menu SET tenant_id = 1 WHERE role_id = 1;


INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (19500, 1, '商家管理', 'sys.menu.merchantManagement', 0, 400, 'merchantManagement', NULL, '1', '0', 'M', '1', '1', NULL, 'store', 'system', now(), 'Merchant management'),
    (19501, 1, 'Legal Content', 'sys.menu.system.legalDocument', 1, 11, 'legal/document', 'system/legal/document', '1', '0', 'C', '1', '1', 'system:legal:document:list', 'documentation', 'system', now(), 'Legal content'),
    (20005, 1, 'Legal Content Query', 'sys.menu.system.legalDocument.query', 19501, 1, '#', '', '1', '0', 'F', '1', '1', 'system:legal:document:query', '#', 'system', now(), ''),
    (20006, 1, 'Legal Content Add', 'sys.menu.system.legalDocument.add', 19501, 2, '#', '', '1', '0', 'F', '1', '1', 'system:legal:document:add', '#', 'system', now(), ''),
    (20007, 1, 'Legal Content Edit', 'sys.menu.system.legalDocument.edit', 19501, 3, '#', '', '1', '0', 'F', '1', '1', 'system:legal:document:edit', '#', 'system', now(), ''),
    (20008, 1, 'Legal Content Delete', 'sys.menu.system.legalDocument.remove', 19501, 4, '#', '', '1', '0', 'F', '1', '1', 'system:legal:document:remove', '#', 'system', now(), ''),
    (20009, 1, 'Merchant Staff Management', 'sys.menu.system.merchantStaff', 19500, 3, 'merchantStaff', 'system/merchant/users', '1', '0', 'C', '1', '1', 'merchant:user:list', 'customer', 'system', now(), 'Merchant staff management'),
    (20010, 1, 'Merchant Staff Query', 'sys.menu.system.merchantStaff.query', 20009, 1, '#', '', '1', '0', 'F', '1', '1', 'merchant:user:query', '#', 'system', now(), ''),
    (20011, 1, 'Merchant Staff Add', 'sys.menu.system.merchantStaff.add', 20009, 2, '#', '', '1', '0', 'F', '1', '1', 'merchant:user:add', '#', 'system', now(), ''),
    (20012, 1, 'Merchant Staff Edit', 'sys.menu.system.merchantStaff.edit', 20009, 3, '#', '', '1', '0', 'F', '1', '1', 'merchant:user:edit', '#', 'system', now(), ''),
    (20013, 1, 'Merchant Staff Delete', 'sys.menu.system.merchantStaff.remove', 20009, 4, '#', '', '1', '0', 'F', '1', '1', 'merchant:user:remove', '#', 'system', now(), '')
ON CONFLICT (menu_id) DO UPDATE
SET menu_name = EXCLUDED.menu_name,
    i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id,
    order_num = EXCLUDED.order_num,
    path = EXCLUDED.path,
    component = EXCLUDED.component,
    is_frame = EXCLUDED.is_frame,
    is_cache = EXCLUDED.is_cache,
    menu_type = EXCLUDED.menu_type,
    visible = EXCLUDED.visible,
    status = EXCLUDED.status,
    perms = EXCLUDED.perms,
    icon = EXCLUDED.icon,
    tenant_id = EXCLUDED.tenant_id,
    update_by = 'system',
    update_time = now();
UPDATE sys_menu SET parent_id = 19500, order_num = 1, path = 'tenantApplication' WHERE menu_id = 107;
UPDATE sys_menu SET parent_id = 19500, order_num = 2, path = 'merchantProfile' WHERE menu_id = 108;
INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
VALUES (1, 19500, 1), (1, 19501, 1), (1, 20004, 1), (1, 20005, 1), (1, 20006, 1), (1, 20007, 1), (1, 20008, 1),
       (1, 20009, 1), (1, 20010, 1), (1, 20011, 1), (1, 20012, 1), (1, 20013, 1)
ON CONFLICT DO NOTHING;

INSERT INTO merchant_level (level_id, tenant_id, level_code, level_name, default_discount_rate, default_credit_limit, default_flag, sort_order, status, del_flag, create_by, create_time, remark)
VALUES
    (210001, 1, 'NORMAL', '普通', 1.0000, 0.00, true, 1, 'ENABLED', '0', 'system', now(), '默认普通商户等级'),
    (210002, 1, 'VIP', 'VIP', 0.9500, 5000.00, false, 2, 'ENABLED', '0', 'system', now(), 'VIP 商户等级'),
    (210003, 1, 'KEY_ACCOUNT', '大客户', 0.9000, 20000.00, false, 3, 'ENABLED', '0', 'system', now(), '大客户等级')
ON CONFLICT (level_id) DO UPDATE
SET level_code = EXCLUDED.level_code,
    level_name = EXCLUDED.level_name,
    default_discount_rate = EXCLUDED.default_discount_rate,
    default_credit_limit = EXCLUDED.default_credit_limit,
    default_flag = EXCLUDED.default_flag,
    sort_order = EXCLUDED.sort_order,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (20100, 1, '商户等级', 'sys.menu.merchant.level', 19500, 4, 'merchantLevel', 'merchant/level', '1', '0', 'C', '1', '1', 'merchant:level:list', 'rate', 'system', now(), '商户等级管理'),
    (20101, 1, '商户等级查询', 'sys.menu.merchant.level.query', 20100, 1, '#', '', '1', '0', 'F', '1', '1', 'merchant:level:query', '#', 'system', now(), ''),
    (20102, 1, '商户等级新增', 'sys.menu.merchant.level.add', 20100, 2, '#', '', '1', '0', 'F', '1', '1', 'merchant:level:add', '#', 'system', now(), ''),
    (20103, 1, '商户等级修改', 'sys.menu.merchant.level.edit', 20100, 3, '#', '', '1', '0', 'F', '1', '1', 'merchant:level:edit', '#', 'system', now(), ''),
    (20104, 1, '商户等级删除', 'sys.menu.merchant.level.remove', 20100, 4, '#', '', '1', '0', 'F', '1', '1', 'merchant:level:remove', '#', 'system', now(), ''),
    (20105, 1, '商户等级导出', 'sys.menu.merchant.level.export', 20100, 5, '#', '', '1', '0', 'F', '1', '1', 'merchant:level:export', '#', 'system', now(), ''),
    (20120, 1, '等级折扣', 'sys.menu.merchant.levelDiscount', 19500, 5, 'levelDiscount', 'merchant/level-discount', '1', '0', 'C', '1', '1', 'merchant:levelDiscount:list', 'money', 'system', now(), '商户等级产品折扣'),
    (20121, 1, '等级折扣查询', 'sys.menu.merchant.levelDiscount.query', 20120, 1, '#', '', '1', '0', 'F', '1', '1', 'merchant:levelDiscount:query', '#', 'system', now(), ''),
    (20122, 1, '等级折扣新增', 'sys.menu.merchant.levelDiscount.add', 20120, 2, '#', '', '1', '0', 'F', '1', '1', 'merchant:levelDiscount:add', '#', 'system', now(), ''),
    (20123, 1, '等级折扣修改', 'sys.menu.merchant.levelDiscount.edit', 20120, 3, '#', '', '1', '0', 'F', '1', '1', 'merchant:levelDiscount:edit', '#', 'system', now(), ''),
    (20124, 1, '等级折扣删除', 'sys.menu.merchant.levelDiscount.remove', 20120, 4, '#', '', '1', '0', 'F', '1', '1', 'merchant:levelDiscount:remove', '#', 'system', now(), ''),
    (20125, 1, '等级折扣导出', 'sys.menu.merchant.levelDiscount.export', 20120, 5, '#', '', '1', '0', 'F', '1', '1', 'merchant:levelDiscount:export', '#', 'system', now(), ''),
    (20200, 1, '客户管理', 'sys.menu.customerManagement', 0, 20, 'customerManagement', NULL, '1', '0', 'M', '1', '1', NULL, 'customer', 'system', now(), '客户管理'),
    (20201, 1, '客户资料', 'sys.menu.customer.profile', 20200, 1, 'customers', 'customer/profile', '1', '0', 'C', '1', '1', 'customer:profile:list', 'customer', 'system', now(), '租户客户资料'),
    (20202, 1, '客户资料查询', 'sys.menu.customer.profile.query', 20201, 1, '#', '', '1', '0', 'F', '1', '1', 'customer:profile:query', '#', 'system', now(), ''),
    (20203, 1, '客户资料新增', 'sys.menu.customer.profile.add', 20201, 2, '#', '', '1', '0', 'F', '1', '1', 'customer:profile:add', '#', 'system', now(), ''),
    (20204, 1, '客户资料修改', 'sys.menu.customer.profile.edit', 20201, 3, '#', '', '1', '0', 'F', '1', '1', 'customer:profile:edit', '#', 'system', now(), ''),
    (20205, 1, '客户资料删除', 'sys.menu.customer.profile.remove', 20201, 4, '#', '', '1', '0', 'F', '1', '1', 'customer:profile:remove', '#', 'system', now(), ''),
    (20240, 1, '客户监管', 'sys.menu.customerSupervision', 0, 410, 'customerSupervision', NULL, '1', '0', 'M', '1', '1', NULL, 'peoples', 'system', now(), '平台客户监管'),
    (20220, 1, '全部客户', 'sys.menu.customer.all', 20240, 1, 'allCustomers', 'customer/all', '1', '0', 'C', '1', '1', 'platform:customer:list', 'peoples', 'system', now(), '平台全部客户管理'),
    (20221, 1, '全部客户查询', 'sys.menu.customer.all.query', 20220, 1, '#', '', '1', '0', 'F', '1', '1', 'platform:customer:query', '#', 'system', now(), '')
ON CONFLICT (menu_id) DO UPDATE
SET menu_name = EXCLUDED.menu_name,
    i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id,
    order_num = EXCLUDED.order_num,
    path = EXCLUDED.path,
    component = EXCLUDED.component,
    is_frame = EXCLUDED.is_frame,
    is_cache = EXCLUDED.is_cache,
    menu_type = EXCLUDED.menu_type,
    visible = EXCLUDED.visible,
    status = EXCLUDED.status,
    perms = EXCLUDED.perms,
    icon = EXCLUDED.icon,
    tenant_id = EXCLUDED.tenant_id,
    update_by = 'system',
    update_time = now();

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT 1, menu_id, 1 FROM sys_menu
WHERE tenant_id = 1
  AND menu_id IN (20100, 20101, 20102, 20103, 20104, 20105, 20120, 20121, 20122, 20123, 20124, 20125,
                  20200, 20201, 20202, 20203, 20204, 20205, 20240, 20220, 20221)
ON CONFLICT DO NOTHING;

WITH RECURSIVE merchant_customer_menus AS (
    SELECT r.role_id, r.tenant_id, m.menu_id, m.parent_id
    FROM sys_role r
    JOIN sys_menu m ON m.tenant_id = r.tenant_id
    WHERE r.role_key IN ('merchant_admin', 'merchant_employee')
      AND r.del_flag = '0'
      AND m.perms IN ('customer:profile:list', 'customer:profile:query', 'customer:profile:add', 'customer:profile:edit', 'customer:profile:remove')
    UNION
    SELECT c.role_id, c.tenant_id, p.menu_id, p.parent_id
    FROM merchant_customer_menus c
    JOIN sys_menu p ON p.menu_id = c.parent_id AND p.tenant_id = c.tenant_id
)
INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT DISTINCT role_id, menu_id, tenant_id
FROM merchant_customer_menus
ON CONFLICT DO NOTHING;

INSERT INTO sys_tenant (tenant_id, tenant_name, tenant_type, contact_name, contact_email, country, status, create_by, create_time, remark)
VALUES (300001, 'Demo Merchant', 'MERCHANT', 'Taylor Smith', 'demo.merchant@example.com', 'US', '1', 'system', now(), 'Local development merchant tenant')
ON CONFLICT (tenant_id) DO UPDATE
SET tenant_name = EXCLUDED.tenant_name,
    tenant_type = EXCLUDED.tenant_type,
    contact_name = EXCLUDED.contact_name,
    contact_email = EXCLUDED.contact_email,
    country = EXCLUDED.country,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now(),
    remark = EXCLUDED.remark;

INSERT INTO sys_dept (dept_id, tenant_id, parent_id, ancestors, dept_name, order_num, leader, email, status, del_flag, create_by, create_time, remark)
VALUES (300001, 300001, 0, '0', 'Dealer', 1, 'Taylor Smith', 'demo.merchant@example.com', '1', '0', 'system', now(), 'Demo merchant default department')
ON CONFLICT (dept_id) DO UPDATE
SET tenant_id = EXCLUDED.tenant_id,
    parent_id = EXCLUDED.parent_id,
    ancestors = EXCLUDED.ancestors,
    dept_name = EXCLUDED.dept_name,
    order_num = EXCLUDED.order_num,
    leader = EXCLUDED.leader,
    email = EXCLUDED.email,
    status = EXCLUDED.status,
    del_flag = EXCLUDED.del_flag,
    update_by = 'system',
    update_time = now(),
    remark = EXCLUDED.remark;

INSERT INTO sys_role (role_id, tenant_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES
    (300001, 300001, '店主', 'merchant_admin', 1, '4', true, true, '1', '0', 'system', now(), 'Demo merchant owner role'),
    (300003, 300001, '营业员', 'merchant_employee', 2, '5', true, true, '1', '0', 'system', now(), 'Demo merchant employee role')
ON CONFLICT (role_id) DO UPDATE
SET tenant_id = EXCLUDED.tenant_id,
    role_name = EXCLUDED.role_name,
    role_key = EXCLUDED.role_key,
    role_sort = EXCLUDED.role_sort,
    data_scope = EXCLUDED.data_scope,
    status = EXCLUDED.status,
    del_flag = EXCLUDED.del_flag,
    update_by = 'system',
    update_time = now(),
    remark = EXCLUDED.remark;

INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (300100, 300001, 'Merchant', 'menu.merchant', 0, 10, 'merchant', NULL, '1', '0', 'M', '1', '1', NULL, 'shop', 'system', now(), 'Demo merchant center'),
    (300101, 300001, 'Merchant Profile', 'menu.merchant.profile', 300100, 1, 'profile', 'merchant/profile', '1', '0', 'C', '1', '1', 'merchant:profile:query', 'store', 'system', now(), 'Current merchant profile'),
    (300102, 300001, 'Merchant Profile Query', 'menu.merchant.profile.query', 300101, 1, '#', '', '1', '0', 'F', '1', '1', 'merchant:profile:query', '#', 'system', now(), ''),
    (300103, 300001, 'Merchant Profile Edit', 'menu.merchant.profile.edit', 300101, 2, '#', '', '1', '0', 'F', '1', '1', 'merchant:profile:edit', '#', 'system', now(), ''),
    (300110, 300001, 'Merchant Users', 'menu.merchant.users', 300100, 2, 'users', 'merchant/user', '1', '0', 'C', '1', '1', 'merchant:user:list', 'user', 'system', now(), 'Current merchant users'),
    (300111, 300001, 'Merchant User Query', 'menu.merchant.users.query', 300110, 1, '#', '', '1', '0', 'F', '1', '1', 'merchant:user:query', '#', 'system', now(), ''),
    (300112, 300001, 'Merchant User Add', 'menu.merchant.users.add', 300110, 2, '#', '', '1', '0', 'F', '1', '1', 'merchant:user:add', '#', 'system', now(), ''),
    (300113, 300001, 'Merchant User Edit', 'menu.merchant.users.edit', 300110, 3, '#', '', '1', '0', 'F', '1', '1', 'merchant:user:edit', '#', 'system', now(), ''),
    (300114, 300001, 'Merchant User Delete', 'menu.merchant.users.remove', 300110, 4, '#', '', '1', '0', 'F', '1', '1', 'merchant:user:remove', '#', 'system', now(), ''),
    (300115, 300001, 'Merchant User Reset Password', 'menu.merchant.users.resetPwd', 300110, 5, '#', '', '1', '0', 'F', '1', '1', 'merchant:user:resetPwd', '#', 'system', now(), ''),
    (300120, 300001, 'Customers', 'menu.customer.profile', 300100, 3, 'customers', 'customer/profile', '1', '0', 'C', '1', '1', 'customer:profile:list', 'customer', 'system', now(), 'Current tenant customers'),
    (300121, 300001, 'Customer Query', 'menu.customer.profile.query', 300120, 1, '#', '', '1', '0', 'F', '1', '1', 'customer:profile:query', '#', 'system', now(), ''),
    (300122, 300001, 'Customer Add', 'menu.customer.profile.add', 300120, 2, '#', '', '1', '0', 'F', '1', '1', 'customer:profile:add', '#', 'system', now(), ''),
    (300123, 300001, 'Customer Edit', 'menu.customer.profile.edit', 300120, 3, '#', '', '1', '0', 'F', '1', '1', 'customer:profile:edit', '#', 'system', now(), ''),
    (300124, 300001, 'Customer Delete', 'menu.customer.profile.remove', 300120, 4, '#', '', '1', '0', 'F', '1', '1', 'customer:profile:remove', '#', 'system', now(), '')
ON CONFLICT (menu_id) DO UPDATE
SET menu_name = EXCLUDED.menu_name,
    i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id,
    order_num = EXCLUDED.order_num,
    path = EXCLUDED.path,
    component = EXCLUDED.component,
    is_frame = EXCLUDED.is_frame,
    is_cache = EXCLUDED.is_cache,
    menu_type = EXCLUDED.menu_type,
    visible = EXCLUDED.visible,
    status = EXCLUDED.status,
    perms = EXCLUDED.perms,
    icon = EXCLUDED.icon,
    tenant_id = EXCLUDED.tenant_id,
    update_by = 'system',
    update_time = now();

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT 300001, menu_id, 300001
FROM sys_menu
WHERE tenant_id = 300001
  AND menu_id BETWEEN 300100 AND 300124
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT 300003, menu_id, 300001
FROM sys_menu
WHERE tenant_id = 300001
  AND (menu_id IN (300100, 300101, 300102) OR menu_id BETWEEN 300120 AND 300124)
ON CONFLICT DO NOTHING;

INSERT INTO sys_user (user_id, tenant_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, password, force_password_change, status, del_flag, create_by, create_time, remark)
VALUES (300001, 300001, 300001, 'demo_merchant', 'Demo Merchant Admin', 'sys_user', 'demo.merchant@example.com', '18830000101', '0',
        '$2a$10$JKJTOxd4d2I4.ee73mbJEe8M4AIhABfTfwNHvAvjGg978hsiBxeV6', '0', '1', '0', 'system', now(), 'Local development merchant administrator')
ON CONFLICT (user_id) DO UPDATE
SET tenant_id = EXCLUDED.tenant_id,
    dept_id = EXCLUDED.dept_id,
    user_name = EXCLUDED.user_name,
    nick_name = EXCLUDED.nick_name,
    email = EXCLUDED.email,
    phonenumber = EXCLUDED.phonenumber,
    status = EXCLUDED.status,
    del_flag = EXCLUDED.del_flag,
    update_by = 'system',
    update_time = now(),
    remark = EXCLUDED.remark;

INSERT INTO sys_user_role (user_id, role_id, tenant_id)
VALUES (300001, 300001, 300001)
ON CONFLICT DO NOTHING;

INSERT INTO merchant_profile (merchant_id, tenant_id, merchant_name, company_name, contact_first_name, contact_last_name, contact_name,
                              primary_email, office_phone, mobile_phone, country, state, city, address_line1, postal_code,
                              status, audit_status, audit_by, audit_by_id, audit_time, level_id, level_code, level_name,
                              discount_rate, credit_limit, credit_term_days, create_by, create_time, remark)
VALUES (300001, 300001, 'Demo Merchant', 'Demo Merchant LLC', 'Taylor', 'Smith', 'Taylor Smith',
        'demo.merchant@example.com', '+1 415 555 0101', '+1 415 555 0102', 'US', 'CA', 'San Francisco',
        '100 Market Street', '94105', '1', 'APPROVED', 'system', 1, now(), 210002, 'VIP', 'VIP',
        0.9500, 5000.00, 30, 'system', now(), 'Local development merchant profile')
ON CONFLICT (merchant_id) DO UPDATE
SET merchant_name = EXCLUDED.merchant_name,
    company_name = EXCLUDED.company_name,
    contact_first_name = EXCLUDED.contact_first_name,
    contact_last_name = EXCLUDED.contact_last_name,
    contact_name = EXCLUDED.contact_name,
    primary_email = EXCLUDED.primary_email,
    office_phone = EXCLUDED.office_phone,
    mobile_phone = EXCLUDED.mobile_phone,
    country = EXCLUDED.country,
    state = EXCLUDED.state,
    city = EXCLUDED.city,
    address_line1 = EXCLUDED.address_line1,
    postal_code = EXCLUDED.postal_code,
    status = EXCLUDED.status,
    audit_status = EXCLUDED.audit_status,
    level_id = EXCLUDED.level_id,
    level_code = EXCLUDED.level_code,
    level_name = EXCLUDED.level_name,
    discount_rate = EXCLUDED.discount_rate,
    credit_limit = EXCLUDED.credit_limit,
    credit_term_days = EXCLUDED.credit_term_days,
    update_by = 'system',
    update_time = now(),
    remark = EXCLUDED.remark;

CREATE TABLE IF NOT EXISTS sys_legal_document (
    document_id bigint PRIMARY KEY,
    document_type varchar(30) NOT NULL,
    locale varchar(20) NOT NULL,
    title varchar(200) NOT NULL,
    content text NOT NULL,
    version varchar(50),
    status varchar(20) NOT NULL DEFAULT 'DRAFT',
    published_time timestamptz,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500),
    create_by_id bigint
);
COMMENT ON TABLE sys_legal_document IS 'Legal document content, UTC timestamps';
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_legal_document_type_locale_version ON sys_legal_document(document_type, locale, version);
CREATE INDEX IF NOT EXISTS idx_sys_legal_document_public ON sys_legal_document(document_type, locale, status, published_time);
COMMENT ON COLUMN sys_legal_document.document_id IS '法律文档ID';
COMMENT ON COLUMN sys_legal_document.document_type IS '文档类型';
COMMENT ON COLUMN sys_legal_document.locale IS '语言区域';
COMMENT ON COLUMN sys_legal_document.title IS '标题';
COMMENT ON COLUMN sys_legal_document.content IS '内容';
COMMENT ON COLUMN sys_legal_document.version IS '版本号';
COMMENT ON COLUMN sys_legal_document.status IS '状态';
COMMENT ON COLUMN sys_legal_document.published_time IS '发布时间，UTC timestamptz';
COMMENT ON COLUMN sys_legal_document.create_by IS '创建者';
COMMENT ON COLUMN sys_legal_document.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_legal_document.update_by IS '更新者';
COMMENT ON COLUMN sys_legal_document.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_legal_document.remark IS '备注';
COMMENT ON COLUMN sys_legal_document.create_by_id IS '创建者ID';

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES
    (19005, 'User gender', 'sys_user_sex', '1', 'system', now(), 'User gender')
ON CONFLICT (dict_id) DO NOTHING;

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES
    (19006, 'Operation type', 'sys_oper_type', '1', 'system', now(), 'Operation log business type')
ON CONFLICT (dict_type) DO UPDATE
SET dict_name = EXCLUDED.dict_name,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES
    (19024, 1, 'Male', '0', 'sys_user_sex', 'primary', 'Y', '1', 'system', now()),
    (19025, 2, 'Female', '1', 'sys_user_sex', 'primary', 'N', '1', 'system', now()),
    (19026, 3, 'Unknown', '2', 'sys_user_sex', 'info', 'N', '1', 'system', now())
ON CONFLICT (dict_code) DO UPDATE
SET dict_sort = EXCLUDED.dict_sort,
    dict_label = EXCLUDED.dict_label,
    dict_value = EXCLUDED.dict_value,
    dict_type = EXCLUDED.dict_type,
    list_class = EXCLUDED.list_class,
    is_default = EXCLUDED.is_default,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
SELECT 19027, 10, 'Cross-tenant Query', '10', 'sys_oper_type', 'warning', 'N', '1', 'system', now()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data WHERE dict_type = 'sys_oper_type' AND dict_value = '10'
);
UPDATE sys_dict_data
SET dict_sort = 10,
    dict_label = 'Cross-tenant Query',
    list_class = 'warning',
    status = '1',
    update_by = 'system',
    update_time = now()
WHERE dict_type = 'sys_oper_type' AND dict_value = '10';

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
SELECT 19028, 11, 'Sensitive Operation', '11', 'sys_oper_type', 'danger', 'N', '1', 'system', now()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data WHERE dict_type = 'sys_oper_type' AND dict_value = '11'
);
UPDATE sys_dict_data
SET dict_sort = 11,
    dict_label = 'Sensitive Operation',
    list_class = 'danger',
    status = '1',
    update_by = 'system',
    update_time = now()
WHERE dict_type = 'sys_oper_type' AND dict_value = '11';


INSERT INTO sys_legal_document (document_id, document_type, locale, title, content, version, status, published_time, create_by, create_time, remark)
VALUES
    (1, 'privacy', 'en_US', 'Privacy Policy', $LEGAL$Privacy Policy

Last Updated: May 28, 2026

1. Introduction
Company Name ("we", "our", or "us") respects your privacy and is committed to protecting the personal information you provide when using our website, applications, and ordering system for custom curtains. This Privacy Policy explains how we collect, use, and share information.

2. Information We Collect
We may collect the following information:
- Company information: name, contact person
- Contact details: email, phone number
- Shipping and billing address
- Order details: product specifications, quantity
- Payment information (processed securely by third-party providers)
- Usage information: IP address, browser type, device information
- Cookies and analytics data

3. How We Use Information
We use your information to:
- Process and fulfill orders
- Provide customer service and support
- Improve our services and products
- Communicate order updates and marketing (if opted-in)
- Prevent fraud and ensure payment security
- Comply with legal obligations

4. Payment Processing
Payments are securely processed by third-party providers such as Stripe or PayPal. We do not store full payment card information on our servers.

5. Sharing of Information
We do not sell personal information. We may share information with:
- Shipping and logistics providers
- Payment processors
- Analytics providers
- IT service providers

6. Data Retention
We retain your data only as long as necessary to provide our services and comply with legal obligations.

7. User Rights
You may contact us to:
- Access the data we hold about your company
- Request corrections
- Request deletion of data
- Opt out of marketing communications

8. Security
We implement reasonable administrative, technical, and physical safeguards to protect your information. No system is completely secure; we cannot guarantee absolute security.

9. Children's Privacy
Our services are not intended for children under 13.

10. Changes to This Policy
We may update this Privacy Policy from time to time. Updated policies will be posted on this page.

11. Contact
Company Name
Email: [your email]
Address: [your business address]$LEGAL$, '2026-05-28', 'PUBLISHED', now(), 'system', now(), 'Imported from privacy.txt'),
    (2, 'terms', 'en_US', 'Terms & Conditions', $LEGAL$Terms & Conditions

Last Updated: May 28, 2026

1. Introduction
These Terms & Conditions ("Terms") govern your use of our ordering system and services for custom curtains. By placing an order, you agree to these Terms.

2. Orders and Specifications
- Customers are responsible for providing accurate measurements and specifications.
- All orders must be reviewed and confirmed before production begins.

3. Custom Product Disclaimer
- All products are custom-made according to customer specifications.
- Minor variations in color, texture, dimensions, or finishing may occur and shall not be considered defects.

4. Pricing and Payment
- Prices are listed in USD and subject to change.
- Deposits may be required before production; balance payment is due as specified.
- Late payments may incur fees.
- Taxes are the responsibility of the customer.

5. Production and Lead Time
- Estimated production times are for reference only.
- Delays caused by materials, logistics, customization, or force majeure are not guaranteed liabilities.

6. Shipping and Delivery
- Risk of loss transfers to the customer upon delivery to the carrier.
- Customers are responsible for inspecting goods upon delivery.

7. Inspection and Claims
- Claims for defects or shortages must be submitted within 7 days of delivery.
- Verified manufacturing defects are eligible for replacement or credit.

8. Returns and Refunds
- Due to the custom nature of the products, orders cannot be canceled once production has started.
- Custom products are non-refundable except for verified manufacturing defects.

9. Intellectual Property
- All designs, images, logos, and software remain the property of Company Name.
- Customers may not reproduce or distribute our content without permission.

10. Limitation of Liability
- Our liability is limited to the total amount paid for the affected order.
- We are not liable for indirect, incidental, or consequential damages.

11. Force Majeure
- We are not responsible for delays or failures caused by events beyond our reasonable control, including natural disasters, transportation issues, or government actions.

12. Governing Law
- These Terms are governed by the laws of the State of [Your State], USA.

13. Contact Information
Company Name
Email: [your email]
Address: [your business address]$LEGAL$, '2026-05-28', 'PUBLISHED', now(), 'system', now(), 'Imported from Terms & Conditions.txt'),
    (3, 'cookie', 'en_US', 'Cookie Policy', $LEGAL$Cookie Policy

Last Updated: May 28, 2026

1. What Are Cookies
Cookies are small files stored on your device that help us improve your browsing experience, remember your preferences, and analyze website usage.

2. Types of Cookies We Use
- Essential Cookies: Required for the ordering system and secure login.
- Functional Cookies: Remember language preferences and other settings.
- Analytics Cookies: Used to analyze website traffic and improve services (e.g., Google Analytics).
- Marketing Cookies: Track advertising performance and targeting.

3. Third-Party Cookies
We may use third-party services that set cookies on our website, including:
- Google
- Stripe
- Facebook
- Cloudflare

4. Managing Cookies
You can manage or disable cookies through your browser settings. Please note that disabling certain cookies may affect the functionality of our website and ordering system.

5. Changes to This Policy
We may update this Cookie Policy at any time. Updated policies will be posted on this page.

6. Contact
Company Name
Email: [your email]
Address: [your business address]$LEGAL$, '2026-05-28', 'PUBLISHED', now(), 'system', now(), 'Imported from Cookie Policy.txt')
ON CONFLICT (document_type, locale, version) DO UPDATE
SET title = EXCLUDED.title,
    content = EXCLUDED.content,
    status = EXCLUDED.status,
    published_time = EXCLUDED.published_time,
    update_by = 'system',
    update_time = now();


-- Standard ISO/CLDR reference data
INSERT INTO sys_country (country_id, country_code, name_en, name_zh, status, sort, create_by, create_time)
VALUES
    (200001, 'AD', 'Andorra', '安道尔', '1', 100, 'system', now()),
    (200002, 'AE', 'United Arab Emirates', '阿拉伯联合酋长国', '1', 101, 'system', now()),
    (200003, 'AF', 'Afghanistan', '阿富汗', '1', 102, 'system', now()),
    (200004, 'AG', 'Antigua & Barbuda', '安提瓜和巴布达', '1', 103, 'system', now()),
    (200005, 'AI', 'Anguilla', '安圭拉', '1', 104, 'system', now()),
    (200006, 'AL', 'Albania', '阿尔巴尼亚', '1', 105, 'system', now()),
    (200007, 'AM', 'Armenia', '亚美尼亚', '1', 106, 'system', now()),
    (200008, 'AO', 'Angola', '安哥拉', '1', 107, 'system', now()),
    (200009, 'AQ', 'Antarctica', '南极洲', '1', 108, 'system', now()),
    (200010, 'AR', 'Argentina', '阿根廷', '1', 109, 'system', now()),
    (200011, 'AS', 'American Samoa', '美属萨摩亚', '1', 110, 'system', now()),
    (200012, 'AT', 'Austria', '奥地利', '1', 111, 'system', now()),
    (200013, 'AU', 'Australia', '澳大利亚', '1', 6, 'system', now()),
    (200014, 'AW', 'Aruba', '阿鲁巴', '1', 113, 'system', now()),
    (200015, 'AX', 'Åland Islands', '奥兰群岛', '1', 114, 'system', now()),
    (200016, 'AZ', 'Azerbaijan', '阿塞拜疆', '1', 115, 'system', now()),
    (200017, 'BA', 'Bosnia & Herzegovina', '波斯尼亚和黑塞哥维那', '1', 116, 'system', now()),
    (200018, 'BB', 'Barbados', '巴巴多斯', '1', 117, 'system', now()),
    (200019, 'BD', 'Bangladesh', '孟加拉国', '1', 118, 'system', now()),
    (200020, 'BE', 'Belgium', '比利时', '1', 119, 'system', now()),
    (200021, 'BF', 'Burkina Faso', '布基纳法索', '1', 120, 'system', now()),
    (200022, 'BG', 'Bulgaria', '保加利亚', '1', 121, 'system', now()),
    (200023, 'BH', 'Bahrain', '巴林', '1', 122, 'system', now()),
    (200024, 'BI', 'Burundi', '布隆迪', '1', 123, 'system', now()),
    (200025, 'BJ', 'Benin', '贝宁', '1', 124, 'system', now()),
    (200026, 'BL', 'St. Barthélemy', '圣巴泰勒米', '1', 125, 'system', now()),
    (200027, 'BM', 'Bermuda', '百慕大', '1', 126, 'system', now()),
    (200028, 'BN', 'Brunei', '文莱', '1', 127, 'system', now()),
    (200029, 'BO', 'Bolivia', '玻利维亚', '1', 128, 'system', now()),
    (200030, 'BQ', 'Caribbean Netherlands', '荷属加勒比区', '1', 129, 'system', now()),
    (200031, 'BR', 'Brazil', '巴西', '1', 130, 'system', now()),
    (200032, 'BS', 'Bahamas', '巴哈马', '1', 131, 'system', now()),
    (200033, 'BT', 'Bhutan', '不丹', '1', 132, 'system', now()),
    (200034, 'BV', 'Bouvet Island', '布韦岛', '1', 133, 'system', now()),
    (200035, 'BW', 'Botswana', '博茨瓦纳', '1', 134, 'system', now()),
    (200036, 'BY', 'Belarus', '白俄罗斯', '1', 135, 'system', now()),
    (200037, 'BZ', 'Belize', '伯利兹', '1', 136, 'system', now()),
    (200038, 'CA', 'Canada', '加拿大', '1', 3, 'system', now()),
    (200039, 'CC', 'Cocos (Keeling) Islands', '科科斯（基林）群岛', '1', 138, 'system', now()),
    (200040, 'CD', 'Congo - Kinshasa', '刚果（金）', '1', 139, 'system', now()),
    (200041, 'CF', 'Central African Republic', '中非共和国', '1', 140, 'system', now()),
    (200042, 'CG', 'Congo - Brazzaville', '刚果（布）', '1', 141, 'system', now()),
    (200043, 'CH', 'Switzerland', '瑞士', '1', 142, 'system', now()),
    (200044, 'CI', 'Côte d’Ivoire', '科特迪瓦', '1', 143, 'system', now()),
    (200045, 'CK', 'Cook Islands', '库克群岛', '1', 144, 'system', now()),
    (200046, 'CL', 'Chile', '智利', '1', 145, 'system', now()),
    (200047, 'CM', 'Cameroon', '喀麦隆', '1', 146, 'system', now()),
    (200048, 'CN', 'China', '中国', '1', 2, 'system', now()),
    (200049, 'CO', 'Colombia', '哥伦比亚', '1', 148, 'system', now()),
    (200050, 'CR', 'Costa Rica', '哥斯达黎加', '1', 149, 'system', now()),
    (200051, 'CU', 'Cuba', '古巴', '1', 150, 'system', now()),
    (200052, 'CV', 'Cape Verde', '佛得角', '1', 151, 'system', now()),
    (200053, 'CW', 'Curaçao', '库拉索', '1', 152, 'system', now()),
    (200054, 'CX', 'Christmas Island', '圣诞岛', '1', 153, 'system', now()),
    (200055, 'CY', 'Cyprus', '塞浦路斯', '1', 154, 'system', now()),
    (200056, 'CZ', 'Czechia', '捷克', '1', 155, 'system', now()),
    (200057, 'DE', 'Germany', '德国', '1', 156, 'system', now()),
    (200058, 'DJ', 'Djibouti', '吉布提', '1', 157, 'system', now()),
    (200059, 'DK', 'Denmark', '丹麦', '1', 158, 'system', now()),
    (200060, 'DM', 'Dominica', '多米尼克', '1', 159, 'system', now()),
    (200061, 'DO', 'Dominican Republic', '多米尼加共和国', '1', 160, 'system', now()),
    (200062, 'DZ', 'Algeria', '阿尔及利亚', '1', 161, 'system', now()),
    (200063, 'EC', 'Ecuador', '厄瓜多尔', '1', 162, 'system', now()),
    (200064, 'EE', 'Estonia', '爱沙尼亚', '1', 163, 'system', now()),
    (200065, 'EG', 'Egypt', '埃及', '1', 164, 'system', now()),
    (200066, 'EH', 'Western Sahara', '西撒哈拉', '1', 165, 'system', now()),
    (200067, 'ER', 'Eritrea', '厄立特里亚', '1', 166, 'system', now()),
    (200068, 'ES', 'Spain', '西班牙', '1', 167, 'system', now()),
    (200069, 'ET', 'Ethiopia', '埃塞俄比亚', '1', 168, 'system', now()),
    (200070, 'FI', 'Finland', '芬兰', '1', 169, 'system', now()),
    (200071, 'FJ', 'Fiji', '斐济', '1', 170, 'system', now()),
    (200072, 'FK', 'Falkland Islands', '福克兰群岛', '1', 171, 'system', now()),
    (200073, 'FM', 'Micronesia', '密克罗尼西亚', '1', 172, 'system', now()),
    (200074, 'FO', 'Faroe Islands', '法罗群岛', '1', 173, 'system', now()),
    (200075, 'FR', 'France', '法国', '1', 174, 'system', now()),
    (200076, 'GA', 'Gabon', '加蓬', '1', 175, 'system', now()),
    (200077, 'GB', 'United Kingdom', '英国', '1', 5, 'system', now()),
    (200078, 'GD', 'Grenada', '格林纳达', '1', 177, 'system', now()),
    (200079, 'GE', 'Georgia', '格鲁吉亚', '1', 178, 'system', now()),
    (200080, 'GF', 'French Guiana', '法属圭亚那', '1', 179, 'system', now()),
    (200081, 'GG', 'Guernsey', '根西岛', '1', 180, 'system', now()),
    (200082, 'GH', 'Ghana', '加纳', '1', 181, 'system', now()),
    (200083, 'GI', 'Gibraltar', '直布罗陀', '1', 182, 'system', now()),
    (200084, 'GL', 'Greenland', '格陵兰', '1', 183, 'system', now()),
    (200085, 'GM', 'Gambia', '冈比亚', '1', 184, 'system', now()),
    (200086, 'GN', 'Guinea', '几内亚', '1', 185, 'system', now()),
    (200087, 'GP', 'Guadeloupe', '瓜德罗普', '1', 186, 'system', now()),
    (200088, 'GQ', 'Equatorial Guinea', '赤道几内亚', '1', 187, 'system', now()),
    (200089, 'GR', 'Greece', '希腊', '1', 188, 'system', now()),
    (200090, 'GS', 'South Georgia & South Sandwich Islands', '南乔治亚和南桑威奇群岛', '1', 189, 'system', now()),
    (200091, 'GT', 'Guatemala', '危地马拉', '1', 190, 'system', now()),
    (200092, 'GU', 'Guam', '关岛', '1', 191, 'system', now()),
    (200093, 'GW', 'Guinea-Bissau', '几内亚比绍', '1', 192, 'system', now()),
    (200094, 'GY', 'Guyana', '圭亚那', '1', 193, 'system', now()),
    (200095, 'HK', 'Hong Kong SAR China', '中国香港特别行政区', '1', 194, 'system', now()),
    (200096, 'HM', 'Heard & McDonald Islands', '赫德岛和麦克唐纳群岛', '1', 195, 'system', now()),
    (200097, 'HN', 'Honduras', '洪都拉斯', '1', 196, 'system', now()),
    (200098, 'HR', 'Croatia', '克罗地亚', '1', 197, 'system', now()),
    (200099, 'HT', 'Haiti', '海地', '1', 198, 'system', now()),
    (200100, 'HU', 'Hungary', '匈牙利', '1', 199, 'system', now()),
    (200101, 'ID', 'Indonesia', '印度尼西亚', '1', 200, 'system', now()),
    (200102, 'IE', 'Ireland', '爱尔兰', '1', 201, 'system', now()),
    (200103, 'IL', 'Israel', '以色列', '1', 202, 'system', now()),
    (200104, 'IM', 'Isle of Man', '马恩岛', '1', 203, 'system', now()),
    (200105, 'IN', 'India', '印度', '1', 204, 'system', now()),
    (200106, 'IO', 'British Indian Ocean Territory', '英属印度洋领地', '1', 205, 'system', now()),
    (200107, 'IQ', 'Iraq', '伊拉克', '1', 206, 'system', now()),
    (200108, 'IR', 'Iran', '伊朗', '1', 207, 'system', now()),
    (200109, 'IS', 'Iceland', '冰岛', '1', 208, 'system', now()),
    (200110, 'IT', 'Italy', '意大利', '1', 209, 'system', now()),
    (200111, 'JE', 'Jersey', '泽西岛', '1', 210, 'system', now()),
    (200112, 'JM', 'Jamaica', '牙买加', '1', 211, 'system', now()),
    (200113, 'JO', 'Jordan', '约旦', '1', 212, 'system', now()),
    (200114, 'JP', 'Japan', '日本', '1', 213, 'system', now()),
    (200115, 'KE', 'Kenya', '肯尼亚', '1', 214, 'system', now()),
    (200116, 'KG', 'Kyrgyzstan', '吉尔吉斯斯坦', '1', 215, 'system', now()),
    (200117, 'KH', 'Cambodia', '柬埔寨', '1', 216, 'system', now()),
    (200118, 'KI', 'Kiribati', '基里巴斯', '1', 217, 'system', now()),
    (200119, 'KM', 'Comoros', '科摩罗', '1', 218, 'system', now()),
    (200120, 'KN', 'St. Kitts & Nevis', '圣基茨和尼维斯', '1', 219, 'system', now()),
    (200121, 'KP', 'North Korea', '朝鲜', '1', 220, 'system', now()),
    (200122, 'KR', 'South Korea', '韩国', '1', 221, 'system', now()),
    (200123, 'KW', 'Kuwait', '科威特', '1', 222, 'system', now()),
    (200124, 'KY', 'Cayman Islands', '开曼群岛', '1', 223, 'system', now()),
    (200125, 'KZ', 'Kazakhstan', '哈萨克斯坦', '1', 224, 'system', now()),
    (200126, 'LA', 'Laos', '老挝', '1', 225, 'system', now()),
    (200127, 'LB', 'Lebanon', '黎巴嫩', '1', 226, 'system', now()),
    (200128, 'LC', 'St. Lucia', '圣卢西亚', '1', 227, 'system', now()),
    (200129, 'LI', 'Liechtenstein', '列支敦士登', '1', 228, 'system', now()),
    (200130, 'LK', 'Sri Lanka', '斯里兰卡', '1', 229, 'system', now()),
    (200131, 'LR', 'Liberia', '利比里亚', '1', 230, 'system', now()),
    (200132, 'LS', 'Lesotho', '莱索托', '1', 231, 'system', now()),
    (200133, 'LT', 'Lithuania', '立陶宛', '1', 232, 'system', now()),
    (200134, 'LU', 'Luxembourg', '卢森堡', '1', 233, 'system', now()),
    (200135, 'LV', 'Latvia', '拉脱维亚', '1', 234, 'system', now()),
    (200136, 'LY', 'Libya', '利比亚', '1', 235, 'system', now()),
    (200137, 'MA', 'Morocco', '摩洛哥', '1', 236, 'system', now()),
    (200138, 'MC', 'Monaco', '摩纳哥', '1', 237, 'system', now()),
    (200139, 'MD', 'Moldova', '摩尔多瓦', '1', 238, 'system', now()),
    (200140, 'ME', 'Montenegro', '黑山', '1', 239, 'system', now()),
    (200141, 'MF', 'St. Martin', '法属圣马丁', '1', 240, 'system', now()),
    (200142, 'MG', 'Madagascar', '马达加斯加', '1', 241, 'system', now()),
    (200143, 'MH', 'Marshall Islands', '马绍尔群岛', '1', 242, 'system', now()),
    (200144, 'MK', 'North Macedonia', '北马其顿', '1', 243, 'system', now()),
    (200145, 'ML', 'Mali', '马里', '1', 244, 'system', now()),
    (200146, 'MM', 'Myanmar (Burma)', '缅甸', '1', 245, 'system', now()),
    (200147, 'MN', 'Mongolia', '蒙古', '1', 246, 'system', now()),
    (200148, 'MO', 'Macao SAR China', '中国澳门特别行政区', '1', 247, 'system', now()),
    (200149, 'MP', 'Northern Mariana Islands', '北马里亚纳群岛', '1', 248, 'system', now()),
    (200150, 'MQ', 'Martinique', '马提尼克', '1', 249, 'system', now()),
    (200151, 'MR', 'Mauritania', '毛里塔尼亚', '1', 250, 'system', now()),
    (200152, 'MS', 'Montserrat', '蒙特塞拉特', '1', 251, 'system', now()),
    (200153, 'MT', 'Malta', '马耳他', '1', 252, 'system', now()),
    (200154, 'MU', 'Mauritius', '毛里求斯', '1', 253, 'system', now()),
    (200155, 'MV', 'Maldives', '马尔代夫', '1', 254, 'system', now()),
    (200156, 'MW', 'Malawi', '马拉维', '1', 255, 'system', now()),
    (200157, 'MX', 'Mexico', '墨西哥', '1', 4, 'system', now()),
    (200158, 'MY', 'Malaysia', '马来西亚', '1', 257, 'system', now()),
    (200159, 'MZ', 'Mozambique', '莫桑比克', '1', 258, 'system', now()),
    (200160, 'NA', 'Namibia', '纳米比亚', '1', 259, 'system', now()),
    (200161, 'NC', 'New Caledonia', '新喀里多尼亚', '1', 260, 'system', now()),
    (200162, 'NE', 'Niger', '尼日尔', '1', 261, 'system', now()),
    (200163, 'NF', 'Norfolk Island', '诺福克岛', '1', 262, 'system', now()),
    (200164, 'NG', 'Nigeria', '尼日利亚', '1', 263, 'system', now()),
    (200165, 'NI', 'Nicaragua', '尼加拉瓜', '1', 264, 'system', now()),
    (200166, 'NL', 'Netherlands', '荷兰', '1', 265, 'system', now()),
    (200167, 'NO', 'Norway', '挪威', '1', 266, 'system', now()),
    (200168, 'NP', 'Nepal', '尼泊尔', '1', 267, 'system', now()),
    (200169, 'NR', 'Nauru', '瑙鲁', '1', 268, 'system', now()),
    (200170, 'NU', 'Niue', '纽埃', '1', 269, 'system', now()),
    (200171, 'NZ', 'New Zealand', '新西兰', '1', 270, 'system', now()),
    (200172, 'OM', 'Oman', '阿曼', '1', 271, 'system', now()),
    (200173, 'PA', 'Panama', '巴拿马', '1', 272, 'system', now()),
    (200174, 'PE', 'Peru', '秘鲁', '1', 273, 'system', now()),
    (200175, 'PF', 'French Polynesia', '法属波利尼西亚', '1', 274, 'system', now()),
    (200176, 'PG', 'Papua New Guinea', '巴布亚新几内亚', '1', 275, 'system', now()),
    (200177, 'PH', 'Philippines', '菲律宾', '1', 276, 'system', now()),
    (200178, 'PK', 'Pakistan', '巴基斯坦', '1', 277, 'system', now()),
    (200179, 'PL', 'Poland', '波兰', '1', 278, 'system', now()),
    (200180, 'PM', 'St. Pierre & Miquelon', '圣皮埃尔和密克隆群岛', '1', 279, 'system', now()),
    (200181, 'PN', 'Pitcairn Islands', '皮特凯恩群岛', '1', 280, 'system', now()),
    (200182, 'PR', 'Puerto Rico', '波多黎各', '1', 281, 'system', now()),
    (200183, 'PS', 'Palestinian Territories', '巴勒斯坦领土', '1', 282, 'system', now()),
    (200184, 'PT', 'Portugal', '葡萄牙', '1', 283, 'system', now()),
    (200185, 'PW', 'Palau', '帕劳', '1', 284, 'system', now()),
    (200186, 'PY', 'Paraguay', '巴拉圭', '1', 285, 'system', now()),
    (200187, 'QA', 'Qatar', '卡塔尔', '1', 286, 'system', now()),
    (200188, 'RE', 'Réunion', '留尼汪', '1', 287, 'system', now()),
    (200189, 'RO', 'Romania', '罗马尼亚', '1', 288, 'system', now()),
    (200190, 'RS', 'Serbia', '塞尔维亚', '1', 289, 'system', now()),
    (200191, 'RU', 'Russia', '俄罗斯', '1', 290, 'system', now()),
    (200192, 'RW', 'Rwanda', '卢旺达', '1', 291, 'system', now()),
    (200193, 'SA', 'Saudi Arabia', '沙特阿拉伯', '1', 292, 'system', now()),
    (200194, 'SB', 'Solomon Islands', '所罗门群岛', '1', 293, 'system', now()),
    (200195, 'SC', 'Seychelles', '塞舌尔', '1', 294, 'system', now()),
    (200196, 'SD', 'Sudan', '苏丹', '1', 295, 'system', now()),
    (200197, 'SE', 'Sweden', '瑞典', '1', 296, 'system', now()),
    (200198, 'SG', 'Singapore', '新加坡', '1', 297, 'system', now()),
    (200199, 'SH', 'St. Helena', '圣赫勒拿', '1', 298, 'system', now()),
    (200200, 'SI', 'Slovenia', '斯洛文尼亚', '1', 299, 'system', now()),
    (200201, 'SJ', 'Svalbard & Jan Mayen', '斯瓦尔巴和扬马延', '1', 300, 'system', now()),
    (200202, 'SK', 'Slovakia', '斯洛伐克', '1', 301, 'system', now()),
    (200203, 'SL', 'Sierra Leone', '塞拉利昂', '1', 302, 'system', now()),
    (200204, 'SM', 'San Marino', '圣马力诺', '1', 303, 'system', now()),
    (200205, 'SN', 'Senegal', '塞内加尔', '1', 304, 'system', now()),
    (200206, 'SO', 'Somalia', '索马里', '1', 305, 'system', now()),
    (200207, 'SR', 'Suriname', '苏里南', '1', 306, 'system', now()),
    (200208, 'SS', 'South Sudan', '南苏丹', '1', 307, 'system', now()),
    (200209, 'ST', 'São Tomé & Príncipe', '圣多美和普林西比', '1', 308, 'system', now()),
    (200210, 'SV', 'El Salvador', '萨尔瓦多', '1', 309, 'system', now()),
    (200211, 'SX', 'Sint Maarten', '荷属圣马丁', '1', 310, 'system', now()),
    (200212, 'SY', 'Syria', '叙利亚', '1', 311, 'system', now()),
    (200213, 'SZ', 'Eswatini', '斯威士兰', '1', 312, 'system', now()),
    (200214, 'TC', 'Turks & Caicos Islands', '特克斯和凯科斯群岛', '1', 313, 'system', now()),
    (200215, 'TD', 'Chad', '乍得', '1', 314, 'system', now()),
    (200216, 'TF', 'French Southern Territories', '法属南部领地', '1', 315, 'system', now()),
    (200217, 'TG', 'Togo', '多哥', '1', 316, 'system', now()),
    (200218, 'TH', 'Thailand', '泰国', '1', 317, 'system', now()),
    (200219, 'TJ', 'Tajikistan', '塔吉克斯坦', '1', 318, 'system', now()),
    (200220, 'TK', 'Tokelau', '托克劳', '1', 319, 'system', now()),
    (200221, 'TL', 'Timor-Leste', '东帝汶', '1', 320, 'system', now()),
    (200222, 'TM', 'Turkmenistan', '土库曼斯坦', '1', 321, 'system', now()),
    (200223, 'TN', 'Tunisia', '突尼斯', '1', 322, 'system', now()),
    (200224, 'TO', 'Tonga', '汤加', '1', 323, 'system', now()),
    (200225, 'TR', 'Türkiye', '土耳其', '1', 324, 'system', now()),
    (200226, 'TT', 'Trinidad & Tobago', '特立尼达和多巴哥', '1', 325, 'system', now()),
    (200227, 'TV', 'Tuvalu', '图瓦卢', '1', 326, 'system', now()),
    (200228, 'TW', 'Taiwan', '台湾', '1', 327, 'system', now()),
    (200229, 'TZ', 'Tanzania', '坦桑尼亚', '1', 328, 'system', now()),
    (200230, 'UA', 'Ukraine', '乌克兰', '1', 329, 'system', now()),
    (200231, 'UG', 'Uganda', '乌干达', '1', 330, 'system', now()),
    (200232, 'UM', 'U.S. Outlying Islands', '美国本土外小岛屿', '1', 331, 'system', now()),
    (200233, 'US', 'United States', '美国', '1', 1, 'system', now()),
    (200234, 'UY', 'Uruguay', '乌拉圭', '1', 333, 'system', now()),
    (200235, 'UZ', 'Uzbekistan', '乌兹别克斯坦', '1', 334, 'system', now()),
    (200236, 'VA', 'Vatican City', '梵蒂冈', '1', 335, 'system', now()),
    (200237, 'VC', 'St. Vincent & Grenadines', '圣文森特和格林纳丁斯', '1', 336, 'system', now()),
    (200238, 'VE', 'Venezuela', '委内瑞拉', '1', 337, 'system', now()),
    (200239, 'VG', 'British Virgin Islands', '英属维尔京群岛', '1', 338, 'system', now()),
    (200240, 'VI', 'U.S. Virgin Islands', '美属维尔京群岛', '1', 339, 'system', now()),
    (200241, 'VN', 'Vietnam', '越南', '1', 340, 'system', now()),
    (200242, 'VU', 'Vanuatu', '瓦努阿图', '1', 341, 'system', now()),
    (200243, 'WF', 'Wallis & Futuna', '瓦利斯和富图纳', '1', 342, 'system', now()),
    (200244, 'WS', 'Samoa', '萨摩亚', '1', 343, 'system', now()),
    (200245, 'YE', 'Yemen', '也门', '1', 344, 'system', now()),
    (200246, 'YT', 'Mayotte', '马约特', '1', 345, 'system', now()),
    (200247, 'ZA', 'South Africa', '南非', '1', 346, 'system', now()),
    (200248, 'ZM', 'Zambia', '赞比亚', '1', 347, 'system', now()),
    (200249, 'ZW', 'Zimbabwe', '津巴布韦', '1', 348, 'system', now())
ON CONFLICT (country_code) DO UPDATE
SET name_en = EXCLUDED.name_en,
    name_zh = EXCLUDED.name_zh,
    status = EXCLUDED.status,
    sort = EXCLUDED.sort,
    update_by = 'system',
    update_time = now();

INSERT INTO sys_currency (currency_id, currency_code, name_en, name_zh, symbol, decimal_places, status, sort, create_by, create_time)
VALUES
    (210001, 'AED', 'United Arab Emirates Dirham', '阿联酋迪拉姆', 'AED', 2, '1', 100, 'system', now()),
    (210002, 'AFN', 'Afghan Afghani', '阿富汗尼', '؋', 0, '1', 101, 'system', now()),
    (210003, 'ALL', 'Albanian Lek', '阿尔巴尼亚列克', 'ALL', 0, '1', 102, 'system', now()),
    (210004, 'AMD', 'Armenian Dram', '亚美尼亚德拉姆', '֏', 2, '1', 103, 'system', now()),
    (210005, 'ANG', 'Netherlands Antillean Guilder', '荷属安的列斯盾', 'ANG', 2, '1', 104, 'system', now()),
    (210006, 'AOA', 'Angolan Kwanza', '安哥拉宽扎', 'Kz', 2, '1', 105, 'system', now()),
    (210007, 'ARS', 'Argentine Peso', '阿根廷比索', '$', 2, '1', 106, 'system', now()),
    (210008, 'AUD', 'Australian Dollar', '澳大利亚元', '$', 2, '1', 7, 'system', now()),
    (210009, 'AWG', 'Aruban Florin', '阿鲁巴弗罗林', 'AWG', 2, '1', 108, 'system', now()),
    (210010, 'AZN', 'Azerbaijani Manat', '阿塞拜疆马纳特', '₼', 2, '1', 109, 'system', now()),
    (210011, 'BAM', 'Bosnia-Herzegovina Convertible Mark', '波斯尼亚-黑塞哥维那可兑换马克', 'KM', 2, '1', 110, 'system', now()),
    (210012, 'BBD', 'Barbadian Dollar', '巴巴多斯元', '$', 2, '1', 111, 'system', now()),
    (210013, 'BDT', 'Bangladeshi Taka', '孟加拉塔卡', '৳', 2, '1', 112, 'system', now()),
    (210014, 'BGN', 'Bulgarian Lev', '保加利亚列弗', 'BGN', 2, '1', 113, 'system', now()),
    (210015, 'BHD', 'Bahraini Dinar', '巴林第纳尔', 'BHD', 3, '1', 114, 'system', now()),
    (210016, 'BIF', 'Burundian Franc', '布隆迪法郎', 'BIF', 0, '1', 115, 'system', now()),
    (210017, 'BMD', 'Bermudan Dollar', '百慕大元', '$', 2, '1', 116, 'system', now()),
    (210018, 'BND', 'Brunei Dollar', '文莱元', '$', 2, '1', 117, 'system', now()),
    (210019, 'BOB', 'Bolivian Boliviano', '玻利维亚诺', 'Bs', 2, '1', 118, 'system', now()),
    (210020, 'BRL', 'Brazilian Real', '巴西雷亚尔', 'R$', 2, '1', 119, 'system', now()),
    (210021, 'BSD', 'Bahamian Dollar', '巴哈马元', '$', 2, '1', 120, 'system', now()),
    (210022, 'BTN', 'Bhutanese Ngultrum', '不丹努尔特鲁姆', 'BTN', 2, '1', 121, 'system', now()),
    (210023, 'BWP', 'Botswanan Pula', '博茨瓦纳普拉', 'P', 2, '1', 122, 'system', now()),
    (210024, 'BYN', 'Belarusian Ruble', '白俄罗斯卢布', 'BYN', 2, '1', 123, 'system', now()),
    (210025, 'BZD', 'Belize Dollar', '伯利兹元', '$', 2, '1', 124, 'system', now()),
    (210026, 'CAD', 'Canadian Dollar', '加拿大元', '$', 2, '1', 3, 'system', now()),
    (210027, 'CDF', 'Congolese Franc', '刚果法郎', 'CDF', 2, '1', 126, 'system', now()),
    (210028, 'CHF', 'Swiss Franc', '瑞士法郎', 'CHF', 2, '1', 127, 'system', now()),
    (210029, 'CLP', 'Chilean Peso', '智利比索', '$', 0, '1', 128, 'system', now()),
    (210030, 'CNY', 'Chinese Yuan', '人民币', '¥', 2, '1', 2, 'system', now()),
    (210031, 'COP', 'Colombian Peso', '哥伦比亚比索', '$', 2, '1', 130, 'system', now()),
    (210032, 'CRC', 'Costa Rican Colón', '哥斯达黎加科朗', '₡', 2, '1', 131, 'system', now()),
    (210033, 'CUC', 'Cuban Convertible Peso', '古巴可兑换比索', '$', 2, '1', 132, 'system', now()),
    (210034, 'CUP', 'Cuban Peso', '古巴比索', '$', 2, '1', 133, 'system', now()),
    (210035, 'CVE', 'Cape Verdean Escudo', '佛得角埃斯库多', 'CVE', 2, '1', 134, 'system', now()),
    (210036, 'CZK', 'Czech Koruna', '捷克克朗', 'Kč', 2, '1', 135, 'system', now()),
    (210037, 'DJF', 'Djiboutian Franc', '吉布提法郎', 'DJF', 0, '1', 136, 'system', now()),
    (210038, 'DKK', 'Danish Krone', '丹麦克朗', 'kr', 2, '1', 137, 'system', now()),
    (210039, 'DOP', 'Dominican Peso', '多米尼加比索', '$', 2, '1', 138, 'system', now()),
    (210040, 'DZD', 'Algerian Dinar', '阿尔及利亚第纳尔', 'DZD', 2, '1', 139, 'system', now()),
    (210041, 'EGP', 'Egyptian Pound', '埃及镑', 'E£', 2, '1', 140, 'system', now()),
    (210042, 'ERN', 'Eritrean Nakfa', '厄立特里亚纳克法', 'ERN', 2, '1', 141, 'system', now()),
    (210043, 'ETB', 'Ethiopian Birr', '埃塞俄比亚比尔', 'ETB', 2, '1', 142, 'system', now()),
    (210044, 'EUR', 'Euro', '欧元', '€', 2, '1', 5, 'system', now()),
    (210045, 'FJD', 'Fijian Dollar', '斐济元', '$', 2, '1', 144, 'system', now()),
    (210046, 'FKP', 'Falkland Islands Pound', '福克兰群岛镑', '£', 2, '1', 145, 'system', now()),
    (210047, 'GBP', 'British Pound', '英镑', '£', 2, '1', 6, 'system', now()),
    (210048, 'GEL', 'Georgian Lari', '格鲁吉亚拉里', '₾', 2, '1', 147, 'system', now()),
    (210049, 'GHS', 'Ghanaian Cedi', '加纳塞地', 'GH₵', 2, '1', 148, 'system', now()),
    (210050, 'GIP', 'Gibraltar Pound', '直布罗陀镑', '£', 2, '1', 149, 'system', now()),
    (210051, 'GMD', 'Gambian Dalasi', '冈比亚达拉西', 'GMD', 2, '1', 150, 'system', now()),
    (210052, 'GNF', 'Guinean Franc', '几内亚法郎', 'FG', 0, '1', 151, 'system', now()),
    (210053, 'GTQ', 'Guatemalan Quetzal', '危地马拉格查尔', 'Q', 2, '1', 152, 'system', now()),
    (210054, 'GYD', 'Guyanaese Dollar', '圭亚那元', '$', 2, '1', 153, 'system', now()),
    (210055, 'HKD', 'Hong Kong Dollar', '港元', '$', 2, '1', 154, 'system', now()),
    (210056, 'HNL', 'Honduran Lempira', '洪都拉斯伦皮拉', 'L', 2, '1', 155, 'system', now()),
    (210057, 'HRK', 'Croatian Kuna', '克罗地亚库纳', 'kn', 2, '1', 156, 'system', now()),
    (210058, 'HTG', 'Haitian Gourde', '海地古德', 'HTG', 2, '1', 157, 'system', now()),
    (210059, 'HUF', 'Hungarian Forint', '匈牙利福林', 'Ft', 2, '1', 158, 'system', now()),
    (210060, 'IDR', 'Indonesian Rupiah', '印度尼西亚卢比', 'Rp', 2, '1', 159, 'system', now()),
    (210061, 'ILS', 'Israeli New Shekel', '以色列新谢克尔', '₪', 2, '1', 160, 'system', now()),
    (210062, 'INR', 'Indian Rupee', '印度卢比', '₹', 2, '1', 161, 'system', now()),
    (210063, 'IQD', 'Iraqi Dinar', '伊拉克第纳尔', 'IQD', 0, '1', 162, 'system', now()),
    (210064, 'IRR', 'Iranian Rial', '伊朗里亚尔', 'IRR', 0, '1', 163, 'system', now()),
    (210065, 'ISK', 'Icelandic Króna', '冰岛克朗', 'kr', 0, '1', 164, 'system', now()),
    (210066, 'JMD', 'Jamaican Dollar', '牙买加元', '$', 2, '1', 165, 'system', now()),
    (210067, 'JOD', 'Jordanian Dinar', '约旦第纳尔', 'JOD', 3, '1', 166, 'system', now()),
    (210068, 'JPY', 'Japanese Yen', '日元', '¥', 0, '1', 167, 'system', now()),
    (210069, 'KES', 'Kenyan Shilling', '肯尼亚先令', 'KES', 2, '1', 168, 'system', now()),
    (210070, 'KGS', 'Kyrgystani Som', '吉尔吉斯斯坦索姆', '⃀', 2, '1', 169, 'system', now()),
    (210071, 'KHR', 'Cambodian Riel', '柬埔寨瑞尔', '៛', 2, '1', 170, 'system', now()),
    (210072, 'KMF', 'Comorian Franc', '科摩罗法郎', 'CF', 0, '1', 171, 'system', now()),
    (210073, 'KPW', 'North Korean Won', '朝鲜元', '₩', 0, '1', 172, 'system', now()),
    (210074, 'KRW', 'South Korean Won', '韩元', '₩', 0, '1', 173, 'system', now()),
    (210075, 'KWD', 'Kuwaiti Dinar', '科威特第纳尔', 'KWD', 3, '1', 174, 'system', now()),
    (210076, 'KYD', 'Cayman Islands Dollar', '开曼元', '$', 2, '1', 175, 'system', now()),
    (210077, 'KZT', 'Kazakhstani Tenge', '哈萨克斯坦坚戈', '₸', 2, '1', 176, 'system', now()),
    (210078, 'LAK', 'Laotian Kip', '老挝基普', '₭', 0, '1', 177, 'system', now()),
    (210079, 'LBP', 'Lebanese Pound', '黎巴嫩镑', 'L£', 0, '1', 178, 'system', now()),
    (210080, 'LKR', 'Sri Lankan Rupee', '斯里兰卡卢比', 'Rs', 2, '1', 179, 'system', now()),
    (210081, 'LRD', 'Liberian Dollar', '利比里亚元', '$', 2, '1', 180, 'system', now()),
    (210082, 'LSL', 'Lesotho Loti', '莱索托洛蒂', 'LSL', 2, '1', 181, 'system', now()),
    (210083, 'LYD', 'Libyan Dinar', '利比亚第纳尔', 'LYD', 3, '1', 182, 'system', now()),
    (210084, 'MAD', 'Moroccan Dirham', '摩洛哥迪拉姆', 'MAD', 2, '1', 183, 'system', now()),
    (210085, 'MDL', 'Moldovan Leu', '摩尔多瓦列伊', 'MDL', 2, '1', 184, 'system', now()),
    (210086, 'MGA', 'Malagasy Ariary', '马达加斯加阿里亚里', 'Ar', 0, '1', 185, 'system', now()),
    (210087, 'MKD', 'Macedonian Denar', '马其顿第纳尔', 'MKD', 2, '1', 186, 'system', now()),
    (210088, 'MMK', 'Myanmar Kyat', '缅甸元', 'K', 0, '1', 187, 'system', now()),
    (210089, 'MNT', 'Mongolian Tugrik', '蒙古图格里克', '₮', 2, '1', 188, 'system', now()),
    (210090, 'MOP', 'Macanese Pataca', '澳门币', 'MOP', 2, '1', 189, 'system', now()),
    (210091, 'MRU', 'Mauritanian Ouguiya', '毛里塔尼亚乌吉亚', 'MRU', 2, '1', 190, 'system', now()),
    (210092, 'MUR', 'Mauritian Rupee', '毛里求斯卢比', 'Rs', 2, '1', 191, 'system', now()),
    (210093, 'MVR', 'Maldivian Rufiyaa', '马尔代夫卢菲亚', 'MVR', 2, '1', 192, 'system', now()),
    (210094, 'MWK', 'Malawian Kwacha', '马拉维克瓦查', 'MWK', 2, '1', 193, 'system', now()),
    (210095, 'MXN', 'Mexican Peso', '墨西哥比索', '$', 2, '1', 4, 'system', now()),
    (210096, 'MYR', 'Malaysian Ringgit', '马来西亚林吉特', 'RM', 2, '1', 195, 'system', now()),
    (210097, 'MZN', 'Mozambican Metical', '莫桑比克美提卡', 'MZN', 2, '1', 196, 'system', now()),
    (210098, 'NAD', 'Namibian Dollar', '纳米比亚元', '$', 2, '1', 197, 'system', now()),
    (210099, 'NGN', 'Nigerian Naira', '尼日利亚奈拉', '₦', 2, '1', 198, 'system', now()),
    (210100, 'NIO', 'Nicaraguan Córdoba', '尼加拉瓜科多巴', 'C$', 2, '1', 199, 'system', now()),
    (210101, 'NOK', 'Norwegian Krone', '挪威克朗', 'kr', 2, '1', 200, 'system', now()),
    (210102, 'NPR', 'Nepalese Rupee', '尼泊尔卢比', 'Rs', 2, '1', 201, 'system', now()),
    (210103, 'NZD', 'New Zealand Dollar', '新西兰元', '$', 2, '1', 202, 'system', now()),
    (210104, 'OMR', 'Omani Rial', '阿曼里亚尔', 'OMR', 3, '1', 203, 'system', now()),
    (210105, 'PAB', 'Panamanian Balboa', '巴拿马巴波亚', 'PAB', 2, '1', 204, 'system', now()),
    (210106, 'PEN', 'Peruvian Sol', '秘鲁索尔', 'PEN', 2, '1', 205, 'system', now()),
    (210107, 'PGK', 'Papua New Guinean Kina', '巴布亚新几内亚基那', 'PGK', 2, '1', 206, 'system', now()),
    (210108, 'PHP', 'Philippine Peso', '菲律宾比索', '₱', 2, '1', 207, 'system', now()),
    (210109, 'PKR', 'Pakistani Rupee', '巴基斯坦卢比', 'Rs', 2, '1', 208, 'system', now()),
    (210110, 'PLN', 'Polish Zloty', '波兰兹罗提', 'zł', 2, '1', 209, 'system', now()),
    (210111, 'PYG', 'Paraguayan Guarani', '巴拉圭瓜拉尼', '₲', 0, '1', 210, 'system', now()),
    (210112, 'QAR', 'Qatari Riyal', '卡塔尔里亚尔', 'QAR', 2, '1', 211, 'system', now()),
    (210113, 'RON', 'Romanian Leu', '罗马尼亚列伊', 'lei', 2, '1', 212, 'system', now()),
    (210114, 'RSD', 'Serbian Dinar', '塞尔维亚第纳尔', 'RSD', 0, '1', 213, 'system', now()),
    (210115, 'RUB', 'Russian Ruble', '俄罗斯卢布', '₽', 2, '1', 214, 'system', now()),
    (210116, 'RWF', 'Rwandan Franc', '卢旺达法郎', 'RF', 0, '1', 215, 'system', now()),
    (210117, 'SAR', 'Saudi Riyal', '沙特里亚尔', 'SAR', 2, '1', 216, 'system', now()),
    (210118, 'SBD', 'Solomon Islands Dollar', '所罗门群岛元', '$', 2, '1', 217, 'system', now()),
    (210119, 'SCR', 'Seychellois Rupee', '塞舌尔卢比', 'SCR', 2, '1', 218, 'system', now()),
    (210120, 'SDG', 'Sudanese Pound', '苏丹镑', 'SDG', 2, '1', 219, 'system', now()),
    (210121, 'SEK', 'Swedish Krona', '瑞典克朗', 'kr', 2, '1', 220, 'system', now()),
    (210122, 'SGD', 'Singapore Dollar', '新加坡元', '$', 2, '1', 221, 'system', now()),
    (210123, 'SHP', 'St. Helena Pound', '圣赫勒拿群岛磅', '£', 2, '1', 222, 'system', now()),
    (210124, 'SLE', 'Sierra Leonean Leone', '塞拉利昂新利昂', 'SLE', 2, '1', 223, 'system', now()),
    (210125, 'SLL', 'Sierra Leonean Leone (1964—2022)', '塞拉利昂利昂', 'SLL', 0, '1', 224, 'system', now()),
    (210126, 'SOS', 'Somali Shilling', '索马里先令', 'SOS', 0, '1', 225, 'system', now()),
    (210127, 'SRD', 'Surinamese Dollar', '苏里南元', '$', 2, '1', 226, 'system', now()),
    (210128, 'SSP', 'South Sudanese Pound', '南苏丹镑', '£', 2, '1', 227, 'system', now()),
    (210129, 'STN', 'São Tomé & Príncipe Dobra', '圣多美和普林西比多布拉', 'Db', 2, '1', 228, 'system', now()),
    (210130, 'SVC', 'Salvadoran Colón', '萨尔瓦多科朗', 'SVC', 2, '1', 229, 'system', now()),
    (210131, 'SYP', 'Syrian Pound', '叙利亚镑', '£', 0, '1', 230, 'system', now()),
    (210132, 'SZL', 'Swazi Lilangeni', '斯威士兰里兰吉尼', 'SZL', 2, '1', 231, 'system', now()),
    (210133, 'THB', 'Thai Baht', '泰铢', '฿', 2, '1', 232, 'system', now()),
    (210134, 'TJS', 'Tajikistani Somoni', '塔吉克斯坦索莫尼', 'TJS', 2, '1', 233, 'system', now()),
    (210135, 'TMT', 'Turkmenistani Manat', '土库曼斯坦马纳特', 'TMT', 2, '1', 234, 'system', now()),
    (210136, 'TND', 'Tunisian Dinar', '突尼斯第纳尔', 'TND', 3, '1', 235, 'system', now()),
    (210137, 'TOP', 'Tongan Paʻanga', '汤加潘加', 'T$', 2, '1', 236, 'system', now()),
    (210138, 'TRY', 'Turkish Lira', '土耳其里拉', '₺', 2, '1', 237, 'system', now()),
    (210139, 'TTD', 'Trinidad & Tobago Dollar', '特立尼达和多巴哥元', '$', 2, '1', 238, 'system', now()),
    (210140, 'TWD', 'New Taiwan Dollar', '新台币', '$', 2, '1', 239, 'system', now()),
    (210141, 'TZS', 'Tanzanian Shilling', '坦桑尼亚先令', 'TZS', 2, '1', 240, 'system', now()),
    (210142, 'UAH', 'Ukrainian Hryvnia', '乌克兰格里夫纳', '₴', 2, '1', 241, 'system', now()),
    (210143, 'UGX', 'Ugandan Shilling', '乌干达先令', 'UGX', 0, '1', 242, 'system', now()),
    (210144, 'USD', 'US Dollar', '美元', '$', 2, '1', 1, 'system', now()),
    (210145, 'UYU', 'Uruguayan Peso', '乌拉圭比索', '$', 2, '1', 244, 'system', now()),
    (210146, 'UZS', 'Uzbekistani Som', '乌兹别克斯坦苏姆', 'UZS', 2, '1', 245, 'system', now()),
    (210147, 'VES', 'Venezuelan Bolívar', '委内瑞拉玻利瓦尔', 'VES', 2, '1', 246, 'system', now()),
    (210148, 'VND', 'Vietnamese Dong', '越南盾', '₫', 0, '1', 247, 'system', now()),
    (210149, 'VUV', 'Vanuatu Vatu', '瓦努阿图瓦图', 'VUV', 0, '1', 248, 'system', now()),
    (210150, 'WST', 'Samoan Tala', '萨摩亚塔拉', 'WST', 2, '1', 249, 'system', now()),
    (210151, 'XAF', 'Central African CFA Franc', '中非法郎', 'FCFA', 0, '1', 250, 'system', now()),
    (210152, 'XCD', 'East Caribbean Dollar', '东加勒比元', '$', 2, '1', 251, 'system', now()),
    (210153, 'XCG', 'Caribbean guilder', 'XCG', 'Cg.', 2, '1', 252, 'system', now()),
    (210154, 'XDR', 'Special Drawing Rights', '特别提款权', 'XDR', 2, '1', 253, 'system', now()),
    (210155, 'XOF', 'West African CFA Franc', '西非法郎', 'F CFA', 0, '1', 254, 'system', now()),
    (210156, 'XPF', 'CFP Franc', '太平洋法郎', 'CFPF', 0, '1', 255, 'system', now()),
    (210157, 'XSU', 'Sucre', '苏克雷', 'XSU', 2, '1', 256, 'system', now()),
    (210158, 'YER', 'Yemeni Rial', '也门里亚尔', 'YER', 0, '1', 257, 'system', now()),
    (210159, 'ZAR', 'South African Rand', '南非兰特', 'R', 2, '1', 258, 'system', now()),
    (210160, 'ZMW', 'Zambian Kwacha', '赞比亚克瓦查', 'ZK', 2, '1', 259, 'system', now()),
    (210161, 'ZWG', 'Zimbabwean Gold', 'ZWG', 'ZWG', 2, '1', 260, 'system', now()),
    (210162, 'ZWL', 'Zimbabwean Dollar (2009–2024)', '津巴布韦元 (2009)', 'ZWL', 2, '1', 261, 'system', now())
ON CONFLICT (currency_code) DO UPDATE
SET name_en = EXCLUDED.name_en,
    name_zh = EXCLUDED.name_zh,
    symbol = EXCLUDED.symbol,
    decimal_places = EXCLUDED.decimal_places,
    status = EXCLUDED.status,
    sort = EXCLUDED.sort,
    update_by = 'system',
    update_time = now();

INSERT INTO sys_language (language_id, language_code, name_en, name_native, status, sort, create_by, create_time)
VALUES
    (220001, 'en_US', 'English (United States)', 'English', '1', 1, 'system', now()),
    (220002, 'zh_CN', 'Chinese (Simplified)', '简体中文', '1', 2, 'system', now())
ON CONFLICT (language_code) DO UPDATE
SET name_en = EXCLUDED.name_en,
    name_native = EXCLUDED.name_native,
    status = EXCLUDED.status,
    sort = EXCLUDED.sort,
    update_by = 'system',
    update_time = now();
