-- 共享产品能力中心 Batch 1 PostgreSQL 草案。
-- 本文件用于开发评审和初始化草案，不会自动执行。
-- 基础资料段以 PostgreSQL 为准，时间字段统一使用 UTC 语义 timestamptz。

-- 旧产品工程/配置/报价/发布域已退场，初始化时主动清理旧结构。
DROP TABLE IF EXISTS pc_config_option CASCADE;
DROP TABLE IF EXISTS pc_config_question CASCADE;
DROP TABLE IF EXISTS pc_config_rule CASCADE;
DROP TABLE IF EXISTS pc_config_template_version CASCADE;
DROP TABLE IF EXISTS pc_config_template CASCADE;
DROP TABLE IF EXISTS pc_engineering_check_case CASCADE;
DROP TABLE IF EXISTS pc_engineering_item_scope CASCADE;
DROP TABLE IF EXISTS pc_engineering_item CASCADE;
DROP TABLE IF EXISTS pc_engineering_output_rule CASCADE;
DROP TABLE IF EXISTS pc_engineering_plan_version CASCADE;
DROP TABLE IF EXISTS pc_engineering_plan CASCADE;
DROP TABLE IF EXISTS pc_engineering_rule CASCADE;
DROP TABLE IF EXISTS pc_price_plan_version CASCADE;
DROP TABLE IF EXISTS pc_price_plan CASCADE;
DROP TABLE IF EXISTS pc_price_rule_item CASCADE;
DROP TABLE IF EXISTS pc_product_import_batch CASCADE;
DROP TABLE IF EXISTS pc_product_import_row_issue CASCADE;
DROP TABLE IF EXISTS pc_product_publish_package CASCADE;
DROP TABLE IF EXISTS pc_product_snapshot_instance CASCADE;
DROP TABLE IF EXISTS pc_product_sync_outbox CASCADE;
DROP TABLE IF EXISTS pc_publish_approval CASCADE;
DROP TABLE IF EXISTS pc_publish_check_result CASCADE;
DROP TABLE IF EXISTS pc_question_group CASCADE;
DROP TABLE IF EXISTS pc_sales_product CASCADE;
DROP TABLE IF EXISTS pc_standard_sku_engineering CASCADE;
DROP TABLE IF EXISTS pc_standard_sku CASCADE;

CREATE TABLE IF NOT EXISTS pc_category (
    category_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    parent_id bigint,
    category_code varchar(80) NOT NULL,
    category_name_cn varchar(200) NOT NULL,
    category_name_en varchar(200),
    business_type varchar(80),
    category_level integer,
    category_path varchar(500),
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    sort_order integer DEFAULT 0,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_category
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    ADD COLUMN IF NOT EXISTS business_type varchar(80);

COMMENT ON TABLE pc_category IS '产品分类表';
COMMENT ON COLUMN pc_category.category_id IS '产品分类ID';
COMMENT ON COLUMN pc_category.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_category.parent_id IS '父分类ID';
COMMENT ON COLUMN pc_category.category_code IS '产品分类编码';
COMMENT ON COLUMN pc_category.category_name_cn IS '产品分类中文名称';
COMMENT ON COLUMN pc_category.category_name_en IS '产品分类英文名称';
COMMENT ON COLUMN pc_category.business_type IS '业务口径类型';
COMMENT ON COLUMN pc_category.category_level IS '分类层级';
COMMENT ON COLUMN pc_category.category_path IS '分类路径编码';
COMMENT ON COLUMN pc_category.status IS '状态：建议 ENABLED 启用，DISABLED 停用';
COMMENT ON COLUMN pc_category.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_category.sort_order IS '排序';
COMMENT ON COLUMN pc_category.remark IS '备注';
COMMENT ON COLUMN pc_category.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_category.create_by IS '创建者';
COMMENT ON COLUMN pc_category.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_category.update_by IS '更新者';
COMMENT ON COLUMN pc_category.update_time IS '更新时间，UTC timestamptz';
DROP INDEX IF EXISTS uk_pc_category_category_code_active;
DROP INDEX IF EXISTS uk_pc_category_code_active;
CREATE INDEX IF NOT EXISTS idx_pc_category_code_active ON pc_category (tenant_id, category_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_category_parent_sort ON pc_category (tenant_id, parent_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_pc_category_business_status ON pc_category (tenant_id, business_type, status);

CREATE TABLE IF NOT EXISTS pc_unit (
    unit_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    unit_code varchar(80) NOT NULL,
    unit_name_cn varchar(200) NOT NULL,
    unit_name_en varchar(200),
    unit_type varchar(80),
    precision_scale integer DEFAULT 0,
    rounding_mode varchar(40),
    base_unit_code varchar(80),
    conversion_rate numeric(18,6),
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    sort_order integer DEFAULT 0,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_unit
    ADD COLUMN IF NOT EXISTS sort_order integer DEFAULT 0;

COMMENT ON TABLE pc_unit IS '产品单位表';
COMMENT ON COLUMN pc_unit.unit_id IS '单位ID';
COMMENT ON COLUMN pc_unit.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_unit.unit_code IS '单位编码';
COMMENT ON COLUMN pc_unit.unit_name_cn IS '单位中文名称';
COMMENT ON COLUMN pc_unit.unit_name_en IS '单位英文名称';
COMMENT ON COLUMN pc_unit.unit_type IS '单位类型';
COMMENT ON COLUMN pc_unit.precision_scale IS '精度小数位';
COMMENT ON COLUMN pc_unit.rounding_mode IS '舍入模式';
COMMENT ON COLUMN pc_unit.base_unit_code IS '基准单位编码';
COMMENT ON COLUMN pc_unit.conversion_rate IS '相对基准单位换算率';
COMMENT ON COLUMN pc_unit.status IS '状态：建议 ENABLED 启用，DISABLED 停用';
COMMENT ON COLUMN pc_unit.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_unit.sort_order IS '排序';
COMMENT ON COLUMN pc_unit.remark IS '备注';
COMMENT ON COLUMN pc_unit.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_unit.create_by IS '创建者';
COMMENT ON COLUMN pc_unit.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_unit.update_by IS '更新者';
COMMENT ON COLUMN pc_unit.update_time IS '更新时间，UTC timestamptz';
DROP INDEX IF EXISTS uk_pc_unit_code_active;
CREATE INDEX IF NOT EXISTS idx_pc_unit_code_active ON pc_unit (tenant_id, unit_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_unit_type_status ON pc_unit (tenant_id, unit_type, status);
CREATE INDEX IF NOT EXISTS idx_pc_unit_sort ON pc_unit (tenant_id, sort_order, unit_id);

CREATE TABLE IF NOT EXISTS pc_product_dict_type (
    dict_type_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1,
    dict_type_code varchar(100) NOT NULL,
    dict_type_name_cn varchar(200) NOT NULL,
    dict_type_name_en varchar(200),
    business_domain varchar(80) NOT NULL DEFAULT 'BASE',
    system_flag boolean NOT NULL DEFAULT false,
    editable_flag boolean NOT NULL DEFAULT true,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    sort_order integer DEFAULT 0,
    remark varchar(500),
    del_flag varchar(1) NOT NULL DEFAULT '0',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);
COMMENT ON TABLE pc_product_dict_type IS '产品业务字典类型表';
COMMENT ON COLUMN pc_product_dict_type.dict_type_code IS '字典类型编码，不使用 sys_dict_type';
COMMENT ON COLUMN pc_product_dict_type.business_domain IS '业务域：BASE / ENGINEERING / CONFIG';
DROP INDEX IF EXISTS uk_pc_product_dict_type_code_active;
CREATE INDEX IF NOT EXISTS idx_pc_product_dict_type_code_active ON pc_product_dict_type (tenant_id, dict_type_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_product_dict_type_domain_status ON pc_product_dict_type (tenant_id, business_domain, status);

CREATE TABLE IF NOT EXISTS pc_product_dict_item (
    dict_item_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1,
    dict_type_code varchar(100) NOT NULL,
    dict_item_value varchar(100) NOT NULL,
    dict_item_label_cn varchar(200) NOT NULL,
    dict_item_label_en varchar(200),
    parent_value varchar(100),
    system_flag boolean NOT NULL DEFAULT false,
    editable_flag boolean NOT NULL DEFAULT true,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    sort_order integer DEFAULT 0,
    remark varchar(500),
    del_flag varchar(1) NOT NULL DEFAULT '0',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);
COMMENT ON TABLE pc_product_dict_item IS '产品业务字典项表';
COMMENT ON COLUMN pc_product_dict_item.dict_type_code IS '字典类型编码，关联 pc_product_dict_type.dict_type_code';
COMMENT ON COLUMN pc_product_dict_item.dict_item_value IS '字典项业务值';
DROP INDEX IF EXISTS uk_pc_product_dict_item_value_active;
CREATE INDEX IF NOT EXISTS idx_pc_product_dict_item_value_active ON pc_product_dict_item (tenant_id, dict_type_code, dict_item_value) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_product_dict_item_type_status ON pc_product_dict_item (tenant_id, dict_type_code, status, sort_order);

CREATE TABLE IF NOT EXISTS pc_material_type_group (
    group_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    group_code varchar(80) NOT NULL,
    group_name_cn varchar(200) NOT NULL,
    group_name_en varchar(200),
    system_flag boolean NOT NULL DEFAULT false,
    editable_flag boolean NOT NULL DEFAULT true,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    sort_order integer DEFAULT 0,
    remark varchar(500),
    del_flag varchar(1) NOT NULL DEFAULT '0',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);
COMMENT ON TABLE pc_material_type_group IS '物料属性分组主数据表';
COMMENT ON COLUMN pc_material_type_group.group_code IS '属性分组编码';
COMMENT ON COLUMN pc_material_type_group.group_name_cn IS '属性分组中文名称';
DROP INDEX IF EXISTS uk_pc_material_type_group_code_active;
CREATE INDEX IF NOT EXISTS idx_pc_material_type_group_code_active ON pc_material_type_group (tenant_id, group_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_material_type_group_status ON pc_material_type_group (tenant_id, status, sort_order);

CREATE TABLE IF NOT EXISTS pc_material_type (
    material_type_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    material_type_code varchar(80) NOT NULL,
    material_type_name_cn varchar(200) NOT NULL,
    material_type_name_en varchar(200),
    attribute_group_id bigint NOT NULL,
    attribute_group_code varchar(80) NOT NULL,
    attribute_group_name_cn varchar(200) NOT NULL,
    system_flag boolean NOT NULL DEFAULT false,
    editable_flag boolean NOT NULL DEFAULT true,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    sort_order integer DEFAULT 0,
    remark varchar(500),
    del_flag varchar(1) NOT NULL DEFAULT '0',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);
COMMENT ON TABLE pc_material_type IS '物料类型主数据表';
COMMENT ON COLUMN pc_material_type.material_type_code IS '物料类型编码';
COMMENT ON COLUMN pc_material_type.attribute_group_code IS '所属属性分组编码';
DROP INDEX IF EXISTS uk_pc_material_type_code_active;
CREATE INDEX IF NOT EXISTS idx_pc_material_type_code_active ON pc_material_type (tenant_id, material_type_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_material_type_group_status ON pc_material_type (tenant_id, attribute_group_code, status, sort_order);

CREATE TABLE IF NOT EXISTS pc_manufacturer (
    manufacturer_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    manufacturer_code varchar(80) NOT NULL,
    manufacturer_name varchar(200) NOT NULL,
    manufacturer_short_name varchar(120),
    manufacturer_flag boolean NOT NULL DEFAULT true,
    supplier_flag boolean NOT NULL DEFAULT true,
    contact_name varchar(120),
    contact_phone varchar(120),
    address varchar(500),
    status varchar(20) NOT NULL DEFAULT 'DISABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    sort_order integer DEFAULT 0,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_manufacturer IS '厂家主数据表';
COMMENT ON COLUMN pc_manufacturer.manufacturer_id IS '厂家ID';
COMMENT ON COLUMN pc_manufacturer.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_manufacturer.manufacturer_code IS '厂家编号，3位业务编号';
COMMENT ON COLUMN pc_manufacturer.manufacturer_name IS '厂家名称';
COMMENT ON COLUMN pc_manufacturer.manufacturer_short_name IS '厂家简称';
COMMENT ON COLUMN pc_manufacturer.manufacturer_flag IS '是否厂家';
COMMENT ON COLUMN pc_manufacturer.supplier_flag IS '是否供应商';
COMMENT ON COLUMN pc_manufacturer.contact_name IS '联系人';
COMMENT ON COLUMN pc_manufacturer.contact_phone IS '联系电话';
COMMENT ON COLUMN pc_manufacturer.address IS '地址';
COMMENT ON COLUMN pc_manufacturer.status IS '审核状态：ENABLED 已审核，DISABLED 未审核';
COMMENT ON COLUMN pc_manufacturer.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_manufacturer.sort_order IS '排序';
COMMENT ON COLUMN pc_manufacturer.remark IS '备注';
COMMENT ON COLUMN pc_manufacturer.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_manufacturer.create_by IS '创建者';
COMMENT ON COLUMN pc_manufacturer.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_manufacturer.update_by IS '更新者';
COMMENT ON COLUMN pc_manufacturer.update_time IS '更新时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_manufacturer_code_active ON pc_manufacturer (tenant_id, manufacturer_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_manufacturer_status_sort ON pc_manufacturer (tenant_id, status, sort_order);

CREATE TABLE IF NOT EXISTS pc_base_attribute (
    attribute_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    attribute_group_code varchar(80) NOT NULL,
    attribute_group_name_cn varchar(200),
    attribute_code varchar(80) NOT NULL,
    attribute_name_cn varchar(200) NOT NULL,
    attribute_name_en varchar(200),
    value_type varchar(40) NOT NULL,
    unit_code varchar(80),
    extra_json jsonb,
    sort_order integer DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_base_attribute
    ADD COLUMN IF NOT EXISTS attribute_group_code varchar(80),
    ADD COLUMN IF NOT EXISTS attribute_group_name_cn varchar(200);

COMMENT ON TABLE pc_base_attribute IS '物料属性表';
COMMENT ON COLUMN pc_base_attribute.attribute_id IS '属性定义ID';
COMMENT ON COLUMN pc_base_attribute.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_base_attribute.attribute_group_code IS '属性分组编码';
COMMENT ON COLUMN pc_base_attribute.attribute_group_name_cn IS '属性分组中文名称快照';
COMMENT ON COLUMN pc_base_attribute.attribute_code IS '属性编码';
COMMENT ON COLUMN pc_base_attribute.attribute_name_cn IS '属性中文名称';
COMMENT ON COLUMN pc_base_attribute.attribute_name_en IS '属性英文名称';
COMMENT ON COLUMN pc_base_attribute.value_type IS '值类型';
COMMENT ON COLUMN pc_base_attribute.unit_code IS '默认单位编码';
COMMENT ON COLUMN pc_base_attribute.extra_json IS '扩展配置JSON';
COMMENT ON COLUMN pc_base_attribute.sort_order IS '排序';
COMMENT ON COLUMN pc_base_attribute.status IS '状态：建议 ENABLED 启用，DISABLED 停用';
COMMENT ON COLUMN pc_base_attribute.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_base_attribute.remark IS '备注';
COMMENT ON COLUMN pc_base_attribute.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_base_attribute.create_by IS '创建者';
COMMENT ON COLUMN pc_base_attribute.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_base_attribute.update_by IS '更新者';
COMMENT ON COLUMN pc_base_attribute.update_time IS '更新时间，UTC timestamptz';
DROP INDEX IF EXISTS uk_pc_base_attribute_code_active;
DROP INDEX IF EXISTS idx_pc_base_attribute_code_active;
CREATE INDEX IF NOT EXISTS idx_pc_base_attribute_code_active ON pc_base_attribute (tenant_id, attribute_group_code, attribute_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_base_attribute_group_status ON pc_base_attribute (tenant_id, attribute_group_code, status);

CREATE TABLE IF NOT EXISTS pc_material (
    material_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    material_code varchar(80) NOT NULL,
    material_name_cn varchar(200) NOT NULL,
    material_name_en varchar(200),
    material_type_id bigint NOT NULL,
    material_type_code varchar(80) NOT NULL,
    material_type_name_cn varchar(200),
    attribute_group_id bigint,
    attribute_group_code varchar(80),
    attribute_group_name_cn varchar(200),
    unit_code varchar(80) NOT NULL,
    secondary_unit_code varchar(80),
    manufacturer_id bigint,
    manufacturer_code varchar(80),
    manufacturer_name varchar(200),
    manufacturer_item_no varchar(120),
    model varchar(120),
    spec varchar(300) NOT NULL,
    spec_model_text varchar(1000),
    color_name varchar(120),
    weight_value numeric(18,6),
    unit_price numeric(18,4),
    audit_by varchar(64),
    audit_time timestamptz,
    sort_order int NOT NULL DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'DISABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_material
    ADD COLUMN IF NOT EXISTS material_type_id bigint,
    ADD COLUMN IF NOT EXISTS material_type_code varchar(80),
    ADD COLUMN IF NOT EXISTS material_type_name_cn varchar(200),
    ADD COLUMN IF NOT EXISTS attribute_group_id bigint,
    ADD COLUMN IF NOT EXISTS attribute_group_code varchar(80),
    ADD COLUMN IF NOT EXISTS attribute_group_name_cn varchar(200),
    ADD COLUMN IF NOT EXISTS unit_code varchar(80),
    ADD COLUMN IF NOT EXISTS secondary_unit_code varchar(80),
    ADD COLUMN IF NOT EXISTS manufacturer_id bigint,
    ADD COLUMN IF NOT EXISTS manufacturer_code varchar(80),
    ADD COLUMN IF NOT EXISTS manufacturer_name varchar(200),
    ADD COLUMN IF NOT EXISTS manufacturer_item_no varchar(120),
    ADD COLUMN IF NOT EXISTS model varchar(120),
    ADD COLUMN IF NOT EXISTS spec varchar(300),
    ADD COLUMN IF NOT EXISTS spec_model_text varchar(1000),
    ADD COLUMN IF NOT EXISTS color_name varchar(120),
    ADD COLUMN IF NOT EXISTS weight_value numeric(18,6),
    ADD COLUMN IF NOT EXISTS unit_price numeric(18,4),
    ADD COLUMN IF NOT EXISTS audit_by varchar(64),
    ADD COLUMN IF NOT EXISTS audit_time timestamptz,
    ADD COLUMN IF NOT EXISTS sort_order int NOT NULL DEFAULT 0;

COMMENT ON TABLE pc_material IS '产品物料表';
COMMENT ON COLUMN pc_material.material_id IS '产品物料ID';
COMMENT ON COLUMN pc_material.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_material.material_code IS '物料编码';
COMMENT ON COLUMN pc_material.material_name_cn IS '物料中文名称';
COMMENT ON COLUMN pc_material.material_name_en IS '物料英文名称';
COMMENT ON COLUMN pc_material.material_type_id IS '物料类型ID';
COMMENT ON COLUMN pc_material.material_type_code IS '物料类型编码';
COMMENT ON COLUMN pc_material.material_type_name_cn IS '物料类型中文名称快照';
COMMENT ON COLUMN pc_material.attribute_group_id IS '属性分组ID';
COMMENT ON COLUMN pc_material.attribute_group_code IS '属性分组编码';
COMMENT ON COLUMN pc_material.attribute_group_name_cn IS '属性分组中文名称快照';
COMMENT ON COLUMN pc_material.unit_code IS '主单位编码';
COMMENT ON COLUMN pc_material.secondary_unit_code IS '副单位编码';
COMMENT ON COLUMN pc_material.manufacturer_id IS '厂家ID';
COMMENT ON COLUMN pc_material.manufacturer_code IS '厂家编码';
COMMENT ON COLUMN pc_material.manufacturer_name IS '厂家名称';
COMMENT ON COLUMN pc_material.manufacturer_item_no IS '厂家物料编码';
COMMENT ON COLUMN pc_material.model IS '型号';
COMMENT ON COLUMN pc_material.spec IS '规格';
COMMENT ON COLUMN pc_material.spec_model_text IS '规格型号展示文本，普通文本';
COMMENT ON COLUMN pc_material.color_name IS '颜色';
COMMENT ON COLUMN pc_material.weight_value IS '克重/重量';
COMMENT ON COLUMN pc_material.unit_price IS '单价';
COMMENT ON COLUMN pc_material.audit_by IS '审核人';
COMMENT ON COLUMN pc_material.audit_time IS '审核时间，UTC timestamptz';
COMMENT ON COLUMN pc_material.sort_order IS '排序';
COMMENT ON COLUMN pc_material.status IS '审核状态：ENABLED 已审核，DISABLED 未审核';
COMMENT ON COLUMN pc_material.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_material.remark IS '备注';
COMMENT ON COLUMN pc_material.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_material.create_by IS '创建者';
COMMENT ON COLUMN pc_material.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_material.update_by IS '更新者';
COMMENT ON COLUMN pc_material.update_time IS '更新时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_material_code_active ON pc_material (tenant_id, material_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_material_type_status ON pc_material (tenant_id, material_type_code, status);
CREATE INDEX IF NOT EXISTS idx_pc_material_group_status ON pc_material (tenant_id, attribute_group_code, status);
CREATE INDEX IF NOT EXISTS idx_pc_material_manufacturer ON pc_material (tenant_id, manufacturer_id, status);
CREATE INDEX IF NOT EXISTS idx_pc_material_manufacturer_name ON pc_material (tenant_id, manufacturer_name);
CREATE INDEX IF NOT EXISTS idx_pc_material_spec_model ON pc_material (tenant_id, model, spec);

CREATE TABLE IF NOT EXISTS pc_change_log (
    change_log_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    biz_module varchar(80) NOT NULL,
    biz_type varchar(80) NOT NULL,
    biz_id bigint,
    biz_code varchar(120),
    action_type varchar(40) NOT NULL,
    action_name varchar(120),
    before_json text,
    after_json text,
    diff_json text,
    operator_id bigint,
    operator_name varchar(64),
    operate_time timestamptz NOT NULL,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_change_log IS '产品业务变更流水表';
COMMENT ON COLUMN pc_change_log.biz_module IS '业务模块，如 BASE_INFO';
COMMENT ON COLUMN pc_change_log.biz_type IS '业务对象类型，如 MATERIAL';
COMMENT ON COLUMN pc_change_log.biz_id IS '业务对象ID';
COMMENT ON COLUMN pc_change_log.biz_code IS '业务对象编码';
COMMENT ON COLUMN pc_change_log.action_type IS '动作类型，如 CREATE、UPDATE、AUDIT、UNAUDIT';
COMMENT ON COLUMN pc_change_log.action_name IS '动作名称';
COMMENT ON COLUMN pc_change_log.before_json IS '变更前JSON';
COMMENT ON COLUMN pc_change_log.after_json IS '变更后JSON';
COMMENT ON COLUMN pc_change_log.diff_json IS '字段差异JSON';
COMMENT ON COLUMN pc_change_log.operator_id IS '操作人ID';
COMMENT ON COLUMN pc_change_log.operator_name IS '操作人';
COMMENT ON COLUMN pc_change_log.operate_time IS '操作时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_change_log_biz ON pc_change_log (tenant_id, biz_type, biz_id, operate_time DESC);
CREATE INDEX IF NOT EXISTS idx_pc_change_log_code ON pc_change_log (tenant_id, biz_code, operate_time DESC);
CREATE INDEX IF NOT EXISTS idx_pc_change_log_action_time ON pc_change_log (tenant_id, action_type, operate_time DESC);

CREATE TABLE IF NOT EXISTS pc_material_attribute (
    material_attribute_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    material_id bigint NOT NULL,
    material_code varchar(80) NOT NULL,
    attribute_id bigint,
    attribute_code varchar(80) NOT NULL,
    attribute_name_cn varchar(200) NOT NULL,
    attribute_name_en varchar(200),
    value_text varchar(1000),
    value_number numeric(18,6),
    value_bool boolean,
    value_unit_code varchar(80),
    sort_order integer DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_material_attribute IS '物料属性值表';
COMMENT ON COLUMN pc_material_attribute.material_attribute_id IS '物料属性值ID';
COMMENT ON COLUMN pc_material_attribute.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_material_attribute.material_id IS '物料ID';
COMMENT ON COLUMN pc_material_attribute.material_code IS '物料编码';
COMMENT ON COLUMN pc_material_attribute.attribute_id IS '属性定义ID';
COMMENT ON COLUMN pc_material_attribute.attribute_code IS '属性编码';
COMMENT ON COLUMN pc_material_attribute.attribute_name_cn IS '属性中文名称';
COMMENT ON COLUMN pc_material_attribute.attribute_name_en IS '属性英文名称';
COMMENT ON COLUMN pc_material_attribute.value_text IS '文本值';
COMMENT ON COLUMN pc_material_attribute.value_number IS '数值';
COMMENT ON COLUMN pc_material_attribute.value_bool IS '布尔值';
COMMENT ON COLUMN pc_material_attribute.value_unit_code IS '数值单位编码';
COMMENT ON COLUMN pc_material_attribute.sort_order IS '排序';
COMMENT ON COLUMN pc_material_attribute.status IS '状态：建议 ENABLED 启用，DISABLED 停用';
COMMENT ON COLUMN pc_material_attribute.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_material_attribute.remark IS '备注';
COMMENT ON COLUMN pc_material_attribute.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_material_attribute.create_by IS '创建者';
COMMENT ON COLUMN pc_material_attribute.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_material_attribute.update_by IS '更新者';
COMMENT ON COLUMN pc_material_attribute.update_time IS '更新时间，UTC timestamptz';
DROP INDEX IF EXISTS uk_pc_material_attr_active;
CREATE INDEX IF NOT EXISTS idx_pc_material_attr_code_active ON pc_material_attribute (tenant_id, material_id, attribute_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_material_attr_material ON pc_material_attribute (tenant_id, material_id, status);
CREATE INDEX IF NOT EXISTS idx_pc_material_attr_attribute ON pc_material_attribute (tenant_id, attribute_id, status);

CREATE TABLE IF NOT EXISTS pc_media_asset (
    asset_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    asset_code varchar(80) NOT NULL,
    asset_name_cn varchar(200) NOT NULL,
    asset_name_en varchar(200),
    asset_type varchar(80),
    usage_type varchar(80),
    language_code varchar(80),
    visibility varchar(100),
    oss_id bigint,
    url varchar(500),
    alt_text varchar(500),
    version_no integer DEFAULT 1,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_media_asset
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0);

COMMENT ON TABLE pc_media_asset IS '产品资料资产表';
COMMENT ON COLUMN pc_media_asset.asset_id IS '产品资料资产ID';
COMMENT ON COLUMN pc_media_asset.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_media_asset.asset_code IS '资料资产编码';
COMMENT ON COLUMN pc_media_asset.asset_name_cn IS '资料资产中文名称';
COMMENT ON COLUMN pc_media_asset.asset_name_en IS '资料资产英文名称';
COMMENT ON COLUMN pc_media_asset.asset_type IS '资料类型';
COMMENT ON COLUMN pc_media_asset.usage_type IS '资料用途';
COMMENT ON COLUMN pc_media_asset.language_code IS '语言编码';
COMMENT ON COLUMN pc_media_asset.visibility IS '可见范围';
COMMENT ON COLUMN pc_media_asset.oss_id IS 'OSS文件ID';
COMMENT ON COLUMN pc_media_asset.url IS '文件访问地址';
COMMENT ON COLUMN pc_media_asset.alt_text IS '图片替代文本';
COMMENT ON COLUMN pc_media_asset.version_no IS '资料版本号';
COMMENT ON COLUMN pc_media_asset.status IS '状态：建议 ENABLED 启用，DISABLED 停用';
COMMENT ON COLUMN pc_media_asset.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_media_asset.remark IS '备注';
COMMENT ON COLUMN pc_media_asset.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_media_asset.create_by IS '创建者';
COMMENT ON COLUMN pc_media_asset.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_media_asset.update_by IS '更新者';
COMMENT ON COLUMN pc_media_asset.update_time IS '更新时间，UTC timestamptz';
DROP INDEX IF EXISTS uk_pc_media_asset_asset_code_active;
DROP INDEX IF EXISTS uk_pc_media_asset_code_active;
CREATE INDEX IF NOT EXISTS idx_pc_media_asset_code_active ON pc_media_asset (tenant_id, asset_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_media_asset_oss ON pc_media_asset (tenant_id, oss_id);
CREATE INDEX IF NOT EXISTS idx_pc_media_asset_type_lang ON pc_media_asset (tenant_id, asset_type, language_code, status);

CREATE TABLE IF NOT EXISTS pc_media_binding (
    binding_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    asset_id bigint NOT NULL,
    asset_code varchar(80) NOT NULL,
    target_type varchar(80) NOT NULL,
    target_id bigint NOT NULL,
    target_code varchar(80) NOT NULL,
    usage_type varchar(80),
    visibility varchar(100),
    language_code varchar(80),
    required_for_publish varchar(1) NOT NULL DEFAULT '0',
    sort_order integer DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_media_binding
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0);

COMMENT ON TABLE pc_media_binding IS '产品资料绑定表';
COMMENT ON COLUMN pc_media_binding.binding_id IS '产品资料绑定ID';
COMMENT ON COLUMN pc_media_binding.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_media_binding.asset_id IS '资料资产ID';
COMMENT ON COLUMN pc_media_binding.asset_code IS '资料资产编码快照';
COMMENT ON COLUMN pc_media_binding.target_type IS '绑定对象类型';
COMMENT ON COLUMN pc_media_binding.target_id IS '绑定对象ID';
COMMENT ON COLUMN pc_media_binding.target_code IS '绑定对象编码快照';
COMMENT ON COLUMN pc_media_binding.usage_type IS '资料用途';
COMMENT ON COLUMN pc_media_binding.visibility IS '可见范围';
COMMENT ON COLUMN pc_media_binding.language_code IS '语言编码';
COMMENT ON COLUMN pc_media_binding.required_for_publish IS '发布是否必需：1是，0否';
COMMENT ON COLUMN pc_media_binding.sort_order IS '排序';
COMMENT ON COLUMN pc_media_binding.status IS '状态：建议 ENABLED 启用，DISABLED 停用';
COMMENT ON COLUMN pc_media_binding.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_media_binding.remark IS '备注';
COMMENT ON COLUMN pc_media_binding.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_media_binding.create_by IS '创建者';
COMMENT ON COLUMN pc_media_binding.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_media_binding.update_by IS '更新者';
COMMENT ON COLUMN pc_media_binding.update_time IS '更新时间，UTC timestamptz';
DROP INDEX IF EXISTS uk_pc_media_binding_asset_code_active;
DROP INDEX IF EXISTS uk_pc_media_binding_target_active;
CREATE INDEX IF NOT EXISTS idx_pc_media_binding_target_active ON pc_media_binding (tenant_id, asset_id, target_type, target_id, usage_type, language_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_media_binding_target ON pc_media_binding (tenant_id, target_type, target_id, status);
CREATE INDEX IF NOT EXISTS idx_pc_media_binding_asset ON pc_media_binding (tenant_id, asset_id, status);

INSERT INTO pc_unit (
    unit_id, tenant_id, unit_code, unit_name_cn, unit_name_en, unit_type, precision_scale, rounding_mode,
    base_unit_code, conversion_rate, status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (110001, 1, 'PCS', '个', 'Piece', 'COUNT', 0, 'HALF_UP', 'PCS', 1.000000, 'ENABLED', '0', 10, '通用计件单位', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (110002, 1, 'SET', '套', 'Set', 'COUNT', 0, 'HALF_UP', 'SET', 1.000000, 'ENABLED', '0', 20, '组件套装单位', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (110003, 1, 'M', '米', 'Meter', 'LENGTH', 3, 'HALF_UP', 'M', 1.000000, 'ENABLED', '0', 30, '长度单位', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (110004, 1, 'MM', '毫米', 'Millimeter', 'LENGTH', 2, 'HALF_UP', 'M', 0.001000, 'ENABLED', '0', 40, '厚度与门幅辅助单位', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (110005, 1, 'CM', '厘米', 'Centimeter', 'LENGTH', 2, 'HALF_UP', 'M', 0.010000, 'ENABLED', '0', 45, '销售产品尺寸单位', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (110006, 1, 'SQM', '平方米', 'Square Meter', 'AREA', 3, 'HALF_UP', 'SQM', 1.000000, 'ENABLED', '0', 50, '面积单位', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (110007, 1, 'GSM', '克每平方米', 'Gram per Square Meter', 'WEIGHT', 0, 'HALF_UP', 'GSM', 1.000000, 'ENABLED', '0', 60, '面料克重单位', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (110008, 1, 'KG', '千克', 'Kilogram', 'WEIGHT', 3, 'HALF_UP', 'KG', 1.000000, 'ENABLED', '0', 70, '重量单位', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00')
ON CONFLICT (unit_id) DO NOTHING;

INSERT INTO pc_manufacturer (
    manufacturer_id, tenant_id, manufacturer_code, manufacturer_name, manufacturer_short_name,
    manufacturer_flag, supplier_flag, status, sort_order, remark, del_flag,
    create_by, create_time, update_by, update_time
) VALUES
    (123900, 1, '900', '通用厂家/暂无厂家', '通用厂家', true, true, 'ENABLED', 900, '通用件、暂无厂家、自制件默认厂家编号', '0', 'system', '2026-06-25 00:00:00+00', 'system', '2026-06-25 00:00:00+00')
ON CONFLICT (manufacturer_id) DO UPDATE
SET manufacturer_code = EXCLUDED.manufacturer_code,
    manufacturer_name = EXCLUDED.manufacturer_name,
    manufacturer_short_name = EXCLUDED.manufacturer_short_name,
    manufacturer_flag = EXCLUDED.manufacturer_flag,
    supplier_flag = EXCLUDED.supplier_flag,
    status = EXCLUDED.status,
    sort_order = EXCLUDED.sort_order,
    remark = EXCLUDED.remark,
    update_by = 'system',
    update_time = now();

INSERT INTO pc_material_type_group (
    group_id, tenant_id, group_code, group_name_cn, group_name_en, system_flag, editable_flag,
    status, sort_order, remark, del_flag, create_by, create_time, update_by, update_time
) VALUES
    (121001, 1, 'FABRIC', '面料', 'Fabric', true, false, 'ENABLED', 10, '面料物料大类', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (121002, 1, 'ALUMINUM', '铝材', 'Aluminum', true, false, 'ENABLED', 20, '铝材、下杆、轨道等归入铝材', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (121003, 1, 'SYSTEM', '系统', 'System', true, false, 'ENABLED', 30, '电机、遥控、控制器等归入系统', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (121004, 1, 'ACCESSORY', '配件', 'Accessory', true, false, 'ENABLED', 40, '安装件、支架、胶条等配件', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (121005, 1, 'PART_PACK', '零件包', 'Parts Pack', true, false, 'ENABLED', 50, '配方中的零件包类物料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (121006, 1, 'PACKAGING', '包装', 'Packaging', true, false, 'ENABLED', 60, '纸箱、PET盒等包装物料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00')
ON CONFLICT (group_id) DO UPDATE
SET group_code = EXCLUDED.group_code,
    group_name_cn = EXCLUDED.group_name_cn,
    group_name_en = EXCLUDED.group_name_en,
    system_flag = EXCLUDED.system_flag,
    editable_flag = EXCLUDED.editable_flag,
    status = EXCLUDED.status,
    sort_order = EXCLUDED.sort_order,
    remark = EXCLUDED.remark,
    update_by = 'system',
    update_time = now();

INSERT INTO pc_material_type (
    material_type_id, tenant_id, material_type_code, material_type_name_cn, material_type_name_en,
    attribute_group_id, attribute_group_code, attribute_group_name_cn, system_flag, editable_flag,
    status, sort_order, remark, del_flag, create_by, create_time, update_by, update_time
) VALUES
    (122001, 1, 'FABRIC', '面料', 'Fabric', 121001, 'FABRIC', '面料', true, false, 'ENABLED', 10, '配方大类：面料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (122002, 1, 'ALUMINUM', '铝材', 'Aluminum', 121002, 'ALUMINUM', '铝材', true, false, 'ENABLED', 20, '配方大类：铝材', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (122003, 1, 'SYSTEM', '系统', 'System', 121003, 'SYSTEM', '系统', true, false, 'ENABLED', 30, '配方大类：系统', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (122004, 1, 'ACCESSORY', '配件', 'Accessory', 121004, 'ACCESSORY', '配件', true, false, 'ENABLED', 40, '配方大类：配件', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (122005, 1, 'PART_PACK', '零件包', 'Parts Pack', 121005, 'PART_PACK', '零件包', true, false, 'ENABLED', 50, '配方大类：零件包', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (122006, 1, 'PACKAGING', '包装', 'Packaging', 121006, 'PACKAGING', '包装', true, false, 'ENABLED', 60, '配方大类：包装', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00')
ON CONFLICT (material_type_id) DO UPDATE
SET material_type_code = EXCLUDED.material_type_code,
    material_type_name_cn = EXCLUDED.material_type_name_cn,
    material_type_name_en = EXCLUDED.material_type_name_en,
    attribute_group_id = EXCLUDED.attribute_group_id,
    attribute_group_code = EXCLUDED.attribute_group_code,
    attribute_group_name_cn = EXCLUDED.attribute_group_name_cn,
    system_flag = EXCLUDED.system_flag,
    editable_flag = EXCLUDED.editable_flag,
    status = EXCLUDED.status,
    sort_order = EXCLUDED.sort_order,
    remark = EXCLUDED.remark,
    update_by = 'system',
    update_time = now();


INSERT INTO pc_product_dict_type (
    dict_type_id, tenant_id, dict_type_code, dict_type_name_cn, dict_type_name_en, business_domain,
    system_flag, editable_flag, status, sort_order, remark, del_flag, create_by, create_time, update_by, update_time
) VALUES
    (118001, 1, 'product_unit_type', '单位类型', 'Unit Type', 'BASE', true, true, 'ENABLED', 10, '单位分类枚举，具体单位仍维护在 pc_unit', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118003, 1, 'product_business_type', '业务类型', 'Business Type', 'BASE', true, true, 'ENABLED', 30, '产品业务口径类型', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118005, 1, 'product_asset_type', '资料类型', 'Asset Type', 'BASE', true, true, 'ENABLED', 50, '资料资产类型', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00')
ON CONFLICT (dict_type_id) DO UPDATE
SET dict_type_name_cn = EXCLUDED.dict_type_name_cn,
    dict_type_name_en = EXCLUDED.dict_type_name_en,
    business_domain = EXCLUDED.business_domain,
    system_flag = EXCLUDED.system_flag,
    editable_flag = EXCLUDED.editable_flag,
    status = EXCLUDED.status,
    sort_order = EXCLUDED.sort_order,
    remark = EXCLUDED.remark,
    update_by = 'system',
    update_time = now();

INSERT INTO pc_product_dict_item (
    dict_item_id, tenant_id, dict_type_code, dict_item_value, dict_item_label_cn, dict_item_label_en, parent_value,
    system_flag, editable_flag, status, sort_order, remark, del_flag, create_by, create_time, update_by, update_time
) VALUES
    (119001, 1, 'product_unit_type', 'LENGTH', '长度', 'Length', NULL, true, true, 'ENABLED', 10, '长度单位分类', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119002, 1, 'product_unit_type', 'COUNT', '数量', 'Count', NULL, true, true, 'ENABLED', 20, '计数单位分类', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119003, 1, 'product_unit_type', 'AREA', '面积', 'Area', NULL, true, true, 'ENABLED', 30, '面积单位分类', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119004, 1, 'product_unit_type', 'WEIGHT', '重量', 'Weight', NULL, true, true, 'ENABLED', 40, '重量单位分类', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119005, 1, 'product_unit_type', 'VOLUME', '体积', 'Volume', NULL, true, true, 'ENABLED', 50, '体积单位分类', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119201, 1, 'product_business_type', 'ROLLER_SHADE', '卷帘', 'Roller Shade', NULL, true, true, 'ENABLED', 10, '卷帘', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119202, 1, 'product_business_type', 'ZEBRA_SHADE', '斑马帘/柔纱帘', 'Zebra Shade', NULL, true, true, 'ENABLED', 20, '斑马帘/柔纱帘', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119203, 1, 'product_business_type', 'OUTDOOR_SHADE', '户外遮阳', 'Outdoor Shade', NULL, true, true, 'ENABLED', 30, '户外遮阳', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119204, 1, 'product_business_type', 'CURTAIN_TRACK', '轨道窗帘', 'Curtain Track', NULL, true, true, 'ENABLED', 40, '轨道窗帘', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119205, 1, 'product_business_type', 'HONEYCOMB_SHADE', '蜂巢帘', 'Honeycomb Shade', NULL, true, true, 'ENABLED', 50, '蜂巢帘', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119401, 1, 'product_asset_type', 'IMAGE', '图片', 'Image', NULL, true, true, 'ENABLED', 10, '图片', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119402, 1, 'product_asset_type', 'PDF', 'PDF', 'PDF', NULL, true, true, 'ENABLED', 20, 'PDF', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119403, 1, 'product_asset_type', 'SPEC', '规格书', 'Specification', NULL, true, true, 'ENABLED', 30, '规格书', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119404, 1, 'product_asset_type', 'INSTALL_GUIDE', '安装说明', 'Installation Guide', NULL, true, true, 'ENABLED', 40, '安装说明', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119405, 1, 'product_asset_type', 'DRAWING', '图纸', 'Drawing', NULL, true, true, 'ENABLED', 50, '图纸', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119406, 1, 'product_asset_type', 'OTHER', '其他', 'Other', NULL, true, true, 'ENABLED', 90, '其他资料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00')
ON CONFLICT (dict_item_id) DO UPDATE
SET dict_item_label_cn = EXCLUDED.dict_item_label_cn,
    dict_item_label_en = EXCLUDED.dict_item_label_en,
    parent_value = EXCLUDED.parent_value,
    system_flag = EXCLUDED.system_flag,
    editable_flag = EXCLUDED.editable_flag,
    status = EXCLUDED.status,
    sort_order = EXCLUDED.sort_order,
    remark = EXCLUDED.remark,
    update_by = 'system',
    update_time = now();

-- =====================================================
-- 第一阶段正式菜单、按钮和字典归一化
-- 说明：
-- 1. 正式侧边栏当前只保留基础信息；配方管理后续单独新建。
-- 2. 旧产品工程/配置/报价/发布入口主动删除，不做兼容保留。
-- 3. 本块必须保持幂等，可重复执行到开发库。
-- =====================================================

DELETE FROM sys_role_menu
WHERE menu_id IN (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_id BETWEEN 24300 AND 24599
       OR path IN ('product-engineering', 'product-config', 'product-release')
       OR component LIKE 'product-engineering/%'
       OR component LIKE 'product-config/%'
       OR component LIKE 'product-center/template%'
       OR component LIKE 'product-center/pricing%'
       OR component LIKE 'product-center/publish%'
       OR component LIKE 'product-center/import%'
       OR component LIKE 'product-center/sales-view%'
);
DELETE FROM sys_menu
WHERE menu_id BETWEEN 24300 AND 24599
   OR path IN ('product-engineering', 'product-config', 'product-release')
   OR component LIKE 'product-engineering/%'
   OR component LIKE 'product-config/%'
   OR component LIKE 'product-center/template%'
   OR component LIKE 'product-center/pricing%'
   OR component LIKE 'product-center/publish%'
   OR component LIKE 'product-center/import%'
   OR component LIKE 'product-center/sales-view%';
DELETE FROM sys_role_menu WHERE menu_id IN (24201, 24202, 24203, 24204, 24205, 24206, 24207, 24208, 24209, 24210, 24211, 24212, 24213, 24220, 24301, 24302);
DELETE FROM sys_menu WHERE menu_id IN (24201, 24202, 24203, 24204, 24205, 24206, 24207, 24208, 24209, 24210, 24211, 24212, 24213, 24220, 24301, 24302);
DELETE FROM sys_role_menu
WHERE menu_id IN (
    SELECT menu_id
    FROM sys_menu
    WHERE path = 'fabric-profiles'
       OR i18n_key = 'productCenter.menu.fabricProfiles'
    UNION
    SELECT child.menu_id
    FROM sys_menu parent
    JOIN sys_menu child ON child.parent_id = parent.menu_id
    WHERE parent.path = 'fabric-profiles'
       OR parent.i18n_key = 'productCenter.menu.fabricProfiles'
);
DELETE FROM sys_menu
WHERE parent_id IN (
    SELECT menu_id
    FROM sys_menu
    WHERE path = 'fabric-profiles'
       OR i18n_key = 'productCenter.menu.fabricProfiles'
)
   OR path = 'fabric-profiles'
   OR i18n_key = 'productCenter.menu.fabricProfiles';

INSERT INTO sys_menu (menu_id, tenant_id, parent_id, menu_name, i18n_key, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
    (24200, 1, 0, 'Basic Information', 'productCenter.menu.masterData', 30, 'product-master', NULL, NULL, '1', '0', 'M', '1', '1', NULL, 'product', 'system', now(), NULL, NULL, '产品能力-基础信息')
ON CONFLICT (menu_id) DO UPDATE
SET tenant_id = EXCLUDED.tenant_id,
    parent_id = EXCLUDED.parent_id,
    menu_name = EXCLUDED.menu_name,
    i18n_key = EXCLUDED.i18n_key,
    order_num = EXCLUDED.order_num,
    path = EXCLUDED.path,
    component = EXCLUDED.component,
    query_param = EXCLUDED.query_param,
    is_frame = EXCLUDED.is_frame,
    is_cache = EXCLUDED.is_cache,
    menu_type = EXCLUDED.menu_type,
    visible = EXCLUDED.visible,
    status = EXCLUDED.status,
    perms = EXCLUDED.perms,
    icon = EXCLUDED.icon,
    update_by = 'system',
    update_time = now(),
    remark = EXCLUDED.remark;

INSERT INTO sys_menu (menu_id, tenant_id, parent_id, menu_name, i18n_key, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
    (24212, 1, 24200, 'Product Categories', 'productCenter.menu.categories', 1, 'categories', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'tree-table', 'system', now(), NULL, NULL, '产品分类'),
    (24213, 1, 24200, 'Base Dictionaries', 'productCenter.menu.productDicts', 2, 'product-dicts', 'product-center/product-dicts', NULL, '1', '0', 'C', '1', '1', 'product:dict:list', 'dict', 'system', now(), NULL, NULL, '基础字典'),
    (24206, 1, 24200, 'Units', 'productCenter.menu.units', 3, 'units', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:unit:list', 'unit', 'system', now(), NULL, NULL, '单位管理'),
    (24209, 1, 24200, 'Manufacturers', 'productCenter.menu.manufacturers', 4, 'manufacturers', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:manufacturer:list', 'company', 'system', now(), NULL, NULL, '厂家管理'),
    (24207, 1, 24200, 'Material Types', 'productCenter.menu.materialTypes', 5, 'material-types', 'product-center/material-types', NULL, '1', '0', 'C', '1', '1', 'product:material-type:list', 'tree-table', 'system', now(), NULL, NULL, '物料类型'),
    (24204, 1, 24200, 'Material Attributes', 'productCenter.menu.baseAttributes', 6, 'base-attributes', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base-attribute:list', 'dict', 'system', now(), NULL, NULL, '物料属性'),
    (24202, 1, 24200, 'Materials', 'productCenter.menu.materials', 7, 'materials', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'inventory', 'system', now(), NULL, NULL, '物料管理'),
    (24208, 1, 24200, 'Material Attribute Values', 'productCenter.menu.materialAttributes', 89, 'material-attributes', 'product-center/base', NULL, '1', '0', 'C', '0', '1', 'product:material-attribute:list', 'list', 'system', now(), NULL, NULL, '物料属性值，从物料管理抽屉同步维护'),
    (24205, 1, 24200, 'Media Assets', 'productCenter.menu.mediaAssets', 94, 'media-assets', 'product-center/assets', NULL, '1', '0', 'C', '0', '1', 'product:asset:list', 'upload', 'system', now(), NULL, NULL, '资料资产'),
    (24210, 1, 24200, 'Media Bindings', 'productCenter.menu.mediaBindings', 94, 'media-bindings', 'product-center/assets', NULL, '1', '0', 'C', '0', '1', 'product:asset:list', 'link', 'system', now(), NULL, NULL, '资料绑定，附件关联台账，默认不在基础资料一级菜单展示')
ON CONFLICT (menu_id) DO UPDATE
SET tenant_id = EXCLUDED.tenant_id,
    parent_id = EXCLUDED.parent_id,
    menu_name = EXCLUDED.menu_name,
    i18n_key = EXCLUDED.i18n_key,
    order_num = EXCLUDED.order_num,
    path = EXCLUDED.path,
    component = EXCLUDED.component,
    query_param = EXCLUDED.query_param,
    is_frame = EXCLUDED.is_frame,
    is_cache = EXCLUDED.is_cache,
    menu_type = EXCLUDED.menu_type,
    visible = EXCLUDED.visible,
    status = EXCLUDED.status,
    perms = EXCLUDED.perms,
    icon = EXCLUDED.icon,
    update_by = 'system',
    update_time = now(),
    remark = EXCLUDED.remark;

INSERT INTO sys_menu (menu_id, tenant_id, parent_id, menu_name, i18n_key, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
    (24214, 1, 24212, 'Category Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:list', '#', 'system', now(), NULL, NULL, '产品分类查询'),
    (24215, 1, 24212, 'Category Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:add', '#', 'system', now(), NULL, NULL, '产品分类新增'),
    (24216, 1, 24212, 'Category Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:edit', '#', 'system', now(), NULL, NULL, '产品分类编辑'),
    (24217, 1, 24212, 'Category Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:remove', '#', 'system', now(), NULL, NULL, '产品分类删除'),
    (24218, 1, 24212, 'Category Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:reference', '#', 'system', now(), NULL, NULL, '产品分类引用检查'),
    (24225, 1, 24206, 'Unit Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:unit:list', '#', 'system', now(), NULL, NULL, '单位查询'),
    (24226, 1, 24206, 'Unit Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:unit:add', '#', 'system', now(), NULL, NULL, '单位新增'),
    (24227, 1, 24206, 'Unit Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:unit:edit', '#', 'system', now(), NULL, NULL, '单位编辑'),
    (24228, 1, 24206, 'Unit Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:unit:remove', '#', 'system', now(), NULL, NULL, '单位删除'),
    (24229, 1, 24206, 'Unit Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:unit:reference', '#', 'system', now(), NULL, NULL, '单位引用检查'),
    (24219, 1, 24209, 'Manufacturer Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:manufacturer:list', '#', 'system', now(), NULL, NULL, '厂家查询'),
    (24220, 1, 24209, 'Manufacturer Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:manufacturer:add', '#', 'system', now(), NULL, NULL, '厂家新增'),
    (24221, 1, 24209, 'Manufacturer Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:manufacturer:edit', '#', 'system', now(), NULL, NULL, '厂家编辑'),
    (24222, 1, 24209, 'Manufacturer Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:manufacturer:remove', '#', 'system', now(), NULL, NULL, '厂家删除'),
    (24223, 1, 24209, 'Manufacturer Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:manufacturer:reference', '#', 'system', now(), NULL, NULL, '厂家引用检查'),
    (24224, 1, 24209, 'Manufacturer Change Status', 'productCenter.common.status', 6, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:manufacturer:changeStatus', '#', 'system', now(), NULL, NULL, '厂家审核/取消审核'),
    (24290, 1, 24209, 'Manufacturer Export', 'common.export', 7, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:manufacturer:export', '#', 'system', now(), NULL, NULL, '厂家导出'),
    (24285, 1, 24207, 'Material Type Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-type:list', '#', 'system', now(), NULL, NULL, '物料类型查询'),
    (24286, 1, 24207, 'Material Type Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-type:add', '#', 'system', now(), NULL, NULL, '物料类型新增'),
    (24287, 1, 24207, 'Material Type Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-type:edit', '#', 'system', now(), NULL, NULL, '物料类型编辑'),
    (24288, 1, 24207, 'Material Type Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-type:remove', '#', 'system', now(), NULL, NULL, '物料类型删除'),
    (24289, 1, 24207, 'Material Type Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-type:reference', '#', 'system', now(), NULL, NULL, '物料类型引用检查'),
    (24295, 1, 24207, 'Material Type Change Status', 'productCenter.common.status', 6, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-type:changeStatus', '#', 'system', now(), NULL, NULL, '物料类型状态变更'),
    (24296, 1, 24207, 'Material Type Export', 'common.export', 7, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-type:export', '#', 'system', now(), NULL, NULL, '物料类型导出'),
    (24230, 1, 24204, 'Material Attribute Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base-attribute:list', '#', 'system', now(), NULL, NULL, '物料属性查询'),
    (24231, 1, 24204, 'Material Attribute Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base-attribute:add', '#', 'system', now(), NULL, NULL, '物料属性新增'),
    (24232, 1, 24204, 'Material Attribute Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base-attribute:edit', '#', 'system', now(), NULL, NULL, '物料属性编辑'),
    (24233, 1, 24204, 'Material Attribute Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base-attribute:remove', '#', 'system', now(), NULL, NULL, '物料属性删除'),
    (24234, 1, 24204, 'Material Attribute Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base-attribute:reference', '#', 'system', now(), NULL, NULL, '物料属性引用检查'),
    (24297, 1, 24204, 'Material Attribute Change Status', 'productCenter.common.status', 6, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base-attribute:changeStatus', '#', 'system', now(), NULL, NULL, '物料属性状态变更'),
    (24298, 1, 24204, 'Material Attribute Export', 'common.export', 7, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base-attribute:export', '#', 'system', now(), NULL, NULL, '物料属性导出'),
    (24235, 1, 24213, 'Base Dictionary Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:dict:list', '#', 'system', now(), NULL, NULL, '基础字典查询'),
    (24236, 1, 24213, 'Base Dictionary Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:dict:add', '#', 'system', now(), NULL, NULL, '基础字典新增'),
    (24237, 1, 24213, 'Base Dictionary Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:dict:edit', '#', 'system', now(), NULL, NULL, '基础字典编辑'),
    (24238, 1, 24213, 'Base Dictionary Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:dict:remove', '#', 'system', now(), NULL, NULL, '基础字典删除'),
    (24239, 1, 24213, 'Base Dictionary Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:dict:reference', '#', 'system', now(), NULL, NULL, '基础字典引用检查'),
    (24240, 1, 24202, 'Material Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:list', '#', 'system', now(), NULL, NULL, '物料查询'),
    (24241, 1, 24202, 'Material Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:add', '#', 'system', now(), NULL, NULL, '物料新增'),
    (24242, 1, 24202, 'Material Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:edit', '#', 'system', now(), NULL, NULL, '物料编辑'),
    (24243, 1, 24202, 'Material Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:remove', '#', 'system', now(), NULL, NULL, '物料删除'),
    (24244, 1, 24202, 'Material Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:reference', '#', 'system', now(), NULL, NULL, '物料引用检查'),
    (24245, 1, 24202, 'Material Super Edit', 'productCenter.common.superEdit', 6, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:superEdit', '#', 'system', now(), NULL, NULL, '物料超级修改'),
    (24280, 1, 24208, 'Material Attribute Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-attribute:list', '#', 'system', now(), NULL, NULL, '物料属性查询'),
    (24281, 1, 24208, 'Material Attribute Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-attribute:add', '#', 'system', now(), NULL, NULL, '物料属性新增'),
    (24282, 1, 24208, 'Material Attribute Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-attribute:edit', '#', 'system', now(), NULL, NULL, '物料属性编辑'),
    (24283, 1, 24208, 'Material Attribute Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-attribute:remove', '#', 'system', now(), NULL, NULL, '物料属性删除'),
    (24284, 1, 24208, 'Material Attribute Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-attribute:reference', '#', 'system', now(), NULL, NULL, '物料属性引用检查'),
    (24260, 1, 24205, 'Asset Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:list', '#', 'system', now(), NULL, NULL, '资料资产查询'),
    (24261, 1, 24205, 'Asset Upload', 'common.upload', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:upload', '#', 'system', now(), NULL, NULL, '资料资产上传'),
    (24263, 1, 24205, 'Asset Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:edit', '#', 'system', now(), NULL, NULL, '资料资产编辑'),
    (24264, 1, 24205, 'Asset Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:remove', '#', 'system', now(), NULL, NULL, '资料资产删除'),
    (24262, 1, 24205, 'Asset Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:reference', '#', 'system', now(), NULL, NULL, '资料资产引用检查'),
    (24270, 1, 24210, 'Binding Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:list', '#', 'system', now(), NULL, NULL, '资料绑定查询'),
    (24271, 1, 24210, 'Binding Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:bind', '#', 'system', now(), NULL, NULL, '资料绑定新增'),
    (24272, 1, 24210, 'Binding Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:bind', '#', 'system', now(), NULL, NULL, '资料绑定编辑'),
    (24274, 1, 24210, 'Binding Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:unbind', '#', 'system', now(), NULL, NULL, '资料绑定删除'),
    (24273, 1, 24210, 'Binding Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:reference', '#', 'system', now(), NULL, NULL, '资料绑定引用检查')
ON CONFLICT (menu_id) DO UPDATE
SET tenant_id = EXCLUDED.tenant_id,
    parent_id = EXCLUDED.parent_id,
    menu_name = EXCLUDED.menu_name,
    i18n_key = EXCLUDED.i18n_key,
    order_num = EXCLUDED.order_num,
    path = EXCLUDED.path,
    component = EXCLUDED.component,
    query_param = EXCLUDED.query_param,
    is_frame = EXCLUDED.is_frame,
    is_cache = EXCLUDED.is_cache,
    menu_type = EXCLUDED.menu_type,
    visible = EXCLUDED.visible,
    status = EXCLUDED.status,
    perms = EXCLUDED.perms,
    icon = EXCLUDED.icon,
    update_by = 'system',
    update_time = now(),
    remark = EXCLUDED.remark;

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT 1, menu_id, 1
FROM sys_menu
WHERE menu_id BETWEEN 24200 AND 24299
ON CONFLICT (role_id, menu_id) DO NOTHING;

DELETE FROM sys_dict_data
WHERE dict_type IN (
    'product_unit',
    'product_unit_type',
    'product_business_type',
    'product_asset_type'
);
DELETE FROM sys_dict_type
WHERE dict_type IN (
    'product_unit',
    'product_unit_type',
    'product_business_type',
    'product_asset_type'
);
