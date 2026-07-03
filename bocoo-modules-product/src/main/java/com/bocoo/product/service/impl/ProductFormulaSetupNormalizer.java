package com.bocoo.product.service.impl;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.bo.ProductFormulaMaterialBo;
import com.bocoo.product.domain.bo.ProductFormulaOptionBo;
import com.bocoo.product.domain.bo.ProductFormulaOptionMaterialBo;
import com.bocoo.product.domain.bo.ProductFormulaOptionValueBo;
import com.bocoo.product.domain.bo.ProductFormulaSetupBo;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaRestriction;
import com.bocoo.product.domain.entity.ProductFormulaVariable;
import com.bocoo.product.domain.entity.ProductFormulaVariableRule;
import com.bocoo.product.service.ProductFormulaUsageRuleService;
import com.bocoo.product.service.ProductFormulaVariableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
public class ProductFormulaSetupNormalizer extends ProductServiceSupport {
    private static final String VISIBILITY_ALWAYS = "ALWAYS";
    private static final String VISIBILITY_CONDITIONAL = "CONDITIONAL";
    private final ProductFormulaMaterialSnapshotResolver materialSnapshotResolver;
    private final ProductFormulaRestrictionNormalizer restrictionNormalizer;
    private final ProductFormulaUsageRuleService usageRuleService;
    private final ProductFormulaVariableService variableService;
    ProductFormulaSetupRows normalize(Long formulaId, ProductFormulaSetupBo bo) {
        ProductFormulaSetupBo safeBo = bo == null ? new ProductFormulaSetupBo() : bo;
        List<ProductFormulaMaterial> materials = normalizeMaterials(formulaId, safeBo.getMaterials());
        List<ProductFormulaOption> options = normalizeOptions(formulaId, safeBo.getOptions());
        List<ProductFormulaOptionValue> values = normalizeOptionValues(formulaId, options, safeBo.getOptionValues());
        normalizeOptionVisibility(options, values);
        List<ProductFormulaOptionMaterial> optionMaterials =
            normalizeOptionMaterials(formulaId, materials, options, values, safeBo.getOptionMaterials());
        List<ProductFormulaRestriction> restrictions = restrictionNormalizer.normalize(formulaId, options, values, safeBo.getRestrictions());
        var usageRules = usageRuleService.normalize(formulaId, materials, options, values, safeBo.getUsageRules());
        List<ProductFormulaVariable> variables = variableService.normalizeVariables(formulaId, safeBo.getVariables());
        List<ProductFormulaVariableRule> variableRules = variableService.normalizeRules(formulaId, variables, safeBo.getVariableRules());
        return new ProductFormulaSetupRows(materials, options, values, optionMaterials, restrictions, usageRules, variables, variableRules);
    }
    ProductFormulaSetupRows normalizeMaterials(Long formulaId, ProductFormulaSetupBo bo, ProductFormulaSetupContext current) {
        ProductFormulaSetupBo safeBo = bo == null ? new ProductFormulaSetupBo() : bo;
        List<ProductFormulaMaterial> materials = normalizeMaterials(formulaId, safeBo.getMaterials());
        Set<String> materialCodes = materials.stream().map(ProductFormulaMaterial::getMaterialCode).collect(Collectors.toSet());
        for (ProductFormulaOptionMaterial optionMaterial : current.optionMaterials()) {
            if (!materialCodes.contains(optionMaterial.getMaterialCode())) {
                throw ServiceException.ofMessageKey("product.formula.optionMaterialNotInPool");
            }
        }
        var usageRules = usageRuleService.normalize(formulaId, materials, current.options(), current.values(), safeBo.getUsageRules());
        return new ProductFormulaSetupRows(materials, current.options(), current.values(), current.optionMaterials(), current.restrictions(),
            usageRules, current.variables(), current.variableRules());
    }
    ProductFormulaSetupRows normalizeOptions(Long formulaId, ProductFormulaSetupBo bo, ProductFormulaSetupContext current) {
        ProductFormulaSetupBo safeBo = bo == null ? new ProductFormulaSetupBo() : bo;
        List<ProductFormulaOption> options = normalizeOptions(formulaId, safeBo.getOptions());
        List<ProductFormulaOptionValue> values = normalizeOptionValues(formulaId, options, safeBo.getOptionValues());
        normalizeOptionVisibility(options, values);
        List<ProductFormulaOptionMaterial> optionMaterials = normalizeOptionMaterials(formulaId, current.materials(), options, values, safeBo.getOptionMaterials());
        List<ProductFormulaRestriction> restrictions = restrictionNormalizer.normalize(formulaId, options, values, safeBo.getRestrictions());
        return new ProductFormulaSetupRows(current.materials(), options, values, optionMaterials, restrictions, current.usageRules(),
            current.variables(), current.variableRules());
    }
    private List<ProductFormulaMaterial> normalizeMaterials(Long formulaId, List<ProductFormulaMaterialBo> rows) {
        List<ProductFormulaMaterial> result = new ArrayList<>();
        int index = 0;
        for (ProductFormulaMaterialBo row : rows == null ? List.<ProductFormulaMaterialBo>of() : rows) {
            ProductFormulaMaterial entity = MapstructUtils.convert(row, ProductFormulaMaterial.class);
            if (entity == null) {
                continue;
            }
            entity.setFormulaId(formulaId);
            entity.setMaterialCode(trim(entity.getMaterialCode()));
            entity.setMaterialNameCn(trim(entity.getMaterialNameCn()));
            entity.setUsageMode(defaultString(trim(entity.getUsageMode()), "FIXED"));
            entity.setUsageFormula(trim(entity.getUsageFormula()));
            entity.setCalculationUnitCode(trim(entity.getCalculationUnitCode()));
            entity.setRoundingMode(trim(entity.getRoundingMode()));
            entity.setProductionRemark(trim(entity.getProductionRemark()));
            entity.setStatus(defaultString(trim(entity.getStatus()), STATUS_ENABLED));
            entity.setDelFlag("0");
            entity.setSortOrder(entity.getSortOrder() == null ? index * 10 + 10 : entity.getSortOrder());
            entity.setLineNo(entity.getLineNo() == null ? index + 1 : entity.getLineNo());
            result.add(entity);
            index++;
        }
        validateMaterialSnapshots(result);
        return result;
    }
    private void validateMaterialSnapshots(List<ProductFormulaMaterial> materials) {
        Set<Long> materialIds = new HashSet<>();
        materialSnapshotResolver.resolve(materials);
        for (ProductFormulaMaterial entity : materials) {
            if (entity.getMaterialId() == null) {
                throw ServiceException.ofMessageKey("product.formula.materialRequired");
            }
            if (!materialIds.add(entity.getMaterialId())) {
                throw ServiceException.ofMessageKey("product.formula.materialDuplicate");
            }
        }
    }
    private List<ProductFormulaOption> normalizeOptions(Long formulaId, List<ProductFormulaOptionBo> rows) {
        List<ProductFormulaOption> result = new ArrayList<>();
        Set<String> codes = new HashSet<>();
        int index = 0;
        for (ProductFormulaOptionBo row : rows == null ? List.<ProductFormulaOptionBo>of() : rows) {
            ProductFormulaOption entity = MapstructUtils.convert(row, ProductFormulaOption.class);
            if (entity == null) {
                continue;
            }
            entity.setFormulaId(formulaId);
            entity.setOptionCode(requiredUpper(entity.getOptionCode(), "product.formula.optionCodeRequired"));
            entity.setOptionNameCn(requiredTrim(entity.getOptionNameCn(), "product.formula.optionNameRequired"));
            entity.setOptionNameEn(trim(entity.getOptionNameEn()));
            if (!codes.add(entity.getOptionCode())) {
                throw ServiceException.ofMessageKey("product.formula.optionCodeDuplicate");
            }
            entity.setSourceType(defaultString(trim(entity.getSourceType()), "MANUAL"));
            entity.setSourceScope(trim(entity.getSourceScope()));
            entity.setSelectionMode(defaultString(trim(entity.getSelectionMode()), "SINGLE"));
            entity.setDefaultValueCode(trim(entity.getDefaultValueCode()));
            entity.setDefaultValueNameCn(trim(entity.getDefaultValueNameCn()));
            entity.setVisibilityMode(defaultString(trimUpper(row.getVisibilityMode()), VISIBILITY_ALWAYS));
            entity.setVisibleConditionOptionCode(trimUpper(row.getVisibleConditionOptionCode()));
            entity.setVisibleConditionOptionNameCn(trim(row.getVisibleConditionOptionNameCn()));
            entity.setVisibleConditionValueCode(trimUpper(row.getVisibleConditionValueCode()));
            entity.setVisibleConditionValueNameCn(trim(row.getVisibleConditionValueNameCn()));
            entity.setRequiredFlag(Boolean.TRUE.equals(entity.getRequiredFlag()));
            entity.setBusinessVisibleFlag(entity.getBusinessVisibleFlag() == null || Boolean.TRUE.equals(entity.getBusinessVisibleFlag()));
            entity.setStatus(defaultString(trim(entity.getStatus()), STATUS_ENABLED));
            entity.setDelFlag("0");
            entity.setSortOrder(entity.getSortOrder() == null ? index * 10 + 10 : entity.getSortOrder());
            result.add(entity);
            index++;
        }
        return result;
    }
    private void normalizeOptionVisibility(List<ProductFormulaOption> options, List<ProductFormulaOptionValue> values) {
        Map<String, ProductFormulaOption> optionMap = options.stream()
            .collect(Collectors.toMap(ProductFormulaOption::getOptionCode, Function.identity(), (left, right) -> left));
        Map<String, ProductFormulaOptionValue> valueMap = values.stream()
            .collect(Collectors.toMap(value -> key(value.getOptionCode(), value.getValueCode()), Function.identity(), (left, right) -> left));
        for (ProductFormulaOption option : options) {
            if (!VISIBILITY_CONDITIONAL.equals(option.getVisibilityMode())) {
                option.setVisibilityMode(VISIBILITY_ALWAYS);
                clearOptionVisibilityCondition(option);
                continue;
            }
            applyVisibilityCondition(option, optionMap, valueMap);
        }
    }
    private void applyVisibilityCondition(ProductFormulaOption option, Map<String, ProductFormulaOption> optionMap,
                                          Map<String, ProductFormulaOptionValue> valueMap) {
        String conditionOptionCode = option.getVisibleConditionOptionCode();
        if (StringUtils.isBlank(conditionOptionCode)) {
            throw ServiceException.ofMessageKey("product.formula.optionVisibilityConditionRequired");
        }
        if (conditionOptionCode.equals(option.getOptionCode())) {
            throw ServiceException.ofMessageKey("product.formula.optionVisibilitySelfDenied");
        }
        ProductFormulaOption conditionOption = optionMap.get(conditionOptionCode);
        if (conditionOption == null) {
            throw ServiceException.ofMessageKey("product.formula.optionVisibilityConditionInvalid");
        }
        String conditionValueCode = option.getVisibleConditionValueCode();
        if (StringUtils.isBlank(conditionValueCode)) {
            throw ServiceException.ofMessageKey("product.formula.optionVisibilityValueRequired");
        }
        ProductFormulaOptionValue conditionValue = valueMap.get(key(conditionOptionCode, conditionValueCode));
        if (conditionValue == null) {
            throw ServiceException.ofMessageKey("product.formula.optionVisibilityValueInvalid");
        }
        option.setVisibleConditionOptionNameCn(conditionOption.getOptionNameCn());
        option.setVisibleConditionValueNameCn(conditionValue.getValueNameCn());
    }
    private void clearOptionVisibilityCondition(ProductFormulaOption option) {
        option.setVisibleConditionOptionCode(null);
        option.setVisibleConditionOptionNameCn(null);
        option.setVisibleConditionValueCode(null);
        option.setVisibleConditionValueNameCn(null);
    }
    private List<ProductFormulaOptionValue> normalizeOptionValues(Long formulaId, List<ProductFormulaOption> options,
                                                                  List<ProductFormulaOptionValueBo> rows) {
        Map<String, ProductFormulaOption> optionMap =
            options.stream().collect(Collectors.toMap(ProductFormulaOption::getOptionCode, Function.identity()));
        Set<String> keys = new HashSet<>();
        List<ProductFormulaOptionValue> result = new ArrayList<>();
        int index = 0;
        for (ProductFormulaOptionValueBo row : rows == null ? List.<ProductFormulaOptionValueBo>of() : rows) {
            ProductFormulaOptionValue entity = MapstructUtils.convert(row, ProductFormulaOptionValue.class);
            if (entity == null) {
                continue;
            }
            entity.setFormulaId(formulaId);
            entity.setOptionCode(requiredUpper(entity.getOptionCode(), "product.formula.optionCodeRequired"));
            ProductFormulaOption option = optionMap.get(entity.getOptionCode());
            if (option == null) {
                throw ServiceException.ofMessageKey("product.formula.optionValueOptionInvalid");
            }
            entity.setOptionId(option.getOptionId());
            entity.setValueCode(requiredUpper(entity.getValueCode(), "product.formula.optionValueCodeRequired"));
            entity.setValueNameCn(requiredTrim(entity.getValueNameCn(), "product.formula.optionValueNameRequired"));
            entity.setValueNameEn(trim(entity.getValueNameEn()));
            if (!keys.add(key(entity.getOptionCode(), entity.getValueCode()))) {
                throw ServiceException.ofMessageKey("product.formula.optionValueDuplicate");
            }
            entity.setDefaultFlag(Boolean.TRUE.equals(entity.getDefaultFlag()));
            entity.setStatus(defaultString(trim(entity.getStatus()), STATUS_ENABLED));
            entity.setDelFlag("0");
            entity.setSortOrder(entity.getSortOrder() == null ? index * 10 + 10 : entity.getSortOrder());
            result.add(entity);
            index++;
        }
        return result;
    }
    private List<ProductFormulaOptionMaterial> normalizeOptionMaterials(Long formulaId, List<ProductFormulaMaterial> materials,
                                                                        List<ProductFormulaOption> options, List<ProductFormulaOptionValue> values,
                                                                        List<ProductFormulaOptionMaterialBo> rows) {
        Map<Long, ProductFormulaMaterial> materialById = materials.stream()
            .filter(material -> material.getFormulaMaterialId() != null)
            .collect(Collectors.toMap(ProductFormulaMaterial::getFormulaMaterialId, Function.identity(), (left, right) -> left));
        Map<String, ProductFormulaMaterial> materialByCode = materials.stream()
            .collect(Collectors.toMap(ProductFormulaMaterial::getMaterialCode, Function.identity(), (left, right) -> left));
        Set<String> optionCodes = options.stream().map(ProductFormulaOption::getOptionCode).collect(Collectors.toSet());
        Set<String> valueKeys = values.stream().map(value -> key(value.getOptionCode(), value.getValueCode())).collect(Collectors.toSet());
        Set<String> duplicateKeys = new HashSet<>();
        List<ProductFormulaOptionMaterial> result = new ArrayList<>();
        int index = 0;
        for (ProductFormulaOptionMaterialBo row : rows == null ? List.<ProductFormulaOptionMaterialBo>of() : rows) {
            ProductFormulaOptionMaterial entity = MapstructUtils.convert(row, ProductFormulaOptionMaterial.class);
            if (entity == null) {
                continue;
            }
            entity.setFormulaId(formulaId);
            entity.setOptionCode(requiredUpper(entity.getOptionCode(), "product.formula.optionCodeRequired"));
            entity.setValueCode(requiredUpper(entity.getValueCode(), "product.formula.optionValueCodeRequired"));
            validateOptionMaterialValue(entity, optionCodes, valueKeys);
            ProductFormulaMaterial material = resolveOptionMaterial(entity, materialById, materialByCode);
            entity.setFormulaMaterialId(material.getFormulaMaterialId());
            entity.setMaterialId(material.getMaterialId());
            entity.setMaterialCode(material.getMaterialCode());
            entity.setMaterialNameCn(material.getMaterialNameCn());
            if (!duplicateKeys.add(key(entity.getOptionCode(), entity.getValueCode(), entity.getMaterialCode()))) {
                throw ServiceException.ofMessageKey("product.formula.optionMaterialDuplicate");
            }
            entity.setRequiredFlag(Boolean.TRUE.equals(entity.getRequiredFlag()));
            entity.setDefaultFlag(Boolean.TRUE.equals(entity.getDefaultFlag()));
            entity.setStatus(defaultString(trim(entity.getStatus()), STATUS_ENABLED));
            entity.setDelFlag("0");
            entity.setSortOrder(entity.getSortOrder() == null ? index * 10 + 10 : entity.getSortOrder());
            result.add(entity);
            index++;
        }
        return result;
    }
    private void validateOptionMaterialValue(ProductFormulaOptionMaterial entity, Set<String> optionCodes, Set<String> valueKeys) {
        if (!optionCodes.contains(entity.getOptionCode()) || !valueKeys.contains(key(entity.getOptionCode(), entity.getValueCode()))) {
            throw ServiceException.ofMessageKey("product.formula.optionMaterialValueInvalid");
        }
    }
    private ProductFormulaMaterial resolveOptionMaterial(ProductFormulaOptionMaterial entity,
                                                         Map<Long, ProductFormulaMaterial> materialById,
                                                         Map<String, ProductFormulaMaterial> materialByCode) {
        ProductFormulaMaterial material = entity.getFormulaMaterialId() == null ? null : materialById.get(entity.getFormulaMaterialId());
        if (material == null && StringUtils.isNotBlank(entity.getMaterialCode())) {
            material = materialByCode.get(entity.getMaterialCode());
        }
        if (material == null) {
            throw ServiceException.ofMessageKey("product.formula.optionMaterialNotInPool");
        }
        return material;
    }
    private String requiredTrim(String value, String messageKey) {
        String trimmed = trim(value);
        if (StringUtils.isBlank(trimmed)) {
            throw ServiceException.ofMessageKey(messageKey);
        }
        return trimmed;
    }
    private String requiredUpper(String value, String messageKey) {
        String trimmed = trimUpper(value);
        if (StringUtils.isBlank(trimmed)) {
            throw ServiceException.ofMessageKey(messageKey);
        }
        return trimmed;
    }
    private String trimUpper(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toUpperCase(java.util.Locale.ROOT);
    }
    private String trim(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }
    private String defaultString(String value, String defaultValue) {
        return StringUtils.isBlank(value) ? defaultValue : value;
    }
    private String key(String... parts) {
        return String.join("|", parts);
    }
}
