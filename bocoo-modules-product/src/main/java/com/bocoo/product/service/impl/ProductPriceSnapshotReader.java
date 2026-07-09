package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.vo.ProductFormulaMaterialVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionMaterialVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionValueVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionVo;
import com.bocoo.product.domain.vo.ProductPriceOptionCombinationVo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
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
    private static final ProductPriceOptionCombinationVo DEFAULT_COMBINATION =
        new ProductPriceOptionCombinationVo("DEFAULT", "默认");

    public List<Map<String, Object>> fabricMaterials(ProductFormulaVersion version) {
        return maps(snapshot(version).get("materials")).stream()
            .filter(row -> "FABRIC".equals(String.valueOf(row.get("attributeGroupCode"))))
            .toList();
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

    public List<ProductPriceOptionCombinationVo> optionCombinations(ProductFormulaVersion version) {
        Map<String, Object> snapshot = snapshot(version);
        List<Map<String, Object>> dimensions = priceDimensions(maps(snapshot.get("options")));
        if (dimensions.isEmpty()) {
            return List.of(DEFAULT_COMBINATION);
        }
        List<List<Map<String, Object>>> valueGroups = dimensions.stream()
            .map(option -> valuesForOption(option, maps(snapshot.get("optionValues"))))
            .filter(values -> !values.isEmpty())
            .toList();
        if (valueGroups.isEmpty()) {
            return List.of(DEFAULT_COMBINATION);
        }
        List<ProductPriceOptionCombinationVo> result = new ArrayList<>();
        appendCombinations(result, valueGroups, 0, new ArrayList<>());
        return result.isEmpty() ? List.of(DEFAULT_COMBINATION) : result;
    }

    private List<Map<String, Object>> priceDimensions(List<Map<String, Object>> options) {
        return options.stream()
            .filter(option -> !"false".equalsIgnoreCase(String.valueOf(option.get("businessVisibleFlag"))))
            .filter(option -> !fabricOption(option))
            .map(option -> Map.entry(option, priceDimensionScore(option)))
            .filter(entry -> entry.getValue() > 0)
            .sorted(Comparator.<Map.Entry<Map<String, Object>, Integer>>comparingInt(Map.Entry::getValue).reversed())
            .limit(2)
            .map(Map.Entry::getKey)
            .toList();
    }

    private int priceDimensionScore(Map<String, Object> option) {
        String text = (value(option, "optionCode") + " " + value(option, "optionNameCn") + " " + value(option, "optionNameEn")).toLowerCase();
        int score = 0;
        if (containsAny(text, "style", "structure", "mode", "款式", "结构", "工艺", "柔式", "平铺", "前穿", "后穿", "堆叠")) {
            score += 3;
        }
        if (containsAny(text, "control", "system", "drive", "motor", "chain", "系统", "控制", "拉珠", "电动", "电机", "无拉")) {
            score += 3;
        }
        return score;
    }

    private boolean fabricOption(Map<String, Object> option) {
        String code = value(option, "optionCode");
        String scope = value(option, "sourceScope");
        String name = value(option, "optionNameCn");
        return "FABRIC".equalsIgnoreCase(code) || scope.contains("FABRIC") || name.contains("面料");
    }

    private List<Map<String, Object>> valuesForOption(Map<String, Object> option, List<Map<String, Object>> values) {
        String optionRefKey = value(option, "optionRefKey");
        String optionCode = value(option, "optionCode");
        return values.stream()
            .filter(row -> sameOption(optionRefKey, optionCode, row))
            .filter(row -> !"DISABLED".equals(value(row, "status")))
            .toList();
    }

    private void appendCombinations(List<ProductPriceOptionCombinationVo> result, List<List<Map<String, Object>>> groups,
                                    int index, List<Map<String, Object>> selected) {
        if (index >= groups.size()) {
            String key = selected.stream()
                .map(row -> stableValue(row, "optionRefKey", "optionCode") + "=" + stableValue(row, "valueRefKey", "valueCode"))
                .reduce((left, right) -> left + ";" + right)
                .orElse(DEFAULT_COMBINATION.getOptionCombinationKey());
            String name = selected.stream()
                .map(row -> StringUtils.blankToDefault(value(row, "valueNameCn"), value(row, "valueCode")))
                .reduce("", String::concat);
            result.add(new ProductPriceOptionCombinationVo(key, StringUtils.blankToDefault(name, DEFAULT_COMBINATION.getOptionCombinationName())));
            return;
        }
        for (Map<String, Object> value : groups.get(index)) {
            selected.add(value);
            appendCombinations(result, groups, index + 1, selected);
            selected.remove(selected.size() - 1);
        }
    }

    private boolean sameOption(String optionRefKey, String optionCode, Map<String, Object> row) {
        if (StringUtils.isNotBlank(optionRefKey)) {
            return optionRefKey.equals(value(row, "optionRefKey"));
        }
        return optionCode.equals(value(row, "optionCode"));
    }

    private String stableValue(Map<String, Object> row, String refField, String fallbackField) {
        return StringUtils.blankToDefault(value(row, refField), value(row, fallbackField));
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

    private String value(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private boolean containsAny(String text, String... patterns) {
        for (String pattern : patterns) {
            if (text.contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
