package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * 产品基础资料 CRUD 辅助方法。
 */
abstract class ProductCrudSupport {

    protected <T, V> TableDataInfo<V> page(BaseMapperPlus<T, V> mapper, PageQuery pageQuery, QueryWrapper<T> wrapper) {
        Page<V> result = mapper.selectVoPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(result);
    }

    protected <T> Boolean save(BaseMapperPlus<T, ?> mapper, Object bo, Class<T> entityClass, String idField) {
        T entity = MapstructUtils.convert(bo, entityClass);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (readField(entity, idField) == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return mapper.insert(entity) > 0;
        }
        return mapper.updateById(entity) > 0;
    }

    protected <T> Boolean remove(BaseMapperPlus<T, ?> mapper, Long[] ids) {
        return mapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    protected <T> Boolean updateStatus(BaseMapperPlus<T, ?> mapper, Class<T> entityClass, String idField, Long id, String status) {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            writeField(entity, idField, id);
            writeField(entity, "status", status);
            return mapper.updateById(entity) > 0;
        } catch (ReflectiveOperationException e) {
            return Boolean.FALSE;
        }
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

    private Object readField(Object target, String fieldName) {
        Field field = findField(target.getClass(), fieldName);
        if (field == null) {
            return null;
        }
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private void writeField(Object target, String fieldName, Object value) throws IllegalAccessException {
        Field field = findField(target.getClass(), fieldName);
        if (field == null) {
            return;
        }
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> type, String fieldName) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
