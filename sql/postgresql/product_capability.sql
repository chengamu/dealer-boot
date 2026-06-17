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
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_category_code_active ON pc_category (tenant_id, category_code) WHERE del_flag = '0';
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
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_unit_code_active ON pc_unit (tenant_id, unit_code) WHERE del_flag = '0';
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
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_product_dict_type_code_active ON pc_product_dict_type (tenant_id, dict_type_code) WHERE del_flag = '0';
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
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_product_dict_item_value_active ON pc_product_dict_item (tenant_id, dict_type_code, dict_item_value) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_product_dict_item_type_status ON pc_product_dict_item (tenant_id, dict_type_code, status, sort_order);

CREATE TABLE IF NOT EXISTS pc_base_attribute (
    attribute_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    attribute_group varchar(80) NOT NULL,
    attribute_code varchar(80) NOT NULL,
    attribute_name_cn varchar(200) NOT NULL,
    attribute_name_en varchar(200),
    value_type varchar(40) NOT NULL,
    unit_code varchar(80),
    material_types varchar(500),
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

COMMENT ON TABLE pc_base_attribute IS '物料属性定义表';
COMMENT ON COLUMN pc_base_attribute.attribute_id IS '属性定义ID';
COMMENT ON COLUMN pc_base_attribute.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_base_attribute.attribute_group IS '属性分组';
COMMENT ON COLUMN pc_base_attribute.attribute_code IS '属性编码';
COMMENT ON COLUMN pc_base_attribute.attribute_name_cn IS '属性中文名称';
COMMENT ON COLUMN pc_base_attribute.attribute_name_en IS '属性英文名称';
COMMENT ON COLUMN pc_base_attribute.value_type IS '值类型';
COMMENT ON COLUMN pc_base_attribute.unit_code IS '默认单位编码';
COMMENT ON COLUMN pc_base_attribute.material_types IS '适用物料类型列表';
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
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_base_attribute_code_active ON pc_base_attribute (tenant_id, attribute_group, attribute_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_base_attribute_group_status ON pc_base_attribute (tenant_id, attribute_group, status);

CREATE TABLE IF NOT EXISTS pc_material (
    material_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    material_code varchar(80) NOT NULL,
    material_name_cn varchar(200) NOT NULL,
    material_name_en varchar(200),
    material_type varchar(80),
    business_type varchar(80),
    unit_code varchar(80),
    purchase_unit_code varchar(80),
    inventory_unit_code varchar(80),
    usage_unit_code varchar(80),
    supplier_code varchar(80),
    supplier_name varchar(200),
    factory_model varchar(120),
    sample_book_no varchar(120),
    vendor_item_no varchar(120),
    primary_spec varchar(200),
    spec_summary varchar(1000),
    primary_color varchar(120),
    primary_weight numeric(18,6),
    purchase_enabled boolean NOT NULL DEFAULT false,
    inventory_enabled boolean NOT NULL DEFAULT false,
    purchase_unit_price numeric(18,4),
    cost_unit_price numeric(18,4),
    price_currency_code varchar(12) DEFAULT 'CNY',
    attribute_summary varchar(1000),
    legacy_source varchar(80),
    legacy_id varchar(120),
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    attribute_json jsonb,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_material
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    ADD COLUMN IF NOT EXISTS purchase_unit_code varchar(80),
    ADD COLUMN IF NOT EXISTS inventory_unit_code varchar(80),
    ADD COLUMN IF NOT EXISTS usage_unit_code varchar(80),
    ADD COLUMN IF NOT EXISTS supplier_code varchar(80),
    ADD COLUMN IF NOT EXISTS factory_model varchar(120),
    ADD COLUMN IF NOT EXISTS sample_book_no varchar(120),
    ADD COLUMN IF NOT EXISTS vendor_item_no varchar(120),
    ADD COLUMN IF NOT EXISTS primary_spec varchar(200),
    ADD COLUMN IF NOT EXISTS spec_summary varchar(1000),
    ADD COLUMN IF NOT EXISTS primary_color varchar(120),
    ADD COLUMN IF NOT EXISTS primary_weight numeric(18,6),
    ADD COLUMN IF NOT EXISTS purchase_enabled boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS inventory_enabled boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS purchase_unit_price numeric(18,4),
    ADD COLUMN IF NOT EXISTS cost_unit_price numeric(18,4),
    ADD COLUMN IF NOT EXISTS price_currency_code varchar(12) DEFAULT 'CNY',
    ADD COLUMN IF NOT EXISTS attribute_summary varchar(1000),
    ADD COLUMN IF NOT EXISTS legacy_source varchar(80),
    ADD COLUMN IF NOT EXISTS legacy_id varchar(120);

COMMENT ON TABLE pc_material IS '产品物料表';
COMMENT ON COLUMN pc_material.material_id IS '产品物料ID';
COMMENT ON COLUMN pc_material.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_material.material_code IS '物料编码';
COMMENT ON COLUMN pc_material.material_name_cn IS '物料中文名称';
COMMENT ON COLUMN pc_material.material_name_en IS '物料英文名称';
COMMENT ON COLUMN pc_material.material_type IS '物料类型';
COMMENT ON COLUMN pc_material.business_type IS '业务口径类型';
COMMENT ON COLUMN pc_material.unit_code IS '单位编码';
COMMENT ON COLUMN pc_material.purchase_unit_code IS '采购单位编码';
COMMENT ON COLUMN pc_material.inventory_unit_code IS '库存单位编码';
COMMENT ON COLUMN pc_material.usage_unit_code IS '工程使用单位编码';
COMMENT ON COLUMN pc_material.supplier_code IS '供应商编码快照';
COMMENT ON COLUMN pc_material.supplier_name IS '供应商名称';
COMMENT ON COLUMN pc_material.factory_model IS '工厂型号';
COMMENT ON COLUMN pc_material.sample_book_no IS '样册编号';
COMMENT ON COLUMN pc_material.vendor_item_no IS '供应商料号';
COMMENT ON COLUMN pc_material.primary_spec IS '主规格摘要';
COMMENT ON COLUMN pc_material.spec_summary IS '规格摘要：标准规格 + 高频补充属性';
COMMENT ON COLUMN pc_material.primary_color IS '主颜色摘要';
COMMENT ON COLUMN pc_material.primary_weight IS '主重量/克重数值';
COMMENT ON COLUMN pc_material.purchase_enabled IS '是否可采购主数据标记';
COMMENT ON COLUMN pc_material.inventory_enabled IS '是否入库主数据标记';
COMMENT ON COLUMN pc_material.purchase_unit_price IS '采购单价';
COMMENT ON COLUMN pc_material.cost_unit_price IS '成本单价';
COMMENT ON COLUMN pc_material.price_currency_code IS '价格币种，默认 CNY';
COMMENT ON COLUMN pc_material.attribute_summary IS '属性摘要';
COMMENT ON COLUMN pc_material.legacy_source IS '旧系统来源';
COMMENT ON COLUMN pc_material.legacy_id IS '旧系统对象ID';
COMMENT ON COLUMN pc_material.status IS '状态：建议 ENABLED 启用，DISABLED 停用';
COMMENT ON COLUMN pc_material.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_material.attribute_json IS '低频兼容属性JSON';
COMMENT ON COLUMN pc_material.remark IS '备注';
COMMENT ON COLUMN pc_material.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_material.create_by IS '创建者';
COMMENT ON COLUMN pc_material.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_material.update_by IS '更新者';
COMMENT ON COLUMN pc_material.update_time IS '更新时间，UTC timestamptz';
DROP INDEX IF EXISTS uk_pc_material_material_code_active;
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_material_code_active ON pc_material (tenant_id, material_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_material_type_status ON pc_material (tenant_id, material_type, status);
CREATE INDEX IF NOT EXISTS idx_pc_material_supplier_code ON pc_material (tenant_id, supplier_code);
CREATE INDEX IF NOT EXISTS idx_pc_material_vendor_item_no ON pc_material (tenant_id, vendor_item_no);
CREATE INDEX IF NOT EXISTS idx_pc_material_sample_book_no ON pc_material (tenant_id, sample_book_no);

UPDATE pc_material
SET spec_summary = COALESCE(NULLIF(spec_summary, ''), NULLIF(attribute_summary, ''), primary_spec)
WHERE spec_summary IS NULL OR spec_summary = '';

CREATE TABLE IF NOT EXISTS pc_fabric_series (
    series_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    series_code varchar(80) NOT NULL,
    series_name_cn varchar(200) NOT NULL,
    series_name_en varchar(200),
    material_type varchar(80),
    default_thickness_unit varchar(80),
    default_thickness_value numeric(18,6),
    thickness_rule_enabled boolean NOT NULL DEFAULT false,
    max_thickness_diff numeric(18,6),
    max_combined_thickness numeric(18,6),
    width_rule_enabled boolean NOT NULL DEFAULT false,
    available_widths jsonb,
    min_width_value numeric(18,6),
    max_width_value numeric(18,6),
    width_unit varchar(80),
    extra_rule_json jsonb,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_fabric_series IS '面料系列表';
COMMENT ON COLUMN pc_fabric_series.series_id IS '面料系列ID';
COMMENT ON COLUMN pc_fabric_series.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_fabric_series.series_code IS '面料系列编码';
COMMENT ON COLUMN pc_fabric_series.series_name_cn IS '面料系列中文名称';
COMMENT ON COLUMN pc_fabric_series.series_name_en IS '面料系列英文名称';
COMMENT ON COLUMN pc_fabric_series.material_type IS '适用物料类型';
COMMENT ON COLUMN pc_fabric_series.default_thickness_unit IS '默认厚度单位';
COMMENT ON COLUMN pc_fabric_series.default_thickness_value IS '默认厚度值';
COMMENT ON COLUMN pc_fabric_series.thickness_rule_enabled IS '是否启用厚度规则';
COMMENT ON COLUMN pc_fabric_series.max_thickness_diff IS '最大厚度差';
COMMENT ON COLUMN pc_fabric_series.max_combined_thickness IS '最大组合厚度';
COMMENT ON COLUMN pc_fabric_series.width_rule_enabled IS '是否启用门幅规则';
COMMENT ON COLUMN pc_fabric_series.available_widths IS '可选门幅列表JSON';
COMMENT ON COLUMN pc_fabric_series.min_width_value IS '最小门幅';
COMMENT ON COLUMN pc_fabric_series.max_width_value IS '最大门幅';
COMMENT ON COLUMN pc_fabric_series.width_unit IS '门幅单位';
COMMENT ON COLUMN pc_fabric_series.extra_rule_json IS '扩展规则JSON';
COMMENT ON COLUMN pc_fabric_series.status IS '状态：建议 ENABLED 启用，DISABLED 停用';
COMMENT ON COLUMN pc_fabric_series.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_fabric_series.remark IS '备注';
COMMENT ON COLUMN pc_fabric_series.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_fabric_series.create_by IS '创建者';
COMMENT ON COLUMN pc_fabric_series.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_fabric_series.update_by IS '更新者';
COMMENT ON COLUMN pc_fabric_series.update_time IS '更新时间，UTC timestamptz';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_fabric_series_code_active ON pc_fabric_series (tenant_id, series_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_fabric_series_type_status ON pc_fabric_series (tenant_id, material_type, status);

CREATE TABLE IF NOT EXISTS pc_fabric_profile (
    profile_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    fabric_code varchar(80),
    material_id bigint NOT NULL,
    material_code varchar(80) NOT NULL,
    material_name_cn varchar(200),
    series_id bigint,
    series_code varchar(80),
    series_name_cn varchar(200),
    color_code varchar(80),
    color_name varchar(120),
    material_composition varchar(500),
    texture_type varchar(120),
    finish_type varchar(120),
    width_value numeric(18,6),
    width_unit varchar(80),
    thickness_value numeric(18,6),
    thickness_unit varchar(80),
    gsm_value numeric(18,6),
    sample_book_no varchar(120),
    vendor_item_no varchar(120),
    supplier_code varchar(80),
    supplier_name varchar(200),
    factory_model varchar(120),
    purchase_unit_code varchar(80),
    inventory_unit_code varchar(80),
    sales_unit_code varchar(80),
    legacy_attribute_text varchar(1000),
    legacy_source varchar(80),
    legacy_id varchar(120),
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE pc_fabric_profile
    ADD COLUMN IF NOT EXISTS fabric_code varchar(80),
    ADD COLUMN IF NOT EXISTS material_name_cn varchar(200);

COMMENT ON TABLE pc_fabric_profile IS '面料资料表';
COMMENT ON COLUMN pc_fabric_profile.profile_id IS '面料资料ID';
COMMENT ON COLUMN pc_fabric_profile.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_fabric_profile.fabric_code IS '面料编码：一期与物料编码保持一致';
COMMENT ON COLUMN pc_fabric_profile.material_id IS '物料ID';
COMMENT ON COLUMN pc_fabric_profile.material_code IS '物料编码';
COMMENT ON COLUMN pc_fabric_profile.material_name_cn IS '物料中文名称快照';
COMMENT ON COLUMN pc_fabric_profile.series_id IS '面料系列ID';
COMMENT ON COLUMN pc_fabric_profile.series_code IS '面料系列编码';
COMMENT ON COLUMN pc_fabric_profile.series_name_cn IS '面料系列中文名称快照';
COMMENT ON COLUMN pc_fabric_profile.color_code IS '颜色编码';
COMMENT ON COLUMN pc_fabric_profile.color_name IS '颜色名称';
COMMENT ON COLUMN pc_fabric_profile.material_composition IS '材质成分';
COMMENT ON COLUMN pc_fabric_profile.texture_type IS '纹理类型';
COMMENT ON COLUMN pc_fabric_profile.finish_type IS '涂层/后整理类型';
COMMENT ON COLUMN pc_fabric_profile.width_value IS '门幅值';
COMMENT ON COLUMN pc_fabric_profile.width_unit IS '门幅单位';
COMMENT ON COLUMN pc_fabric_profile.thickness_value IS '厚度值';
COMMENT ON COLUMN pc_fabric_profile.thickness_unit IS '厚度单位';
COMMENT ON COLUMN pc_fabric_profile.gsm_value IS '克重值';
COMMENT ON COLUMN pc_fabric_profile.sample_book_no IS '样册编号';
COMMENT ON COLUMN pc_fabric_profile.vendor_item_no IS '供应商料号';
COMMENT ON COLUMN pc_fabric_profile.supplier_code IS '供应商编码快照';
COMMENT ON COLUMN pc_fabric_profile.supplier_name IS '供应商名称';
COMMENT ON COLUMN pc_fabric_profile.factory_model IS '工厂型号';
COMMENT ON COLUMN pc_fabric_profile.purchase_unit_code IS '采购单位编码';
COMMENT ON COLUMN pc_fabric_profile.inventory_unit_code IS '库存单位编码';
COMMENT ON COLUMN pc_fabric_profile.sales_unit_code IS '销售单位编码';
COMMENT ON COLUMN pc_fabric_profile.legacy_attribute_text IS '旧系统属性摘要';
COMMENT ON COLUMN pc_fabric_profile.legacy_source IS '旧系统来源';
COMMENT ON COLUMN pc_fabric_profile.legacy_id IS '旧系统对象ID';
COMMENT ON COLUMN pc_fabric_profile.status IS '状态：建议 ENABLED 启用，DISABLED 停用';
COMMENT ON COLUMN pc_fabric_profile.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_fabric_profile.remark IS '备注';
COMMENT ON COLUMN pc_fabric_profile.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_fabric_profile.create_by IS '创建者';
COMMENT ON COLUMN pc_fabric_profile.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_fabric_profile.update_by IS '更新者';
COMMENT ON COLUMN pc_fabric_profile.update_time IS '更新时间，UTC timestamptz';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_fabric_profile_material_active ON pc_fabric_profile (tenant_id, material_id) WHERE del_flag = '0';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_fabric_profile_material_code_active ON pc_fabric_profile (tenant_id, material_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_fabric_profile_series_status ON pc_fabric_profile (tenant_id, series_id, status);
CREATE INDEX IF NOT EXISTS idx_pc_fabric_profile_sample_book_no ON pc_fabric_profile (tenant_id, sample_book_no);
CREATE INDEX IF NOT EXISTS idx_pc_fabric_profile_vendor_item_no ON pc_fabric_profile (tenant_id, vendor_item_no);
CREATE INDEX IF NOT EXISTS idx_pc_fabric_profile_supplier_code ON pc_fabric_profile (tenant_id, supplier_code);

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
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_material_attr_active ON pc_material_attribute (tenant_id, material_id, attribute_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_material_attr_material ON pc_material_attribute (tenant_id, material_id, status);
CREATE INDEX IF NOT EXISTS idx_pc_material_attr_attribute ON pc_material_attribute (tenant_id, attribute_id, status);

CREATE TABLE IF NOT EXISTS pc_component (
    component_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    component_code varchar(80) NOT NULL,
    component_name_cn varchar(200) NOT NULL,
    component_name_en varchar(200),
    component_type varchar(80),
    business_type varchar(80),
    material_id bigint,
    material_code varchar(80),
    material_name_cn varchar(200),
    material_name_en varchar(200),
    default_qty numeric(18,6),
    qty_mode varchar(100),
    unit_code varchar(80),
    item_count integer DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    scope_json jsonb,
    legacy_source varchar(80),
    legacy_id varchar(120),
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_component
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    ADD COLUMN IF NOT EXISTS business_type varchar(80),
    ADD COLUMN IF NOT EXISTS item_count integer DEFAULT 0,
    ADD COLUMN IF NOT EXISTS legacy_source varchar(80),
    ADD COLUMN IF NOT EXISTS legacy_id varchar(120);

COMMENT ON TABLE pc_component IS '产品组件表';
COMMENT ON COLUMN pc_component.component_id IS '产品组件ID';
COMMENT ON COLUMN pc_component.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_component.component_code IS '组件编码';
COMMENT ON COLUMN pc_component.component_name_cn IS '组件中文名称';
COMMENT ON COLUMN pc_component.component_name_en IS '组件英文名称';
COMMENT ON COLUMN pc_component.component_type IS '组件类型';
COMMENT ON COLUMN pc_component.business_type IS '业务口径类型';
COMMENT ON COLUMN pc_component.material_id IS '默认展示物料ID';
COMMENT ON COLUMN pc_component.material_code IS '默认展示物料编码快照';
COMMENT ON COLUMN pc_component.material_name_cn IS '默认展示物料中文名称快照';
COMMENT ON COLUMN pc_component.material_name_en IS '默认展示物料英文名称快照';
COMMENT ON COLUMN pc_component.default_qty IS '默认数量';
COMMENT ON COLUMN pc_component.qty_mode IS '数量模式';
COMMENT ON COLUMN pc_component.unit_code IS '单位编码';
COMMENT ON COLUMN pc_component.item_count IS '组件明细数量';
COMMENT ON COLUMN pc_component.status IS '状态：建议 ENABLED 启用，DISABLED 停用';
COMMENT ON COLUMN pc_component.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_component.scope_json IS '适用范围JSON';
COMMENT ON COLUMN pc_component.legacy_source IS '旧系统来源';
COMMENT ON COLUMN pc_component.legacy_id IS '旧系统对象ID';
COMMENT ON COLUMN pc_component.remark IS '备注';
COMMENT ON COLUMN pc_component.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_component.create_by IS '创建者';
COMMENT ON COLUMN pc_component.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_component.update_by IS '更新者';
COMMENT ON COLUMN pc_component.update_time IS '更新时间，UTC timestamptz';
DROP INDEX IF EXISTS uk_pc_component_component_code_active;
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_component_code_active ON pc_component (tenant_id, component_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_component_type_status ON pc_component (tenant_id, component_type, status);

CREATE TABLE IF NOT EXISTS pc_component_item (
    component_item_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    component_id bigint NOT NULL,
    component_code varchar(80) NOT NULL,
    material_id bigint,
    material_code varchar(80),
    material_name_cn varchar(200),
    item_role varchar(80),
    qty_formula varchar(200),
    default_qty numeric(18,6),
    unit_code varchar(80),
    sort_order integer DEFAULT 0,
    required_flag boolean NOT NULL DEFAULT true,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_component_item IS '组件明细表';
COMMENT ON COLUMN pc_component_item.component_item_id IS '组件明细ID';
COMMENT ON COLUMN pc_component_item.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_component_item.component_id IS '组件ID';
COMMENT ON COLUMN pc_component_item.component_code IS '组件编码';
COMMENT ON COLUMN pc_component_item.material_id IS '物料ID';
COMMENT ON COLUMN pc_component_item.material_code IS '物料编码';
COMMENT ON COLUMN pc_component_item.material_name_cn IS '物料中文名称快照';
COMMENT ON COLUMN pc_component_item.item_role IS '组件明细角色';
COMMENT ON COLUMN pc_component_item.qty_formula IS '数量公式';
COMMENT ON COLUMN pc_component_item.default_qty IS '默认数量';
COMMENT ON COLUMN pc_component_item.unit_code IS '单位编码';
COMMENT ON COLUMN pc_component_item.sort_order IS '排序';
COMMENT ON COLUMN pc_component_item.required_flag IS '是否必选';
COMMENT ON COLUMN pc_component_item.status IS '状态：建议 ENABLED 启用，DISABLED 停用';
COMMENT ON COLUMN pc_component_item.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_component_item.remark IS '备注';
COMMENT ON COLUMN pc_component_item.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_component_item.create_by IS '创建者';
COMMENT ON COLUMN pc_component_item.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_component_item.update_by IS '更新者';
COMMENT ON COLUMN pc_component_item.update_time IS '更新时间，UTC timestamptz';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_component_item_sort_active ON pc_component_item (tenant_id, component_id, sort_order) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_component_item_component ON pc_component_item (tenant_id, component_id, status);
CREATE INDEX IF NOT EXISTS idx_pc_component_item_material ON pc_component_item (tenant_id, material_id, status);

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
    legacy_source varchar(80),
    legacy_id varchar(120),
    legacy_path varchar(500),
    legacy_url varchar(500),
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
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    ADD COLUMN IF NOT EXISTS legacy_source varchar(80),
    ADD COLUMN IF NOT EXISTS legacy_id varchar(120),
    ADD COLUMN IF NOT EXISTS legacy_path varchar(500),
    ADD COLUMN IF NOT EXISTS legacy_url varchar(500);

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
COMMENT ON COLUMN pc_media_asset.legacy_source IS '旧系统来源';
COMMENT ON COLUMN pc_media_asset.legacy_id IS '旧系统对象ID';
COMMENT ON COLUMN pc_media_asset.legacy_path IS '旧系统文件路径';
COMMENT ON COLUMN pc_media_asset.legacy_url IS '旧系统文件URL';
COMMENT ON COLUMN pc_media_asset.status IS '状态：建议 ENABLED 启用，DISABLED 停用';
COMMENT ON COLUMN pc_media_asset.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_media_asset.remark IS '备注';
COMMENT ON COLUMN pc_media_asset.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_media_asset.create_by IS '创建者';
COMMENT ON COLUMN pc_media_asset.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_media_asset.update_by IS '更新者';
COMMENT ON COLUMN pc_media_asset.update_time IS '更新时间，UTC timestamptz';
DROP INDEX IF EXISTS uk_pc_media_asset_asset_code_active;
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_media_asset_code_active ON pc_media_asset (tenant_id, asset_code) WHERE del_flag = '0';
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
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_media_binding_target_active ON pc_media_binding (tenant_id, asset_id, target_type, target_id, usage_type, language_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_media_binding_target ON pc_media_binding (tenant_id, target_type, target_id, status);
CREATE INDEX IF NOT EXISTS idx_pc_media_binding_asset ON pc_media_binding (tenant_id, asset_id, status);

-- 基础资料最小 seed，覆盖 5 个产品测试样本：
-- ROLLER_SHADE_BASIC / ROLLER_SHADE_MOTOR / ZEBRA_SHADE_BASIC / OUTDOOR_SHADE / CURTAIN_TRACK_SAMPLE

INSERT INTO pc_category (
    category_id, tenant_id, parent_id, category_code, category_name_cn, category_name_en, business_type,
    category_level, category_path, status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (100001, 1, 0, 'WINDOW_COVERING', '窗饰产品', 'Window Covering', 'PRODUCT_BASE', 1, 'WINDOW_COVERING', 'ENABLED', '0', 1, '基础资料根分类', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (100101, 1, 100001, 'ROLLER_SHADE_BASIC', '基础卷帘样本', 'Roller Shade Basic Sample', 'PRODUCT_BASE', 2, 'WINDOW_COVERING/ROLLER_SHADE_BASIC', 'ENABLED', '0', 10, 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (100102, 1, 100001, 'ROLLER_SHADE_MOTOR', '电动卷帘样本', 'Motorized Roller Shade Sample', 'PRODUCT_BASE', 2, 'WINDOW_COVERING/ROLLER_SHADE_MOTOR', 'ENABLED', '0', 20, 'Sample: ROLLER_SHADE_MOTOR', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (100103, 1, 100001, 'ZEBRA_SHADE_BASIC', '柔纱帘样本', 'Zebra Shade Basic Sample', 'PRODUCT_BASE', 2, 'WINDOW_COVERING/ZEBRA_SHADE_BASIC', 'ENABLED', '0', 30, 'Sample: ZEBRA_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (100104, 1, 100001, 'OUTDOOR_SHADE', '户外遮阳样本', 'Outdoor Shade Sample', 'PRODUCT_BASE', 2, 'WINDOW_COVERING/OUTDOOR_SHADE', 'ENABLED', '0', 40, 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (100105, 1, 100001, 'CURTAIN_TRACK_SAMPLE', '轨道配件样本', 'Curtain Track Sample', 'PRODUCT_BASE', 2, 'WINDOW_COVERING/CURTAIN_TRACK_SAMPLE', 'ENABLED', '0', 50, 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00')
ON CONFLICT (tenant_id, category_code) WHERE del_flag = '0' DO NOTHING;

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
ON CONFLICT (tenant_id, unit_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_product_dict_type (
    dict_type_id, tenant_id, dict_type_code, dict_type_name_cn, dict_type_name_en, business_domain,
    system_flag, editable_flag, status, sort_order, remark, del_flag, create_by, create_time, update_by, update_time
) VALUES
    (118001, 1, 'product_unit_type', '单位类型', 'Unit Type', 'BASE', true, true, 'ENABLED', 10, '单位分类枚举，具体单位仍维护在 pc_unit', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118002, 1, 'product_material_type', '物料类型', 'Material Type', 'BASE', true, true, 'ENABLED', 20, '产品物料类型', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118003, 1, 'product_business_type', '业务类型', 'Business Type', 'BASE', true, true, 'ENABLED', 30, '产品业务口径类型', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118004, 1, 'product_component_type', '组件包类型', 'Component Pack Type', 'BASE', true, true, 'ENABLED', 40, '组件包类型', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118005, 1, 'product_asset_type', '资料类型', 'Asset Type', 'BASE', true, true, 'ENABLED', 50, '资料资产类型', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118006, 1, 'product_attribute_group', '物料属性分组', 'Material Attribute Group', 'BASE', true, true, 'ENABLED', 60, '物料属性定义分组', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
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
ON CONFLICT (tenant_id, dict_type_code) WHERE del_flag = '0' DO UPDATE
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
    (119101, 1, 'product_material_type', 'FABRIC', '面料', 'Fabric', NULL, true, true, 'ENABLED', 10, '面料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119102, 1, 'product_material_type', 'PROFILE', '铝材/型材', 'Profile', NULL, true, true, 'ENABLED', 20, '型材', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119103, 1, 'product_material_type', 'BOTTOM_RAIL', '下杆', 'Bottom Rail', NULL, true, true, 'ENABLED', 30, '下杆', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119104, 1, 'product_material_type', 'TRACK', '轨道', 'Track', NULL, true, true, 'ENABLED', 40, '轨道', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119105, 1, 'product_material_type', 'MOTOR', '电机', 'Motor', NULL, true, true, 'ENABLED', 50, '电机', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119106, 1, 'product_material_type', 'REMOTE', '遥控器', 'Remote', NULL, true, true, 'ENABLED', 60, '遥控器', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119107, 1, 'product_material_type', 'CHAIN', '拉珠', 'Chain', NULL, true, true, 'ENABLED', 70, '拉珠/链条', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119108, 1, 'product_material_type', 'SOLAR_PANEL', '太阳能板', 'Solar Panel', NULL, true, true, 'ENABLED', 80, '太阳能板', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119109, 1, 'product_material_type', 'HARDWARE', '配件', 'Hardware', NULL, true, true, 'ENABLED', 90, '配件/五金', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119110, 1, 'product_material_type', 'INSTALL_PART', '安装件', 'Install Part', NULL, true, true, 'ENABLED', 100, '安装件', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119111, 1, 'product_material_type', 'PACKAGING', '包装件', 'Packaging', NULL, true, true, 'ENABLED', 110, '包装件', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119112, 1, 'product_material_type', 'FINISHED_GOOD', '标准成品', 'Finished Good', NULL, true, true, 'ENABLED', 120, '标准成品', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119113, 1, 'product_material_type', 'SERVICE', '服务项', 'Service', NULL, true, true, 'ENABLED', 130, '服务项预留', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119201, 1, 'product_business_type', 'ROLLER_SHADE', '卷帘', 'Roller Shade', NULL, true, true, 'ENABLED', 10, '卷帘', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119202, 1, 'product_business_type', 'ZEBRA_SHADE', '斑马帘/柔纱帘', 'Zebra Shade', NULL, true, true, 'ENABLED', 20, '斑马帘/柔纱帘', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119203, 1, 'product_business_type', 'OUTDOOR_SHADE', '户外遮阳', 'Outdoor Shade', NULL, true, true, 'ENABLED', 30, '户外遮阳', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119204, 1, 'product_business_type', 'CURTAIN_TRACK', '轨道窗帘', 'Curtain Track', NULL, true, true, 'ENABLED', 40, '轨道窗帘', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119205, 1, 'product_business_type', 'HONEYCOMB_SHADE', '蜂巢帘', 'Honeycomb Shade', NULL, true, true, 'ENABLED', 50, '蜂巢帘', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119301, 1, 'product_component_type', 'SYSTEM_PACK', '系统包', 'System Pack', NULL, true, true, 'ENABLED', 10, '系统组件包', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119302, 1, 'product_component_type', 'ACCESSORY_PACK', '配件包', 'Accessory Pack', NULL, true, true, 'ENABLED', 20, '配件包', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119303, 1, 'product_component_type', 'INSTALL_PACK', '安装零件包', 'Install Pack', NULL, true, true, 'ENABLED', 30, '安装零件包', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119304, 1, 'product_component_type', 'PACKAGING_PACK', '包装包', 'Packaging Pack', NULL, true, true, 'ENABLED', 40, '包装包', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119401, 1, 'product_asset_type', 'IMAGE', '图片', 'Image', NULL, true, true, 'ENABLED', 10, '图片', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119402, 1, 'product_asset_type', 'PDF', 'PDF', 'PDF', NULL, true, true, 'ENABLED', 20, 'PDF', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119403, 1, 'product_asset_type', 'SPEC', '规格书', 'Specification', NULL, true, true, 'ENABLED', 30, '规格书', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119404, 1, 'product_asset_type', 'INSTALL_GUIDE', '安装说明', 'Installation Guide', NULL, true, true, 'ENABLED', 40, '安装说明', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119405, 1, 'product_asset_type', 'DRAWING', '图纸', 'Drawing', NULL, true, true, 'ENABLED', 50, '图纸', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119406, 1, 'product_asset_type', 'OTHER', '其他', 'Other', NULL, true, true, 'ENABLED', 90, '其他资料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119451, 1, 'product_attribute_group', 'FABRIC', '面料参数', 'Fabric Attributes', NULL, true, true, 'ENABLED', 10, '面料类物料参数', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119452, 1, 'product_attribute_group', 'CONTROL', '控制系统参数', 'Control Attributes', NULL, true, true, 'ENABLED', 20, '电机、遥控、拉珠等控制系统参数', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119453, 1, 'product_attribute_group', 'HARDWARE', '五金配件参数', 'Hardware Attributes', NULL, true, true, 'ENABLED', 30, '铝材、下杆、安装件、配件参数', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119454, 1, 'product_attribute_group', 'PACKAGING', '包装参数', 'Packaging Attributes', NULL, true, true, 'ENABLED', 40, '包装件参数', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
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
    (120204, 1, 'config_option_source_type', 'FABRIC_PROFILE', '面料资料', 'Fabric Profile', NULL, true, true, 'ENABLED', 40, '面料资料来源', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (120205, 1, 'config_option_source_type', 'FABRIC_SERIES', '面料系列', 'Fabric Series', NULL, true, true, 'ENABLED', 50, '面料系列来源', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (120206, 1, 'config_option_source_type', 'COMPONENT', '组件包', 'Component Pack', NULL, true, true, 'ENABLED', 60, '组件包来源', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (120207, 1, 'config_option_source_type', 'MEDIA_ASSET', '资料资产', 'Media Asset', NULL, true, true, 'ENABLED', 70, '资料资产来源', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00')
ON CONFLICT (tenant_id, dict_type_code, dict_item_value) WHERE del_flag = '0' DO UPDATE
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

INSERT INTO pc_base_attribute (
    attribute_id, tenant_id, attribute_group, attribute_code, attribute_name_cn, attribute_name_en, value_type,
    unit_code, material_types, extra_json, sort_order, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
    (120001, 1, 'FABRIC', 'COLOR', '颜色', 'Color', 'TEXT', NULL, 'FABRIC', '{"seed":true}'::jsonb, 10, 'ENABLED', '0', '基础颜色属性', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (120002, 1, 'FABRIC', 'TEXTURE_TYPE', '纹理', 'Texture Type', 'TEXT', NULL, 'FABRIC', '{"seed":true}'::jsonb, 20, 'ENABLED', '0', '面料纹理属性', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (120003, 1, 'FABRIC', 'FINISH_TYPE', '涂层工艺', 'Finish Type', 'TEXT', NULL, 'FABRIC', '{"seed":true}'::jsonb, 30, 'ENABLED', '0', '面料后整理属性', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (120004, 1, 'CONTROL', 'VOLTAGE', '电压', 'Voltage', 'NUMBER', NULL, 'MOTOR', '{"seed":true}'::jsonb, 40, 'ENABLED', '0', '电机电压属性', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (120005, 1, 'CONTROL', 'PROTOCOL', '协议', 'Protocol', 'TEXT', NULL, 'MOTOR,REMOTE', '{"seed":true}'::jsonb, 50, 'ENABLED', '0', '控制协议属性', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (120006, 1, 'CONTROL', 'CHANNEL_COUNT', '通道数', 'Channel Count', 'NUMBER', NULL, 'REMOTE', '{"seed":true}'::jsonb, 60, 'ENABLED', '0', '遥控器通道属性', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (120007, 1, 'HARDWARE', 'MATERIAL_COMPOSITION', '材质', 'Material Composition', 'TEXT', NULL, 'HARDWARE,TRACK', '{"seed":true}'::jsonb, 70, 'ENABLED', '0', '五金材质属性', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (120008, 1, 'HARDWARE', 'MOUNT_TYPE', '安装方式', 'Mount Type', 'TEXT', NULL, 'HARDWARE', '{"seed":true}'::jsonb, 80, 'ENABLED', '0', '安装码属性', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (120009, 1, 'HARDWARE', 'WEIGHT', '重量', 'Weight', 'NUMBER', 'KG', 'HARDWARE', '{"seed":true}'::jsonb, 90, 'ENABLED', '0', '配重条属性', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (120010, 1, 'TRACK', 'LENGTH_SPEC', '长度规格', 'Length Spec', 'TEXT', NULL, 'TRACK', '{"seed":true}'::jsonb, 100, 'ENABLED', '0', '轨道长度规格属性', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00')
ON CONFLICT (tenant_id, attribute_group, attribute_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_material (
    material_id, tenant_id, material_code, material_name_cn, material_name_en, material_type, business_type, unit_code,
    supplier_code, supplier_name, factory_model, sample_book_no, vendor_item_no, primary_spec, primary_color, primary_weight,
    attribute_summary, legacy_source, legacy_id, status, del_flag, attribute_json, remark, create_by, create_time, update_by, update_time
) VALUES
    (130001, 1, 'FABRIC_BASIC_WHITE', '白色基础涂层布', 'Basic White Coated Fabric', 'FABRIC', 'ROLLER_SHADE', 'SQM', 'SUP-ROLLER', 'Hangzhou Fabric Lab', 'BW-280', 'SB-ROLLER-01', 'V-BW-280', '2800mm / 0.38mm / 320gsm', 'White', 320.000000, 'ROLLER_SHADE_BASIC fabric', 'OFBIZ', 'PRODUCT:ROLLER_SHADE_BASIC_FABRIC', 'ENABLED', '0', '{"sample":"ROLLER_SHADE_BASIC"}'::jsonb, 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (130002, 1, 'MOTOR_AOK_45_ZIGBEE', '奥克45管状电机', 'AOK 45 Tubular Motor', 'MOTOR', 'ROLLER_SHADE', 'PCS', 'SUP-AOK', 'AOK Motion', 'AOK-45-ZB', NULL, 'AOK45-ZB', '45mm Zigbee motor', 'Black', NULL, 'ROLLER_SHADE_MOTOR motor', 'OFBIZ', 'PRODUCT:ROLLER_SHADE_MOTOR', 'ENABLED', '0', '{"sample":"ROLLER_SHADE_MOTOR"}'::jsonb, 'Sample: ROLLER_SHADE_MOTOR', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (130003, 1, 'REMOTE_15CH_WHITE', '15通道白色遥控器', '15 Channel White Remote', 'REMOTE', 'ROLLER_SHADE', 'PCS', 'SUP-AOK', 'AOK Motion', 'RMT-15-WH', NULL, 'RMT15-WH', '15 channel hand remote', 'White', NULL, 'ROLLER_SHADE_MOTOR remote', 'OFBIZ', 'PRODUCT:ROLLER_SHADE_REMOTE', 'ENABLED', '0', '{"sample":"ROLLER_SHADE_MOTOR"}'::jsonb, 'Sample: ROLLER_SHADE_MOTOR', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (130004, 1, 'FABRIC_ZEBRA_IVORY', '象牙白双层柔纱布', 'Ivory Zebra Dual Fabric', 'FABRIC', 'ZEBRA_SHADE', 'SQM', 'SUP-ZEBRA', 'Ningbo Zebra Textile', 'ZB-IV-300', 'SB-ZEBRA-03', 'ZB-IV-300', '3000mm / dual layer', 'Ivory', 185.000000, 'ZEBRA_SHADE_BASIC fabric', 'OFBIZ', 'PRODUCT:ZEBRA_SHADE_BASIC_FABRIC', 'ENABLED', '0', '{"sample":"ZEBRA_SHADE_BASIC"}'::jsonb, 'Sample: ZEBRA_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (130005, 1, 'FABRIC_OUTDOOR_CHARCOAL', '深灰户外遮阳布', 'Outdoor Charcoal Solar Fabric', 'FABRIC', 'OUTDOOR_SHADE', 'SQM', 'SUP-OUT', 'SolarTex Outdoor', 'OD-CH-320', 'SB-OUT-08', 'OD-CH-320', '3200mm / PVC coated', 'Charcoal', 460.000000, 'OUTDOOR_SHADE fabric', 'OFBIZ', 'PRODUCT:OUTDOOR_SHADE_FABRIC', 'ENABLED', '0', '{"sample":"OUTDOOR_SHADE"}'::jsonb, 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (130006, 1, 'GLUE_TAPE_PET_20', 'PET胶条20mm', 'PET Glue Tape 20mm', 'HARDWARE', 'ROLLER_SHADE', 'M', 'SUP-HW', 'Hardware Supply Co', 'PET-20', NULL, 'GT-PET-20', '20mm adhesive tape', 'Transparent', NULL, 'ROLLER_SHADE_BASIC hardware', 'OFBIZ', 'PRODUCT:GLUE_TAPE', 'ENABLED', '0', '{"sample":"ROLLER_SHADE_BASIC"}'::jsonb, 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (130007, 1, 'BRACKET_STD_PAIR', '标准安装码对装', 'Standard Bracket Pair', 'HARDWARE', 'ROLLER_SHADE', 'SET', 'SUP-HW', 'Hardware Supply Co', 'BRK-STD', NULL, 'BRK-STD', 'Standard wall/ceiling bracket pair', 'Silver', NULL, 'ROLLER_SHADE_BASIC bracket', 'OFBIZ', 'PRODUCT:BRACKET_STD', 'ENABLED', '0', '{"sample":"ROLLER_SHADE_BASIC"}'::jsonb, 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (130008, 1, 'HEM_BAR_ALU_32', '32mm铝合金配重条', '32mm Aluminum Hem Bar', 'HARDWARE', 'ROLLER_SHADE', 'M', 'SUP-HW', 'Hardware Supply Co', 'HB-32', NULL, 'HB-32', '32mm aluminum hem bar', 'Silver', NULL, 'ROLLER_SHADE_BASIC hem bar', 'OFBIZ', 'PRODUCT:HEM_BAR', 'ENABLED', '0', '{"sample":"ROLLER_SHADE_BASIC"}'::jsonb, 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (130009, 1, 'BRACKET_OUTDOOR_HD', '户外重载安装码', 'Outdoor Heavy Duty Bracket', 'HARDWARE', 'OUTDOOR_SHADE', 'SET', 'SUP-OUT', 'Outdoor Hardware Inc', 'OD-BRK-HD', NULL, 'OD-BRK-HD', 'Heavy duty outdoor bracket', 'Black', NULL, 'OUTDOOR_SHADE bracket', 'OFBIZ', 'PRODUCT:OUTDOOR_BRACKET', 'ENABLED', '0', '{"sample":"OUTDOOR_SHADE"}'::jsonb, 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (130010, 1, 'CHAIN_OUTDOOR_SS', '户外不锈钢拉珠链', 'Outdoor Stainless Chain', 'HARDWARE', 'OUTDOOR_SHADE', 'M', 'SUP-OUT', 'Outdoor Hardware Inc', 'OD-CHAIN-SS', NULL, 'OD-CHAIN-SS', 'Outdoor stainless control chain', 'Steel', NULL, 'OUTDOOR_SHADE chain', 'OFBIZ', 'PRODUCT:OUTDOOR_CHAIN', 'ENABLED', '0', '{"sample":"OUTDOOR_SHADE"}'::jsonb, 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (130011, 1, 'TRACK_ALU_WAVE_32', '32型铝轨道', '32mm Aluminum Track', 'TRACK', 'CURTAIN_TRACK', 'M', 'SUP-TRACK', 'Track Systems Ltd', 'TRK-32', NULL, 'TRK-32', '32mm wave curtain track', 'White', NULL, 'CURTAIN_TRACK_SAMPLE track', 'OFBIZ', 'PRODUCT:CURTAIN_TRACK', 'ENABLED', '0', '{"sample":"CURTAIN_TRACK_SAMPLE"}'::jsonb, 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (130012, 1, 'MOTOR_TRACK_AC', '轨道交流电机', 'Track AC Motor', 'MOTOR', 'CURTAIN_TRACK', 'PCS', 'SUP-TRACK', 'Track Systems Ltd', 'TRK-MOTOR-AC', NULL, 'TRK-MOTOR-AC', 'AC curtain track motor', 'White', NULL, 'CURTAIN_TRACK_SAMPLE motor', 'OFBIZ', 'PRODUCT:CURTAIN_TRACK_MOTOR', 'ENABLED', '0', '{"sample":"CURTAIN_TRACK_SAMPLE"}'::jsonb, 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (130013, 1, 'REMOTE_TRACK_5CH', '轨道5通道遥控器', 'Track 5 Channel Remote', 'REMOTE', 'CURTAIN_TRACK', 'PCS', 'SUP-TRACK', 'Track Systems Ltd', 'TRK-RMT-5', NULL, 'TRK-RMT-5', '5 channel curtain track remote', 'White', NULL, 'CURTAIN_TRACK_SAMPLE remote', 'OFBIZ', 'PRODUCT:CURTAIN_TRACK_REMOTE', 'ENABLED', '0', '{"sample":"CURTAIN_TRACK_SAMPLE"}'::jsonb, 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (130014, 1, 'CHAIN_ROLLER_WHITE', '卷帘白色拉珠链', 'Roller White Chain', 'CHAIN', 'ROLLER_SHADE', 'M', 'SUP-HW', 'Hardware Supply Co', 'CHAIN-WH', NULL, 'CHAIN-WH', 'White roller control chain', 'White', NULL, 'ROLLER_SHADE chain', 'OFBIZ', 'PRODUCT:ROLLER_CHAIN', 'ENABLED', '0', '{"sample":"ROLLER_SHADE_BASIC"}'::jsonb, 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00')
ON CONFLICT (tenant_id, material_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_material (
    material_id, tenant_id, material_code, material_name_cn, material_name_en, material_type, business_type, unit_code,
    supplier_code, supplier_name, factory_model, sample_book_no, vendor_item_no, primary_spec, primary_color, primary_weight,
    attribute_summary, legacy_source, legacy_id, status, del_flag, attribute_json, remark, create_by, create_time, update_by, update_time
) VALUES
    (130101, 1, 'FABRIC_HONEYCOMB_IVORY', '象牙白蜂巢布', 'Ivory Honeycomb Fabric', 'FABRIC', 'HONEYCOMB_SHADE', 'SQM', 'SUP-HONEY', 'Honeycomb Textile', 'HC-IV-25', 'SB-HONEY-01', 'HC-IV-25', '25mm cell / 0.45mm / 210gsm', 'Ivory', 210.000000, 'HONEYCOMB_SHADE fabric', 'OFBIZ', 'PRODUCT:HONEYCOMB_FABRIC_IVORY', 'ENABLED', '0', '{"sample":"HONEYCOMB_SHADE"}'::jsonb, 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (130102, 1, 'FABRIC_HONEYCOMB_GREY', '浅灰蜂巢布', 'Light Grey Honeycomb Fabric', 'FABRIC', 'HONEYCOMB_SHADE', 'SQM', 'SUP-HONEY', 'Honeycomb Textile', 'HC-GY-25', 'SB-HONEY-01', 'HC-GY-25', '25mm cell / 0.48mm / 220gsm', 'Grey', 220.000000, 'HONEYCOMB_SHADE fabric', 'OFBIZ', 'PRODUCT:HONEYCOMB_FABRIC_GREY', 'ENABLED', '0', '{"sample":"HONEYCOMB_SHADE"}'::jsonb, 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (130103, 1, 'PROFILE_HONEYCOMB_HEADRAIL', '蜂巢帘上轨', 'Honeycomb Headrail', 'PROFILE', 'HONEYCOMB_SHADE', 'M', 'SUP-HW', 'Hardware Supply Co', 'HC-HR-55', NULL, 'HC-HR-55', '55mm aluminum headrail', 'White', NULL, 'HONEYCOMB_SHADE profile', 'OFBIZ', 'PRODUCT:HONEYCOMB_HEADRAIL', 'ENABLED', '0', '{"sample":"HONEYCOMB_SHADE"}'::jsonb, 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (130104, 1, 'BOTTOM_RAIL_HONEYCOMB_WHITE', '蜂巢帘白色下杆', 'Honeycomb White Bottom Rail', 'BOTTOM_RAIL', 'HONEYCOMB_SHADE', 'M', 'SUP-HW', 'Hardware Supply Co', 'HC-BR-WH', NULL, 'HC-BR-WH', 'White bottom rail', 'White', NULL, 'HONEYCOMB_SHADE bottom rail', 'OFBIZ', 'PRODUCT:HONEYCOMB_BOTTOM_RAIL', 'ENABLED', '0', '{"sample":"HONEYCOMB_SHADE"}'::jsonb, 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (130105, 1, 'CHAIN_HONEYCOMB_WHITE', '蜂巢帘白色拉珠链', 'Honeycomb White Chain', 'CHAIN', 'HONEYCOMB_SHADE', 'M', 'SUP-HW', 'Hardware Supply Co', 'HC-CHAIN-WH', NULL, 'HC-CHAIN-WH', 'White honeycomb control chain', 'White', NULL, 'HONEYCOMB_SHADE chain', 'OFBIZ', 'PRODUCT:HONEYCOMB_CHAIN', 'ENABLED', '0', '{"sample":"HONEYCOMB_SHADE"}'::jsonb, 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (130106, 1, 'REMOTE_6CH_WHITE', '6通道白色遥控器', '6 Channel White Remote', 'REMOTE', 'HONEYCOMB_SHADE', 'PCS', 'SUP-AOK', 'AOK Motion', 'RMT-6-WH', NULL, 'RMT6-WH', '6 channel hand remote', 'White', NULL, 'HONEYCOMB_SHADE remote', 'OFBIZ', 'PRODUCT:REMOTE_6CH', 'ENABLED', '0', '{"sample":"HONEYCOMB_SHADE"}'::jsonb, 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (130107, 1, 'SOLAR_PANEL_STD', '标准太阳能板', 'Standard Solar Panel', 'SOLAR_PANEL', 'HONEYCOMB_SHADE', 'PCS', 'SUP-AOK', 'AOK Motion', 'SOLAR-STD', NULL, 'SOLAR-STD', 'Window shade solar panel', 'Black', NULL, 'HONEYCOMB_SHADE solar panel', 'OFBIZ', 'PRODUCT:SOLAR_PANEL', 'ENABLED', '0', '{"sample":"HONEYCOMB_SHADE"}'::jsonb, 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (130108, 1, 'BOX_ROLLER_STD', '卷帘标准纸箱', 'Roller Standard Carton', 'PACKAGING', 'ROLLER_SHADE', 'PCS', 'SUP-PACK', 'Packing Supply', 'BOX-RL-STD', NULL, 'BOX-RL-STD', 'Roller shade carton', 'Kraft', NULL, 'ROLLER_SHADE packaging', 'OFBIZ', 'PRODUCT:BOX_ROLLER_STD', 'ENABLED', '0', '{"sample":"ROLLER_SHADE_BASIC"}'::jsonb, 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (130109, 1, 'BOX_HONEYCOMB_STD', '蜂巢帘标准纸箱', 'Honeycomb Standard Carton', 'PACKAGING', 'HONEYCOMB_SHADE', 'PCS', 'SUP-PACK', 'Packing Supply', 'BOX-HC-STD', NULL, 'BOX-HC-STD', 'Honeycomb shade carton', 'Kraft', NULL, 'HONEYCOMB_SHADE packaging', 'OFBIZ', 'PRODUCT:BOX_HONEYCOMB_STD', 'ENABLED', '0', '{"sample":"HONEYCOMB_SHADE"}'::jsonb, 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, material_code) WHERE del_flag = '0' DO UPDATE
SET material_name_cn = EXCLUDED.material_name_cn,
    material_name_en = EXCLUDED.material_name_en,
    material_type = EXCLUDED.material_type,
    business_type = EXCLUDED.business_type,
    unit_code = EXCLUDED.unit_code,
    primary_spec = EXCLUDED.primary_spec,
    primary_color = EXCLUDED.primary_color,
    attribute_summary = EXCLUDED.attribute_summary,
    legacy_source = EXCLUDED.legacy_source,
    legacy_id = EXCLUDED.legacy_id,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

UPDATE pc_material
SET spec_summary = COALESCE(NULLIF(spec_summary, ''), NULLIF(attribute_summary, ''), primary_spec),
    purchase_unit_code = COALESCE(purchase_unit_code, unit_code),
    inventory_unit_code = COALESCE(inventory_unit_code, unit_code),
    usage_unit_code = COALESCE(usage_unit_code, unit_code),
    purchase_enabled = true,
    inventory_enabled = true
WHERE tenant_id = 1 AND del_flag = '0';

INSERT INTO pc_fabric_series (
    series_id, tenant_id, series_code, series_name_cn, series_name_en, material_type, default_thickness_unit,
    default_thickness_value, thickness_rule_enabled, max_thickness_diff, max_combined_thickness, width_rule_enabled,
    available_widths, min_width_value, max_width_value, width_unit, extra_rule_json, status, del_flag, remark,
    create_by, create_time, update_by, update_time
) VALUES
    (140001, 1, 'FS_BASIC_COATED', '基础涂层布系列', 'Basic Coated Fabric Series', 'FABRIC', 'MM', 0.380000, true, 0.080000, 0.760000, true, '[2.0, 2.5, 2.8]'::jsonb, 2.000000, 2.800000, 'M', '{"sample":"ROLLER_SHADE_BASIC"}'::jsonb, 'ENABLED', '0', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (140002, 1, 'FS_ZEBRA_DUAL', '双层柔纱布系列', 'Dual Layer Zebra Fabric Series', 'FABRIC', 'MM', 0.300000, true, 0.050000, 0.600000, true, '[2.8, 3.0]'::jsonb, 2.800000, 3.000000, 'M', '{"sample":"ZEBRA_SHADE_BASIC","doubleLayer":true}'::jsonb, 'ENABLED', '0', 'Sample: ZEBRA_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (140003, 1, 'FS_OUTDOOR_SOLAR', '户外遮阳布系列', 'Outdoor Solar Fabric Series', 'FABRIC', 'MM', 0.550000, true, 0.100000, 1.100000, true, '[3.0, 3.2]'::jsonb, 3.000000, 3.200000, 'M', '{"sample":"OUTDOOR_SHADE","uvResistance":"high"}'::jsonb, 'ENABLED', '0', 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (140101, 1, 'FS_HONEYCOMB_25', '25mm蜂巢布系列', '25mm Honeycomb Fabric Series', 'FABRIC', 'MM', 0.450000, true, 0.100000, 0.950000, true, '[2.0, 2.5]'::jsonb, 2.000000, 2.500000, 'M', '{"sample":"HONEYCOMB_SHADE","doubleFabricAllowed":true}'::jsonb, 'ENABLED', '0', 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, series_code) WHERE del_flag = '0' DO UPDATE
SET series_name_cn = EXCLUDED.series_name_cn,
    series_name_en = EXCLUDED.series_name_en,
    max_combined_thickness = EXCLUDED.max_combined_thickness,
    available_widths = EXCLUDED.available_widths,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

INSERT INTO pc_fabric_profile (
    profile_id, tenant_id, fabric_code, material_id, material_code, material_name_cn, series_id, series_code, series_name_cn, color_code, color_name,
    material_composition, texture_type, finish_type, width_value, width_unit, thickness_value, thickness_unit, gsm_value,
    sample_book_no, vendor_item_no, supplier_code, supplier_name, factory_model, purchase_unit_code, inventory_unit_code,
    sales_unit_code, legacy_attribute_text, legacy_source, legacy_id, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
    (150001, 1, 'FABRIC_BASIC_WHITE', 130001, 'FABRIC_BASIC_WHITE', '基础白色涂层布', 140001, 'FS_BASIC_COATED', '基础涂层布系列', 'WHITE', 'White', '100% Polyester', 'Smooth', 'PVC Coated', 2.800000, 'M', 0.380000, 'MM', 320.000000, 'SB-ROLLER-01', 'V-BW-280', 'SUP-ROLLER', 'Hangzhou Fabric Lab', 'BW-280', 'SQM', 'SQM', 'SQM', 'OFBiz sample book: SB-ROLLER-01', 'OFBIZ', 'FABRIC_PROFILE:ROLLER_SHADE_BASIC', 'ENABLED', '0', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (150002, 1, 'FABRIC_ZEBRA_IVORY', 130004, 'FABRIC_ZEBRA_IVORY', '象牙白双层柔纱布', 140002, 'FS_ZEBRA_DUAL', '双层柔纱布系列', 'IVORY', 'Ivory', 'Polyester Blend', 'Dual Layer', 'Semi-Sheer', 3.000000, 'M', 0.300000, 'MM', 185.000000, 'SB-ZEBRA-03', 'ZB-IV-300', 'SUP-ZEBRA', 'Ningbo Zebra Textile', 'ZB-IV-300', 'SQM', 'SQM', 'SQM', 'Double layer combination thickness seed', 'OFBIZ', 'FABRIC_PROFILE:ZEBRA_SHADE_BASIC', 'ENABLED', '0', 'Sample: ZEBRA_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (150003, 1, 'FABRIC_OUTDOOR_CHARCOAL', 130005, 'FABRIC_OUTDOOR_CHARCOAL', '户外炭灰遮阳布', 140003, 'FS_OUTDOOR_SOLAR', '户外遮阳布系列', 'CHARCOAL', 'Charcoal', 'PVC + Polyester', 'Outdoor Basket Weave', 'UV Resistant', 3.200000, 'M', 0.550000, 'MM', 460.000000, 'SB-OUT-08', 'OD-CH-320', 'SUP-OUT', 'SolarTex Outdoor', 'OD-CH-320', 'SQM', 'SQM', 'SQM', 'Outdoor heavy gsm fabric seed', 'OFBIZ', 'FABRIC_PROFILE:OUTDOOR_SHADE', 'ENABLED', '0', 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (150101, 1, 'FABRIC_HONEYCOMB_IVORY', 130101, 'FABRIC_HONEYCOMB_IVORY', '象牙白蜂巢布', 140101, 'FS_HONEYCOMB_25', '25mm蜂巢布系列', 'IVORY', 'Ivory', '100% Polyester', 'Cellular', 'Pleated', 2.500000, 'M', 0.450000, 'MM', 210.000000, 'SB-HONEY-01', 'HC-IV-25', 'SUP-HONEY', 'Honeycomb Textile', 'HC-IV-25', 'SQM', 'SQM', 'SQM', 'Honeycomb 25mm fabric seed', 'OFBIZ', 'FABRIC_PROFILE:HONEYCOMB_IVORY', 'ENABLED', '0', 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (150102, 1, 'FABRIC_HONEYCOMB_GREY', 130102, 'FABRIC_HONEYCOMB_GREY', '浅灰蜂巢布', 140101, 'FS_HONEYCOMB_25', '25mm蜂巢布系列', 'GREY', 'Grey', '100% Polyester', 'Cellular', 'Pleated', 2.500000, 'M', 0.480000, 'MM', 220.000000, 'SB-HONEY-01', 'HC-GY-25', 'SUP-HONEY', 'Honeycomb Textile', 'HC-GY-25', 'SQM', 'SQM', 'SQM', 'Honeycomb 25mm fabric seed', 'OFBIZ', 'FABRIC_PROFILE:HONEYCOMB_GREY', 'ENABLED', '0', 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, material_id) WHERE del_flag = '0' DO UPDATE
SET fabric_code = EXCLUDED.fabric_code,
    material_name_cn = EXCLUDED.material_name_cn,
    series_id = EXCLUDED.series_id,
    series_code = EXCLUDED.series_code,
    series_name_cn = EXCLUDED.series_name_cn,
    width_value = EXCLUDED.width_value,
    thickness_value = EXCLUDED.thickness_value,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

UPDATE pc_fabric_profile
SET fabric_code = material_code
WHERE fabric_code IS NULL;

UPDATE pc_fabric_profile fp
SET material_name_cn = pm.material_name_cn
FROM pc_material pm
WHERE fp.tenant_id = pm.tenant_id
  AND fp.material_code = pm.material_code
  AND (fp.material_name_cn IS NULL OR fp.material_name_cn = '');

INSERT INTO pc_material_attribute (
    material_attribute_id, tenant_id, material_id, material_code, attribute_id, attribute_code, attribute_name_cn, attribute_name_en,
    value_text, value_number, value_bool, value_unit_code, sort_order, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
    (160001, 1, 130002, 'MOTOR_AOK_45_ZIGBEE', 120004, 'VOLTAGE', '电压', 'Voltage', '220V', 220.000000, NULL, NULL, 10, 'ENABLED', '0', 'Sample: ROLLER_SHADE_MOTOR', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (160002, 1, 130002, 'MOTOR_AOK_45_ZIGBEE', 120005, 'PROTOCOL', '协议', 'Protocol', 'ZIGBEE', NULL, NULL, NULL, 20, 'ENABLED', '0', 'Sample: ROLLER_SHADE_MOTOR', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (160003, 1, 130003, 'REMOTE_15CH_WHITE', 120006, 'CHANNEL_COUNT', '通道数', 'Channel Count', '15', 15.000000, NULL, NULL, 10, 'ENABLED', '0', 'Sample: ROLLER_SHADE_MOTOR', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (160004, 1, 130003, 'REMOTE_15CH_WHITE', 120005, 'PROTOCOL', '协议', 'Protocol', 'ZIGBEE', NULL, NULL, NULL, 20, 'ENABLED', '0', 'Sample: ROLLER_SHADE_MOTOR', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (160005, 1, 130006, 'GLUE_TAPE_PET_20', 120007, 'MATERIAL_COMPOSITION', '材质', 'Material Composition', 'PET', NULL, NULL, NULL, 10, 'ENABLED', '0', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (160006, 1, 130007, 'BRACKET_STD_PAIR', 120008, 'MOUNT_TYPE', '安装方式', 'Mount Type', 'Wall/Ceiling', NULL, NULL, NULL, 10, 'ENABLED', '0', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (160007, 1, 130008, 'HEM_BAR_ALU_32', 120009, 'WEIGHT', '重量', 'Weight', '0.42kg/m', 0.420000, NULL, 'KG', 10, 'ENABLED', '0', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (160008, 1, 130009, 'BRACKET_OUTDOOR_HD', 120008, 'MOUNT_TYPE', '安装方式', 'Mount Type', 'Wall/Frame', NULL, NULL, NULL, 10, 'ENABLED', '0', 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (160009, 1, 130010, 'CHAIN_OUTDOOR_SS', 120007, 'MATERIAL_COMPOSITION', '材质', 'Material Composition', 'Stainless Steel', NULL, NULL, NULL, 10, 'ENABLED', '0', 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (160010, 1, 130011, 'TRACK_ALU_WAVE_32', 120010, 'LENGTH_SPEC', '长度规格', 'Length Spec', '3.2m standard stock length', NULL, NULL, NULL, 10, 'ENABLED', '0', 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (160011, 1, 130012, 'MOTOR_TRACK_AC', 120004, 'VOLTAGE', '电压', 'Voltage', '220V', 220.000000, NULL, NULL, 10, 'ENABLED', '0', 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (160012, 1, 130012, 'MOTOR_TRACK_AC', 120005, 'PROTOCOL', '协议', 'Protocol', 'Matter', NULL, NULL, NULL, 20, 'ENABLED', '0', 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (160013, 1, 130013, 'REMOTE_TRACK_5CH', 120006, 'CHANNEL_COUNT', '通道数', 'Channel Count', '5', 5.000000, NULL, NULL, 10, 'ENABLED', '0', 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00')
ON CONFLICT (tenant_id, material_id, attribute_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_component (
    component_id, tenant_id, component_code, component_name_cn, component_name_en, component_type, business_type,
    material_id, material_code, material_name_cn, material_name_en, default_qty, qty_mode, unit_code, item_count,
    status, del_flag, scope_json, legacy_source, legacy_id, remark, create_by, create_time, update_by, update_time
) VALUES
    (170001, 1, 'COMP_ROLLER_BASIC', '卷帘基础组件包', 'Roller Basic Component Pack', 'PACKAGE', 'ROLLER_SHADE', 130006, 'GLUE_TAPE_PET_20', 'PET胶条20mm', 'PET Glue Tape 20mm', 1.000000, 'FIXED', 'SET', 3, 'ENABLED', '0', '{"sample":"ROLLER_SHADE_BASIC"}'::jsonb, 'OFBIZ', 'COMPONENT:ROLLER_SHADE_BASIC', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (170002, 1, 'COMP_ROLLER_MOTOR', '卷帘电动控制包', 'Roller Motor Control Pack', 'PACKAGE', 'ROLLER_SHADE', 130002, 'MOTOR_AOK_45_ZIGBEE', '奥克45管状电机', 'AOK 45 Tubular Motor', 1.000000, 'FIXED', 'SET', 3, 'ENABLED', '0', '{"sample":"ROLLER_SHADE_MOTOR"}'::jsonb, 'OFBIZ', 'COMPONENT:ROLLER_SHADE_MOTOR', 'Sample: ROLLER_SHADE_MOTOR', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (170003, 1, 'COMP_ZEBRA_BASIC', '柔纱帘基础组件包', 'Zebra Basic Component Pack', 'PACKAGE', 'ZEBRA_SHADE', 130004, 'FABRIC_ZEBRA_IVORY', '象牙白双层柔纱布', 'Ivory Zebra Dual Fabric', 1.000000, 'FIXED', 'SET', 3, 'ENABLED', '0', '{"sample":"ZEBRA_SHADE_BASIC"}'::jsonb, 'OFBIZ', 'COMPONENT:ZEBRA_SHADE_BASIC', 'Sample: ZEBRA_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (170004, 1, 'COMP_OUTDOOR_INSTALL', '户外遮阳安装包', 'Outdoor Shade Install Pack', 'PACKAGE', 'OUTDOOR_SHADE', 130009, 'BRACKET_OUTDOOR_HD', '户外重载安装码', 'Outdoor Heavy Duty Bracket', 1.000000, 'FIXED', 'SET', 3, 'ENABLED', '0', '{"sample":"OUTDOOR_SHADE"}'::jsonb, 'OFBIZ', 'COMPONENT:OUTDOOR_SHADE', 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (170005, 1, 'COMP_CURTAIN_TRACK_MOTOR', '轨道电机组件包', 'Curtain Track Motor Pack', 'PACKAGE', 'CURTAIN_TRACK', 130011, 'TRACK_ALU_WAVE_32', '32型铝轨道', '32mm Aluminum Track', 1.000000, 'FIXED', 'SET', 4, 'ENABLED', '0', '{"sample":"CURTAIN_TRACK_SAMPLE"}'::jsonb, 'OFBIZ', 'COMPONENT:CURTAIN_TRACK_SAMPLE', 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00')
ON CONFLICT (tenant_id, component_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_component (
    component_id, tenant_id, component_code, component_name_cn, component_name_en, component_type, business_type,
    material_id, material_code, material_name_cn, material_name_en, default_qty, qty_mode, unit_code, item_count,
    status, del_flag, scope_json, legacy_source, legacy_id, remark, create_by, create_time, update_by, update_time
) VALUES
    (170101, 1, 'COMP_HONEYCOMB_CHAIN', '蜂巢帘拉珠系统包', 'Honeycomb Chain System Pack', 'SYSTEM_PACK', 'HONEYCOMB_SHADE', 130105, 'CHAIN_HONEYCOMB_WHITE', '蜂巢帘白色拉珠链', 'Honeycomb White Chain', 1.000000, 'FIXED', 'SET', 4, 'ENABLED', '0', '{"sample":"HONEYCOMB_SHADE","system":"chain"}'::jsonb, 'OFBIZ', 'COMPONENT:HONEYCOMB_CHAIN', 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (170102, 1, 'COMP_HONEYCOMB_MOTOR', '蜂巢帘电动系统包', 'Honeycomb Motor System Pack', 'SYSTEM_PACK', 'HONEYCOMB_SHADE', 130002, 'MOTOR_AOK_45_ZIGBEE', '奥克45管状电机', 'AOK 45 Tubular Motor', 1.000000, 'FIXED', 'SET', 5, 'ENABLED', '0', '{"sample":"HONEYCOMB_SHADE","system":"motor"}'::jsonb, 'OFBIZ', 'COMPONENT:HONEYCOMB_MOTOR', 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (170103, 1, 'COMP_INSTALL_STD', '通用安装零件包', 'Standard Install Parts Pack', 'INSTALL_PACK', 'COMMON', 130007, 'BRACKET_STD_PAIR', '标准安装码对装', 'Standard Bracket Pair', 1.000000, 'FIXED', 'SET', 2, 'ENABLED', '0', '{"sample":"COMMON"}'::jsonb, 'OFBIZ', 'COMPONENT:INSTALL_STD', 'Common install pack', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (170104, 1, 'COMP_PACKAGING_STD', '通用包装包', 'Standard Packaging Pack', 'PACKAGING_PACK', 'COMMON', 130108, 'BOX_ROLLER_STD', '卷帘标准纸箱', 'Roller Standard Carton', 1.000000, 'FIXED', 'SET', 2, 'ENABLED', '0', '{"sample":"COMMON"}'::jsonb, 'OFBIZ', 'COMPONENT:PACKAGING_STD', 'Common packaging pack', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, component_code) WHERE del_flag = '0' DO UPDATE
SET component_name_cn = EXCLUDED.component_name_cn,
    component_name_en = EXCLUDED.component_name_en,
    component_type = EXCLUDED.component_type,
    business_type = EXCLUDED.business_type,
    item_count = EXCLUDED.item_count,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

INSERT INTO pc_component_item (
    component_item_id, tenant_id, component_id, component_code, material_id, material_code, material_name_cn, item_role,
    qty_formula, default_qty, unit_code, sort_order, required_flag, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
    (180001, 1, 170001, 'COMP_ROLLER_BASIC', 130006, 'GLUE_TAPE_PET_20', 'PET胶条20mm', 'ACCESSORY', '1', 1.000000, 'M', 10, true, 'ENABLED', '0', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180002, 1, 170001, 'COMP_ROLLER_BASIC', 130007, 'BRACKET_STD_PAIR', '标准安装码对装', 'BRACKET', '2', 2.000000, 'SET', 20, true, 'ENABLED', '0', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180003, 1, 170001, 'COMP_ROLLER_BASIC', 130008, 'HEM_BAR_ALU_32', '32mm铝合金配重条', 'WEIGHT_BAR', '1', 1.000000, 'M', 30, true, 'ENABLED', '0', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180004, 1, 170002, 'COMP_ROLLER_MOTOR', 130002, 'MOTOR_AOK_45_ZIGBEE', '奥克45管状电机', 'MOTOR', '1', 1.000000, 'PCS', 10, true, 'ENABLED', '0', 'Sample: ROLLER_SHADE_MOTOR', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180005, 1, 170002, 'COMP_ROLLER_MOTOR', 130003, 'REMOTE_15CH_WHITE', '15通道白色遥控器', 'REMOTE', '1', 1.000000, 'PCS', 20, true, 'ENABLED', '0', 'Sample: ROLLER_SHADE_MOTOR', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180006, 1, 170002, 'COMP_ROLLER_MOTOR', 130007, 'BRACKET_STD_PAIR', '标准安装码对装', 'BRACKET', '2', 2.000000, 'SET', 30, true, 'ENABLED', '0', 'Sample: ROLLER_SHADE_MOTOR', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180007, 1, 170003, 'COMP_ZEBRA_BASIC', 130004, 'FABRIC_ZEBRA_IVORY', '象牙白双层柔纱布', 'FABRIC', '1', 1.000000, 'SQM', 10, true, 'ENABLED', '0', 'Sample: ZEBRA_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180008, 1, 170003, 'COMP_ZEBRA_BASIC', 130007, 'BRACKET_STD_PAIR', '标准安装码对装', 'BRACKET', '2', 2.000000, 'SET', 20, true, 'ENABLED', '0', 'Sample: ZEBRA_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180009, 1, 170003, 'COMP_ZEBRA_BASIC', 130008, 'HEM_BAR_ALU_32', '32mm铝合金配重条', 'WEIGHT_BAR', '1', 1.000000, 'M', 30, true, 'ENABLED', '0', 'Sample: ZEBRA_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180010, 1, 170004, 'COMP_OUTDOOR_INSTALL', 130005, 'FABRIC_OUTDOOR_CHARCOAL', '深灰户外遮阳布', 'FABRIC', '1', 1.000000, 'SQM', 10, true, 'ENABLED', '0', 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180011, 1, 170004, 'COMP_OUTDOOR_INSTALL', 130009, 'BRACKET_OUTDOOR_HD', '户外重载安装码', 'BRACKET', '2', 2.000000, 'SET', 20, true, 'ENABLED', '0', 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180012, 1, 170004, 'COMP_OUTDOOR_INSTALL', 130010, 'CHAIN_OUTDOOR_SS', '户外不锈钢拉珠链', 'CHAIN', '1', 1.000000, 'M', 30, true, 'ENABLED', '0', 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180013, 1, 170005, 'COMP_CURTAIN_TRACK_MOTOR', 130011, 'TRACK_ALU_WAVE_32', '32型铝轨道', 'TRACK', '1', 1.000000, 'M', 10, true, 'ENABLED', '0', 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180014, 1, 170005, 'COMP_CURTAIN_TRACK_MOTOR', 130012, 'MOTOR_TRACK_AC', '轨道交流电机', 'MOTOR', '1', 1.000000, 'PCS', 20, true, 'ENABLED', '0', 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180015, 1, 170005, 'COMP_CURTAIN_TRACK_MOTOR', 130013, 'REMOTE_TRACK_5CH', '轨道5通道遥控器', 'REMOTE', '1', 1.000000, 'PCS', 30, true, 'ENABLED', '0', 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (180016, 1, 170005, 'COMP_CURTAIN_TRACK_MOTOR', 130007, 'BRACKET_STD_PAIR', '标准安装码对装', 'BRACKET', '2', 2.000000, 'SET', 40, true, 'ENABLED', '0', 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00')
ON CONFLICT (tenant_id, component_id, sort_order) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_component_item (
    component_item_id, tenant_id, component_id, component_code, material_id, material_code, material_name_cn, item_role,
    qty_formula, default_qty, unit_code, sort_order, required_flag, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
    (180101, 1, 170101, 'COMP_HONEYCOMB_CHAIN', 130105, 'CHAIN_HONEYCOMB_WHITE', '蜂巢帘白色拉珠链', 'CHAIN', '1', 1.000000, 'M', 10, true, 'ENABLED', '0', 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (180102, 1, 170101, 'COMP_HONEYCOMB_CHAIN', 130103, 'PROFILE_HONEYCOMB_HEADRAIL', '蜂巢帘上轨', 'PROFILE', '1', 1.000000, 'M', 20, true, 'ENABLED', '0', 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (180103, 1, 170101, 'COMP_HONEYCOMB_CHAIN', 130104, 'BOTTOM_RAIL_HONEYCOMB_WHITE', '蜂巢帘白色下杆', 'BOTTOM_RAIL', '1', 1.000000, 'M', 30, true, 'ENABLED', '0', 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (180104, 1, 170102, 'COMP_HONEYCOMB_MOTOR', 130002, 'MOTOR_AOK_45_ZIGBEE', '奥克45管状电机', 'MOTOR', '1', 1.000000, 'PCS', 10, true, 'ENABLED', '0', 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (180105, 1, 170102, 'COMP_HONEYCOMB_MOTOR', 130106, 'REMOTE_6CH_WHITE', '6通道白色遥控器', 'REMOTE', '1', 1.000000, 'PCS', 20, false, 'ENABLED', '0', 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (180106, 1, 170102, 'COMP_HONEYCOMB_MOTOR', 130107, 'SOLAR_PANEL_STD', '标准太阳能板', 'SOLAR_PANEL', '1', 1.000000, 'PCS', 30, false, 'ENABLED', '0', 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (180107, 1, 170103, 'COMP_INSTALL_STD', 130007, 'BRACKET_STD_PAIR', '标准安装码对装', 'BRACKET', '2', 2.000000, 'SET', 10, true, 'ENABLED', '0', 'Common install pack', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (180108, 1, 170104, 'COMP_PACKAGING_STD', 130108, 'BOX_ROLLER_STD', '卷帘标准纸箱', 'PACKAGING', '1', 1.000000, 'PCS', 10, true, 'ENABLED', '0', 'Common packaging pack', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (180109, 1, 170104, 'COMP_PACKAGING_STD', 130109, 'BOX_HONEYCOMB_STD', '蜂巢帘标准纸箱', 'PACKAGING', '1', 1.000000, 'PCS', 20, true, 'ENABLED', '0', 'Common packaging pack', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, component_id, sort_order) WHERE del_flag = '0' DO UPDATE
SET material_id = EXCLUDED.material_id,
    material_code = EXCLUDED.material_code,
    material_name_cn = EXCLUDED.material_name_cn,
    item_role = EXCLUDED.item_role,
    default_qty = EXCLUDED.default_qty,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

INSERT INTO pc_media_asset (
    asset_id, tenant_id, asset_code, asset_name_cn, asset_name_en, asset_type, usage_type, language_code, visibility,
    oss_id, url, alt_text, version_no, legacy_source, legacy_id, legacy_path, legacy_url, status, del_flag, remark,
    create_by, create_time, update_by, update_time
) VALUES
    (190001, 1, 'ASSET_ROLLER_SWATCH', '卷帘基础色卡', 'Roller Basic Swatch', 'SWATCH', 'FABRIC_SERIES', 'en_US', 'INTERNAL', NULL, NULL, 'Basic coated fabric swatch', 1, 'OFBIZ', 'CONTENT:ROLLER_SWATCH', '/legacy/ofbiz/content/roller-basic-swatch.pdf', 'ofbiz://roller-basic-swatch', 'ENABLED', '0', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (190002, 1, 'ASSET_MOTOR_SPEC', '卷帘电机规格书', 'Roller Motor Specification', 'SPEC', 'MATERIAL', 'en_US', 'INTERNAL', NULL, NULL, 'AOK motor specification', 1, 'OFBIZ', 'CONTENT:MOTOR_SPEC', '/legacy/ofbiz/content/roller-motor-spec.pdf', 'ofbiz://roller-motor-spec', 'ENABLED', '0', 'Sample: ROLLER_SHADE_MOTOR', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (190003, 1, 'ASSET_ZEBRA_INSTALL', '柔纱帘安装说明', 'Zebra Installation Guide', 'INSTALL_GUIDE', 'COMPONENT', 'en_US', 'INTERNAL', NULL, NULL, 'Zebra basic install guide', 1, 'OFBIZ', 'CONTENT:ZEBRA_INSTALL', '/legacy/ofbiz/content/zebra-install.pdf', 'ofbiz://zebra-install', 'ENABLED', '0', 'Sample: ZEBRA_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (190004, 1, 'ASSET_OUTDOOR_DRAWING', '户外遮阳安装图', 'Outdoor Installation Drawing', 'DRAWING', 'CATEGORY', 'en_US', 'INTERNAL', NULL, NULL, 'Outdoor shade installation drawing', 1, 'OFBIZ', 'CONTENT:OUTDOOR_DRAWING', '/legacy/ofbiz/content/outdoor-drawing.pdf', 'ofbiz://outdoor-drawing', 'ENABLED', '0', 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (190005, 1, 'ASSET_TRACK_GUIDE', '轨道安装说明', 'Curtain Track Installation Guide', 'INSTALL_GUIDE', 'COMPONENT', 'en_US', 'INTERNAL', NULL, NULL, 'Curtain track motor pack install guide', 1, 'OFBIZ', 'CONTENT:TRACK_GUIDE', '/legacy/ofbiz/content/track-install.pdf', 'ofbiz://track-install', 'ENABLED', '0', 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00')
ON CONFLICT (tenant_id, asset_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_media_asset (
    asset_id, tenant_id, asset_code, asset_name_cn, asset_name_en, asset_type, usage_type, language_code, visibility,
    oss_id, url, alt_text, version_no, legacy_source, legacy_id, legacy_path, legacy_url, status, del_flag, remark,
    create_by, create_time, update_by, update_time
) VALUES
    (190101, 1, 'ASSET_ROLLER_INSTALL', '卷帘安装说明', 'Roller Installation Guide', 'INSTALL_GUIDE', 'COMPONENT', 'zh_CN', 'INTERNAL', NULL, NULL, 'Roller installation guide', 1, 'OFBIZ', 'CONTENT:ROLLER_INSTALL', '/legacy/ofbiz/content/roller-install.pdf', 'ofbiz://roller-install', 'ENABLED', '0', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (190102, 1, 'ASSET_HONEYCOMB_INSTALL', '蜂巢帘安装说明', 'Honeycomb Installation Guide', 'INSTALL_GUIDE', 'COMPONENT', 'zh_CN', 'INTERNAL', NULL, NULL, 'Honeycomb installation guide', 1, 'OFBIZ', 'CONTENT:HONEYCOMB_INSTALL', '/legacy/ofbiz/content/honeycomb-install.pdf', 'ofbiz://honeycomb-install', 'ENABLED', '0', 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, asset_code) WHERE del_flag = '0' DO UPDATE
SET asset_name_cn = EXCLUDED.asset_name_cn,
    asset_name_en = EXCLUDED.asset_name_en,
    asset_type = EXCLUDED.asset_type,
    usage_type = EXCLUDED.usage_type,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

INSERT INTO pc_media_binding (
    binding_id, tenant_id, asset_id, asset_code, target_type, target_id, target_code, usage_type, visibility,
    language_code, required_for_publish, sort_order, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
    (195001, 1, 190001, 'ASSET_ROLLER_SWATCH', 'FABRIC_SERIES', 140001, 'FS_BASIC_COATED', 'SWATCH', 'INTERNAL', 'en_US', '0', 10, 'ENABLED', '0', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (195002, 1, 190002, 'ASSET_MOTOR_SPEC', 'MATERIAL', 130002, 'MOTOR_AOK_45_ZIGBEE', 'SPEC', 'INTERNAL', 'en_US', '1', 10, 'ENABLED', '0', 'Sample: ROLLER_SHADE_MOTOR', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (195003, 1, 190003, 'ASSET_ZEBRA_INSTALL', 'COMPONENT', 170003, 'COMP_ZEBRA_BASIC', 'INSTALL_GUIDE', 'INTERNAL', 'en_US', '0', 10, 'ENABLED', '0', 'Sample: ZEBRA_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (195004, 1, 190004, 'ASSET_OUTDOOR_DRAWING', 'CATEGORY', 100104, 'OUTDOOR_SHADE', 'DRAWING', 'INTERNAL', 'en_US', '1', 10, 'ENABLED', '0', 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (195005, 1, 190005, 'ASSET_TRACK_GUIDE', 'COMPONENT', 170005, 'COMP_CURTAIN_TRACK_MOTOR', 'INSTALL_GUIDE', 'INTERNAL', 'en_US', '0', 10, 'ENABLED', '0', 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00')
ON CONFLICT (tenant_id, asset_id, target_type, target_id, usage_type, language_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_media_binding (
    binding_id, tenant_id, asset_id, asset_code, target_type, target_id, target_code, usage_type, visibility,
    language_code, required_for_publish, sort_order, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
    (195101, 1, 190101, 'ASSET_ROLLER_INSTALL', 'COMPONENT', 170001, 'COMP_ROLLER_BASIC', 'INSTALL_GUIDE', 'INTERNAL', 'zh_CN', '0', 10, 'ENABLED', '0', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (195102, 1, 190102, 'ASSET_HONEYCOMB_INSTALL', 'COMPONENT', 170101, 'COMP_HONEYCOMB_CHAIN', 'INSTALL_GUIDE', 'INTERNAL', 'zh_CN', '0', 10, 'ENABLED', '0', 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, asset_id, target_type, target_id, usage_type, language_code) WHERE del_flag = '0' DO UPDATE
SET asset_code = EXCLUDED.asset_code,
    target_code = EXCLUDED.target_code,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

-- 清理旧一期产品配置草案表：当前基础资料重构不再创建这些未实现表。
DROP TABLE IF EXISTS pc_import_row_issue;
DROP TABLE IF EXISTS pc_import_batch;
DROP TABLE IF EXISTS pc_product_sync_outbox;
DROP TABLE IF EXISTS pc_product_snapshot_instance;
DROP TABLE IF EXISTS pc_product_publish_package;
DROP TABLE IF EXISTS pc_publish_approval;
DROP TABLE IF EXISTS pc_publish_check_result;
DROP TABLE IF EXISTS pc_price_rule_item;
DROP TABLE IF EXISTS pc_price_plan_version;
DROP TABLE IF EXISTS pc_price_plan;
DROP TABLE IF EXISTS pc_config_rule;
DROP TABLE IF EXISTS pc_config_option;
DROP TABLE IF EXISTS pc_config_question;
DROP TABLE IF EXISTS pc_question_group;
DROP TABLE IF EXISTS pc_config_template_version;
DROP TABLE IF EXISTS pc_config_template;
DROP TABLE IF EXISTS pc_sales_variant;
DROP TABLE IF EXISTS pc_product_model;

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
COMMENT ON TABLE pc_engineering_check_case IS '工程配置检查样例表';
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
    legacy_source varchar(80),
    legacy_id varchar(120),
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
COMMENT ON COLUMN pc_sales_product.legacy_source IS '旧系统来源';
COMMENT ON COLUMN pc_sales_product.legacy_id IS '旧系统编号';
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

INSERT INTO pc_sales_product (
    sales_product_id, tenant_id, sales_product_code, sales_product_name_cn, sales_product_name_en, category_id, category_code, category_name_cn, category_name_en,
    product_type, sales_mode, template_id, template_code, template_version_id, template_version_no, default_width, default_height,
    dimension_unit, biz_status, legacy_source, legacy_id, status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (200001, 1, 'SP_ROLLER_BASIC', '基础卷帘', 'Basic Roller Shade', 100101, 'ROLLER_SHADE_BASIC', '基础卷帘分类', 'Basic Roller Shade Category', 'ROLLER_SHADE', 'CONFIGURABLE', 210001, 'TPL_ROLLER_BASIC', 211001, 'V1', 120.000000, 160.000000, 'CM', 'DRAFT', 'OFBIZ', 'SP:ROLLER_BASIC', 'ENABLED', '0', 10, 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (200002, 1, 'SP_OUTDOOR_SHADE', '户外卷帘', 'Outdoor Roller Shade', 100104, 'OUTDOOR_SHADE', '户外遮阳分类', 'Outdoor Shade Category', 'OUTDOOR_SHADE', 'CONFIGURABLE', 210002, 'TPL_OUTDOOR_SHADE', 211002, 'V1', 180.000000, 220.000000, 'CM', 'DRAFT', 'OFBIZ', 'SP:OUTDOOR_SHADE', 'ENABLED', '0', 20, 'Sample: OUTDOOR_SHADE', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (200003, 1, 'SP_ZEBRA_DUAL', '双层柔纱帘', 'Dual Layer Zebra Shade', 100103, 'ZEBRA_SHADE_BASIC', '柔纱帘分类', 'Zebra Shade Category', 'ZEBRA_SHADE', 'CONFIGURABLE', 210003, 'TPL_ZEBRA_DUAL', 211003, 'V1', 130.000000, 180.000000, 'CM', 'DRAFT', 'OFBIZ', 'SP:ZEBRA_DUAL', 'ENABLED', '0', 30, 'Sample: ZEBRA_SHADE_BASIC', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (200004, 1, 'SP_ROLLER_MOTOR', '电动卷帘', 'Motorized Roller Shade', 100102, 'ROLLER_SHADE_MOTOR', '电动卷帘分类', 'Motorized Roller Category', 'ROLLER_SHADE', 'CONFIGURABLE', 210004, 'TPL_ROLLER_MOTOR', 211004, 'V1', 150.000000, 200.000000, 'CM', 'DRAFT', 'OFBIZ', 'SP:ROLLER_MOTOR', 'ENABLED', '0', 40, 'Sample: ROLLER_SHADE_MOTOR', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (200005, 1, 'SP_TRACK_MEDIA', '资料轨道套装', 'Track Pack With Media', 100105, 'CURTAIN_TRACK_SAMPLE', '轨道套装分类', 'Curtain Track Category', 'CURTAIN_TRACK', 'CONFIGURABLE', 210005, 'TPL_TRACK_MEDIA', 211005, 'V1', 240.000000, 260.000000, 'CM', 'DRAFT', 'OFBIZ', 'SP:TRACK_MEDIA', 'ENABLED', '0', 50, 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00')
ON CONFLICT (tenant_id, sales_product_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_config_template (
    template_id, tenant_id, template_code, template_name_cn, template_name_en, product_model_id, product_model_code,
    sales_product_id, sales_product_code, current_version_id, current_version_no, published_version_id, published_version_no,
    biz_status, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
    (210001, 1, 'TPL_ROLLER_BASIC', '基础卷帘配置模板', 'Basic Roller Config Template', 200001, 'SP_ROLLER_BASIC', 200001, 'SP_ROLLER_BASIC', 211001, 'V1', NULL, NULL, 'DRAFT', 'ENABLED', '0', 'Sample template', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (210002, 1, 'TPL_OUTDOOR_SHADE', '户外卷帘配置模板', 'Outdoor Shade Config Template', 200002, 'SP_OUTDOOR_SHADE', 200002, 'SP_OUTDOOR_SHADE', 211002, 'V1', NULL, NULL, 'DRAFT', 'ENABLED', '0', 'Sample template', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (210003, 1, 'TPL_ZEBRA_DUAL', '柔纱帘配置模板', 'Zebra Config Template', 200003, 'SP_ZEBRA_DUAL', 200003, 'SP_ZEBRA_DUAL', 211003, 'V1', NULL, NULL, 'DRAFT', 'ENABLED', '0', 'Sample template', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (210004, 1, 'TPL_ROLLER_MOTOR', '电动卷帘配置模板', 'Motorized Roller Config Template', 200004, 'SP_ROLLER_MOTOR', 200004, 'SP_ROLLER_MOTOR', 211004, 'V1', NULL, NULL, 'DRAFT', 'ENABLED', '0', 'Sample template', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (210005, 1, 'TPL_TRACK_MEDIA', '资料轨道配置模板', 'Track Media Config Template', 200005, 'SP_TRACK_MEDIA', 200005, 'SP_TRACK_MEDIA', 211005, 'V1', NULL, NULL, 'DRAFT', 'ENABLED', '0', 'Sample template', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00')
ON CONFLICT (tenant_id, template_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_config_template_version (
    template_version_id, tenant_id, template_id, template_code, version_no, version_name, version_status,
    product_model_id, product_model_code, sales_product_id, sales_product_code, schema_json, draft_hash,
    del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
    (211001, 1, 210001, 'TPL_ROLLER_BASIC', 'V1', 'Initial Draft', 'DRAFT', 200001, 'SP_ROLLER_BASIC', 200001, 'SP_ROLLER_BASIC', '{"sample":"ROLLER_SHADE_BASIC"}'::jsonb, 'seed-roller-basic', '0', 'Sample version', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (211002, 1, 210002, 'TPL_OUTDOOR_SHADE', 'V1', 'Initial Draft', 'DRAFT', 200002, 'SP_OUTDOOR_SHADE', 200002, 'SP_OUTDOOR_SHADE', '{"sample":"OUTDOOR_SHADE"}'::jsonb, 'seed-outdoor', '0', 'Sample version', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (211003, 1, 210003, 'TPL_ZEBRA_DUAL', 'V1', 'Initial Draft', 'DRAFT', 200003, 'SP_ZEBRA_DUAL', 200003, 'SP_ZEBRA_DUAL', '{"sample":"ZEBRA_SHADE_BASIC"}'::jsonb, 'seed-zebra', '0', 'Sample version', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (211004, 1, 210004, 'TPL_ROLLER_MOTOR', 'V1', 'Initial Draft', 'DRAFT', 200004, 'SP_ROLLER_MOTOR', 200004, 'SP_ROLLER_MOTOR', '{"sample":"ROLLER_SHADE_MOTOR"}'::jsonb, 'seed-motor', '0', 'Sample version', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (211005, 1, 210005, 'TPL_TRACK_MEDIA', 'V1', 'Initial Draft', 'DRAFT', 200005, 'SP_TRACK_MEDIA', 200005, 'SP_TRACK_MEDIA', '{"sample":"CURTAIN_TRACK_SAMPLE"}'::jsonb, 'seed-track', '0', 'Sample version', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00')
ON CONFLICT (tenant_id, template_id, version_no) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_question_group (
    question_group_id, tenant_id, group_code, group_name_cn, group_name_en, description_cn, description_en,
    status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (212001, 1, 'GROUP_FABRIC', '面料选择', 'Fabric Selection', '选择面料系列和颜色', 'Choose fabric series and color', 'ENABLED', '0', 10, 'Seed group', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (212002, 1, 'GROUP_CONTROL', '控制方式', 'Control Method', '选择手动或电动控制', 'Choose manual or motor control', 'ENABLED', '0', 20, 'Seed group', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (212003, 1, 'GROUP_INSTALL', '安装附件', 'Installation Accessories', '选择安装和附件资料', 'Choose install packs and media', 'ENABLED', '0', 30, 'Seed group', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00')
ON CONFLICT (tenant_id, group_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_config_question (
    question_id, tenant_id, template_version_id, question_group_id, question_code, question_name_cn, question_name_en,
    help_text_cn, help_text_en, input_type, required_flag, customer_visible, default_value, validation_json, display_rule_json,
    status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (213001, 1, 211001, 212001, 'FABRIC_SERIES', '面料系列', 'Fabric Series', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 10, 'SP_ROLLER_BASIC', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (213002, 1, 211001, 212002, 'CONTROL_METHOD', '控制方式', 'Control Method', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 20, 'SP_ROLLER_BASIC', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (213003, 1, 211001, 212003, 'INSTALL_PACK', '安装包', 'Install Pack', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 30, 'SP_ROLLER_BASIC', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (213011, 1, 211002, 212001, 'FABRIC_SERIES', '面料系列', 'Fabric Series', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 10, 'SP_OUTDOOR_SHADE', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (213012, 1, 211002, 212002, 'CONTROL_METHOD', '控制方式', 'Control Method', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 20, 'SP_OUTDOOR_SHADE', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (213013, 1, 211002, 212003, 'INSTALL_PACK', '安装包', 'Install Pack', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 30, 'SP_OUTDOOR_SHADE', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (213021, 1, 211003, 212001, 'FABRIC_SERIES', '面料系列', 'Fabric Series', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 10, 'SP_ZEBRA_DUAL', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (213022, 1, 211003, 212002, 'CONTROL_METHOD', '控制方式', 'Control Method', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 20, 'SP_ZEBRA_DUAL', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (213023, 1, 211003, 212003, 'INSTALL_PACK', '安装包', 'Install Pack', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 30, 'SP_ZEBRA_DUAL', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (213031, 1, 211004, 212001, 'FABRIC_SERIES', '面料系列', 'Fabric Series', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 10, 'SP_ROLLER_MOTOR', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (213032, 1, 211004, 212002, 'CONTROL_METHOD', '控制方式', 'Control Method', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 20, 'SP_ROLLER_MOTOR', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (213033, 1, 211004, 212003, 'INSTALL_PACK', '安装包', 'Install Pack', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 30, 'SP_ROLLER_MOTOR', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (213041, 1, 211005, 212001, 'TRACK_TYPE', '轨道类型', 'Track Type', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 10, 'SP_TRACK_MEDIA', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (213042, 1, 211005, 212002, 'CONTROL_METHOD', '控制方式', 'Control Method', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 20, 'SP_TRACK_MEDIA', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (213043, 1, 211005, 212003, 'MEDIA_PACK', '资料包', 'Media Pack', NULL, NULL, 'SINGLE_SELECT', '1', '1', NULL, '{"required":true}'::jsonb, NULL, 'ENABLED', '0', 30, 'SP_TRACK_MEDIA', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00')
ON CONFLICT (tenant_id, template_version_id, question_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_config_option (
    option_id, tenant_id, question_id, template_version_id, option_code, option_name_cn, option_name_en, option_value,
    source_type, source_ref_id, source_code, source_name, display_name_cn, display_name_en, value_code, component_json, media_json,
    rule_json, status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (214001, 1, 213001, 211001, 'FABRIC_BASIC_WHITE', '基础白色涂层布', 'Basic White Fabric', 'FABRIC_BASIC_WHITE', 'FABRIC_PROFILE', 150001, 'FABRIC_BASIC_WHITE', '基础白色涂层布', '基础白色涂层布', 'Basic White Fabric', 'FABRIC_BASIC_WHITE', NULL, '[{"assetCode":"ASSET_ROLLER_SWATCH","usageType":"SWATCH"}]'::jsonb, NULL, 'ENABLED', '0', 10, 'fabric', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (214002, 1, 213001, 211001, 'FABRIC_OUTDOOR_CHARCOAL', '户外炭灰遮阳布', 'Outdoor Charcoal Fabric', 'FABRIC_OUTDOOR_CHARCOAL', 'FABRIC_PROFILE', 150003, 'FABRIC_OUTDOOR_CHARCOAL', '户外炭灰遮阳布', '户外炭灰遮阳布', 'Outdoor Charcoal Fabric', 'FABRIC_OUTDOOR_CHARCOAL', NULL, NULL, '{"disabled":true,"disabledReason":"product.config.option.seriesMismatch"}'::jsonb, 'ENABLED', '0', 20, 'disabled sample', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (214003, 1, 213002, 211001, 'CONTROL_CHAIN', '手动拉珠', 'Chain Control', 'CHAIN', 'BASE_ATTRIBUTE', 120008, 'MOUNT_TYPE', '安装方式', '手动拉珠', 'Chain Control', 'CHAIN', NULL, NULL, NULL, 'ENABLED', '0', 10, 'control', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (214004, 1, 213002, 211001, 'CONTROL_MOTOR', '电机控制', 'Motor Control', 'MOTOR', 'MATERIAL', 130002, 'MOTOR_AOK_45_ZIGBEE', '奥克45管状电机', '电机控制', 'Motor Control', 'MOTOR', '[{"componentCode":"COMP_ROLLER_MOTOR","qty":1,"unitCode":"SET"}]'::jsonb, '[{"assetCode":"ASSET_MOTOR_SPEC","usageType":"SPEC"}]'::jsonb, NULL, 'ENABLED', '0', 20, 'motor option', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (214005, 1, 213003, 211001, 'PACK_BASIC', '基础安装包', 'Basic Install Pack', 'PACK_BASIC', 'COMPONENT', 170001, 'COMP_ROLLER_BASIC', '卷帘基础组件包', '基础安装包', 'Basic Install Pack', 'PACK_BASIC', '[{"componentCode":"COMP_ROLLER_BASIC","qty":1,"unitCode":"SET"}]'::jsonb, NULL, NULL, 'ENABLED', '0', 10, 'pack', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00'),
    (214006, 1, 213003, 211001, 'PACK_NONE', '不含安装包', 'No Install Pack', 'PACK_NONE', 'MANUAL', NULL, 'PACK_NONE', '不含安装包', '不含安装包', 'No Install Pack', 'PACK_NONE', NULL, NULL, NULL, 'ENABLED', '0', 20, 'pack', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00')
ON CONFLICT (tenant_id, question_id, option_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_config_option (
    option_id, tenant_id, question_id, template_version_id, option_code, option_name_cn, option_name_en, option_value,
    source_type, source_ref_id, source_code, source_name, display_name_cn, display_name_en, value_code, component_json, media_json,
    rule_json, status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
)
SELECT ids.option_id, 1, base.question_id + seed.question_offset, base.template_version_id + seed.version_offset, base.option_code, base.option_name_cn, base.option_name_en, base.option_value,
       source_type, source_ref_id, source_code, source_name, display_name_cn, display_name_en, value_code,
       component_json, media_json, rule_json, status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
FROM (
    VALUES (10, 1, 100, 214001), (20, 2, 200, 214001), (30, 3, 300, 214001), (40, 4, 400, 214001)
) AS seed(question_offset, version_offset, option_offset, base_option_id)
JOIN pc_config_option base ON base.option_id BETWEEN 214001 AND 214006
CROSS JOIN LATERAL (SELECT base.option_id + option_offset AS option_id) ids
WHERE base.tenant_id = 1
ON CONFLICT (tenant_id, question_id, option_code) WHERE del_flag = '0' DO NOTHING;

UPDATE pc_config_option
SET source_type = 'COMPONENT',
    source_ref_id = 170004,
    source_code = 'COMP_OUTDOOR_INSTALL',
    source_name = '户外遮阳安装包',
    component_json = '[{"componentCode":"COMP_OUTDOOR_INSTALL","qty":1,"unitCode":"SET"}]'::jsonb,
    media_json = '[{"assetCode":"ASSET_OUTDOOR_DRAWING","usageType":"DRAWING"}]'::jsonb
WHERE template_version_id = 211002 AND option_code = 'PACK_BASIC';

UPDATE pc_config_option
SET source_type = 'COMPONENT',
    source_ref_id = 170003,
    source_code = 'COMP_ZEBRA_BASIC',
    source_name = '柔纱帘基础组件包',
    component_json = '[{"componentCode":"COMP_ZEBRA_BASIC","qty":1,"unitCode":"SET"}]'::jsonb,
    media_json = '[{"assetCode":"ASSET_ZEBRA_INSTALL","usageType":"INSTALL_GUIDE"}]'::jsonb
WHERE template_version_id = 211003 AND option_code = 'PACK_BASIC';

UPDATE pc_config_option
SET source_type = 'COMPONENT',
    source_ref_id = 170005,
    source_code = 'COMP_CURTAIN_TRACK_MOTOR',
    source_name = '轨道电机组件包',
    component_json = '[{"componentCode":"COMP_CURTAIN_TRACK_MOTOR","qty":1,"unitCode":"SET"}]'::jsonb,
    media_json = '[{"assetCode":"ASSET_TRACK_GUIDE","usageType":"INSTALL_GUIDE"}]'::jsonb
WHERE template_version_id = 211005 AND option_code = 'PACK_BASIC';

INSERT INTO pc_config_rule (
    rule_id, tenant_id, template_version_id, rule_code, rule_name_cn, rule_name_en, rule_type, priority,
    condition_json, action_json, error_message_cn, error_message_en, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
    (215001, 1, 211001, 'RULE_BASIC_REQUIRED', '基础卷帘必填校验', 'Basic required validation', 'REQUIRED', 10, '{"required":["FABRIC_SERIES","CONTROL_METHOD","INSTALL_PACK"]}'::jsonb, '{"result":"BLOCKER"}'::jsonb, '必填配置未完成', 'Required options are missing', 'ENABLED', '0', 'Seed rule', 'system', '2026-06-12 00:00:00+00', 'system', '2026-06-12 00:00:00+00')
ON CONFLICT (tenant_id, template_version_id, rule_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_engineering_plan (
    plan_id, tenant_id, plan_code, plan_name_cn, plan_name_en, category_id, category_code, category_name_cn, category_name_en,
    series_id, series_code, series_name_cn, series_name_en, current_version_id, current_version_no, biz_status, status, del_flag,
    sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (300001, 1, 'ENG_ROLLER_BASIC', '卷帘工程方案', 'Roller Shade Engineering Plan', 100101, 'ROLLER_SHADE_BASIC', '基础卷帘分类', 'Basic Roller Shade Category',
     140001, 'FS_BASIC_COATED', '基础涂层面料', 'Basic Coated Fabric', 301001, 'V1', 'DRAFT', 'ENABLED', '0',
     10, 'Seed engineering plan for roller shade', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, plan_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_engineering_plan_version (
    version_id, tenant_id, plan_id, plan_code, version_no, version_name, biz_status, status, del_flag, rule_schema_version, config_json,
    remark, create_by, create_time, update_by, update_time
) VALUES
    (301001, 1, 300001, 'ENG_ROLLER_BASIC', 'V1', '初始草稿', 'DRAFT', 'ENABLED', '0', 'JSON_V1', '{"sample":true}'::jsonb,
     'Seed engineering version', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, plan_id, version_no) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_engineering_item (
    item_id, tenant_id, version_id, plan_id, item_code, item_name_cn, item_name_en, item_type, source_type,
    required_flag, multi_select_flag, customer_selectable, default_source_code, status, del_flag, sort_order, extra_json,
    remark, create_by, create_time, update_by, update_time
) VALUES
    (302001, 1, 301001, 300001, 'MAIN_FABRIC', '主面料', 'Main Fabric', 'FABRIC', 'MATERIAL', '1', '0', '1', 'FABRIC_BASIC_WHITE', 'ENABLED', '0', 10, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (302002, 1, 301001, 300001, 'SYSTEM', '系统', 'System', 'MATERIAL', 'MATERIAL', '1', '0', '1', 'CHAIN_ROLLER_WHITE', 'ENABLED', '0', 20, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (302003, 1, 301001, 300001, 'INSTALL_PACK', '安装零件包', 'Install Pack', 'COMPONENT', 'COMPONENT', '1', '0', '0', 'COMP_ROLLER_BASIC', 'ENABLED', '0', 30, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (302004, 1, 301001, 300001, 'PACKAGING', '包装方式', 'Packaging', 'MATERIAL', 'MATERIAL', '1', '0', '0', 'BOX_ROLLER_STD', 'ENABLED', '0', 40, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, version_id, item_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_engineering_item_scope (
    scope_id, tenant_id, version_id, item_id, item_code, scope_type, scope_code, scope_name_cn, scope_name_en, include_flag,
    condition_json, status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (303001, 1, 301001, 302001, 'MAIN_FABRIC', 'MATERIAL_TYPE', 'FABRIC', '面料', 'Fabric', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303002, 1, 301001, 302002, 'SYSTEM', 'MATERIAL_CODE', 'CHAIN_ROLLER_WHITE', '手动拉珠', 'Chain Control', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303003, 1, 301001, 302002, 'SYSTEM', 'MATERIAL_CODE', 'MOTOR_AOK_45_ZIGBEE', '奥克45管状电机', 'AOK 45 Tubular Motor', 'INCLUDE', NULL, 'ENABLED', '0', 20, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303004, 1, 301001, 302003, 'INSTALL_PACK', 'COMPONENT_CODE', 'COMP_ROLLER_BASIC', '卷帘基础组件包', 'Roller Basic Component Pack', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT DO NOTHING;

INSERT INTO pc_engineering_rule (
    rule_id, tenant_id, version_id, rule_code, rule_name_cn, rule_name_en, rule_type, condition_json, action_json, severity,
    message_cn, message_en, status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (304001, 1, 301001, 'WIDTH_DISABLE_CHAIN', '宽度超限禁用拉珠', 'Disable chain when width is too large', 'DIMENSION_LIMIT',
     '{"all":[{"left":"input.width","op":"GT","right":250}]}'::jsonb,
     '{"type":"DISABLE_OPTION","itemCode":"SYSTEM","scopeCode":"CHAIN_ROLLER_WHITE"}'::jsonb,
     'WARNING', '当前宽度不建议选择拉珠系统', 'Chain system is not recommended for this width', 'ENABLED', '0', 10, 'Seed rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, version_id, rule_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_engineering_output_rule (
    output_rule_id, tenant_id, version_id, rule_code, rule_name_cn, rule_name_en, condition_json, output_type, output_code,
    output_name_cn, output_name_en, default_qty, unit_code, reason_cn, reason_en, status, del_flag, sort_order,
    remark, create_by, create_time, update_by, update_time
) VALUES
    (305001, 1, 301001, 'MOTOR_OUTPUT_PACK', '电机带出电机组件包', 'Motor outputs motor component pack',
     '{"all":[{"left":"selected.SYSTEM","op":"EQ","right":"MOTOR_AOK_45_ZIGBEE"}]}'::jsonb,
     'COMPONENT', 'COMP_ROLLER_MOTOR', '卷帘电动控制包', 'Roller Motor Control Pack', 1.000000, 'SET',
     '选择电机系统后自动带出电动控制包', 'Motor system requires motor control pack', 'ENABLED', '0', 10,
     'Seed output rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (305002, 1, 301001, 'MOTOR_OUTPUT_SPEC', '电机带出规格书', 'Motor outputs specification',
     '{"all":[{"left":"selected.SYSTEM","op":"EQ","right":"MOTOR_AOK_45_ZIGBEE"}]}'::jsonb,
     'MEDIA', 'ASSET_MOTOR_SPEC', '卷帘电机规格书', 'Roller Motor Specification', 1.000000, 'PCS',
     '选择电机系统后自动带出电机规格书', 'Motor system requires motor specification', 'ENABLED', '0', 20,
     'Seed output rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT DO NOTHING;

INSERT INTO pc_standard_sku_engineering (
    sku_engineering_id, tenant_id, version_id, standard_sku_id, standard_sku_code, standard_sku_name_cn, standard_sku_name_en,
    fixed_items_json, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
    (306001, 1, 301001, NULL, 'STD_ROLLER_55_CHAIN', '55寸拉珠卷帘', '55 inch chain roller shade',
     '{"MAIN_FABRIC":"FABRIC_BASIC_WHITE","SYSTEM":"CHAIN_ROLLER_WHITE","INSTALL_PACK":"COMP_ROLLER_BASIC"}'::jsonb,
     'ENABLED', '0', 'Seed standard SKU engineering', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, version_id, standard_sku_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_engineering_check_case (
    check_case_id, tenant_id, version_id, case_code, case_name_cn, case_name_en, input_json, expected_json,
    status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (307001, 1, 301001, 'ROLLER_MOTOR_180', '卷帘电机180宽检查', 'Roller motor width 180 check',
     '{"width":180,"height":160,"selectedItems":{"SYSTEM":"MOTOR_AOK_45_ZIGBEE"}}'::jsonb,
     '{"blockers":0,"outputs":["COMP_ROLLER_MOTOR"]}'::jsonb,
     'ENABLED', '0', 10, 'Seed check case', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, version_id, case_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_category (
    category_id, tenant_id, parent_id, category_code, category_name_cn, category_name_en, business_type,
    category_level, category_path, status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (100106, 1, 100001, 'HONEYCOMB_SHADE', '蜂巢帘', 'Honeycomb Shade', 'PRODUCT_BASE', 2, 'WINDOW_COVERING/HONEYCOMB_SHADE', 'ENABLED', '0', 15, 'Sample: HONEYCOMB_SHADE', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, category_code) WHERE del_flag = '0' DO UPDATE
SET category_name_cn = EXCLUDED.category_name_cn,
    category_name_en = EXCLUDED.category_name_en,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

INSERT INTO pc_engineering_plan (
    plan_id, tenant_id, plan_code, plan_name_cn, plan_name_en, category_id, category_code, category_name_cn, category_name_en,
    series_id, series_code, series_name_cn, series_name_en, current_version_id, current_version_no, biz_status, status, del_flag,
    sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (300002, 1, 'ENG_HONEYCOMB_25', '蜂巢帘工程方案', 'Honeycomb Shade Engineering Plan', 100106, 'HONEYCOMB_SHADE', '蜂巢帘', 'Honeycomb Shade',
     140101, 'FS_HONEYCOMB_25', '25mm蜂巢布系列', '25mm Honeycomb Fabric Series', 301002, 'V1', 'DRAFT', 'ENABLED', '0',
     20, 'Seed engineering plan for honeycomb shade', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, plan_code) WHERE del_flag = '0' DO UPDATE
SET plan_name_cn = EXCLUDED.plan_name_cn,
    plan_name_en = EXCLUDED.plan_name_en,
    category_id = EXCLUDED.category_id,
    category_code = EXCLUDED.category_code,
    category_name_cn = EXCLUDED.category_name_cn,
    series_id = EXCLUDED.series_id,
    series_code = EXCLUDED.series_code,
    current_version_id = EXCLUDED.current_version_id,
    current_version_no = EXCLUDED.current_version_no,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

INSERT INTO pc_engineering_plan_version (
    version_id, tenant_id, plan_id, plan_code, version_no, version_name, biz_status, status, del_flag, rule_schema_version, config_json,
    remark, create_by, create_time, update_by, update_time
) VALUES
    (301002, 1, 300002, 'ENG_HONEYCOMB_25', 'V1', '初始草稿', 'DRAFT', 'ENABLED', '0', 'JSON_V1', '{"sample":true,"series":"HONEYCOMB_SHADE"}'::jsonb,
     'Seed engineering version', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, plan_id, version_no) WHERE del_flag = '0' DO UPDATE
SET version_name = EXCLUDED.version_name,
    biz_status = EXCLUDED.biz_status,
    status = EXCLUDED.status,
    config_json = EXCLUDED.config_json,
    update_by = 'system',
    update_time = now();

INSERT INTO pc_engineering_item (
    item_id, tenant_id, version_id, plan_id, item_code, item_name_cn, item_name_en, item_type, source_type,
    required_flag, multi_select_flag, customer_selectable, default_source_code, status, del_flag, sort_order, extra_json,
    remark, create_by, create_time, update_by, update_time
) VALUES
    (302005, 1, 301001, 300001, 'SECOND_FABRIC', '副面料', 'Second Fabric', 'FABRIC', 'MATERIAL', '0', '0', '1', NULL, 'ENABLED', '0', 15, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (302006, 1, 301001, 300001, 'ALUMINUM', '铝材/下杆/轨道', 'Aluminum / Bottom Rail / Track', 'MATERIAL', 'MATERIAL', '1', '1', '0', 'HEM_BAR_ALU_32', 'ENABLED', '0', 25, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (302007, 1, 301001, 300001, 'ACCESSORY', '配件', 'Accessory', 'MATERIAL', 'MATERIAL', '0', '1', '0', NULL, 'ENABLED', '0', 35, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (302008, 1, 301001, 300001, 'MEDIA', '资料', 'Media', 'MEDIA', 'MEDIA', '0', '1', '0', NULL, 'ENABLED', '0', 50, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (302101, 1, 301002, 300002, 'MAIN_FABRIC', '主面料', 'Main Fabric', 'FABRIC', 'MATERIAL', '1', '0', '1', 'FABRIC_HONEYCOMB_IVORY', 'ENABLED', '0', 10, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (302102, 1, 301002, 300002, 'SECOND_FABRIC', '副面料', 'Second Fabric', 'FABRIC', 'MATERIAL', '0', '0', '1', NULL, 'ENABLED', '0', 15, '{"doubleFabricAllowed":true}'::jsonb, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (302103, 1, 301002, 300002, 'ALUMINUM', '铝材/下杆/轨道', 'Aluminum / Bottom Rail / Track', 'MATERIAL', 'MATERIAL', '1', '1', '0', 'PROFILE_HONEYCOMB_HEADRAIL', 'ENABLED', '0', 20, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (302104, 1, 301002, 300002, 'SYSTEM', '系统', 'System', 'MATERIAL', 'MATERIAL', '1', '0', '1', 'CHAIN_HONEYCOMB_WHITE', 'ENABLED', '0', 30, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (302105, 1, 301002, 300002, 'ACCESSORY', '配件', 'Accessory', 'MATERIAL', 'MATERIAL', '0', '1', '0', NULL, 'ENABLED', '0', 40, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (302106, 1, 301002, 300002, 'INSTALL_PACK', '安装零件包', 'Install Pack', 'COMPONENT', 'COMPONENT', '1', '0', '0', 'COMP_INSTALL_STD', 'ENABLED', '0', 50, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (302107, 1, 301002, 300002, 'PACKAGING', '包装方式', 'Packaging', 'COMPONENT', 'COMPONENT', '1', '0', '0', 'COMP_PACKAGING_STD', 'ENABLED', '0', 60, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (302108, 1, 301002, 300002, 'MEDIA', '资料', 'Media', 'MEDIA', 'MEDIA', '0', '1', '0', NULL, 'ENABLED', '0', 70, NULL, 'Seed item', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, version_id, item_code) WHERE del_flag = '0' DO UPDATE
SET item_name_cn = EXCLUDED.item_name_cn,
    item_name_en = EXCLUDED.item_name_en,
    item_type = EXCLUDED.item_type,
    source_type = EXCLUDED.source_type,
    required_flag = EXCLUDED.required_flag,
    default_source_code = EXCLUDED.default_source_code,
    status = EXCLUDED.status,
    sort_order = EXCLUDED.sort_order,
    update_by = 'system',
    update_time = now();

INSERT INTO pc_engineering_item_scope (
    scope_id, tenant_id, version_id, item_id, item_code, scope_type, scope_code, scope_name_cn, scope_name_en, include_flag,
    condition_json, status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (303005, 1, 301001, 302005, 'SECOND_FABRIC', 'MATERIAL_TYPE', 'FABRIC', '面料', 'Fabric', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303006, 1, 301001, 302006, 'ALUMINUM', 'MATERIAL_CODE', 'HEM_BAR_ALU_32', '32mm铝合金配重条', '32mm Aluminum Hem Bar', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303007, 1, 301001, 302007, 'ACCESSORY', 'MATERIAL_CODE', 'GLUE_TAPE_PET_20', 'PET胶条20mm', 'PET Glue Tape 20mm', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303008, 1, 301001, 302008, 'MEDIA', 'MEDIA_CODE', 'ASSET_ROLLER_INSTALL', '卷帘安装说明', 'Roller Installation Guide', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303101, 1, 301002, 302101, 'MAIN_FABRIC', 'FABRIC_SERIES', 'FS_HONEYCOMB_25', '25mm蜂巢布系列', '25mm Honeycomb Fabric Series', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303102, 1, 301002, 302102, 'SECOND_FABRIC', 'MATERIAL_TYPE', 'FABRIC', '面料', 'Fabric', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303103, 1, 301002, 302103, 'ALUMINUM', 'MATERIAL_TYPE', 'PROFILE', '型材', 'Profile', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303104, 1, 301002, 302103, 'ALUMINUM', 'MATERIAL_TYPE', 'BOTTOM_RAIL', '下杆', 'Bottom Rail', 'INCLUDE', NULL, 'ENABLED', '0', 20, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303105, 1, 301002, 302104, 'SYSTEM', 'MATERIAL_CODE', 'CHAIN_HONEYCOMB_WHITE', '拉珠', 'Chain Control', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303106, 1, 301002, 302104, 'SYSTEM', 'MATERIAL_CODE', 'MOTOR_AOK_45_ZIGBEE', '电机', 'Motor Control', 'INCLUDE', NULL, 'ENABLED', '0', 20, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303107, 1, 301002, 302105, 'ACCESSORY', 'MATERIAL_TYPE', 'REMOTE', '遥控器', 'Remote', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303108, 1, 301002, 302105, 'ACCESSORY', 'MATERIAL_TYPE', 'SOLAR_PANEL', '太阳能板', 'Solar Panel', 'INCLUDE', NULL, 'ENABLED', '0', 20, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303109, 1, 301002, 302106, 'INSTALL_PACK', 'COMPONENT_CODE', 'COMP_INSTALL_STD', '通用安装零件包', 'Standard Install Pack', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303110, 1, 301002, 302107, 'PACKAGING', 'COMPONENT_CODE', 'COMP_PACKAGING_STD', '通用包装包', 'Standard Packaging Pack', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (303111, 1, 301002, 302108, 'MEDIA', 'MEDIA_CODE', 'ASSET_HONEYCOMB_INSTALL', '蜂巢帘安装说明', 'Honeycomb Installation Guide', 'INCLUDE', NULL, 'ENABLED', '0', 10, 'Seed scope', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT DO NOTHING;

INSERT INTO pc_engineering_rule (
    rule_id, tenant_id, version_id, rule_code, rule_name_cn, rule_name_en, rule_type, condition_json, action_json, severity,
    message_cn, message_en, status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (304002, 1, 301001, 'ROLLER_THICKNESS_BLOCK', '卷帘组合厚度阻断', 'Roller combined thickness blocker', 'THICKNESS_LIMIT', '{"all":[{"left":"input.combinedThickness","op":"GT","right":0.76}]}'::jsonb, '{"type":"BLOCK"}'::jsonb, 'BLOCKER', '当前面料组合厚度超出卷帘工程能力。', 'Combined fabric thickness exceeds roller engineering capability.', 'ENABLED', '0', 20, 'Seed rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (304003, 1, 301001, 'ROLLER_MOTOR_MEDIA_HINT', '卷帘电机资料提示', 'Roller motor media hint', 'OPTION_LIMIT', '{"all":[{"left":"selected.SYSTEM","op":"EQ","right":"MOTOR_AOK_45_ZIGBEE"}]}'::jsonb, '{"type":"MEDIA_HINT","assetCode":"ASSET_MOTOR_SPEC","assetNameCn":"电机规格书","assetNameEn":"Motor Specification"}'::jsonb, 'INFO', '选择电机系统时需要查看电机规格书和安装说明。', 'Motor system requires specification and installation guide.', 'ENABLED', '0', 30, 'Seed rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (304101, 1, 301002, 'HONEY_WIDTH_DISABLE_CHAIN', '蜂巢帘宽度超限禁用拉珠', 'Disable chain when honeycomb width is too large', 'DIMENSION_LIMIT', '{"all":[{"left":"input.width","op":"GT","right":250}]}'::jsonb, '{"type":"DISABLE_OPTION","itemCode":"SYSTEM","scopeCode":"CHAIN_HONEYCOMB_WHITE"}'::jsonb, 'WARNING', '当前宽度不建议选择拉珠系统。', 'Chain system is not recommended for this width.', 'ENABLED', '0', 10, 'Seed rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (304102, 1, 301002, 'HONEY_THICKNESS_BLOCK', '蜂巢帘组合厚度阻断', 'Honeycomb combined thickness blocker', 'THICKNESS_LIMIT', '{"all":[{"left":"input.combinedThickness","op":"GT","right":0.95}]}'::jsonb, '{"type":"BLOCK"}'::jsonb, 'BLOCKER', '双面料组合厚度超出蜂巢帘工程能力。', 'Combined fabric thickness exceeds honeycomb engineering capability.', 'ENABLED', '0', 20, 'Seed rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (304103, 1, 301002, 'HONEY_MOTOR_REQUIRE_ACCESSORY', '蜂巢帘电机要求补齐配件', 'Honeycomb motor requires accessory', 'COMPLETENESS', '{"all":[{"left":"selected.SYSTEM","op":"EQ","right":"MOTOR_AOK_45_ZIGBEE"}]}'::jsonb, '{"type":"REQUIRE_ITEM","itemCode":"ACCESSORY"}'::jsonb, 'WARNING', '选择电机系统时需要确认遥控器或太阳能板等配件。', 'Motor system requires accessory confirmation.', 'ENABLED', '0', 30, 'Seed rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (304104, 1, 301002, 'HONEY_MOTOR_MEDIA_HINT', '蜂巢帘电机资料提示', 'Honeycomb motor media hint', 'OPTION_LIMIT', '{"all":[{"left":"selected.SYSTEM","op":"EQ","right":"MOTOR_AOK_45_ZIGBEE"}]}'::jsonb, '{"type":"MEDIA_HINT","assetCode":"ASSET_MOTOR_SPEC","assetNameCn":"电机规格书","assetNameEn":"Motor Specification"}'::jsonb, 'INFO', '选择电机系统时需要查看电机规格书和安装说明。', 'Motor system requires specification and installation guide.', 'ENABLED', '0', 40, 'Seed rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, version_id, rule_code) WHERE del_flag = '0' DO UPDATE
SET rule_name_cn = EXCLUDED.rule_name_cn,
    condition_json = EXCLUDED.condition_json,
    action_json = EXCLUDED.action_json,
    severity = EXCLUDED.severity,
    message_cn = EXCLUDED.message_cn,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

INSERT INTO pc_engineering_output_rule (
    output_rule_id, tenant_id, version_id, rule_code, rule_name_cn, rule_name_en, condition_json, output_type, output_code,
    output_name_cn, output_name_en, default_qty, unit_code, reason_cn, reason_en, status, del_flag, sort_order,
    remark, create_by, create_time, update_by, update_time
) VALUES
    (305003, 1, 301001, 'ROLLER_OUTPUT_INSTALL', '卷帘带出安装包', 'Roller outputs install pack', '{"all":[]}'::jsonb, 'COMPONENT', 'COMP_INSTALL_STD', '通用安装零件包', 'Standard Install Pack', 1.000000, 'SET', '默认带出安装零件包', 'Default install pack', 'ENABLED', '0', 30, 'Seed output rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (305004, 1, 301001, 'ROLLER_OUTPUT_PACKAGING', '卷帘带出包装', 'Roller outputs packaging', '{"all":[]}'::jsonb, 'MATERIAL', 'BOX_ROLLER_STD', '卷帘标准纸箱', 'Roller Standard Carton', 1.000000, 'PCS', '默认带出包装方式', 'Default packaging', 'ENABLED', '0', 40, 'Seed output rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (305101, 1, 301002, 'HONEY_OUTPUT_CHAIN_PACK', '蜂巢帘拉珠带出系统包', 'Honeycomb chain outputs system pack', '{"all":[{"left":"selected.SYSTEM","op":"EQ","right":"CHAIN_HONEYCOMB_WHITE"}]}'::jsonb, 'COMPONENT', 'COMP_HONEYCOMB_CHAIN', '蜂巢帘拉珠系统包', 'Honeycomb Chain System Pack', 1.000000, 'SET', '选择拉珠系统后自动带出拉珠系统包', 'Chain system outputs chain pack', 'ENABLED', '0', 10, 'Seed output rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (305102, 1, 301002, 'HONEY_OUTPUT_MOTOR_PACK', '蜂巢帘电机带出系统包', 'Honeycomb motor outputs system pack', '{"all":[{"left":"selected.SYSTEM","op":"EQ","right":"MOTOR_AOK_45_ZIGBEE"}]}'::jsonb, 'COMPONENT', 'COMP_HONEYCOMB_MOTOR', '蜂巢帘电动系统包', 'Honeycomb Motor System Pack', 1.000000, 'SET', '选择电机系统后自动带出电动系统包', 'Motor system outputs motor pack', 'ENABLED', '0', 20, 'Seed output rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (305103, 1, 301002, 'HONEY_OUTPUT_INSTALL', '蜂巢帘带出安装包', 'Honeycomb outputs install pack', '{"all":[]}'::jsonb, 'COMPONENT', 'COMP_INSTALL_STD', '通用安装零件包', 'Standard Install Pack', 1.000000, 'SET', '默认带出安装零件包', 'Default install pack', 'ENABLED', '0', 30, 'Seed output rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (305104, 1, 301002, 'HONEY_OUTPUT_PACKAGING', '蜂巢帘带出包装', 'Honeycomb outputs packaging', '{"all":[]}'::jsonb, 'MATERIAL', 'BOX_HONEYCOMB_STD', '蜂巢帘标准纸箱', 'Honeycomb Standard Carton', 1.000000, 'PCS', '默认带出包装方式', 'Default packaging', 'ENABLED', '0', 40, 'Seed output rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (305105, 1, 301002, 'HONEY_OUTPUT_INSTALL_GUIDE', '蜂巢帘带出安装说明', 'Honeycomb outputs install guide', '{"all":[]}'::jsonb, 'MEDIA', 'ASSET_HONEYCOMB_INSTALL', '蜂巢帘安装说明', 'Honeycomb Installation Guide', 1.000000, 'PCS', '默认带出安装说明资料', 'Default installation guide', 'ENABLED', '0', 50, 'Seed output rule', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT DO NOTHING;

INSERT INTO pc_standard_sku_engineering (
    sku_engineering_id, tenant_id, version_id, standard_sku_id, standard_sku_code, standard_sku_name_cn, standard_sku_name_en,
    fixed_items_json, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
    (306002, 1, 301002, NULL, 'STD_HONEYCOMB_55_CHAIN', '55寸拉珠蜂巢帘', '55 inch chain honeycomb shade', '{"MAIN_FABRIC":"FABRIC_HONEYCOMB_IVORY","SYSTEM":"CHAIN_HONEYCOMB_WHITE","ALUMINUM":"PROFILE_HONEYCOMB_HEADRAIL","INSTALL_PACK":"COMP_INSTALL_STD","PACKAGING":"COMP_PACKAGING_STD"}'::jsonb, 'ENABLED', '0', 'Seed standard SKU engineering', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, version_id, standard_sku_code) WHERE del_flag = '0' DO UPDATE
SET standard_sku_name_cn = EXCLUDED.standard_sku_name_cn,
    fixed_items_json = EXCLUDED.fixed_items_json,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

INSERT INTO pc_engineering_check_case (
    check_case_id, tenant_id, version_id, case_code, case_name_cn, case_name_en, input_json, expected_json,
    status, del_flag, sort_order, remark, create_by, create_time, update_by, update_time
) VALUES
    (307002, 1, 301002, 'HONEY_CHAIN_180', '蜂巢帘拉珠180宽检查', 'Honeycomb chain width 180 check', '{"width":180,"height":160,"combinedThickness":0.6,"selectedItems":{"SYSTEM":"CHAIN_HONEYCOMB_WHITE"}}'::jsonb, '{"blockers":0,"outputs":["COMP_HONEYCOMB_CHAIN"]}'::jsonb, 'ENABLED', '0', 10, 'Seed check case', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00'),
    (307003, 1, 301002, 'HONEY_MOTOR_180', '蜂巢帘电机180宽检查', 'Honeycomb motor width 180 check', '{"width":180,"height":160,"combinedThickness":0.6,"selectedItems":{"SYSTEM":"MOTOR_AOK_45_ZIGBEE","ACCESSORY":"REMOTE_6CH_WHITE"}}'::jsonb, '{"blockers":0,"outputs":["COMP_HONEYCOMB_MOTOR"]}'::jsonb, 'ENABLED', '0', 20, 'Seed check case', 'system', '2026-06-14 00:00:00+00', 'system', '2026-06-14 00:00:00+00')
ON CONFLICT (tenant_id, version_id, case_code) WHERE del_flag = '0' DO UPDATE
SET case_name_cn = EXCLUDED.case_name_cn,
    input_json = EXCLUDED.input_json,
    expected_json = EXCLUDED.expected_json,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now();

-- =====================================================
-- 第一阶段正式菜单、按钮和字典归一化
-- 说明：
-- 1. 正式侧边栏使用四个一级菜单：基础信息、产品工程设计、产品配置中心、产品发布。
-- 2. 菜单最多两级；页面内 tabs 只能作为局部细节，不承载正式主入口。
-- 3. 本块必须保持幂等，可重复执行到开发库。
-- =====================================================

DELETE FROM sys_role_menu WHERE menu_id IN (24201, 24202, 24203, 24204, 24205, 24206, 24207, 24208, 24209, 24210, 24211, 24212, 24213, 24220, 24301, 24302);
DELETE FROM sys_menu WHERE menu_id IN (24201, 24202, 24203, 24204, 24205, 24206, 24207, 24208, 24209, 24210, 24211, 24212, 24213, 24220, 24301, 24302);

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
    (24211, 1, 24200, 'Data Entry Guide', 'productCenter.menu.masterGuide', 1, 'guide', 'product-center/master-guide', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'guide', 'system', now(), NULL, NULL, '基础资料录入向导'),
    (24212, 1, 24200, 'Product Categories', 'productCenter.menu.categories', 2, 'categories', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'tree-table', 'system', now(), NULL, NULL, '产品分类'),
    (24206, 1, 24200, 'Units', 'productCenter.menu.units', 3, 'units', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:unit:list', 'unit', 'system', now(), NULL, NULL, '单位管理'),
    (24213, 1, 24200, 'Product Dictionaries', 'productCenter.menu.productDicts', 4, 'product-dicts', 'product-center/product-dicts', NULL, '1', '0', 'C', '1', '1', 'product:dict:list', 'dict', 'system', now(), NULL, NULL, '产品业务字典'),
    (24204, 1, 24200, 'Material Attribute Definitions', 'productCenter.menu.baseAttributes', 5, 'base-attributes', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base-attribute:list', 'dict', 'system', now(), NULL, NULL, '物料属性定义'),
    (24202, 1, 24200, 'Materials', 'productCenter.menu.materials', 6, 'materials', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'inventory', 'system', now(), NULL, NULL, '物料资料'),
    (24208, 1, 24200, 'Material Attributes', 'productCenter.menu.materialAttributes', 7, 'material-attributes', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:material-attribute:list', 'list', 'system', now(), NULL, NULL, '物料属性'),
    (24201, 1, 24200, 'Fabric Series', 'productCenter.menu.fabricSeries', 8, 'fabric-series', 'product-center/fabric', NULL, '1', '0', 'C', '1', '1', 'product:fabric:list', 'color', 'system', now(), NULL, NULL, '面料系列'),
    (24207, 1, 24200, 'Fabric Profiles', 'productCenter.menu.fabricProfiles', 9, 'fabric-profiles', 'product-center/fabric', NULL, '1', '0', 'C', '1', '1', 'product:fabric:list', 'swatch', 'system', now(), NULL, NULL, '面料资料'),
    (24203, 1, 24200, 'Component Packs', 'productCenter.menu.components', 10, 'components', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'component', 'system', now(), NULL, NULL, '组件包'),
    (24205, 1, 24200, 'Media Assets', 'productCenter.menu.mediaAssets', 11, 'media-assets', 'product-center/assets', NULL, '1', '0', 'C', '1', '1', 'product:asset:list', 'upload', 'system', now(), NULL, NULL, '资料资产'),
    (24209, 1, 24200, 'Component Items', 'productCenter.menu.componentItems', 92, 'component-items', 'product-center/base', NULL, '1', '0', 'C', '0', '1', 'product:component-item:list', 'list', 'system', now(), NULL, NULL, '组件明细，从组件包行内入口维护'),
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
    (24522, 1, 24500, 'Check Cases', 'productCenter.menu.engineeringCheckCase', 93, 'check-case', 'product-engineering/check-case', NULL, '1', '0', 'C', '0', '1', 'product:engineering:list', 'example', 'system', now(), NULL, NULL, '检查样例，低频隐藏'),
    (24304, 1, 24300, 'Configuration Entry Workbench', 'productCenter.menu.template', 1, 'template', 'product-center/template', NULL, '1', '0', 'C', '1', '1', 'product:template:list', 'form', 'system', now(), NULL, NULL, '配置录入工作台'),
    (24301, 1, 24300, 'Sales Products', 'productCenter.menu.salesProducts', 2, 'sales-products', 'product-center/sales-products', NULL, '1', '0', 'C', '1', '1', 'product:sales-product:list', 'product', 'system', now(), NULL, NULL, '销售产品台账'),
    (24303, 1, 24300, 'Question Groups', 'productCenter.menu.questionGroups', 3, 'question-groups', 'product-center/question-groups', NULL, '1', '0', 'C', '1', '1', 'product:template:list', 'list', 'system', now(), NULL, NULL, '问题组模板'),
    (24307, 1, 24300, 'Config Questions', 'productCenter.menu.configQuestions', 4, 'config-questions', 'product-center/config-questions', NULL, '1', '0', 'C', '1', '1', 'product:template:list', 'question', 'system', now(), NULL, NULL, '配置问题'),
    (24308, 1, 24300, 'Config Options', 'productCenter.menu.configOptions', 5, 'config-options', 'product-center/config-options', NULL, '1', '0', 'C', '1', '1', 'product:template:list', 'list', 'system', now(), NULL, NULL, '配置答案'),
    (24309, 1, 24300, 'Config Evaluator', 'productCenter.menu.configEvaluator', 6, 'config-evaluator', 'product-center/config-evaluator', NULL, '1', '0', 'C', '1', '1', 'product:template:test', 'calculator', 'system', now(), NULL, NULL, '配置求值器'),
    (24302, 1, 24300, 'Standard SKU', 'productCenter.menu.standardSkus', 7, 'standard-skus', 'product-center/standard-skus', NULL, '1', '0', 'C', '0', '1', 'product:standard-sku:list', 'barcode', 'system', now(), NULL, NULL, '标品SKU预留入口'),
    (24305, 1, 24300, 'Pricing', 'productCenter.menu.pricing', 91, 'pricing', 'product-center/pricing', NULL, '1', '0', 'C', '0', '1', 'product:price:list', 'money', 'system', now(), NULL, NULL, '价格中心，本阶段不改造，默认隐藏'),
    (24306, 1, 24300, 'Quote Preview', 'productCenter.menu.quotePreview', 92, 'quote-preview', 'product-center/quote-preview', NULL, '1', '0', 'C', '0', '1', 'product:price:test', 'quote', 'system', now(), NULL, NULL, '报价预览，本阶段不改造，默认隐藏'),
    (24401, 1, 24400, 'Publish Gate', 'productCenter.menu.publish', 1, 'publish', 'product-center/publish', NULL, '1', '0', 'C', '1', '1', 'product:publish:list', 'check', 'system', now(), NULL, NULL, '测试发布'),
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
    (24230, 1, 24204, 'Material Attribute Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base-attribute:list', '#', 'system', now(), NULL, NULL, '物料属性查询'),
    (24231, 1, 24204, 'Material Attribute Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base-attribute:add', '#', 'system', now(), NULL, NULL, '物料属性新增'),
    (24232, 1, 24204, 'Material Attribute Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base-attribute:edit', '#', 'system', now(), NULL, NULL, '物料属性编辑'),
    (24233, 1, 24204, 'Material Attribute Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base-attribute:remove', '#', 'system', now(), NULL, NULL, '物料属性删除'),
    (24234, 1, 24204, 'Material Attribute Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base-attribute:reference', '#', 'system', now(), NULL, NULL, '物料属性引用检查'),
    (24235, 1, 24213, 'Product Dictionary Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:dict:list', '#', 'system', now(), NULL, NULL, '产品字典查询'),
    (24236, 1, 24213, 'Product Dictionary Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:dict:add', '#', 'system', now(), NULL, NULL, '产品字典新增'),
    (24237, 1, 24213, 'Product Dictionary Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:dict:edit', '#', 'system', now(), NULL, NULL, '产品字典编辑'),
    (24238, 1, 24213, 'Product Dictionary Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:dict:remove', '#', 'system', now(), NULL, NULL, '产品字典删除'),
    (24239, 1, 24213, 'Product Dictionary Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:dict:reference', '#', 'system', now(), NULL, NULL, '产品字典引用检查'),
    (24240, 1, 24202, 'Material Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:list', '#', 'system', now(), NULL, NULL, '物料查询'),
    (24241, 1, 24202, 'Material Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:add', '#', 'system', now(), NULL, NULL, '物料新增'),
    (24242, 1, 24202, 'Material Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:edit', '#', 'system', now(), NULL, NULL, '物料编辑'),
    (24243, 1, 24202, 'Material Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:remove', '#', 'system', now(), NULL, NULL, '物料删除'),
    (24280, 1, 24208, 'Material Attribute Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-attribute:list', '#', 'system', now(), NULL, NULL, '物料属性查询'),
    (24281, 1, 24208, 'Material Attribute Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-attribute:add', '#', 'system', now(), NULL, NULL, '物料属性新增'),
    (24282, 1, 24208, 'Material Attribute Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-attribute:edit', '#', 'system', now(), NULL, NULL, '物料属性编辑'),
    (24283, 1, 24208, 'Material Attribute Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-attribute:remove', '#', 'system', now(), NULL, NULL, '物料属性删除'),
    (24284, 1, 24208, 'Material Attribute Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:material-attribute:reference', '#', 'system', now(), NULL, NULL, '物料属性引用检查'),
    (24250, 1, 24203, 'Component Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:list', '#', 'system', now(), NULL, NULL, '辅材查询'),
    (24251, 1, 24203, 'Component Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:add', '#', 'system', now(), NULL, NULL, '辅材新增'),
    (24252, 1, 24203, 'Component Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:edit', '#', 'system', now(), NULL, NULL, '辅材编辑'),
    (24253, 1, 24203, 'Component Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:remove', '#', 'system', now(), NULL, NULL, '辅材删除'),
    (24290, 1, 24209, 'Component Item Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:component-item:list', '#', 'system', now(), NULL, NULL, '组件明细查询'),
    (24291, 1, 24209, 'Component Item Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:component-item:add', '#', 'system', now(), NULL, NULL, '组件明细新增'),
    (24292, 1, 24209, 'Component Item Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:component-item:edit', '#', 'system', now(), NULL, NULL, '组件明细编辑'),
    (24293, 1, 24209, 'Component Item Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:component-item:remove', '#', 'system', now(), NULL, NULL, '组件明细删除'),
    (24294, 1, 24209, 'Component Item Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:component-item:reference', '#', 'system', now(), NULL, NULL, '组件明细引用检查'),
    (24260, 1, 24205, 'Asset Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:list', '#', 'system', now(), NULL, NULL, '资料资产查询'),
    (24261, 1, 24205, 'Asset Upload', 'common.upload', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:upload', '#', 'system', now(), NULL, NULL, '资料资产上传'),
    (24262, 1, 24205, 'Asset Reference', 'productCenter.common.references', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:reference', '#', 'system', now(), NULL, NULL, '资料资产引用检查'),
    (24270, 1, 24210, 'Binding Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:list', '#', 'system', now(), NULL, NULL, '资料绑定查询'),
    (24271, 1, 24210, 'Binding Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:bind', '#', 'system', now(), NULL, NULL, '资料绑定新增'),
    (24272, 1, 24210, 'Binding Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:bind', '#', 'system', now(), NULL, NULL, '资料绑定编辑'),
    (24273, 1, 24210, 'Binding Reference', 'productCenter.common.references', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:reference', '#', 'system', now(), NULL, NULL, '资料绑定引用检查'),
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
    'product_material_type',
    'product_component_type',
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
    'product_material_type',
    'product_component_type',
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
