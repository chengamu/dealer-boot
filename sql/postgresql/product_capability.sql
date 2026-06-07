-- 共享产品能力中心 Batch 1 PostgreSQL 草案。
-- 本文件用于开发评审和初始化草案，不会自动执行。

CREATE TABLE IF NOT EXISTS pc_category (
    category_id bigint PRIMARY KEY,
    parent_id bigint,
    category_code varchar(80),
    category_name_cn varchar(200),
    category_name_en varchar(200),
    category_level integer,
    category_path varchar(100),
    status varchar(80),
    del_flag varchar(80),
    sort_order integer,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_category IS '产品分类表';
COMMENT ON COLUMN pc_category.category_id IS '产品分类ID';
COMMENT ON COLUMN pc_category.parent_id IS '父分类ID';
COMMENT ON COLUMN pc_category.category_code IS '产品分类编码';
COMMENT ON COLUMN pc_category.category_name_cn IS '产品分类中文名称';
COMMENT ON COLUMN pc_category.category_name_en IS '产品分类英文名称';
COMMENT ON COLUMN pc_category.category_level IS '分类层级';
COMMENT ON COLUMN pc_category.category_path IS '分类路径编码';
COMMENT ON COLUMN pc_category.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_category.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_category.sort_order IS '排序';
COMMENT ON COLUMN pc_category.remark IS '备注';
COMMENT ON COLUMN pc_category.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_category.create_by IS '创建者';
COMMENT ON COLUMN pc_category.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_category.update_by IS '更新者';
COMMENT ON COLUMN pc_category.update_time IS '更新时间，UTC timestamptz';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_category_category_code_active ON pc_category (category_code) WHERE COALESCE(del_flag, '0') = '0';
CREATE INDEX IF NOT EXISTS idx_pc_category_status ON pc_category (status);

CREATE TABLE IF NOT EXISTS pc_material (
    material_id bigint PRIMARY KEY,
    material_code varchar(80),
    material_name_cn varchar(200),
    material_name_en varchar(200),
    material_type varchar(80),
    business_type varchar(80),
    unit_code varchar(80),
    supplier_name varchar(200),
    status varchar(80),
    del_flag varchar(80),
    attribute_json jsonb,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_material IS '产品物料表';
COMMENT ON COLUMN pc_material.material_id IS '产品物料ID';
COMMENT ON COLUMN pc_material.material_code IS '物料编码';
COMMENT ON COLUMN pc_material.material_name_cn IS '物料中文名称';
COMMENT ON COLUMN pc_material.material_name_en IS '物料英文名称';
COMMENT ON COLUMN pc_material.material_type IS '物料类型';
COMMENT ON COLUMN pc_material.business_type IS '业务口径类型';
COMMENT ON COLUMN pc_material.unit_code IS '单位编码';
COMMENT ON COLUMN pc_material.supplier_name IS '供应商名称';
COMMENT ON COLUMN pc_material.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_material.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_material.attribute_json IS '物料属性JSON';
COMMENT ON COLUMN pc_material.remark IS '备注';
COMMENT ON COLUMN pc_material.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_material.create_by IS '创建者';
COMMENT ON COLUMN pc_material.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_material.update_by IS '更新者';
COMMENT ON COLUMN pc_material.update_time IS '更新时间，UTC timestamptz';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_material_material_code_active ON pc_material (material_code) WHERE COALESCE(del_flag, '0') = '0';
CREATE INDEX IF NOT EXISTS idx_pc_material_status ON pc_material (status);

CREATE TABLE IF NOT EXISTS pc_component (
    component_id bigint PRIMARY KEY,
    component_code varchar(80),
    component_name_cn varchar(200),
    component_name_en varchar(200),
    component_type varchar(80),
    material_id bigint,
    material_code varchar(80),
    material_name_cn varchar(200),
    material_name_en varchar(200),
    default_qty numeric(18,6),
    qty_mode varchar(100),
    unit_code varchar(80),
    status varchar(80),
    del_flag varchar(80),
    scope_json jsonb,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_component IS '产品组件表';
COMMENT ON COLUMN pc_component.component_id IS '产品组件ID';
COMMENT ON COLUMN pc_component.component_code IS '组件编码';
COMMENT ON COLUMN pc_component.component_name_cn IS '组件中文名称';
COMMENT ON COLUMN pc_component.component_name_en IS '组件英文名称';
COMMENT ON COLUMN pc_component.component_type IS '组件类型';
COMMENT ON COLUMN pc_component.material_id IS '关联物料ID';
COMMENT ON COLUMN pc_component.material_code IS '关联物料编码快照';
COMMENT ON COLUMN pc_component.material_name_cn IS '关联物料中文名称快照';
COMMENT ON COLUMN pc_component.material_name_en IS '关联物料英文名称快照';
COMMENT ON COLUMN pc_component.default_qty IS '默认数量';
COMMENT ON COLUMN pc_component.qty_mode IS '数量模式';
COMMENT ON COLUMN pc_component.unit_code IS '单位编码';
COMMENT ON COLUMN pc_component.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_component.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_component.scope_json IS '适用范围JSON';
COMMENT ON COLUMN pc_component.remark IS '备注';
COMMENT ON COLUMN pc_component.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_component.create_by IS '创建者';
COMMENT ON COLUMN pc_component.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_component.update_by IS '更新者';
COMMENT ON COLUMN pc_component.update_time IS '更新时间，UTC timestamptz';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_component_component_code_active ON pc_component (component_code) WHERE COALESCE(del_flag, '0') = '0';
CREATE INDEX IF NOT EXISTS idx_pc_component_status ON pc_component (status);

CREATE TABLE IF NOT EXISTS pc_media_asset (
    asset_id bigint PRIMARY KEY,
    asset_code varchar(80),
    asset_name_cn varchar(200),
    asset_name_en varchar(200),
    asset_type varchar(80),
    usage_type varchar(80),
    language_code varchar(80),
    visibility varchar(100),
    oss_id bigint,
    url varchar(500),
    alt_text varchar(500),
    version_no integer,
    status varchar(80),
    del_flag varchar(80),
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_media_asset IS '产品资料资产表';
COMMENT ON COLUMN pc_media_asset.asset_id IS '产品资料资产ID';
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
COMMENT ON COLUMN pc_media_asset.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_media_asset.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_media_asset.remark IS '备注';
COMMENT ON COLUMN pc_media_asset.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_media_asset.create_by IS '创建者';
COMMENT ON COLUMN pc_media_asset.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_media_asset.update_by IS '更新者';
COMMENT ON COLUMN pc_media_asset.update_time IS '更新时间，UTC timestamptz';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_media_asset_asset_code_active ON pc_media_asset (asset_code) WHERE COALESCE(del_flag, '0') = '0';
CREATE INDEX IF NOT EXISTS idx_pc_media_asset_status ON pc_media_asset (status);

CREATE TABLE IF NOT EXISTS pc_media_binding (
    binding_id bigint PRIMARY KEY,
    asset_id bigint,
    asset_code varchar(80),
    target_type varchar(80),
    target_id bigint,
    target_code varchar(80),
    usage_type varchar(80),
    visibility varchar(100),
    language_code varchar(80),
    required_for_publish varchar(100),
    sort_order integer,
    status varchar(80),
    del_flag varchar(80),
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_media_binding IS '产品资料绑定表';
COMMENT ON COLUMN pc_media_binding.binding_id IS '产品资料绑定ID';
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
COMMENT ON COLUMN pc_media_binding.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_media_binding.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_media_binding.remark IS '备注';
COMMENT ON COLUMN pc_media_binding.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_media_binding.create_by IS '创建者';
COMMENT ON COLUMN pc_media_binding.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_media_binding.update_by IS '更新者';
COMMENT ON COLUMN pc_media_binding.update_time IS '更新时间，UTC timestamptz';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_media_binding_asset_code_active ON pc_media_binding (asset_code) WHERE COALESCE(del_flag, '0') = '0';
CREATE INDEX IF NOT EXISTS idx_pc_media_binding_status ON pc_media_binding (status);

CREATE TABLE IF NOT EXISTS pc_product_model (
    model_id bigint PRIMARY KEY,
    model_code varchar(80),
    model_name_cn varchar(200),
    model_name_en varchar(200),
    category_id bigint,
    category_code varchar(80),
    category_name_cn varchar(200),
    product_family varchar(100),
    structure_type varchar(80),
    sales_mode varchar(100),
    product_nature varchar(100),
    unit_code varchar(80),
    status varchar(80),
    del_flag varchar(80),
    publish_status varchar(80),
    owner_user_id bigint,
    asset_owner_user_id bigint,
    price_owner_user_id bigint,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_product_model IS '产品模型表';
COMMENT ON COLUMN pc_product_model.model_id IS '产品模型ID';
COMMENT ON COLUMN pc_product_model.model_code IS '产品模型编码';
COMMENT ON COLUMN pc_product_model.model_name_cn IS '产品模型中文名称';
COMMENT ON COLUMN pc_product_model.model_name_en IS '产品模型英文名称';
COMMENT ON COLUMN pc_product_model.category_id IS '产品分类ID';
COMMENT ON COLUMN pc_product_model.category_code IS '产品分类编码快照';
COMMENT ON COLUMN pc_product_model.category_name_cn IS '产品分类中文名称快照';
COMMENT ON COLUMN pc_product_model.product_family IS '产品族';
COMMENT ON COLUMN pc_product_model.structure_type IS '结构类型';
COMMENT ON COLUMN pc_product_model.sales_mode IS '销售模式';
COMMENT ON COLUMN pc_product_model.product_nature IS '产品性质';
COMMENT ON COLUMN pc_product_model.unit_code IS '单位编码';
COMMENT ON COLUMN pc_product_model.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_product_model.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_product_model.publish_status IS '发布状态';
COMMENT ON COLUMN pc_product_model.owner_user_id IS '产品负责人用户ID';
COMMENT ON COLUMN pc_product_model.asset_owner_user_id IS '资料负责人用户ID';
COMMENT ON COLUMN pc_product_model.price_owner_user_id IS '价格负责人用户ID';
COMMENT ON COLUMN pc_product_model.remark IS '备注';
COMMENT ON COLUMN pc_product_model.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_product_model.create_by IS '创建者';
COMMENT ON COLUMN pc_product_model.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_product_model.update_by IS '更新者';
COMMENT ON COLUMN pc_product_model.update_time IS '更新时间，UTC timestamptz';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_product_model_model_code_active ON pc_product_model (model_code) WHERE COALESCE(del_flag, '0') = '0';
CREATE INDEX IF NOT EXISTS idx_pc_product_model_status ON pc_product_model (status);

CREATE TABLE IF NOT EXISTS pc_sales_variant (
    variant_id bigint PRIMARY KEY,
    model_id bigint,
    variant_code varchar(80),
    variant_name_cn varchar(200),
    variant_name_en varchar(200),
    dimension_json jsonb,
    market_code varchar(80),
    control_method varchar(100),
    grade varchar(100),
    package_type varchar(80),
    status varchar(80),
    del_flag varchar(80),
    sort_order integer,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_sales_variant IS '产品销售变体表';
COMMENT ON COLUMN pc_sales_variant.variant_id IS '产品销售变体ID';
COMMENT ON COLUMN pc_sales_variant.model_id IS '产品模型ID';
COMMENT ON COLUMN pc_sales_variant.variant_code IS '销售变体编码';
COMMENT ON COLUMN pc_sales_variant.variant_name_cn IS '销售变体中文名称';
COMMENT ON COLUMN pc_sales_variant.variant_name_en IS '销售变体英文名称';
COMMENT ON COLUMN pc_sales_variant.dimension_json IS '销售维度JSON';
COMMENT ON COLUMN pc_sales_variant.market_code IS '市场编码';
COMMENT ON COLUMN pc_sales_variant.control_method IS '控制方式';
COMMENT ON COLUMN pc_sales_variant.grade IS '等级';
COMMENT ON COLUMN pc_sales_variant.package_type IS '包装类型';
COMMENT ON COLUMN pc_sales_variant.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_sales_variant.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_sales_variant.sort_order IS '排序';
COMMENT ON COLUMN pc_sales_variant.remark IS '备注';
COMMENT ON COLUMN pc_sales_variant.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_sales_variant.create_by IS '创建者';
COMMENT ON COLUMN pc_sales_variant.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_sales_variant.update_by IS '更新者';
COMMENT ON COLUMN pc_sales_variant.update_time IS '更新时间，UTC timestamptz';
CREATE UNIQUE INDEX IF NOT EXISTS uk_pc_sales_variant_variant_code_active ON pc_sales_variant (variant_code) WHERE COALESCE(del_flag, '0') = '0';
CREATE INDEX IF NOT EXISTS idx_pc_sales_variant_status ON pc_sales_variant (status);

-- 产品能力 Batch 1 菜单和权限草案。
-- 菜单最多两级：一级“产品能力”，二级直接进入已落地页面。
INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (24000, 1, 'Product Capability', 'productCenter.menu.title', 0, 30, 'product-center', NULL, '1', '0', 'M', '1', '1', NULL, 'component', 'system', now(), 'Shared product capability center'),
    (24001, 1, 'Workbench', 'productCenter.menu.workbench', 24000, 1, 'workbench', 'product-center/workbench', '1', '0', 'C', '1', '1', 'product:center:view', 'dashboard', 'system', now(), 'Product capability workbench'),
    (24002, 1, 'Base Info', 'productCenter.menu.base', 24000, 2, 'base', 'product-center/base', '1', '0', 'C', '0', '1', 'product:base:list', 'dict', 'system', now(), 'Legacy combined base information entry'),
    (24003, 1, 'Product Models Legacy', 'productCenter.menu.model', 24000, 3, 'model', 'product-center/model', '1', '0', 'C', '0', '1', 'product:model:list', 'tree-table', 'system', now(), 'Legacy combined product models entry'),
    (24004, 1, 'Components / Assets', 'productCenter.menu.assets', 24000, 4, 'assets', 'product-center/assets', '1', '0', 'C', '0', '1', 'product:asset:list', 'upload', 'system', now(), 'Legacy combined components and media assets entry'),
    (24005, 1, 'Config Template', 'productCenter.menu.template', 24000, 9, 'template', 'product-center/template', '1', '0', 'C', '1', '1', 'product:template:list', 'form', 'system', now(), 'Config template workbench'),
    (24010, 1, 'Workbench View', 'productCenter.menu.workbench', 24001, 1, '#', '', '1', '0', 'F', '1', '1', 'product:center:view', '#', 'system', now(), ''),
    (24020, 1, 'Base List', 'productCenter.menu.base', 24002, 1, '#', '', '1', '0', 'F', '1', '1', 'product:base:list', '#', 'system', now(), ''),
    (24021, 1, 'Base Add', 'common.add', 24002, 2, '#', '', '1', '0', 'F', '1', '1', 'product:base:add', '#', 'system', now(), ''),
    (24022, 1, 'Base Edit', 'common.edit', 24002, 3, '#', '', '1', '0', 'F', '1', '1', 'product:base:edit', '#', 'system', now(), ''),
    (24023, 1, 'Base Remove', 'common.delete', 24002, 4, '#', '', '1', '0', 'F', '1', '1', 'product:base:remove', '#', 'system', now(), ''),
    (24024, 1, 'Base Reference', 'productCenter.common.references', 24002, 5, '#', '', '1', '0', 'F', '1', '1', 'product:base:reference', '#', 'system', now(), ''),
    (24030, 1, 'Model List', 'productCenter.menu.model', 24003, 1, '#', '', '1', '0', 'F', '1', '1', 'product:model:list', '#', 'system', now(), ''),
    (24031, 1, 'Model Add', 'common.add', 24003, 2, '#', '', '1', '0', 'F', '1', '1', 'product:model:add', '#', 'system', now(), ''),
    (24032, 1, 'Model Edit', 'common.edit', 24003, 3, '#', '', '1', '0', 'F', '1', '1', 'product:model:edit', '#', 'system', now(), ''),
    (24033, 1, 'Model Remove', 'common.delete', 24003, 4, '#', '', '1', '0', 'F', '1', '1', 'product:model:remove', '#', 'system', now(), ''),
    (24040, 1, 'Asset List', 'productCenter.menu.assets', 24004, 1, '#', '', '1', '0', 'F', '1', '1', 'product:asset:list', '#', 'system', now(), ''),
    (24041, 1, 'Asset Upload', 'common.upload', 24004, 2, '#', '', '1', '0', 'F', '1', '1', 'product:asset:upload', '#', 'system', now(), ''),
    (24042, 1, 'Asset Bind', 'productCenter.binding.title', 24004, 3, '#', '', '1', '0', 'F', '1', '1', 'product:asset:bind', '#', 'system', now(), ''),
    (24043, 1, 'Asset Reference', 'productCenter.common.references', 24004, 4, '#', '', '1', '0', 'F', '1', '1', 'product:asset:reference', '#', 'system', now(), ''),
    (24050, 1, 'Template List', 'productCenter.menu.template', 24005, 1, '#', '', '1', '0', 'F', '1', '1', 'product:template:list', '#', 'system', now(), ''),
    (24051, 1, 'Template Edit', 'common.edit', 24005, 2, '#', '', '1', '0', 'F', '1', '1', 'product:template:edit', '#', 'system', now(), ''),
    (24052, 1, 'Template Rule', 'productCenter.template.rule', 24005, 3, '#', '', '1', '0', 'F', '1', '1', 'product:template:rule', '#', 'system', now(), ''),
    (24053, 1, 'Template Test', 'productCenter.template.evaluate', 24005, 4, '#', '', '1', '0', 'F', '1', '1', 'product:template:test', '#', 'system', now(), '')
ON CONFLICT (menu_id) DO UPDATE
SET tenant_id = EXCLUDED.tenant_id,
    menu_name = EXCLUDED.menu_name,
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

INSERT INTO sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (24100, 1, 'Product Categories', 'productCenter.menu.categories', 24000, 2, 'categories', 'product-center/base', '1', '0', 'C', '1', '1', 'product:base:list', 'tree-table', 'system', now(), 'Product category master data'),
    (24101, 1, 'Materials', 'productCenter.menu.materials', 24000, 3, 'materials', 'product-center/base', '1', '0', 'C', '1', '1', 'product:base:list', 'dict', 'system', now(), 'Product material master data'),
    (24102, 1, 'Components', 'productCenter.menu.components', 24000, 4, 'components', 'product-center/base', '1', '0', 'C', '1', '1', 'product:base:list', 'component', 'system', now(), 'Product component library'),
    (24103, 1, 'Media Assets', 'productCenter.menu.mediaAssets', 24000, 5, 'media-assets', 'product-center/assets', '1', '0', 'C', '1', '1', 'product:asset:list', 'upload', 'system', now(), 'Product media asset library'),
    (24104, 1, 'Media Bindings', 'productCenter.menu.mediaBindings', 24000, 6, 'media-bindings', 'product-center/assets', '1', '0', 'C', '1', '1', 'product:asset:list', 'link', 'system', now(), 'Product media binding management'),
    (24105, 1, 'Product Models', 'productCenter.menu.models', 24000, 7, 'models', 'product-center/model', '1', '0', 'C', '1', '1', 'product:model:list', 'tree-table', 'system', now(), 'Product model master data'),
    (24106, 1, 'Sales Variants', 'productCenter.menu.salesVariants', 24000, 8, 'sales-variants', 'product-center/model', '1', '0', 'C', '1', '1', 'product:model:list', 'list', 'system', now(), 'Product sales variant management')
ON CONFLICT (menu_id) DO UPDATE
SET tenant_id = EXCLUDED.tenant_id,
    menu_name = EXCLUDED.menu_name,
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
SELECT 1, menu_id, 1 FROM sys_menu
WHERE menu_id BETWEEN 24000 AND 24199
ON CONFLICT DO NOTHING;

-- 产品配置模板和规则 Batch 2 草案。

CREATE TABLE IF NOT EXISTS pc_config_template (
    template_id bigint PRIMARY KEY,
    template_code varchar(100),
    template_name_cn varchar(200),
    template_name_en varchar(200),
    product_model_id bigint,
    product_model_code varchar(100),
    current_version_id bigint,
    current_version_no varchar(100),
    published_version_id bigint,
    published_version_no varchar(100),
    biz_status varchar(100),
    status varchar(100),
    del_flag varchar(100),
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_config_template IS '配置模板表';
COMMENT ON COLUMN pc_config_template.template_id IS '配置模板ID';
COMMENT ON COLUMN pc_config_template.template_code IS '配置模板编码';
COMMENT ON COLUMN pc_config_template.template_name_cn IS '配置模板中文名称';
COMMENT ON COLUMN pc_config_template.template_name_en IS '配置模板英文名称';
COMMENT ON COLUMN pc_config_template.product_model_id IS '产品模型ID';
COMMENT ON COLUMN pc_config_template.product_model_code IS '产品模型编码';
COMMENT ON COLUMN pc_config_template.current_version_id IS '当前版本ID';
COMMENT ON COLUMN pc_config_template.current_version_no IS '当前版本号';
COMMENT ON COLUMN pc_config_template.published_version_id IS '当前已发布版本ID';
COMMENT ON COLUMN pc_config_template.published_version_no IS '当前已发布版本号';
COMMENT ON COLUMN pc_config_template.biz_status IS '业务状态：DRAFT草稿，PUBLISHED已发布，ARCHIVED已归档';
COMMENT ON COLUMN pc_config_template.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_config_template.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_config_template.remark IS '备注';
COMMENT ON COLUMN pc_config_template.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_config_template.create_by IS '创建者';
COMMENT ON COLUMN pc_config_template.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_config_template.update_by IS '更新者';
COMMENT ON COLUMN pc_config_template.update_time IS '更新时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_config_template_status ON pc_config_template (status);


CREATE TABLE IF NOT EXISTS pc_config_template_version (
    template_version_id bigint PRIMARY KEY,
    template_id bigint,
    template_code varchar(100),
    version_no varchar(100),
    version_name varchar(100),
    version_status varchar(100),
    product_model_id bigint,
    product_model_code varchar(100),
    sales_variant_id bigint,
    sales_variant_code varchar(100),
    price_plan_version_id bigint,
    price_plan_code varchar(100),
    schema_json jsonb,
    draft_hash varchar(100),
    effective_from timestamptz,
    effective_to timestamptz,
    published_package_id bigint,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_config_template_version IS '配置模板版本表';
COMMENT ON COLUMN pc_config_template_version.template_version_id IS '配置模板版本ID';
COMMENT ON COLUMN pc_config_template_version.template_id IS '配置模板ID';
COMMENT ON COLUMN pc_config_template_version.template_code IS '配置模板编码';
COMMENT ON COLUMN pc_config_template_version.version_no IS '版本号';
COMMENT ON COLUMN pc_config_template_version.version_name IS '版本名称';
COMMENT ON COLUMN pc_config_template_version.version_status IS '版本状态：DRAFT草稿，SUBMITTED已提交，PUBLISHED已发布，ARCHIVED已归档';
COMMENT ON COLUMN pc_config_template_version.product_model_id IS '产品模型ID';
COMMENT ON COLUMN pc_config_template_version.product_model_code IS '产品模型编码';
COMMENT ON COLUMN pc_config_template_version.sales_variant_id IS '默认销售变体ID';
COMMENT ON COLUMN pc_config_template_version.sales_variant_code IS '默认销售变体编码';
COMMENT ON COLUMN pc_config_template_version.price_plan_version_id IS '引用价格方案版本ID';
COMMENT ON COLUMN pc_config_template_version.price_plan_code IS '引用价格方案编码';
COMMENT ON COLUMN pc_config_template_version.schema_json IS '模板结构快照JSON';
COMMENT ON COLUMN pc_config_template_version.draft_hash IS '草稿内容哈希';
COMMENT ON COLUMN pc_config_template_version.effective_from IS '生效开始时间，UTC语义';
COMMENT ON COLUMN pc_config_template_version.effective_to IS '生效结束时间，UTC语义';
COMMENT ON COLUMN pc_config_template_version.published_package_id IS '发布后生成的产品发布包ID';
COMMENT ON COLUMN pc_config_template_version.remark IS '备注';
COMMENT ON COLUMN pc_config_template_version.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_config_template_version.create_by IS '创建者';
COMMENT ON COLUMN pc_config_template_version.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_config_template_version.update_by IS '更新者';
COMMENT ON COLUMN pc_config_template_version.update_time IS '更新时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_config_template_version_status ON pc_config_template_version (version_status);


CREATE TABLE IF NOT EXISTS pc_question_group (
    question_group_id bigint PRIMARY KEY,
    group_code varchar(100),
    group_name_cn varchar(200),
    group_name_en varchar(200),
    description_cn varchar(100),
    description_en varchar(100),
    status varchar(100),
    del_flag varchar(100),
    sort_order integer,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_question_group IS '配置问题组表';
COMMENT ON COLUMN pc_question_group.question_group_id IS '配置问题组ID';
COMMENT ON COLUMN pc_question_group.group_code IS '问题组编码';
COMMENT ON COLUMN pc_question_group.group_name_cn IS '问题组中文名称';
COMMENT ON COLUMN pc_question_group.group_name_en IS '问题组英文名称';
COMMENT ON COLUMN pc_question_group.description_cn IS '问题组中文说明';
COMMENT ON COLUMN pc_question_group.description_en IS '问题组英文说明';
COMMENT ON COLUMN pc_question_group.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_question_group.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_question_group.sort_order IS '排序';
COMMENT ON COLUMN pc_question_group.remark IS '备注';
COMMENT ON COLUMN pc_question_group.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_question_group.create_by IS '创建者';
COMMENT ON COLUMN pc_question_group.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_question_group.update_by IS '更新者';
COMMENT ON COLUMN pc_question_group.update_time IS '更新时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_question_group_status ON pc_question_group (status);


CREATE TABLE IF NOT EXISTS pc_config_question (
    question_id bigint PRIMARY KEY,
    template_version_id bigint,
    question_group_id bigint,
    question_code varchar(100),
    question_name_cn varchar(200),
    question_name_en varchar(200),
    help_text_cn varchar(1000),
    help_text_en varchar(1000),
    input_type varchar(100),
    required_flag varchar(100),
    customer_visible varchar(100),
    default_value varchar(100),
    validation_json jsonb,
    display_rule_json jsonb,
    status varchar(100),
    sort_order integer,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_config_question IS '配置问题表';
COMMENT ON COLUMN pc_config_question.question_id IS '配置问题ID';
COMMENT ON COLUMN pc_config_question.template_version_id IS '配置模板版本ID';
COMMENT ON COLUMN pc_config_question.question_group_id IS '配置问题组ID';
COMMENT ON COLUMN pc_config_question.question_code IS '配置问题编码';
COMMENT ON COLUMN pc_config_question.question_name_cn IS '配置问题中文名称';
COMMENT ON COLUMN pc_config_question.question_name_en IS '配置问题英文名称';
COMMENT ON COLUMN pc_config_question.help_text_cn IS '中文帮助说明';
COMMENT ON COLUMN pc_config_question.help_text_en IS '英文帮助说明';
COMMENT ON COLUMN pc_config_question.input_type IS '输入类型';
COMMENT ON COLUMN pc_config_question.required_flag IS '是否必填：1是，0否';
COMMENT ON COLUMN pc_config_question.customer_visible IS '是否客户可见：1是，0否';
COMMENT ON COLUMN pc_config_question.default_value IS '默认值';
COMMENT ON COLUMN pc_config_question.validation_json IS '校验规则JSON';
COMMENT ON COLUMN pc_config_question.display_rule_json IS '展示规则摘要JSON';
COMMENT ON COLUMN pc_config_question.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_config_question.sort_order IS '排序';
COMMENT ON COLUMN pc_config_question.remark IS '备注';
COMMENT ON COLUMN pc_config_question.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_config_question.create_by IS '创建者';
COMMENT ON COLUMN pc_config_question.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_config_question.update_by IS '更新者';
COMMENT ON COLUMN pc_config_question.update_time IS '更新时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_config_question_status ON pc_config_question (status);


CREATE TABLE IF NOT EXISTS pc_config_option (
    option_id bigint PRIMARY KEY,
    question_id bigint,
    template_version_id bigint,
    option_code varchar(100),
    option_name_cn varchar(200),
    option_name_en varchar(200),
    option_value varchar(100),
    help_text_cn varchar(1000),
    help_text_en varchar(1000),
    component_json jsonb,
    media_json jsonb,
    price_impact_json jsonb,
    status varchar(100),
    sort_order integer,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_config_option IS '配置答案选项表';
COMMENT ON COLUMN pc_config_option.option_id IS '配置答案选项ID';
COMMENT ON COLUMN pc_config_option.question_id IS '配置问题ID';
COMMENT ON COLUMN pc_config_option.template_version_id IS '配置模板版本ID';
COMMENT ON COLUMN pc_config_option.option_code IS '答案选项编码';
COMMENT ON COLUMN pc_config_option.option_name_cn IS '答案选项中文名称';
COMMENT ON COLUMN pc_config_option.option_name_en IS '答案选项英文名称';
COMMENT ON COLUMN pc_config_option.option_value IS '答案选项值';
COMMENT ON COLUMN pc_config_option.help_text_cn IS '答案中文说明';
COMMENT ON COLUMN pc_config_option.help_text_en IS '答案英文说明';
COMMENT ON COLUMN pc_config_option.component_json IS '答案默认带出组件摘要JSON';
COMMENT ON COLUMN pc_config_option.media_json IS '答案绑定资料摘要JSON';
COMMENT ON COLUMN pc_config_option.price_impact_json IS '答案价格影响摘要JSON';
COMMENT ON COLUMN pc_config_option.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_config_option.sort_order IS '排序';
COMMENT ON COLUMN pc_config_option.remark IS '备注';
COMMENT ON COLUMN pc_config_option.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_config_option.create_by IS '创建者';
COMMENT ON COLUMN pc_config_option.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_config_option.update_by IS '更新者';
COMMENT ON COLUMN pc_config_option.update_time IS '更新时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_config_option_status ON pc_config_option (status);


CREATE TABLE IF NOT EXISTS pc_config_rule (
    rule_id bigint PRIMARY KEY,
    template_version_id bigint,
    rule_code varchar(100),
    rule_name_cn varchar(200),
    rule_name_en varchar(200),
    rule_type varchar(100),
    priority integer,
    condition_json jsonb,
    action_json jsonb,
    error_message_cn varchar(100),
    error_message_en varchar(100),
    status varchar(100),
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_config_rule IS '配置规则表';
COMMENT ON COLUMN pc_config_rule.rule_id IS '配置规则ID';
COMMENT ON COLUMN pc_config_rule.template_version_id IS '配置模板版本ID';
COMMENT ON COLUMN pc_config_rule.rule_code IS '配置规则编码';
COMMENT ON COLUMN pc_config_rule.rule_name_cn IS '配置规则中文名称';
COMMENT ON COLUMN pc_config_rule.rule_name_en IS '配置规则英文名称';
COMMENT ON COLUMN pc_config_rule.rule_type IS '规则类型';
COMMENT ON COLUMN pc_config_rule.priority IS '规则优先级';
COMMENT ON COLUMN pc_config_rule.condition_json IS '规则条件JSON';
COMMENT ON COLUMN pc_config_rule.action_json IS '规则动作JSON';
COMMENT ON COLUMN pc_config_rule.error_message_cn IS '中文错误提示';
COMMENT ON COLUMN pc_config_rule.error_message_en IS '英文错误提示';
COMMENT ON COLUMN pc_config_rule.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_config_rule.remark IS '备注';
COMMENT ON COLUMN pc_config_rule.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_config_rule.create_by IS '创建者';
COMMENT ON COLUMN pc_config_rule.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_config_rule.update_by IS '更新者';
COMMENT ON COLUMN pc_config_rule.update_time IS '更新时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_config_rule_status ON pc_config_rule (status);


-- =====================================================
-- Batch 2 价格中心
-- 说明：共享产品能力中心价格表为平台共享能力，不加租户字段；所有时间字段按 UTC 语义存储。
-- =====================================================

CREATE TABLE IF NOT EXISTS pc_price_plan (
    price_plan_id bigint PRIMARY KEY,
    price_plan_code varchar(100),
    price_plan_name_cn varchar(200),
    price_plan_name_en varchar(200),
    product_model_id bigint,
    product_model_code varchar(100),
    sales_variant_id bigint,
    sales_variant_code varchar(100),
    currency_code varchar(100),
    pricing_mode varchar(100),
    current_version_id bigint,
    current_version_no varchar(100),
    published_version_id bigint,
    published_version_no varchar(100),
    biz_status varchar(100),
    status varchar(100),
    del_flag varchar(100),
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_price_plan IS '价格方案表';
COMMENT ON COLUMN pc_price_plan.price_plan_id IS '价格方案ID';
COMMENT ON COLUMN pc_price_plan.price_plan_code IS '价格方案编码';
COMMENT ON COLUMN pc_price_plan.price_plan_name_cn IS '价格方案中文名称';
COMMENT ON COLUMN pc_price_plan.price_plan_name_en IS '价格方案英文名称';
COMMENT ON COLUMN pc_price_plan.product_model_id IS '产品模型ID';
COMMENT ON COLUMN pc_price_plan.product_model_code IS '产品模型编码';
COMMENT ON COLUMN pc_price_plan.sales_variant_id IS '销售变体ID';
COMMENT ON COLUMN pc_price_plan.sales_variant_code IS '销售变体编码';
COMMENT ON COLUMN pc_price_plan.currency_code IS '币种编码';
COMMENT ON COLUMN pc_price_plan.pricing_mode IS '计价模式：FIXED固定价，MATRIX矩阵价，AREA面积价，OPTION_ADDER选项加价';
COMMENT ON COLUMN pc_price_plan.current_version_id IS '当前版本ID';
COMMENT ON COLUMN pc_price_plan.current_version_no IS '当前版本号';
COMMENT ON COLUMN pc_price_plan.published_version_id IS '当前已发布版本ID';
COMMENT ON COLUMN pc_price_plan.published_version_no IS '当前已发布版本号';
COMMENT ON COLUMN pc_price_plan.biz_status IS '业务状态：DRAFT草稿，PUBLISHED已发布，ARCHIVED已归档';
COMMENT ON COLUMN pc_price_plan.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_price_plan.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_price_plan.remark IS '备注';
COMMENT ON COLUMN pc_price_plan.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_price_plan.create_by IS '创建者';
COMMENT ON COLUMN pc_price_plan.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_price_plan.update_by IS '更新者';
COMMENT ON COLUMN pc_price_plan.update_time IS '更新时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_price_plan_status ON pc_price_plan (status);


CREATE TABLE IF NOT EXISTS pc_price_plan_version (
    price_plan_version_id bigint PRIMARY KEY,
    price_plan_id bigint,
    price_plan_code varchar(100),
    version_no varchar(100),
    version_name varchar(100),
    version_status varchar(100),
    product_model_id bigint,
    product_model_code varchar(100),
    sales_variant_id bigint,
    sales_variant_code varchar(100),
    currency_code varchar(100),
    pricing_mode varchar(100),
    base_amount numeric(18,4),
    price_schema_json jsonb,
    draft_hash varchar(100),
    effective_from timestamptz,
    effective_to timestamptz,
    published_package_id bigint,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_price_plan_version IS '价格方案版本表';
COMMENT ON COLUMN pc_price_plan_version.price_plan_version_id IS '价格方案版本ID';
COMMENT ON COLUMN pc_price_plan_version.price_plan_id IS '价格方案ID';
COMMENT ON COLUMN pc_price_plan_version.price_plan_code IS '价格方案编码';
COMMENT ON COLUMN pc_price_plan_version.version_no IS '版本号';
COMMENT ON COLUMN pc_price_plan_version.version_name IS '版本名称';
COMMENT ON COLUMN pc_price_plan_version.version_status IS '版本状态：DRAFT草稿，SUBMITTED已提交，PUBLISHED已发布，ARCHIVED已归档';
COMMENT ON COLUMN pc_price_plan_version.product_model_id IS '产品模型ID';
COMMENT ON COLUMN pc_price_plan_version.product_model_code IS '产品模型编码';
COMMENT ON COLUMN pc_price_plan_version.sales_variant_id IS '销售变体ID';
COMMENT ON COLUMN pc_price_plan_version.sales_variant_code IS '销售变体编码';
COMMENT ON COLUMN pc_price_plan_version.currency_code IS '币种编码';
COMMENT ON COLUMN pc_price_plan_version.pricing_mode IS '计价模式';
COMMENT ON COLUMN pc_price_plan_version.base_amount IS '基础金额';
COMMENT ON COLUMN pc_price_plan_version.price_schema_json IS '价格结构快照JSON';
COMMENT ON COLUMN pc_price_plan_version.draft_hash IS '草稿内容哈希';
COMMENT ON COLUMN pc_price_plan_version.effective_from IS '生效开始时间，UTC语义';
COMMENT ON COLUMN pc_price_plan_version.effective_to IS '生效结束时间，UTC语义';
COMMENT ON COLUMN pc_price_plan_version.published_package_id IS '发布后生成的产品发布包ID';
COMMENT ON COLUMN pc_price_plan_version.remark IS '备注';
COMMENT ON COLUMN pc_price_plan_version.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_price_plan_version.create_by IS '创建者';
COMMENT ON COLUMN pc_price_plan_version.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_price_plan_version.update_by IS '更新者';
COMMENT ON COLUMN pc_price_plan_version.update_time IS '更新时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_price_plan_version_status ON pc_price_plan_version (version_status);


CREATE TABLE IF NOT EXISTS pc_price_rule_item (
    rule_item_id bigint PRIMARY KEY,
    price_plan_version_id bigint,
    price_plan_code varchar(100),
    item_code varchar(100),
    item_name_cn varchar(200),
    item_name_en varchar(200),
    item_type varchar(100),
    match_json jsonb,
    formula_json jsonb,
    base_amount numeric(18,4),
    unit_price numeric(18,4),
    currency_code varchar(100),
    priority integer,
    status varchar(100),
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_price_rule_item IS '价格规则项表';
COMMENT ON COLUMN pc_price_rule_item.rule_item_id IS '价格规则项ID';
COMMENT ON COLUMN pc_price_rule_item.price_plan_version_id IS '价格方案版本ID';
COMMENT ON COLUMN pc_price_rule_item.price_plan_code IS '价格方案编码';
COMMENT ON COLUMN pc_price_rule_item.item_code IS '价格规则项编码';
COMMENT ON COLUMN pc_price_rule_item.item_name_cn IS '价格规则项中文名称';
COMMENT ON COLUMN pc_price_rule_item.item_name_en IS '价格规则项英文名称';
COMMENT ON COLUMN pc_price_rule_item.item_type IS '规则项类型：BASE基础价，MATRIX矩阵价，AREA面积价，OPTION_ADDER选项加价';
COMMENT ON COLUMN pc_price_rule_item.match_json IS '匹配条件快照JSON';
COMMENT ON COLUMN pc_price_rule_item.formula_json IS '计价公式快照JSON';
COMMENT ON COLUMN pc_price_rule_item.base_amount IS '基础金额';
COMMENT ON COLUMN pc_price_rule_item.unit_price IS '单位价格';
COMMENT ON COLUMN pc_price_rule_item.currency_code IS '币种编码';
COMMENT ON COLUMN pc_price_rule_item.priority IS '优先级';
COMMENT ON COLUMN pc_price_rule_item.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_price_rule_item.remark IS '备注';
COMMENT ON COLUMN pc_price_rule_item.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_price_rule_item.create_by IS '创建者';
COMMENT ON COLUMN pc_price_rule_item.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_price_rule_item.update_by IS '更新者';
COMMENT ON COLUMN pc_price_rule_item.update_time IS '更新时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_price_rule_item_status ON pc_price_rule_item (status);

INSERT INTO sys_menu (menu_id, tenant_id, parent_id, menu_name, i18n_key, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
    (24006, 1, 24000, 'Pricing', 'productCenter.menu.pricing', 10, 'pricing', 'product-center/pricing', NULL, 1, 0, 'C', '1', '1', 'product:price:list', 'money', 'admin', now(), '', NULL, '产品能力-价格中心')
ON CONFLICT (menu_id) DO UPDATE SET tenant_id = EXCLUDED.tenant_id, parent_id = EXCLUDED.parent_id, menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key, order_num = EXCLUDED.order_num, path = EXCLUDED.path, component = EXCLUDED.component, visible = EXCLUDED.visible, status = EXCLUDED.status, perms = EXCLUDED.perms, icon = EXCLUDED.icon;

INSERT INTO sys_menu (menu_id, tenant_id, parent_id, menu_name, i18n_key, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
    (24060, 1, 24006, 'Pricing Query', 'common.search', 1, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:price:list', '#', 'admin', now(), '', NULL, '价格中心查询'),
    (24061, 1, 24006, 'Pricing Edit', 'common.edit', 2, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:price:edit', '#', 'admin', now(), '', NULL, '价格中心维护'),
    (24062, 1, 24006, 'Pricing Test', 'productCenter.price.calculate', 3, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:price:test', '#', 'admin', now(), '', NULL, '价格试算')
ON CONFLICT (menu_id) DO UPDATE SET tenant_id = EXCLUDED.tenant_id, parent_id = EXCLUDED.parent_id, menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key, order_num = EXCLUDED.order_num, visible = EXCLUDED.visible, status = EXCLUDED.status, perms = EXCLUDED.perms;


-- =====================================================
-- Batch 2 发布检查、审批、发布包与同步 Outbox
-- 说明：发布包为不可变快照；所有时间字段按 UTC 语义存储，不使用 Redis 保存权威快照。
-- =====================================================

CREATE TABLE IF NOT EXISTS pc_publish_check_result (
    check_id bigint PRIMARY KEY,
    target_type varchar(100),
    target_id bigint,
    target_code varchar(100),
    check_code varchar(100),
    check_name_cn varchar(200),
    check_name_en varchar(200),
    check_level varchar(100),
    check_status varchar(100),
    message_key varchar(100),
    message_cn varchar(100),
    message_en varchar(100),
    evidence_json jsonb,
    resolved_flag varchar(100),
    status varchar(100),
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_publish_check_result IS '发布检查结果表';
COMMENT ON COLUMN pc_publish_check_result.check_id IS '发布检查结果ID';
COMMENT ON COLUMN pc_publish_check_result.target_type IS '检查对象类型';
COMMENT ON COLUMN pc_publish_check_result.target_id IS '检查对象ID';
COMMENT ON COLUMN pc_publish_check_result.target_code IS '检查对象编码';
COMMENT ON COLUMN pc_publish_check_result.check_code IS '检查项编码';
COMMENT ON COLUMN pc_publish_check_result.check_name_cn IS '检查项中文名称';
COMMENT ON COLUMN pc_publish_check_result.check_name_en IS '检查项英文名称';
COMMENT ON COLUMN pc_publish_check_result.check_level IS '检查等级：PASS通过，WARNING警告，BLOCKER阻断';
COMMENT ON COLUMN pc_publish_check_result.check_status IS '检查状态：PASS通过，WARNING警告，BLOCKER阻断';
COMMENT ON COLUMN pc_publish_check_result.message_key IS '消息国际化key';
COMMENT ON COLUMN pc_publish_check_result.message_cn IS '中文消息';
COMMENT ON COLUMN pc_publish_check_result.message_en IS '英文消息';
COMMENT ON COLUMN pc_publish_check_result.evidence_json IS '检查证据快照JSON';
COMMENT ON COLUMN pc_publish_check_result.resolved_flag IS '是否已处理：1是，0否';
COMMENT ON COLUMN pc_publish_check_result.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_publish_check_result.remark IS '备注';
COMMENT ON COLUMN pc_publish_check_result.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_publish_check_result.create_by IS '创建者';
COMMENT ON COLUMN pc_publish_check_result.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_publish_check_result.update_by IS '更新者';
COMMENT ON COLUMN pc_publish_check_result.update_time IS '更新时间，UTC timestamptz';


CREATE TABLE IF NOT EXISTS pc_publish_approval (
    approval_id bigint PRIMARY KEY,
    target_type varchar(100),
    target_id bigint,
    target_code varchar(100),
    approval_status varchar(100),
    submitter_user_id bigint,
    submitter_name varchar(100),
    approver_user_id bigint,
    approver_name varchar(100),
    approved_time timestamptz,
    approval_comment varchar(1000),
    snapshot_hash varchar(100),
    snapshot_json jsonb,
    status varchar(100),
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_publish_approval IS '发布审批记录表';
COMMENT ON COLUMN pc_publish_approval.approval_id IS '发布审批记录ID';
COMMENT ON COLUMN pc_publish_approval.target_type IS '审批对象类型';
COMMENT ON COLUMN pc_publish_approval.target_id IS '审批对象ID';
COMMENT ON COLUMN pc_publish_approval.target_code IS '审批对象编码';
COMMENT ON COLUMN pc_publish_approval.approval_status IS '审批状态：SUBMITTED已提交，APPROVED已通过，REJECTED已拒绝';
COMMENT ON COLUMN pc_publish_approval.submitter_user_id IS '提交人用户ID';
COMMENT ON COLUMN pc_publish_approval.submitter_name IS '提交人名称';
COMMENT ON COLUMN pc_publish_approval.approver_user_id IS '审批人用户ID';
COMMENT ON COLUMN pc_publish_approval.approver_name IS '审批人名称';
COMMENT ON COLUMN pc_publish_approval.approved_time IS '审批时间，UTC语义';
COMMENT ON COLUMN pc_publish_approval.approval_comment IS '审批意见';
COMMENT ON COLUMN pc_publish_approval.snapshot_hash IS '审批对象快照哈希';
COMMENT ON COLUMN pc_publish_approval.snapshot_json IS '审批对象快照JSON';
COMMENT ON COLUMN pc_publish_approval.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_publish_approval.remark IS '备注';
COMMENT ON COLUMN pc_publish_approval.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_publish_approval.create_by IS '创建者';
COMMENT ON COLUMN pc_publish_approval.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_publish_approval.update_by IS '更新者';
COMMENT ON COLUMN pc_publish_approval.update_time IS '更新时间，UTC timestamptz';


CREATE TABLE IF NOT EXISTS pc_product_publish_package (
    package_id bigint PRIMARY KEY,
    package_code varchar(100),
    package_type varchar(100),
    product_model_id bigint,
    product_model_code varchar(100),
    sales_variant_id bigint,
    sales_variant_code varchar(100),
    template_version_id bigint,
    template_version_no varchar(100),
    price_plan_version_id bigint,
    price_plan_code varchar(100),
    package_status varchar(100),
    package_hash varchar(100),
    package_json jsonb,
    version_snapshot_json jsonb,
    effective_from timestamptz,
    effective_to timestamptz,
    published_time timestamptz,
    published_by_id bigint,
    published_by_name varchar(100),
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_product_publish_package IS '产品发布包表';
COMMENT ON COLUMN pc_product_publish_package.package_id IS '产品发布包ID';
COMMENT ON COLUMN pc_product_publish_package.package_code IS '发布包编码';
COMMENT ON COLUMN pc_product_publish_package.package_type IS '发布包类型：PRODUCT产品能力发布包';
COMMENT ON COLUMN pc_product_publish_package.product_model_id IS '产品模型ID';
COMMENT ON COLUMN pc_product_publish_package.product_model_code IS '产品模型编码';
COMMENT ON COLUMN pc_product_publish_package.sales_variant_id IS '销售变体ID';
COMMENT ON COLUMN pc_product_publish_package.sales_variant_code IS '销售变体编码';
COMMENT ON COLUMN pc_product_publish_package.template_version_id IS '配置模板版本ID';
COMMENT ON COLUMN pc_product_publish_package.template_version_no IS '配置模板版本号';
COMMENT ON COLUMN pc_product_publish_package.price_plan_version_id IS '价格方案版本ID';
COMMENT ON COLUMN pc_product_publish_package.price_plan_code IS '价格方案编码';
COMMENT ON COLUMN pc_product_publish_package.package_status IS '发布包状态：PUBLISHED已发布，ARCHIVED已归档';
COMMENT ON COLUMN pc_product_publish_package.package_hash IS '发布包内容哈希';
COMMENT ON COLUMN pc_product_publish_package.package_json IS '发布包完整快照JSON';
COMMENT ON COLUMN pc_product_publish_package.version_snapshot_json IS '版本摘要快照JSON';
COMMENT ON COLUMN pc_product_publish_package.effective_from IS '生效开始时间，UTC语义';
COMMENT ON COLUMN pc_product_publish_package.effective_to IS '生效结束时间，UTC语义';
COMMENT ON COLUMN pc_product_publish_package.published_time IS '发布时间，UTC语义';
COMMENT ON COLUMN pc_product_publish_package.published_by_id IS '发布人用户ID';
COMMENT ON COLUMN pc_product_publish_package.published_by_name IS '发布人名称';
COMMENT ON COLUMN pc_product_publish_package.remark IS '备注';
COMMENT ON COLUMN pc_product_publish_package.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_product_publish_package.create_by IS '创建者';
COMMENT ON COLUMN pc_product_publish_package.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_product_publish_package.update_by IS '更新者';
COMMENT ON COLUMN pc_product_publish_package.update_time IS '更新时间，UTC timestamptz';


CREATE TABLE IF NOT EXISTS pc_product_snapshot_instance (
    snapshot_id bigint PRIMARY KEY,
    source_system varchar(100),
    source_biz_type varchar(100),
    source_biz_no varchar(100),
    source_biz_line_no varchar(100),
    customer_code varchar(100),
    package_id bigint,
    package_code varchar(100),
    package_hash varchar(100),
    product_model_code varchar(100),
    sales_variant_code varchar(100),
    template_version_id bigint,
    template_version_no varchar(100),
    price_plan_version_id bigint,
    price_plan_code varchar(100),
    selected_options_json jsonb,
    input_values_json jsonb,
    snapshot_json jsonb,
    snapshot_hash varchar(100),
    snapshot_status varchar(100),
    built_time timestamptz,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_product_snapshot_instance IS '产品能力快照实例表';
COMMENT ON COLUMN pc_product_snapshot_instance.snapshot_id IS '产品能力快照实例ID';
COMMENT ON COLUMN pc_product_snapshot_instance.source_system IS '来源系统：ORDER订单，ERP，MES';
COMMENT ON COLUMN pc_product_snapshot_instance.source_biz_type IS '来源业务类型：ORDER_LINE订单行，QUOTE_LINE报价行，MES_PLAN生产计划';
COMMENT ON COLUMN pc_product_snapshot_instance.source_biz_no IS '来源业务单号';
COMMENT ON COLUMN pc_product_snapshot_instance.source_biz_line_no IS '来源业务行号';
COMMENT ON COLUMN pc_product_snapshot_instance.customer_code IS '客户编码';
COMMENT ON COLUMN pc_product_snapshot_instance.package_id IS '发布包ID';
COMMENT ON COLUMN pc_product_snapshot_instance.package_code IS '发布包编码';
COMMENT ON COLUMN pc_product_snapshot_instance.package_hash IS '发布包哈希';
COMMENT ON COLUMN pc_product_snapshot_instance.product_model_code IS '产品模型编码';
COMMENT ON COLUMN pc_product_snapshot_instance.sales_variant_code IS '销售变体编码';
COMMENT ON COLUMN pc_product_snapshot_instance.template_version_id IS '配置模板版本ID';
COMMENT ON COLUMN pc_product_snapshot_instance.template_version_no IS '配置模板版本号';
COMMENT ON COLUMN pc_product_snapshot_instance.price_plan_version_id IS '价格方案版本ID';
COMMENT ON COLUMN pc_product_snapshot_instance.price_plan_code IS '价格方案编码';
COMMENT ON COLUMN pc_product_snapshot_instance.selected_options_json IS '用户已选答案JSON';
COMMENT ON COLUMN pc_product_snapshot_instance.input_values_json IS '尺寸和输入参数JSON';
COMMENT ON COLUMN pc_product_snapshot_instance.snapshot_json IS '产品能力快照JSON';
COMMENT ON COLUMN pc_product_snapshot_instance.snapshot_hash IS '产品能力快照哈希';
COMMENT ON COLUMN pc_product_snapshot_instance.snapshot_status IS '快照状态：BUILT已构建，VOID作废';
COMMENT ON COLUMN pc_product_snapshot_instance.built_time IS '快照构建时间，UTC语义';
COMMENT ON COLUMN pc_product_snapshot_instance.remark IS '备注';
COMMENT ON COLUMN pc_product_snapshot_instance.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_product_snapshot_instance.create_by IS '创建者';
COMMENT ON COLUMN pc_product_snapshot_instance.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_product_snapshot_instance.update_by IS '更新者';
COMMENT ON COLUMN pc_product_snapshot_instance.update_time IS '更新时间，UTC timestamptz';

CREATE INDEX IF NOT EXISTS idx_pc_snapshot_source ON pc_product_snapshot_instance(source_system, source_biz_type, source_biz_no, source_biz_line_no);
CREATE INDEX IF NOT EXISTS idx_pc_snapshot_package ON pc_product_snapshot_instance(package_id, package_code);
CREATE INDEX IF NOT EXISTS idx_pc_snapshot_hash ON pc_product_snapshot_instance(snapshot_hash);
CREATE INDEX IF NOT EXISTS idx_pc_snapshot_built_time ON pc_product_snapshot_instance(built_time DESC);


CREATE TABLE IF NOT EXISTS pc_product_sync_outbox (
    outbox_id bigint PRIMARY KEY,
    package_id bigint,
    package_code varchar(100),
    target_system varchar(100),
    event_type varchar(100),
    payload_hash varchar(100),
    payload_json jsonb,
    sync_status varchar(100),
    retry_count integer,
    next_retry_time timestamptz,
    last_error_key varchar(100),
    last_error_message varchar(1000),
    status varchar(100),
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_product_sync_outbox IS '产品能力同步Outbox表';
COMMENT ON COLUMN pc_product_sync_outbox.outbox_id IS '产品能力同步OutboxID';
COMMENT ON COLUMN pc_product_sync_outbox.package_id IS '发布包ID';
COMMENT ON COLUMN pc_product_sync_outbox.package_code IS '发布包编码';
COMMENT ON COLUMN pc_product_sync_outbox.target_system IS '目标系统：ORDER订单，ERP，MES';
COMMENT ON COLUMN pc_product_sync_outbox.event_type IS '事件类型';
COMMENT ON COLUMN pc_product_sync_outbox.payload_hash IS '负载哈希';
COMMENT ON COLUMN pc_product_sync_outbox.payload_json IS '同步负载JSON';
COMMENT ON COLUMN pc_product_sync_outbox.sync_status IS '同步状态：PENDING待同步，SUCCESS成功，FAILED失败';
COMMENT ON COLUMN pc_product_sync_outbox.retry_count IS '重试次数';
COMMENT ON COLUMN pc_product_sync_outbox.next_retry_time IS '下次重试时间，UTC语义';
COMMENT ON COLUMN pc_product_sync_outbox.last_error_key IS '最后错误国际化key';
COMMENT ON COLUMN pc_product_sync_outbox.last_error_message IS '最后错误消息';
COMMENT ON COLUMN pc_product_sync_outbox.status IS '状态：1正常，0停用';
COMMENT ON COLUMN pc_product_sync_outbox.remark IS '备注';
COMMENT ON COLUMN pc_product_sync_outbox.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_product_sync_outbox.create_by IS '创建者';
COMMENT ON COLUMN pc_product_sync_outbox.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_product_sync_outbox.update_by IS '更新者';
COMMENT ON COLUMN pc_product_sync_outbox.update_time IS '更新时间，UTC timestamptz';

CREATE TABLE IF NOT EXISTS pc_import_batch (
    batch_id bigint PRIMARY KEY,
    batch_code varchar(100),
    import_type varchar(100),
    source_system varchar(100),
    source_file_name varchar(255),
    source_file_url varchar(500),
    target_object_type varchar(100),
    target_object_code varchar(100),
    import_status varchar(100),
    total_rows integer,
    success_rows integer,
    warning_rows integer,
    failed_rows integer,
    mapping_json jsonb,
    preview_json jsonb,
    error_summary_json jsonb,
    started_time timestamptz,
    finished_time timestamptz,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_import_batch IS '产品能力导入批次表';
COMMENT ON COLUMN pc_import_batch.batch_id IS '产品能力导入批次ID';
COMMENT ON COLUMN pc_import_batch.batch_code IS '导入批次编码';
COMMENT ON COLUMN pc_import_batch.import_type IS '导入类型：MATERIAL物料，COMPONENT组件，OPTION答案，PRICE价格，MEDIA资料';
COMMENT ON COLUMN pc_import_batch.source_system IS '来源系统：MANUAL人工，ERP，MES，LEGACY老系统';
COMMENT ON COLUMN pc_import_batch.source_file_name IS '原始文件名';
COMMENT ON COLUMN pc_import_batch.source_file_url IS '原始文件地址';
COMMENT ON COLUMN pc_import_batch.target_object_type IS '目标对象类型：PRODUCT_MODEL产品模型，TEMPLATE配置模板，PRICE_PLAN价格方案';
COMMENT ON COLUMN pc_import_batch.target_object_code IS '目标对象编码';
COMMENT ON COLUMN pc_import_batch.import_status IS '导入状态：DRAFT草稿，PARSED已解析，VALIDATED已校验，COMMITTED已提交，FAILED失败，CANCELED已取消';
COMMENT ON COLUMN pc_import_batch.total_rows IS '总行数';
COMMENT ON COLUMN pc_import_batch.success_rows IS '成功行数';
COMMENT ON COLUMN pc_import_batch.warning_rows IS '警告行数';
COMMENT ON COLUMN pc_import_batch.failed_rows IS '失败行数';
COMMENT ON COLUMN pc_import_batch.mapping_json IS '字段映射JSON';
COMMENT ON COLUMN pc_import_batch.preview_json IS '导入预览JSON';
COMMENT ON COLUMN pc_import_batch.error_summary_json IS '错误汇总JSON';
COMMENT ON COLUMN pc_import_batch.started_time IS '导入开始时间，UTC语义';
COMMENT ON COLUMN pc_import_batch.finished_time IS '导入完成时间，UTC语义';
COMMENT ON COLUMN pc_import_batch.remark IS '备注';
COMMENT ON COLUMN pc_import_batch.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_import_batch.create_by IS '创建者';
COMMENT ON COLUMN pc_import_batch.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_import_batch.update_by IS '更新者';
COMMENT ON COLUMN pc_import_batch.update_time IS '更新时间，UTC timestamptz';

CREATE INDEX IF NOT EXISTS idx_pc_import_batch_code ON pc_import_batch(batch_code);
CREATE INDEX IF NOT EXISTS idx_pc_import_batch_type ON pc_import_batch(import_type, import_status);
CREATE INDEX IF NOT EXISTS idx_pc_import_batch_target ON pc_import_batch(target_object_type, target_object_code);
CREATE INDEX IF NOT EXISTS idx_pc_import_batch_create_time ON pc_import_batch(create_time DESC);

CREATE TABLE IF NOT EXISTS pc_import_row_issue (
    issue_id bigint PRIMARY KEY,
    batch_id bigint,
    row_no integer,
    column_name varchar(100),
    issue_level varchar(100),
    issue_code varchar(100),
    issue_message varchar(1000),
    raw_row_json jsonb,
    fixed_row_json jsonb,
    status varchar(100),
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_import_row_issue IS '产品能力导入行问题表';
COMMENT ON COLUMN pc_import_row_issue.issue_id IS '产品能力导入行问题ID';
COMMENT ON COLUMN pc_import_row_issue.batch_id IS '导入批次ID';
COMMENT ON COLUMN pc_import_row_issue.row_no IS '行号';
COMMENT ON COLUMN pc_import_row_issue.column_name IS '字段名';
COMMENT ON COLUMN pc_import_row_issue.issue_level IS '问题级别：ERROR错误，WARNING警告';
COMMENT ON COLUMN pc_import_row_issue.issue_code IS '问题编码';
COMMENT ON COLUMN pc_import_row_issue.issue_message IS '问题消息';
COMMENT ON COLUMN pc_import_row_issue.raw_row_json IS '原始行JSON';
COMMENT ON COLUMN pc_import_row_issue.fixed_row_json IS '修正后行JSON';
COMMENT ON COLUMN pc_import_row_issue.status IS '状态：1待处理，2已处理，0忽略';
COMMENT ON COLUMN pc_import_row_issue.remark IS '备注';
COMMENT ON COLUMN pc_import_row_issue.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_import_row_issue.create_by IS '创建者';
COMMENT ON COLUMN pc_import_row_issue.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_import_row_issue.update_by IS '更新者';
COMMENT ON COLUMN pc_import_row_issue.update_time IS '更新时间，UTC timestamptz';

CREATE INDEX IF NOT EXISTS idx_pc_import_issue_batch ON pc_import_row_issue(batch_id, row_no);
CREATE INDEX IF NOT EXISTS idx_pc_import_issue_level ON pc_import_row_issue(batch_id, issue_level, status);

INSERT INTO sys_menu (menu_id, tenant_id, parent_id, menu_name, i18n_key, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
    (24007, 1, 24000, 'Publish Gate', 'productCenter.menu.publish', 11, 'publish', 'product-center/publish', NULL, 1, 0, 'C', '1', '1', 'product:publish:list', 'check', 'admin', now(), '', NULL, '产品能力-测试发布')
ON CONFLICT (menu_id) DO UPDATE SET tenant_id = EXCLUDED.tenant_id, parent_id = EXCLUDED.parent_id, menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key, order_num = EXCLUDED.order_num, path = EXCLUDED.path, component = EXCLUDED.component, visible = EXCLUDED.visible, status = EXCLUDED.status, perms = EXCLUDED.perms, icon = EXCLUDED.icon;

INSERT INTO sys_menu (menu_id, tenant_id, parent_id, menu_name, i18n_key, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
    (24070, 1, 24007, 'Publish Query', 'common.search', 1, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:publish:list', '#', 'admin', now(), '', NULL, '发布查询'),
    (24071, 1, 24007, 'Publish Check', 'productCenter.publish.check', 2, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:publish:check', '#', 'admin', now(), '', NULL, '发布检查'),
    (24072, 1, 24007, 'Publish Approve', 'productCenter.publish.approve', 3, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:publish:approve', '#', 'admin', now(), '', NULL, '发布审批'),
    (24073, 1, 24007, 'Publish Package', 'productCenter.publish.publish', 4, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:publish:publish', '#', 'admin', now(), '', NULL, '生成发布包'),
    (24074, 1, 24007, 'Retry Sync', 'productCenter.publish.retrySync', 5, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:publish:retrySync', '#', 'admin', now(), '', NULL, '重试产品能力同步')
ON CONFLICT (menu_id) DO UPDATE SET tenant_id = EXCLUDED.tenant_id, parent_id = EXCLUDED.parent_id, menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key, order_num = EXCLUDED.order_num, visible = EXCLUDED.visible, status = EXCLUDED.status, perms = EXCLUDED.perms;

INSERT INTO sys_menu (menu_id, tenant_id, parent_id, menu_name, i18n_key, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
    (24008, 1, 24000, 'Sales View', 'productCenter.menu.salesView', 12, 'sales-view', 'product-center/sales-view', NULL, 1, 0, 'C', '1', '1', 'product:sales-view:list', 'eye', 'admin', now(), '', NULL, '产品能力-销售只读视图'),
    (24080, 1, 24008, 'Sales View Query', 'common.search', 1, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:sales-view:list', '#', 'admin', now(), '', NULL, '销售只读查询'),
    (24081, 1, 24008, 'Build Order Snapshot', 'productCenter.orderSnapshot.build', 2, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:order-snapshot:build', '#', 'admin', now(), '', NULL, '构建订单产品快照'),
    (24082, 1, 24008, 'Snapshot Instance Query', 'common.search', 3, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:snapshot-instance:list', '#', 'admin', now(), '', NULL, '产品能力快照实例查询'),
    (24083, 1, 24008, 'Snapshot Instance Detail', 'common.detail', 4, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:snapshot-instance:query', '#', 'admin', now(), '', NULL, '产品能力快照实例详情'),
    (24084, 1, 24008, 'Build Snapshot Instance', 'productCenter.orderSnapshot.build', 5, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:snapshot-instance:build', '#', 'admin', now(), '', NULL, '构建并保存产品能力快照实例')
ON CONFLICT (menu_id) DO UPDATE SET tenant_id = EXCLUDED.tenant_id, parent_id = EXCLUDED.parent_id, menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key, order_num = EXCLUDED.order_num, path = EXCLUDED.path, component = EXCLUDED.component, visible = EXCLUDED.visible, status = EXCLUDED.status, perms = EXCLUDED.perms, icon = EXCLUDED.icon;

INSERT INTO sys_menu (menu_id, tenant_id, parent_id, menu_name, i18n_key, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
    (24090, 1, 24000, 'Import Batch Query', 'common.search', 90, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:import:list', '#', 'admin', now(), '', NULL, '产品能力导入批次查询'),
    (24091, 1, 24000, 'Import Batch Detail', 'common.detail', 91, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:import:query', '#', 'admin', now(), '', NULL, '产品能力导入批次详情'),
    (24092, 1, 24000, 'Import Batch Add', 'common.add', 92, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:import:add', '#', 'admin', now(), '', NULL, '产品能力导入批次新增'),
    (24093, 1, 24000, 'Import Batch Edit', 'common.edit', 93, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:import:edit', '#', 'admin', now(), '', NULL, '产品能力导入批次编辑'),
    (24094, 1, 24000, 'Import Batch Remove', 'common.delete', 94, '#', NULL, NULL, 1, 0, 'F', '1', '1', 'product:import:remove', '#', 'admin', now(), '', NULL, '产品能力导入批次删除')
ON CONFLICT (menu_id) DO UPDATE SET tenant_id = EXCLUDED.tenant_id, parent_id = EXCLUDED.parent_id, menu_name = EXCLUDED.menu_name, i18n_key = EXCLUDED.i18n_key, order_num = EXCLUDED.order_num, visible = EXCLUDED.visible, status = EXCLUDED.status, perms = EXCLUDED.perms;

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT 1, menu_id, 1 FROM sys_menu WHERE menu_id BETWEEN 24000 AND 24199
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- =====================================================
-- 第一阶段正式菜单、按钮和字典归一化
-- 说明：
-- 1. 旧 24000 “Product Capability” 作为历史入口隐藏，不删除，避免破坏已存在角色授权和审计记录。
-- 2. 正式侧边栏使用三个一级菜单：基础资料、配置与价格、发布与应用。
-- 3. 菜单最多两级；页面内 tabs 只能作为局部细节，不承载正式主入口。
-- 4. 本块必须保持幂等，可重复执行到开发库。
-- =====================================================

UPDATE sys_menu
SET visible = '0',
    status = '0',
    update_by = 'system',
    update_time = now(),
    remark = 'Shared product capability center | 第一阶段归一化后隐藏的历史产品能力父菜单'
WHERE menu_id = 24000;

UPDATE sys_menu
SET visible = '0',
    status = '0',
    update_by = 'system',
    update_time = now(),
    remark = '第一阶段归一化后隐藏的历史产品能力草案菜单'
WHERE menu_id BETWEEN 24001 AND 24199;

INSERT INTO sys_menu (menu_id, tenant_id, parent_id, menu_name, i18n_key, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
    (24200, 1, 0, 'Product Master Data', 'productCenter.menu.masterData', 30, 'product-master', NULL, NULL, '1', '0', 'M', '1', '1', NULL, 'dict', 'system', now(), NULL, NULL, '产品能力-基础资料'),
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
    (24201, 1, 24200, 'Workbench', 'productCenter.menu.workbench', 1, 'workbench', 'product-center/workbench', NULL, '1', '0', 'C', '1', '1', 'product:center:view', 'dashboard', 'system', now(), NULL, NULL, '共享产品能力工作台'),
    (24202, 1, 24200, 'Product Categories', 'productCenter.menu.categories', 2, 'categories', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'tree-table', 'system', now(), NULL, NULL, '产品分类'),
    (24203, 1, 24200, 'Materials', 'productCenter.menu.materials', 3, 'materials', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'dict', 'system', now(), NULL, NULL, '物料管理'),
    (24204, 1, 24200, 'Auxiliary Materials', 'productCenter.menu.components', 4, 'components', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'component', 'system', now(), NULL, NULL, '辅材管理'),
    (24205, 1, 24200, 'Media Assets', 'productCenter.menu.mediaAssets', 5, 'media-assets', 'product-center/assets', NULL, '1', '0', 'C', '1', '1', 'product:asset:list', 'upload', 'system', now(), NULL, NULL, '资料资产'),
    (24206, 1, 24200, 'Media Bindings', 'productCenter.menu.mediaBindings', 6, 'media-bindings', 'product-center/assets', NULL, '1', '0', 'C', '1', '1', 'product:asset:list', 'link', 'system', now(), NULL, NULL, '资料绑定'),
    (24301, 1, 24300, 'Product Models', 'productCenter.menu.models', 1, 'models', 'product-center/model', NULL, '1', '0', 'C', '1', '1', 'product:model:list', 'tree-table', 'system', now(), NULL, NULL, '产品模型'),
    (24302, 1, 24300, 'Sales Variants', 'productCenter.menu.salesVariants', 2, 'sales-variants', 'product-center/model', NULL, '1', '0', 'C', '1', '1', 'product:model:list', 'list', 'system', now(), NULL, NULL, '销售变体'),
    (24303, 1, 24300, 'Question Groups', 'productCenter.menu.questionGroups', 3, 'question-groups', 'product-center/question-groups', NULL, '1', '0', 'C', '1', '1', 'product:template:list', 'list', 'system', now(), NULL, NULL, '问题组模板'),
    (24304, 1, 24300, 'Config Template', 'productCenter.menu.template', 4, 'template', 'product-center/template', NULL, '1', '0', 'C', '1', '1', 'product:template:list', 'form', 'system', now(), NULL, NULL, '配置模板'),
    (24305, 1, 24300, 'Pricing', 'productCenter.menu.pricing', 5, 'pricing', 'product-center/pricing', NULL, '1', '0', 'C', '1', '1', 'product:price:list', 'money', 'system', now(), NULL, NULL, '价格中心'),
    (24306, 1, 24300, 'Quote Preview', 'productCenter.menu.quotePreview', 6, 'quote-preview', 'product-center/quote-preview', NULL, '1', '0', 'C', '1', '1', 'product:price:test', 'calculator', 'system', now(), NULL, NULL, '报价预览'),
    (24401, 1, 24400, 'Publish Gate', 'productCenter.menu.publish', 1, 'publish', 'product-center/publish', NULL, '1', '0', 'C', '1', '1', 'product:publish:list', 'check', 'system', now(), NULL, NULL, '测试发布'),
    (24402, 1, 24400, 'Approvals', 'productCenter.menu.approvals', 2, 'approvals', 'product-center/approvals', NULL, '1', '0', 'C', '1', '1', 'product:publish:list', 'validCode', 'system', now(), NULL, NULL, '审核审批'),
    (24403, 1, 24400, 'Gap Tasks', 'productCenter.menu.gapTasks', 3, 'gap-tasks', 'product-center/gap-tasks', NULL, '1', '0', 'C', '1', '1', 'product:publish:list', 'bug', 'system', now(), NULL, NULL, '缺口待办'),
    (24404, 1, 24400, 'Publish Packages', 'productCenter.menu.packages', 4, 'packages', 'product-center/packages', NULL, '1', '0', 'C', '1', '1', 'product:publish:list', 'zip', 'system', now(), NULL, NULL, '发布包'),
    (24405, 1, 24400, 'Sync Outbox', 'productCenter.menu.syncOutbox', 5, 'sync-outbox', 'product-center/sync-outbox', NULL, '1', '0', 'C', '1', '1', 'product:publish:list', 'redis', 'system', now(), NULL, NULL, '同步日志'),
    (24406, 1, 24400, 'Import Center', 'productCenter.menu.importCenter', 6, 'import', 'product-center/import', NULL, '1', '0', 'C', '1', '1', 'product:import:list', 'upload', 'system', now(), NULL, NULL, '导入中心'),
    (24407, 1, 24400, 'Sales View', 'productCenter.menu.salesView', 7, 'sales-view', 'product-center/sales-view', NULL, '1', '0', 'C', '1', '1', 'product:sales-view:list', 'eye', 'system', now(), NULL, NULL, '销售只读总览')
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
    (24220, 1, 24201, 'Workbench View', 'productCenter.menu.workbench', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:center:view', '#', 'system', now(), NULL, NULL, '工作台查看'),
    (24230, 1, 24202, 'Category Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:list', '#', 'system', now(), NULL, NULL, '产品分类查询'),
    (24231, 1, 24202, 'Category Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:add', '#', 'system', now(), NULL, NULL, '产品分类新增'),
    (24232, 1, 24202, 'Category Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:edit', '#', 'system', now(), NULL, NULL, '产品分类编辑'),
    (24233, 1, 24202, 'Category Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:remove', '#', 'system', now(), NULL, NULL, '产品分类删除'),
    (24234, 1, 24202, 'Category Reference', 'productCenter.common.references', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:reference', '#', 'system', now(), NULL, NULL, '产品分类引用检查'),
    (24240, 1, 24203, 'Material Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:list', '#', 'system', now(), NULL, NULL, '物料查询'),
    (24241, 1, 24203, 'Material Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:add', '#', 'system', now(), NULL, NULL, '物料新增'),
    (24242, 1, 24203, 'Material Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:edit', '#', 'system', now(), NULL, NULL, '物料编辑'),
    (24243, 1, 24203, 'Material Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:remove', '#', 'system', now(), NULL, NULL, '物料删除'),
    (24250, 1, 24204, 'Component Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:list', '#', 'system', now(), NULL, NULL, '辅材查询'),
    (24251, 1, 24204, 'Component Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:add', '#', 'system', now(), NULL, NULL, '辅材新增'),
    (24252, 1, 24204, 'Component Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:edit', '#', 'system', now(), NULL, NULL, '辅材编辑'),
    (24253, 1, 24204, 'Component Delete', 'common.delete', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:base:remove', '#', 'system', now(), NULL, NULL, '辅材删除'),
    (24260, 1, 24205, 'Asset Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:list', '#', 'system', now(), NULL, NULL, '资料资产查询'),
    (24261, 1, 24205, 'Asset Upload', 'common.upload', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:upload', '#', 'system', now(), NULL, NULL, '资料资产上传'),
    (24262, 1, 24205, 'Asset Reference', 'productCenter.common.references', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:reference', '#', 'system', now(), NULL, NULL, '资料资产引用检查'),
    (24270, 1, 24206, 'Binding Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:list', '#', 'system', now(), NULL, NULL, '资料绑定查询'),
    (24271, 1, 24206, 'Binding Add', 'common.add', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:bind', '#', 'system', now(), NULL, NULL, '资料绑定新增'),
    (24272, 1, 24206, 'Binding Edit', 'common.edit', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:bind', '#', 'system', now(), NULL, NULL, '资料绑定编辑'),
    (24273, 1, 24206, 'Binding Reference', 'productCenter.common.references', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:asset:reference', '#', 'system', now(), NULL, NULL, '资料绑定引用检查'),
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

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES
    (242001, 'Product material type', 'product_material_type', '1', 'system', now(), '产品能力物料类型'),
    (242002, 'Product component type', 'product_component_type', '1', 'system', now(), '产品能力辅材/组件类型'),
    (242003, 'Product business type', 'product_business_type', '1', 'system', now(), '产品能力业务类型'),
    (242004, 'Product unit', 'product_unit', '1', 'system', now(), '产品能力单位')
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
    (242120, 1, 'Motor', 'MOTOR', 'product_component_type', 'success', 'Y', '1', 'system', now(), '电机'),
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
    (242140, 1, 'Finished', 'FINISHED', 'product_business_type', 'success', 'Y', '1', 'system', now(), '成品'),
    (242141, 2, 'Custom', 'CUSTOM', 'product_business_type', 'primary', 'N', '1', 'system', now(), '定制品'),
    (242142, 3, 'Raw', 'RAW', 'product_business_type', 'info', 'N', '1', 'system', now(), '原料'),
    (242143, 4, 'Purchase', 'PURCHASE', 'product_business_type', 'warning', 'N', '1', 'system', now(), '采购件'),
    (242144, 5, 'Service', 'SERVICE', 'product_business_type', 'info', 'N', '1', 'system', now(), '服务'),
    (242150, 1, 'cm', 'cm', 'product_unit', 'primary', 'Y', '1', 'system', now(), '厘米'),
    (242151, 2, 'inch', 'inch', 'product_unit', 'primary', 'N', '1', 'system', now(), '英寸'),
    (242152, 3, 'm', 'm', 'product_unit', 'primary', 'N', '1', 'system', now(), '米'),
    (242153, 4, 'm2', 'm2', 'product_unit', 'primary', 'N', '1', 'system', now(), '平方米'),
    (242154, 5, 'PC', 'PC', 'product_unit', 'primary', 'N', '1', 'system', now(), '件'),
    (242155, 6, 'Set', 'Set', 'product_unit', 'primary', 'N', '1', 'system', now(), '套')
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
