package com.bocoo.dealer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;

import java.util.function.Consumer;
import java.util.function.Supplier;

abstract class DealerServiceSupport {
    protected static final Long PLATFORM_TENANT_ID = 1L;

    protected Long tenantId() {
        Long id = LoginHelper.getTenantId();
        if (id == null) throw ServiceException.ofMessageKey("tenant.context.missing");
        return id;
    }

    protected <T> QueryWrapper<T> active() {
        return new QueryWrapper<T>().eq("del_flag", "0");
    }

    protected <T> T platformProduct(Supplier<T> supplier) {
        return TenantContextHolder.callWithTenant(PLATFORM_TENANT_ID, supplier);
    }

    protected <T> T ignoreTenant(Supplier<T> supplier) {
        return TenantContextHolder.callWithIgnore(supplier);
    }

    protected <T, V> TableDataInfo<V> page(BaseMapperPlus<T, V> mapper, PageQuery pageQuery,
                                            QueryWrapper<T> query, Consumer<QueryWrapper<T>> sorter) {
        if (pageQuery == null || StringUtils.isBlank(pageQuery.getOrderByColumn())) sorter.accept(query);
        IPage<V> page = mapper.selectVoPage(pageQuery.build(), query);
        return TableDataInfo.build(page);
    }
}
