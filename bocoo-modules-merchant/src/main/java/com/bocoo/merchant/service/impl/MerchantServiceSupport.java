package com.bocoo.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

abstract class MerchantServiceSupport {

    protected static final String STATUS_ENABLED = "ENABLED";
    protected static final String STATUS_DISABLED = "DISABLED";
    protected static final String DEL_FLAG_NORMAL = "0";
    protected static final Long PLATFORM_TENANT_ID = 1L;

    protected <T, V> TableDataInfo<V> page(BaseMapperPlus<T, V> mapper, PageQuery pageQuery,
                                           QueryWrapper<T> wrapper, Consumer<QueryWrapper<T>> defaultSorter) {
        if (!hasPageSort(pageQuery)) {
            defaultSorter.accept(wrapper);
        }
        IPage<V> page = mapper.selectVoPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    protected boolean hasPageSort(PageQuery pageQuery) {
        return pageQuery != null && StringUtils.isNotBlank(pageQuery.getOrderByColumn()) && StringUtils.isNotBlank(pageQuery.getIsAsc());
    }

    protected <T> QueryWrapper<T> activeQuery() {
        return new QueryWrapper<T>().eq("del_flag", DEL_FLAG_NORMAL);
    }

    protected void like(QueryWrapper<?> q, String column, String value) {
        q.like(StringUtils.isNotBlank(value), column, value);
    }

    protected void eq(QueryWrapper<?> q, String column, Object value) {
        q.eq(value != null && (!(value instanceof String text) || StringUtils.isNotBlank(text)), column, value);
    }

    protected void checkPlatformTenant() {
        if (!LoginHelper.isPlatformTenant()) {
            throw ServiceException.ofMessageKey("merchant.platformOnly");
        }
    }

    protected Long currentTenantId() {
        Long tenantId = LoginHelper.getTenantId();
        if (tenantId == null) {
            throw ServiceException.ofMessageKey("tenant.context.missing");
        }
        return tenantId;
    }

    protected <T> T platformIgnoreTenant(Supplier<T> supplier) {
        return TenantContextHolder.callWithIgnore(supplier);
    }

    protected String normalizeStatus(String status) {
        String normalized = StringUtils.trimToEmpty(status).toUpperCase(Locale.ROOT);
        if (!STATUS_ENABLED.equals(normalized) && !STATUS_DISABLED.equals(normalized)) {
            throw ServiceException.ofMessageKey("merchant.status.invalid");
        }
        return normalized;
    }

    protected <T> Boolean softRemove(BaseMapperPlus<T, ?> mapper, String idColumn, Long[] ids) {
        return mapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    protected boolean enabled(String status) {
        return STATUS_ENABLED.equalsIgnoreCase(status);
    }
}
