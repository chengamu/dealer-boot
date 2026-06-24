-- 共享产品能力中心 Batch 1 PostgreSQL 草案。
-- 本文件用于开发评审和初始化草案，不会自动执行。
-- 基础资料段以 PostgreSQL 为准，时间字段统一使用 UTC 语义 timestamptz。

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
COMMENT ON COLUMN pc_material.manufacturer_code IS '厂家编码';
COMMENT ON COLUMN pc_material.manufacturer_name IS '厂家名称';
COMMENT ON COLUMN pc_material.manufacturer_item_no IS '厂家料号/供应商料号';
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
    (118005, 1, 'product_asset_type', '资料类型', 'Asset Type', 'BASE', true, true, 'ENABLED', 50, '资料资产类型', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118101, 1, 'engineering_item_type', '工程构成项类型', 'Engineering Item Type', 'ENGINEERING', true, true, 'ENABLED', 110, '工程构成项类型', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118102, 1, 'engineering_scope_type', '可选范围类型', 'Engineering Scope Type', 'ENGINEERING', true, true, 'ENABLED', 120, '工程可选范围类型', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118103, 1, 'engineering_rule_source', '规则条件来源', 'Engineering Rule Source', 'ENGINEERING', true, true, 'ENABLED', 130, '工程规则条件来源', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118104, 1, 'engineering_rule_type', '规则类型', 'Engineering Rule Type', 'ENGINEERING', true, true, 'ENABLED', 140, '工程规则类型', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118105, 1, 'engineering_rule_operator', '规则操作符', 'Engineering Rule Operator', 'ENGINEERING', true, true, 'ENABLED', 150, '工程规则操作符', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118106, 1, 'engineering_rule_action', '能力规则动作', 'Engineering Rule Action', 'ENGINEERING', true, true, 'ENABLED', 160, '工程能力规则动作', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118107, 1, 'engineering_output_type', '带出类型', 'Engineering Output Type', 'ENGINEERING', true, true, 'ENABLED', 170, '工程带出对象类型', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118108, 1, 'engineering_qty_mode', '数量模式', 'Engineering Qty Mode', 'ENGINEERING', true, true, 'ENABLED', 180, '工程数量模式', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118109, 1, 'engineering_severity', '规则严重级别', 'Engineering Severity', 'ENGINEERING', true, true, 'ENABLED', 190, '工程规则严重级别', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118201, 1, 'config_input_type', '配置输入类型', 'Config Input Type', 'CONFIG', true, true, 'ENABLED', 210, '产品配置输入类型预留', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118202, 1, 'config_option_source_type', '配置答案来源类型', 'Config Option Source Type', 'CONFIG', true, true, 'ENABLED', 220, '产品配置答案来源类型预留', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118203, 1, 'config_question_scope', '配置问题范围', 'Config Question Scope', 'CONFIG', true, true, 'ENABLED', 230, '产品配置问题范围预留', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00')
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
    (119406, 1, 'product_asset_type', 'OTHER', '其他', 'Other', NULL, true, true, 'ENABLED', 90, '其他资料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119501, 1, 'engineering_item_type', 'MAIN_FABRIC', '主面料', 'Main Fabric', NULL, true, true, 'ENABLED', 10, '主面料构成项', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119502, 1, 'engineering_item_type', 'SECONDARY_FABRIC', '副面料', 'Secondary Fabric', NULL, true, true, 'ENABLED', 20, '副面料构成项', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119503, 1, 'engineering_item_type', 'PROFILE', '铝材/下杆/轨道', 'Profile / Rail / Track', NULL, true, true, 'ENABLED', 30, '型材类构成项', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119504, 1, 'engineering_item_type', 'SYSTEM', '系统', 'System', NULL, true, true, 'ENABLED', 40, '系统构成项', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119505, 1, 'engineering_item_type', 'ACCESSORY', '配件', 'Accessory', NULL, true, true, 'ENABLED', 50, '配件构成项', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119506, 1, 'engineering_item_type', 'INSTALL_PACK', '安装零件包', 'Install Pack', NULL, true, true, 'ENABLED', 60, '安装零件包', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119507, 1, 'engineering_item_type', 'PACKAGING', '包装方式', 'Packaging', NULL, true, true, 'ENABLED', 70, '包装方式', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119508, 1, 'engineering_item_type', 'MEDIA', '资料', 'Media', NULL, true, true, 'ENABLED', 80, '资料构成项', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119601, 1, 'engineering_scope_type', 'MATERIAL_TYPE', '物料类型', 'Material Type', NULL, true, true, 'ENABLED', 10, '按物料类型选范围', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119602, 1, 'engineering_scope_type', 'FABRIC_SERIES', '面料系列', 'Fabric Series', NULL, true, true, 'ENABLED', 20, '按面料系列选范围', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119603, 1, 'engineering_scope_type', 'COMPONENT_TYPE', '组件包类型', 'Component Type', NULL, true, true, 'ENABLED', 30, '按组件包类型选范围', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119604, 1, 'engineering_scope_type', 'MEDIA_TYPE', '资料类型', 'Media Type', NULL, true, true, 'ENABLED', 40, '按资料类型选范围', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119605, 1, 'engineering_scope_type', 'MATERIAL_CODE', '指定物料', 'Specified Material', NULL, true, true, 'ENABLED', 50, '指定物料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119606, 1, 'engineering_scope_type', 'COMPONENT_CODE', '指定组件包', 'Specified Component', NULL, true, true, 'ENABLED', 60, '指定组件包', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119607, 1, 'engineering_scope_type', 'MEDIA_CODE', '指定资料', 'Specified Media', NULL, true, true, 'ENABLED', 70, '指定资料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119701, 1, 'engineering_rule_source', 'MATERIAL', '物料', 'Material', NULL, true, true, 'ENABLED', 10, '规则条件可引用物料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119702, 1, 'engineering_rule_source', 'COMPONENT', '组件包', 'Component Pack', NULL, true, true, 'ENABLED', 20, '规则条件可引用组件包', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119703, 1, 'engineering_rule_source', 'FABRIC_SERIES', '面料系列', 'Fabric Series', NULL, true, true, 'ENABLED', 30, '规则条件可引用面料系列', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119704, 1, 'engineering_rule_source', 'BASE_ATTRIBUTE', '物料属性', 'Material Attribute', NULL, true, true, 'ENABLED', 40, '规则条件可引用物料属性', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119705, 1, 'engineering_rule_source', 'MEDIA', '资料附件', 'Media', NULL, true, true, 'ENABLED', 50, '规则条件可引用资料附件', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119711, 1, 'engineering_rule_type', 'DIMENSION_LIMIT', '尺寸限制', 'Dimension Limit', NULL, true, true, 'ENABLED', 10, '尺寸限制规则', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119712, 1, 'engineering_rule_type', 'THICKNESS_LIMIT', '厚度限制', 'Thickness Limit', NULL, true, true, 'ENABLED', 20, '厚度限制规则', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119713, 1, 'engineering_rule_type', 'OPTION_LIMIT', '选项限制', 'Option Limit', NULL, true, true, 'ENABLED', 30, '选项限制规则', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119714, 1, 'engineering_rule_type', 'COMPLETENESS', '完整性要求', 'Completeness', NULL, true, true, 'ENABLED', 40, '完整性要求规则', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119721, 1, 'engineering_rule_operator', 'EQ', '等于', 'Equals', NULL, true, true, 'ENABLED', 10, '等于', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119722, 1, 'engineering_rule_operator', 'NE', '不等于', 'Not Equals', NULL, true, true, 'ENABLED', 20, '不等于', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119723, 1, 'engineering_rule_operator', 'GT', '大于', 'Greater Than', NULL, true, true, 'ENABLED', 30, '大于', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119724, 1, 'engineering_rule_operator', 'GTE', '大于等于', 'Greater Or Equal', NULL, true, true, 'ENABLED', 40, '大于等于', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119725, 1, 'engineering_rule_operator', 'LT', '小于', 'Less Than', NULL, true, true, 'ENABLED', 50, '小于', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119726, 1, 'engineering_rule_operator', 'LTE', '小于等于', 'Less Or Equal', NULL, true, true, 'ENABLED', 60, '小于等于', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119731, 1, 'engineering_rule_action', 'DISABLE_OPTION', '禁用选项', 'Disable Option', NULL, true, true, 'ENABLED', 10, '禁用选项', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119732, 1, 'engineering_rule_action', 'WARN', '提示', 'Warn', NULL, true, true, 'ENABLED', 20, '提示', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119733, 1, 'engineering_rule_action', 'BLOCK', '阻断', 'Block', NULL, true, true, 'ENABLED', 30, '阻断', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119734, 1, 'engineering_rule_action', 'REQUIRE_ITEM', '要求补齐构成项', 'Require Item', NULL, true, true, 'ENABLED', 40, '要求补齐构成项', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119735, 1, 'engineering_rule_action', 'MEDIA_HINT', '附件资料提示', 'Media Hint', NULL, true, true, 'ENABLED', 50, '附件资料提示', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119741, 1, 'engineering_output_type', 'COMPONENT', '组件包', 'Component Pack', NULL, true, true, 'ENABLED', 10, '带出组件包', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119742, 1, 'engineering_output_type', 'MATERIAL', '物料', 'Material', NULL, true, true, 'ENABLED', 20, '带出物料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119743, 1, 'engineering_output_type', 'MEDIA', '资料', 'Media', NULL, true, true, 'ENABLED', 30, '带出资料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119751, 1, 'engineering_qty_mode', 'FIXED', '固定数量', 'Fixed Qty', NULL, true, true, 'ENABLED', 10, '固定数量', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119752, 1, 'engineering_qty_mode', 'ORDER_QTY', '按订单数量', 'By Order Qty', NULL, true, true, 'ENABLED', 20, '按订单数量', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119753, 1, 'engineering_qty_mode', 'CURTAIN_QTY', '按帘数量', 'By Curtain Qty', NULL, true, true, 'ENABLED', 30, '按帘数量', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119761, 1, 'engineering_severity', 'INFO', '提示', 'Info', NULL, true, true, 'ENABLED', 10, '提示级别', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119762, 1, 'engineering_severity', 'WARNING', '警告', 'Warning', NULL, true, true, 'ENABLED', 20, '警告级别', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119763, 1, 'engineering_severity', 'BLOCKER', '阻断', 'Blocker', NULL, true, true, 'ENABLED', 30, '阻断级别', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (120101, 1, 'config_input_type', 'TEXT', '文本', 'Text', NULL, true, true, 'ENABLED', 10, '文本输入', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (120102, 1, 'config_input_type', 'NUMBER', '数字', 'Number', NULL, true, true, 'ENABLED', 20, '数字输入', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (120103, 1, 'config_input_type', 'SINGLE_SELECT', '单选', 'Single Select', NULL, true, true, 'ENABLED', 30, '单选', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (120104, 1, 'config_input_type', 'MULTI_SELECT', '多选', 'Multi Select', NULL, true, true, 'ENABLED', 40, '多选', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (120105, 1, 'config_input_type', 'BOOLEAN', '是否', 'Boolean', NULL, true, true, 'ENABLED', 50, '是否输入', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (120201, 1, 'config_option_source_type', 'MANUAL', '手工维护', 'Manual', NULL, true, true, 'ENABLED', 10, '手工维护', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (120202, 1, 'config_option_source_type', 'BASE_ATTRIBUTE', '物料属性', 'Material Attribute', NULL, true, true, 'ENABLED', 20, '物料属性来源', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (120203, 1, 'config_option_source_type', 'MATERIAL', '物料', 'Material', NULL, true, true, 'ENABLED', 30, '物料来源', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (120205, 1, 'config_option_source_type', 'FABRIC_SERIES', '面料系列', 'Fabric Series', NULL, true, true, 'ENABLED', 40, '面料系列来源', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (120206, 1, 'config_option_source_type', 'COMPONENT', '组件包', 'Component Pack', NULL, true, true, 'ENABLED', 50, '组件包来源', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (120207, 1, 'config_option_source_type', 'MEDIA_ASSET', '资料资产', 'Media Asset', NULL, true, true, 'ENABLED', 60, '资料资产来源', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00')
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

CREATE TABLE IF NOT EXISTS pc_engineering_plan (
    plan_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    plan_code varchar(80) NOT NULL,
    plan_name_cn varchar(200) NOT NULL,
    plan_name_en varchar(200),
    category_id bigint,
    category_code varchar(80),
    category_name_cn varchar(200),
    category_name_en varchar(200),
    series_id bigint,
    series_code varchar(80),
    series_name_cn varchar(200),
    series_name_en varchar(200),
    current_version_id bigint,
    current_version_no varchar(80),
    biz_status varchar(40) NOT NULL DEFAULT 'DRAFT',
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
COMMENT ON TABLE pc_engineering_plan IS '工程方案表';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_engineering_plan_code_active ON pc_engineering_plan (tenant_id, plan_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_engineering_plan_series ON pc_engineering_plan (tenant_id, series_code, status);

CREATE TABLE IF NOT EXISTS pc_engineering_plan_version (
    version_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    plan_id bigint NOT NULL,
    plan_code varchar(80) NOT NULL,
    version_no varchar(80) NOT NULL,
    version_name varchar(200),
    biz_status varchar(40) NOT NULL DEFAULT 'DRAFT',
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    rule_schema_version varchar(40) DEFAULT 'JSON_V1',
    config_json jsonb,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);
COMMENT ON TABLE pc_engineering_plan_version IS '工程方案版本表';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_engineering_version_active ON pc_engineering_plan_version (tenant_id, plan_id, version_no) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_engineering_version_plan ON pc_engineering_plan_version (tenant_id, plan_id, biz_status);

CREATE TABLE IF NOT EXISTS pc_engineering_item (
    item_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    version_id bigint NOT NULL,
    plan_id bigint,
    item_code varchar(80) NOT NULL,
    item_name_cn varchar(200) NOT NULL,
    item_name_en varchar(200),
    item_type varchar(80) NOT NULL,
    source_type varchar(80) NOT NULL,
    required_flag varchar(1) NOT NULL DEFAULT '0',
    multi_select_flag varchar(1) NOT NULL DEFAULT '0',
    customer_selectable varchar(1) NOT NULL DEFAULT '1',
    default_source_code varchar(120),
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    sort_order integer DEFAULT 0,
    extra_json jsonb,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);
COMMENT ON TABLE pc_engineering_item IS '工程构成项表';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_engineering_item_code_active ON pc_engineering_item (tenant_id, version_id, item_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_engineering_item_version ON pc_engineering_item (tenant_id, version_id, status, sort_order);

CREATE TABLE IF NOT EXISTS pc_engineering_item_scope (
    scope_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    version_id bigint NOT NULL,
    item_id bigint NOT NULL,
    item_code varchar(80) NOT NULL,
    scope_type varchar(80) NOT NULL,
    scope_code varchar(120) NOT NULL,
    scope_name_cn varchar(200),
    scope_name_en varchar(200),
    include_flag varchar(20) NOT NULL DEFAULT 'INCLUDE',
    condition_json jsonb,
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
COMMENT ON TABLE pc_engineering_item_scope IS '工程构成项可选范围表';
CREATE INDEX IF NOT EXISTS idx_pc_engineering_scope_item ON pc_engineering_item_scope (tenant_id, version_id, item_id, status, sort_order);
CREATE INDEX IF NOT EXISTS idx_pc_engineering_scope_code ON pc_engineering_item_scope (tenant_id, scope_type, scope_code);

CREATE TABLE IF NOT EXISTS pc_engineering_rule (
    rule_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    version_id bigint NOT NULL,
    rule_code varchar(80) NOT NULL,
    rule_name_cn varchar(200) NOT NULL,
    rule_name_en varchar(200),
    rule_type varchar(80) NOT NULL,
    condition_json jsonb,
    action_json jsonb,
    severity varchar(40) NOT NULL DEFAULT 'WARNING',
    message_cn varchar(500),
    message_en varchar(500),
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
COMMENT ON TABLE pc_engineering_rule IS '工程能力规则表';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_engineering_rule_code_active ON pc_engineering_rule (tenant_id, version_id, rule_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_engineering_rule_version ON pc_engineering_rule (tenant_id, version_id, status, sort_order);

CREATE TABLE IF NOT EXISTS pc_engineering_output_rule (
    output_rule_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    version_id bigint NOT NULL,
    rule_code varchar(80) NOT NULL,
    rule_name_cn varchar(200) NOT NULL,
    rule_name_en varchar(200),
    condition_json jsonb,
    output_type varchar(80) NOT NULL,
    output_code varchar(120) NOT NULL,
    output_name_cn varchar(200),
    output_name_en varchar(200),
    default_qty numeric(18,6),
    unit_code varchar(80),
    reason_cn varchar(500),
    reason_en varchar(500),
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
COMMENT ON TABLE pc_engineering_output_rule IS '工程带出规则表';
CREATE INDEX IF NOT EXISTS idx_pc_engineering_output_version ON pc_engineering_output_rule (tenant_id, version_id, status, sort_order);
CREATE INDEX IF NOT EXISTS idx_pc_engineering_output_code ON pc_engineering_output_rule (tenant_id, output_type, output_code);

CREATE TABLE IF NOT EXISTS pc_standard_sku_engineering (
    sku_engineering_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    version_id bigint NOT NULL,
    standard_sku_id bigint,
    standard_sku_code varchar(80) NOT NULL,
    standard_sku_name_cn varchar(200),
    standard_sku_name_en varchar(200),
    fixed_items_json jsonb,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);
COMMENT ON TABLE pc_standard_sku_engineering IS '标品固定工程配置表';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_standard_sku_eng_active ON pc_standard_sku_engineering (tenant_id, version_id, standard_sku_code) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS pc_engineering_check_case (
    check_case_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    version_id bigint NOT NULL,
    case_code varchar(80) NOT NULL,
    case_name_cn varchar(200) NOT NULL,
    case_name_en varchar(200),
    input_json jsonb NOT NULL,
    expected_json jsonb,
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
COMMENT ON TABLE pc_engineering_check_case IS '工程配置检查用例表';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_engineering_case_active ON pc_engineering_check_case (tenant_id, version_id, case_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_engineering_case_version ON pc_engineering_check_case (tenant_id, version_id, status, sort_order);

CREATE TABLE IF NOT EXISTS pc_sales_product (
    sales_product_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    sales_product_code varchar(80) NOT NULL,
    sales_product_name_cn varchar(200) NOT NULL,
    sales_product_name_en varchar(200),
    category_id bigint,
    category_code varchar(80),
    category_name_cn varchar(200),
    category_name_en varchar(200),
    product_type varchar(80),
    sales_mode varchar(80),
    template_id bigint,
    template_code varchar(80),
    template_version_id bigint,
    template_version_no varchar(80),
    default_width numeric(18,6),
    default_height numeric(18,6),
    dimension_unit varchar(80),
    biz_status varchar(40) NOT NULL DEFAULT 'DRAFT',
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

COMMENT ON TABLE pc_sales_product IS '销售产品表';
COMMENT ON COLUMN pc_sales_product.sales_product_id IS '销售产品ID';
COMMENT ON COLUMN pc_sales_product.sales_product_code IS '销售产品编码';
COMMENT ON COLUMN pc_sales_product.sales_product_name_cn IS '销售产品中文名称';
COMMENT ON COLUMN pc_sales_product.sales_product_name_en IS '销售产品英文名称';
COMMENT ON COLUMN pc_sales_product.category_id IS '产品分类ID';
COMMENT ON COLUMN pc_sales_product.category_code IS '产品分类编码';
COMMENT ON COLUMN pc_sales_product.category_name_cn IS '产品分类中文名称快照';
COMMENT ON COLUMN pc_sales_product.category_name_en IS '产品分类英文名称快照';
COMMENT ON COLUMN pc_sales_product.product_type IS '产品类型';
COMMENT ON COLUMN pc_sales_product.sales_mode IS '销售模式';
COMMENT ON COLUMN pc_sales_product.template_id IS '默认配置模板ID';
COMMENT ON COLUMN pc_sales_product.template_code IS '默认配置模板编码';
COMMENT ON COLUMN pc_sales_product.template_version_id IS '默认配置模板版本ID';
COMMENT ON COLUMN pc_sales_product.template_version_no IS '默认配置模板版本号';
COMMENT ON COLUMN pc_sales_product.default_width IS '默认宽度';
COMMENT ON COLUMN pc_sales_product.default_height IS '默认高度';
COMMENT ON COLUMN pc_sales_product.dimension_unit IS '尺寸单位';
COMMENT ON COLUMN pc_sales_product.biz_status IS '业务状态';
COMMENT ON COLUMN pc_sales_product.status IS '状态：ENABLED启用，DISABLED停用';
COMMENT ON COLUMN pc_sales_product.del_flag IS '删除标志：0存在，2删除';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_sales_product_code_active ON pc_sales_product (tenant_id, sales_product_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_sales_product_category_status ON pc_sales_product (tenant_id, category_code, status);
CREATE INDEX IF NOT EXISTS idx_pc_sales_product_template ON pc_sales_product (tenant_id, template_id, template_version_id);

CREATE TABLE IF NOT EXISTS pc_standard_sku (
    standard_sku_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    sku_code varchar(80) NOT NULL,
    sku_name_cn varchar(200) NOT NULL,
    sku_name_en varchar(200),
    sales_product_id bigint,
    sales_product_code varchar(80),
    width_value numeric(18,6),
    height_value numeric(18,6),
    dimension_unit varchar(80),
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

COMMENT ON TABLE pc_standard_sku IS '标品SKU预留表';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_standard_sku_code_active ON pc_standard_sku (tenant_id, sku_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_standard_sku_product ON pc_standard_sku (tenant_id, sales_product_id, status);

CREATE TABLE IF NOT EXISTS pc_config_template (
    template_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    template_code varchar(80) NOT NULL,
    template_name_cn varchar(200) NOT NULL,
    template_name_en varchar(200),
    product_model_id bigint,
    product_model_code varchar(80),
    sales_product_id bigint,
    sales_product_code varchar(80),
    current_version_id bigint,
    current_version_no varchar(80),
    published_version_id bigint,
    published_version_no varchar(80),
    biz_status varchar(40) NOT NULL DEFAULT 'DRAFT',
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_config_template IS '配置模板表';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_config_template_code_active ON pc_config_template (tenant_id, template_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_config_template_sales_product ON pc_config_template (tenant_id, sales_product_id, status);

CREATE TABLE IF NOT EXISTS pc_config_template_version (
    template_version_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    template_id bigint NOT NULL,
    template_code varchar(80) NOT NULL,
    version_no varchar(80) NOT NULL,
    version_name varchar(200),
    version_status varchar(40) NOT NULL DEFAULT 'DRAFT',
    product_model_id bigint,
    product_model_code varchar(80),
    sales_product_id bigint,
    sales_product_code varchar(80),
    sales_variant_id bigint,
    sales_variant_code varchar(80),
    price_plan_version_id bigint,
    price_plan_code varchar(80),
    schema_json jsonb,
    draft_hash varchar(120),
    effective_from timestamptz,
    effective_to timestamptz,
    published_package_id bigint,
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_config_template_version IS '配置模板版本表';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_config_template_version_active ON pc_config_template_version (tenant_id, template_id, version_no) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_config_template_version_status ON pc_config_template_version (tenant_id, version_status);

CREATE TABLE IF NOT EXISTS pc_question_group (
    question_group_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    group_code varchar(80) NOT NULL,
    group_name_cn varchar(200) NOT NULL,
    group_name_en varchar(200),
    description_cn varchar(500),
    description_en varchar(500),
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

COMMENT ON TABLE pc_question_group IS '配置问题组表';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_question_group_code_active ON pc_question_group (tenant_id, group_code) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS pc_config_question (
    question_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    template_version_id bigint NOT NULL,
    question_group_id bigint,
    question_code varchar(80) NOT NULL,
    question_name_cn varchar(200) NOT NULL,
    question_name_en varchar(200),
    help_text_cn varchar(500),
    help_text_en varchar(500),
    input_type varchar(80) NOT NULL,
    required_flag varchar(1) NOT NULL DEFAULT '0',
    customer_visible varchar(1) NOT NULL DEFAULT '1',
    default_value varchar(200),
    validation_json jsonb,
    display_rule_json jsonb,
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

COMMENT ON TABLE pc_config_question IS '配置问题表';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_config_question_code_active ON pc_config_question (tenant_id, template_version_id, question_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_config_question_group ON pc_config_question (tenant_id, question_group_id, status);

CREATE TABLE IF NOT EXISTS pc_config_option (
    option_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    question_id bigint NOT NULL,
    template_version_id bigint NOT NULL,
    option_code varchar(80) NOT NULL,
    option_name_cn varchar(200) NOT NULL,
    option_name_en varchar(200),
    option_value varchar(200),
    source_type varchar(80),
    source_ref_id bigint,
    source_code varchar(80),
    source_name varchar(200),
    display_name_cn varchar(200),
    display_name_en varchar(200),
    value_code varchar(80),
    help_text_cn varchar(500),
    help_text_en varchar(500),
    component_json jsonb,
    media_json jsonb,
    price_impact_json jsonb,
    rule_json jsonb,
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

COMMENT ON TABLE pc_config_option IS '配置答案表';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_config_option_code_active ON pc_config_option (tenant_id, question_id, option_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_config_option_source ON pc_config_option (tenant_id, source_type, source_ref_id, status);

CREATE TABLE IF NOT EXISTS pc_config_rule (
    rule_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    template_version_id bigint NOT NULL,
    rule_code varchar(80) NOT NULL,
    rule_name_cn varchar(200) NOT NULL,
    rule_name_en varchar(200),
    rule_type varchar(80) NOT NULL,
    priority integer DEFAULT 0,
    condition_json jsonb,
    action_json jsonb,
    error_message_cn varchar(500),
    error_message_en varchar(500),
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_config_rule IS '配置规则表';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_config_rule_code_active ON pc_config_rule (tenant_id, template_version_id, rule_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_config_rule_type_status ON pc_config_rule (tenant_id, rule_type, status);

-- =====================================================
-- 第一阶段正式菜单、按钮和字典归一化
-- 说明：
-- 1. 正式侧边栏使用四个一级菜单：基础信息、产品工程设计、产品配置中心、产品发布。
-- 2. 菜单最多两级；页面内 tabs 只能作为局部细节，不承载正式主入口。
-- 3. 本块必须保持幂等，可重复执行到开发库。
-- =====================================================

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
    (24200, 1, 0, 'Basic Information', 'productCenter.menu.masterData', 30, 'product-master', NULL, NULL, '1', '0', 'M', '1', '1', NULL, 'product', 'system', now(), NULL, NULL, '产品能力-基础信息'),
    (24500, 1, 0, 'Product Engineering Design', 'productCenter.menu.engineering', 31, 'product-engineering', NULL, NULL, '1', '0', 'M', '1', '1', NULL, 'tool', 'system', now(), NULL, NULL, '产品工程设计'),
    (24300, 1, 0, 'Product Configuration Center', 'productCenter.menu.configPricing', 32, 'product-config', NULL, NULL, '1', '0', 'M', '1', '1', NULL, 'build', 'system', now(), NULL, NULL, '产品配置中心'),
    (24400, 1, 0, 'Product Release', 'productCenter.menu.releaseApplication', 33, 'product-release', NULL, NULL, '1', '0', 'M', '1', '1', NULL, 'deployment-unit', 'system', now(), NULL, NULL, '产品发布')
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
    (24207, 1, 24200, 'Material Types', 'productCenter.menu.materialTypes', 4, 'material-types', 'product-center/material-types', NULL, '1', '0', 'C', '1', '1', 'product:material-type:list', 'tree-table', 'system', now(), NULL, NULL, '物料类型'),
    (24204, 1, 24200, 'Material Attributes', 'productCenter.menu.baseAttributes', 5, 'base-attributes', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base-attribute:list', 'dict', 'system', now(), NULL, NULL, '物料属性'),
    (24202, 1, 24200, 'Materials', 'productCenter.menu.materials', 6, 'materials', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'inventory', 'system', now(), NULL, NULL, '物料管理'),
    (24208, 1, 24200, 'Material Attribute Values', 'productCenter.menu.materialAttributes', 89, 'material-attributes', 'product-center/base', NULL, '1', '0', 'C', '0', '1', 'product:material-attribute:list', 'list', 'system', now(), NULL, NULL, '物料属性值，从物料管理抽屉同步维护'),
    (24205, 1, 24200, 'Media Assets', 'productCenter.menu.mediaAssets', 94, 'media-assets', 'product-center/assets', NULL, '1', '0', 'C', '0', '1', 'product:asset:list', 'upload', 'system', now(), NULL, NULL, '资料资产'),
    (24210, 1, 24200, 'Media Bindings', 'productCenter.menu.mediaBindings', 94, 'media-bindings', 'product-center/assets', NULL, '1', '0', 'C', '0', '1', 'product:asset:list', 'link', 'system', now(), NULL, NULL, '资料绑定，附件关联台账，默认不在基础资料一级菜单展示'),
    (24501, 1, 24500, 'Product Engineering Design', 'productCenter.menu.engineeringWorkbench', 1, 'workbench', 'product-engineering/workbench', NULL, '1', '0', 'C', '1', '1', 'product:engineering:list', 'tool', 'system', now(), NULL, NULL, '产品工程设计工作台'),
    (24502, 1, 24500, 'Engineering Plan', 'productCenter.menu.engineeringPlan', 2, 'plan', 'product-engineering/plan', NULL, '1', '0', 'C', '1', '1', 'product:engineering:list', 'tree-table', 'system', now(), NULL, NULL, '工程方案'),
    (24503, 1, 24500, 'Engineering Items', 'productCenter.menu.engineeringItem', 3, 'item', 'product-engineering/item', NULL, '1', '0', 'C', '1', '1', 'product:engineering:list', 'component', 'system', now(), NULL, NULL, '工程构成项'),
    (24504, 1, 24500, 'Capability Rules', 'productCenter.menu.engineeringRule', 4, 'rule', 'product-engineering/rule', NULL, '1', '0', 'C', '1', '1', 'product:engineering:list', 'validCode', 'system', now(), NULL, NULL, '能力规则'),
    (24505, 1, 24500, 'Output Rules', 'productCenter.menu.engineeringOutputRule', 5, 'output-rule', 'product-engineering/output-rule', NULL, '1', '0', 'C', '1', '1', 'product:engineering:list', 'link', 'system', now(), NULL, NULL, '带出规则'),
    (24506, 1, 24500, 'Fixed Standard SKU', 'productCenter.menu.engineeringStandardSku', 6, 'standard-sku', 'product-engineering/standard-sku', NULL, '1', '0', 'C', '1', '1', 'product:engineering:list', 'barcode', 'system', now(), NULL, NULL, '标品固定配置'),
    (24507, 1, 24500, 'Engineering Preview', 'productCenter.menu.engineeringPreview', 7, 'preview', 'product-engineering/preview', NULL, '1', '0', 'C', '1', '1', 'product:engineering:preview', 'calculator', 'system', now(), NULL, NULL, '工程配置预览'),
    (24508, 1, 24500, 'Engineering Check', 'productCenter.menu.engineeringCheck', 8, 'check', 'product-engineering/check', NULL, '1', '0', 'C', '1', '1', 'product:engineering:check', 'check', 'system', now(), NULL, NULL, '工程配置检查'),
    (24520, 1, 24500, 'Engineering Plan Version', 'productCenter.menu.engineeringVersion', 91, 'version', 'product-engineering/version', NULL, '1', '0', 'C', '0', '1', 'product:engineering:list', 'version', 'system', now(), NULL, NULL, '工程方案版本，低频隐藏'),
    (24521, 1, 24500, 'Selectable Ranges', 'productCenter.menu.engineeringScope', 92, 'scope', 'product-engineering/scope', NULL, '1', '0', 'C', '0', '1', 'product:engineering:list', 'list', 'system', now(), NULL, NULL, '可选范围，低频隐藏'),
    (24522, 1, 24500, 'Check Cases', 'productCenter.menu.engineeringCheckCase', 93, 'check-case', 'product-engineering/check-case', NULL, '1', '0', 'C', '0', '1', 'product:engineering:list', 'example', 'system', now(), NULL, NULL, '检查用例，低频隐藏'),
    (24304, 1, 24300, 'Configuration Entry Workbench', 'productCenter.menu.template', 1, 'template', 'product-center/template', NULL, '1', '0', 'C', '1', '1', 'product:template:list', 'form', 'system', now(), NULL, NULL, '配置录入工作台'),
    (24301, 1, 24300, 'Sales Products', 'productCenter.menu.salesProducts', 2, 'sales-products', 'product-center/sales-products', NULL, '1', '0', 'C', '1', '1', 'product:sales-product:list', 'product', 'system', now(), NULL, NULL, '销售产品台账'),
    (24303, 1, 24300, 'Question Groups', 'productCenter.menu.questionGroups', 3, 'question-groups', 'product-center/question-groups', NULL, '1', '0', 'C', '1', '1', 'product:template:list', 'list', 'system', now(), NULL, NULL, '问题组模板'),
    (24307, 1, 24300, 'Config Questions', 'productCenter.menu.configQuestions', 4, 'config-questions', 'product-center/config-questions', NULL, '1', '0', 'C', '1', '1', 'product:template:list', 'question', 'system', now(), NULL, NULL, '配置问题'),
    (24308, 1, 24300, 'Config Options', 'productCenter.menu.configOptions', 5, 'config-options', 'product-center/config-options', NULL, '1', '0', 'C', '1', '1', 'product:template:list', 'list', 'system', now(), NULL, NULL, '配置答案'),
    (24309, 1, 24300, 'Config Evaluator', 'productCenter.menu.configEvaluator', 6, 'config-evaluator', 'product-center/config-evaluator', NULL, '1', '0', 'C', '1', '1', 'product:template:test', 'calculator', 'system', now(), NULL, NULL, '配置求值器'),
    (24302, 1, 24300, 'Standard SKU', 'productCenter.menu.standardSkus', 7, 'standard-skus', 'product-center/standard-skus', NULL, '1', '0', 'C', '0', '1', 'product:standard-sku:list', 'barcode', 'system', now(), NULL, NULL, '标品SKU预留入口'),
    (24305, 1, 24300, 'Pricing', 'productCenter.menu.pricing', 91, 'pricing', 'product-center/pricing', NULL, '1', '0', 'C', '0', '1', 'product:price:list', 'money', 'system', now(), NULL, NULL, '价格中心，本阶段不改造，默认隐藏'),
    (24306, 1, 24300, 'Quote Preview', 'productCenter.menu.quotePreview', 92, 'quote-preview', 'product-center/quote-preview', NULL, '1', '0', 'C', '0', '1', 'product:price:test', 'quote', 'system', now(), NULL, NULL, '报价预览，本阶段不改造，默认隐藏'),
    (24401, 1, 24400, 'Publish Gate', 'productCenter.menu.publish', 1, 'publish', 'product-center/publish', NULL, '1', '0', 'C', '1', '1', 'product:publish:list', 'check', 'system', now(), NULL, NULL, '发布入口'),
    (24402, 1, 24400, 'Approvals', 'productCenter.menu.approvals', 2, 'approvals', 'product-center/approvals', NULL, '1', '0', 'C', '1', '1', 'product:publish:list', 'validCode', 'system', now(), NULL, NULL, '审核审批'),
    (24403, 1, 24400, 'Gap Tasks', 'productCenter.menu.gapTasks', 3, 'gap-tasks', 'product-center/gap-tasks', NULL, '1', '0', 'C', '1', '1', 'product:publish:list', 'bug', 'system', now(), NULL, NULL, '缺口待办'),
    (24404, 1, 24400, 'Publish Packages', 'productCenter.menu.packages', 4, 'packages', 'product-center/packages', NULL, '1', '0', 'C', '1', '1', 'product:publish:list', 'zip', 'system', now(), NULL, NULL, '发布包'),
    (24405, 1, 24400, 'Sync Outbox', 'productCenter.menu.syncOutbox', 5, 'sync-outbox', 'product-center/sync-outbox', NULL, '1', '0', 'C', '1', '1', 'product:publish:list', 'erp', 'system', now(), NULL, NULL, '同步日志'),
    (24406, 1, 24400, 'Import Center', 'productCenter.menu.importCenter', 6, 'import', 'product-center/import', NULL, '1', '0', 'C', '1', '1', 'product:import:list', 'import', 'system', now(), NULL, NULL, '导入中心'),
    (24407, 1, 24400, 'Sales View', 'productCenter.menu.salesView', 7, 'sales-view', 'product-center/sales-view', NULL, '1', '0', 'C', '1', '1', 'product:sales-view:list', 'sales', 'system', now(), NULL, NULL, '销售只读总览')
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
    (24273, 1, 24210, 'Binding Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:reference', '#', 'system', now(), NULL, NULL, '资料绑定引用检查'),
    (24510, 1, 24501, 'Engineering Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:engineering:list', '#', 'system', now(), NULL, NULL, '工程配置查询'),
    (24511, 1, 24501, 'Engineering Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:engineering:add', '#', 'system', now(), NULL, NULL, '工程配置新增'),
    (24512, 1, 24501, 'Engineering Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:engineering:edit', '#', 'system', now(), NULL, NULL, '工程配置编辑'),
    (24513, 1, 24501, 'Engineering Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:engineering:remove', '#', 'system', now(), NULL, NULL, '工程配置删除'),
    (24514, 1, 24501, 'Engineering Preview', 'productCenter.engineering.preview', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:engineering:preview', '#', 'system', now(), NULL, NULL, '工程配置预览'),
    (24515, 1, 24501, 'Engineering Check', 'productCenter.engineering.check', 6, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:engineering:check', '#', 'system', now(), NULL, NULL, '工程配置检查'),
    (24310, 1, 24301, 'Sales Product Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:sales-product:list', '#', 'system', now(), NULL, NULL, '销售产品查询'),
    (24311, 1, 24301, 'Sales Product Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:sales-product:add', '#', 'system', now(), NULL, NULL, '销售产品新增'),
    (24312, 1, 24301, 'Sales Product Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:sales-product:edit', '#', 'system', now(), NULL, NULL, '销售产品编辑'),
    (24313, 1, 24301, 'Sales Product Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:sales-product:remove', '#', 'system', now(), NULL, NULL, '销售产品删除'),
    (24314, 1, 24301, 'Sales Product Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:sales-product:reference', '#', 'system', now(), NULL, NULL, '销售产品引用检查'),
    (24330, 1, 24303, 'Question Group Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:list', '#', 'system', now(), NULL, NULL, '问题组查询'),
    (24331, 1, 24303, 'Question Group Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), NULL, NULL, '问题组新增'),
    (24332, 1, 24303, 'Question Group Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), NULL, NULL, '问题组编辑'),
    (24333, 1, 24303, 'Question Group Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), NULL, NULL, '问题组删除'),
    (24340, 1, 24304, 'Template Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:list', '#', 'system', now(), NULL, NULL, '配置模板查询'),
    (24341, 1, 24304, 'Template Edit', 'common.edit', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), NULL, NULL, '配置模板维护'),
    (24342, 1, 24304, 'Template Rule', 'productCenter.template.rule', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:rule', '#', 'system', now(), NULL, NULL, '配置规则维护'),
    (24343, 1, 24304, 'Template Evaluate', 'productCenter.template.evaluate', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:test', '#', 'system', now(), NULL, NULL, '配置求值'),
    (24370, 1, 24307, 'Question Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:list', '#', 'system', now(), NULL, NULL, '配置问题查询'),
    (24371, 1, 24307, 'Question Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), NULL, NULL, '配置问题新增'),
    (24372, 1, 24307, 'Question Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), NULL, NULL, '配置问题编辑'),
    (24373, 1, 24307, 'Question Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), NULL, NULL, '配置问题删除'),
    (24374, 1, 24307, 'Question Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:list', '#', 'system', now(), NULL, NULL, '配置问题引用检查'),
    (24380, 1, 24308, 'Option Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:list', '#', 'system', now(), NULL, NULL, '配置答案查询'),
    (24381, 1, 24308, 'Option Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), NULL, NULL, '配置答案新增'),
    (24382, 1, 24308, 'Option Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), NULL, NULL, '配置答案编辑'),
    (24383, 1, 24308, 'Option Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), NULL, NULL, '配置答案删除'),
    (24384, 1, 24308, 'Option Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:list', '#', 'system', now(), NULL, NULL, '配置答案引用检查'),
    (24390, 1, 24309, 'Evaluator Run', 'productCenter.template.evaluate', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:test', '#', 'system', now(), NULL, NULL, '配置求值器运行'),
    (24350, 1, 24305, 'Pricing Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:price:list', '#', 'system', now(), NULL, NULL, '价格查询'),
    (24351, 1, 24305, 'Pricing Edit', 'common.edit', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:price:edit', '#', 'system', now(), NULL, NULL, '价格维护'),
    (24352, 1, 24305, 'Pricing Test', 'productCenter.price.calculate', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:price:test', '#', 'system', now(), NULL, NULL, '价格试算'),
    (24360, 1, 24306, 'Quote Preview', 'productCenter.quote.preview', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:price:test', '#', 'system', now(), NULL, NULL, '报价预览'),
    (24410, 1, 24401, 'Publish Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:publish:list', '#', 'system', now(), NULL, NULL, '发布查询'),
    (24411, 1, 24401, 'Publish Check', 'productCenter.publish.check', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:publish:check', '#', 'system', now(), NULL, NULL, '发布检查'),
    (24412, 1, 24401, 'Publish Package', 'productCenter.publish.publish', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:publish:publish', '#', 'system', now(), NULL, NULL, '生成发布包'),
    (24420, 1, 24402, 'Approval Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:publish:list', '#', 'system', now(), NULL, NULL, '审核查询'),
    (24421, 1, 24402, 'Approval Submit', 'productCenter.publish.submit', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:publish:submit', '#', 'system', now(), NULL, NULL, '提交审核'),
    (24422, 1, 24402, 'Approval Approve', 'productCenter.publish.approve', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:publish:approve', '#', 'system', now(), NULL, NULL, '审核通过'),
    (24423, 1, 24402, 'Approval Reject', 'productCenter.publish.reject', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:publish:reject', '#', 'system', now(), NULL, NULL, '审核拒绝'),
    (24430, 1, 24403, 'Gap Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:publish:list', '#', 'system', now(), NULL, NULL, '缺口查询'),
    (24431, 1, 24403, 'Gap Resolve', 'productCenter.publish.fixBlocker', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:publish:resolve', '#', 'system', now(), NULL, NULL, '缺口处理'),
    (24440, 1, 24404, 'Package Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:publish:list', '#', 'system', now(), NULL, NULL, '发布包查询'),
    (24441, 1, 24404, 'Package Detail', 'common.detail', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:publish:list', '#', 'system', now(), NULL, NULL, '发布包详情'),
    (24450, 1, 24405, 'Sync Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:publish:list', '#', 'system', now(), NULL, NULL, '同步日志查询'),
    (24451, 1, 24405, 'Retry Sync', 'productCenter.publish.retrySync', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:publish:retrySync', '#', 'system', now(), NULL, NULL, '同步重试'),
    (24460, 1, 24406, 'Import Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:import:list', '#', 'system', now(), NULL, NULL, '导入批次查询'),
    (24461, 1, 24406, 'Import Detail', 'common.detail', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:import:query', '#', 'system', now(), NULL, NULL, '导入批次详情'),
    (24462, 1, 24406, 'Import Parse', 'common.import', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:import:add', '#', 'system', now(), NULL, NULL, '导入解析'),
    (24463, 1, 24406, 'Import Edit', 'common.edit', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:import:edit', '#', 'system', now(), NULL, NULL, '导入批次编辑'),
    (24464, 1, 24406, 'Import Delete', 'common.delete', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:import:remove', '#', 'system', now(), NULL, NULL, '导入批次删除'),
    (24470, 1, 24407, 'Sales View Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:sales-view:list', '#', 'system', now(), NULL, NULL, '销售只读查询'),
    (24471, 1, 24407, 'Build Order Snapshot', 'productCenter.orderSnapshot.build', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:order-snapshot:build', '#', 'system', now(), NULL, NULL, '构建订单产品快照'),
    (24472, 1, 24407, 'Snapshot Instance Query', 'common.search', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:snapshot-instance:list', '#', 'system', now(), NULL, NULL, '产品能力快照实例查询'),
    (24473, 1, 24407, 'Snapshot Instance Detail', 'common.detail', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:snapshot-instance:query', '#', 'system', now(), NULL, NULL, '产品能力快照实例详情'),
    (24474, 1, 24407, 'Build Snapshot Instance', 'productCenter.orderSnapshot.build', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:snapshot-instance:build', '#', 'system', now(), NULL, NULL, '构建并保存产品能力快照实例')
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
WHERE menu_id BETWEEN 24200 AND 24499
   OR menu_id BETWEEN 24500 AND 24599
ON CONFLICT (role_id, menu_id) DO NOTHING;

DELETE FROM sys_dict_data
WHERE dict_type IN (
    'product_unit',
    'product_unit_type',
    'product_business_type',
    'product_asset_type',
    'product_engineering_item_type',
    'product_engineering_source_type',
    'product_engineering_rule_type',
    'product_engineering_output_type',
    'product_engineering_severity',
    'engineering_item_type',
    'engineering_scope_type',
    'engineering_rule_source',
    'engineering_rule_type',
    'engineering_rule_operator',
    'engineering_rule_action',
    'engineering_output_type',
    'engineering_qty_mode',
    'engineering_severity',
    'config_input_type',
    'config_option_source_type',
    'config_question_scope'
);
DELETE FROM sys_dict_type
WHERE dict_type IN (
    'product_unit',
    'product_unit_type',
    'product_business_type',
    'product_asset_type',
    'product_engineering_item_type',
    'product_engineering_source_type',
    'product_engineering_rule_type',
    'product_engineering_output_type',
    'product_engineering_severity',
    'engineering_item_type',
    'engineering_scope_type',
    'engineering_rule_source',
    'engineering_rule_type',
    'engineering_rule_operator',
    'engineering_rule_action',
    'engineering_output_type',
    'engineering_qty_mode',
    'engineering_severity',
    'config_input_type',
    'config_option_source_type',
    'config_question_scope'
);
