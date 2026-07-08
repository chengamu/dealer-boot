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

CREATE TABLE IF NOT EXISTS pc_formula (
    formula_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    formula_code varchar(80) NOT NULL,
    formula_name varchar(200) NOT NULL,
    category_id bigint NOT NULL,
    category_code varchar(80) NOT NULL,
    category_name_cn varchar(200) NOT NULL,
    product_type_code varchar(80) NOT NULL,
    product_type_name_cn varchar(200) NOT NULL,
    min_width_inch numeric(18,4) NOT NULL DEFAULT 0,
    min_height_inch numeric(18,4) NOT NULL DEFAULT 0,
    max_width_inch numeric(18,4) NOT NULL,
    max_height_inch numeric(18,4) NOT NULL,
    size_summary varchar(200),
    material_line_count int NOT NULL DEFAULT 0,
    configured_flag boolean NOT NULL DEFAULT false,
    current_version_id bigint,
    current_version_no int,
    current_version_label varchar(40),
    draft_version_no int NOT NULL DEFAULT 1,
    latest_validation_status varchar(30) NOT NULL DEFAULT 'NOT_VALIDATED',
    latest_validation_message varchar(500),
    latest_validation_time timestamptz,
    material_validation_status varchar(30) NOT NULL DEFAULT 'NOT_VALIDATED',
    material_validation_message varchar(500),
    material_validation_time timestamptz,
    option_validation_status varchar(30) NOT NULL DEFAULT 'NOT_VALIDATED',
    option_validation_message varchar(500),
    option_validation_time timestamptz,
    simulation_validation_status varchar(30) NOT NULL DEFAULT 'NOT_VALIDATED',
    simulation_validation_message varchar(500),
    simulation_validation_time timestamptz,
    status varchar(30) NOT NULL DEFAULT 'DRAFT',
    audit_by varchar(64),
    audit_time timestamptz,
    reject_reason varchar(500),
    sort_order int NOT NULL DEFAULT 0,
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_formula
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    ADD COLUMN IF NOT EXISTS formula_code varchar(80),
    ADD COLUMN IF NOT EXISTS formula_name varchar(200),
    ADD COLUMN IF NOT EXISTS category_id bigint,
    ADD COLUMN IF NOT EXISTS category_code varchar(80),
    ADD COLUMN IF NOT EXISTS category_name_cn varchar(200),
    ADD COLUMN IF NOT EXISTS product_type_code varchar(80),
    ADD COLUMN IF NOT EXISTS product_type_name_cn varchar(200),
    ADD COLUMN IF NOT EXISTS min_width_inch numeric(18,4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS min_height_inch numeric(18,4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS max_width_inch numeric(18,4),
    ADD COLUMN IF NOT EXISTS max_height_inch numeric(18,4),
    ADD COLUMN IF NOT EXISTS size_summary varchar(200),
    ADD COLUMN IF NOT EXISTS material_line_count int NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS configured_flag boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS current_version_id bigint,
    ADD COLUMN IF NOT EXISTS current_version_no int,
    ADD COLUMN IF NOT EXISTS current_version_label varchar(40),
    ADD COLUMN IF NOT EXISTS draft_version_no int NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS latest_validation_status varchar(30) NOT NULL DEFAULT 'NOT_VALIDATED',
    ADD COLUMN IF NOT EXISTS latest_validation_message varchar(500),
    ADD COLUMN IF NOT EXISTS latest_validation_time timestamptz,
    ADD COLUMN IF NOT EXISTS material_validation_status varchar(30) NOT NULL DEFAULT 'NOT_VALIDATED',
    ADD COLUMN IF NOT EXISTS material_validation_message varchar(500),
    ADD COLUMN IF NOT EXISTS material_validation_time timestamptz,
    ADD COLUMN IF NOT EXISTS option_validation_status varchar(30) NOT NULL DEFAULT 'NOT_VALIDATED',
    ADD COLUMN IF NOT EXISTS option_validation_message varchar(500),
    ADD COLUMN IF NOT EXISTS option_validation_time timestamptz,
    ADD COLUMN IF NOT EXISTS simulation_validation_status varchar(30) NOT NULL DEFAULT 'NOT_VALIDATED',
    ADD COLUMN IF NOT EXISTS simulation_validation_message varchar(500),
    ADD COLUMN IF NOT EXISTS simulation_validation_time timestamptz,
    ADD COLUMN IF NOT EXISTS status varchar(30) NOT NULL DEFAULT 'DRAFT',
    ADD COLUMN IF NOT EXISTS audit_by varchar(64),
    ADD COLUMN IF NOT EXISTS audit_time timestamptz,
    ADD COLUMN IF NOT EXISTS reject_reason varchar(500),
    ADD COLUMN IF NOT EXISTS sort_order int NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS del_flag varchar(1) NOT NULL DEFAULT '0',
    ADD COLUMN IF NOT EXISTS remark varchar(500);

COMMENT ON TABLE pc_formula IS '产品配方主表';
COMMENT ON COLUMN pc_formula.formula_id IS '配方ID';
COMMENT ON COLUMN pc_formula.tenant_id IS '租户ID';
COMMENT ON COLUMN pc_formula.formula_code IS '配方编号，程序校验唯一';
COMMENT ON COLUMN pc_formula.formula_name IS '配方名称';
COMMENT ON COLUMN pc_formula.category_id IS '产品分类ID';
COMMENT ON COLUMN pc_formula.category_code IS '产品分类编码快照';
COMMENT ON COLUMN pc_formula.category_name_cn IS '产品分类中文名称快照';
COMMENT ON COLUMN pc_formula.product_type_code IS '产品类型编码，来源 pc_product_dict_item.product_type';
COMMENT ON COLUMN pc_formula.product_type_name_cn IS '产品类型中文名称快照';
COMMENT ON COLUMN pc_formula.min_width_inch IS '最小宽度，单位英寸';
COMMENT ON COLUMN pc_formula.min_height_inch IS '最小高度，单位英寸';
COMMENT ON COLUMN pc_formula.max_width_inch IS '最大宽度，单位英寸';
COMMENT ON COLUMN pc_formula.max_height_inch IS '最大高度，单位英寸';
COMMENT ON COLUMN pc_formula.size_summary IS '尺寸摘要';
COMMENT ON COLUMN pc_formula.material_line_count IS '物料行数，设置配方阶段维护';
COMMENT ON COLUMN pc_formula.configured_flag IS '是否已设置配方明细';
COMMENT ON COLUMN pc_formula.current_version_id IS '当前生效版本ID';
COMMENT ON COLUMN pc_formula.current_version_no IS '当前生效版本号';
COMMENT ON COLUMN pc_formula.current_version_label IS '当前生效版本展示号';
COMMENT ON COLUMN pc_formula.draft_version_no IS '草稿版本预览号';
COMMENT ON COLUMN pc_formula.latest_validation_status IS '最近校验状态：NOT_VALIDATED、PASS、FAIL';
COMMENT ON COLUMN pc_formula.latest_validation_message IS '最近校验消息key';
COMMENT ON COLUMN pc_formula.latest_validation_time IS '最近校验时间，UTC timestamptz';
COMMENT ON COLUMN pc_formula.material_validation_status IS '原料校验状态：NOT_VALIDATED、PASS、FAIL';
COMMENT ON COLUMN pc_formula.material_validation_message IS '原料校验消息key';
COMMENT ON COLUMN pc_formula.material_validation_time IS '原料校验时间，UTC timestamptz';
COMMENT ON COLUMN pc_formula.option_validation_status IS '选项校验状态：NOT_VALIDATED、PASS、FAIL';
COMMENT ON COLUMN pc_formula.option_validation_message IS '选项校验消息key';
COMMENT ON COLUMN pc_formula.option_validation_time IS '选项校验时间，UTC timestamptz';
COMMENT ON COLUMN pc_formula.simulation_validation_status IS '模拟校验状态：NOT_VALIDATED、PASS、FAIL';
COMMENT ON COLUMN pc_formula.simulation_validation_message IS '模拟校验消息key';
COMMENT ON COLUMN pc_formula.simulation_validation_time IS '模拟校验时间，UTC timestamptz';
COMMENT ON COLUMN pc_formula.status IS '单状态：DRAFT、PENDING_REVIEW、REJECTED、EFFECTIVE、STOPPED';
COMMENT ON COLUMN pc_formula.audit_by IS '最后审核人';
COMMENT ON COLUMN pc_formula.audit_time IS '最后审核时间，UTC timestamptz';
COMMENT ON COLUMN pc_formula.reject_reason IS '最近驳回原因';
COMMENT ON COLUMN pc_formula.sort_order IS '排序';
COMMENT ON COLUMN pc_formula.del_flag IS '删除标志：0存在，2删除';
COMMENT ON COLUMN pc_formula.remark IS '备注';
COMMENT ON COLUMN pc_formula.create_by_id IS '创建者ID';
COMMENT ON COLUMN pc_formula.create_by IS '创建者';
COMMENT ON COLUMN pc_formula.create_time IS '创建时间，UTC timestamptz';
COMMENT ON COLUMN pc_formula.update_by IS '更新者';
COMMENT ON COLUMN pc_formula.update_time IS '更新时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_formula_code_active ON pc_formula (tenant_id, formula_code) WHERE del_flag = '0';
DROP INDEX IF EXISTS idx_pc_formula_natural_active;
CREATE INDEX IF NOT EXISTS idx_pc_formula_natural_active ON pc_formula (tenant_id, formula_name, category_id, product_type_code, min_width_inch, min_height_inch, max_width_inch, max_height_inch) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_formula_category_type_status ON pc_formula (tenant_id, category_code, product_type_code, status);
CREATE INDEX IF NOT EXISTS idx_pc_formula_status_update ON pc_formula (tenant_id, status, update_time DESC);

UPDATE pc_formula
SET size_summary = trim(trailing '.' from trim(trailing '0' from min_width_inch::text))
    || '≤W≤' || trim(trailing '.' from trim(trailing '0' from max_width_inch::text))
    || 'in, ' || trim(trailing '.' from trim(trailing '0' from min_height_inch::text))
    || '≤H≤' || trim(trailing '.' from trim(trailing '0' from max_height_inch::text)) || 'in'
WHERE max_width_inch IS NOT NULL
  AND max_height_inch IS NOT NULL;

UPDATE pc_formula
SET status = CASE status
    WHEN 'ENABLED' THEN 'EFFECTIVE'
    WHEN 'DISABLED' THEN 'STOPPED'
    ELSE status
END
WHERE status IN ('ENABLED', 'DISABLED');

DROP TABLE IF EXISTS pc_formula_item;

CREATE TABLE IF NOT EXISTS pc_formula_material (
    formula_material_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    formula_id bigint NOT NULL,
    line_no int,
    material_id bigint NOT NULL,
    material_code varchar(80) NOT NULL,
    material_name_cn varchar(200) NOT NULL,
    spec_model_text varchar(500),
    attribute_group_id bigint,
    attribute_group_code varchar(80),
    attribute_group_name_cn varchar(200),
    material_type_id bigint,
    material_type_code varchar(80),
    material_type_name_cn varchar(200),
    unit_code varchar(80),
    default_flag boolean NOT NULL DEFAULT false,
    required_flag boolean NOT NULL DEFAULT true,
    usage_mode varchar(40) NOT NULL DEFAULT 'FIXED',
    usage_formula text,
    fixed_usage_qty numeric(18,6),
    calculation_unit_code varchar(80),
    rounding_mode varchar(40),
    min_usage_qty numeric(18,6),
    max_usage_qty numeric(18,6),
    loss_rate numeric(10,6),
    production_remark varchar(500),
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    sort_order int NOT NULL DEFAULT 0,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_formula_material
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    ADD COLUMN IF NOT EXISTS formula_id bigint,
    ADD COLUMN IF NOT EXISTS line_no int,
    ADD COLUMN IF NOT EXISTS material_id bigint,
    ADD COLUMN IF NOT EXISTS material_code varchar(80),
    ADD COLUMN IF NOT EXISTS material_name_cn varchar(200),
    ADD COLUMN IF NOT EXISTS spec_model_text varchar(500),
    ADD COLUMN IF NOT EXISTS attribute_group_id bigint,
    ADD COLUMN IF NOT EXISTS attribute_group_code varchar(80),
    ADD COLUMN IF NOT EXISTS attribute_group_name_cn varchar(200),
    ADD COLUMN IF NOT EXISTS material_type_id bigint,
    ADD COLUMN IF NOT EXISTS material_type_code varchar(80),
    ADD COLUMN IF NOT EXISTS material_type_name_cn varchar(200),
    ADD COLUMN IF NOT EXISTS unit_code varchar(80),
    ADD COLUMN IF NOT EXISTS default_flag boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS required_flag boolean NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS usage_mode varchar(40) NOT NULL DEFAULT 'FIXED',
    ADD COLUMN IF NOT EXISTS usage_formula text,
    ADD COLUMN IF NOT EXISTS fixed_usage_qty numeric(18,6),
    ADD COLUMN IF NOT EXISTS calculation_unit_code varchar(80),
    ADD COLUMN IF NOT EXISTS rounding_mode varchar(40),
    ADD COLUMN IF NOT EXISTS min_usage_qty numeric(18,6),
    ADD COLUMN IF NOT EXISTS max_usage_qty numeric(18,6),
    ADD COLUMN IF NOT EXISTS loss_rate numeric(10,6),
    ADD COLUMN IF NOT EXISTS production_remark varchar(500),
    ADD COLUMN IF NOT EXISTS status varchar(20) NOT NULL DEFAULT 'ENABLED',
    ADD COLUMN IF NOT EXISTS del_flag varchar(1) NOT NULL DEFAULT '0',
    ADD COLUMN IF NOT EXISTS sort_order int NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS remark varchar(500);

COMMENT ON TABLE pc_formula_material IS '产品配方原料池草稿表';
COMMENT ON COLUMN pc_formula_material.formula_material_id IS '配方原料池ID';
COMMENT ON COLUMN pc_formula_material.formula_id IS '配方ID';
COMMENT ON COLUMN pc_formula_material.material_code IS '物料编码快照';
COMMENT ON COLUMN pc_formula_material.attribute_group_code IS '属性分组编码快照';
COMMENT ON COLUMN pc_formula_material.material_type_code IS '物料类型编码快照';
COMMENT ON COLUMN pc_formula_material.usage_mode IS '用量方式：FIXED、FORMULA';
COMMENT ON COLUMN pc_formula_material.usage_formula IS '用量公式';
COMMENT ON COLUMN pc_formula_material.calculation_unit_code IS '计算单位编码，来源 pc_unit';
CREATE INDEX IF NOT EXISTS idx_pc_formula_material_formula_sort ON pc_formula_material (tenant_id, formula_id, sort_order, formula_material_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_formula_material_code ON pc_formula_material (tenant_id, formula_id, material_code) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS pc_formula_usage_rule (
    usage_rule_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    formula_id bigint NOT NULL,
    formula_material_id bigint,
    material_id bigint,
    material_code varchar(80) NOT NULL,
    material_name_cn varchar(200),
    rule_name varchar(200),
    condition_type varchar(40) NOT NULL DEFAULT 'OPTION_VALUE',
    condition_option_code varchar(80),
    condition_option_name_cn varchar(200),
    condition_value_code varchar(80),
    condition_value_name_cn varchar(200),
    condition_expression text,
    condition_text varchar(500),
    condition_key varchar(300),
    usage_mode varchar(40) NOT NULL DEFAULT 'FIXED',
    fixed_usage_qty numeric(18,6),
    length_formula text,
    length_formula_text text,
    width_formula text,
    width_formula_text text,
    height_formula text,
    height_formula_text text,
    weight_formula text,
    weight_formula_text text,
    usage_formula text,
    usage_formula_text text,
    calculation_unit_code varchar(80),
    rounding_mode varchar(40),
    min_usage_qty numeric(18,6),
    max_usage_qty numeric(18,6),
    loss_rate numeric(10,6),
    default_rule_flag boolean NOT NULL DEFAULT false,
    production_remark varchar(500),
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    sort_order int NOT NULL DEFAULT 0,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_formula_usage_rule
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    ADD COLUMN IF NOT EXISTS formula_id bigint,
    ADD COLUMN IF NOT EXISTS formula_material_id bigint,
    ADD COLUMN IF NOT EXISTS material_id bigint,
    ADD COLUMN IF NOT EXISTS material_code varchar(80),
    ADD COLUMN IF NOT EXISTS material_name_cn varchar(200),
    ADD COLUMN IF NOT EXISTS rule_name varchar(200),
    ADD COLUMN IF NOT EXISTS condition_type varchar(40) NOT NULL DEFAULT 'OPTION_VALUE',
    ADD COLUMN IF NOT EXISTS condition_option_code varchar(80),
    ADD COLUMN IF NOT EXISTS condition_option_name_cn varchar(200),
    ADD COLUMN IF NOT EXISTS condition_value_code varchar(80),
    ADD COLUMN IF NOT EXISTS condition_value_name_cn varchar(200),
    ADD COLUMN IF NOT EXISTS condition_expression text,
    ADD COLUMN IF NOT EXISTS condition_text varchar(500),
    ADD COLUMN IF NOT EXISTS condition_key varchar(300),
    ADD COLUMN IF NOT EXISTS usage_mode varchar(40) NOT NULL DEFAULT 'FIXED',
    ADD COLUMN IF NOT EXISTS fixed_usage_qty numeric(18,6),
    ADD COLUMN IF NOT EXISTS length_formula text,
    ADD COLUMN IF NOT EXISTS length_formula_text text,
    ADD COLUMN IF NOT EXISTS width_formula text,
    ADD COLUMN IF NOT EXISTS width_formula_text text,
    ADD COLUMN IF NOT EXISTS height_formula text,
    ADD COLUMN IF NOT EXISTS height_formula_text text,
    ADD COLUMN IF NOT EXISTS weight_formula text,
    ADD COLUMN IF NOT EXISTS weight_formula_text text,
    ADD COLUMN IF NOT EXISTS usage_formula text,
    ADD COLUMN IF NOT EXISTS usage_formula_text text,
    ADD COLUMN IF NOT EXISTS calculation_unit_code varchar(80),
    ADD COLUMN IF NOT EXISTS rounding_mode varchar(40),
    ADD COLUMN IF NOT EXISTS min_usage_qty numeric(18,6),
    ADD COLUMN IF NOT EXISTS max_usage_qty numeric(18,6),
    ADD COLUMN IF NOT EXISTS loss_rate numeric(10,6),
    ADD COLUMN IF NOT EXISTS default_rule_flag boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS production_remark varchar(500),
    ADD COLUMN IF NOT EXISTS status varchar(20) NOT NULL DEFAULT 'ENABLED',
    ADD COLUMN IF NOT EXISTS del_flag varchar(1) NOT NULL DEFAULT '0',
    ADD COLUMN IF NOT EXISTS sort_order int NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS remark varchar(500);

ALTER TABLE IF EXISTS pc_formula_usage_rule
    DROP COLUMN IF EXISTS width_deduct_mm,
    DROP COLUMN IF EXISTS height_deduct_mm;

COMMENT ON TABLE pc_formula_usage_rule IS '产品配方条件用量规则草稿表';
COMMENT ON COLUMN pc_formula_usage_rule.usage_rule_id IS '条件用量规则ID';
COMMENT ON COLUMN pc_formula_usage_rule.formula_material_id IS '配方原料池ID';
COMMENT ON COLUMN pc_formula_usage_rule.condition_type IS '条件类型：DEFAULT、OPTION_VALUE、EXPRESSION';
COMMENT ON COLUMN pc_formula_usage_rule.condition_option_code IS '适用配置项编码，如 FABRIC';
COMMENT ON COLUMN pc_formula_usage_rule.condition_value_code IS '适用配置项值，如面料物料编码';
COMMENT ON COLUMN pc_formula_usage_rule.condition_expression IS '内部条件表达式，如 fabric == "XLF241801"';
COMMENT ON COLUMN pc_formula_usage_rule.condition_text IS '条件展示文本，如 面料 = XLF241801';
COMMENT ON COLUMN pc_formula_usage_rule.condition_key IS '条件唯一键，默认 DEFAULT 或 OPTION:{option}:{value}';
COMMENT ON COLUMN pc_formula_usage_rule.usage_mode IS '用量方式：FIXED、FORMULA';
COMMENT ON COLUMN pc_formula_usage_rule.length_formula IS '长度公式内部表达式';
COMMENT ON COLUMN pc_formula_usage_rule.length_formula_text IS '长度公式展示文本';
COMMENT ON COLUMN pc_formula_usage_rule.width_formula IS '宽度公式内部表达式';
COMMENT ON COLUMN pc_formula_usage_rule.width_formula_text IS '宽度公式展示文本';
COMMENT ON COLUMN pc_formula_usage_rule.height_formula IS '高度或厚度公式内部表达式';
COMMENT ON COLUMN pc_formula_usage_rule.height_formula_text IS '高度或厚度公式展示文本';
COMMENT ON COLUMN pc_formula_usage_rule.weight_formula IS '重量公式内部表达式';
COMMENT ON COLUMN pc_formula_usage_rule.weight_formula_text IS '重量公式展示文本';
COMMENT ON COLUMN pc_formula_usage_rule.usage_formula_text IS '用量公式展示文本，保留技术科输入口径';
COMMENT ON COLUMN pc_formula_usage_rule.default_rule_flag IS '默认规则标记，条件未命中时使用';
CREATE INDEX IF NOT EXISTS idx_pc_formula_usage_rule_material ON pc_formula_usage_rule (tenant_id, formula_id, formula_material_id, status) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_formula_usage_rule_code ON pc_formula_usage_rule (tenant_id, formula_id, material_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_formula_usage_rule_condition ON pc_formula_usage_rule (tenant_id, formula_id, condition_option_code, condition_value_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_formula_usage_rule_condition_key ON pc_formula_usage_rule (tenant_id, formula_id, material_code, condition_key, status) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS pc_formula_variable (
    variable_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    formula_id bigint NOT NULL,
    variable_key varchar(80) NOT NULL,
    variable_code varchar(80) NOT NULL,
    variable_name varchar(200) NOT NULL,
    del_flag varchar(1) NOT NULL DEFAULT '0',
    sort_order int NOT NULL DEFAULT 0,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_formula_variable
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    ADD COLUMN IF NOT EXISTS formula_id bigint,
    ADD COLUMN IF NOT EXISTS variable_key varchar(80),
    ADD COLUMN IF NOT EXISTS variable_code varchar(80),
    ADD COLUMN IF NOT EXISTS variable_name varchar(200),
    ADD COLUMN IF NOT EXISTS del_flag varchar(1) NOT NULL DEFAULT '0',
    ADD COLUMN IF NOT EXISTS sort_order int NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS remark varchar(500);

COMMENT ON TABLE pc_formula_variable IS '产品配方内部变量草稿表';
COMMENT ON COLUMN pc_formula_variable.variable_id IS '内部变量ID';
COMMENT ON COLUMN pc_formula_variable.formula_id IS '配方ID';
COMMENT ON COLUMN pc_formula_variable.variable_key IS '变量隐藏引用键，公式内部使用 var_变量键';
COMMENT ON COLUMN pc_formula_variable.variable_code IS '变量编码，人工识别使用，不作为公式唯一引用';
COMMENT ON COLUMN pc_formula_variable.variable_name IS '变量名称，技术科公式展示使用';
CREATE INDEX IF NOT EXISTS idx_pc_formula_variable_formula_sort ON pc_formula_variable (tenant_id, formula_id, sort_order, variable_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_formula_variable_code ON pc_formula_variable (tenant_id, formula_id, variable_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_formula_variable_key ON pc_formula_variable (tenant_id, formula_id, variable_key) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS pc_formula_variable_rule (
    rule_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    formula_id bigint NOT NULL,
    variable_id bigint,
    variable_key varchar(80) NOT NULL,
    variable_code varchar(80) NOT NULL,
    condition_expression text,
    condition_text varchar(500),
    value_type varchar(40) NOT NULL DEFAULT 'FIXED',
    fixed_value numeric(18,6),
    formula_expression text,
    formula_text text,
    default_rule_flag boolean NOT NULL DEFAULT false,
    del_flag varchar(1) NOT NULL DEFAULT '0',
    sort_order int NOT NULL DEFAULT 0,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_formula_variable_rule
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    ADD COLUMN IF NOT EXISTS formula_id bigint,
    ADD COLUMN IF NOT EXISTS variable_id bigint,
    ADD COLUMN IF NOT EXISTS variable_key varchar(80),
    ADD COLUMN IF NOT EXISTS variable_code varchar(80),
    ADD COLUMN IF NOT EXISTS condition_expression text,
    ADD COLUMN IF NOT EXISTS condition_text varchar(500),
    ADD COLUMN IF NOT EXISTS value_type varchar(40) NOT NULL DEFAULT 'FIXED',
    ADD COLUMN IF NOT EXISTS fixed_value numeric(18,6),
    ADD COLUMN IF NOT EXISTS formula_expression text,
    ADD COLUMN IF NOT EXISTS formula_text text,
    ADD COLUMN IF NOT EXISTS default_rule_flag boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS del_flag varchar(1) NOT NULL DEFAULT '0',
    ADD COLUMN IF NOT EXISTS sort_order int NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS remark varchar(500);

COMMENT ON TABLE pc_formula_variable_rule IS '产品配方内部变量取值规则草稿表';
COMMENT ON COLUMN pc_formula_variable_rule.rule_id IS '内部变量规则ID';
COMMENT ON COLUMN pc_formula_variable_rule.variable_key IS '变量隐藏引用键快照';
COMMENT ON COLUMN pc_formula_variable_rule.variable_code IS '变量编码快照';
COMMENT ON COLUMN pc_formula_variable_rule.condition_expression IS '规则内部条件表达式';
COMMENT ON COLUMN pc_formula_variable_rule.value_type IS '取值方式：FIXED、FORMULA';
COMMENT ON COLUMN pc_formula_variable_rule.default_rule_flag IS '默认规则标记';
CREATE INDEX IF NOT EXISTS idx_pc_formula_variable_rule_formula_sort ON pc_formula_variable_rule (tenant_id, formula_id, variable_key, sort_order, rule_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_formula_variable_rule_variable ON pc_formula_variable_rule (tenant_id, formula_id, variable_id) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS pc_formula_option (
    option_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    formula_id bigint NOT NULL,
    option_code varchar(80) NOT NULL,
    option_name_cn varchar(200) NOT NULL,
    option_name_en varchar(200),
    source_type varchar(40) NOT NULL DEFAULT 'MANUAL',
    source_scope varchar(300),
    selection_mode varchar(40) NOT NULL DEFAULT 'SINGLE',
    display_mode varchar(40) NOT NULL DEFAULT 'SELECT',
    default_value_code varchar(80),
    default_value_name_cn varchar(200),
    visibility_mode varchar(40) NOT NULL DEFAULT 'ALWAYS',
    visible_condition_option_code varchar(80),
    visible_condition_option_name_cn varchar(200),
    visible_condition_value_code varchar(80),
    visible_condition_value_name_cn varchar(200),
    required_flag boolean NOT NULL DEFAULT false,
    business_visible_flag boolean NOT NULL DEFAULT true,
    help_enabled boolean NOT NULL DEFAULT false,
    help_type varchar(30) NOT NULL DEFAULT 'LINK',
    help_title varchar(200),
    help_url varchar(500),
    help_content text,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    sort_order int NOT NULL DEFAULT 0,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_formula_option IS '产品配方业务配置项草稿表';
ALTER TABLE IF EXISTS pc_formula_option ADD COLUMN IF NOT EXISTS option_name_en varchar(200);
ALTER TABLE IF EXISTS pc_formula_option ADD COLUMN IF NOT EXISTS display_mode varchar(40) NOT NULL DEFAULT 'SELECT';
ALTER TABLE IF EXISTS pc_formula_option ADD COLUMN IF NOT EXISTS visibility_mode varchar(40) NOT NULL DEFAULT 'ALWAYS';
ALTER TABLE IF EXISTS pc_formula_option ADD COLUMN IF NOT EXISTS visible_condition_option_code varchar(80);
ALTER TABLE IF EXISTS pc_formula_option ADD COLUMN IF NOT EXISTS visible_condition_option_name_cn varchar(200);
ALTER TABLE IF EXISTS pc_formula_option ADD COLUMN IF NOT EXISTS visible_condition_value_code varchar(80);
ALTER TABLE IF EXISTS pc_formula_option ADD COLUMN IF NOT EXISTS visible_condition_value_name_cn varchar(200);
ALTER TABLE IF EXISTS pc_formula_option ADD COLUMN IF NOT EXISTS required_flag boolean NOT NULL DEFAULT false;
ALTER TABLE IF EXISTS pc_formula_option ADD COLUMN IF NOT EXISTS business_visible_flag boolean NOT NULL DEFAULT true;
ALTER TABLE IF EXISTS pc_formula_option ADD COLUMN IF NOT EXISTS help_enabled boolean NOT NULL DEFAULT false;
ALTER TABLE IF EXISTS pc_formula_option ADD COLUMN IF NOT EXISTS help_type varchar(30) NOT NULL DEFAULT 'LINK';
ALTER TABLE IF EXISTS pc_formula_option ADD COLUMN IF NOT EXISTS help_title varchar(200);
ALTER TABLE IF EXISTS pc_formula_option ADD COLUMN IF NOT EXISTS help_url varchar(500);
ALTER TABLE IF EXISTS pc_formula_option ADD COLUMN IF NOT EXISTS help_content text;
COMMENT ON COLUMN pc_formula_option.visibility_mode IS '订单端显示模式：ALWAYS 始终显示，CONDITIONAL 满足条件显示';
COMMENT ON COLUMN pc_formula_option.display_mode IS '订单端控件展示方式：SELECT 普通下拉，IMAGE_SELECT 图文下拉';
COMMENT ON COLUMN pc_formula_option.visible_condition_option_code IS '条件显示依赖配置项编码';
COMMENT ON COLUMN pc_formula_option.visible_condition_option_name_cn IS '条件显示依赖配置项名称快照';
COMMENT ON COLUMN pc_formula_option.visible_condition_value_code IS '条件显示依赖选项值编码';
COMMENT ON COLUMN pc_formula_option.visible_condition_value_name_cn IS '条件显示依赖选项值名称快照';
COMMENT ON COLUMN pc_formula_option.required_flag IS '下单必填标记';
COMMENT ON COLUMN pc_formula_option.business_visible_flag IS '业务下单可见标记';
COMMENT ON COLUMN pc_formula_option.help_enabled IS '订单端帮助开关';
COMMENT ON COLUMN pc_formula_option.help_type IS '订单端帮助类型：LINK 超链接，TEXT 纯文本说明';
COMMENT ON COLUMN pc_formula_option.help_title IS '订单端帮助标题';
COMMENT ON COLUMN pc_formula_option.help_url IS '订单端帮助超链接';
COMMENT ON COLUMN pc_formula_option.help_content IS '订单端帮助纯文本说明';
CREATE INDEX IF NOT EXISTS idx_pc_formula_option_formula_sort ON pc_formula_option (tenant_id, formula_id, sort_order, option_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_formula_option_code ON pc_formula_option (tenant_id, formula_id, option_code) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS pc_formula_option_value (
    option_value_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    formula_id bigint NOT NULL,
    option_id bigint,
    option_code varchar(80) NOT NULL,
    value_code varchar(80) NOT NULL,
    value_name_cn varchar(200) NOT NULL,
    value_name_en varchar(200),
    default_flag boolean NOT NULL DEFAULT false,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    sort_order int NOT NULL DEFAULT 0,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_formula_option_value IS '产品配方业务配置项可选值草稿表';
ALTER TABLE IF EXISTS pc_formula_option_value ADD COLUMN IF NOT EXISTS value_name_en varchar(200);
CREATE INDEX IF NOT EXISTS idx_pc_formula_option_value_formula_sort ON pc_formula_option_value (tenant_id, formula_id, option_code, sort_order, option_value_id) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS pc_formula_option_material (
    option_material_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    formula_id bigint NOT NULL,
    option_id bigint,
    option_value_id bigint,
    option_code varchar(80) NOT NULL,
    value_code varchar(80) NOT NULL,
    formula_material_id bigint,
    material_id bigint,
    material_code varchar(80),
    material_name_cn varchar(200),
    required_flag boolean NOT NULL DEFAULT false,
    default_flag boolean NOT NULL DEFAULT false,
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    sort_order int NOT NULL DEFAULT 0,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_formula_option_material IS '产品配方选项值关联原料池物料草稿表';
CREATE INDEX IF NOT EXISTS idx_pc_formula_option_material_formula_sort ON pc_formula_option_material (tenant_id, formula_id, option_code, value_code, sort_order, option_material_id) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS pc_formula_restriction (
    restriction_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    formula_id bigint NOT NULL,
    restriction_name varchar(200),
    target_option_code varchar(80) NOT NULL,
    condition_type varchar(40) NOT NULL,
    condition_option_code varchar(80),
    condition_operator varchar(40) NOT NULL,
    condition_value_code varchar(80),
    condition_value_number numeric(18,6),
    condition_expression text,
    condition_text varchar(500),
    action_type varchar(40) NOT NULL,
    target_value_code varchar(80),
    message_text varchar(500),
    status varchar(20) NOT NULL DEFAULT 'ENABLED',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    sort_order int NOT NULL DEFAULT 0,
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

COMMENT ON TABLE pc_formula_restriction IS '产品配方限制条件草稿表';
ALTER TABLE IF EXISTS pc_formula_restriction ADD COLUMN IF NOT EXISTS condition_expression text;
ALTER TABLE IF EXISTS pc_formula_restriction ADD COLUMN IF NOT EXISTS condition_text varchar(500);
COMMENT ON COLUMN pc_formula_restriction.condition_expression IS '限制条件表达式，返回 true 时触发限制';
COMMENT ON COLUMN pc_formula_restriction.condition_text IS '限制条件展示文本';
CREATE INDEX IF NOT EXISTS idx_pc_formula_restriction_formula_sort ON pc_formula_restriction (tenant_id, formula_id, sort_order, restriction_id) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS pc_formula_version (
    version_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    formula_id bigint NOT NULL,
    version_no int NOT NULL,
    version_label varchar(40) NOT NULL,
    version_status varchar(30) NOT NULL,
    formula_snapshot_json text,
    setup_snapshot_json text,
    validation_status varchar(30),
    validation_report_json text,
    submit_by varchar(64),
    submit_time timestamptz,
    audit_by varchar(64),
    audit_time timestamptz,
    reject_reason varchar(500),
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_formula_version
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    ADD COLUMN IF NOT EXISTS formula_id bigint,
    ADD COLUMN IF NOT EXISTS version_no int,
    ADD COLUMN IF NOT EXISTS version_label varchar(40),
    ADD COLUMN IF NOT EXISTS version_status varchar(30),
    ADD COLUMN IF NOT EXISTS formula_snapshot_json text,
    ADD COLUMN IF NOT EXISTS setup_snapshot_json text,
    ADD COLUMN IF NOT EXISTS validation_status varchar(30),
    ADD COLUMN IF NOT EXISTS validation_report_json text,
    ADD COLUMN IF NOT EXISTS submit_by varchar(64),
    ADD COLUMN IF NOT EXISTS submit_time timestamptz,
    ADD COLUMN IF NOT EXISTS audit_by varchar(64),
    ADD COLUMN IF NOT EXISTS audit_time timestamptz,
    ADD COLUMN IF NOT EXISTS reject_reason varchar(500),
    ADD COLUMN IF NOT EXISTS del_flag varchar(1) NOT NULL DEFAULT '0',
    ADD COLUMN IF NOT EXISTS remark varchar(500);

COMMENT ON TABLE pc_formula_version IS '产品配方审核版本快照表';
COMMENT ON COLUMN pc_formula_version.version_id IS '版本ID';
COMMENT ON COLUMN pc_formula_version.formula_id IS '配方ID';
COMMENT ON COLUMN pc_formula_version.version_no IS '版本号，系统按配方递增';
COMMENT ON COLUMN pc_formula_version.version_label IS '版本展示号，如 V1、V2';
COMMENT ON COLUMN pc_formula_version.version_status IS '版本状态：PENDING_REVIEW、REJECTED、EFFECTIVE、STOPPED';
COMMENT ON COLUMN pc_formula_version.formula_snapshot_json IS '配方主表快照JSON';
ALTER TABLE IF EXISTS pc_formula_version DROP COLUMN IF EXISTS item_snapshot_json;
COMMENT ON COLUMN pc_formula_version.setup_snapshot_json IS '配方设置快照JSON';
COMMENT ON COLUMN pc_formula_version.validation_status IS '审核时校验状态';
COMMENT ON COLUMN pc_formula_version.validation_report_json IS '审核时校验报告JSON';
COMMENT ON COLUMN pc_formula_version.audit_time IS '审核时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_formula_version_formula_no ON pc_formula_version (tenant_id, formula_id, version_no DESC) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS pc_sale_product (
    sale_product_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    sale_product_code varchar(80) NOT NULL,
    sale_product_name varchar(200) NOT NULL,
    category_id bigint NOT NULL,
    category_code varchar(80) NOT NULL,
    category_name_cn varchar(200) NOT NULL,
    product_type_code varchar(80) NOT NULL,
    product_type_name_cn varchar(200) NOT NULL,
    formula_id bigint NOT NULL,
    formula_code varchar(80) NOT NULL,
    formula_name varchar(200) NOT NULL,
    formula_version_id bigint NOT NULL,
    formula_version_no int NOT NULL,
    formula_version_label varchar(40) NOT NULL,
    min_width_inch numeric(18,4) NOT NULL DEFAULT 0,
    min_height_inch numeric(18,4) NOT NULL DEFAULT 0,
    max_width_inch numeric(18,4) NOT NULL,
    max_height_inch numeric(18,4) NOT NULL,
    size_summary varchar(200),
    price_status varchar(30) NOT NULL DEFAULT 'NOT_READY',
    status varchar(30) NOT NULL DEFAULT 'DISABLED',
    sort_order int NOT NULL DEFAULT 0,
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_sale_product
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    ADD COLUMN IF NOT EXISTS sale_product_code varchar(80),
    ADD COLUMN IF NOT EXISTS sale_product_name varchar(200),
    ADD COLUMN IF NOT EXISTS category_id bigint,
    ADD COLUMN IF NOT EXISTS category_code varchar(80),
    ADD COLUMN IF NOT EXISTS category_name_cn varchar(200),
    ADD COLUMN IF NOT EXISTS product_type_code varchar(80),
    ADD COLUMN IF NOT EXISTS product_type_name_cn varchar(200),
    ADD COLUMN IF NOT EXISTS formula_id bigint,
    ADD COLUMN IF NOT EXISTS formula_code varchar(80),
    ADD COLUMN IF NOT EXISTS formula_name varchar(200),
    ADD COLUMN IF NOT EXISTS formula_version_id bigint,
    ADD COLUMN IF NOT EXISTS formula_version_no int,
    ADD COLUMN IF NOT EXISTS formula_version_label varchar(40),
    ADD COLUMN IF NOT EXISTS min_width_inch numeric(18,4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS min_height_inch numeric(18,4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS max_width_inch numeric(18,4),
    ADD COLUMN IF NOT EXISTS max_height_inch numeric(18,4),
    ADD COLUMN IF NOT EXISTS size_summary varchar(200),
    ADD COLUMN IF NOT EXISTS price_status varchar(30) NOT NULL DEFAULT 'NOT_READY',
    ADD COLUMN IF NOT EXISTS status varchar(30) NOT NULL DEFAULT 'DISABLED',
    ADD COLUMN IF NOT EXISTS sort_order int NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS del_flag varchar(1) NOT NULL DEFAULT '0',
    ADD COLUMN IF NOT EXISTS remark varchar(500);

COMMENT ON TABLE pc_sale_product IS '可售产品表，绑定已生效配方版本，不是订单SKU';
COMMENT ON COLUMN pc_sale_product.sale_product_code IS '可售产品编号，程序校验唯一';
COMMENT ON COLUMN pc_sale_product.formula_version_id IS '绑定的配方生效版本ID';
COMMENT ON COLUMN pc_sale_product.price_status IS '价格状态：NOT_READY、READY、WARNING';
COMMENT ON COLUMN pc_sale_product.status IS '销售状态：ENABLED、DISABLED';
CREATE INDEX IF NOT EXISTS idx_pc_sale_product_code_active ON pc_sale_product (tenant_id, sale_product_code) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_sale_product_category_status ON pc_sale_product (tenant_id, category_id, status) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_sale_product_formula_version ON pc_sale_product (tenant_id, formula_version_id, status) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS pc_price_setting (
    price_setting_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    sale_product_id bigint NOT NULL,
    sale_product_code varchar(80) NOT NULL,
    sale_product_name varchar(200) NOT NULL,
    formula_id bigint NOT NULL,
    formula_version_id bigint NOT NULL,
    formula_version_label varchar(40) NOT NULL,
    currency_code varchar(20) NOT NULL DEFAULT 'USD',
    validation_status varchar(30) NOT NULL DEFAULT 'NOT_READY',
    validation_message varchar(500),
    validation_time timestamptz,
    status varchar(30) NOT NULL DEFAULT 'DRAFT',
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_price_setting
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    ADD COLUMN IF NOT EXISTS sale_product_id bigint,
    ADD COLUMN IF NOT EXISTS sale_product_code varchar(80),
    ADD COLUMN IF NOT EXISTS sale_product_name varchar(200),
    ADD COLUMN IF NOT EXISTS formula_id bigint,
    ADD COLUMN IF NOT EXISTS formula_version_id bigint,
    ADD COLUMN IF NOT EXISTS formula_version_label varchar(40),
    ADD COLUMN IF NOT EXISTS currency_code varchar(20) NOT NULL DEFAULT 'USD',
    ADD COLUMN IF NOT EXISTS validation_status varchar(30) NOT NULL DEFAULT 'NOT_READY',
    ADD COLUMN IF NOT EXISTS validation_message varchar(500),
    ADD COLUMN IF NOT EXISTS validation_time timestamptz,
    ADD COLUMN IF NOT EXISTS status varchar(30) NOT NULL DEFAULT 'DRAFT',
    ADD COLUMN IF NOT EXISTS del_flag varchar(1) NOT NULL DEFAULT '0',
    ADD COLUMN IF NOT EXISTS remark varchar(500);

COMMENT ON TABLE pc_price_setting IS '产品价格设置表，可售产品的价格工作台头表';
COMMENT ON COLUMN pc_price_setting.sale_product_id IS '可售产品ID';
COMMENT ON COLUMN pc_price_setting.formula_version_id IS '价格设置绑定的配方版本ID';
COMMENT ON COLUMN pc_price_setting.validation_time IS '价格校验时间，UTC timestamptz';
CREATE INDEX IF NOT EXISTS idx_pc_price_setting_sale_product ON pc_price_setting (tenant_id, sale_product_id) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_price_setting_version ON pc_price_setting (tenant_id, formula_version_id, status) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS pc_price_fabric_rule (
    fabric_rule_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    price_setting_id bigint NOT NULL,
    sale_product_id bigint NOT NULL,
    formula_version_id bigint NOT NULL,
    material_id bigint,
    material_code varchar(80),
    material_name_cn varchar(200),
    unit_code varchar(40),
    option_combination_key varchar(300),
    option_combination_name varchar(300),
    price_mode varchar(40) NOT NULL DEFAULT 'FORMULA',
    base_price numeric(18,4),
    area_formula text,
    min_bill_area numeric(18,4),
    loss_rate numeric(8,4),
    status varchar(30) NOT NULL DEFAULT 'ENABLED',
    sort_order int NOT NULL DEFAULT 0,
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_price_fabric_rule
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    ADD COLUMN IF NOT EXISTS price_setting_id bigint,
    ADD COLUMN IF NOT EXISTS sale_product_id bigint,
    ADD COLUMN IF NOT EXISTS formula_version_id bigint,
    ADD COLUMN IF NOT EXISTS material_id bigint,
    ADD COLUMN IF NOT EXISTS material_code varchar(80),
    ADD COLUMN IF NOT EXISTS material_name_cn varchar(200),
    ADD COLUMN IF NOT EXISTS unit_code varchar(40),
    ADD COLUMN IF NOT EXISTS option_combination_key varchar(300),
    ADD COLUMN IF NOT EXISTS option_combination_name varchar(300),
    ADD COLUMN IF NOT EXISTS price_mode varchar(40) NOT NULL DEFAULT 'FORMULA',
    ADD COLUMN IF NOT EXISTS base_price numeric(18,4),
    ADD COLUMN IF NOT EXISTS area_formula text,
    ADD COLUMN IF NOT EXISTS min_bill_area numeric(18,4),
    ADD COLUMN IF NOT EXISTS loss_rate numeric(8,4),
    ADD COLUMN IF NOT EXISTS status varchar(30) NOT NULL DEFAULT 'ENABLED',
    ADD COLUMN IF NOT EXISTS sort_order int NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS del_flag varchar(1) NOT NULL DEFAULT '0',
    ADD COLUMN IF NOT EXISTS remark varchar(500);
ALTER TABLE IF EXISTS pc_price_fabric_rule
    ALTER COLUMN price_mode SET DEFAULT 'FORMULA';

COMMENT ON TABLE pc_price_fabric_rule IS '面料价格矩阵表，按配方版本面料和选项组合维护单价与价格公式';
COMMENT ON COLUMN pc_price_fabric_rule.option_combination_key IS '选项组合键，例如 STYLE=ROMAN;CONTROL=MOTORIZED';
COMMENT ON COLUMN pc_price_fabric_rule.option_combination_name IS '选项组合展示名称，例如 柔式电动';
COMMENT ON COLUMN pc_price_fabric_rule.price_mode IS '计价方式：FORMULA';
COMMENT ON COLUMN pc_price_fabric_rule.area_formula IS '价格公式文本，使用 unitPrice、width、drop 等变量';
CREATE INDEX IF NOT EXISTS idx_pc_price_fabric_rule_setting ON pc_price_fabric_rule (tenant_id, price_setting_id, sort_order) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_price_fabric_rule_material ON pc_price_fabric_rule (tenant_id, material_code, status) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_price_fabric_rule_combo ON pc_price_fabric_rule (tenant_id, price_setting_id, material_code, option_combination_key) WHERE del_flag = '0';

CREATE TABLE IF NOT EXISTS pc_price_fee_rule (
    fee_rule_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    price_setting_id bigint NOT NULL,
    sale_product_id bigint NOT NULL,
    formula_version_id bigint NOT NULL,
    fee_code varchar(80),
    fee_name varchar(200),
    fee_category varchar(80),
    trigger_condition text,
    fee_mode varchar(40) NOT NULL DEFAULT 'FORMULA',
    fee_amount numeric(18,4),
    formula_text text,
    status varchar(30) NOT NULL DEFAULT 'ENABLED',
    sort_order int NOT NULL DEFAULT 0,
    del_flag varchar(1) NOT NULL DEFAULT '0',
    remark varchar(500),
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz,
    update_by varchar(64),
    update_time timestamptz
);

ALTER TABLE IF EXISTS pc_price_fee_rule
    ADD COLUMN IF NOT EXISTS tenant_id bigint NOT NULL DEFAULT 1 CHECK (tenant_id <> 0),
    ADD COLUMN IF NOT EXISTS price_setting_id bigint,
    ADD COLUMN IF NOT EXISTS sale_product_id bigint,
    ADD COLUMN IF NOT EXISTS formula_version_id bigint,
    ADD COLUMN IF NOT EXISTS fee_code varchar(80),
    ADD COLUMN IF NOT EXISTS fee_name varchar(200),
    ADD COLUMN IF NOT EXISTS fee_category varchar(80),
    ADD COLUMN IF NOT EXISTS trigger_condition text,
    ADD COLUMN IF NOT EXISTS fee_mode varchar(40) NOT NULL DEFAULT 'FORMULA',
    ADD COLUMN IF NOT EXISTS fee_amount numeric(18,4),
    ADD COLUMN IF NOT EXISTS formula_text text,
    ADD COLUMN IF NOT EXISTS status varchar(30) NOT NULL DEFAULT 'ENABLED',
    ADD COLUMN IF NOT EXISTS sort_order int NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS del_flag varchar(1) NOT NULL DEFAULT '0',
    ADD COLUMN IF NOT EXISTS remark varchar(500);
ALTER TABLE IF EXISTS pc_price_fee_rule
    ALTER COLUMN fee_mode SET DEFAULT 'FORMULA';

COMMENT ON TABLE pc_price_fee_rule IS '邮费公式表，按不带电/带电维护运输费用公式';
COMMENT ON COLUMN pc_price_fee_rule.fee_code IS '邮费类型：MANUAL 不带电、MOTORIZED 带电';
COMMENT ON COLUMN pc_price_fee_rule.fee_mode IS '费用方式：FORMULA';
CREATE INDEX IF NOT EXISTS idx_pc_price_fee_rule_setting ON pc_price_fee_rule (tenant_id, price_setting_id, sort_order) WHERE del_flag = '0';
CREATE INDEX IF NOT EXISTS idx_pc_price_fee_rule_category ON pc_price_fee_rule (tenant_id, fee_category, status) WHERE del_flag = '0';

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
    formula_summary_visible_flag boolean NOT NULL DEFAULT true,
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
ALTER TABLE IF EXISTS pc_material_type_group
    ADD COLUMN IF NOT EXISTS formula_summary_visible_flag boolean NOT NULL DEFAULT true;
COMMENT ON TABLE pc_material_type_group IS '物料属性分组主数据表';
COMMENT ON COLUMN pc_material_type_group.group_code IS '属性分组编码';
COMMENT ON COLUMN pc_material_type_group.group_name_cn IS '属性分组中文名称';
COMMENT ON COLUMN pc_material_type_group.formula_summary_visible_flag IS '是否在配方设置顶部统计卡展示';
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
    sales_price numeric(18,4),
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
    ADD COLUMN IF NOT EXISTS sales_price numeric(18,4),
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
COMMENT ON COLUMN pc_material.sales_price IS '销售价';
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
    (121001, 1, 'FABRIC', '面料', 'Fabric', false, true, 'ENABLED', 10, '面料物料大类', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (121002, 1, 'ALUMINUM', '铝材', 'Aluminum', false, true, 'ENABLED', 20, '铝材、下杆、轨道等归入铝材', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (121003, 1, 'SYSTEM', '系统', 'System', false, true, 'ENABLED', 30, '电机、遥控、控制器等归入系统', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (121004, 1, 'ACCESSORY', '配件', 'Accessory', false, true, 'ENABLED', 40, '安装件、支架、胶条等配件', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (121005, 1, 'PART_PACK', '零件包', 'Parts Pack', false, true, 'ENABLED', 50, '配方中的零件包类物料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (121006, 1, 'PACKAGING', '包装', 'Packaging', false, true, 'ENABLED', 60, '纸箱、PET盒等包装物料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00')
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
    (122001, 1, 'FABRIC', '面料', 'Fabric', 121001, 'FABRIC', '面料', false, true, 'ENABLED', 10, '布料、纱帘、底布等面料类物料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (122101, 1, 'BOTTOM_BAR', '下杆', 'Bottom Bar', 121002, 'ALUMINUM', '铝材', false, true, 'ENABLED', 10, '成品或定制帘下杆铝材', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122102, 1, 'TOP_TUBE', '上杆/卷管', 'Top Tube', 121002, 'ALUMINUM', '铝材', false, true, 'ENABLED', 20, '上杆、卷管、套管类铝材', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122103, 1, 'COVER_SHELL', '罩壳', 'Cover Shell', 121002, 'ALUMINUM', '铝材', false, true, 'ENABLED', 30, '罩壳、外壳类铝材', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122104, 1, 'SIDE_COVER', '边盖', 'Side Cover', 121002, 'ALUMINUM', '铝材', false, true, 'ENABLED', 40, '边盖、端盖类铝材', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122105, 1, 'CHANNEL', '轨道', 'Channel', 121002, 'ALUMINUM', '铝材', false, true, 'ENABLED', 50, '轨道、槽道类铝材', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122201, 1, 'PULL_SYSTEM', '拉珠系统', 'Pull System', 121003, 'SYSTEM', '系统', false, true, 'ENABLED', 10, '拉珠、珠链、制头等手动控制系统', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122202, 1, 'MOTOR', '电机', 'Motor', 121003, 'SYSTEM', '系统', false, true, 'ENABLED', 20, '电机、管状电机等电动控制系统', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122203, 1, 'REMOTE_CONTROLLER', '遥控器', 'Remote Controller', 121003, 'SYSTEM', '系统', false, true, 'ENABLED', 30, '遥控器、发射器', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122204, 1, 'CONTROLLER', '控制器', 'Controller', 121003, 'SYSTEM', '系统', false, true, 'ENABLED', 40, '控制器、接收器、网关', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122205, 1, 'LIMITER', '限位器', 'Limiter', 121003, 'SYSTEM', '系统', false, true, 'ENABLED', 50, '限位器、止停件', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122301, 1, 'BRACKET', '支架', 'Bracket', 121004, 'ACCESSORY', '配件', false, true, 'ENABLED', 10, '安装支架、托架', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122302, 1, 'SCREW', '螺丝', 'Screw', 121004, 'ACCESSORY', '配件', false, true, 'ENABLED', 20, '螺丝、螺钉、螺母', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122303, 1, 'GLUE_STRIP', '胶条', 'Glue Strip', 121004, 'ACCESSORY', '配件', false, true, 'ENABLED', 30, '胶条、背胶、粘贴耗材', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122304, 1, 'HANDLE', '手柄', 'Handle', 121004, 'ACCESSORY', '配件', false, true, 'ENABLED', 40, '手柄、拉手、操作件', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122305, 1, 'INSTALL_CODE', '安装码', 'Install Code', 121004, 'ACCESSORY', '配件', false, true, 'ENABLED', 50, '安装码、固定码、安装配件', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122401, 1, 'INSTALL_KIT', '安装包', 'Install Kit', 121005, 'PART_PACK', '零件包', false, true, 'ENABLED', 10, '安装零件包、配件包', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122402, 1, 'SPARE_PART_PACK', '备件包', 'Spare Part Pack', 121005, 'PART_PACK', '零件包', false, true, 'ENABLED', 20, '备件包、维修包', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122501, 1, 'CARTON', '纸箱', 'Carton', 121006, 'PACKAGING', '包装', false, true, 'ENABLED', 10, '外箱、纸箱包装', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122502, 1, 'PET_BOX', 'PET盒', 'PET Box', 121006, 'PACKAGING', '包装', false, true, 'ENABLED', 20, 'PET盒、透明盒', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122503, 1, 'MANUAL', '说明书', 'Manual', 121006, 'PACKAGING', '包装', false, true, 'ENABLED', 30, '说明书、资料页', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122504, 1, 'LABEL', '标贴', 'Label', 121006, 'PACKAGING', '包装', false, true, 'ENABLED', 40, '标签、标贴、合格证', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00'),
    (122505, 1, 'BAG', '包装袋', 'Bag', 121006, 'PACKAGING', '包装', false, true, 'ENABLED', 50, '包装袋、保护袋', '0', 'system', '2026-06-26 00:00:00+00', 'system', '2026-06-26 00:00:00+00')
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

INSERT INTO pc_base_attribute (
    attribute_id, tenant_id, attribute_group_code, attribute_group_name_cn,
    attribute_code, attribute_name_cn, attribute_name_en, value_type, unit_code, extra_json,
    sort_order, status, del_flag, remark, create_by, create_time, update_by, update_time
) VALUES
    (123001, 1, 'FABRIC', '面料', 'THICKNESS', '厚度', 'Thickness', 'NUMBER', 'MM', '{"formulaUsable": true}'::jsonb, 10, 'ENABLED', '0', '配方限制和规则计算常用属性', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00'),
    (123002, 1, 'FABRIC', '面料', 'FABRIC_WIDTH', '幅宽', 'Fabric Width', 'NUMBER', 'MM', '{"formulaUsable": true}'::jsonb, 20, 'ENABLED', '0', '面料门幅/幅宽，用于用量和限制规则', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00'),
    (123003, 1, 'FABRIC', '面料', 'GSM', '克重', 'GSM', 'NUMBER', 'GSM', '{"formulaUsable": true}'::jsonb, 30, 'ENABLED', '0', '面料克重，用于重量估算和限制规则', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00'),
    (123101, 1, 'ALUMINUM', '铝材', 'LENGTH', '长度', 'Length', 'NUMBER', 'MM', '{"formulaUsable": true}'::jsonb, 10, 'ENABLED', '0', '下杆、上杆、罩壳等铝材长度属性', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00'),
    (123102, 1, 'ALUMINUM', '铝材', 'WEIGHT', '重量', 'Weight', 'NUMBER', 'KG', '{"formulaUsable": true}'::jsonb, 20, 'ENABLED', '0', '罩壳、下杆、边盖等铝材重量属性', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00'),
    (123201, 1, 'SYSTEM', '系统', 'LOAD_CAPACITY', '承重', 'Load Capacity', 'NUMBER', 'KG', '{"formulaUsable": true}'::jsonb, 10, 'ENABLED', '0', '电机、拉珠系统等承重限制属性', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00'),
    (123202, 1, 'SYSTEM', '系统', 'CHANNEL_COUNT', '通道数', 'Channel Count', 'NUMBER', NULL, '{"formulaUsable": true}'::jsonb, 20, 'ENABLED', '0', '遥控器、控制器通道数量属性', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00'),
    (123203, 1, 'SYSTEM', '系统', 'RATED_POWER', '额定功率', 'Rated Power', 'NUMBER', NULL, '{"formulaUsable": true}'::jsonb, 30, 'ENABLED', '0', '电机功率属性，单位由业务备注或后续单位扩展明确', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00'),
    (123301, 1, 'ACCESSORY', '配件', 'LENGTH', '长度', 'Length', 'NUMBER', 'MM', '{"formulaUsable": true}'::jsonb, 10, 'ENABLED', '0', '胶条、安装码、手柄等配件长度属性', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00'),
    (123302, 1, 'ACCESSORY', '配件', 'WEIGHT', '重量', 'Weight', 'NUMBER', 'KG', '{"formulaUsable": true}'::jsonb, 20, 'ENABLED', '0', '支架、安装码、手柄等配件重量属性', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00'),
    (123401, 1, 'PART_PACK', '零件包', 'WEIGHT', '重量', 'Weight', 'NUMBER', 'KG', '{"formulaUsable": true}'::jsonb, 10, 'ENABLED', '0', '安装包、备件包重量属性', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00'),
    (123501, 1, 'PACKAGING', '包装', 'LENGTH', '长度', 'Length', 'NUMBER', 'MM', '{"formulaUsable": true}'::jsonb, 10, 'ENABLED', '0', '纸箱、PET盒等包装长度属性', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00'),
    (123502, 1, 'PACKAGING', '包装', 'WIDTH', '宽度', 'Width', 'NUMBER', 'MM', '{"formulaUsable": true}'::jsonb, 20, 'ENABLED', '0', '纸箱、PET盒等包装宽度属性', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00'),
    (123503, 1, 'PACKAGING', '包装', 'HEIGHT', '高度', 'Height', 'NUMBER', 'MM', '{"formulaUsable": true}'::jsonb, 30, 'ENABLED', '0', '纸箱、PET盒等包装高度属性', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00'),
    (123504, 1, 'PACKAGING', '包装', 'WEIGHT', '重量', 'Weight', 'NUMBER', 'KG', '{"formulaUsable": true}'::jsonb, 40, 'ENABLED', '0', '包装物料重量属性', 'system', '2026-07-03 00:00:00+00', 'system', '2026-07-03 00:00:00+00')
ON CONFLICT (attribute_id) DO UPDATE
SET attribute_group_code = EXCLUDED.attribute_group_code,
    attribute_group_name_cn = EXCLUDED.attribute_group_name_cn,
    attribute_code = EXCLUDED.attribute_code,
    attribute_name_cn = EXCLUDED.attribute_name_cn,
    attribute_name_en = EXCLUDED.attribute_name_en,
    value_type = EXCLUDED.value_type,
    unit_code = EXCLUDED.unit_code,
    extra_json = EXCLUDED.extra_json,
    sort_order = EXCLUDED.sort_order,
    status = EXCLUDED.status,
    del_flag = EXCLUDED.del_flag,
    remark = EXCLUDED.remark,
    update_by = 'system',
    update_time = now();

DELETE FROM pc_product_dict_item
WHERE dict_type_code LIKE 'engineering\_%' ESCAPE '\'
   OR dict_type_code LIKE 'config\_%' ESCAPE '\';

DELETE FROM pc_product_dict_type
WHERE dict_type_code LIKE 'engineering\_%' ESCAPE '\'
   OR dict_type_code LIKE 'config\_%' ESCAPE '\';

INSERT INTO pc_product_dict_type (
    dict_type_id, tenant_id, dict_type_code, dict_type_name_cn, dict_type_name_en, business_domain,
    system_flag, editable_flag, status, sort_order, remark, del_flag, create_by, create_time, update_by, update_time
) VALUES
    (118001, 1, 'product_unit_type', '单位类型', 'Unit Type', 'BASE', false, true, 'ENABLED', 10, '单位分类枚举，具体单位仍维护在 pc_unit', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118003, 1, 'product_business_type', '业务类型', 'Business Type', 'BASE', false, true, 'ENABLED', 30, '产品业务口径类型', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (118004, 1, 'product_type', '产品类型', 'Product Type', 'FORMULA', false, true, 'ENABLED', 40, '配方主表产品类型，如成品帘、定制帘', '0', 'system', '2026-06-25 00:00:00+00', 'system', '2026-06-25 00:00:00+00'),
    (118005, 1, 'product_asset_type', '资料类型', 'Asset Type', 'BASE', false, true, 'ENABLED', 50, '资料资产类型', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00')
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
    (119001, 1, 'product_unit_type', 'LENGTH', '长度', 'Length', NULL, false, true, 'ENABLED', 10, '长度单位分类', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119002, 1, 'product_unit_type', 'COUNT', '数量', 'Count', NULL, false, true, 'ENABLED', 20, '计数单位分类', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119003, 1, 'product_unit_type', 'AREA', '面积', 'Area', NULL, false, true, 'ENABLED', 30, '面积单位分类', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119004, 1, 'product_unit_type', 'WEIGHT', '重量', 'Weight', NULL, false, true, 'ENABLED', 40, '重量单位分类', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119005, 1, 'product_unit_type', 'VOLUME', '体积', 'Volume', NULL, false, true, 'ENABLED', 50, '体积单位分类', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119201, 1, 'product_business_type', 'ROLLER_SHADE', '卷帘', 'Roller Shade', NULL, false, true, 'ENABLED', 10, '卷帘', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119202, 1, 'product_business_type', 'ZEBRA_SHADE', '斑马帘/柔纱帘', 'Zebra Shade', NULL, false, true, 'ENABLED', 20, '斑马帘/柔纱帘', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119203, 1, 'product_business_type', 'OUTDOOR_SHADE', '户外遮阳', 'Outdoor Shade', NULL, false, true, 'ENABLED', 30, '户外遮阳', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119204, 1, 'product_business_type', 'CURTAIN_TRACK', '轨道窗帘', 'Curtain Track', NULL, false, true, 'ENABLED', 40, '轨道窗帘', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119205, 1, 'product_business_type', 'HONEYCOMB_SHADE', '蜂巢帘', 'Honeycomb Shade', NULL, false, true, 'ENABLED', 50, '蜂巢帘', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119301, 1, 'product_type', 'FINISHED_CURTAIN', '成品帘', 'Finished Curtain', NULL, false, true, 'ENABLED', 10, '固定尺寸或标准成品产品', '0', 'system', '2026-06-25 00:00:00+00', 'system', '2026-06-25 00:00:00+00'),
    (119302, 1, 'product_type', 'CUSTOM_CURTAIN', '定制帘', 'Custom Curtain', NULL, false, true, 'ENABLED', 20, '按订单尺寸和选项定制产品', '0', 'system', '2026-06-25 00:00:00+00', 'system', '2026-06-25 00:00:00+00'),
    (119401, 1, 'product_asset_type', 'IMAGE', '图片', 'Image', NULL, false, true, 'ENABLED', 10, '图片', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119402, 1, 'product_asset_type', 'PDF', 'PDF', 'PDF', NULL, false, true, 'ENABLED', 20, 'PDF', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119403, 1, 'product_asset_type', 'SPEC', '规格书', 'Specification', NULL, false, true, 'ENABLED', 30, '规格书', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119404, 1, 'product_asset_type', 'INSTALL_GUIDE', '安装说明', 'Installation Guide', NULL, false, true, 'ENABLED', 40, '安装说明', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119405, 1, 'product_asset_type', 'DRAWING', '图纸', 'Drawing', NULL, false, true, 'ENABLED', 50, '图纸', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00'),
    (119406, 1, 'product_asset_type', 'OTHER', '其他', 'Other', NULL, false, true, 'ENABLED', 90, '其他资料', '0', 'system', '2026-06-16 00:00:00+00', 'system', '2026-06-16 00:00:00+00')
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
-- 1. 正式侧边栏保留基础信息，并新增第一版配方管理主表。
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
    (24200, 1, 0, 'Basic Information', 'productCenter.menu.masterData', 30, 'product-master', NULL, NULL, '1', '0', 'M', '1', '1', NULL, 'pc-master', 'system', now(), NULL, NULL, '产品能力-基础信息'),
    (24300, 1, 0, 'Formula Management', 'productCenter.menu.formulaRoot', 31, 'product-formula', NULL, NULL, '1', '0', 'M', '1', '1', NULL, 'pc-formula', 'system', now(), NULL, NULL, '产品能力-配方管理')
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
    (24301, 1, 24300, 'Formula Archives', 'productCenter.menu.formulas', 1, 'formulas', 'product-formula/formulas', NULL, '1', '0', 'C', '1', '1', 'product:formula:list', 'pc-formula-archive', 'system', now(), NULL, NULL, '配方档案'),
    (24302, 1, 24300, 'Formula Reviews', 'productCenter.menu.formulaReviews', 5, 'reviews', 'product-formula/reviews', NULL, '1', '0', 'C', '1', '1', 'product:formula:review', 'pc-formula-review', 'system', now(), NULL, NULL, '配方审核'),
    (24303, 1, 24300, 'Formula Materials', 'productCenter.menu.formulaMaterials', 2, 'formulas/materials', 'product-formula/formulas/materials', NULL, '1', '0', 'C', '1', '1', 'product:formula:setup', 'pc-formula-material', 'system', now(), NULL, NULL, '配方原料'),
    (24304, 1, 24300, 'Formula Options', 'productCenter.menu.formulaOptions', 3, 'formulas/options', 'product-formula/formulas/options', NULL, '1', '0', 'C', '1', '1', 'product:formula:setup', 'pc-formula-option', 'system', now(), NULL, NULL, '配方选项'),
    (24305, 1, 24300, 'Formula Simulation', 'productCenter.menu.formulaSimulation', 4, 'formulas/simulation', 'product-formula/formulas/simulation', NULL, '1', '0', 'C', '1', '1', 'product:formula:setup', 'pc-formula-simulation', 'system', now(), NULL, NULL, '配方模拟'),
    (24401, 1, 24300, 'Sale Products', 'productCenter.menu.saleProducts', 6, 'sale-products', 'product-pricing/sale-products', NULL, '1', '0', 'C', '1', '1', 'product:sale-product:list', 'pc-sale-product', 'system', now(), NULL, NULL, '可售产品'),
    (24402, 1, 24300, 'Price Settings', 'productCenter.menu.priceSettings', 7, 'price-settings', 'product-pricing/price-settings', NULL, '1', '0', 'C', '1', '1', 'product:pricing:query', 'pc-pricing', 'system', now(), NULL, NULL, '价格设置'),
    (24212, 1, 24200, 'Product Categories', 'productCenter.menu.categories', 1, 'categories', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'pc-category', 'system', now(), NULL, NULL, '产品分类'),
    (24213, 1, 24200, 'Base Dictionaries', 'productCenter.menu.productDicts', 2, 'product-dicts', 'product-center/product-dicts', NULL, '1', '0', 'C', '1', '1', 'product:dict:list', 'pc-dict', 'system', now(), NULL, NULL, '基础字典'),
    (24206, 1, 24200, 'Units', 'productCenter.menu.units', 3, 'units', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:unit:list', 'pc-unit', 'system', now(), NULL, NULL, '单位管理'),
    (24209, 1, 24200, 'Manufacturers', 'productCenter.menu.manufacturers', 4, 'manufacturers', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:manufacturer:list', 'pc-manufacturer', 'system', now(), NULL, NULL, '厂家管理'),
    (24207, 1, 24200, 'Material Types', 'productCenter.menu.materialTypes', 5, 'material-types', 'product-center/material-types', NULL, '1', '0', 'C', '1', '1', 'product:material-type:list', 'pc-material-type', 'system', now(), NULL, NULL, '物料类型'),
    (24204, 1, 24200, 'Material Attributes', 'productCenter.menu.baseAttributes', 6, 'base-attributes', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base-attribute:list', 'pc-attribute', 'system', now(), NULL, NULL, '物料属性'),
    (24202, 1, 24200, 'Materials', 'productCenter.menu.materials', 7, 'materials', 'product-center/base', NULL, '1', '0', 'C', '1', '1', 'product:base:list', 'pc-material', 'system', now(), NULL, NULL, '物料管理'),
    (24208, 1, 24200, 'Material Attribute Values', 'productCenter.menu.materialAttributes', 89, 'material-attributes', 'product-center/base', NULL, '1', '0', 'C', '0', '1', 'product:material-attribute:list', 'pc-attribute', 'system', now(), NULL, NULL, '物料属性值，从物料管理抽屉同步维护'),
    (24205, 1, 24200, 'Media Assets', 'productCenter.menu.mediaAssets', 94, 'media-assets', 'product-center/assets', NULL, '1', '0', 'C', '0', '1', 'product:asset:list', 'pc-media', 'system', now(), NULL, NULL, '资料资产'),
    (24210, 1, 24200, 'Media Bindings', 'productCenter.menu.mediaBindings', 94, 'media-bindings', 'product-center/assets', NULL, '1', '0', 'C', '0', '1', 'product:asset:list', 'pc-media-link', 'system', now(), NULL, NULL, '资料绑定，附件关联台账，默认不在基础资料一级菜单展示')
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
    (24310, 1, 24301, 'Formula Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:formula:list', '#', 'system', now(), NULL, NULL, '配方查询'),
    (24311, 1, 24301, 'Formula Detail', 'common.detail', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:formula:query', '#', 'system', now(), NULL, NULL, '配方详情'),
    (24312, 1, 24301, 'Formula Add', 'common.add', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:formula:add', '#', 'system', now(), NULL, NULL, '配方新增'),
    (24313, 1, 24301, 'Formula Edit', 'common.edit', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:formula:edit', '#', 'system', now(), NULL, NULL, '配方编辑'),
    (24314, 1, 24301, 'Formula Delete', 'common.delete', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:formula:remove', '#', 'system', now(), NULL, NULL, '配方删除'),
    (24315, 1, 24301, 'Formula Setup', 'productCenter.formula.actions.setup', 6, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:formula:setup', '#', 'system', now(), NULL, NULL, '设置配方'),
    (24316, 1, 24301, 'Formula Submit Review', 'productCenter.formula.actions.submitReview', 7, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:formula:submitReview', '#', 'system', now(), NULL, NULL, '配方提交审核'),
    (24317, 1, 24302, 'Formula Review Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:formula:review', '#', 'system', now(), NULL, NULL, '配方审核查询'),
    (24318, 1, 24302, 'Formula Review Detail', 'common.detail', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:formula:review', '#', 'system', now(), NULL, NULL, '配方审核详情'),
    (24319, 1, 24301, 'Formula Stop', 'productCenter.formula.actions.stop', 10, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:formula:stop', '#', 'system', now(), NULL, NULL, '配方停用'),
    (24320, 1, 24301, 'Formula Reference', 'productCenter.common.references', 11, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:formula:reference', '#', 'system', now(), NULL, NULL, '配方引用和变更记录'),
    (24321, 1, 24302, 'Formula Review Approve', 'productCenter.formula.actions.approve', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:formula:approve', '#', 'system', now(), NULL, NULL, '配方审核通过'),
    (24322, 1, 24302, 'Formula Review Reject', 'productCenter.formula.actions.reject', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:formula:reject', '#', 'system', now(), NULL, NULL, '配方驳回'),
    (24410, 1, 24401, 'Sale Product Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:sale-product:list', '#', 'system', now(), NULL, NULL, '可售产品查询'),
    (24411, 1, 24401, 'Sale Product Detail', 'common.detail', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:sale-product:query', '#', 'system', now(), NULL, NULL, '可售产品详情'),
    (24412, 1, 24401, 'Sale Product Add', 'common.add', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:sale-product:add', '#', 'system', now(), NULL, NULL, '可售产品新增'),
    (24413, 1, 24401, 'Sale Product Edit', 'common.edit', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:sale-product:edit', '#', 'system', now(), NULL, NULL, '可售产品编辑'),
    (24414, 1, 24401, 'Sale Product Delete', 'common.delete', 5, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:sale-product:remove', '#', 'system', now(), NULL, NULL, '可售产品删除'),
    (24415, 1, 24401, 'Sale Product Export', 'common.export', 6, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:sale-product:export', '#', 'system', now(), NULL, NULL, '可售产品导出'),
    (24416, 1, 24401, 'Sale Product Reference', 'productCenter.common.references', 7, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:sale-product:reference', '#', 'system', now(), NULL, NULL, '可售产品引用检查'),
    (24420, 1, 24402, 'Price Setting Query', 'common.search', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:pricing:query', '#', 'system', now(), NULL, NULL, '价格设置查询'),
    (24421, 1, 24402, 'Price Setting Edit', 'common.edit', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:pricing:edit', '#', 'system', now(), NULL, NULL, '价格设置维护'),
    (24422, 1, 24402, 'Price Setting Validate', 'productCenter.pricing.validatePrice', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'product:pricing:validate', '#', 'system', now(), NULL, NULL, '价格校验'),
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
WHERE menu_id BETWEEN 24200 AND 24599
ON CONFLICT (role_id, menu_id) DO NOTHING;

DELETE FROM sys_dict_data
WHERE dict_type IN (
    'product_unit',
    'product_unit_type',
    'product_business_type',
    'product_type',
    'product_asset_type'
);
DELETE FROM sys_dict_type
WHERE dict_type IN (
    'product_unit',
    'product_unit_type',
    'product_business_type',
    'product_type',
    'product_asset_type'
);
