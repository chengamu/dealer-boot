package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.product.domain.bo.ProductFormulaSimulationBo;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.vo.ProductFormulaMaterialVo;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.domain.vo.ProductFormulaSimulationItemVo;
import com.bocoo.product.domain.vo.ProductFormulaSimulationVo;
import com.bocoo.product.domain.vo.ProductFormulaUsageRuleVo;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.service.ProductFormulaSetupService;
import com.bocoo.product.service.ProductFormulaSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductFormulaSimulationServiceImpl extends ProductServiceSupport implements ProductFormulaSimulationService {

    private static final String STATUS_DRAFT = ProductFormulaServiceImpl.STATUS_DRAFT;
    private static final String STATUS_REJECTED = ProductFormulaServiceImpl.STATUS_REJECTED;
    private static final String VALIDATION_NOT_VALIDATED = "NOT_VALIDATED";
    private static final String VALIDATION_PASS = "PASS";
    private static final String VALIDATION_FAIL = "FAIL";

    private final ProductFormulaMapper formulaMapper;
    private final ProductMaterialMapper productMaterialMapper;
    private final ProductFormulaSetupService setupService;

    @Override
    public ProductFormulaSimulationVo query(Long formulaId) {
        ProductFormula formula = requireFormula(formulaId);
        ProductFormulaSimulationVo vo = new ProductFormulaSimulationVo();
        vo.setFormulaId(formulaId);
        vo.setStatus(formula.getSimulationValidationStatus());
        vo.setMessage(formula.getSimulationValidationMessage());
        vo.setSimulationTime(formula.getSimulationValidationTime());
        return vo;
    }

    @Override
    public ProductFormulaSimulationVo run(Long formulaId, ProductFormulaSimulationBo bo) {
        requireFormula(formulaId);
        ProductFormulaSetupVo setup = setupService.querySetup(formulaId);
        ProductFormulaSimulationVo vo = new ProductFormulaSimulationVo();
        vo.setFormulaId(formulaId);
        vo.setOrderWidth(bo == null ? null : bo.getOrderWidth());
        vo.setOrderHeight(bo == null ? null : bo.getOrderHeight());
        vo.setSelectedOptionValues(bo == null ? null : bo.getSelectedOptionValues());
        vo.setSimulationTime(TimeUtils.utcNow());

        String messageKey = setupService.validationMessageKey(formulaId);
        if (messageKey != null) {
            vo.setStatus(VALIDATION_FAIL);
            vo.setMessage(messageKey);
            return vo;
        }

        Map<Long, ProductMaterial> materialMap = new HashMap<>();
        setup.getMaterials().stream()
            .map(ProductFormulaMaterialVo::getMaterialId)
            .filter(Objects::nonNull)
            .distinct()
            .forEach(id -> {
                ProductMaterial source = productMaterialMapper.selectById(id);
                if (source != null) {
                    materialMap.put(id, source);
                }
            });
        Map<Long, List<ProductFormulaUsageRuleVo>> usageRulesByMaterial = setup.getUsageRules().stream()
            .filter(rule -> rule.getFormulaMaterialId() != null)
            .collect(Collectors.groupingBy(ProductFormulaUsageRuleVo::getFormulaMaterialId));

        List<ProductFormulaSimulationItemVo> items = setup.getMaterials().stream()
            .filter(material -> material.getMaterialId() != null)
            .map(material -> buildItem(material, materialMap.get(material.getMaterialId()),
                usageRulesByMaterial.getOrDefault(material.getFormulaMaterialId(), List.of())))
            .toList();
        vo.setItems(items);
        vo.setTotalAmount(items.stream()
            .map(ProductFormulaSimulationItemVo::getAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP));
        vo.setStatus(VALIDATION_PASS);
        vo.setMessage("product.formula.simulationPassed");
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = ServiceException.class)
    public Boolean validate(Long formulaId, ProductFormulaSimulationBo bo) {
        ProductFormula formula = requireFormula(formulaId);
        assertEditable(formula);
        if (!VALIDATION_PASS.equals(formula.getMaterialValidationStatus())
            || !VALIDATION_PASS.equals(formula.getOptionValidationStatus())) {
            updateSimulationValidation(formulaId, VALIDATION_FAIL, "product.formula.simulationPrerequisiteRequired", TimeUtils.utcNow());
            throw ServiceException.ofMessageKey("product.formula.simulationPrerequisiteRequired");
        }
        ProductFormulaSimulationVo result = run(formulaId, bo);
        updateSimulationValidation(formulaId, result.getStatus(), result.getMessage(), result.getSimulationTime());
        if (!VALIDATION_PASS.equals(result.getStatus())) {
            throw ServiceException.ofMessageKey(result.getMessage());
        }
        return Boolean.TRUE;
    }

    private ProductFormulaSimulationItemVo buildItem(ProductFormulaMaterialVo material, ProductMaterial source,
                                                     List<ProductFormulaUsageRuleVo> usageRules) {
        ProductFormulaSimulationItemVo item = new ProductFormulaSimulationItemVo();
        item.setFormulaMaterialId(material.getFormulaMaterialId());
        item.setMaterialId(material.getMaterialId());
        item.setMaterialCode(material.getMaterialCode());
        item.setMaterialNameCn(material.getMaterialNameCn());
        item.setMaterialTypeNameCn(material.getMaterialTypeNameCn());
        item.setAttributeGroupNameCn(material.getAttributeGroupNameCn());
        item.setSpecModelText(material.getSpecModelText());
        item.setUnitCode(material.getUnitCode());
        item.setLossRate(material.getLossRate());
        item.setProductionRemark(material.getProductionRemark());
        item.setUsageQty(resolveUsageQty(material, usageRules));
        item.setUsageSummary(resolveUsageSummary(item.getUsageQty(), usageRules));
        if (source != null) {
            item.setUnitPrice(source.getUnitPrice());
            item.setSalesPrice(source.getSalesPrice());
        }
        BigDecimal price = Optional.ofNullable(item.getSalesPrice()).orElse(item.getUnitPrice());
        if (price != null && item.getUsageQty() != null) {
            item.setAmount(price.multiply(item.getUsageQty()).setScale(2, RoundingMode.HALF_UP));
        }
        return item;
    }

    private BigDecimal resolveUsageQty(ProductFormulaMaterialVo material, List<ProductFormulaUsageRuleVo> usageRules) {
        return usageRules.stream()
            .filter(rule -> Boolean.TRUE.equals(rule.getDefaultRuleFlag()))
            .sorted(Comparator.comparing(ProductFormulaUsageRuleVo::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
            .map(ProductFormulaUsageRuleVo::getFixedUsageQty)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(material.getFixedUsageQty() == null ? BigDecimal.ONE : material.getFixedUsageQty())
            .setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveUsageSummary(BigDecimal usageQty, List<ProductFormulaUsageRuleVo> usageRules) {
        if (usageRules == null || usageRules.isEmpty()) {
            return "product.formula.simulation.fixedUsage";
        }
        long formulaCount = usageRules.stream().filter(rule -> "FORMULA".equals(rule.getUsageMode())).count();
        if (formulaCount > 0) {
            return "product.formula.simulation.formulaRules";
        }
        return usageQty == null ? "product.formula.simulation.fixedUsage" : usageQty.toPlainString();
    }

    private void updateSimulationValidation(Long formulaId, String status, String message, LocalDateTime time) {
        ProductFormula current = requireFormula(formulaId);
        String latestStatus = overallStatus(current.getMaterialValidationStatus(), current.getOptionValidationStatus(), status);
        String latestMessage = overallMessage(current.getMaterialValidationMessage(), current.getOptionValidationMessage(), message);
        formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, formulaId)
            .set(ProductFormula::getSimulationValidationStatus, status)
            .set(ProductFormula::getSimulationValidationMessage, message)
            .set(ProductFormula::getSimulationValidationTime, time)
            .set(ProductFormula::getLatestValidationStatus, latestStatus)
            .set(ProductFormula::getLatestValidationMessage, latestMessage)
            .set(ProductFormula::getLatestValidationTime, time));
    }

    private String overallStatus(String materialStatus, String optionStatus, String simulationStatus) {
        if (VALIDATION_FAIL.equals(materialStatus) || VALIDATION_FAIL.equals(optionStatus) || VALIDATION_FAIL.equals(simulationStatus)) {
            return VALIDATION_FAIL;
        }
        if (VALIDATION_PASS.equals(materialStatus) && VALIDATION_PASS.equals(optionStatus) && VALIDATION_PASS.equals(simulationStatus)) {
            return VALIDATION_PASS;
        }
        return VALIDATION_NOT_VALIDATED;
    }

    private String overallMessage(String materialMessage, String optionMessage, String simulationMessage) {
        if (materialMessage != null) {
            return materialMessage;
        }
        if (optionMessage != null) {
            return optionMessage;
        }
        return simulationMessage;
    }

    private ProductFormula requireFormula(Long formulaId) {
        ProductFormula formula = formulaId == null ? null : formulaMapper.selectById(formulaId);
        if (formula == null) {
            throw ServiceException.ofMessageKey("product.base.edit.notFound");
        }
        return formula;
    }

    private void assertEditable(ProductFormula formula) {
        if (!STATUS_DRAFT.equals(formula.getStatus()) && !STATUS_REJECTED.equals(formula.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.editDenied");
        }
    }
}
