package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductEntityDefaults;

import java.util.Arrays;
import java.util.function.Function;

/**
 * 产品模块 ServiceImpl 公共查询辅助。
 */
abstract class ProductServiceSupport {

    protected <T, V> TableDataInfo<V> page(BaseMapperPlus<T, V> mapper, PageQuery pageQuery, QueryWrapper<T> wrapper) {
        Page<V> result = mapper.selectVoPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(result);
    }

    protected <T> Boolean remove(BaseMapperPlus<T, ?> mapper, Long[] ids) {
        return mapper.deleteBatchIds(Arrays.asList(ids)) > 0;
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
