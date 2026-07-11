package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.vo.ProductFormulaMaterialVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionMaterialVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionValueVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionVo;
import com.bocoo.product.domain.vo.ProductFormulaRestrictionVo;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.domain.vo.ProductFormulaUsageRuleVo;
import com.bocoo.product.domain.vo.ProductFormulaVariableRuleVo;
import com.bocoo.product.domain.vo.ProductFormulaVariableVo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ProductPriceSnapshotReader {

    private static final JsonMapper JSON = JsonMapper.builder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final Set<String> AUDIT_FIELDS = Set.of(
        "createById", "createBy", "createTime", "updateBy", "updateTime", "delFlag"
    );
    public List<Map<String, Object>> priceMaterials(ProductFormulaVersion version) {
        return maps(snapshot(version).get("materials"));
    }

    public List<ProductFormulaMaterialVo> formulaMaterials(ProductFormulaVersion version) {
        return voList(snapshot(version).get("materials"), ProductFormulaMaterialVo.class);
    }

    public List<ProductFormulaOptionVo> formulaOptions(ProductFormulaVersion version) {
        return voList(snapshot(version).get("options"), ProductFormulaOptionVo.class);
    }

    public List<ProductFormulaOptionValueVo> formulaOptionValues(ProductFormulaVersion version) {
        return voList(snapshot(version).get("optionValues"), ProductFormulaOptionValueVo.class);
    }

    public List<ProductFormulaOptionMaterialVo> formulaOptionMaterials(ProductFormulaVersion version) {
        return voList(snapshot(version).get("optionMaterials"), ProductFormulaOptionMaterialVo.class);
    }

    public ProductFormulaSetupVo formulaSetup(ProductFormulaVersion version) {
        Map<String, Object> source = snapshot(version);
        ProductFormulaSetupVo setup = new ProductFormulaSetupVo();
        setup.setMaterials(voList(source.get("materials"), ProductFormulaMaterialVo.class));
        setup.setOptions(voList(source.get("options"), ProductFormulaOptionVo.class));
        setup.setOptionValues(voList(source.get("optionValues"), ProductFormulaOptionValueVo.class));
        setup.setOptionMaterials(voList(source.get("optionMaterials"), ProductFormulaOptionMaterialVo.class));
        setup.setRestrictions(voList(source.get("restrictions"), ProductFormulaRestrictionVo.class));
        setup.setUsageRules(voList(source.get("usageRules"), ProductFormulaUsageRuleVo.class));
        setup.setVariables(voList(source.get("variables"), ProductFormulaVariableVo.class));
        setup.setVariableRules(voList(source.get("variableRules"), ProductFormulaVariableRuleVo.class));
        return setup;
    }

    public Map<String, Integer> materialGroupCounts(ProductFormulaVersion version) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Map<String, Object> material : maps(snapshot(version).get("materials"))) {
            Object groupCode = material.get("attributeGroupCode");
            if (groupCode == null) {
                groupCode = material.get("materialTypeCode");
            }
            String code = String.valueOf(groupCode);
            counts.merge(StringUtils.isBlank(code) || "null".equals(code) ? "UNCLASSIFIED" : code, 1, Integer::sum);
        }
        return counts;
    }

    public Map<String, Map<String, Object>> priceSnapshotByMaterialCode(ProductFormulaVersion version) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (Map<String, Object> row : maps(snapshot(version).get("priceSnapshot"))) {
            Object materialCode = row.get("materialCode");
            if (materialCode != null && StringUtils.isNotBlank(String.valueOf(materialCode))) {
                result.put(String.valueOf(materialCode), row);
            }
        }
        return result;
    }

    private Map<String, Object> snapshot(ProductFormulaVersion version) {
        if (version == null || StringUtils.isBlank(version.getSetupSnapshotJson())) {
            return Map.of();
        }
        try {
            return JSON.readValue(version.getSetupSnapshotJson(), MAP_TYPE);
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private List<Map<String, Object>> maps(Object value) {
        if (!(value instanceof List<?> rows)) {
            return List.of();
        }
        return rows.stream()
            .filter(Map.class::isInstance)
            .map(row -> copyMap((Map<?, ?>) row))
            .toList();
    }

    private <T> List<T> voList(Object value, Class<T> targetType) {
        return maps(value).stream()
            .map(row -> JSON.convertValue(row, targetType))
            .toList();
    }

    private Map<String, Object> copyMap(Map<?, ?> source) {
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
            return copyMap(map);
        }
        if (value instanceof List<?> list) {
            return list.stream().map(this::cleanValue).toList();
        }
        return value;
    }

}
