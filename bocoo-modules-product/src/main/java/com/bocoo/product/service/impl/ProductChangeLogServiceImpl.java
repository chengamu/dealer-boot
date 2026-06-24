package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.product.domain.bo.ProductChangeLogBo;
import com.bocoo.product.domain.entity.ProductChangeLog;
import com.bocoo.product.domain.vo.ProductChangeLogVo;
import com.bocoo.product.mapper.ProductChangeLogMapper;
import com.bocoo.product.service.ProductChangeLogService;
import com.bocoo.product.service.ProductEntityDefaults;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class ProductChangeLogServiceImpl implements ProductChangeLogService {

    private static final JsonMapper OBJECT_MAPPER = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ProductChangeLogMapper changeLogMapper;

    @Override
    public TableDataInfo<ProductChangeLogVo> queryPageList(ProductChangeLogBo bo, PageQuery pageQuery) {
        QueryWrapper<ProductChangeLog> wrapper = Wrappers.query();
        if (bo != null) {
            eq(wrapper, "biz_module", bo.getBizModule());
            eq(wrapper, "biz_type", bo.getBizType());
            eq(wrapper, "biz_id", bo.getBizId());
            eq(wrapper, "biz_code", bo.getBizCode());
            eq(wrapper, "action_type", bo.getActionType());
            like(wrapper, "operator_name", bo.getOperatorName());
        }
        wrapper.orderByDesc("operate_time", "change_log_id");
        IPage<ProductChangeLogVo> page = changeLogMapper.selectVoPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    @Override
    public void record(String bizModule, String bizType, Long bizId, String bizCode, String actionType,
                       Object beforeValue, Object afterValue, String remark) {
        ProductChangeLog entity = new ProductChangeLog();
        entity.setBizModule(bizModule);
        entity.setBizType(bizType);
        entity.setBizId(bizId);
        entity.setBizCode(bizCode);
        entity.setActionType(actionType);
        entity.setActionName(actionNameOf(actionType));
        entity.setBeforeJson(toJson(beforeValue));
        entity.setAfterJson(toJson(afterValue));
        entity.setDiffJson(buildDiffJson(beforeValue, afterValue));
        entity.setOperatorId(currentUserId());
        entity.setOperatorName(currentUsername());
        entity.setOperateTime(TimeUtils.utcNow());
        entity.setRemark(remark);
        ProductEntityDefaults.prepareInsert(entity);
        changeLogMapper.insert(entity);
    }

    private String buildDiffJson(Object beforeValue, Object afterValue) {
        Map<String, Object> beforeMap = toMap(beforeValue);
        Map<String, Object> afterMap = toMap(afterValue);
        if (beforeMap == null && afterMap == null) {
            return null;
        }
        Map<String, Object> diff = new LinkedHashMap<>();
        TreeSet<String> keys = new TreeSet<>();
        if (beforeMap != null) {
            keys.addAll(beforeMap.keySet());
        }
        if (afterMap != null) {
            keys.addAll(afterMap.keySet());
        }
        for (String key : keys) {
            Object beforeItem = beforeMap == null ? null : beforeMap.get(key);
            Object afterItem = afterMap == null ? null : afterMap.get(key);
            if (Objects.equals(beforeItem, afterItem)) {
                continue;
            }
            Map<String, Object> fieldDiff = new LinkedHashMap<>();
            fieldDiff.put("before", beforeItem);
            fieldDiff.put("after", afterItem);
            diff.put(key, fieldDiff);
        }
        return diff.isEmpty() ? null : toJson(diff);
    }

    private Map<String, Object> toMap(Object value) {
        if (value == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(value, MAP_TYPE);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize change log payload", e);
        }
    }

    private String actionNameOf(String actionType) {
        if (StringUtils.isBlank(actionType)) {
            return null;
        }
        return switch (actionType) {
            case "CREATE" -> "新增";
            case "UPDATE" -> "修改";
            case "SUPER_UPDATE" -> "超级修改";
            case "AUDIT" -> "审核";
            case "UNAUDIT" -> "取消审核";
            case "DELETE" -> "删除";
            default -> actionType;
        };
    }

    private void eq(QueryWrapper<?> q, String column, Object value) {
        q.eq(value != null && (!(value instanceof String text) || StringUtils.isNotBlank(text)), column, value);
    }

    private void like(QueryWrapper<?> q, String column, String value) {
        q.like(StringUtils.isNotBlank(value), column, value);
    }

    private Long currentUserId() {
        try {
            return LoginHelper.getUserId();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String currentUsername() {
        try {
            return LoginHelper.getUsername();
        } catch (Exception ignored) {
            return "system";
        }
    }
}
