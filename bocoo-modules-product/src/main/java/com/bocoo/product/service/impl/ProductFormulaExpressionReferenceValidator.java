package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductBaseAttribute;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.mapper.ProductBaseAttributeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductFormulaExpressionReferenceValidator extends ProductServiceSupport {

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("\\b(option_[A-Za-z0-9_]+|material_[A-Za-z0-9_]+|fabric)\\b");
    private static final Pattern OPTION_COMPARE_PATTERN =
        Pattern.compile("\\b(option_([A-Za-z0-9_]+)|fabric)\\s*(==|!=)\\s*(['\"])(.*?)\\4");
    private static final Set<String> MATERIAL_FIELDS = Set.of("materialType", "materialCode", "materialName", "attributeGroup");

    private final ProductBaseAttributeMapper baseAttributeMapper;

    String validationMessageKey(String expression, List<ProductFormulaOption> options,
                                List<ProductFormulaOptionValue> values,
                                List<ProductFormulaOptionMaterial> optionMaterials,
                                List<ProductFormulaMaterial> materials) {
        if (StringUtils.isBlank(expression) || "DEFAULT".equals(expression)) {
            return null;
        }
        Map<String, ProductFormulaOption> optionByToken = optionTokenMap(options);
        Set<String> valueKeys = valueKeys(values);
        var matcher = IDENTIFIER_PATTERN.matcher(expression);
        while (matcher.find()) {
            String identifier = matcher.group(1);
            if ("fabric".equals(identifier)) {
                if (!optionByToken.containsKey("FABRIC")) return "product.formula.expressionReferenceInvalid";
            } else if (identifier.startsWith("option_")) {
                if (!optionByToken.containsKey(identifier.substring("option_".length()))) return "product.formula.expressionReferenceInvalid";
            } else if (invalidMaterialReference(identifier, options, optionMaterials, materials)) {
                return "product.formula.expressionReferenceInvalid";
            }
        }
        return invalidOptionValueReference(expression, optionByToken, valueKeys) ? "product.formula.expressionReferenceInvalid" : null;
    }

    private boolean invalidOptionValueReference(String expression, Map<String, ProductFormulaOption> optionByToken, Set<String> valueKeys) {
        var matcher = OPTION_COMPARE_PATTERN.matcher(expression);
        while (matcher.find()) {
            ProductFormulaOption option = optionByToken.get("fabric".equals(matcher.group(1)) ? "FABRIC" : matcher.group(2));
            if (option == null || invalidOptionValue(option, matcher.group(5), valueKeys)) {
                return true;
            }
        }
        return false;
    }

    private boolean invalidOptionValue(ProductFormulaOption option, String valueToken, Set<String> valueKeys) {
        return !valueKeys.contains(key(option.getOptionRefKey(), valueToken))
            && !valueKeys.contains(key(option.getOptionCode(), valueToken));
    }

    private boolean invalidMaterialReference(String identifier, List<ProductFormulaOption> options,
                                             List<ProductFormulaOptionMaterial> optionMaterials,
                                             List<ProductFormulaMaterial> materials) {
        String body = identifier.substring("material_".length());
        if (validMaterialPoolReference(body, materials)) {
            return false;
        }
        List<Map.Entry<String, ProductFormulaOption>> optionTokens = optionTokenMap(options).entrySet().stream()
            .sorted(Comparator.<Map.Entry<String, ProductFormulaOption>>comparingInt(entry -> identifierPart(entry.getKey()).length()).reversed())
            .toList();
        for (Map.Entry<String, ProductFormulaOption> entry : optionTokens) {
            String prefix = identifierPart(entry.getKey()) + "_";
            if (!body.startsWith(prefix)) {
                continue;
            }
            String attributeCode = body.substring(prefix.length());
            return !optionHasAttribute(entry.getValue(), attributeCode, optionMaterials, materials);
        }
        return true;
    }

    private boolean validMaterialPoolReference(String body, List<ProductFormulaMaterial> materials) {
        List<String> groupCodes = materials.stream()
            .map(ProductFormulaMaterial::getAttributeGroupCode)
            .filter(StringUtils::isNotBlank)
            .distinct()
            .sorted(Comparator.comparingInt((String value) -> identifierPart(value).length()).reversed())
            .toList();
        for (String groupCode : groupCodes) {
            String prefix = identifierPart(groupCode) + "_";
            if (!body.startsWith(prefix)) {
                continue;
            }
            String attributeCode = body.substring(prefix.length());
            if (MATERIAL_FIELDS.contains(attributeCode)) {
                return true;
            }
            Set<Long> materialIds = materials.stream()
                .filter(material -> groupCode.equals(material.getAttributeGroupCode()) && material.getMaterialId() != null)
                .map(ProductFormulaMaterial::getMaterialId)
                .collect(Collectors.toSet());
            return !materialIds.isEmpty() && groupHasAttribute(Set.of(groupCode), attributeCode);
        }
        return false;
    }

    private boolean optionHasAttribute(ProductFormulaOption option, String attributeCode,
                                       List<ProductFormulaOptionMaterial> optionMaterials,
                                       List<ProductFormulaMaterial> materials) {
        Map<String, ProductFormulaMaterial> materialByCode = materials.stream()
            .collect(Collectors.toMap(ProductFormulaMaterial::getMaterialCode, material -> material, (left, right) -> left));
        Set<Long> materialIds = optionMaterials.stream()
            .filter(row -> optionMatches(option, row))
            .map(ProductFormulaOptionMaterial::getMaterialCode)
            .map(materialByCode::get)
            .filter(material -> material != null && material.getMaterialId() != null)
            .map(ProductFormulaMaterial::getMaterialId)
            .collect(Collectors.toSet());
        if (materialIds.isEmpty()) {
            return false;
        }
        if (MATERIAL_FIELDS.contains(attributeCode)) {
            return true;
        }
        Set<String> groupCodes = materials.stream()
            .filter(material -> materialIds.contains(material.getMaterialId()))
            .map(ProductFormulaMaterial::getAttributeGroupCode)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());
        return groupHasAttribute(groupCodes, attributeCode);
    }

    private boolean optionMatches(ProductFormulaOption option, ProductFormulaOptionMaterial material) {
        return option != null
            && ((StringUtils.isNotBlank(option.getOptionRefKey()) && option.getOptionRefKey().equals(material.getOptionRefKey()))
            || (StringUtils.isNotBlank(option.getOptionCode()) && option.getOptionCode().equals(material.getOptionCode())));
    }

    private Map<String, ProductFormulaOption> optionTokenMap(List<ProductFormulaOption> options) {
        return options.stream()
            .flatMap(option -> java.util.stream.Stream.of(
                Map.entry(StringUtils.blankToDefault(option.getOptionRefKey(), ""), option),
                Map.entry(StringUtils.blankToDefault(option.getOptionCode(), ""), option)
            ))
            .filter(entry -> StringUtils.isNotBlank(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left));
    }

    private Set<String> valueKeys(List<ProductFormulaOptionValue> values) {
        return values.stream()
            .flatMap(value -> java.util.stream.Stream.of(
                key(value.getOptionRefKey(), value.getValueRefKey()),
                key(value.getOptionCode(), value.getValueCode()),
                key(value.getOptionRefKey(), value.getValueCode()),
                key(value.getOptionCode(), value.getValueRefKey())
            ))
            .collect(Collectors.toSet());
    }

    private boolean groupHasAttribute(Set<String> groupCodes, String attributeCode) {
        if (groupCodes.isEmpty() || StringUtils.isBlank(attributeCode)) {
            return false;
        }
        return baseAttributeMapper.selectCount(activeQuery(ProductBaseAttribute.class)
            .in("attribute_group_code", groupCodes)
            .eq("attribute_code", attributeCode)
            .eq("status", "ENABLED")) > 0;
    }

    private String identifierPart(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private String key(String... parts) {
        return java.util.Arrays.stream(parts)
            .map(part -> part == null ? "" : part)
            .collect(Collectors.joining("|"));
    }
}
