-- AI assistant governance tables.
-- Java business DB is the source of truth for AI permissions, quota, usage and audit summaries.

CREATE TABLE IF NOT EXISTS ai_tenant_config (
    tenant_id bigint PRIMARY KEY CHECK (tenant_id <> 0),
    enabled boolean NOT NULL DEFAULT false,
    default_provider varchar(80),
    default_model varchar(120),
    daily_request_limit bigint,
    daily_token_limit bigint,
    daily_cost_limit numeric(18,6),
    status char(1) NOT NULL DEFAULT '1',
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);

CREATE TABLE IF NOT EXISTS ai_user_quota (
    quota_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    user_id bigint NOT NULL,
    daily_request_limit bigint,
    daily_token_limit bigint,
    daily_cost_limit numeric(18,6),
    status char(1) NOT NULL DEFAULT '1',
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_ai_user_quota_tenant_user ON ai_user_quota (tenant_id, user_id);

CREATE TABLE IF NOT EXISTS ai_tool_policy (
    policy_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    subject_type varchar(20) NOT NULL,
    subject_id bigint,
    tool_code varchar(160) NOT NULL,
    mode varchar(20) NOT NULL DEFAULT 'DISABLED',
    risk_level varchar(20) NOT NULL DEFAULT 'LOW',
    status char(1) NOT NULL DEFAULT '1',
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz
);
CREATE INDEX IF NOT EXISTS idx_ai_tool_policy_subject ON ai_tool_policy (tenant_id, subject_type, subject_id, tool_code);

CREATE TABLE IF NOT EXISTS ai_service_credential (
    credential_id bigint PRIMARY KEY,
    service_name varchar(80) NOT NULL,
    key_version varchar(80) NOT NULL,
    secret_ciphertext text NOT NULL,
    secret_fingerprint varchar(64) NOT NULL,
    status char(1) NOT NULL DEFAULT '1',
    last_used_time timestamptz,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_ai_service_credential_version ON ai_service_credential (service_name, key_version);
CREATE INDEX IF NOT EXISTS idx_ai_service_credential_status ON ai_service_credential (service_name, status);

CREATE TABLE IF NOT EXISTS ai_provider_config (
    provider_id bigint PRIMARY KEY,
    provider_code varchar(80) NOT NULL,
    provider_name varchar(120) NOT NULL,
    base_url varchar(500) NOT NULL,
    chat_completions_path varchar(255) NOT NULL DEFAULT '/chat/completions',
    default_model varchar(120) NOT NULL,
    timeout_seconds int NOT NULL DEFAULT 120,
    enabled boolean NOT NULL DEFAULT false,
    status char(1) NOT NULL DEFAULT '1',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_ai_provider_config_code ON ai_provider_config (provider_code);
CREATE INDEX IF NOT EXISTS idx_ai_provider_config_enabled ON ai_provider_config (enabled, status);

CREATE TABLE IF NOT EXISTS ai_provider_model (
    model_id bigint PRIMARY KEY,
    provider_id bigint NOT NULL,
    model_code varchar(120) NOT NULL,
    model_name varchar(160) NOT NULL,
    model_type varchar(40) NOT NULL DEFAULT 'CHAT',
    context_window int,
    input_price numeric(18,8),
    output_price numeric(18,8),
    default_model boolean NOT NULL DEFAULT false,
    status char(1) NOT NULL DEFAULT '1',
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_ai_provider_model_code ON ai_provider_model (provider_id, model_code);
CREATE INDEX IF NOT EXISTS idx_ai_provider_model_status ON ai_provider_model (provider_id, status, default_model);

CREATE TABLE IF NOT EXISTS ai_provider_credential (
    credential_id bigint PRIMARY KEY,
    provider_id bigint NOT NULL,
    api_key_ciphertext text NOT NULL,
    key_fingerprint varchar(64) NOT NULL,
    status char(1) NOT NULL DEFAULT '1',
    last_used_time timestamptz,
    create_by_id bigint,
    create_by varchar(64),
    create_time timestamptz DEFAULT now(),
    update_by varchar(64),
    update_time timestamptz,
    remark varchar(500)
);
CREATE INDEX IF NOT EXISTS idx_ai_provider_credential_provider ON ai_provider_credential (provider_id, status, create_time DESC);

CREATE TABLE IF NOT EXISTS ai_usage_ledger (
    usage_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    user_id bigint NOT NULL,
    session_id varchar(80),
    request_id varchar(80),
    provider_call_id varchar(120),
    provider varchar(80),
    model varchar(120),
    input_tokens bigint,
    output_tokens bigint,
    cost_amount numeric(18,6),
    latency_ms bigint,
    status varchar(30),
    created_time timestamptz DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_ai_usage_provider_call ON ai_usage_ledger (provider_call_id) WHERE provider_call_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ai_usage_tenant_user_time ON ai_usage_ledger (tenant_id, user_id, created_time DESC);

CREATE TABLE IF NOT EXISTS ai_usage_daily (
    id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    user_id bigint NOT NULL,
    usage_date date NOT NULL,
    request_count bigint NOT NULL DEFAULT 0,
    input_tokens bigint NOT NULL DEFAULT 0,
    output_tokens bigint NOT NULL DEFAULT 0,
    cost_amount numeric(18,6) NOT NULL DEFAULT 0
);

-- 兼容已按租户、用户、日期作为复合主键创建的开发库。
ALTER TABLE ai_usage_daily
    ADD COLUMN IF NOT EXISTS id bigint GENERATED BY DEFAULT AS IDENTITY;
ALTER TABLE ai_usage_daily ALTER COLUMN id SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_ai_usage_daily_tenant_user_date ON ai_usage_daily (tenant_id, user_id, usage_date);

DO $$
DECLARE
    primary_key_name text;
    primary_key_definition text;
BEGIN
    SELECT conname, pg_get_constraintdef(oid)
    INTO primary_key_name, primary_key_definition
    FROM pg_constraint
    WHERE conrelid = 'ai_usage_daily'::regclass
      AND contype = 'p';

    IF primary_key_name IS NOT NULL AND primary_key_definition <> 'PRIMARY KEY (id)' THEN
        EXECUTE format('ALTER TABLE ai_usage_daily DROP CONSTRAINT %I', primary_key_name);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'ai_usage_daily'::regclass
          AND contype = 'p'
    ) THEN
        ALTER TABLE ai_usage_daily ADD CONSTRAINT ai_usage_daily_pkey PRIMARY KEY (id);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS ai_audit_summary (
    audit_id bigint PRIMARY KEY,
    tenant_id bigint NOT NULL CHECK (tenant_id <> 0),
    user_id bigint NOT NULL,
    session_id varchar(80),
    request_id varchar(80),
    action_type varchar(80),
    tool_code varchar(160),
    business_target varchar(255),
    risk_level varchar(20),
    approval_status varchar(30),
    status varchar(30),
    created_time timestamptz DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_ai_audit_tenant_user_time ON ai_audit_summary (tenant_id, user_id, created_time DESC);

DELETE FROM sys_role_menu
WHERE tenant_id = 1 AND menu_id = 25006;

DELETE FROM sys_menu
WHERE tenant_id = 1 AND menu_id = 25006 AND perms = 'ai:tool:manage';

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES
    (25030, 'AI model type', 'ai_model_type', '1', 'system', now(), '智能中枢模型类型')
ON CONFLICT (dict_type) DO UPDATE
SET dict_name = EXCLUDED.dict_name,
    status = EXCLUDED.status,
    remark = EXCLUDED.remark,
    update_time = now();

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES
    (25031, 1, 'Chat model', 'CHAT', 'ai_model_type', 'primary', 'Y', '1', 'system', now()),
    (25032, 2, 'Embedding model', 'EMBEDDING', 'ai_model_type', 'success', 'N', '1', 'system', now())
ON CONFLICT (dict_code) DO UPDATE
SET dict_sort = EXCLUDED.dict_sort,
    dict_label = EXCLUDED.dict_label,
    dict_value = EXCLUDED.dict_value,
    dict_type = EXCLUDED.dict_type,
    list_class = EXCLUDED.list_class,
    is_default = EXCLUDED.is_default,
    status = EXCLUDED.status,
    update_time = now();

INSERT INTO sys_menu (menu_id, tenant_id, parent_id, menu_name, i18n_key, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (25000, 1, 0, 'Smart Hub', 'ai.menu.root', 90, 'ai', 'Layout', NULL, '1', '0', 'M', '1', '1', NULL, 'ai', 'system', now(), '智能中枢'),
    (25001, 1, 25000, 'Service Keys', 'ai.menu.credentials', 1, 'credentials', 'ai/credentials/index', NULL, '1', '0', 'C', '1', '1', 'ai:credential:list', 'lock', 'system', now(), '服务密钥'),
    (25010, 1, 25000, 'Channel Config', 'ai.menu.providers', 2, 'providers', 'ai/providers/index', NULL, '1', '0', 'C', '1', '1', 'ai:provider:list', 'server', 'system', now(), '渠道配置'),
    (25011, 1, 25000, 'Model Config', 'ai.menu.models', 3, 'models', 'ai/models/index', NULL, '1', '0', 'C', '1', '1', 'ai:model:list', 'component', 'system', now(), '模型配置'),
    (25007, 1, 25000, 'Quota Manage', 'ai.menu.quotas', 4, 'quotas', 'ai/quotas/index', NULL, '1', '0', 'C', '1', '1', 'ai:quota:list', 'money', 'system', now(), '额度管理'),
    (25008, 1, 25000, 'Usage View', 'ai.menu.usage', 5, 'usage', 'ai/usage/index', NULL, '1', '0', 'C', '1', '1', 'ai:usage:list', 'report', 'system', now(), '用量查看'),
    (25009, 1, 25000, 'Audit View', 'ai.menu.audit', 6, 'audit', 'ai/audit/index', NULL, '1', '0', 'C', '1', '1', 'ai:audit:list', 'log', 'system', now(), '审计查看'),
    (25002, 1, 25000, 'Smart Assistant Use', 'ai.menu.assistantUse', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'ai:assistant:use', '#', 'system', now(), '使用智能助手'),
    (25003, 1, 25000, 'Smart Assistant Admin', 'ai.menu.assistantAdmin', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'ai:assistant:admin', '#', 'system', now(), '智能助手管理'),
    (25004, 1, 25001, 'Generate Service Key', 'ai.menu.credentialGenerate', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'ai:credential:generate', '#', 'system', now(), '生成服务密钥'),
    (25012, 1, 25001, 'Enable Service Key', 'ai.menu.credentialEnable', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'ai:credential:enable', '#', 'system', now(), '启用服务密钥'),
    (25020, 1, 25001, 'Disable Service Key', 'ai.menu.credentialDisable', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'ai:credential:disable', '#', 'system', now(), '禁用服务密钥'),
    (25021, 1, 25001, 'Delete Service Key', 'ai.menu.credentialRemove', 4, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'ai:credential:remove', '#', 'system', now(), '删除服务密钥'),
    (25005, 1, 25010, 'Add Provider', 'ai.menu.providerAdd', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'ai:provider:add', '#', 'system', now(), '新增 Provider'),
    (25013, 1, 25010, 'Edit Provider', 'ai.menu.providerEdit', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'ai:provider:edit', '#', 'system', now(), '编辑 Provider'),
    (25014, 1, 25010, 'Update Provider Key', 'ai.menu.providerKey', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'ai:provider:key', '#', 'system', now(), '更新 Provider Key'),
    (25015, 1, 25011, 'Add Model', 'ai.menu.modelAdd', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'ai:model:add', '#', 'system', now(), '新增模型'),
    (25016, 1, 25011, 'Edit Model', 'ai.menu.modelEdit', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'ai:model:edit', '#', 'system', now(), '编辑模型'),
    (25017, 1, 25011, 'Set Default Model', 'ai.menu.modelDefault', 3, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'ai:model:default', '#', 'system', now(), '设为默认模型'),
    (25018, 1, 25007, 'Add Quota', 'ai.menu.quotaAdd', 1, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'ai:quota:add', '#', 'system', now(), '新增额度'),
    (25019, 1, 25007, 'Edit Quota', 'ai.menu.quotaEdit', 2, '#', NULL, NULL, '1', '0', 'F', '1', '1', 'ai:quota:edit', '#', 'system', now(), '编辑额度')
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
    remark = EXCLUDED.remark;

INSERT INTO ai_provider_model (model_id, provider_id, model_code, model_name, model_type, default_model, status, create_by, create_time, remark)
SELECT 260000000000000201, provider_id, default_model, default_model, 'CHAT', true, '1', 'system', now(), '默认模型'
FROM ai_provider_config
WHERE provider_code = 'deepseek'
ON CONFLICT (provider_id, model_code) DO UPDATE
SET model_name = EXCLUDED.model_name,
    model_type = EXCLUDED.model_type,
    default_model = EXCLUDED.default_model,
    status = EXCLUDED.status,
    remark = EXCLUDED.remark,
    update_time = now();

INSERT INTO sys_role_menu (role_id, menu_id, tenant_id)
SELECT 1, menu_id, 1 FROM sys_menu
WHERE tenant_id = 1 AND menu_id BETWEEN 25000 AND 25021
ON CONFLICT DO NOTHING;
