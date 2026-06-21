package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductEntityDefaults;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 产品模块 ServiceImpl 公共查询辅助。
 */
abstract class ProductServiceSupport {

    protected static final String STATUS_ENABLED = "ENABLED";
    protected static final String STATUS_DISABLED = "DISABLED";

    protected <T, V> TableDataInfo<V> page(BaseMapperPlus<T, V> mapper, PageQuery pageQuery, QueryWrapper<T> wrapper) {
        Page<V> result = mapper.selectVoPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(result);
    }

    protected <T, V> TableDataInfo<V> page(BaseMapperPlus<T, V> mapper, PageQuery pageQuery, QueryWrapper<T> wrapper, Consumer<QueryWrapper<T>> defaultSorter) {
        applyDefaultSort(pageQuery, wrapper, defaultSorter);
        return page(mapper, pageQuery, wrapper);
    }

    protected <T> QueryWrapper<T> applyDefaultSort(PageQuery pageQuery, QueryWrapper<T> wrapper, Consumer<QueryWrapper<T>> defaultSorter) {
        if (!hasPageSort(pageQuery)) {
            defaultSorter.accept(wrapper);
        }
        return wrapper;
    }

    protected boolean hasPageSort(PageQuery pageQuery) {
        return pageQuery != null && StringUtils.isNotBlank(pageQuery.getOrderByColumn()) && StringUtils.isNotBlank(pageQuery.getIsAsc());
    }

    protected <T> Boolean remove(BaseMapperPlus<T, ?> mapper, Long[] ids) {
        return mapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    protected void assertNoReferences(ReferenceCheckResultVo references) {
        if (references != null && !Boolean.TRUE.equals(references.getAllowed())) {
            String blockerKey = StringUtils.blankToDefault(references.getBlockerReasonKey(), "product.base.delete.hasReferences");
            throw ServiceException.ofMessageKey(blockerKey);
        }
    }

    protected <T> Boolean saveEntity(BaseMapperPlus<T, ?> mapper, T entity, Function<T, Long> idReader) {
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (idReader.apply(entity) == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return mapper.insert(entity) > 0;
        }
        return mapper.updateById(entity) > 0;
    }

    protected ReferenceCheckResultVo referenceResult(long count, String blockerKey, String summary) {
        ReferenceCheckResultVo vo = new ReferenceCheckResultVo();
        vo.setReferenceCount(count);
        vo.setAllowed(count <= 0);
        if (count > 0) {
            vo.setBlockerReasonKey(blockerKey);
            if (StringUtils.isNotBlank(summary)) {
                vo.getReferenceSummaries().add(summary);
            }
        }
        return vo;
    }

    protected BaseEditCheckResultVo editCheckResult(String status, ReferenceCheckResultVo references) {
        BaseEditCheckResultVo vo = new BaseEditCheckResultVo();
        vo.setStatus(status);
        vo.setEditable(!isEnabledStatus(status));
        if (!Boolean.TRUE.equals(vo.getEditable())) {
            vo.setReason("product.base.edit.enabledDenied");
            vo.setReasonKey("product.base.edit.enabledDenied");
        }
        if (references != null) {
            vo.getImpactSummary().addAll(references.getReferenceSummaries());
        }
        return vo;
    }

    protected BaseEditCheckResultVo deniedEditCheck(String status, String reasonKey, ReferenceCheckResultVo references) {
        BaseEditCheckResultVo vo = editCheckResult(status, references);
        vo.setEditable(Boolean.FALSE);
        vo.setReason(reasonKey);
        vo.setReasonKey(reasonKey);
        return vo;
    }

    protected void assertNormalEditable(String status) {
        if (isEnabledStatus(status)) {
            throw ServiceException.ofMessageKey("product.base.edit.enabledDenied");
        }
    }

    protected boolean isEnabledStatus(String status) {
        return status != null && STATUS_ENABLED.equalsIgnoreCase(status);
    }

    protected <T> QueryWrapper<T> activeQuery() {
        return Wrappers.<T>query().eq("del_flag", "0");
    }

    protected <T> QueryWrapper<T> activeQuery(Class<T> ignored) {
        return activeQuery();
    }

    protected void like(QueryWrapper<?> q, String column, String value) {
        q.like(StringUtils.isNotBlank(value), column, value);
    }

    protected void eq(QueryWrapper<?> q, String column, Object value) {
        q.eq(value != null && (!(value instanceof String text) || StringUtils.isNotBlank(text)), column, value);
    }
}
