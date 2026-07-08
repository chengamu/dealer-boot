package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.product.domain.bo.ProductFormulaSimulationBo;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.domain.vo.ProductFormulaSimulationVo;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.service.ProductFormulaSetupService;
import com.bocoo.product.service.ProductFormulaSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductFormulaSimulationServiceImpl extends ProductServiceSupport implements ProductFormulaSimulationService {

    private static final String STATUS_DRAFT = ProductFormulaServiceImpl.STATUS_DRAFT;
    private static final String STATUS_REJECTED = ProductFormulaServiceImpl.STATUS_REJECTED;
    private static final String STATUS_EFFECTIVE = ProductFormulaServiceImpl.STATUS_EFFECTIVE;
    private static final String VALIDATION_NOT_VALIDATED = "NOT_VALIDATED";
    private static final String VALIDATION_PASS = "PASS";
    private static final String VALIDATION_FAIL = "FAIL";

    private final ProductFormulaMapper formulaMapper;
    private final ProductFormulaSetupService setupService;
    private final ProductFormulaSimulationEngine simulationEngine;

    @Override
    public ProductFormulaSimulationVo query(Long formulaId) {
        ProductFormula formula = requireFormula(formulaId);
        assertRunnable(formula);
        ProductFormulaSimulationVo vo = new ProductFormulaSimulationVo();
        vo.setFormulaId(formulaId);
        vo.setStatus(formula.getSimulationValidationStatus());
        vo.setMessage(formula.getSimulationValidationMessage());
        vo.setSimulationTime(formula.getSimulationValidationTime());
        return vo;
    }

    @Override
    public ProductFormulaSimulationVo run(Long formulaId, ProductFormulaSimulationBo bo) {
        ProductFormula formula = requireFormula(formulaId);
        assertRunnable(formula);
        ProductFormulaSetupVo setup = setupService.querySetup(formulaId);
        return simulationEngine.run(formulaId, formula, setup, bo, setupService.validationMessageKey(formulaId));
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

    private void assertRunnable(ProductFormula formula) {
        if (!STATUS_DRAFT.equals(formula.getStatus()) && !STATUS_REJECTED.equals(formula.getStatus()) && !STATUS_EFFECTIVE.equals(formula.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.statusInvalid");
        }
    }
}
