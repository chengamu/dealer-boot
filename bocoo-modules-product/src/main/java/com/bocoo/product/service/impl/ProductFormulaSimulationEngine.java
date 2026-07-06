package com.bocoo.product.service.impl;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.product.domain.bo.ProductFormulaSimulationBo;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.vo.ProductFormulaMaterialVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionMaterialVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionValueVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionVo;
import com.bocoo.product.domain.vo.ProductFormulaRestrictionVo;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.domain.vo.ProductFormulaSimulationItemVo;
import com.bocoo.product.domain.vo.ProductFormulaSimulationVo;
import com.bocoo.product.domain.vo.ProductFormulaUsageRuleVo;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.service.ProductFormulaVariableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
class ProductFormulaSimulationEngine extends ProductServiceSupport {
    private static final String VALIDATION_FAIL = "FAIL";
    private static final String VALIDATION_PASS = "PASS";
    private final ProductMaterialMapper productMaterialMapper;
    private final ProductFormulaSimulationUsageCalculator usageCalculator;
    private final ProductFormulaVariableService variableService;
    ProductFormulaSimulationVo run(Long formulaId, ProductFormula formula, ProductFormulaSetupVo setup,
                                   ProductFormulaSimulationBo bo, String setupMessageKey) {
        ProductFormulaSimulationVo vo = baseVo(formulaId, bo);
        String messageKey = setupMessageKey == null ? validateInput(formula, setup, vo) : setupMessageKey;
        if (messageKey != null) {
            return fail(vo, messageKey);
        }
        List<ProductFormulaMaterialVo> bomMaterials = resolveBomMaterials(setup, vo.getSelectedOptionValues());
        Map<String, Object> context = expressionContext(formula, vo);
        context.putAll(selectedOptionMaterialContext(setup, vo.getSelectedOptionValues()));
        messageKey = validateRestrictions(setup.getRestrictions(), vo.getSelectedOptionValues(), context);
        if (messageKey != null) {
            return fail(vo, messageKey);
        }
        try {
            context = variableService.evaluateVariables(setup.getVariables(), setup.getVariableRules(), context);
            List<ProductFormulaSimulationItemVo> items = buildItems(setup, bomMaterials, context);
            if (items.isEmpty()) {
                return fail(vo, "product.formula.notConfigured");
            }
            vo.setItems(items);
            BigDecimal singleAmount = items.stream().map(ProductFormulaSimulationItemVo::getAmount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
            vo.setSingleAmount(singleAmount);
            vo.setTotalAmount(singleAmount.multiply(BigDecimal.valueOf(vo.getOrderQuantity())).setScale(2, RoundingMode.HALF_UP));
            vo.setStatus(VALIDATION_PASS);
            vo.setMessage("product.formula.simulationPassed");
            return vo;
        } catch (IllegalArgumentException ex) {
            return fail(vo, "product.formula.usageFormulaInvalid");
        }
    }
    private ProductFormulaSimulationVo baseVo(Long formulaId, ProductFormulaSimulationBo bo) {
        ProductFormulaSimulationVo vo = new ProductFormulaSimulationVo();
        vo.setFormulaId(formulaId);
        vo.setOrderWidth(bo == null ? null : bo.getOrderWidth());
        vo.setOrderHeight(bo == null ? null : bo.getOrderHeight());
        vo.setOrderQuantity(bo == null || bo.getOrderQuantity() == null ? 1 : bo.getOrderQuantity());
        vo.setRoom(bo == null ? null : bo.getRoom());
        vo.setSelectedOptionValues(bo == null || bo.getSelectedOptionValues() == null
            ? new LinkedHashMap<>() : new LinkedHashMap<>(bo.getSelectedOptionValues()));
        vo.setSimulationTime(TimeUtils.utcNow());
        return vo;
    }
    private ProductFormulaSimulationVo fail(ProductFormulaSimulationVo vo, String messageKey) {
        vo.setStatus(VALIDATION_FAIL);
        vo.setMessage(messageKey);
        return vo;
    }
    private String validateInput(ProductFormula formula, ProductFormulaSetupVo setup, ProductFormulaSimulationVo vo) {
        BigDecimal width = vo.getOrderWidth();
        BigDecimal height = vo.getOrderHeight();
        if (width == null || height == null) {
            return "product.formula.simulationSizeRequired";
        }
        if (vo.getOrderQuantity() == null || vo.getOrderQuantity() < 1) {
            return "product.formula.simulationQuantityRequired";
        }
        if ((formula.getMinWidthInch() != null && width.compareTo(formula.getMinWidthInch()) < 0)
            || (formula.getMaxWidthInch() != null && width.compareTo(formula.getMaxWidthInch()) > 0)
            || (formula.getMinHeightInch() != null && height.compareTo(formula.getMinHeightInch()) < 0)
            || (formula.getMaxHeightInch() != null && height.compareTo(formula.getMaxHeightInch()) > 0)) {
            return "product.formula.simulationSizeOutOfRange";
        }
        return validateOptions(setup, vo.getSelectedOptionValues());
    }
    private String validateOptions(ProductFormulaSetupVo setup, Map<String, String> selectedValues) {
        Map<String, List<ProductFormulaOptionValueVo>> valuesByOption = setup.getOptionValues().stream()
            .filter(value -> isEnabledStatus(value.getStatus()) && StringUtils.isNotBlank(value.getOptionCode()))
            .collect(Collectors.groupingBy(ProductFormulaOptionValueVo::getOptionCode));
        for (ProductFormulaOptionVo option : setup.getOptions()) {
            if (!isEnabledStatus(option.getStatus())) {
                continue;
            }
            if (!isOptionVisible(option, selectedValues)) {
                continue;
            }
            String selected = selectedValues.get(option.getOptionCode());
            if (Boolean.TRUE.equals(option.getRequiredFlag()) && StringUtils.isBlank(selected)) {
                return "product.formula.simulationOptionRequired";
            }
            if (StringUtils.isNotBlank(selected) && valuesByOption.getOrDefault(option.getOptionCode(), List.of()).stream()
                .noneMatch(value -> selected.equals(value.getValueCode()))) {
                return "product.formula.simulationOptionInvalid";
            }
        }
        return null;
    }
    private boolean isOptionVisible(ProductFormulaOptionVo option, Map<String, String> selectedValues) {
        return !"CONDITIONAL".equals(option.getVisibilityMode())
            || (StringUtils.isNotBlank(option.getVisibleConditionOptionCode())
            && Objects.equals(selectedValues.get(option.getVisibleConditionOptionCode()), option.getVisibleConditionValueCode()));
    }
    private List<ProductFormulaMaterialVo> resolveBomMaterials(ProductFormulaSetupVo setup, Map<String, String> selectedValues) {
        Map<String, ProductFormulaMaterialVo> materialsByCode = setup.getMaterials().stream()
            .filter(material -> isEnabledStatus(material.getStatus()) && StringUtils.isNotBlank(material.getMaterialCode()))
            .collect(Collectors.toMap(ProductFormulaMaterialVo::getMaterialCode, material -> material, (l, r) -> l, LinkedHashMap::new));
        Map<String, ProductFormulaMaterialVo> bom = new LinkedHashMap<>();
        setup.getMaterials().stream().filter(material -> isEnabledStatus(material.getStatus()) && Boolean.TRUE.equals(material.getRequiredFlag()))
            .forEach(material -> bom.put(material.getMaterialCode(), material));
        setup.getOptionMaterials().stream()
            .filter(optionMaterial -> isEnabledStatus(optionMaterial.getStatus())
                && Objects.equals(selectedValues.get(optionMaterial.getOptionCode()), optionMaterial.getValueCode()))
            .map(ProductFormulaOptionMaterialVo::getMaterialCode).map(materialsByCode::get).filter(Objects::nonNull)
            .forEach(material -> bom.put(material.getMaterialCode(), material));
        return bom.values().stream()
            .sorted(Comparator.comparing(ProductFormulaMaterialVo::getSortOrder, Comparator.nullsLast(Integer::compareTo))).toList();
    }
    private List<ProductFormulaSimulationItemVo> buildItems(ProductFormulaSetupVo setup, List<ProductFormulaMaterialVo> bomMaterials,
                                                           Map<String, Object> context) {
        Map<Long, ProductMaterial> materialMap = batchMaterialMap(bomMaterials);
        Map<Long, List<ProductFormulaUsageRuleVo>> rulesByMaterial = setup.getUsageRules().stream()
            .filter(rule -> rule.getFormulaMaterialId() != null)
            .collect(Collectors.groupingBy(ProductFormulaUsageRuleVo::getFormulaMaterialId));
        return bomMaterials.stream().filter(material -> material.getMaterialId() != null)
            .map(material -> usageCalculator.buildItem(material, materialMap.get(material.getMaterialId()),
                rulesByMaterial.getOrDefault(material.getFormulaMaterialId(), List.of()), context))
            .toList();
    }
    private Map<Long, ProductMaterial> batchMaterialMap(List<ProductFormulaMaterialVo> materials) {
        Set<Long> materialIds = materials.stream().map(ProductFormulaMaterialVo::getMaterialId)
            .filter(Objects::nonNull).collect(Collectors.toSet());
        if (materialIds.isEmpty()) {
            return Map.of();
        }
        return productMaterialMapper.selectList(activeQuery(ProductMaterial.class).in("material_id", materialIds)).stream()
            .collect(Collectors.toMap(ProductMaterial::getMaterialId, material -> material, (left, right) -> left));
    }
    private Map<String, Object> expressionContext(ProductFormula formula, ProductFormulaSimulationVo vo) {
        double widthIn = vo.getOrderWidth().doubleValue();
        double lengthIn = vo.getOrderHeight().doubleValue();
        double widthCm = widthIn * 2.54D;
        double lengthCm = lengthIn * 2.54D;
        Map<String, Object> context = new HashMap<>();
        context.put("orderWidthIn", widthIn);
        context.put("orderLengthIn", lengthIn);
        context.put("orderWidthCm", widthCm);
        context.put("orderLengthCm", lengthCm);
        context.put("orderAreaM2", widthCm * lengthCm / 10000D);
        context.put("productType", formula.getProductTypeCode());
        context.put("fabric", vo.getSelectedOptionValues().getOrDefault("FABRIC", ""));
        context.put("optionValue", "");
        vo.getSelectedOptionValues().forEach((optionCode, valueCode) -> context.put("option_" + optionCode, valueCode));
        return context;
    }

    private Map<String, Object> selectedOptionMaterialContext(ProductFormulaSetupVo setup, Map<String, String> selectedValues) {
        Map<String, ProductFormulaMaterialVo> materialsByCode = setup.getMaterials().stream()
            .filter(material -> isEnabledStatus(material.getStatus()) && StringUtils.isNotBlank(material.getMaterialCode()))
            .collect(Collectors.toMap(ProductFormulaMaterialVo::getMaterialCode, material -> material, (left, right) -> left));
        Map<String, Object> context = new HashMap<>();
        setup.getOptionMaterials().stream()
            .filter(optionMaterial -> isEnabledStatus(optionMaterial.getStatus())
                && Objects.equals(selectedValues.get(optionMaterial.getOptionCode()), optionMaterial.getValueCode()))
            .sorted(Comparator.comparing(ProductFormulaOptionMaterialVo::getDefaultFlag, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ProductFormulaOptionMaterialVo::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
            .forEach(optionMaterial -> applyOptionMaterialContext(context, optionMaterial.getOptionCode(), materialsByCode.get(optionMaterial.getMaterialCode())));
        return context;
    }

    private void applyOptionMaterialContext(Map<String, Object> context, String optionCode, ProductFormulaMaterialVo material) {
        if (StringUtils.isBlank(optionCode) || material == null) {
            return;
        }
        String prefix = "material_" + identifierPart(optionCode) + "_";
        context.putIfAbsent(prefix + "materialType", material.getMaterialTypeCode());
        context.putIfAbsent(prefix + "materialCode", material.getMaterialCode());
        context.putIfAbsent(prefix + "materialName", material.getMaterialNameCn());
        if (material.getAttributeList() == null) {
            return;
        }
        material.getAttributeList().forEach(attribute -> {
            if (StringUtils.isNotBlank(attribute.getAttributeCode())) {
                context.putIfAbsent(prefix + identifierPart(attribute.getAttributeCode()), attributeValue(attribute));
            }
        });
    }

    private Object attributeValue(com.bocoo.product.domain.vo.ProductMaterialAttributeVo attribute) {
        if (attribute.getValueNumber() != null) {
            return attribute.getValueNumber().doubleValue();
        }
        if (attribute.getValueBool() != null) {
            return attribute.getValueBool();
        }
        return attribute.getValueText();
    }

    private String identifierPart(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private String validateRestrictions(List<ProductFormulaRestrictionVo> restrictions, Map<String, String> selectedValues,
                                        Map<String, Object> context) {
        for (ProductFormulaRestrictionVo restriction : restrictions) {
            if (!isEnabledStatus(restriction.getStatus())) {
                continue;
            }
            if (restrictionTargetsSelection(restriction, selectedValues) && restrictionConditionMatched(restriction, selectedValues, context)
                && "DISABLE".equals(restriction.getActionType())) {
                return StringUtils.blankToDefault(restriction.getMessageText(), "product.formula.simulationRestrictionHit");
            }
        }
        return null;
    }
    private boolean restrictionTargetsSelection(ProductFormulaRestrictionVo restriction, Map<String, String> selectedValues) {
        if (StringUtils.isBlank(restriction.getTargetOptionCode())) {
            return true;
        }
        String selected = selectedValues.get(restriction.getTargetOptionCode());
        return StringUtils.isBlank(restriction.getTargetValueCode()) || Objects.equals(selected, restriction.getTargetValueCode());
    }
    private boolean restrictionConditionMatched(ProductFormulaRestrictionVo restriction, Map<String, String> selectedValues,
                                                Map<String, Object> context) {
        return switch (StringUtils.blankToDefault(restriction.getConditionType(), "")) {
            case "EXPRESSION" -> ProductFormulaExpressionValidator.evaluateCondition(restriction.getConditionExpression(), context);
            case "OPTION_VALUE" -> compareString(selectedValues.get(restriction.getConditionOptionCode()),
                restriction.getConditionValueCode(), restriction.getConditionOperator());
            case "WIDTH" -> compareNumber(numberValue(context.get("orderWidthIn")), restriction.getConditionValueNumber(), restriction.getConditionOperator());
            case "HEIGHT" -> compareNumber(numberValue(context.get("orderLengthIn")), restriction.getConditionValueNumber(), restriction.getConditionOperator());
            default -> false;
        };
    }
    private boolean compareString(String left, String right, String operator) {
        boolean equals = Objects.equals(left, right);
        return "NE".equals(operator) || "!=".equals(operator) ? !equals : equals;
    }
    private boolean compareNumber(BigDecimal left, BigDecimal right, String operator) {
        if (left == null || right == null) {
            return false;
        }
        int compared = left.compareTo(right);
        return switch (StringUtils.blankToDefault(operator, "EQ")) {
            case "GT", ">" -> compared > 0;
            case "GE", ">=" -> compared >= 0;
            case "LT", "<" -> compared < 0;
            case "LE", "<=" -> compared <= 0;
            case "NE", "!=" -> compared != 0;
            default -> compared == 0;
        };
    }
    private BigDecimal numberValue(Object value) {
        return value instanceof Number number ? BigDecimal.valueOf(number.doubleValue()) : null;
    }
}
