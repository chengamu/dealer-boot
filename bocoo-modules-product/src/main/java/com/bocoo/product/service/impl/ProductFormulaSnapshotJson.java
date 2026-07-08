package com.bocoo.product.service.impl;

import com.bocoo.product.domain.entity.ProductFormula;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ProductFormulaSnapshotJson {

    private static final Set<String> AUDIT_FIELDS = Set.of(
        "createById", "createBy", "createTime", "updateBy", "updateTime", "delFlag"
    );

    private static final JsonMapper OBJECT_MAPPER = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();

    public ProductFormula copyStatusSnapshot(ProductFormula source) {
        ProductFormula target = new ProductFormula();
        target.setFormulaId(source.getFormulaId());
        target.setFormulaCode(source.getFormulaCode());
        target.setFormulaName(source.getFormulaName());
        target.setStatus(source.getStatus());
        target.setMaterialLineCount(source.getMaterialLineCount());
        target.setConfiguredFlag(source.getConfiguredFlag());
        target.setCurrentVersionId(source.getCurrentVersionId());
        target.setCurrentVersionNo(source.getCurrentVersionNo());
        target.setCurrentVersionLabel(source.getCurrentVersionLabel());
        target.setDraftVersionNo(source.getDraftVersionNo());
        target.setLatestValidationStatus(source.getLatestValidationStatus());
        target.setLatestValidationMessage(source.getLatestValidationMessage());
        target.setLatestValidationTime(source.getLatestValidationTime());
        target.setMaterialValidationStatus(source.getMaterialValidationStatus());
        target.setMaterialValidationMessage(source.getMaterialValidationMessage());
        target.setMaterialValidationTime(source.getMaterialValidationTime());
        target.setOptionValidationStatus(source.getOptionValidationStatus());
        target.setOptionValidationMessage(source.getOptionValidationMessage());
        target.setOptionValidationTime(source.getOptionValidationTime());
        target.setSimulationValidationStatus(source.getSimulationValidationStatus());
        target.setSimulationValidationMessage(source.getSimulationValidationMessage());
        target.setSimulationValidationTime(source.getSimulationValidationTime());
        target.setAuditBy(source.getAuditBy());
        target.setAuditTime(source.getAuditTime());
        target.setRejectReason(source.getRejectReason());
        return target;
    }

    public String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof ProductFormula formula) {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("formulaId", formula.getFormulaId());
                snapshot.put("formulaCode", formula.getFormulaCode());
                snapshot.put("formulaName", formula.getFormulaName());
                snapshot.put("categoryId", formula.getCategoryId());
                snapshot.put("categoryCode", formula.getCategoryCode());
                snapshot.put("categoryNameCn", formula.getCategoryNameCn());
                snapshot.put("productTypeCode", formula.getProductTypeCode());
                snapshot.put("productTypeNameCn", formula.getProductTypeNameCn());
                snapshot.put("minWidthInch", formula.getMinWidthInch());
                snapshot.put("minHeightInch", formula.getMinHeightInch());
                snapshot.put("maxWidthInch", formula.getMaxWidthInch());
                snapshot.put("maxHeightInch", formula.getMaxHeightInch());
                snapshot.put("sizeSummary", formula.getSizeSummary());
                return OBJECT_MAPPER.writeValueAsString(snapshot);
            }
            if (value instanceof Map<?, ?> map) {
                return OBJECT_MAPPER.writeValueAsString(cleanMap(map));
            }
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize formula snapshot", e);
        }
    }

    public <T> T fromJson(String value, Class<T> type) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(value, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse formula snapshot", e);
        }
    }

    private Map<String, Object> cleanMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (key != null && !AUDIT_FIELDS.contains(String.valueOf(key))) {
                result.put(String.valueOf(key), cleanValue(value));
            }
        });
        return result;
    }

    private Object cleanValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return cleanMap(map);
        }
        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>();
            list.forEach(item -> result.add(cleanValue(item)));
            return result;
        }
        return value;
    }
}
