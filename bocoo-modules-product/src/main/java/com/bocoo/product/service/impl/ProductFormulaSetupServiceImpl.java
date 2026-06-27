package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.product.domain.bo.ProductFormulaMaterialBo;
import com.bocoo.product.domain.bo.ProductFormulaOptionBo;
import com.bocoo.product.domain.bo.ProductFormulaOptionMaterialBo;
import com.bocoo.product.domain.bo.ProductFormulaOptionValueBo;
import com.bocoo.product.domain.bo.ProductFormulaRestrictionBo;
import com.bocoo.product.domain.bo.ProductFormulaSetupBo;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaRestriction;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductUnit;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductFormulaMaterialMapper;
import com.bocoo.product.mapper.ProductFormulaOptionMapper;
import com.bocoo.product.mapper.ProductFormulaOptionMaterialMapper;
import com.bocoo.product.mapper.ProductFormulaOptionValueMapper;
import com.bocoo.product.mapper.ProductFormulaRestrictionMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductUnitMapper;
import com.bocoo.product.service.ProductChangeLogService;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductFormulaSetupService;
import com.bocoo.product.service.ProductFormulaUsageRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductFormulaSetupServiceImpl extends ProductServiceSupport implements ProductFormulaSetupService {

    private static final String STATUS_DRAFT = ProductFormulaServiceImpl.STATUS_DRAFT;
    private static final String STATUS_REJECTED = ProductFormulaServiceImpl.STATUS_REJECTED;
    private static final String VALIDATION_NOT_VALIDATED = "NOT_VALIDATED";
    private static final String VISIBILITY_ALWAYS = "ALWAYS";
    private static final String VISIBILITY_CONDITIONAL = "CONDITIONAL";

    private final ProductFormulaMapper formulaMapper;
    private final ProductFormulaMaterialMapper materialMapper;
    private final ProductFormulaOptionMapper optionMapper;
    private final ProductFormulaOptionValueMapper optionValueMapper;
    private final ProductFormulaOptionMaterialMapper optionMaterialMapper;
    private final ProductFormulaRestrictionMapper restrictionMapper;
    private final ProductMaterialMapper productMaterialMapper;
    private final ProductUnitMapper unitMapper;
    private final ProductChangeLogService changeLogService;
    private final ProductFormulaUsageRuleService usageRuleService;

    @Override
    public ProductFormulaSetupVo querySetup(Long formulaId) {
        ProductFormulaSetupVo vo = new ProductFormulaSetupVo();
        vo.setFormula(formulaMapper.selectVoById(formulaId));
        vo.setMaterials(materialMapper.selectVoList(activeQuery(ProductFormulaMaterial.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "line_no", "formula_material_id")));
        vo.setOptions(optionMapper.selectVoList(activeQuery(ProductFormulaOption.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "option_id")));
        vo.setOptionValues(optionValueMapper.selectVoList(activeQuery(ProductFormulaOptionValue.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "option_value_id")));
        vo.setOptionMaterials(optionMaterialMapper.selectVoList(activeQuery(ProductFormulaOptionMaterial.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "option_material_id")));
        vo.setRestrictions(restrictionMapper.selectVoList(activeQuery(ProductFormulaRestriction.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "restriction_id")));
        vo.setUsageRules(usageRuleService.queryByFormula(formulaId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveSetup(Long formulaId, ProductFormulaSetupBo bo) {
        ProductFormula current = requireEditableFormula(formulaId);
        ProductFormulaSetupBo safeBo = bo == null ? new ProductFormulaSetupBo() : bo;
        List<ProductFormulaMaterial> materials = normalizeMaterials(formulaId, safeBo.getMaterials());
        List<ProductFormulaOption> options = normalizeOptions(formulaId, safeBo.getOptions());
        List<ProductFormulaOptionValue> values = normalizeOptionValues(formulaId, options, safeBo.getOptionValues());
        normalizeOptionVisibility(options, values);
        List<ProductFormulaOptionMaterial> optionMaterials = normalizeOptionMaterials(formulaId, materials, options, values, safeBo.getOptionMaterials());
        List<ProductFormulaRestriction> restrictions = normalizeRestrictions(formulaId, options, values, safeBo.getRestrictions());
        List<com.bocoo.product.domain.entity.ProductFormulaUsageRule> usageRules = usageRuleService.normalize(
            formulaId, materials, options, values, safeBo.getUsageRules());

        replaceFormulaRows(formulaId, materials, options, values, optionMaterials, restrictions, usageRules);
        refreshFormulaSetup(current, materials.size(), "SAVE_SETUP", safeBo);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = ServiceException.class)
    public Boolean validateSetup(Long formulaId) {
        ProductFormula current = requireEditableFormula(formulaId);
        String messageKey = validationMessageKey(formulaId);
        String status = messageKey == null ? "PASS" : "FAIL";
        ProductFormula after = copyStatusSnapshot(current);
        after.setLatestValidationStatus(status);
        after.setLatestValidationMessage(messageKey);
        after.setLatestValidationTime(TimeUtils.utcNow());
        formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, formulaId)
            .set(ProductFormula::getLatestValidationStatus, status)
            .set(ProductFormula::getLatestValidationMessage, messageKey)
            .set(ProductFormula::getLatestValidationTime, after.getLatestValidationTime()));
        recordFormulaChange(formulaId, current.getFormulaCode(), "PASS".equals(status) ? "VALIDATE_PASS" : "VALIDATE_FAIL", current, after);
        if (!"PASS".equals(status)) {
            throw ServiceException.ofMessageKey(messageKey);
        }
        return Boolean.TRUE;
    }

    @Override
    public int materialCount(Long formulaId) {
        return activeMaterials(formulaId).size();
    }

    @Override
    public String validationMessageKey(Long formulaId) {
        List<ProductFormulaMaterial> materials = activeMaterials(formulaId);
        List<com.bocoo.product.domain.entity.ProductFormulaUsageRule> usageRules = usageRuleService.activeRules(formulaId);
        Map<String, List<com.bocoo.product.domain.entity.ProductFormulaUsageRule>> usageRulesByMaterial = usageRules.stream()
            .filter(rule -> STATUS_ENABLED.equals(rule.getStatus()))
            .collect(Collectors.groupingBy(com.bocoo.product.domain.entity.ProductFormulaUsageRule::getMaterialCode));
        if (materials.isEmpty()) {
            return "product.formula.notConfigured";
        }
        for (ProductFormulaMaterial material : materials) {
            if (material.getMaterialId() == null && StringUtils.isBlank(material.getMaterialCode())) {
                return "product.formula.materialRequired";
            }
            if (StringUtils.isBlank(material.getUnitCode())) {
                return "product.formula.materialUnitRequired";
            }
            if (usageRulesByMaterial.containsKey(material.getMaterialCode())) {
                continue;
            }
            if (StringUtils.isBlank(material.getUsageMode())) {
                return "product.formula.materialUsageModeRequired";
            }
            if (requiresFormula(material.getUsageMode()) && StringUtils.isBlank(material.getUsageFormula())) {
                return "product.formula.materialUsageRuleRequired";
            }
            if ("FIXED".equals(material.getUsageMode()) && material.getFixedUsageQty() == null) {
                return "product.formula.materialUsageRuleRequired";
            }
        }
        List<ProductFormulaOption> options = activeOptions(formulaId);
        if (options.isEmpty()) {
            return "product.formula.optionRequired";
        }
        Map<String, ProductFormulaOption> optionMap = options.stream()
            .filter(option -> StringUtils.isNotBlank(option.getOptionCode()))
            .collect(Collectors.toMap(ProductFormulaOption::getOptionCode, Function.identity(), (left, right) -> left));
        Set<String> optionCodes = optionMap.keySet();
        List<ProductFormulaOptionValue> values = activeValues(formulaId);
        Map<String, Long> optionValueCounts = values.stream()
            .filter(value -> StringUtils.isNotBlank(value.getOptionCode()))
            .collect(Collectors.groupingBy(ProductFormulaOptionValue::getOptionCode, Collectors.counting()));
        Set<String> valueKeys = values.stream().map(value -> key(value.getOptionCode(), value.getValueCode())).collect(Collectors.toSet());
        for (ProductFormulaOption option : options) {
            if (StringUtils.isBlank(option.getOptionCode()) || StringUtils.isBlank(option.getOptionNameCn())) {
                return "product.formula.optionRequired";
            }
            if (optionValueCounts.getOrDefault(option.getOptionCode(), 0L) == 0L) {
                return "product.formula.optionValueRequired";
            }
            String visibilityMessageKey = validateOptionVisibility(option, optionMap, valueKeys);
            if (visibilityMessageKey != null) {
                return visibilityMessageKey;
            }
        }
        Set<String> materialCodes = materials.stream().map(ProductFormulaMaterial::getMaterialCode).collect(Collectors.toSet());
        Set<String> optionMaterialKeys = new HashSet<>();
        for (ProductFormulaOptionMaterial optionMaterial : activeOptionMaterials(formulaId)) {
            if (!optionCodes.contains(optionMaterial.getOptionCode())
                || !valueKeys.contains(key(optionMaterial.getOptionCode(), optionMaterial.getValueCode()))) {
                return "product.formula.optionMaterialValueInvalid";
            }
            if (!materialCodes.contains(optionMaterial.getMaterialCode())) {
                return "product.formula.optionMaterialNotInPool";
            }
            if (!optionMaterialKeys.add(key(optionMaterial.getOptionCode(), optionMaterial.getValueCode(), optionMaterial.getMaterialCode()))) {
                return "product.formula.optionMaterialDuplicate";
            }
        }
        for (ProductFormulaRestriction restriction : activeRestrictions(formulaId)) {
            String restrictionMessageKey = validateRestriction(restriction, optionCodes, valueKeys);
            if (restrictionMessageKey != null) {
                return restrictionMessageKey;
            }
            if (StringUtils.isNotBlank(restriction.getTargetOptionCode()) && !optionCodes.contains(restriction.getTargetOptionCode())) {
                return "product.formula.restrictionTargetInvalid";
            }
        }
        String usageRuleMessageKey = usageRuleService.validationMessageKey(materials, options, values, usageRules);
        if (usageRuleMessageKey != null) {
            return usageRuleMessageKey;
        }
        return null;
    }

    @Override
    public Map<String, Object> snapshot(Long formulaId) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("materials", activeMaterials(formulaId));
        snapshot.put("options", activeOptions(formulaId));
        snapshot.put("optionValues", activeValues(formulaId));
        snapshot.put("optionMaterials", activeOptionMaterials(formulaId));
        snapshot.put("restrictions", activeRestrictions(formulaId));
        snapshot.put("usageRules", usageRuleService.activeRules(formulaId));
        return snapshot;
    }

    private List<ProductFormulaMaterial> normalizeMaterials(Long formulaId, List<ProductFormulaMaterialBo> rows) {
        List<ProductFormulaMaterial> result = new ArrayList<>();
        Set<Long> materialIds = new HashSet<>();
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
            normalizeMaterialSnapshot(entity);
            if (entity.getMaterialId() == null) {
                throw ServiceException.ofMessageKey("product.formula.materialRequired");
            }
            if (!materialIds.add(entity.getMaterialId())) {
                throw ServiceException.ofMessageKey("product.formula.materialDuplicate");
            }
            validateUnit(entity.getUnitCode(), "product.formula.materialUnitRequired", "product.formula.materialUnitNotFound");
            result.add(entity);
            index++;
        }
        return result;
    }

    private void normalizeMaterialSnapshot(ProductFormulaMaterial entity) {
        ProductMaterial material = null;
        if (entity.getMaterialId() != null) {
            material = productMaterialMapper.selectById(entity.getMaterialId());
        }
        if (material == null && StringUtils.isNotBlank(entity.getMaterialCode())) {
            material = productMaterialMapper.selectOne(activeQuery(ProductMaterial.class).eq("material_code", entity.getMaterialCode()));
        }
        if (material == null || !"0".equals(material.getDelFlag()) || !STATUS_ENABLED.equals(material.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.materialNotFound");
        }
        entity.setMaterialId(material.getMaterialId());
        entity.setMaterialCode(material.getMaterialCode());
        entity.setMaterialNameCn(material.getMaterialNameCn());
        entity.setSpecModelText(material.getSpecModelText());
        entity.setAttributeGroupId(material.getAttributeGroupId());
        entity.setAttributeGroupCode(material.getAttributeGroupCode());
        entity.setAttributeGroupNameCn(material.getAttributeGroupNameCn());
        entity.setMaterialTypeId(material.getMaterialTypeId());
        entity.setMaterialTypeCode(material.getMaterialTypeCode());
        entity.setMaterialTypeNameCn(material.getMaterialTypeNameCn());
        if (StringUtils.isBlank(entity.getUnitCode())) {
            entity.setUnitCode(material.getUnitCode());
        }
        if (StringUtils.isBlank(entity.getCalculationUnitCode())) {
            entity.setCalculationUnitCode(entity.getUnitCode());
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
    }

    private void clearOptionVisibilityCondition(ProductFormulaOption option) {
        option.setVisibleConditionOptionCode(null);
        option.setVisibleConditionOptionNameCn(null);
        option.setVisibleConditionValueCode(null);
        option.setVisibleConditionValueNameCn(null);
    }

    private List<ProductFormulaOptionValue> normalizeOptionValues(Long formulaId, List<ProductFormulaOption> options, List<ProductFormulaOptionValueBo> rows) {
        Map<String, ProductFormulaOption> optionMap = options.stream().collect(Collectors.toMap(ProductFormulaOption::getOptionCode, Function.identity()));
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
            if (!optionCodes.contains(entity.getOptionCode()) || !valueKeys.contains(key(entity.getOptionCode(), entity.getValueCode()))) {
                throw ServiceException.ofMessageKey("product.formula.optionMaterialValueInvalid");
            }
            ProductFormulaMaterial material = entity.getFormulaMaterialId() == null ? null : materialById.get(entity.getFormulaMaterialId());
            if (material == null && StringUtils.isNotBlank(entity.getMaterialCode())) {
                material = materialByCode.get(entity.getMaterialCode());
            }
            if (material == null) {
                throw ServiceException.ofMessageKey("product.formula.optionMaterialNotInPool");
            }
            entity.setFormulaMaterialId(material.getFormulaMaterialId());
            entity.setMaterialId(material.getMaterialId());
            entity.setMaterialCode(material.getMaterialCode());
            entity.setMaterialNameCn(material.getMaterialNameCn());
            String duplicateKey = key(entity.getOptionCode(), entity.getValueCode(), entity.getMaterialCode());
            if (!duplicateKeys.add(duplicateKey)) {
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

    private List<ProductFormulaRestriction> normalizeRestrictions(Long formulaId, List<ProductFormulaOption> options,
                                                                  List<ProductFormulaOptionValue> values,
                                                                  List<ProductFormulaRestrictionBo> rows) {
        Set<String> optionCodes = options.stream().map(ProductFormulaOption::getOptionCode).collect(Collectors.toSet());
        Set<String> valueKeys = values.stream().map(value -> key(value.getOptionCode(), value.getValueCode())).collect(Collectors.toSet());
        List<ProductFormulaRestriction> result = new ArrayList<>();
        int index = 0;
        for (ProductFormulaRestrictionBo row : rows == null ? List.<ProductFormulaRestrictionBo>of() : rows) {
            ProductFormulaRestriction entity = MapstructUtils.convert(row, ProductFormulaRestriction.class);
            if (entity == null) {
                continue;
            }
            entity.setFormulaId(formulaId);
            entity.setRestrictionName(defaultString(trim(entity.getRestrictionName()), "限制条件" + (index + 1)));
            entity.setTargetOptionCode(requiredUpper(entity.getTargetOptionCode(), "product.formula.restrictionTargetRequired"));
            if (!optionCodes.contains(entity.getTargetOptionCode())) {
                throw ServiceException.ofMessageKey("product.formula.restrictionTargetInvalid");
            }
            entity.setConditionType(requiredUpper(entity.getConditionType(), "product.formula.restrictionConditionRequired"));
            entity.setConditionOptionCode(trimUpper(entity.getConditionOptionCode()));
            if (StringUtils.isNotBlank(entity.getConditionOptionCode()) && !optionCodes.contains(entity.getConditionOptionCode())) {
                throw ServiceException.ofMessageKey("product.formula.restrictionConditionInvalid");
            }
            entity.setConditionOperator(requiredUpper(entity.getConditionOperator(), "product.formula.restrictionConditionRequired"));
            entity.setConditionValueCode(trimUpper(entity.getConditionValueCode()));
            entity.setActionType(requiredUpper(entity.getActionType(), "product.formula.restrictionActionRequired"));
            entity.setTargetValueCode(trimUpper(entity.getTargetValueCode()));
            String restrictionMessageKey = validateRestriction(entity, optionCodes, valueKeys);
            if (restrictionMessageKey != null) {
                throw ServiceException.ofMessageKey(restrictionMessageKey);
            }
            entity.setMessageText(trim(entity.getMessageText()));
            entity.setStatus(defaultString(trim(entity.getStatus()), STATUS_ENABLED));
            entity.setDelFlag("0");
            entity.setSortOrder(entity.getSortOrder() == null ? index * 10 + 10 : entity.getSortOrder());
            result.add(entity);
            index++;
        }
        return result;
    }

    private String validateOptionVisibility(ProductFormulaOption option, Map<String, ProductFormulaOption> optionMap, Set<String> valueKeys) {
        if (!VISIBILITY_CONDITIONAL.equals(option.getVisibilityMode())) {
            return null;
        }
        if (StringUtils.isBlank(option.getVisibleConditionOptionCode())) {
            return "product.formula.optionVisibilityConditionRequired";
        }
        if (option.getVisibleConditionOptionCode().equals(option.getOptionCode())) {
            return "product.formula.optionVisibilitySelfDenied";
        }
        if (!optionMap.containsKey(option.getVisibleConditionOptionCode())) {
            return "product.formula.optionVisibilityConditionInvalid";
        }
        if (StringUtils.isBlank(option.getVisibleConditionValueCode())) {
            return "product.formula.optionVisibilityValueRequired";
        }
        if (!valueKeys.contains(key(option.getVisibleConditionOptionCode(), option.getVisibleConditionValueCode()))) {
            return "product.formula.optionVisibilityValueInvalid";
        }
        return null;
    }

    private String validateRestriction(ProductFormulaRestriction restriction, Set<String> optionCodes, Set<String> valueKeys) {
        if (StringUtils.isBlank(restriction.getTargetOptionCode()) || !optionCodes.contains(restriction.getTargetOptionCode())) {
            return "product.formula.restrictionTargetInvalid";
        }
        if (StringUtils.isNotBlank(restriction.getTargetValueCode())
            && !valueKeys.contains(key(restriction.getTargetOptionCode(), restriction.getTargetValueCode()))) {
            return "product.formula.restrictionTargetInvalid";
        }
        if (StringUtils.isBlank(restriction.getConditionType()) || StringUtils.isBlank(restriction.getConditionOperator())
            || StringUtils.isBlank(restriction.getActionType())) {
            return "product.formula.restrictionConditionInvalid";
        }
        if ("OPTION_VALUE".equals(restriction.getConditionType())) {
            if (StringUtils.isBlank(restriction.getConditionOptionCode()) || !optionCodes.contains(restriction.getConditionOptionCode())) {
                return "product.formula.restrictionConditionInvalid";
            }
            if (StringUtils.isBlank(restriction.getConditionValueCode())
                || !valueKeys.contains(key(restriction.getConditionOptionCode(), restriction.getConditionValueCode()))) {
                return "product.formula.restrictionConditionInvalid";
            }
            return null;
        }
        if (("WIDTH".equals(restriction.getConditionType()) || "HEIGHT".equals(restriction.getConditionType())
            || "WEIGHT".equals(restriction.getConditionType())) && restriction.getConditionValueNumber() == null) {
            return "product.formula.restrictionConditionInvalid";
        }
        if (StringUtils.isNotBlank(restriction.getConditionOptionCode()) && !optionCodes.contains(restriction.getConditionOptionCode())) {
            return "product.formula.restrictionConditionInvalid";
        }
        if (StringUtils.isNotBlank(restriction.getConditionValueCode())
            && (StringUtils.isBlank(restriction.getConditionOptionCode())
            || !valueKeys.contains(key(restriction.getConditionOptionCode(), restriction.getConditionValueCode())))) {
            return "product.formula.restrictionConditionInvalid";
        }
        return null;
    }

    private void replaceFormulaRows(Long formulaId, List<ProductFormulaMaterial> materials, List<ProductFormulaOption> options,
                                    List<ProductFormulaOptionValue> values, List<ProductFormulaOptionMaterial> optionMaterials,
                                    List<ProductFormulaRestriction> restrictions,
                                    List<com.bocoo.product.domain.entity.ProductFormulaUsageRule> usageRules) {
        deleteByFormula(formulaId);
        insertAll(materials, materialMapper);
        Map<String, Long> optionIds = insertOptions(options);
        Map<String, Long> valueIds = insertValues(values, optionIds);
        assignGeneratedIds(optionMaterials, materials, optionIds, valueIds);
        insertAll(optionMaterials, optionMaterialMapper);
        insertAll(restrictions, restrictionMapper);
        usageRuleService.insertAll(usageRules, materials);
    }

    private void deleteByFormula(Long formulaId) {
        materialMapper.delete(activeQuery(ProductFormulaMaterial.class).eq("formula_id", formulaId));
        optionMapper.delete(activeQuery(ProductFormulaOption.class).eq("formula_id", formulaId));
        optionValueMapper.delete(activeQuery(ProductFormulaOptionValue.class).eq("formula_id", formulaId));
        optionMaterialMapper.delete(activeQuery(ProductFormulaOptionMaterial.class).eq("formula_id", formulaId));
        restrictionMapper.delete(activeQuery(ProductFormulaRestriction.class).eq("formula_id", formulaId));
        usageRuleService.deleteByFormula(formulaId);
    }

    private <T> void insertAll(List<T> rows, com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper) {
        for (T row : rows) {
            if (row instanceof com.bocoo.common.mybatis.core.domain.BaseEntity entity) {
                ProductEntityDefaults.prepareInsert(entity);
            }
            mapper.insert(row);
        }
    }

    private Map<String, Long> insertOptions(List<ProductFormulaOption> options) {
        Map<String, Long> ids = new LinkedHashMap<>();
        for (ProductFormulaOption option : options) {
            ProductEntityDefaults.prepareInsert(option);
            optionMapper.insert(option);
            ids.put(option.getOptionCode(), option.getOptionId());
        }
        return ids;
    }

    private Map<String, Long> insertValues(List<ProductFormulaOptionValue> values, Map<String, Long> optionIds) {
        Map<String, Long> ids = new LinkedHashMap<>();
        for (ProductFormulaOptionValue value : values) {
            value.setOptionId(optionIds.get(value.getOptionCode()));
            ProductEntityDefaults.prepareInsert(value);
            optionValueMapper.insert(value);
            ids.put(key(value.getOptionCode(), value.getValueCode()), value.getOptionValueId());
        }
        return ids;
    }

    private void assignGeneratedIds(List<ProductFormulaOptionMaterial> optionMaterials, List<ProductFormulaMaterial> materials,
                                    Map<String, Long> optionIds, Map<String, Long> valueIds) {
        Map<String, Long> materialIds = materials.stream()
            .collect(Collectors.toMap(ProductFormulaMaterial::getMaterialCode, ProductFormulaMaterial::getFormulaMaterialId, (left, right) -> left));
        for (ProductFormulaOptionMaterial optionMaterial : optionMaterials) {
            optionMaterial.setOptionId(optionIds.get(optionMaterial.getOptionCode()));
            optionMaterial.setOptionValueId(valueIds.get(key(optionMaterial.getOptionCode(), optionMaterial.getValueCode())));
            optionMaterial.setFormulaMaterialId(materialIds.get(optionMaterial.getMaterialCode()));
        }
    }

    private List<ProductFormulaMaterial> activeMaterials(Long formulaId) {
        return materialMapper.selectList(activeQuery(ProductFormulaMaterial.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "line_no", "formula_material_id"));
    }

    private List<ProductFormulaOption> activeOptions(Long formulaId) {
        return optionMapper.selectList(activeQuery(ProductFormulaOption.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "option_id"));
    }

    private List<ProductFormulaOptionValue> activeValues(Long formulaId) {
        return optionValueMapper.selectList(activeQuery(ProductFormulaOptionValue.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "option_value_id"));
    }

    private List<ProductFormulaOptionMaterial> activeOptionMaterials(Long formulaId) {
        return optionMaterialMapper.selectList(activeQuery(ProductFormulaOptionMaterial.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "option_material_id"));
    }

    private List<ProductFormulaRestriction> activeRestrictions(Long formulaId) {
        return restrictionMapper.selectList(activeQuery(ProductFormulaRestriction.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "restriction_id"));
    }

    private ProductFormula requireEditableFormula(Long formulaId) {
        ProductFormula formula = formulaId == null ? null : formulaMapper.selectById(formulaId);
        if (formula == null) {
            throw ServiceException.ofMessageKey("product.base.edit.notFound");
        }
        if (!STATUS_DRAFT.equals(formula.getStatus()) && !STATUS_REJECTED.equals(formula.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.editDenied");
        }
        return formula;
    }

    private void refreshFormulaSetup(ProductFormula current, int materialCount, String actionType, Object afterPayload) {
        ProductFormula after = copyStatusSnapshot(current);
        after.setMaterialLineCount(materialCount);
        after.setConfiguredFlag(materialCount > 0);
        after.setLatestValidationStatus(VALIDATION_NOT_VALIDATED);
        after.setLatestValidationMessage(null);
        after.setLatestValidationTime(null);
        formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, current.getFormulaId())
            .set(ProductFormula::getMaterialLineCount, materialCount)
            .set(ProductFormula::getConfiguredFlag, materialCount > 0)
            .set(ProductFormula::getLatestValidationStatus, VALIDATION_NOT_VALIDATED)
            .set(ProductFormula::getLatestValidationMessage, null)
            .set(ProductFormula::getLatestValidationTime, null));
        recordFormulaChange(current.getFormulaId(), current.getFormulaCode(), actionType, current, afterPayload);
    }

    private void validateUnit(String unitCode, String requiredKey, String notFoundKey) {
        if (StringUtils.isBlank(unitCode)) {
            throw ServiceException.ofMessageKey(requiredKey);
        }
        ProductUnit unit = unitMapper.selectOne(activeQuery(ProductUnit.class)
            .eq("unit_code", unitCode)
            .eq("status", STATUS_ENABLED));
        if (unit == null) {
            throw ServiceException.ofMessageKey(notFoundKey);
        }
    }

    private boolean requiresFormula(String usageMode) {
        return "FORMULA".equals(usageMode);
    }

    private ProductFormula copyStatusSnapshot(ProductFormula source) {
        ProductFormula target = new ProductFormula();
        target.setFormulaId(source.getFormulaId());
        target.setFormulaCode(source.getFormulaCode());
        target.setFormulaName(source.getFormulaName());
        target.setStatus(source.getStatus());
        target.setMaterialLineCount(source.getMaterialLineCount());
        target.setConfiguredFlag(source.getConfiguredFlag());
        target.setLatestValidationStatus(source.getLatestValidationStatus());
        target.setLatestValidationMessage(source.getLatestValidationMessage());
        target.setLatestValidationTime(source.getLatestValidationTime());
        return target;
    }

    private void recordFormulaChange(Long formulaId, String formulaCode, String actionType, Object before, Object after) {
        changeLogService.record("FORMULA", "FORMULA", formulaId, formulaCode, actionType, before, after, null);
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
