-- base-boot PostgreSQL baseline for dealer.
-- Platform/factory tenant is tenant_id = 0.

CREATE TABLE IF NOT EXISTS sys_tenant (
    tenant_id bigint PRIMARY KEY,
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
    tenant_id bigint,
    merchant_name varchar(100) NOT NULL,
    contact_name varchar(50),
    email varchar(100) NOT NULL,
    country varchar(50),
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

CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 0,
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
    tenant_id bigint NOT NULL DEFAULT 0,
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
    tenant_id bigint NOT NULL DEFAULT 0,
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
    tenant_id bigint NOT NULL DEFAULT 0,
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
    tenant_id bigint NOT NULL DEFAULT 0,
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
    tenant_id bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, role_id)
);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_role_user ON sys_user_role (role_id, user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_role_tenant_user ON sys_user_role (tenant_id, user_id, role_id);

CREATE TABLE IF NOT EXISTS sys_user_post (
    user_id bigint NOT NULL,
    post_id bigint NOT NULL,
    tenant_id bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, post_id)
);
CREATE INDEX IF NOT EXISTS idx_sys_user_post_post_user ON sys_user_post (post_id, user_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_post_tenant_user ON sys_user_post (tenant_id, user_id, post_id);

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id bigint NOT NULL,
    menu_id bigint NOT NULL,
    tenant_id bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (role_id, menu_id)
);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_menu_role ON sys_role_menu (menu_id, role_id);
CREATE INDEX IF NOT EXISTS idx_sys_role_menu_tenant_role ON sys_role_menu (tenant_id, role_id, menu_id);

CREATE TABLE IF NOT EXISTS sys_role_dept (
    role_id bigint NOT NULL,
    dept_id bigint NOT NULL,
    tenant_id bigint NOT NULL DEFAULT 0,
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
    tenant_id bigint NOT NULL DEFAULT 0,
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
COMMENT ON COLUMN sys_tenant.tenant_id IS '租户ID，平台/厂家固定为0';
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
COMMENT ON COLUMN sys_tenant_apply.contact_name IS '联系人姓名';
COMMENT ON COLUMN sys_tenant_apply.email IS '登录邮箱/联系邮箱';
COMMENT ON COLUMN sys_tenant_apply.country IS '国家或地区';
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
VALUES (0, 'Platform Factory', 'PLATFORM', 'admin@example.com', '1', 'system', now(), 'Factory/platform tenant')
ON CONFLICT (tenant_id) DO NOTHING;

INSERT INTO sys_dept (dept_id, tenant_id, parent_id, ancestors, dept_name, order_num, status, del_flag, create_by, create_time)
VALUES (100, 0, 0, '0', 'Platform', 0, '1', '0', 'system', now())
ON CONFLICT (dept_id) DO NOTHING;

INSERT INTO sys_user (user_id, tenant_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, password, status, del_flag, create_by, create_time, remark)
VALUES (1, 0, 100, 'admin', 'System Admin', 'sys_user', 'admin@example.com', '18888888888', '0',
        '$2a$10$gkt8GIcTlW28k3a.osOvQus81YBcY9JHr7zLqaaknk4O2x9xX/JMm', '1', '0', 'system', now(), 'Platform administrator')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO sys_role (role_id, tenant_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES
    (1, 0, 'Super Admin', 'admin', 1, '1', true, true, '1', '0', 'system', now(), 'Platform super admin'),
    (2, 0, 'Merchant Admin', 'merchant_admin', 2, '1', true, true, '1', '0', 'system', now(), 'Default merchant admin role')
ON CONFLICT (role_id) DO NOTHING;

INSERT INTO sys_user_role (user_id, role_id, tenant_id)
VALUES (1, 1, 0)
ON CONFLICT DO NOTHING;

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
    (1, 0, 'System', 'sys.menu.system', 0, 100, 'system', NULL, '1', '0', 'M', '1', '1', NULL, 'system', 'system', now(), 'System menu'),
    (100, 0, 'User', 'sys.menu.system.user', 1, 1, 'user', 'system/user/index', '1', '0', 'C', '1', '1', 'system:user:list', 'user', 'system', now(), 'User management'),
    (101, 0, 'Role', 'sys.menu.system.role', 1, 2, 'role', 'system/role/index', '1', '0', 'C', '1', '1', 'system:role:list', 'peoples', 'system', now(), 'Role management'),
    (102, 0, 'Menu', 'sys.menu.system.menu', 1, 3, 'menu', 'system/menu/index', '1', '0', 'C', '1', '1', 'system:menu:list', 'tree-table', 'system', now(), 'Menu management'),
    (103, 0, 'Dept', 'sys.menu.system.dept', 1, 4, 'dept', 'system/dept/index', '1', '0', 'C', '1', '1', 'system:dept:list', 'tree', 'system', now(), 'Dept management'),
    (104, 0, 'Post', 'sys.menu.system.post', 1, 5, 'post', 'system/post/index', '1', '0', 'C', '1', '1', 'system:post:list', 'post', 'system', now(), 'Post management'),
    (105, 0, 'Dict', 'sys.menu.system.dict', 1, 6, 'dict', 'system/dict/index', '1', '0', 'C', '1', '1', 'system:dict:list', 'dict', 'system', now(), 'Dict management'),
    (106, 0, 'Config', 'sys.menu.system.config', 1, 7, 'config', 'system/config/index', '1', '0', 'C', '1', '1', 'system:config:list', 'edit', 'system', now(), 'Config management'),
    (107, 0, 'Merchant Audit', 'sys.menu.system.merchantAudit', 1, 8, 'tenantApplication', 'system/tenant/applications', '1', '0', 'C', '1', '1', 'system:tenant:application:list', 'peoples', 'system', now(), 'Merchant tenant audit'),
    (1001, 0, 'Tenant Application Query', 'sys.menu.system.merchantAudit.query', 107, 1, '#', '', '1', '0', 'F', '1', '1', 'system:tenant:application:query', '#', 'system', now(), ''),
    (1002, 0, 'Tenant Application Audit', 'sys.menu.system.merchantAudit.audit', 107, 2, '#', '', '1', '0', 'F', '1', '1', 'system:tenant:application:audit', '#', 'system', now(), '')
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
UPDATE sys_menu SET i18n_key = 'sys.menu.system.merchantAudit.query' WHERE menu_id = 1001 AND i18n_key IS NULL;
UPDATE sys_menu SET i18n_key = 'sys.menu.system.merchantAudit.audit' WHERE menu_id = 1002 AND i18n_key IS NULL;

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
    (9120, 'sys.menu.log.logininfor', 'zh_CN', '登录日志', 'system', now(), '基础菜单')
ON CONFLICT (message_key, locale) DO UPDATE
SET message_value = EXCLUDED.message_value,
    update_by = 'system',
    update_time = now();

INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (120, 0, 'Notice', 'sys.menu.system.notice', 1, 9, 'notice', 'system/notice/index', '1', '0', 'C', '1', '1', 'system:notice:list', 'message', 'system', now(), 'Notice management'),
    (121, 0, 'File', 'sys.menu.system.oss', 1, 10, 'oss', 'system/oss/index', '1', '0', 'C', '1', '1', 'system:oss:list', 'upload', 'system', now(), 'File management'),
    (200, 0, 'Monitor', 'sys.menu.monitor', 0, 120, 'monitor', NULL, '1', '0', 'M', '1', '1', NULL, 'monitor', 'system', now(), 'System monitor'),
    (201, 0, 'Online Users', 'sys.menu.monitor.online', 200, 1, 'online', 'monitor/online/index', '1', '0', 'C', '1', '1', 'monitor:online:list', 'online', 'system', now(), 'Online users'),
    (202, 0, 'Cache Monitor', 'sys.menu.monitor.cache', 200, 2, 'cache', 'monitor/cache/index', '1', '0', 'C', '1', '1', 'monitor:cache:list', 'redis', 'system', now(), 'Cache monitor'),
    (203, 0, 'Cache List', 'sys.menu.monitor.cacheList', 200, 3, 'cacheList', 'monitor/cache/list', '1', '0', 'C', '1', '1', 'monitor:cache:list', 'redis-list', 'system', now(), 'Cache list'),
    (300, 0, 'Code Generator', 'sys.menu.tool.gen', 0, 130, '/gen', 'tool/gen/index', '1', '0', 'C', '1', '1', 'tool:gen:list', 'code', 'system', now(), 'Code generator'),
    (400, 0, 'Logs', 'sys.menu.log', 0, 140, 'log', NULL, '1', '0', 'M', '1', '1', NULL, 'log', 'system', now(), 'Log management'),
    (401, 0, 'Operation Logs', 'sys.menu.log.operlog', 400, 1, 'operlog', 'monitor/operlog/index', '1', '0', 'C', '1', '1', 'monitor:operlog:list', 'form', 'system', now(), 'Operation logs'),
    (402, 0, 'Login Logs', 'sys.menu.log.logininfor', 400, 2, 'logininfor', 'monitor/logininfor/index', '1', '0', 'C', '1', '1', 'monitor:logininfor:list', 'logininfor', 'system', now(), 'Login logs')
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
    update_by = 'system',
    update_time = now();

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT 1, menu_id, 0 FROM sys_menu
ON CONFLICT DO NOTHING;
