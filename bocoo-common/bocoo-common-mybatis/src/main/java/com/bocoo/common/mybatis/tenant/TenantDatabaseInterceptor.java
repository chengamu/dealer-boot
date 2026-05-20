package com.bocoo.common.mybatis.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.bocoo.common.core.config.properties.TenantProperties;
import com.bocoo.common.core.context.TenantContextHolder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

public class TenantDatabaseInterceptor implements TenantLineHandler {

    private final TenantProperties properties;

    public TenantDatabaseInterceptor(TenantProperties properties) {
        this.properties = properties;
    }

    @Override
    public Expression getTenantId() {
        return new LongValue(TenantContextHolder.getRequiredTenantId());
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return TenantContextHolder.isIgnore()
            || properties.getIgnoreTables().stream().anyMatch(tableName::equalsIgnoreCase);
    }
}
