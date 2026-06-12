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

COMMENT ON TABLE pc_base_attribute IS '基础属性定义表';
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
    supplier_code varchar(80),
    supplier_name varchar(200),
    factory_model varchar(120),
    sample_book_no varchar(120),
    vendor_item_no varchar(120),
    primary_spec varchar(200),
    primary_color varchar(120),
    primary_weight numeric(18,6),
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
    ADD COLUMN IF NOT EXISTS supplier_code varchar(80),
    ADD COLUMN IF NOT EXISTS factory_model varchar(120),
    ADD COLUMN IF NOT EXISTS sample_book_no varchar(120),
    ADD COLUMN IF NOT EXISTS vendor_item_no varchar(120),
    ADD COLUMN IF NOT EXISTS primary_spec varchar(200),
    ADD COLUMN IF NOT EXISTS primary_color varchar(120),
    ADD COLUMN IF NOT EXISTS primary_weight numeric(18,6),
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
COMMENT ON COLUMN pc_material.supplier_code IS '供应商编码快照';
COMMENT ON COLUMN pc_material.supplier_name IS '供应商名称';
COMMENT ON COLUMN pc_material.factory_model IS '工厂型号';
COMMENT ON COLUMN pc_material.sample_book_no IS '样册编号';
COMMENT ON COLUMN pc_material.vendor_item_no IS '供应商料号';
COMMENT ON COLUMN pc_material.primary_spec IS '主规格摘要';
COMMENT ON COLUMN pc_material.primary_color IS '主颜色摘要';
COMMENT ON COLUMN pc_material.primary_weight IS '主重量/克重数值';
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
    (110005, 1, 'SQM', '平方米', 'Square Meter', 'AREA', 3, 'HALF_UP', 'SQM', 1.000000, 'ENABLED', '0', 50, '面积单位', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (110006, 1, 'GSM', '克每平方米', 'Gram per Square Meter', 'WEIGHT', 0, 'HALF_UP', 'GSM', 1.000000, 'ENABLED', '0', 60, '面料克重单位', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (110007, 1, 'KG', '千克', 'Kilogram', 'WEIGHT', 3, 'HALF_UP', 'KG', 1.000000, 'ENABLED', '0', 70, '重量单位', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00')
ON CONFLICT (tenant_id, unit_code) WHERE del_flag = '0' DO NOTHING;

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
    (130013, 1, 'REMOTE_TRACK_5CH', '轨道5通道遥控器', 'Track 5 Channel Remote', 'REMOTE', 'CURTAIN_TRACK', 'PCS', 'SUP-TRACK', 'Track Systems Ltd', 'TRK-RMT-5', NULL, 'TRK-RMT-5', '5 channel curtain track remote', 'White', NULL, 'CURTAIN_TRACK_SAMPLE remote', 'OFBIZ', 'PRODUCT:CURTAIN_TRACK_REMOTE', 'ENABLED', '0', '{"sample":"CURTAIN_TRACK_SAMPLE"}'::jsonb, 'Sample: CURTAIN_TRACK_SAMPLE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00')
ON CONFLICT (tenant_id, material_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_fabric_series (
    series_id, tenant_id, series_code, series_name_cn, series_name_en, material_type, default_thickness_unit,
    default_thickness_value, thickness_rule_enabled, max_thickness_diff, max_combined_thickness, width_rule_enabled,
    available_widths, min_width_value, max_width_value, width_unit, extra_rule_json, status, del_flag, remark,
    create_by, create_time, update_by, update_time
) VALUES
    (140001, 1, 'FS_BASIC_COATED', '基础涂层布系列', 'Basic Coated Fabric Series', 'FABRIC', 'MM', 0.380000, true, 0.080000, 0.760000, true, '[2.0, 2.5, 2.8]'::jsonb, 2.000000, 2.800000, 'M', '{"sample":"ROLLER_SHADE_BASIC"}'::jsonb, 'ENABLED', '0', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (140002, 1, 'FS_ZEBRA_DUAL', '双层柔纱布系列', 'Dual Layer Zebra Fabric Series', 'FABRIC', 'MM', 0.300000, true, 0.050000, 0.600000, true, '[2.8, 3.0]'::jsonb, 2.800000, 3.000000, 'M', '{"sample":"ZEBRA_SHADE_BASIC","doubleLayer":true}'::jsonb, 'ENABLED', '0', 'Sample: ZEBRA_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (140003, 1, 'FS_OUTDOOR_SOLAR', '户外遮阳布系列', 'Outdoor Solar Fabric Series', 'FABRIC', 'MM', 0.550000, true, 0.100000, 1.100000, true, '[3.0, 3.2]'::jsonb, 3.000000, 3.200000, 'M', '{"sample":"OUTDOOR_SHADE","uvResistance":"high"}'::jsonb, 'ENABLED', '0', 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00')
ON CONFLICT (tenant_id, series_code) WHERE del_flag = '0' DO NOTHING;

INSERT INTO pc_fabric_profile (
    profile_id, tenant_id, fabric_code, material_id, material_code, material_name_cn, series_id, series_code, series_name_cn, color_code, color_name,
    material_composition, texture_type, finish_type, width_value, width_unit, thickness_value, thickness_unit, gsm_value,
    sample_book_no, vendor_item_no, supplier_code, supplier_name, factory_model, purchase_unit_code, inventory_unit_code,
    sales_unit_code, legacy_attribute_text, legacy_source, legacy_id, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
    (150001, 1, 'FABRIC_BASIC_WHITE', 130001, 'FABRIC_BASIC_WHITE', '基础白色涂层布', 140001, 'FS_BASIC_COATED', '基础涂层布系列', 'WHITE', 'White', '100% Polyester', 'Smooth', 'PVC Coated', 2.800000, 'M', 0.380000, 'MM', 320.000000, 'SB-ROLLER-01', 'V-BW-280', 'SUP-ROLLER', 'Hangzhou Fabric Lab', 'BW-280', 'SQM', 'SQM', 'SQM', 'OFBiz sample book: SB-ROLLER-01', 'OFBIZ', 'FABRIC_PROFILE:ROLLER_SHADE_BASIC', 'ENABLED', '0', 'Sample: ROLLER_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (150002, 1, 'FABRIC_ZEBRA_IVORY', 130004, 'FABRIC_ZEBRA_IVORY', '象牙白双层柔纱布', 140002, 'FS_ZEBRA_DUAL', '双层柔纱布系列', 'IVORY', 'Ivory', 'Polyester Blend', 'Dual Layer', 'Semi-Sheer', 3.000000, 'M', 0.300000, 'MM', 185.000000, 'SB-ZEBRA-03', 'ZB-IV-300', 'SUP-ZEBRA', 'Ningbo Zebra Textile', 'ZB-IV-300', 'SQM', 'SQM', 'SQM', 'Double layer combination thickness seed', 'OFBIZ', 'FABRIC_PROFILE:ZEBRA_SHADE_BASIC', 'ENABLED', '0', 'Sample: ZEBRA_SHADE_BASIC', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00'),
    (150003, 1, 'FABRIC_OUTDOOR_CHARCOAL', 130005, 'FABRIC_OUTDOOR_CHARCOAL', '户外炭灰遮阳布', 140003, 'FS_OUTDOOR_SOLAR', '户外遮阳布系列', 'CHARCOAL', 'Charcoal', 'PVC + Polyester', 'Outdoor Basket Weave', 'UV Resistant', 3.200000, 'M', 0.550000, 'MM', 460.000000, 'SB-OUT-08', 'OD-CH-320', 'SUP-OUT', 'SolarTex Outdoor', 'OD-CH-320', 'SQM', 'SQM', 'SQM', 'Outdoor heavy gsm fabric seed', 'OFBIZ', 'FABRIC_PROFILE:OUTDOOR_SHADE', 'ENABLED', '0', 'Sample: OUTDOOR_SHADE', 'system', '2026-06-11 00:00:00+00', 'system', '2026-06-11 00:00:00+00')
ON CONFLICT (tenant_id, material_id) WHERE del_flag = '0' DO NOTHING;

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

-- =====================================================
-- 第一阶段正式菜单、按钮和字典归一化
-- 说明：
-- 1. 正式侧边栏使用三个一级菜单：基础资料、配置与价格、发布与应用。
-- 2. 菜单最多两级；页面内 tabs 只能作为局部细节，不承载正式主入口。
-- 3. 本块必须保持幂等，可重复执行到开发库。
-- =====================================================

DELETE FROM sys_role_menu WHERE menu_id IN (24201, 24202, 24203, 24204, 24205, 24206, 24207, 24208, 24209, 24210, 24220, 24301, 24302);
DELETE FROM sys_menu WHERE menu_id IN (24201, 24202, 24203, 24204, 24205, 24206, 24207, 24208, 24209, 24210, 24220, 24301, 24302);

INSERT INTO sys_menu (menu_id, tenant_id, parent_id, menu_name, i18n_key, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
    (24200, 1, 0, 'Product Master Data', 'productCenter.menu.masterData', 30, 'product-master', NULL, NULL, '1', '0', 'M', '1', '1', NULL, 'product', 'system', now(), NULL, NULL, '产品能力-基础资料'),
    (24300, 1, 0, 'Product Configuration Pricing', 'productCenter.menu.configPricing', 31, 'product-config', NULL, NULL, '1', '0', 'M', '1', '1', NULL, 'build', 'system', now(), NULL, NULL, '产品能力-配置与价格'),
    (24400, 1, 0, 'Product Release Application', 'productCenter.menu.releaseApplication', 32, 'product-release', NULL, NULL, '1', '0', 'M', '1', '1', NULL, 'deployment-unit', 'system', now(), NULL, NULL, '产品能力-发布与应用')
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
    (24206, 1, 24200, 'Units', 'productCenter.menu.units', 1, 'units', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'unit', 'system', now(), NULL, NULL, '单位管理'),
    (24204, 1, 24200, 'Config Dictionary', 'productCenter.menu.baseAttributes', 2, 'base-attributes', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'dict', 'system', now(), NULL, NULL, '配置字典'),
    (24202, 1, 24200, 'Materials', 'productCenter.menu.materials', 3, 'materials', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'inventory', 'system', now(), NULL, NULL, '物料设置'),
    (24201, 1, 24200, 'Fabric Series', 'productCenter.menu.fabricSeries', 4, 'fabric-series', 'product-center/fabric', NULL, '1', '0', 'C', '1', '1', 'product:fabric:list', 'color', 'system', now(), NULL, NULL, '面料系列'),
    (24207, 1, 24200, 'Fabric Profiles', 'productCenter.menu.fabricProfiles', 5, 'fabric-profiles', 'product-center/fabric', NULL, '1', '0', 'C', '1', '1', 'product:fabric:list', 'swatch', 'system', now(), NULL, NULL, '面料资料'),
    (24203, 1, 24200, 'Component Packs', 'productCenter.menu.components', 6, 'components', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'component', 'system', now(), NULL, NULL, '组件包'),
    (24208, 1, 24200, 'Material Attributes', 'productCenter.menu.materialAttributes', 91, 'material-attributes', 'product-center/base', NULL, '1', '0', 'C', '0', '1', 'product:material-attribute:list', 'list', 'system', now(), NULL, NULL, '物料属性，预留能力，默认不在基础资料一级菜单展示'),
    (24209, 1, 24200, 'Component Items', 'productCenter.menu.componentItems', 92, 'component-items', 'product-center/base', NULL, '1', '0', 'C', '0', '1', 'product:component-item:list', 'list', 'system', now(), NULL, NULL, '组件明细，从组件包行内入口维护'),
    (24205, 1, 24200, 'Media Assets', 'productCenter.menu.mediaAssets', 93, 'media-assets', 'product-center/assets', NULL, '1', '0', 'C', '0', '1', 'product:asset:list', 'upload', 'system', now(), NULL, NULL, '资料资产，附件台账，默认不在基础资料一级菜单展示'),
    (24210, 1, 24200, 'Media Bindings', 'productCenter.menu.mediaBindings', 94, 'media-bindings', 'product-center/assets', NULL, '1', '0', 'C', '0', '1', 'product:asset:list', 'link', 'system', now(), NULL, NULL, '资料绑定，附件关联台账，默认不在基础资料一级菜单展示'),
    (24303, 1, 24300, 'Question Groups', 'productCenter.menu.questionGroups', 3, 'question-groups', 'product-center/question-groups', NULL, '1', '0', 'C', '1', '1', 'product:template:list', 'list', 'system', now(), NULL, NULL, '问题组模板'),
    (24304, 1, 24300, 'Config Template', 'productCenter.menu.template', 4, 'template', 'product-center/template', NULL, '1', '0', 'C', '1', '1', 'product:template:list', 'form', 'system', now(), NULL, NULL, '配置模板'),
    (24305, 1, 24300, 'Pricing', 'productCenter.menu.pricing', 5, 'pricing', 'product-center/pricing', NULL, '1', '0', 'C', '1', '1', 'product:price:list', 'money', 'system', now(), NULL, NULL, '价格中心'),
    (24306, 1, 24300, 'Quote Preview', 'productCenter.menu.quotePreview', 6, 'quote-preview', 'product-center/quote-preview', NULL, '1', '0', 'C', '1', '1', 'product:price:test', 'quote', 'system', now(), NULL, NULL, '报价预览'),
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
    (24230, 1, 24204, 'Config Dictionary Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:list', '#', 'system', now(), NULL, NULL, '配置字典查询'),
    (24231, 1, 24204, 'Config Dictionary Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:add', '#', 'system', now(), NULL, NULL, '配置字典新增'),
    (24232, 1, 24204, 'Config Dictionary Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:edit', '#', 'system', now(), NULL, NULL, '配置字典编辑'),
    (24233, 1, 24204, 'Config Dictionary Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:remove', '#', 'system', now(), NULL, NULL, '配置字典删除'),
    (24234, 1, 24204, 'Config Dictionary Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:reference', '#', 'system', now(), NULL, NULL, '配置字典引用检查'),
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
    (24310, 1, 24301, 'Model Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:model:list', '#', 'system', now(), NULL, NULL, '产品模型查询'),
    (24311, 1, 24301, 'Model Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:model:add', '#', 'system', now(), NULL, NULL, '产品模型新增'),
    (24312, 1, 24301, 'Model Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:model:edit', '#', 'system', now(), NULL, NULL, '产品模型编辑'),
    (24313, 1, 24301, 'Model Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:model:remove', '#', 'system', now(), NULL, NULL, '产品模型删除'),
    (24320, 1, 24302, 'Variant Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:model:list', '#', 'system', now(), NULL, NULL, '销售变体查询'),
    (24321, 1, 24302, 'Variant Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:model:add', '#', 'system', now(), NULL, NULL, '销售变体新增'),
    (24322, 1, 24302, 'Variant Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:model:edit', '#', 'system', now(), NULL, NULL, '销售变体编辑'),
    (24323, 1, 24302, 'Variant Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:model:remove', '#', 'system', now(), NULL, NULL, '销售变体删除'),
    (24330, 1, 24303, 'Question Group Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:list', '#', 'system', now(), NULL, NULL, '问题组查询'),
    (24331, 1, 24303, 'Question Group Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), NULL, NULL, '问题组新增'),
    (24332, 1, 24303, 'Question Group Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), NULL, NULL, '问题组编辑'),
    (24333, 1, 24303, 'Question Group Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), NULL, NULL, '问题组删除'),
    (24340, 1, 24304, 'Template Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:list', '#', 'system', now(), NULL, NULL, '配置模板查询'),
    (24341, 1, 24304, 'Template Edit', 'common.edit', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), NULL, NULL, '配置模板维护'),
    (24342, 1, 24304, 'Template Rule', 'productCenter.template.rule', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:rule', '#', 'system', now(), NULL, NULL, '配置规则维护'),
    (24343, 1, 24304, 'Template Evaluate', 'productCenter.template.evaluate', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:template:test', '#', 'system', now(), NULL, NULL, '配置求值'),
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
ON CONFLICT (role_id, menu_id) DO NOTHING;

DELETE FROM sys_dict_data WHERE dict_type = 'product_unit';
DELETE FROM sys_dict_type WHERE dict_type = 'product_unit';
DELETE FROM sys_dict_data
WHERE dict_type = 'product_business_type'
  AND dict_value IN ('FINISHED', 'CUSTOM', 'RAW', 'PURCHASE', 'SERVICE');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES
    (242001, 'Product material type', 'product_material_type', '1', 'system', now(), '产品能力物料类型'),
    (242002, 'Product component type', 'product_component_type', '1', 'system', now(), '产品能力辅材/组件类型'),
    (242003, 'Product business type', 'product_business_type', '1', 'system', now(), '产品能力业务类型')
ON CONFLICT (dict_type) DO UPDATE
SET dict_name = EXCLUDED.dict_name,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now(),
    remark = EXCLUDED.remark;

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time, remark)
VALUES
    (242100, 1, 'Fabric', 'FABRIC', 'product_material_type', 'primary', 'Y', '1', 'system', now(), '面料'),
    (242101, 2, 'Color', 'COLOR', 'product_material_type', 'primary', 'N', '1', 'system', now(), '颜色/色卡'),
    (242102, 3, 'Motor', 'MOTOR', 'product_material_type', 'success', 'N', '1', 'system', now(), '电机'),
    (242103, 4, 'Remote', 'REMOTE', 'product_material_type', 'success', 'N', '1', 'system', now(), '遥控器'),
    (242104, 5, 'Hub', 'HUB', 'product_material_type', 'success', 'N', '1', 'system', now(), '网关'),
    (242105, 6, 'Solar Panel', 'SOLAR_PANEL', 'product_material_type', 'warning', 'N', '1', 'system', now(), '太阳能板'),
    (242106, 7, 'Profile', 'PROFILE', 'product_material_type', 'info', 'N', '1', 'system', now(), '型材'),
    (242107, 8, 'Bottom Rail', 'BOTTOM_RAIL', 'product_material_type', 'info', 'N', '1', 'system', now(), '下杆'),
    (242108, 9, 'Accessory', 'ACCESSORY', 'product_material_type', 'info', 'N', '1', 'system', now(), '辅材/配件'),
    (242109, 10, 'Packaging', 'PACKAGING', 'product_material_type', 'info', 'N', '1', 'system', now(), '包装件'),
    (242110, 11, 'Service', 'SERVICE', 'product_material_type', 'info', 'N', '1', 'system', now(), '服务项'),
    (242111, 12, 'Finished Good', 'FINISHED_GOOD', 'product_material_type', 'success', 'N', '1', 'system', now(), '标准成品'),
    (242112, 13, 'Hardware', 'HARDWARE', 'product_material_type', 'info', 'N', '1', 'system', now(), '五金/辅材'),
    (242113, 14, 'Track', 'TRACK', 'product_material_type', 'info', 'N', '1', 'system', now(), '轨道'),
    (242114, 15, 'Package', 'PACKAGE', 'product_component_type', 'primary', 'Y', '1', 'system', now(), '组件包'),
    (242120, 1, 'Motor', 'MOTOR', 'product_component_type', 'success', 'N', '1', 'system', now(), '电机'),
    (242121, 2, 'Remote', 'REMOTE', 'product_component_type', 'success', 'N', '1', 'system', now(), '遥控器'),
    (242122, 3, 'Hub', 'HUB', 'product_component_type', 'success', 'N', '1', 'system', now(), '网关'),
    (242123, 4, 'Solar Panel', 'SOLAR_PANEL', 'product_component_type', 'warning', 'N', '1', 'system', now(), '太阳能板'),
    (242124, 5, 'Bottom Rail', 'BOTTOM_RAIL', 'product_component_type', 'info', 'N', '1', 'system', now(), '下杆'),
    (242125, 6, 'Bracket', 'BRACKET', 'product_component_type', 'info', 'N', '1', 'system', now(), '支架'),
    (242126, 7, 'Chain', 'CHAIN', 'product_component_type', 'info', 'N', '1', 'system', now(), '链条'),
    (242127, 8, 'Tube', 'TUBE', 'product_component_type', 'info', 'N', '1', 'system', now(), '卷管'),
    (242128, 9, 'Profile', 'PROFILE', 'product_component_type', 'info', 'N', '1', 'system', now(), '型材'),
    (242129, 10, 'Fabric', 'FABRIC', 'product_component_type', 'primary', 'N', '1', 'system', now(), '面料'),
    (242130, 11, 'Accessory', 'ACCESSORY', 'product_component_type', 'info', 'N', '1', 'system', now(), '配件'),
    (242131, 12, 'Packaging', 'PACKAGING', 'product_component_type', 'info', 'N', '1', 'system', now(), '包装件'),
    (242132, 13, 'Sample', 'SAMPLE', 'product_component_type', 'info', 'N', '1', 'system', now(), '样品'),
    (242140, 1, 'Roller Shade', 'ROLLER_SHADE', 'product_business_type', 'primary', 'Y', '1', 'system', now(), '卷帘'),
    (242141, 2, 'Zebra Shade', 'ZEBRA_SHADE', 'product_business_type', 'primary', 'N', '1', 'system', now(), '斑马帘/柔纱帘'),
    (242142, 3, 'Outdoor Shade', 'OUTDOOR_SHADE', 'product_business_type', 'success', 'N', '1', 'system', now(), '户外遮阳'),
    (242143, 4, 'Curtain Track', 'CURTAIN_TRACK', 'product_business_type', 'info', 'N', '1', 'system', now(), '轨道窗帘')
ON CONFLICT (dict_code) DO UPDATE
SET dict_sort = EXCLUDED.dict_sort,
    dict_label = EXCLUDED.dict_label,
    dict_value = EXCLUDED.dict_value,
    dict_type = EXCLUDED.dict_type,
    list_class = EXCLUDED.list_class,
    is_default = EXCLUDED.is_default,
    status = EXCLUDED.status,
    update_by = 'system',
    update_time = now(),
    remark = EXCLUDED.remark;
