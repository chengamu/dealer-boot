package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.vo.ProductFormulaOptionValueVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductPriceConditionSnapshotFactory {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Pattern OPTION_PATTERN = Pattern.compile("option_([A-Za-z0-9_]+)\\s*(==|!=)\\s*[\"']([^\"']+)[\"']");
    private static final Pattern OPTION_REFERENCE_PATTERN = Pattern.compile("\\boption_([A-Za-z0-9_]+)\\b");
    private static final Pattern MEASURE_PATTERN = Pattern.compile("\\b(width|drop|widthCm|dropCm|areaM2|areaSqft)\\b");

    private final ProductPriceSnapshotReader snapshotReader;

    Snapshot snapshot(ProductFormulaVersion version, String expression, String text) {
        String normalized = ProductFormulaExpressionNormalizer.normalizeConditionExpression(expression);
        if ("DEFAULT".equals(normalized)) {
            return new Snapshot(ProductFormulaConditionJsonFactory.defaultCondition(), "DEFAULT");
        }
        List<ProductFormulaOptionVo> options = snapshotReader.formulaOptions(version);
        List<ProductFormulaOptionValueVo> values = snapshotReader.formulaOptionValues(version);
        Map<String, ProductFormulaOptionVo> optionByRef = options.stream()
            .filter(row -> StringUtils.isNotBlank(row.getOptionRefKey()))
            .collect(Collectors.toMap(ProductFormulaOptionVo::getOptionRefKey, row -> row, (left, right) -> left));
        Map<String, ProductFormulaOptionValueVo> valueByRef = values.stream()
            .filter(row -> StringUtils.isNotBlank(row.getValueRefKey()))
            .collect(Collectors.toMap(ProductFormulaOptionValueVo::getValueRefKey, row -> row, (left, right) -> left));
        List<Map<String, Object>> references = new ArrayList<>();
        Set<String> referencedOptions = OPTION_REFERENCE_PATTERN.matcher(normalized).results()
            .map(result -> result.group(1)).collect(Collectors.toSet());
        Set<String> matchedOptions = new java.util.HashSet<>();
        Matcher optionMatcher = OPTION_PATTERN.matcher(normalized);
        while (optionMatcher.find()) {
            ProductFormulaOptionVo option = optionByRef.get(optionMatcher.group(1));
            ProductFormulaOptionValueVo value = valueByRef.get(optionMatcher.group(3));
            if (option == null || value == null || !option.getOptionRefKey().equals(value.getOptionRefKey())) {
                throw ServiceException.ofMessageKey("product.priceSetting.conditionReferenceInvalid");
            }
            Map<String, Object> reference = new LinkedHashMap<>();
            reference.put("type", "OPTION_VALUE");
            reference.put("operator", optionMatcher.group(2));
            reference.put("optionRefKey", option.getOptionRefKey());
            reference.put("optionCodeSnapshot", option.getOptionCode());
            reference.put("optionNameSnapshot", option.getOptionNameCn());
            reference.put("valueRefKey", value.getValueRefKey());
            reference.put("valueCodeSnapshot", value.getValueCode());
            reference.put("valueNameSnapshot", value.getValueNameCn());
            references.add(reference);
            matchedOptions.add(option.getOptionRefKey());
        }
        if (!matchedOptions.containsAll(referencedOptions)) {
            throw ServiceException.ofMessageKey("product.priceSetting.conditionReferenceInvalid");
        }
        Set<String> measures = MEASURE_PATTERN.matcher(normalized).results().map(result -> result.group(1)).collect(Collectors.toSet());
        measures.forEach(field -> references.add(Map.of("type", "MEASURE", "field", field)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "EXPRESSION");
        body.put("expression", normalized);
        body.put("text", StringUtils.blankToDefault(text, normalized));
        body.put("references", references);
        try {
            return new Snapshot(JSON.writeValueAsString(body), normalized);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build price condition snapshot", e);
        }
    }

    record Snapshot(String json, String key) {
    }
}
