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
CREATE UNIQUE INDEX IF NOT EXISTS uk_merchant_profile_tenant ON merchant_profile (tenant_id);
CREATE INDEX IF NOT EXISTS idx_merchant_profile_primary_email ON merchant_profile (primary_email);
CREATE INDEX IF NOT EXISTS idx_merchant_profile_status ON merchant_profile (status, audit_status);

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
    i18n_key varchar(128),
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
CREATE INDEX IF NOT EXISTS idx_sys_dict_data_i18n_key ON sys_dict_data (i18n_key) WHERE i18n_key IS NOT NULL;

CREATE TABLE IF NOT EXISTS sys_i18n_message (
    message_id bigint PRIMARY KEY,
    message_key varchar(128) NOT NULL,
    locale varchar(20) NOT NULL,
    message_value varchar(500) NOT NULL,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500),
    create_by_id bigint
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_i18n_message ON sys_i18n_message (message_key, locale);
CREATE INDEX IF NOT EXISTS idx_sys_i18n_message_locale ON sys_i18n_message (locale);

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
COMMENT ON COLUMN sys_dict_data.i18n_key IS '国际化消息键';
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

COMMENT ON TABLE sys_i18n_message IS '国际化消息表';
COMMENT ON COLUMN sys_i18n_message.message_id IS '消息ID';
COMMENT ON COLUMN sys_i18n_message.message_key IS '消息键';
COMMENT ON COLUMN sys_i18n_message.locale IS '语言标识，例如 zh_CN、en_US';
COMMENT ON COLUMN sys_i18n_message.message_value IS '消息内容';
COMMENT ON COLUMN sys_i18n_message.create_by IS '创建者';
COMMENT ON COLUMN sys_i18n_message.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN sys_i18n_message.update_by IS '更新者';
COMMENT ON COLUMN sys_i18n_message.update_time IS '更新时间，UTC timestamptz';
COMMENT ON COLUMN sys_i18n_message.remark IS '备注';
COMMENT ON COLUMN sys_i18n_message.create_by_id IS '创建者ID';

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

INSERT INTO sys_user (user_id, tenant_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, password, status, del_flag, create_by, create_time, remark)
VALUES (1, 1, 100, 'admin', 'System Admin', 'sys_user', 'admin@example.com', '18888888888', '0',
        '$2a$10$gkt8GIcTlW28k3a.osOvQus81YBcY9JHr7zLqaaknk4O2x9xX/JMm', '1', '0', 'system', now(), 'Platform administrator')
ON CONFLICT (user_id) DO NOTHING;
UPDATE sys_user SET tenant_id = 1 WHERE user_id = 1;

INSERT INTO sys_role (role_id, tenant_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES
    (1, 1, 'Super Admin', 'admin', 1, '1', true, true, '1', '0', 'system', now(), 'Platform super admin'),
    (2, 1, 'Merchant Admin', 'merchant_admin', 2, '1', true, true, '1', '0', 'system', now(), 'Default merchant admin role')
ON CONFLICT (role_id) DO NOTHING;
UPDATE sys_role SET tenant_id = 1 WHERE role_id IN (1, 2);

INSERT INTO sys_user_role (user_id, role_id, tenant_id)
VALUES (1, 1, 1)
ON CONFLICT DO NOTHING;
UPDATE sys_user_role SET tenant_id = 1 WHERE user_id = 1 AND role_id = 1;

INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES
    (1, 'Captcha enabled', 'sys.account.captchaEnabled', 'true', 'Y', 'system', now(), 'Enable captcha'),
    (2, 'Registration enabled', 'sys.account.registerUser', 'false', 'Y', 'system', now(), 'Legacy direct user registration disabled'),
    (3, 'Initial password', 'sys.user.initPassword', '123456', 'Y', 'system', now(), 'Default password for manually created users')
ON CONFLICT (config_id) DO NOTHING;

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES
    (1, 'Normal/Disabled', 'sys_normal_disable', '1', 'system', now(), 'Base status'),
    (2, 'Tenant application status', 'tenant_apply_status', '1', 'system', now(), 'Merchant application status'),
    (3, 'Show/Hide', 'sys_show_hide', '1', 'system', now(), 'Menu visibility')
ON CONFLICT (dict_id) DO NOTHING;

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, i18n_key, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES
    (1, 1, 'Normal', 'sys.dict.sys_normal_disable.normal', '1', 'sys_normal_disable', 'primary', 'Y', '1', 'system', now()),
    (2, 2, 'Disabled', 'sys.dict.sys_normal_disable.disabled', '0', 'sys_normal_disable', 'danger', 'N', '1', 'system', now()),
    (3, 1, 'Pending', 'sys.dict.tenant_apply_status.pending', 'PENDING', 'tenant_apply_status', 'warning', 'Y', '1', 'system', now()),
    (4, 2, 'Approved', 'sys.dict.tenant_apply_status.approved', 'APPROVED', 'tenant_apply_status', 'success', 'N', '1', 'system', now()),
    (5, 3, 'Rejected', 'sys.dict.tenant_apply_status.rejected', 'REJECTED', 'tenant_apply_status', 'danger', 'N', '1', 'system', now()),
    (6, 1, 'Show', 'sys.dict.sys_show_hide.show', '1', 'sys_show_hide', 'primary', 'Y', '1', 'system', now()),
    (7, 2, 'Hide', 'sys.dict.sys_show_hide.hide', '0', 'sys_show_hide', 'danger', 'N', '1', 'system', now())
ON CONFLICT (dict_code) DO NOTHING;

INSERT INTO sys_i18n_message (message_id, message_key, locale, message_value, create_by, create_time, remark)
VALUES
    (9001, 'sys.menu.system', 'en_US', 'System', 'system', now(), 'Base menu'),
    (9002, 'sys.menu.system', 'zh_CN', '系统管理', 'system', now(), '基础菜单'),
    (9003, 'sys.menu.system.user', 'en_US', 'User', 'system', now(), 'Base menu'),
    (9004, 'sys.menu.system.user', 'zh_CN', '用户管理', 'system', now(), '基础菜单'),
    (9005, 'sys.menu.system.role', 'en_US', 'Role', 'system', now(), 'Base menu'),
    (9006, 'sys.menu.system.role', 'zh_CN', '角色管理', 'system', now(), '基础菜单'),
    (9007, 'sys.menu.system.menu', 'en_US', 'Menu', 'system', now(), 'Base menu'),
    (9008, 'sys.menu.system.menu', 'zh_CN', '菜单管理', 'system', now(), '基础菜单'),
    (9009, 'sys.menu.system.dept', 'en_US', 'Dept', 'system', now(), 'Base menu'),
    (9010, 'sys.menu.system.dept', 'zh_CN', '部门管理', 'system', now(), '基础菜单'),
    (9011, 'sys.menu.system.post', 'en_US', 'Post', 'system', now(), 'Base menu'),
    (9012, 'sys.menu.system.post', 'zh_CN', '岗位管理', 'system', now(), '基础菜单'),
    (9013, 'sys.menu.system.dict', 'en_US', 'Dict', 'system', now(), 'Base menu'),
    (9014, 'sys.menu.system.dict', 'zh_CN', '字典管理', 'system', now(), '基础菜单'),
    (9015, 'sys.menu.system.config', 'en_US', 'Config', 'system', now(), 'Base menu'),
    (9016, 'sys.menu.system.config', 'zh_CN', '参数配置', 'system', now(), '基础菜单'),
    (9017, 'sys.menu.system.merchantAudit', 'en_US', 'Merchant Audit', 'system', now(), 'Base menu'),
    (9018, 'sys.menu.system.merchantAudit', 'zh_CN', '商家审核', 'system', now(), '基础菜单'),
    (9019, 'sys.menu.system.merchantAudit.query', 'en_US', 'Tenant Application Query', 'system', now(), 'Base permission'),
    (9020, 'sys.menu.system.merchantAudit.query', 'zh_CN', '商家申请查询', 'system', now(), '基础权限'),
    (9021, 'sys.menu.system.merchantAudit.audit', 'en_US', 'Tenant Application Audit', 'system', now(), 'Base permission'),
    (9022, 'sys.menu.system.merchantAudit.audit', 'zh_CN', '商家申请审核', 'system', now(), '基础权限'),
    (9037, 'sys.menu.system.merchantProfile', 'en_US', 'Merchant Profile', 'system', now(), 'Base menu'),
    (9038, 'sys.menu.system.merchantProfile', 'zh_CN', '商家资料', 'system', now(), '基础菜单'),
    (9039, 'sys.menu.system.merchantProfile.query', 'en_US', 'Merchant Profile Query', 'system', now(), 'Base permission'),
    (9040, 'sys.menu.system.merchantProfile.query', 'zh_CN', '商家资料查询', 'system', now(), '基础权限'),
    (19401, 'sys.menu.system.merchantProfile.edit', 'en_US', 'Merchant Profile Edit', 'system', now(), 'Base permission'),
    (19402, 'sys.menu.system.merchantProfile.edit', 'zh_CN', '商家资料维护', 'system', now(), '基础权限'),
    (9041, 'menu.merchant', 'en_US', 'Merchant', 'system', now(), 'Merchant menu'),
    (9042, 'menu.merchant', 'zh_CN', '商家中心', 'system', now(), '商家菜单'),
    (9043, 'menu.merchant.profile', 'en_US', 'Merchant Profile', 'system', now(), 'Merchant menu'),
    (9044, 'menu.merchant.profile', 'zh_CN', '商家资料', 'system', now(), '商家菜单'),
    (9045, 'menu.merchant.profile.query', 'en_US', 'Merchant Profile Query', 'system', now(), 'Merchant permission'),
    (9046, 'menu.merchant.profile.query', 'zh_CN', '商家资料查询', 'system', now(), '商家权限'),
    (9047, 'menu.merchant.profile.edit', 'en_US', 'Merchant Profile Edit', 'system', now(), 'Merchant permission'),
    (9048, 'menu.merchant.profile.edit', 'zh_CN', '商家资料编辑', 'system', now(), '商家权限'),
    (9023, 'sys.dict.sys_normal_disable.normal', 'en_US', 'Normal', 'system', now(), 'Base dict'),
    (9024, 'sys.dict.sys_normal_disable.normal', 'zh_CN', '正常', 'system', now(), '基础字典'),
    (9025, 'sys.dict.sys_normal_disable.disabled', 'en_US', 'Disabled', 'system', now(), 'Base dict'),
    (9026, 'sys.dict.sys_normal_disable.disabled', 'zh_CN', '停用', 'system', now(), '基础字典'),
    (9027, 'sys.dict.tenant_apply_status.pending', 'en_US', 'Pending', 'system', now(), 'Base dict'),
    (9028, 'sys.dict.tenant_apply_status.pending', 'zh_CN', '待审核', 'system', now(), '基础字典'),
    (9029, 'sys.dict.tenant_apply_status.approved', 'en_US', 'Approved', 'system', now(), 'Base dict'),
    (9030, 'sys.dict.tenant_apply_status.approved', 'zh_CN', '已通过', 'system', now(), '基础字典'),
    (9031, 'sys.dict.tenant_apply_status.rejected', 'en_US', 'Rejected', 'system', now(), 'Base dict'),
    (9032, 'sys.dict.tenant_apply_status.rejected', 'zh_CN', '已拒绝', 'system', now(), '基础字典'),
    (9033, 'sys.dict.sys_show_hide.show', 'en_US', 'Show', 'system', now(), 'Base dict'),
    (9034, 'sys.dict.sys_show_hide.show', 'zh_CN', '显示', 'system', now(), '基础字典'),
    (9035, 'sys.dict.sys_show_hide.hide', 'en_US', 'Hide', 'system', now(), 'Base dict'),
    (9036, 'sys.dict.sys_show_hide.hide', 'zh_CN', '隐藏', 'system', now(), '基础字典')
ON CONFLICT (message_id) DO NOTHING;

UPDATE sys_dict_data SET i18n_key = 'sys.dict.sys_normal_disable.normal' WHERE dict_type = 'sys_normal_disable' AND dict_value = '1' AND i18n_key IS NULL;
UPDATE sys_dict_data SET i18n_key = 'sys.dict.sys_normal_disable.disabled' WHERE dict_type = 'sys_normal_disable' AND dict_value = '0' AND i18n_key IS NULL;
UPDATE sys_dict_data SET i18n_key = 'sys.dict.tenant_apply_status.pending' WHERE dict_type = 'tenant_apply_status' AND dict_value = 'PENDING' AND i18n_key IS NULL;
UPDATE sys_dict_data SET i18n_key = 'sys.dict.tenant_apply_status.approved' WHERE dict_type = 'tenant_apply_status' AND dict_value = 'APPROVED' AND i18n_key IS NULL;
UPDATE sys_dict_data SET i18n_key = 'sys.dict.tenant_apply_status.rejected' WHERE dict_type = 'tenant_apply_status' AND dict_value = 'REJECTED' AND i18n_key IS NULL;
UPDATE sys_dict_data SET i18n_key = 'sys.dict.sys_show_hide.show' WHERE dict_type = 'sys_show_hide' AND dict_value = '1' AND i18n_key IS NULL;
UPDATE sys_dict_data SET i18n_key = 'sys.dict.sys_show_hide.hide' WHERE dict_type = 'sys_show_hide' AND dict_value = '0' AND i18n_key IS NULL;

INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (1, 1, 'System', 'sys.menu.system', 0, 100, 'system', NULL, '1', '0', 'M', '1', '1', NULL, 'system', 'system', now(), 'System menu'),
    (100, 1, 'User', 'sys.menu.system.user', 1, 1, 'user', 'system/user/index', '1', '0', 'C', '1', '1', 'system:user:list', 'user', 'system', now(), 'User management'),
    (101, 1, 'Role', 'sys.menu.system.role', 1, 2, 'role', 'system/role/index', '1', '0', 'C', '1', '1', 'system:role:list', 'peoples', 'system', now(), 'Role management'),
    (102, 1, 'Menu', 'sys.menu.system.menu', 1, 3, 'menu', 'system/menu/index', '1', '0', 'C', '1', '1', 'system:menu:list', 'tree-table', 'system', now(), 'Menu management'),
    (103, 1, 'Dept', 'sys.menu.system.dept', 1, 4, 'dept', 'system/dept/index', '1', '0', 'C', '1', '1', 'system:dept:list', 'tree', 'system', now(), 'Dept management'),
    (104, 1, 'Post', 'sys.menu.system.post', 1, 5, 'post', 'system/post/index', '1', '0', 'C', '1', '1', 'system:post:list', 'post', 'system', now(), 'Post management'),
    (105, 1, 'Dict', 'sys.menu.system.dict', 1, 6, 'dict', 'system/dict/index', '1', '0', 'C', '1', '1', 'system:dict:list', 'dict', 'system', now(), 'Dict management'),
    (106, 1, 'Config', 'sys.menu.system.config', 1, 7, 'config', 'system/config/index', '1', '0', 'C', '1', '1', 'system:config:list', 'edit', 'system', now(), 'Config management'),
    (107, 1, 'Merchant Audit', 'sys.menu.system.merchantAudit', 1, 8, 'tenantApplication', 'system/tenant/applications', '1', '0', 'C', '1', '1', 'system:tenant:application:list', 'peoples', 'system', now(), 'Merchant tenant audit'),
    (108, 1, 'Merchant Profile', 'sys.menu.system.merchantProfile', 1, 9, 'merchantProfile', 'system/merchant/profile', '1', '0', 'C', '1', '1', 'system:merchant:profile:list', 'peoples', 'system', now(), 'Merchant profile management'),
    (1001, 1, 'Tenant Application Query', 'sys.menu.system.merchantAudit.query', 107, 1, '#', '', '1', '0', 'F', '1', '1', 'system:tenant:application:query', '#', 'system', now(), ''),
    (1002, 1, 'Tenant Application Audit', 'sys.menu.system.merchantAudit.audit', 107, 2, '#', '', '1', '0', 'F', '1', '1', 'system:tenant:application:audit', '#', 'system', now(), ''),
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
UPDATE sys_menu SET i18n_key = 'sys.menu.system.merchantAudit.audit' WHERE menu_id = 1002 AND i18n_key IS NULL;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.merchantProfile.query' WHERE menu_id = 1003 AND i18n_key IS NULL;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.merchantProfile.edit' WHERE menu_id = 20004 AND i18n_key IS NULL;
UPDATE sys_menu SET tenant_id = 1 WHERE menu_id IN (1, 100, 101, 102, 103, 104, 105, 106, 107, 108, 1001, 1002, 1003,  20004);

INSERT INTO sys_i18n_message (message_id, message_key, locale, message_value, create_by, create_time, remark)
VALUES
    (9101, 'sys.menu.system.notice', 'en_US', 'Notice', 'system', now(), 'Base menu'),
    (9102, 'sys.menu.system.notice', 'zh_CN', '通知公告', 'system', now(), '基础菜单'),
    (9103, 'sys.menu.system.oss', 'en_US', 'File', 'system', now(), 'Base menu'),
    (9104, 'sys.menu.system.oss', 'zh_CN', '文件管理', 'system', now(), '基础菜单'),
    (9105, 'sys.menu.monitor', 'en_US', 'Monitor', 'system', now(), 'Base menu'),
    (9106, 'sys.menu.monitor', 'zh_CN', '系统监控', 'system', now(), '基础菜单'),
    (9107, 'sys.menu.monitor.online', 'en_US', 'Online Users', 'system', now(), 'Base menu'),
    (9108, 'sys.menu.monitor.online', 'zh_CN', '在线用户', 'system', now(), '基础菜单'),
    (9109, 'sys.menu.monitor.cache', 'en_US', 'Cache Monitor', 'system', now(), 'Base menu'),
    (9110, 'sys.menu.monitor.cache', 'zh_CN', '缓存监控', 'system', now(), '基础菜单'),
    (9111, 'sys.menu.monitor.cacheList', 'en_US', 'Cache List', 'system', now(), 'Base menu'),
    (9112, 'sys.menu.monitor.cacheList', 'zh_CN', '缓存列表', 'system', now(), '基础菜单'),
    (9113, 'sys.menu.tool.gen', 'en_US', 'Code Generator', 'system', now(), 'Base menu'),
    (9114, 'sys.menu.tool.gen', 'zh_CN', '代码生成', 'system', now(), '基础菜单'),
    (9115, 'sys.menu.log', 'en_US', 'Logs', 'system', now(), 'Base menu'),
    (9116, 'sys.menu.log', 'zh_CN', '日志管理', 'system', now(), '基础菜单'),
    (9117, 'sys.menu.log.operlog', 'en_US', 'Operation Logs', 'system', now(), 'Base menu'),
    (9118, 'sys.menu.log.operlog', 'zh_CN', '操作日志', 'system', now(), '基础菜单'),
    (9119, 'sys.menu.log.logininfor', 'en_US', 'Login Logs', 'system', now(), 'Base menu'),
    (9120, 'sys.menu.log.logininfor', 'zh_CN', '登录日志', 'system', now(), '基础菜单'),
    (9121, 'sys.menu.monitor.cacheList.clear', 'en_US', 'Clear Cache', 'system', now(), 'Base menu'),
    (9122, 'sys.menu.monitor.cacheList.clear', 'zh_CN', '清理缓存', 'system', now(), '基础菜单')
ON CONFLICT (message_key, locale) DO UPDATE
SET message_value = EXCLUDED.message_value,
    update_by = 'system',
    update_time = now();

INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (120, 1, 'Notice', 'sys.menu.system.notice', 1, 9, 'notice', 'system/notice/index', '1', '0', 'C', '1', '1', 'system:notice:list', 'message', 'system', now(), 'Notice management'),
    (121, 1, 'File', 'sys.menu.system.oss', 1, 10, 'oss', 'system/oss/index', '1', '0', 'C', '1', '1', 'system:oss:list', 'upload', 'system', now(), 'File management'),
    (200, 1, 'Monitor', 'sys.menu.monitor', 0, 120, 'monitor', NULL, '1', '0', 'M', '1', '1', NULL, 'monitor', 'system', now(), 'System monitor'),
    (201, 1, 'Online Users', 'sys.menu.monitor.online', 200, 1, 'online', 'monitor/online/index', '1', '0', 'C', '1', '1', 'monitor:online:list', 'online', 'system', now(), 'Online users'),
    (202, 1, 'Cache Monitor', 'sys.menu.monitor.cache', 200, 2, 'cache', 'monitor/cache/index', '1', '0', 'C', '1', '1', 'monitor:cache:list', 'redis', 'system', now(), 'Cache monitor'),
    (203, 1, 'Cache List', 'sys.menu.monitor.cacheList', 200, 3, 'cacheList', 'monitor/cache/list', '1', '0', 'C', '1', '1', 'monitor:cache:list', 'redis-list', 'system', now(), 'Cache list'),
    (204, 1, 'Clear Cache', 'sys.menu.monitor.cacheList.clear', 203, 1, '#', '', '1', '0', 'F', '1', '1', 'monitor:cache:remove', '#', 'system', now(), 'Clear cache'),
    (300, 1, 'Code Generator', 'sys.menu.tool.gen', 0, 130, '/gen', 'tool/gen/index', '1', '0', 'C', '1', '1', 'tool:gen:list', 'code', 'system', now(), 'Code generator'),
    (400, 1, 'Logs', 'sys.menu.log', 0, 140, 'log', NULL, '1', '0', 'M', '1', '1', NULL, 'log', 'system', now(), 'Log management'),
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
UPDATE sys_menu SET tenant_id = 1 WHERE menu_id IN (120, 121, 200, 201, 202, 203, 204, 300, 400, 401, 402);

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT 1, menu_id, 1 FROM sys_menu
WHERE tenant_id = 1
ON CONFLICT DO NOTHING;
UPDATE sys_role_menu SET tenant_id = 1 WHERE role_id = 1;

INSERT INTO sys_i18n_message (message_id, message_key, locale, message_value, create_by, create_time, remark)
VALUES
    (19301, 'sys.menu.merchantManagement', 'en_US', 'Merchant Management', 'system', now(), 'Merchant module'),
    (19302, 'sys.menu.merchantManagement', 'zh_CN', '商家管理', 'system', now(), '商家模块'),
    (19303, 'sys.menu.system.legalDocument', 'en_US', 'Legal Content', 'system', now(), 'Legal content'),
    (19304, 'sys.menu.system.legalDocument', 'zh_CN', '法务内容', 'system', now(), '法务内容'),
    (19305, 'sys.menu.system.legalDocument.query', 'en_US', 'Legal Content Query', 'system', now(), 'Legal content permission'),
    (19306, 'sys.menu.system.legalDocument.query', 'zh_CN', '法务内容查询', 'system', now(), '法务内容权限'),
    (19307, 'sys.menu.system.legalDocument.add', 'en_US', 'Legal Content Add', 'system', now(), 'Legal content permission'),
    (19308, 'sys.menu.system.legalDocument.add', 'zh_CN', '法务内容新增', 'system', now(), '法务内容权限'),
    (19309, 'sys.menu.system.legalDocument.edit', 'en_US', 'Legal Content Edit', 'system', now(), 'Legal content permission'),
    (19310, 'sys.menu.system.legalDocument.edit', 'zh_CN', '法务内容修改', 'system', now(), '法务内容权限'),
    (19311, 'sys.menu.system.legalDocument.remove', 'en_US', 'Legal Content Delete', 'system', now(), 'Legal content permission'),
    (19312, 'sys.menu.system.legalDocument.remove', 'zh_CN', '法务内容删除', 'system', now(), '法务内容权限')
ON CONFLICT (message_key, locale) DO UPDATE
SET message_value = EXCLUDED.message_value,
    update_by = 'system',
    update_time = now();

INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (19500, 1, 'Merchant Management', 'sys.menu.merchantManagement', 0, 20, 'merchantManagement', NULL, '1', '0', 'M', '1', '1', NULL, 'peoples', 'system', now(), 'Merchant management'),
    (19501, 1, 'Legal Content', 'sys.menu.system.legalDocument', 1, 11, 'legal/document', 'system/legal/document', '1', '0', 'C', '1', '1', 'system:legal:document:list', 'documentation', 'system', now(), 'Legal content'),
    (20005, 1, 'Legal Content Query', 'sys.menu.system.legalDocument.query', 19501, 1, '#', '', '1', '0', 'F', '1', '1', 'system:legal:document:query', '#', 'system', now(), ''),
    (20006, 1, 'Legal Content Add', 'sys.menu.system.legalDocument.add', 19501, 2, '#', '', '1', '0', 'F', '1', '1', 'system:legal:document:add', '#', 'system', now(), ''),
    (20007, 1, 'Legal Content Edit', 'sys.menu.system.legalDocument.edit', 19501, 3, '#', '', '1', '0', 'F', '1', '1', 'system:legal:document:edit', '#', 'system', now(), ''),
    (20008, 1, 'Legal Content Delete', 'sys.menu.system.legalDocument.remove', 19501, 4, '#', '', '1', '0', 'F', '1', '1', 'system:legal:document:remove', '#', 'system', now(), '')
ON CONFLICT (menu_id) DO UPDATE
SET menu_name = EXCLUDED.menu_name,
    i18n_key = EXCLUDED.i18n_key,
    parent_id = EXCLUDED.parent_id,
    order_num = EXCLUDED.order_num,
    path = EXCLUDED.path,
    component = EXCLUDED.component,
    menu_type = EXCLUDED.menu_type,
    visible = EXCLUDED.visible,
    status = EXCLUDED.status,
    tenant_id = EXCLUDED.tenant_id,
    update_by = 'system',
    update_time = now();
UPDATE sys_menu SET parent_id = 19500, order_num = 1, path = 'tenantApplication' WHERE menu_id = 107;
UPDATE sys_menu SET parent_id = 19500, order_num = 2, path = 'merchantProfile' WHERE menu_id = 108;
INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
VALUES (1, 19500, 1), (1, 19501, 1), (1, 20004, 1), (1, 20005, 1), (1, 20006, 1), (1, 20007, 1), (1, 20008, 1)
ON CONFLICT DO NOTHING;

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

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES
    (19004, 'Country', 'sys_country', '1', 'system', now(), 'Country options'),
    (19005, 'User gender', 'sys_user_sex', '1', 'system', now(), 'User gender')
ON CONFLICT (dict_id) DO NOTHING;

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, i18n_key, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES
    (19020, 1, 'United States', 'sys.dict.sys_country.us', 'US', 'sys_country', 'primary', 'Y', '1', 'system', now()),
    (19021, 2, 'China', 'sys.dict.sys_country.cn', 'CN', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (19022, 3, 'Canada', 'sys.dict.sys_country.ca', 'CA', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (19023, 4, 'Mexico', 'sys.dict.sys_country.mx', 'MX', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30000, 5, 'Afghanistan', 'sys.dict.sys_country.af', 'AF', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30001, 6, 'Åland Islands', 'sys.dict.sys_country.ax', 'AX', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30002, 7, 'Albania', 'sys.dict.sys_country.al', 'AL', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30003, 8, 'Algeria', 'sys.dict.sys_country.dz', 'DZ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30004, 9, 'American Samoa', 'sys.dict.sys_country.as', 'AS', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30005, 10, 'Andorra', 'sys.dict.sys_country.ad', 'AD', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30006, 11, 'Angola', 'sys.dict.sys_country.ao', 'AO', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30007, 12, 'Anguilla', 'sys.dict.sys_country.ai', 'AI', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30008, 13, 'Antarctica', 'sys.dict.sys_country.aq', 'AQ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30009, 14, 'Antigua and Barbuda', 'sys.dict.sys_country.ag', 'AG', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30010, 15, 'Argentina', 'sys.dict.sys_country.ar', 'AR', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30011, 16, 'Armenia', 'sys.dict.sys_country.am', 'AM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30012, 17, 'Aruba', 'sys.dict.sys_country.aw', 'AW', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30013, 18, 'Australia', 'sys.dict.sys_country.au', 'AU', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30014, 19, 'Austria', 'sys.dict.sys_country.at', 'AT', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30015, 20, 'Azerbaijan', 'sys.dict.sys_country.az', 'AZ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30016, 21, 'Bahamas', 'sys.dict.sys_country.bs', 'BS', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30017, 22, 'Bahrain', 'sys.dict.sys_country.bh', 'BH', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30018, 23, 'Bangladesh', 'sys.dict.sys_country.bd', 'BD', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30019, 24, 'Barbados', 'sys.dict.sys_country.bb', 'BB', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30020, 25, 'Belarus', 'sys.dict.sys_country.by', 'BY', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30021, 26, 'Belgium', 'sys.dict.sys_country.be', 'BE', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30022, 27, 'Belize', 'sys.dict.sys_country.bz', 'BZ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30023, 28, 'Benin', 'sys.dict.sys_country.bj', 'BJ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30024, 29, 'Bermuda', 'sys.dict.sys_country.bm', 'BM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30025, 30, 'Bhutan', 'sys.dict.sys_country.bt', 'BT', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30026, 31, 'Bolivia', 'sys.dict.sys_country.bo', 'BO', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30027, 32, 'Bosnia and Herzegovina', 'sys.dict.sys_country.ba', 'BA', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30028, 33, 'Botswana', 'sys.dict.sys_country.bw', 'BW', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30029, 34, 'Bouvet Island', 'sys.dict.sys_country.bv', 'BV', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30030, 35, 'Brazil', 'sys.dict.sys_country.br', 'BR', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30031, 36, 'British Indian Ocean Territory', 'sys.dict.sys_country.io', 'IO', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30032, 37, 'British Virgin Islands', 'sys.dict.sys_country.vg', 'VG', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30033, 38, 'Brunei', 'sys.dict.sys_country.bn', 'BN', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30034, 39, 'Bulgaria', 'sys.dict.sys_country.bg', 'BG', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30035, 40, 'Burkina Faso', 'sys.dict.sys_country.bf', 'BF', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30036, 41, 'Burundi', 'sys.dict.sys_country.bi', 'BI', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30037, 42, 'Cambodia', 'sys.dict.sys_country.kh', 'KH', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30038, 43, 'Cameroon', 'sys.dict.sys_country.cm', 'CM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30039, 44, 'Cape Verde', 'sys.dict.sys_country.cv', 'CV', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30040, 45, 'Caribbean Netherlands', 'sys.dict.sys_country.bq', 'BQ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30041, 46, 'Cayman Islands', 'sys.dict.sys_country.ky', 'KY', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30042, 47, 'Central African Republic', 'sys.dict.sys_country.cf', 'CF', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30043, 48, 'Chad', 'sys.dict.sys_country.td', 'TD', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30044, 49, 'Chile', 'sys.dict.sys_country.cl', 'CL', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30045, 50, 'Christmas Island', 'sys.dict.sys_country.cx', 'CX', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30046, 51, 'Cocos (Keeling) Islands', 'sys.dict.sys_country.cc', 'CC', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30047, 52, 'Colombia', 'sys.dict.sys_country.co', 'CO', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30048, 53, 'Comoros', 'sys.dict.sys_country.km', 'KM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30049, 54, 'Cook Islands', 'sys.dict.sys_country.ck', 'CK', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30050, 55, 'Costa Rica', 'sys.dict.sys_country.cr', 'CR', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30051, 56, 'Croatia', 'sys.dict.sys_country.hr', 'HR', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30052, 57, 'Cuba', 'sys.dict.sys_country.cu', 'CU', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30053, 58, 'Curaçao', 'sys.dict.sys_country.cw', 'CW', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30054, 59, 'Cyprus', 'sys.dict.sys_country.cy', 'CY', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30055, 60, 'Czechia', 'sys.dict.sys_country.cz', 'CZ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30056, 61, 'Denmark', 'sys.dict.sys_country.dk', 'DK', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30057, 62, 'Djibouti', 'sys.dict.sys_country.dj', 'DJ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30058, 63, 'Dominica', 'sys.dict.sys_country.dm', 'DM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30059, 64, 'Dominican Republic', 'sys.dict.sys_country.do', 'DO', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30060, 65, 'DR Congo', 'sys.dict.sys_country.cd', 'CD', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30061, 66, 'Ecuador', 'sys.dict.sys_country.ec', 'EC', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30062, 67, 'Egypt', 'sys.dict.sys_country.eg', 'EG', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30063, 68, 'El Salvador', 'sys.dict.sys_country.sv', 'SV', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30064, 69, 'Equatorial Guinea', 'sys.dict.sys_country.gq', 'GQ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30065, 70, 'Eritrea', 'sys.dict.sys_country.er', 'ER', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30066, 71, 'Estonia', 'sys.dict.sys_country.ee', 'EE', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30067, 72, 'Eswatini', 'sys.dict.sys_country.sz', 'SZ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30068, 73, 'Ethiopia', 'sys.dict.sys_country.et', 'ET', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30069, 74, 'Falkland Islands', 'sys.dict.sys_country.fk', 'FK', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30070, 75, 'Faroe Islands', 'sys.dict.sys_country.fo', 'FO', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30071, 76, 'Fiji', 'sys.dict.sys_country.fj', 'FJ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30072, 77, 'Finland', 'sys.dict.sys_country.fi', 'FI', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30073, 78, 'France', 'sys.dict.sys_country.fr', 'FR', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30074, 79, 'French Guiana', 'sys.dict.sys_country.gf', 'GF', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30075, 80, 'French Polynesia', 'sys.dict.sys_country.pf', 'PF', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30076, 81, 'French Southern and Antarctic Lands', 'sys.dict.sys_country.tf', 'TF', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30077, 82, 'Gabon', 'sys.dict.sys_country.ga', 'GA', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30078, 83, 'Gambia', 'sys.dict.sys_country.gm', 'GM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30079, 84, 'Georgia', 'sys.dict.sys_country.ge', 'GE', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30080, 85, 'Germany', 'sys.dict.sys_country.de', 'DE', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30081, 86, 'Ghana', 'sys.dict.sys_country.gh', 'GH', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30082, 87, 'Gibraltar', 'sys.dict.sys_country.gi', 'GI', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30083, 88, 'Greece', 'sys.dict.sys_country.gr', 'GR', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30084, 89, 'Greenland', 'sys.dict.sys_country.gl', 'GL', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30085, 90, 'Grenada', 'sys.dict.sys_country.gd', 'GD', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30086, 91, 'Guadeloupe', 'sys.dict.sys_country.gp', 'GP', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30087, 92, 'Guam', 'sys.dict.sys_country.gu', 'GU', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30088, 93, 'Guatemala', 'sys.dict.sys_country.gt', 'GT', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30089, 94, 'Guernsey', 'sys.dict.sys_country.gg', 'GG', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30090, 95, 'Guinea', 'sys.dict.sys_country.gn', 'GN', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30091, 96, 'Guinea-Bissau', 'sys.dict.sys_country.gw', 'GW', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30092, 97, 'Guyana', 'sys.dict.sys_country.gy', 'GY', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30093, 98, 'Haiti', 'sys.dict.sys_country.ht', 'HT', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30094, 99, 'Heard Island and McDonald Islands', 'sys.dict.sys_country.hm', 'HM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30095, 100, 'Honduras', 'sys.dict.sys_country.hn', 'HN', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30096, 101, 'Hong Kong', 'sys.dict.sys_country.hk', 'HK', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30097, 102, 'Hungary', 'sys.dict.sys_country.hu', 'HU', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30098, 103, 'Iceland', 'sys.dict.sys_country.is', 'IS', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30099, 104, 'India', 'sys.dict.sys_country.in', 'IN', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30100, 105, 'Indonesia', 'sys.dict.sys_country.id', 'ID', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30101, 106, 'Iran', 'sys.dict.sys_country.ir', 'IR', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30102, 107, 'Iraq', 'sys.dict.sys_country.iq', 'IQ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30103, 108, 'Ireland', 'sys.dict.sys_country.ie', 'IE', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30104, 109, 'Isle of Man', 'sys.dict.sys_country.im', 'IM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30105, 110, 'Israel', 'sys.dict.sys_country.il', 'IL', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30106, 111, 'Italy', 'sys.dict.sys_country.it', 'IT', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30107, 112, 'Ivory Coast', 'sys.dict.sys_country.ci', 'CI', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30108, 113, 'Jamaica', 'sys.dict.sys_country.jm', 'JM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30109, 114, 'Japan', 'sys.dict.sys_country.jp', 'JP', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30110, 115, 'Jersey', 'sys.dict.sys_country.je', 'JE', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30111, 116, 'Jordan', 'sys.dict.sys_country.jo', 'JO', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30112, 117, 'Kazakhstan', 'sys.dict.sys_country.kz', 'KZ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30113, 118, 'Kenya', 'sys.dict.sys_country.ke', 'KE', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30114, 119, 'Kiribati', 'sys.dict.sys_country.ki', 'KI', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30115, 120, 'Kuwait', 'sys.dict.sys_country.kw', 'KW', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30116, 121, 'Kyrgyzstan', 'sys.dict.sys_country.kg', 'KG', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30117, 122, 'Laos', 'sys.dict.sys_country.la', 'LA', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30118, 123, 'Latvia', 'sys.dict.sys_country.lv', 'LV', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30119, 124, 'Lebanon', 'sys.dict.sys_country.lb', 'LB', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30120, 125, 'Lesotho', 'sys.dict.sys_country.ls', 'LS', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30121, 126, 'Liberia', 'sys.dict.sys_country.lr', 'LR', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30122, 127, 'Libya', 'sys.dict.sys_country.ly', 'LY', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30123, 128, 'Liechtenstein', 'sys.dict.sys_country.li', 'LI', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30124, 129, 'Lithuania', 'sys.dict.sys_country.lt', 'LT', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30125, 130, 'Luxembourg', 'sys.dict.sys_country.lu', 'LU', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30126, 131, 'Macau', 'sys.dict.sys_country.mo', 'MO', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30127, 132, 'Madagascar', 'sys.dict.sys_country.mg', 'MG', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30128, 133, 'Malawi', 'sys.dict.sys_country.mw', 'MW', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30129, 134, 'Malaysia', 'sys.dict.sys_country.my', 'MY', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30130, 135, 'Maldives', 'sys.dict.sys_country.mv', 'MV', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30131, 136, 'Mali', 'sys.dict.sys_country.ml', 'ML', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30132, 137, 'Malta', 'sys.dict.sys_country.mt', 'MT', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30133, 138, 'Marshall Islands', 'sys.dict.sys_country.mh', 'MH', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30134, 139, 'Martinique', 'sys.dict.sys_country.mq', 'MQ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30135, 140, 'Mauritania', 'sys.dict.sys_country.mr', 'MR', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30136, 141, 'Mauritius', 'sys.dict.sys_country.mu', 'MU', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30137, 142, 'Mayotte', 'sys.dict.sys_country.yt', 'YT', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30138, 143, 'Micronesia', 'sys.dict.sys_country.fm', 'FM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30139, 144, 'Moldova', 'sys.dict.sys_country.md', 'MD', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30140, 145, 'Monaco', 'sys.dict.sys_country.mc', 'MC', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30141, 146, 'Mongolia', 'sys.dict.sys_country.mn', 'MN', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30142, 147, 'Montenegro', 'sys.dict.sys_country.me', 'ME', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30143, 148, 'Montserrat', 'sys.dict.sys_country.ms', 'MS', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30144, 149, 'Morocco', 'sys.dict.sys_country.ma', 'MA', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30145, 150, 'Mozambique', 'sys.dict.sys_country.mz', 'MZ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30146, 151, 'Myanmar', 'sys.dict.sys_country.mm', 'MM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30147, 152, 'Namibia', 'sys.dict.sys_country.na', 'NA', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30148, 153, 'Nauru', 'sys.dict.sys_country.nr', 'NR', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30149, 154, 'Nepal', 'sys.dict.sys_country.np', 'NP', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30150, 155, 'Netherlands', 'sys.dict.sys_country.nl', 'NL', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30151, 156, 'New Caledonia', 'sys.dict.sys_country.nc', 'NC', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30152, 157, 'New Zealand', 'sys.dict.sys_country.nz', 'NZ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30153, 158, 'Nicaragua', 'sys.dict.sys_country.ni', 'NI', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30154, 159, 'Niger', 'sys.dict.sys_country.ne', 'NE', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30155, 160, 'Nigeria', 'sys.dict.sys_country.ng', 'NG', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30156, 161, 'Niue', 'sys.dict.sys_country.nu', 'NU', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30157, 162, 'Norfolk Island', 'sys.dict.sys_country.nf', 'NF', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30158, 163, 'North Korea', 'sys.dict.sys_country.kp', 'KP', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30159, 164, 'North Macedonia', 'sys.dict.sys_country.mk', 'MK', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30160, 165, 'Northern Mariana Islands', 'sys.dict.sys_country.mp', 'MP', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30161, 166, 'Norway', 'sys.dict.sys_country.no', 'NO', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30162, 167, 'Oman', 'sys.dict.sys_country.om', 'OM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30163, 168, 'Pakistan', 'sys.dict.sys_country.pk', 'PK', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30164, 169, 'Palau', 'sys.dict.sys_country.pw', 'PW', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30165, 170, 'Palestine', 'sys.dict.sys_country.ps', 'PS', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30166, 171, 'Panama', 'sys.dict.sys_country.pa', 'PA', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30167, 172, 'Papua New Guinea', 'sys.dict.sys_country.pg', 'PG', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30168, 173, 'Paraguay', 'sys.dict.sys_country.py', 'PY', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30169, 174, 'Peru', 'sys.dict.sys_country.pe', 'PE', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30170, 175, 'Philippines', 'sys.dict.sys_country.ph', 'PH', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30171, 176, 'Pitcairn Islands', 'sys.dict.sys_country.pn', 'PN', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30172, 177, 'Poland', 'sys.dict.sys_country.pl', 'PL', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30173, 178, 'Portugal', 'sys.dict.sys_country.pt', 'PT', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30174, 179, 'Puerto Rico', 'sys.dict.sys_country.pr', 'PR', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30175, 180, 'Qatar', 'sys.dict.sys_country.qa', 'QA', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30176, 181, 'Republic of the Congo', 'sys.dict.sys_country.cg', 'CG', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30177, 182, 'Réunion', 'sys.dict.sys_country.re', 'RE', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30178, 183, 'Romania', 'sys.dict.sys_country.ro', 'RO', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30179, 184, 'Russia', 'sys.dict.sys_country.ru', 'RU', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30180, 185, 'Rwanda', 'sys.dict.sys_country.rw', 'RW', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30181, 186, 'Saint Barthélemy', 'sys.dict.sys_country.bl', 'BL', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30182, 187, 'Saint Helena, Ascension and Tristan da Cunha', 'sys.dict.sys_country.sh', 'SH', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30183, 188, 'Saint Kitts and Nevis', 'sys.dict.sys_country.kn', 'KN', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30184, 189, 'Saint Lucia', 'sys.dict.sys_country.lc', 'LC', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30185, 190, 'Saint Martin', 'sys.dict.sys_country.mf', 'MF', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30186, 191, 'Saint Pierre and Miquelon', 'sys.dict.sys_country.pm', 'PM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30187, 192, 'Saint Vincent and the Grenadines', 'sys.dict.sys_country.vc', 'VC', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30188, 193, 'Samoa', 'sys.dict.sys_country.ws', 'WS', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30189, 194, 'San Marino', 'sys.dict.sys_country.sm', 'SM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30190, 195, 'São Tomé and Príncipe', 'sys.dict.sys_country.st', 'ST', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30191, 196, 'Saudi Arabia', 'sys.dict.sys_country.sa', 'SA', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30192, 197, 'Senegal', 'sys.dict.sys_country.sn', 'SN', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30193, 198, 'Serbia', 'sys.dict.sys_country.rs', 'RS', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30194, 199, 'Seychelles', 'sys.dict.sys_country.sc', 'SC', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30195, 200, 'Sierra Leone', 'sys.dict.sys_country.sl', 'SL', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30196, 201, 'Singapore', 'sys.dict.sys_country.sg', 'SG', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30197, 202, 'Sint Maarten', 'sys.dict.sys_country.sx', 'SX', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30198, 203, 'Slovakia', 'sys.dict.sys_country.sk', 'SK', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30199, 204, 'Slovenia', 'sys.dict.sys_country.si', 'SI', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30200, 205, 'Solomon Islands', 'sys.dict.sys_country.sb', 'SB', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30201, 206, 'Somalia', 'sys.dict.sys_country.so', 'SO', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30202, 207, 'South Africa', 'sys.dict.sys_country.za', 'ZA', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30203, 208, 'South Georgia', 'sys.dict.sys_country.gs', 'GS', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30204, 209, 'South Korea', 'sys.dict.sys_country.kr', 'KR', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30205, 210, 'South Sudan', 'sys.dict.sys_country.ss', 'SS', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30206, 211, 'Spain', 'sys.dict.sys_country.es', 'ES', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30207, 212, 'Sri Lanka', 'sys.dict.sys_country.lk', 'LK', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30208, 213, 'Sudan', 'sys.dict.sys_country.sd', 'SD', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30209, 214, 'Suriname', 'sys.dict.sys_country.sr', 'SR', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30210, 215, 'Svalbard and Jan Mayen', 'sys.dict.sys_country.sj', 'SJ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30211, 216, 'Sweden', 'sys.dict.sys_country.se', 'SE', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30212, 217, 'Switzerland', 'sys.dict.sys_country.ch', 'CH', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30213, 218, 'Syria', 'sys.dict.sys_country.sy', 'SY', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30214, 219, 'Taiwan', 'sys.dict.sys_country.tw', 'TW', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30215, 220, 'Tajikistan', 'sys.dict.sys_country.tj', 'TJ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30216, 221, 'Tanzania', 'sys.dict.sys_country.tz', 'TZ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30217, 222, 'Thailand', 'sys.dict.sys_country.th', 'TH', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30218, 223, 'Timor-Leste', 'sys.dict.sys_country.tl', 'TL', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30219, 224, 'Togo', 'sys.dict.sys_country.tg', 'TG', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30220, 225, 'Tokelau', 'sys.dict.sys_country.tk', 'TK', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30221, 226, 'Tonga', 'sys.dict.sys_country.to', 'TO', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30222, 227, 'Trinidad and Tobago', 'sys.dict.sys_country.tt', 'TT', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30223, 228, 'Tunisia', 'sys.dict.sys_country.tn', 'TN', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30224, 229, 'Türkiye', 'sys.dict.sys_country.tr', 'TR', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30225, 230, 'Turkmenistan', 'sys.dict.sys_country.tm', 'TM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30226, 231, 'Turks and Caicos Islands', 'sys.dict.sys_country.tc', 'TC', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30227, 232, 'Tuvalu', 'sys.dict.sys_country.tv', 'TV', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30228, 233, 'Uganda', 'sys.dict.sys_country.ug', 'UG', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30229, 234, 'Ukraine', 'sys.dict.sys_country.ua', 'UA', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30230, 235, 'United Arab Emirates', 'sys.dict.sys_country.ae', 'AE', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30231, 236, 'United Kingdom', 'sys.dict.sys_country.gb', 'GB', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30232, 237, 'United States Minor Outlying Islands', 'sys.dict.sys_country.um', 'UM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30233, 238, 'United States Virgin Islands', 'sys.dict.sys_country.vi', 'VI', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30234, 239, 'Uruguay', 'sys.dict.sys_country.uy', 'UY', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30235, 240, 'Uzbekistan', 'sys.dict.sys_country.uz', 'UZ', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30236, 241, 'Vanuatu', 'sys.dict.sys_country.vu', 'VU', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30237, 242, 'Vatican City', 'sys.dict.sys_country.va', 'VA', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30238, 243, 'Venezuela', 'sys.dict.sys_country.ve', 'VE', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30239, 244, 'Vietnam', 'sys.dict.sys_country.vn', 'VN', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30240, 245, 'Wallis and Futuna', 'sys.dict.sys_country.wf', 'WF', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30241, 246, 'Western Sahara', 'sys.dict.sys_country.eh', 'EH', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30242, 247, 'Yemen', 'sys.dict.sys_country.ye', 'YE', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30243, 248, 'Zambia', 'sys.dict.sys_country.zm', 'ZM', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (30244, 249, 'Zimbabwe', 'sys.dict.sys_country.zw', 'ZW', 'sys_country', 'primary', 'N', '1', 'system', now()),
    (19024, 1, 'Male', 'sys.dict.sys_user_sex.male', '0', 'sys_user_sex', 'primary', 'Y', '1', 'system', now()),
    (19025, 2, 'Female', 'sys.dict.sys_user_sex.female', '1', 'sys_user_sex', 'primary', 'N', '1', 'system', now()),
    (19026, 3, 'Unknown', 'sys.dict.sys_user_sex.unknown', '2', 'sys_user_sex', 'info', 'N', '1', 'system', now())
ON CONFLICT (dict_code) DO UPDATE
SET dict_sort = EXCLUDED.dict_sort,
    dict_label = EXCLUDED.dict_label,
    i18n_key = EXCLUDED.i18n_key,
    dict_value = EXCLUDED.dict_value,
    dict_type = EXCLUDED.dict_type,
    list_class = EXCLUDED.list_class,
    is_default = EXCLUDED.is_default,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

INSERT INTO sys_i18n_message (message_id, message_key, locale, message_value, create_by, create_time, remark)
VALUES
    (310000, 'sys.dict.sys_country.us', 'en_US', 'United States', 'system', now(), 'Country'),
    (310001, 'sys.dict.sys_country.us', 'zh_CN', '美国', 'system', now(), '??'),
    (310002, 'sys.dict.sys_country.cn', 'en_US', 'China', 'system', now(), 'Country'),
    (310003, 'sys.dict.sys_country.cn', 'zh_CN', '中国', 'system', now(), '??'),
    (310004, 'sys.dict.sys_country.ca', 'en_US', 'Canada', 'system', now(), 'Country'),
    (310005, 'sys.dict.sys_country.ca', 'zh_CN', '加拿大', 'system', now(), '??'),
    (310006, 'sys.dict.sys_country.mx', 'en_US', 'Mexico', 'system', now(), 'Country'),
    (310007, 'sys.dict.sys_country.mx', 'zh_CN', '墨西哥', 'system', now(), '??'),
    (310008, 'sys.dict.sys_country.af', 'en_US', 'Afghanistan', 'system', now(), 'Country'),
    (310009, 'sys.dict.sys_country.af', 'zh_CN', '阿富汗', 'system', now(), '??'),
    (310010, 'sys.dict.sys_country.ax', 'en_US', 'Åland Islands', 'system', now(), 'Country'),
    (310011, 'sys.dict.sys_country.ax', 'zh_CN', '奥兰群岛', 'system', now(), '??'),
    (310012, 'sys.dict.sys_country.al', 'en_US', 'Albania', 'system', now(), 'Country'),
    (310013, 'sys.dict.sys_country.al', 'zh_CN', '阿尔巴尼亚', 'system', now(), '??'),
    (310014, 'sys.dict.sys_country.dz', 'en_US', 'Algeria', 'system', now(), 'Country'),
    (310015, 'sys.dict.sys_country.dz', 'zh_CN', '阿尔及利亚', 'system', now(), '??'),
    (310016, 'sys.dict.sys_country.as', 'en_US', 'American Samoa', 'system', now(), 'Country'),
    (310017, 'sys.dict.sys_country.as', 'zh_CN', '美属萨摩亚', 'system', now(), '??'),
    (310018, 'sys.dict.sys_country.ad', 'en_US', 'Andorra', 'system', now(), 'Country'),
    (310019, 'sys.dict.sys_country.ad', 'zh_CN', '安道尔', 'system', now(), '??'),
    (310020, 'sys.dict.sys_country.ao', 'en_US', 'Angola', 'system', now(), 'Country'),
    (310021, 'sys.dict.sys_country.ao', 'zh_CN', '安哥拉', 'system', now(), '??'),
    (310022, 'sys.dict.sys_country.ai', 'en_US', 'Anguilla', 'system', now(), 'Country'),
    (310023, 'sys.dict.sys_country.ai', 'zh_CN', '安圭拉', 'system', now(), '??'),
    (310024, 'sys.dict.sys_country.aq', 'en_US', 'Antarctica', 'system', now(), 'Country'),
    (310025, 'sys.dict.sys_country.aq', 'zh_CN', '南极洲', 'system', now(), '??'),
    (310026, 'sys.dict.sys_country.ag', 'en_US', 'Antigua and Barbuda', 'system', now(), 'Country'),
    (310027, 'sys.dict.sys_country.ag', 'zh_CN', '安提瓜和巴布达', 'system', now(), '??'),
    (310028, 'sys.dict.sys_country.ar', 'en_US', 'Argentina', 'system', now(), 'Country'),
    (310029, 'sys.dict.sys_country.ar', 'zh_CN', '阿根廷', 'system', now(), '??'),
    (310030, 'sys.dict.sys_country.am', 'en_US', 'Armenia', 'system', now(), 'Country'),
    (310031, 'sys.dict.sys_country.am', 'zh_CN', '亚美尼亚', 'system', now(), '??'),
    (310032, 'sys.dict.sys_country.aw', 'en_US', 'Aruba', 'system', now(), 'Country'),
    (310033, 'sys.dict.sys_country.aw', 'zh_CN', '阿鲁巴', 'system', now(), '??'),
    (310034, 'sys.dict.sys_country.au', 'en_US', 'Australia', 'system', now(), 'Country'),
    (310035, 'sys.dict.sys_country.au', 'zh_CN', '澳大利亚', 'system', now(), '??'),
    (310036, 'sys.dict.sys_country.at', 'en_US', 'Austria', 'system', now(), 'Country'),
    (310037, 'sys.dict.sys_country.at', 'zh_CN', '奥地利', 'system', now(), '??'),
    (310038, 'sys.dict.sys_country.az', 'en_US', 'Azerbaijan', 'system', now(), 'Country'),
    (310039, 'sys.dict.sys_country.az', 'zh_CN', '阿塞拜疆', 'system', now(), '??'),
    (310040, 'sys.dict.sys_country.bs', 'en_US', 'Bahamas', 'system', now(), 'Country'),
    (310041, 'sys.dict.sys_country.bs', 'zh_CN', '巴哈马', 'system', now(), '??'),
    (310042, 'sys.dict.sys_country.bh', 'en_US', 'Bahrain', 'system', now(), 'Country'),
    (310043, 'sys.dict.sys_country.bh', 'zh_CN', '巴林', 'system', now(), '??'),
    (310044, 'sys.dict.sys_country.bd', 'en_US', 'Bangladesh', 'system', now(), 'Country'),
    (310045, 'sys.dict.sys_country.bd', 'zh_CN', '孟加拉国', 'system', now(), '??'),
    (310046, 'sys.dict.sys_country.bb', 'en_US', 'Barbados', 'system', now(), 'Country'),
    (310047, 'sys.dict.sys_country.bb', 'zh_CN', '巴巴多斯', 'system', now(), '??'),
    (310048, 'sys.dict.sys_country.by', 'en_US', 'Belarus', 'system', now(), 'Country'),
    (310049, 'sys.dict.sys_country.by', 'zh_CN', '白俄罗斯', 'system', now(), '??'),
    (310050, 'sys.dict.sys_country.be', 'en_US', 'Belgium', 'system', now(), 'Country'),
    (310051, 'sys.dict.sys_country.be', 'zh_CN', '比利时', 'system', now(), '??'),
    (310052, 'sys.dict.sys_country.bz', 'en_US', 'Belize', 'system', now(), 'Country'),
    (310053, 'sys.dict.sys_country.bz', 'zh_CN', '伯利兹', 'system', now(), '??'),
    (310054, 'sys.dict.sys_country.bj', 'en_US', 'Benin', 'system', now(), 'Country'),
    (310055, 'sys.dict.sys_country.bj', 'zh_CN', '贝宁', 'system', now(), '??'),
    (310056, 'sys.dict.sys_country.bm', 'en_US', 'Bermuda', 'system', now(), 'Country'),
    (310057, 'sys.dict.sys_country.bm', 'zh_CN', '百慕大', 'system', now(), '??'),
    (310058, 'sys.dict.sys_country.bt', 'en_US', 'Bhutan', 'system', now(), 'Country'),
    (310059, 'sys.dict.sys_country.bt', 'zh_CN', '不丹', 'system', now(), '??'),
    (310060, 'sys.dict.sys_country.bo', 'en_US', 'Bolivia', 'system', now(), 'Country'),
    (310061, 'sys.dict.sys_country.bo', 'zh_CN', '玻利维亚', 'system', now(), '??'),
    (310062, 'sys.dict.sys_country.ba', 'en_US', 'Bosnia and Herzegovina', 'system', now(), 'Country'),
    (310063, 'sys.dict.sys_country.ba', 'zh_CN', '波斯尼亚和黑塞哥维那', 'system', now(), '??'),
    (310064, 'sys.dict.sys_country.bw', 'en_US', 'Botswana', 'system', now(), 'Country'),
    (310065, 'sys.dict.sys_country.bw', 'zh_CN', '博茨瓦纳', 'system', now(), '??'),
    (310066, 'sys.dict.sys_country.bv', 'en_US', 'Bouvet Island', 'system', now(), 'Country'),
    (310067, 'sys.dict.sys_country.bv', 'zh_CN', '布维岛', 'system', now(), '??'),
    (310068, 'sys.dict.sys_country.br', 'en_US', 'Brazil', 'system', now(), 'Country'),
    (310069, 'sys.dict.sys_country.br', 'zh_CN', '巴西', 'system', now(), '??'),
    (310070, 'sys.dict.sys_country.io', 'en_US', 'British Indian Ocean Territory', 'system', now(), 'Country'),
    (310071, 'sys.dict.sys_country.io', 'zh_CN', '英属印度洋领地', 'system', now(), '??'),
    (310072, 'sys.dict.sys_country.vg', 'en_US', 'British Virgin Islands', 'system', now(), 'Country'),
    (310073, 'sys.dict.sys_country.vg', 'zh_CN', '英属维尔京群岛', 'system', now(), '??'),
    (310074, 'sys.dict.sys_country.bn', 'en_US', 'Brunei', 'system', now(), 'Country'),
    (310075, 'sys.dict.sys_country.bn', 'zh_CN', '文莱', 'system', now(), '??'),
    (310076, 'sys.dict.sys_country.bg', 'en_US', 'Bulgaria', 'system', now(), 'Country'),
    (310077, 'sys.dict.sys_country.bg', 'zh_CN', '保加利亚', 'system', now(), '??'),
    (310078, 'sys.dict.sys_country.bf', 'en_US', 'Burkina Faso', 'system', now(), 'Country'),
    (310079, 'sys.dict.sys_country.bf', 'zh_CN', '布基纳法索', 'system', now(), '??'),
    (310080, 'sys.dict.sys_country.bi', 'en_US', 'Burundi', 'system', now(), 'Country'),
    (310081, 'sys.dict.sys_country.bi', 'zh_CN', '布隆迪', 'system', now(), '??'),
    (310082, 'sys.dict.sys_country.kh', 'en_US', 'Cambodia', 'system', now(), 'Country'),
    (310083, 'sys.dict.sys_country.kh', 'zh_CN', '柬埔寨', 'system', now(), '??'),
    (310084, 'sys.dict.sys_country.cm', 'en_US', 'Cameroon', 'system', now(), 'Country'),
    (310085, 'sys.dict.sys_country.cm', 'zh_CN', '喀麦隆', 'system', now(), '??'),
    (310086, 'sys.dict.sys_country.cv', 'en_US', 'Cape Verde', 'system', now(), 'Country'),
    (310087, 'sys.dict.sys_country.cv', 'zh_CN', '佛得角', 'system', now(), '??'),
    (310088, 'sys.dict.sys_country.bq', 'en_US', 'Caribbean Netherlands', 'system', now(), 'Country'),
    (310089, 'sys.dict.sys_country.bq', 'zh_CN', '荷蘭加勒比區', 'system', now(), '??'),
    (310090, 'sys.dict.sys_country.ky', 'en_US', 'Cayman Islands', 'system', now(), 'Country'),
    (310091, 'sys.dict.sys_country.ky', 'zh_CN', '开曼群岛', 'system', now(), '??'),
    (310092, 'sys.dict.sys_country.cf', 'en_US', 'Central African Republic', 'system', now(), 'Country'),
    (310093, 'sys.dict.sys_country.cf', 'zh_CN', '中非共和国', 'system', now(), '??'),
    (310094, 'sys.dict.sys_country.td', 'en_US', 'Chad', 'system', now(), 'Country'),
    (310095, 'sys.dict.sys_country.td', 'zh_CN', '乍得', 'system', now(), '??'),
    (310096, 'sys.dict.sys_country.cl', 'en_US', 'Chile', 'system', now(), 'Country'),
    (310097, 'sys.dict.sys_country.cl', 'zh_CN', '智利', 'system', now(), '??'),
    (310098, 'sys.dict.sys_country.cx', 'en_US', 'Christmas Island', 'system', now(), 'Country'),
    (310099, 'sys.dict.sys_country.cx', 'zh_CN', '圣诞岛', 'system', now(), '??'),
    (310100, 'sys.dict.sys_country.cc', 'en_US', 'Cocos (Keeling) Islands', 'system', now(), 'Country'),
    (310101, 'sys.dict.sys_country.cc', 'zh_CN', '科科斯', 'system', now(), '??'),
    (310102, 'sys.dict.sys_country.co', 'en_US', 'Colombia', 'system', now(), 'Country'),
    (310103, 'sys.dict.sys_country.co', 'zh_CN', '哥伦比亚', 'system', now(), '??'),
    (310104, 'sys.dict.sys_country.km', 'en_US', 'Comoros', 'system', now(), 'Country'),
    (310105, 'sys.dict.sys_country.km', 'zh_CN', '科摩罗', 'system', now(), '??'),
    (310106, 'sys.dict.sys_country.ck', 'en_US', 'Cook Islands', 'system', now(), 'Country'),
    (310107, 'sys.dict.sys_country.ck', 'zh_CN', '库克群岛', 'system', now(), '??'),
    (310108, 'sys.dict.sys_country.cr', 'en_US', 'Costa Rica', 'system', now(), 'Country'),
    (310109, 'sys.dict.sys_country.cr', 'zh_CN', '哥斯达黎加', 'system', now(), '??'),
    (310110, 'sys.dict.sys_country.hr', 'en_US', 'Croatia', 'system', now(), 'Country'),
    (310111, 'sys.dict.sys_country.hr', 'zh_CN', '克罗地亚', 'system', now(), '??'),
    (310112, 'sys.dict.sys_country.cu', 'en_US', 'Cuba', 'system', now(), 'Country'),
    (310113, 'sys.dict.sys_country.cu', 'zh_CN', '古巴', 'system', now(), '??'),
    (310114, 'sys.dict.sys_country.cw', 'en_US', 'Curaçao', 'system', now(), 'Country'),
    (310115, 'sys.dict.sys_country.cw', 'zh_CN', '库拉索', 'system', now(), '??'),
    (310116, 'sys.dict.sys_country.cy', 'en_US', 'Cyprus', 'system', now(), 'Country'),
    (310117, 'sys.dict.sys_country.cy', 'zh_CN', '塞浦路斯', 'system', now(), '??'),
    (310118, 'sys.dict.sys_country.cz', 'en_US', 'Czechia', 'system', now(), 'Country'),
    (310119, 'sys.dict.sys_country.cz', 'zh_CN', '捷克', 'system', now(), '??'),
    (310120, 'sys.dict.sys_country.dk', 'en_US', 'Denmark', 'system', now(), 'Country'),
    (310121, 'sys.dict.sys_country.dk', 'zh_CN', '丹麦', 'system', now(), '??'),
    (310122, 'sys.dict.sys_country.dj', 'en_US', 'Djibouti', 'system', now(), 'Country'),
    (310123, 'sys.dict.sys_country.dj', 'zh_CN', '吉布提', 'system', now(), '??'),
    (310124, 'sys.dict.sys_country.dm', 'en_US', 'Dominica', 'system', now(), 'Country'),
    (310125, 'sys.dict.sys_country.dm', 'zh_CN', '多米尼加', 'system', now(), '??'),
    (310126, 'sys.dict.sys_country.do', 'en_US', 'Dominican Republic', 'system', now(), 'Country'),
    (310127, 'sys.dict.sys_country.do', 'zh_CN', '多明尼加', 'system', now(), '??'),
    (310128, 'sys.dict.sys_country.cd', 'en_US', 'DR Congo', 'system', now(), 'Country'),
    (310129, 'sys.dict.sys_country.cd', 'zh_CN', '民主刚果', 'system', now(), '??'),
    (310130, 'sys.dict.sys_country.ec', 'en_US', 'Ecuador', 'system', now(), 'Country'),
    (310131, 'sys.dict.sys_country.ec', 'zh_CN', '厄瓜多尔', 'system', now(), '??'),
    (310132, 'sys.dict.sys_country.eg', 'en_US', 'Egypt', 'system', now(), 'Country'),
    (310133, 'sys.dict.sys_country.eg', 'zh_CN', '埃及', 'system', now(), '??'),
    (310134, 'sys.dict.sys_country.sv', 'en_US', 'El Salvador', 'system', now(), 'Country'),
    (310135, 'sys.dict.sys_country.sv', 'zh_CN', '萨尔瓦多', 'system', now(), '??'),
    (310136, 'sys.dict.sys_country.gq', 'en_US', 'Equatorial Guinea', 'system', now(), 'Country'),
    (310137, 'sys.dict.sys_country.gq', 'zh_CN', '赤道几内亚', 'system', now(), '??'),
    (310138, 'sys.dict.sys_country.er', 'en_US', 'Eritrea', 'system', now(), 'Country'),
    (310139, 'sys.dict.sys_country.er', 'zh_CN', '厄立特里亚', 'system', now(), '??'),
    (310140, 'sys.dict.sys_country.ee', 'en_US', 'Estonia', 'system', now(), 'Country'),
    (310141, 'sys.dict.sys_country.ee', 'zh_CN', '爱沙尼亚', 'system', now(), '??'),
    (310142, 'sys.dict.sys_country.sz', 'en_US', 'Eswatini', 'system', now(), 'Country'),
    (310143, 'sys.dict.sys_country.sz', 'zh_CN', '斯威士兰', 'system', now(), '??'),
    (310144, 'sys.dict.sys_country.et', 'en_US', 'Ethiopia', 'system', now(), 'Country'),
    (310145, 'sys.dict.sys_country.et', 'zh_CN', '埃塞俄比亚', 'system', now(), '??'),
    (310146, 'sys.dict.sys_country.fk', 'en_US', 'Falkland Islands', 'system', now(), 'Country'),
    (310147, 'sys.dict.sys_country.fk', 'zh_CN', '福克兰群岛', 'system', now(), '??'),
    (310148, 'sys.dict.sys_country.fo', 'en_US', 'Faroe Islands', 'system', now(), 'Country'),
    (310149, 'sys.dict.sys_country.fo', 'zh_CN', '法罗群岛', 'system', now(), '??'),
    (310150, 'sys.dict.sys_country.fj', 'en_US', 'Fiji', 'system', now(), 'Country'),
    (310151, 'sys.dict.sys_country.fj', 'zh_CN', '斐济', 'system', now(), '??'),
    (310152, 'sys.dict.sys_country.fi', 'en_US', 'Finland', 'system', now(), 'Country'),
    (310153, 'sys.dict.sys_country.fi', 'zh_CN', '芬兰', 'system', now(), '??'),
    (310154, 'sys.dict.sys_country.fr', 'en_US', 'France', 'system', now(), 'Country'),
    (310155, 'sys.dict.sys_country.fr', 'zh_CN', '法国', 'system', now(), '??'),
    (310156, 'sys.dict.sys_country.gf', 'en_US', 'French Guiana', 'system', now(), 'Country'),
    (310157, 'sys.dict.sys_country.gf', 'zh_CN', '法属圭亚那', 'system', now(), '??'),
    (310158, 'sys.dict.sys_country.pf', 'en_US', 'French Polynesia', 'system', now(), 'Country'),
    (310159, 'sys.dict.sys_country.pf', 'zh_CN', '法属波利尼西亚', 'system', now(), '??'),
    (310160, 'sys.dict.sys_country.tf', 'en_US', 'French Southern and Antarctic Lands', 'system', now(), 'Country'),
    (310161, 'sys.dict.sys_country.tf', 'zh_CN', '法国南部和南极土地', 'system', now(), '??'),
    (310162, 'sys.dict.sys_country.ga', 'en_US', 'Gabon', 'system', now(), 'Country'),
    (310163, 'sys.dict.sys_country.ga', 'zh_CN', '加蓬', 'system', now(), '??'),
    (310164, 'sys.dict.sys_country.gm', 'en_US', 'Gambia', 'system', now(), 'Country'),
    (310165, 'sys.dict.sys_country.gm', 'zh_CN', '冈比亚', 'system', now(), '??'),
    (310166, 'sys.dict.sys_country.ge', 'en_US', 'Georgia', 'system', now(), 'Country'),
    (310167, 'sys.dict.sys_country.ge', 'zh_CN', '格鲁吉亚', 'system', now(), '??'),
    (310168, 'sys.dict.sys_country.de', 'en_US', 'Germany', 'system', now(), 'Country'),
    (310169, 'sys.dict.sys_country.de', 'zh_CN', '德国', 'system', now(), '??'),
    (310170, 'sys.dict.sys_country.gh', 'en_US', 'Ghana', 'system', now(), 'Country'),
    (310171, 'sys.dict.sys_country.gh', 'zh_CN', '加纳', 'system', now(), '??'),
    (310172, 'sys.dict.sys_country.gi', 'en_US', 'Gibraltar', 'system', now(), 'Country'),
    (310173, 'sys.dict.sys_country.gi', 'zh_CN', '直布罗陀', 'system', now(), '??'),
    (310174, 'sys.dict.sys_country.gr', 'en_US', 'Greece', 'system', now(), 'Country'),
    (310175, 'sys.dict.sys_country.gr', 'zh_CN', '希腊', 'system', now(), '??'),
    (310176, 'sys.dict.sys_country.gl', 'en_US', 'Greenland', 'system', now(), 'Country'),
    (310177, 'sys.dict.sys_country.gl', 'zh_CN', '格陵兰', 'system', now(), '??'),
    (310178, 'sys.dict.sys_country.gd', 'en_US', 'Grenada', 'system', now(), 'Country'),
    (310179, 'sys.dict.sys_country.gd', 'zh_CN', '格林纳达', 'system', now(), '??'),
    (310180, 'sys.dict.sys_country.gp', 'en_US', 'Guadeloupe', 'system', now(), 'Country'),
    (310181, 'sys.dict.sys_country.gp', 'zh_CN', '瓜德罗普岛', 'system', now(), '??'),
    (310182, 'sys.dict.sys_country.gu', 'en_US', 'Guam', 'system', now(), 'Country'),
    (310183, 'sys.dict.sys_country.gu', 'zh_CN', '关岛', 'system', now(), '??'),
    (310184, 'sys.dict.sys_country.gt', 'en_US', 'Guatemala', 'system', now(), 'Country'),
    (310185, 'sys.dict.sys_country.gt', 'zh_CN', '危地马拉', 'system', now(), '??'),
    (310186, 'sys.dict.sys_country.gg', 'en_US', 'Guernsey', 'system', now(), 'Country'),
    (310187, 'sys.dict.sys_country.gg', 'zh_CN', '根西岛', 'system', now(), '??'),
    (310188, 'sys.dict.sys_country.gn', 'en_US', 'Guinea', 'system', now(), 'Country'),
    (310189, 'sys.dict.sys_country.gn', 'zh_CN', '几内亚', 'system', now(), '??'),
    (310190, 'sys.dict.sys_country.gw', 'en_US', 'Guinea-Bissau', 'system', now(), 'Country'),
    (310191, 'sys.dict.sys_country.gw', 'zh_CN', '几内亚比绍', 'system', now(), '??'),
    (310192, 'sys.dict.sys_country.gy', 'en_US', 'Guyana', 'system', now(), 'Country'),
    (310193, 'sys.dict.sys_country.gy', 'zh_CN', '圭亚那', 'system', now(), '??'),
    (310194, 'sys.dict.sys_country.ht', 'en_US', 'Haiti', 'system', now(), 'Country'),
    (310195, 'sys.dict.sys_country.ht', 'zh_CN', '海地', 'system', now(), '??'),
    (310196, 'sys.dict.sys_country.hm', 'en_US', 'Heard Island and McDonald Islands', 'system', now(), 'Country'),
    (310197, 'sys.dict.sys_country.hm', 'zh_CN', '赫德岛和麦当劳群岛', 'system', now(), '??'),
    (310198, 'sys.dict.sys_country.hn', 'en_US', 'Honduras', 'system', now(), 'Country'),
    (310199, 'sys.dict.sys_country.hn', 'zh_CN', '洪都拉斯', 'system', now(), '??'),
    (310200, 'sys.dict.sys_country.hk', 'en_US', 'Hong Kong', 'system', now(), 'Country'),
    (310201, 'sys.dict.sys_country.hk', 'zh_CN', '香港', 'system', now(), '??'),
    (310202, 'sys.dict.sys_country.hu', 'en_US', 'Hungary', 'system', now(), 'Country'),
    (310203, 'sys.dict.sys_country.hu', 'zh_CN', '匈牙利', 'system', now(), '??'),
    (310204, 'sys.dict.sys_country.is', 'en_US', 'Iceland', 'system', now(), 'Country'),
    (310205, 'sys.dict.sys_country.is', 'zh_CN', '冰岛', 'system', now(), '??'),
    (310206, 'sys.dict.sys_country.in', 'en_US', 'India', 'system', now(), 'Country'),
    (310207, 'sys.dict.sys_country.in', 'zh_CN', '印度', 'system', now(), '??'),
    (310208, 'sys.dict.sys_country.id', 'en_US', 'Indonesia', 'system', now(), 'Country'),
    (310209, 'sys.dict.sys_country.id', 'zh_CN', '印度尼西亚', 'system', now(), '??'),
    (310210, 'sys.dict.sys_country.ir', 'en_US', 'Iran', 'system', now(), 'Country'),
    (310211, 'sys.dict.sys_country.ir', 'zh_CN', '伊朗', 'system', now(), '??'),
    (310212, 'sys.dict.sys_country.iq', 'en_US', 'Iraq', 'system', now(), 'Country'),
    (310213, 'sys.dict.sys_country.iq', 'zh_CN', '伊拉克', 'system', now(), '??'),
    (310214, 'sys.dict.sys_country.ie', 'en_US', 'Ireland', 'system', now(), 'Country'),
    (310215, 'sys.dict.sys_country.ie', 'zh_CN', '爱尔兰', 'system', now(), '??'),
    (310216, 'sys.dict.sys_country.im', 'en_US', 'Isle of Man', 'system', now(), 'Country'),
    (310217, 'sys.dict.sys_country.im', 'zh_CN', '马恩岛', 'system', now(), '??'),
    (310218, 'sys.dict.sys_country.il', 'en_US', 'Israel', 'system', now(), 'Country'),
    (310219, 'sys.dict.sys_country.il', 'zh_CN', '以色列', 'system', now(), '??'),
    (310220, 'sys.dict.sys_country.it', 'en_US', 'Italy', 'system', now(), 'Country'),
    (310221, 'sys.dict.sys_country.it', 'zh_CN', '意大利', 'system', now(), '??'),
    (310222, 'sys.dict.sys_country.ci', 'en_US', 'Ivory Coast', 'system', now(), 'Country'),
    (310223, 'sys.dict.sys_country.ci', 'zh_CN', '科特迪瓦', 'system', now(), '??'),
    (310224, 'sys.dict.sys_country.jm', 'en_US', 'Jamaica', 'system', now(), 'Country'),
    (310225, 'sys.dict.sys_country.jm', 'zh_CN', '牙买加', 'system', now(), '??'),
    (310226, 'sys.dict.sys_country.jp', 'en_US', 'Japan', 'system', now(), 'Country'),
    (310227, 'sys.dict.sys_country.jp', 'zh_CN', '日本', 'system', now(), '??'),
    (310228, 'sys.dict.sys_country.je', 'en_US', 'Jersey', 'system', now(), 'Country'),
    (310229, 'sys.dict.sys_country.je', 'zh_CN', '泽西岛', 'system', now(), '??'),
    (310230, 'sys.dict.sys_country.jo', 'en_US', 'Jordan', 'system', now(), 'Country'),
    (310231, 'sys.dict.sys_country.jo', 'zh_CN', '约旦', 'system', now(), '??'),
    (310232, 'sys.dict.sys_country.kz', 'en_US', 'Kazakhstan', 'system', now(), 'Country'),
    (310233, 'sys.dict.sys_country.kz', 'zh_CN', '哈萨克斯坦', 'system', now(), '??'),
    (310234, 'sys.dict.sys_country.ke', 'en_US', 'Kenya', 'system', now(), 'Country'),
    (310235, 'sys.dict.sys_country.ke', 'zh_CN', '肯尼亚', 'system', now(), '??'),
    (310236, 'sys.dict.sys_country.ki', 'en_US', 'Kiribati', 'system', now(), 'Country'),
    (310237, 'sys.dict.sys_country.ki', 'zh_CN', '基里巴斯', 'system', now(), '??'),
    (310238, 'sys.dict.sys_country.kw', 'en_US', 'Kuwait', 'system', now(), 'Country'),
    (310239, 'sys.dict.sys_country.kw', 'zh_CN', '科威特', 'system', now(), '??'),
    (310240, 'sys.dict.sys_country.kg', 'en_US', 'Kyrgyzstan', 'system', now(), 'Country'),
    (310241, 'sys.dict.sys_country.kg', 'zh_CN', '吉尔吉斯斯坦', 'system', now(), '??'),
    (310242, 'sys.dict.sys_country.la', 'en_US', 'Laos', 'system', now(), 'Country'),
    (310243, 'sys.dict.sys_country.la', 'zh_CN', '老挝', 'system', now(), '??'),
    (310244, 'sys.dict.sys_country.lv', 'en_US', 'Latvia', 'system', now(), 'Country'),
    (310245, 'sys.dict.sys_country.lv', 'zh_CN', '拉脱维亚', 'system', now(), '??'),
    (310246, 'sys.dict.sys_country.lb', 'en_US', 'Lebanon', 'system', now(), 'Country'),
    (310247, 'sys.dict.sys_country.lb', 'zh_CN', '黎巴嫩', 'system', now(), '??'),
    (310248, 'sys.dict.sys_country.ls', 'en_US', 'Lesotho', 'system', now(), 'Country'),
    (310249, 'sys.dict.sys_country.ls', 'zh_CN', '莱索托', 'system', now(), '??'),
    (310250, 'sys.dict.sys_country.lr', 'en_US', 'Liberia', 'system', now(), 'Country'),
    (310251, 'sys.dict.sys_country.lr', 'zh_CN', '利比里亚', 'system', now(), '??'),
    (310252, 'sys.dict.sys_country.ly', 'en_US', 'Libya', 'system', now(), 'Country'),
    (310253, 'sys.dict.sys_country.ly', 'zh_CN', '利比亚', 'system', now(), '??'),
    (310254, 'sys.dict.sys_country.li', 'en_US', 'Liechtenstein', 'system', now(), 'Country'),
    (310255, 'sys.dict.sys_country.li', 'zh_CN', '列支敦士登', 'system', now(), '??'),
    (310256, 'sys.dict.sys_country.lt', 'en_US', 'Lithuania', 'system', now(), 'Country'),
    (310257, 'sys.dict.sys_country.lt', 'zh_CN', '立陶宛', 'system', now(), '??'),
    (310258, 'sys.dict.sys_country.lu', 'en_US', 'Luxembourg', 'system', now(), 'Country'),
    (310259, 'sys.dict.sys_country.lu', 'zh_CN', '卢森堡', 'system', now(), '??'),
    (310260, 'sys.dict.sys_country.mo', 'en_US', 'Macau', 'system', now(), 'Country'),
    (310261, 'sys.dict.sys_country.mo', 'zh_CN', '澳门', 'system', now(), '??'),
    (310262, 'sys.dict.sys_country.mg', 'en_US', 'Madagascar', 'system', now(), 'Country'),
    (310263, 'sys.dict.sys_country.mg', 'zh_CN', '马达加斯加', 'system', now(), '??'),
    (310264, 'sys.dict.sys_country.mw', 'en_US', 'Malawi', 'system', now(), 'Country'),
    (310265, 'sys.dict.sys_country.mw', 'zh_CN', '马拉维', 'system', now(), '??'),
    (310266, 'sys.dict.sys_country.my', 'en_US', 'Malaysia', 'system', now(), 'Country'),
    (310267, 'sys.dict.sys_country.my', 'zh_CN', '马来西亚', 'system', now(), '??'),
    (310268, 'sys.dict.sys_country.mv', 'en_US', 'Maldives', 'system', now(), 'Country'),
    (310269, 'sys.dict.sys_country.mv', 'zh_CN', '马尔代夫', 'system', now(), '??'),
    (310270, 'sys.dict.sys_country.ml', 'en_US', 'Mali', 'system', now(), 'Country'),
    (310271, 'sys.dict.sys_country.ml', 'zh_CN', '马里', 'system', now(), '??'),
    (310272, 'sys.dict.sys_country.mt', 'en_US', 'Malta', 'system', now(), 'Country'),
    (310273, 'sys.dict.sys_country.mt', 'zh_CN', '马耳他', 'system', now(), '??'),
    (310274, 'sys.dict.sys_country.mh', 'en_US', 'Marshall Islands', 'system', now(), 'Country'),
    (310275, 'sys.dict.sys_country.mh', 'zh_CN', '马绍尔群岛', 'system', now(), '??'),
    (310276, 'sys.dict.sys_country.mq', 'en_US', 'Martinique', 'system', now(), 'Country'),
    (310277, 'sys.dict.sys_country.mq', 'zh_CN', '马提尼克', 'system', now(), '??'),
    (310278, 'sys.dict.sys_country.mr', 'en_US', 'Mauritania', 'system', now(), 'Country'),
    (310279, 'sys.dict.sys_country.mr', 'zh_CN', '毛里塔尼亚', 'system', now(), '??'),
    (310280, 'sys.dict.sys_country.mu', 'en_US', 'Mauritius', 'system', now(), 'Country'),
    (310281, 'sys.dict.sys_country.mu', 'zh_CN', '毛里求斯', 'system', now(), '??'),
    (310282, 'sys.dict.sys_country.yt', 'en_US', 'Mayotte', 'system', now(), 'Country'),
    (310283, 'sys.dict.sys_country.yt', 'zh_CN', '马约特', 'system', now(), '??'),
    (310284, 'sys.dict.sys_country.fm', 'en_US', 'Micronesia', 'system', now(), 'Country'),
    (310285, 'sys.dict.sys_country.fm', 'zh_CN', '密克罗尼西亚', 'system', now(), '??'),
    (310286, 'sys.dict.sys_country.md', 'en_US', 'Moldova', 'system', now(), 'Country'),
    (310287, 'sys.dict.sys_country.md', 'zh_CN', '摩尔多瓦', 'system', now(), '??'),
    (310288, 'sys.dict.sys_country.mc', 'en_US', 'Monaco', 'system', now(), 'Country'),
    (310289, 'sys.dict.sys_country.mc', 'zh_CN', '摩纳哥', 'system', now(), '??'),
    (310290, 'sys.dict.sys_country.mn', 'en_US', 'Mongolia', 'system', now(), 'Country'),
    (310291, 'sys.dict.sys_country.mn', 'zh_CN', '蒙古', 'system', now(), '??'),
    (310292, 'sys.dict.sys_country.me', 'en_US', 'Montenegro', 'system', now(), 'Country'),
    (310293, 'sys.dict.sys_country.me', 'zh_CN', '黑山', 'system', now(), '??'),
    (310294, 'sys.dict.sys_country.ms', 'en_US', 'Montserrat', 'system', now(), 'Country'),
    (310295, 'sys.dict.sys_country.ms', 'zh_CN', '蒙特塞拉特', 'system', now(), '??'),
    (310296, 'sys.dict.sys_country.ma', 'en_US', 'Morocco', 'system', now(), 'Country'),
    (310297, 'sys.dict.sys_country.ma', 'zh_CN', '摩洛哥', 'system', now(), '??'),
    (310298, 'sys.dict.sys_country.mz', 'en_US', 'Mozambique', 'system', now(), 'Country'),
    (310299, 'sys.dict.sys_country.mz', 'zh_CN', '莫桑比克', 'system', now(), '??'),
    (310300, 'sys.dict.sys_country.mm', 'en_US', 'Myanmar', 'system', now(), 'Country'),
    (310301, 'sys.dict.sys_country.mm', 'zh_CN', '缅甸', 'system', now(), '??'),
    (310302, 'sys.dict.sys_country.na', 'en_US', 'Namibia', 'system', now(), 'Country'),
    (310303, 'sys.dict.sys_country.na', 'zh_CN', '纳米比亚', 'system', now(), '??'),
    (310304, 'sys.dict.sys_country.nr', 'en_US', 'Nauru', 'system', now(), 'Country'),
    (310305, 'sys.dict.sys_country.nr', 'zh_CN', '瑙鲁', 'system', now(), '??'),
    (310306, 'sys.dict.sys_country.np', 'en_US', 'Nepal', 'system', now(), 'Country'),
    (310307, 'sys.dict.sys_country.np', 'zh_CN', '尼泊尔', 'system', now(), '??'),
    (310308, 'sys.dict.sys_country.nl', 'en_US', 'Netherlands', 'system', now(), 'Country'),
    (310309, 'sys.dict.sys_country.nl', 'zh_CN', '荷兰', 'system', now(), '??'),
    (310310, 'sys.dict.sys_country.nc', 'en_US', 'New Caledonia', 'system', now(), 'Country'),
    (310311, 'sys.dict.sys_country.nc', 'zh_CN', '新喀里多尼亚', 'system', now(), '??'),
    (310312, 'sys.dict.sys_country.nz', 'en_US', 'New Zealand', 'system', now(), 'Country'),
    (310313, 'sys.dict.sys_country.nz', 'zh_CN', '新西兰', 'system', now(), '??'),
    (310314, 'sys.dict.sys_country.ni', 'en_US', 'Nicaragua', 'system', now(), 'Country'),
    (310315, 'sys.dict.sys_country.ni', 'zh_CN', '尼加拉瓜', 'system', now(), '??'),
    (310316, 'sys.dict.sys_country.ne', 'en_US', 'Niger', 'system', now(), 'Country'),
    (310317, 'sys.dict.sys_country.ne', 'zh_CN', '尼日尔', 'system', now(), '??'),
    (310318, 'sys.dict.sys_country.ng', 'en_US', 'Nigeria', 'system', now(), 'Country'),
    (310319, 'sys.dict.sys_country.ng', 'zh_CN', '尼日利亚', 'system', now(), '??'),
    (310320, 'sys.dict.sys_country.nu', 'en_US', 'Niue', 'system', now(), 'Country'),
    (310321, 'sys.dict.sys_country.nu', 'zh_CN', '纽埃', 'system', now(), '??'),
    (310322, 'sys.dict.sys_country.nf', 'en_US', 'Norfolk Island', 'system', now(), 'Country'),
    (310323, 'sys.dict.sys_country.nf', 'zh_CN', '诺福克岛', 'system', now(), '??'),
    (310324, 'sys.dict.sys_country.kp', 'en_US', 'North Korea', 'system', now(), 'Country'),
    (310325, 'sys.dict.sys_country.kp', 'zh_CN', '朝鲜', 'system', now(), '??'),
    (310326, 'sys.dict.sys_country.mk', 'en_US', 'North Macedonia', 'system', now(), 'Country'),
    (310327, 'sys.dict.sys_country.mk', 'zh_CN', '北馬其頓', 'system', now(), '??'),
    (310328, 'sys.dict.sys_country.mp', 'en_US', 'Northern Mariana Islands', 'system', now(), 'Country'),
    (310329, 'sys.dict.sys_country.mp', 'zh_CN', '北马里亚纳群岛', 'system', now(), '??'),
    (310330, 'sys.dict.sys_country.no', 'en_US', 'Norway', 'system', now(), 'Country'),
    (310331, 'sys.dict.sys_country.no', 'zh_CN', '挪威', 'system', now(), '??'),
    (310332, 'sys.dict.sys_country.om', 'en_US', 'Oman', 'system', now(), 'Country'),
    (310333, 'sys.dict.sys_country.om', 'zh_CN', '阿曼', 'system', now(), '??'),
    (310334, 'sys.dict.sys_country.pk', 'en_US', 'Pakistan', 'system', now(), 'Country'),
    (310335, 'sys.dict.sys_country.pk', 'zh_CN', '巴基斯坦', 'system', now(), '??'),
    (310336, 'sys.dict.sys_country.pw', 'en_US', 'Palau', 'system', now(), 'Country'),
    (310337, 'sys.dict.sys_country.pw', 'zh_CN', '帕劳', 'system', now(), '??'),
    (310338, 'sys.dict.sys_country.ps', 'en_US', 'Palestine', 'system', now(), 'Country'),
    (310339, 'sys.dict.sys_country.ps', 'zh_CN', '巴勒斯坦', 'system', now(), '??'),
    (310340, 'sys.dict.sys_country.pa', 'en_US', 'Panama', 'system', now(), 'Country'),
    (310341, 'sys.dict.sys_country.pa', 'zh_CN', '巴拿马', 'system', now(), '??'),
    (310342, 'sys.dict.sys_country.pg', 'en_US', 'Papua New Guinea', 'system', now(), 'Country'),
    (310343, 'sys.dict.sys_country.pg', 'zh_CN', '巴布亚新几内亚', 'system', now(), '??'),
    (310344, 'sys.dict.sys_country.py', 'en_US', 'Paraguay', 'system', now(), 'Country'),
    (310345, 'sys.dict.sys_country.py', 'zh_CN', '巴拉圭', 'system', now(), '??'),
    (310346, 'sys.dict.sys_country.pe', 'en_US', 'Peru', 'system', now(), 'Country'),
    (310347, 'sys.dict.sys_country.pe', 'zh_CN', '秘鲁', 'system', now(), '??'),
    (310348, 'sys.dict.sys_country.ph', 'en_US', 'Philippines', 'system', now(), 'Country'),
    (310349, 'sys.dict.sys_country.ph', 'zh_CN', '菲律宾', 'system', now(), '??'),
    (310350, 'sys.dict.sys_country.pn', 'en_US', 'Pitcairn Islands', 'system', now(), 'Country'),
    (310351, 'sys.dict.sys_country.pn', 'zh_CN', '皮特凯恩群岛', 'system', now(), '??'),
    (310352, 'sys.dict.sys_country.pl', 'en_US', 'Poland', 'system', now(), 'Country'),
    (310353, 'sys.dict.sys_country.pl', 'zh_CN', '波兰', 'system', now(), '??'),
    (310354, 'sys.dict.sys_country.pt', 'en_US', 'Portugal', 'system', now(), 'Country'),
    (310355, 'sys.dict.sys_country.pt', 'zh_CN', '葡萄牙', 'system', now(), '??'),
    (310356, 'sys.dict.sys_country.pr', 'en_US', 'Puerto Rico', 'system', now(), 'Country'),
    (310357, 'sys.dict.sys_country.pr', 'zh_CN', '波多黎各', 'system', now(), '??'),
    (310358, 'sys.dict.sys_country.qa', 'en_US', 'Qatar', 'system', now(), 'Country'),
    (310359, 'sys.dict.sys_country.qa', 'zh_CN', '卡塔尔', 'system', now(), '??'),
    (310360, 'sys.dict.sys_country.cg', 'en_US', 'Republic of the Congo', 'system', now(), 'Country'),
    (310361, 'sys.dict.sys_country.cg', 'zh_CN', '刚果', 'system', now(), '??'),
    (310362, 'sys.dict.sys_country.re', 'en_US', 'Réunion', 'system', now(), 'Country'),
    (310363, 'sys.dict.sys_country.re', 'zh_CN', '留尼旺岛', 'system', now(), '??'),
    (310364, 'sys.dict.sys_country.ro', 'en_US', 'Romania', 'system', now(), 'Country'),
    (310365, 'sys.dict.sys_country.ro', 'zh_CN', '罗马尼亚', 'system', now(), '??'),
    (310366, 'sys.dict.sys_country.ru', 'en_US', 'Russia', 'system', now(), 'Country'),
    (310367, 'sys.dict.sys_country.ru', 'zh_CN', '俄罗斯', 'system', now(), '??'),
    (310368, 'sys.dict.sys_country.rw', 'en_US', 'Rwanda', 'system', now(), 'Country'),
    (310369, 'sys.dict.sys_country.rw', 'zh_CN', '卢旺达', 'system', now(), '??'),
    (310370, 'sys.dict.sys_country.bl', 'en_US', 'Saint Barthélemy', 'system', now(), 'Country'),
    (310371, 'sys.dict.sys_country.bl', 'zh_CN', '圣巴泰勒米', 'system', now(), '??'),
    (310372, 'sys.dict.sys_country.sh', 'en_US', 'Saint Helena, Ascension and Tristan da Cunha', 'system', now(), 'Country'),
    (310373, 'sys.dict.sys_country.sh', 'zh_CN', '圣赫勒拿、阿森松和特里斯坦-达库尼亚', 'system', now(), '??'),
    (310374, 'sys.dict.sys_country.kn', 'en_US', 'Saint Kitts and Nevis', 'system', now(), 'Country'),
    (310375, 'sys.dict.sys_country.kn', 'zh_CN', '圣基茨和尼维斯', 'system', now(), '??'),
    (310376, 'sys.dict.sys_country.lc', 'en_US', 'Saint Lucia', 'system', now(), 'Country'),
    (310377, 'sys.dict.sys_country.lc', 'zh_CN', '圣卢西亚', 'system', now(), '??'),
    (310378, 'sys.dict.sys_country.mf', 'en_US', 'Saint Martin', 'system', now(), 'Country'),
    (310379, 'sys.dict.sys_country.mf', 'zh_CN', '圣马丁', 'system', now(), '??'),
    (310380, 'sys.dict.sys_country.pm', 'en_US', 'Saint Pierre and Miquelon', 'system', now(), 'Country'),
    (310381, 'sys.dict.sys_country.pm', 'zh_CN', '圣皮埃尔和密克隆', 'system', now(), '??'),
    (310382, 'sys.dict.sys_country.vc', 'en_US', 'Saint Vincent and the Grenadines', 'system', now(), 'Country'),
    (310383, 'sys.dict.sys_country.vc', 'zh_CN', '圣文森特和格林纳丁斯', 'system', now(), '??'),
    (310384, 'sys.dict.sys_country.ws', 'en_US', 'Samoa', 'system', now(), 'Country'),
    (310385, 'sys.dict.sys_country.ws', 'zh_CN', '萨摩亚', 'system', now(), '??'),
    (310386, 'sys.dict.sys_country.sm', 'en_US', 'San Marino', 'system', now(), 'Country'),
    (310387, 'sys.dict.sys_country.sm', 'zh_CN', '圣马力诺', 'system', now(), '??'),
    (310388, 'sys.dict.sys_country.st', 'en_US', 'São Tomé and Príncipe', 'system', now(), 'Country'),
    (310389, 'sys.dict.sys_country.st', 'zh_CN', '圣多美和普林西比', 'system', now(), '??'),
    (310390, 'sys.dict.sys_country.sa', 'en_US', 'Saudi Arabia', 'system', now(), 'Country'),
    (310391, 'sys.dict.sys_country.sa', 'zh_CN', '沙特阿拉伯', 'system', now(), '??'),
    (310392, 'sys.dict.sys_country.sn', 'en_US', 'Senegal', 'system', now(), 'Country'),
    (310393, 'sys.dict.sys_country.sn', 'zh_CN', '塞内加尔', 'system', now(), '??'),
    (310394, 'sys.dict.sys_country.rs', 'en_US', 'Serbia', 'system', now(), 'Country'),
    (310395, 'sys.dict.sys_country.rs', 'zh_CN', '塞尔维亚', 'system', now(), '??'),
    (310396, 'sys.dict.sys_country.sc', 'en_US', 'Seychelles', 'system', now(), 'Country'),
    (310397, 'sys.dict.sys_country.sc', 'zh_CN', '塞舌尔', 'system', now(), '??'),
    (310398, 'sys.dict.sys_country.sl', 'en_US', 'Sierra Leone', 'system', now(), 'Country'),
    (310399, 'sys.dict.sys_country.sl', 'zh_CN', '塞拉利昂', 'system', now(), '??'),
    (310400, 'sys.dict.sys_country.sg', 'en_US', 'Singapore', 'system', now(), 'Country'),
    (310401, 'sys.dict.sys_country.sg', 'zh_CN', '新加坡', 'system', now(), '??'),
    (310402, 'sys.dict.sys_country.sx', 'en_US', 'Sint Maarten', 'system', now(), 'Country'),
    (310403, 'sys.dict.sys_country.sx', 'zh_CN', '圣马丁岛', 'system', now(), '??'),
    (310404, 'sys.dict.sys_country.sk', 'en_US', 'Slovakia', 'system', now(), 'Country'),
    (310405, 'sys.dict.sys_country.sk', 'zh_CN', '斯洛伐克', 'system', now(), '??'),
    (310406, 'sys.dict.sys_country.si', 'en_US', 'Slovenia', 'system', now(), 'Country'),
    (310407, 'sys.dict.sys_country.si', 'zh_CN', '斯洛文尼亚', 'system', now(), '??'),
    (310408, 'sys.dict.sys_country.sb', 'en_US', 'Solomon Islands', 'system', now(), 'Country'),
    (310409, 'sys.dict.sys_country.sb', 'zh_CN', '所罗门群岛', 'system', now(), '??'),
    (310410, 'sys.dict.sys_country.so', 'en_US', 'Somalia', 'system', now(), 'Country'),
    (310411, 'sys.dict.sys_country.so', 'zh_CN', '索马里', 'system', now(), '??'),
    (310412, 'sys.dict.sys_country.za', 'en_US', 'South Africa', 'system', now(), 'Country'),
    (310413, 'sys.dict.sys_country.za', 'zh_CN', '南非', 'system', now(), '??'),
    (310414, 'sys.dict.sys_country.gs', 'en_US', 'South Georgia', 'system', now(), 'Country'),
    (310415, 'sys.dict.sys_country.gs', 'zh_CN', '南乔治亚', 'system', now(), '??'),
    (310416, 'sys.dict.sys_country.kr', 'en_US', 'South Korea', 'system', now(), 'Country'),
    (310417, 'sys.dict.sys_country.kr', 'zh_CN', '韩国', 'system', now(), '??'),
    (310418, 'sys.dict.sys_country.ss', 'en_US', 'South Sudan', 'system', now(), 'Country'),
    (310419, 'sys.dict.sys_country.ss', 'zh_CN', '南苏丹', 'system', now(), '??'),
    (310420, 'sys.dict.sys_country.es', 'en_US', 'Spain', 'system', now(), 'Country'),
    (310421, 'sys.dict.sys_country.es', 'zh_CN', '西班牙', 'system', now(), '??'),
    (310422, 'sys.dict.sys_country.lk', 'en_US', 'Sri Lanka', 'system', now(), 'Country'),
    (310423, 'sys.dict.sys_country.lk', 'zh_CN', '斯里兰卡', 'system', now(), '??'),
    (310424, 'sys.dict.sys_country.sd', 'en_US', 'Sudan', 'system', now(), 'Country'),
    (310425, 'sys.dict.sys_country.sd', 'zh_CN', '苏丹', 'system', now(), '??'),
    (310426, 'sys.dict.sys_country.sr', 'en_US', 'Suriname', 'system', now(), 'Country'),
    (310427, 'sys.dict.sys_country.sr', 'zh_CN', '苏里南', 'system', now(), '??'),
    (310428, 'sys.dict.sys_country.sj', 'en_US', 'Svalbard and Jan Mayen', 'system', now(), 'Country'),
    (310429, 'sys.dict.sys_country.sj', 'zh_CN', '斯瓦尔巴特', 'system', now(), '??'),
    (310430, 'sys.dict.sys_country.se', 'en_US', 'Sweden', 'system', now(), 'Country'),
    (310431, 'sys.dict.sys_country.se', 'zh_CN', '瑞典', 'system', now(), '??'),
    (310432, 'sys.dict.sys_country.ch', 'en_US', 'Switzerland', 'system', now(), 'Country'),
    (310433, 'sys.dict.sys_country.ch', 'zh_CN', '瑞士', 'system', now(), '??'),
    (310434, 'sys.dict.sys_country.sy', 'en_US', 'Syria', 'system', now(), 'Country'),
    (310435, 'sys.dict.sys_country.sy', 'zh_CN', '叙利亚', 'system', now(), '??'),
    (310436, 'sys.dict.sys_country.tw', 'en_US', 'Taiwan', 'system', now(), 'Country'),
    (310437, 'sys.dict.sys_country.tw', 'zh_CN', '台灣', 'system', now(), '??'),
    (310438, 'sys.dict.sys_country.tj', 'en_US', 'Tajikistan', 'system', now(), 'Country'),
    (310439, 'sys.dict.sys_country.tj', 'zh_CN', '塔吉克斯坦', 'system', now(), '??'),
    (310440, 'sys.dict.sys_country.tz', 'en_US', 'Tanzania', 'system', now(), 'Country'),
    (310441, 'sys.dict.sys_country.tz', 'zh_CN', '坦桑尼亚', 'system', now(), '??'),
    (310442, 'sys.dict.sys_country.th', 'en_US', 'Thailand', 'system', now(), 'Country'),
    (310443, 'sys.dict.sys_country.th', 'zh_CN', '泰国', 'system', now(), '??'),
    (310444, 'sys.dict.sys_country.tl', 'en_US', 'Timor-Leste', 'system', now(), 'Country'),
    (310445, 'sys.dict.sys_country.tl', 'zh_CN', '东帝汶', 'system', now(), '??'),
    (310446, 'sys.dict.sys_country.tg', 'en_US', 'Togo', 'system', now(), 'Country'),
    (310447, 'sys.dict.sys_country.tg', 'zh_CN', '多哥', 'system', now(), '??'),
    (310448, 'sys.dict.sys_country.tk', 'en_US', 'Tokelau', 'system', now(), 'Country'),
    (310449, 'sys.dict.sys_country.tk', 'zh_CN', '托克劳', 'system', now(), '??'),
    (310450, 'sys.dict.sys_country.to', 'en_US', 'Tonga', 'system', now(), 'Country'),
    (310451, 'sys.dict.sys_country.to', 'zh_CN', '汤加', 'system', now(), '??'),
    (310452, 'sys.dict.sys_country.tt', 'en_US', 'Trinidad and Tobago', 'system', now(), 'Country'),
    (310453, 'sys.dict.sys_country.tt', 'zh_CN', '特立尼达和多巴哥', 'system', now(), '??'),
    (310454, 'sys.dict.sys_country.tn', 'en_US', 'Tunisia', 'system', now(), 'Country'),
    (310455, 'sys.dict.sys_country.tn', 'zh_CN', '突尼斯', 'system', now(), '??'),
    (310456, 'sys.dict.sys_country.tr', 'en_US', 'Türkiye', 'system', now(), 'Country'),
    (310457, 'sys.dict.sys_country.tr', 'zh_CN', '土耳其', 'system', now(), '??'),
    (310458, 'sys.dict.sys_country.tm', 'en_US', 'Turkmenistan', 'system', now(), 'Country'),
    (310459, 'sys.dict.sys_country.tm', 'zh_CN', '土库曼斯坦', 'system', now(), '??'),
    (310460, 'sys.dict.sys_country.tc', 'en_US', 'Turks and Caicos Islands', 'system', now(), 'Country'),
    (310461, 'sys.dict.sys_country.tc', 'zh_CN', '特克斯和凯科斯群岛', 'system', now(), '??'),
    (310462, 'sys.dict.sys_country.tv', 'en_US', 'Tuvalu', 'system', now(), 'Country'),
    (310463, 'sys.dict.sys_country.tv', 'zh_CN', '图瓦卢', 'system', now(), '??'),
    (310464, 'sys.dict.sys_country.ug', 'en_US', 'Uganda', 'system', now(), 'Country'),
    (310465, 'sys.dict.sys_country.ug', 'zh_CN', '乌干达', 'system', now(), '??'),
    (310466, 'sys.dict.sys_country.ua', 'en_US', 'Ukraine', 'system', now(), 'Country'),
    (310467, 'sys.dict.sys_country.ua', 'zh_CN', '乌克兰', 'system', now(), '??'),
    (310468, 'sys.dict.sys_country.ae', 'en_US', 'United Arab Emirates', 'system', now(), 'Country'),
    (310469, 'sys.dict.sys_country.ae', 'zh_CN', '阿拉伯联合酋长国', 'system', now(), '??'),
    (310470, 'sys.dict.sys_country.gb', 'en_US', 'United Kingdom', 'system', now(), 'Country'),
    (310471, 'sys.dict.sys_country.gb', 'zh_CN', '英国', 'system', now(), '??'),
    (310472, 'sys.dict.sys_country.um', 'en_US', 'United States Minor Outlying Islands', 'system', now(), 'Country'),
    (310473, 'sys.dict.sys_country.um', 'zh_CN', '美国本土外小岛屿', 'system', now(), '??'),
    (310474, 'sys.dict.sys_country.vi', 'en_US', 'United States Virgin Islands', 'system', now(), 'Country'),
    (310475, 'sys.dict.sys_country.vi', 'zh_CN', '美属维尔京群岛', 'system', now(), '??'),
    (310476, 'sys.dict.sys_country.uy', 'en_US', 'Uruguay', 'system', now(), 'Country'),
    (310477, 'sys.dict.sys_country.uy', 'zh_CN', '乌拉圭', 'system', now(), '??'),
    (310478, 'sys.dict.sys_country.uz', 'en_US', 'Uzbekistan', 'system', now(), 'Country'),
    (310479, 'sys.dict.sys_country.uz', 'zh_CN', '乌兹别克斯坦', 'system', now(), '??'),
    (310480, 'sys.dict.sys_country.vu', 'en_US', 'Vanuatu', 'system', now(), 'Country'),
    (310481, 'sys.dict.sys_country.vu', 'zh_CN', '瓦努阿图', 'system', now(), '??'),
    (310482, 'sys.dict.sys_country.va', 'en_US', 'Vatican City', 'system', now(), 'Country'),
    (310483, 'sys.dict.sys_country.va', 'zh_CN', '梵蒂冈', 'system', now(), '??'),
    (310484, 'sys.dict.sys_country.ve', 'en_US', 'Venezuela', 'system', now(), 'Country'),
    (310485, 'sys.dict.sys_country.ve', 'zh_CN', '委内瑞拉', 'system', now(), '??'),
    (310486, 'sys.dict.sys_country.vn', 'en_US', 'Vietnam', 'system', now(), 'Country'),
    (310487, 'sys.dict.sys_country.vn', 'zh_CN', '越南', 'system', now(), '??'),
    (310488, 'sys.dict.sys_country.wf', 'en_US', 'Wallis and Futuna', 'system', now(), 'Country'),
    (310489, 'sys.dict.sys_country.wf', 'zh_CN', '瓦利斯和富图纳群岛', 'system', now(), '??'),
    (310490, 'sys.dict.sys_country.eh', 'en_US', 'Western Sahara', 'system', now(), 'Country'),
    (310491, 'sys.dict.sys_country.eh', 'zh_CN', '西撒哈拉', 'system', now(), '??'),
    (310492, 'sys.dict.sys_country.ye', 'en_US', 'Yemen', 'system', now(), 'Country'),
    (310493, 'sys.dict.sys_country.ye', 'zh_CN', '也门', 'system', now(), '??'),
    (310494, 'sys.dict.sys_country.zm', 'en_US', 'Zambia', 'system', now(), 'Country'),
    (310495, 'sys.dict.sys_country.zm', 'zh_CN', '赞比亚', 'system', now(), '??'),
    (310496, 'sys.dict.sys_country.zw', 'en_US', 'Zimbabwe', 'system', now(), 'Country'),
    (310497, 'sys.dict.sys_country.zw', 'zh_CN', '津巴布韦', 'system', now(), '??'),
    (19209, 'sys.dict.sys_user_sex.male', 'en_US', 'Male', 'system', now(), 'User gender'),
    (19210, 'sys.dict.sys_user_sex.male', 'zh_CN', U&'\7537', 'system', now(), 'User gender'),
    (19211, 'sys.dict.sys_user_sex.female', 'en_US', 'Female', 'system', now(), 'User gender'),
    (19212, 'sys.dict.sys_user_sex.female', 'zh_CN', U&'\5973', 'system', now(), 'User gender'),
    (19213, 'sys.dict.sys_user_sex.unknown', 'en_US', 'Unknown', 'system', now(), 'User gender'),
    (19214, 'sys.dict.sys_user_sex.unknown', 'zh_CN', U&'\672A\77E5', 'system', now(), 'User gender')
ON CONFLICT (message_key, locale) DO UPDATE
SET message_value = EXCLUDED.message_value,
    update_by = 'system',
    update_time = now();

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
