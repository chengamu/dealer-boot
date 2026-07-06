package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductMaterialAttribute;
import com.bocoo.product.mapper.ProductMaterialAttributeMapper;
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

    private final ProductMaterialAttributeMapper materialAttributeMapper;

    String validationMessageKey(String expression, List<ProductFormulaOption> options,
                                List<ProductFormulaOptionValue> values,
                                List<ProductFormulaOptionMaterial> optionMaterials,
                                List<ProductFormulaMaterial> materials) {
        if (StringUtils.isBlank(expression) || "DEFAULT".equals(expression)) {
            return null;
        }
        Set<String> optionCodes = options.stream().map(ProductFormulaOption::getOptionCode).collect(Collectors.toSet());
        Set<String> valueKeys = values.stream().map(value -> key(value.getOptionCode(), value.getValueCode())).collect(Collectors.toSet());
        var matcher = IDENTIFIER_PATTERN.matcher(expression);
        while (matcher.find()) {
            String identifier = matcher.group(1);
            if ("fabric".equals(identifier)) {
                if (!optionCodes.contains("FABRIC")) return "product.formula.expressionReferenceInvalid";
            } else if (identifier.startsWith("option_")) {
                if (!optionCodes.contains(identifier.substring("option_".length()))) return "product.formula.expressionReferenceInvalid";
            } else if (invalidMaterialReference(identifier, options, optionMaterials, materials)) {
                return "product.formula.expressionReferenceInvalid";
            }
        }
        return invalidOptionValueReference(expression, valueKeys) ? "product.formula.expressionReferenceInvalid" : null;
    }

    private boolean invalidOptionValueReference(String expression, Set<String> valueKeys) {
        var matcher = OPTION_COMPARE_PATTERN.matcher(expression);
        while (matcher.find()) {
            String optionCode = "fabric".equals(matcher.group(1)) ? "FABRIC" : matcher.group(2);
            if (!valueKeys.contains(key(optionCode, matcher.group(5)))) {
                return true;
            }
        }
        return false;
    }

    private boolean invalidMaterialReference(String identifier, List<ProductFormulaOption> options,
                                             List<ProductFormulaOptionMaterial> optionMaterials,
                                             List<ProductFormulaMaterial> materials) {
        String body = identifier.substring("material_".length());
        if (validMaterialPoolReference(body, materials)) {
            return false;
        }
        List<String> optionCodes = options.stream().map(ProductFormulaOption::getOptionCode)
            .filter(StringUtils::isNotBlank)
            .sorted(Comparator.comparingInt((String value) -> identifierPart(value).length()).reversed())
            .toList();
        for (String optionCode : optionCodes) {
            String prefix = identifierPart(optionCode) + "_";
            if (!body.startsWith(prefix)) {
                continue;
            }
            String attributeCode = body.substring(prefix.length());
            return !optionHasAttribute(optionCode, attributeCode, optionMaterials, materials);
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
            return !materialIds.isEmpty() && !materialAttributeMapper.selectList(activeQuery(ProductMaterialAttribute.class)
                .in("material_id", materialIds)
                .eq("attribute_code", attributeCode)).isEmpty();
        }
        return false;
    }

    private boolean optionHasAttribute(String optionCode, String attributeCode,
                                       List<ProductFormulaOptionMaterial> optionMaterials,
                                       List<ProductFormulaMaterial> materials) {
        Map<String, ProductFormulaMaterial> materialByCode = materials.stream()
            .collect(Collectors.toMap(ProductFormulaMaterial::getMaterialCode, material -> material, (left, right) -> left));
        Set<Long> materialIds = optionMaterials.stream()
            .filter(row -> optionCode.equals(row.getOptionCode()))
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
        return !materialAttributeMapper.selectList(activeQuery(ProductMaterialAttribute.class)
            .in("material_id", materialIds)
            .eq("attribute_code", attributeCode)).isEmpty();
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
