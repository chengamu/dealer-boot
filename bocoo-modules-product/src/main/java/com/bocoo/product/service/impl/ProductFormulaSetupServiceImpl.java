package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.product.domain.bo.ProductFormulaSetupBo;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.service.ProductChangeLogService;
import com.bocoo.product.service.ProductFormulaSetupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductFormulaSetupServiceImpl extends ProductServiceSupport implements ProductFormulaSetupService {

    private static final String STATUS_DRAFT = ProductFormulaServiceImpl.STATUS_DRAFT;
    private static final String STATUS_REJECTED = ProductFormulaServiceImpl.STATUS_REJECTED;
    private static final String VALIDATION_NOT_VALIDATED = "NOT_VALIDATED";
    private static final String VALIDATION_PASS = "PASS";
    private static final String VALIDATION_FAIL = "FAIL";
    private final ProductFormulaMapper formulaMapper;
    private final ProductChangeLogService changeLogService;
    private final ProductFormulaSetupReader setupReader;
    private final ProductFormulaSetupValidator setupValidator;
    private final ProductFormulaSetupNormalizer setupNormalizer;
    private final ProductFormulaSetupWriter setupWriter;

    @Override
    public ProductFormulaSetupVo querySetup(Long formulaId) {
        return setupReader.querySetup(formulaId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveSetup(Long formulaId, ProductFormulaSetupBo bo) {
        ProductFormula current = requireEditableFormula(formulaId);
        ProductFormulaSetupBo safeBo = bo == null ? new ProductFormulaSetupBo() : bo;
        ProductFormulaSetupRows rows = setupNormalizer.normalize(formulaId, safeBo);
        setupWriter.replace(formulaId, rows);
        refreshFormulaSetup(current, rows.materials().size(), "SAVE_SETUP", safeBo);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = ServiceException.class)
    public Boolean validateSetup(Long formulaId) {
        ProductFormula current = requireEditableFormula(formulaId);
        ProductFormulaSetupContext context = setupReader.context(formulaId);
        String materialMessageKey = setupValidator.materialValidationMessageKey(context);
        String optionMessageKey = setupValidator.optionValidationMessageKey(context);
        String messageKey = materialMessageKey == null ? optionMessageKey : materialMessageKey;
        String status = messageKey == null ? VALIDATION_PASS : VALIDATION_FAIL;
        String materialStatus = materialMessageKey == null ? VALIDATION_PASS : VALIDATION_FAIL;
        String optionStatus = optionMessageKey == null ? VALIDATION_PASS : VALIDATION_FAIL;
        var now = TimeUtils.utcNow();
        ProductFormula after = copyStatusSnapshot(current);
        after.setLatestValidationStatus(status);
        after.setLatestValidationMessage(messageKey);
        after.setLatestValidationTime(now);
        after.setMaterialValidationStatus(materialStatus);
        after.setMaterialValidationMessage(materialMessageKey);
        after.setMaterialValidationTime(now);
        after.setOptionValidationStatus(optionStatus);
        after.setOptionValidationMessage(optionMessageKey);
        after.setOptionValidationTime(now);
        formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, formulaId)
            .set(ProductFormula::getLatestValidationStatus, status)
            .set(ProductFormula::getLatestValidationMessage, messageKey)
            .set(ProductFormula::getLatestValidationTime, now)
            .set(ProductFormula::getMaterialValidationStatus, materialStatus)
            .set(ProductFormula::getMaterialValidationMessage, materialMessageKey)
            .set(ProductFormula::getMaterialValidationTime, now)
            .set(ProductFormula::getOptionValidationStatus, optionStatus)
            .set(ProductFormula::getOptionValidationMessage, optionMessageKey)
            .set(ProductFormula::getOptionValidationTime, now));
        recordFormulaChange(formulaId, current.getFormulaCode(), VALIDATION_PASS.equals(status) ? "VALIDATE_PASS" : "VALIDATE_FAIL", current, after);
        if (!VALIDATION_PASS.equals(status)) {
            throw ServiceException.ofMessageKey(messageKey);
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = ServiceException.class)
    public Boolean validateMaterials(Long formulaId) {
        ProductFormula current = requireEditableFormula(formulaId);
        String messageKey = materialValidationMessageKey(formulaId);
        updateMaterialValidation(current, messageKey);
        if (messageKey != null) {
            throw ServiceException.ofMessageKey(messageKey);
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = ServiceException.class)
    public Boolean validateOptions(Long formulaId) {
        ProductFormula current = requireEditableFormula(formulaId);
        String messageKey = optionValidationMessageKey(formulaId);
        updateOptionValidation(current, messageKey);
        if (messageKey != null) {
            throw ServiceException.ofMessageKey(messageKey);
        }
        return Boolean.TRUE;
    }

    @Override
    public int materialCount(Long formulaId) {
        return setupReader.materialCount(formulaId);
    }

    @Override
    public String validationMessageKey(Long formulaId) {
        return setupValidator.validationMessageKey(setupReader.context(formulaId));
    }

    @Override
    public String materialValidationMessageKey(Long formulaId) {
        return setupValidator.materialValidationMessageKey(setupReader.context(formulaId));
    }

    @Override
    public String optionValidationMessageKey(Long formulaId) {
        return setupValidator.optionValidationMessageKey(setupReader.context(formulaId));
    }

    @Override
    public Map<String, Object> snapshot(Long formulaId) {
        return setupReader.snapshot(formulaId);
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
        after.setMaterialValidationStatus(VALIDATION_NOT_VALIDATED);
        after.setMaterialValidationMessage(null);
        after.setMaterialValidationTime(null);
        after.setOptionValidationStatus(VALIDATION_NOT_VALIDATED);
        after.setOptionValidationMessage(null);
        after.setOptionValidationTime(null);
        after.setSimulationValidationStatus(VALIDATION_NOT_VALIDATED);
        after.setSimulationValidationMessage(null);
        after.setSimulationValidationTime(null);
        formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, current.getFormulaId())
            .set(ProductFormula::getMaterialLineCount, materialCount)
            .set(ProductFormula::getConfiguredFlag, materialCount > 0)
            .set(ProductFormula::getLatestValidationStatus, VALIDATION_NOT_VALIDATED)
            .set(ProductFormula::getLatestValidationMessage, null)
            .set(ProductFormula::getLatestValidationTime, null)
            .set(ProductFormula::getMaterialValidationStatus, VALIDATION_NOT_VALIDATED)
            .set(ProductFormula::getMaterialValidationMessage, null)
            .set(ProductFormula::getMaterialValidationTime, null)
            .set(ProductFormula::getOptionValidationStatus, VALIDATION_NOT_VALIDATED)
            .set(ProductFormula::getOptionValidationMessage, null)
            .set(ProductFormula::getOptionValidationTime, null)
            .set(ProductFormula::getSimulationValidationStatus, VALIDATION_NOT_VALIDATED)
            .set(ProductFormula::getSimulationValidationMessage, null)
            .set(ProductFormula::getSimulationValidationTime, null));
        recordFormulaChange(current.getFormulaId(), current.getFormulaCode(), actionType, current, afterPayload);
    }

    private void updateMaterialValidation(ProductFormula current, String messageKey) {
        String status = messageKey == null ? VALIDATION_PASS : VALIDATION_FAIL;
        var now = TimeUtils.utcNow();
        ProductFormula after = copyStatusSnapshot(current);
        after.setMaterialValidationStatus(status);
        after.setMaterialValidationMessage(messageKey);
        after.setMaterialValidationTime(now);
        after.setLatestValidationStatus(overallStatus(status, current.getOptionValidationStatus(), current.getSimulationValidationStatus()));
        after.setLatestValidationMessage(overallMessage(messageKey, current.getOptionValidationMessage(), current.getSimulationValidationMessage()));
        after.setLatestValidationTime(now);
        formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, current.getFormulaId())
            .set(ProductFormula::getMaterialValidationStatus, status)
            .set(ProductFormula::getMaterialValidationMessage, messageKey)
            .set(ProductFormula::getMaterialValidationTime, now)
            .set(ProductFormula::getLatestValidationStatus, after.getLatestValidationStatus())
            .set(ProductFormula::getLatestValidationMessage, after.getLatestValidationMessage())
            .set(ProductFormula::getLatestValidationTime, now));
        recordFormulaChange(current.getFormulaId(), current.getFormulaCode(),
            VALIDATION_PASS.equals(status) ? "VALIDATE_MATERIAL_PASS" : "VALIDATE_MATERIAL_FAIL", current, after);
    }

    private void updateOptionValidation(ProductFormula current, String messageKey) {
        String status = messageKey == null ? VALIDATION_PASS : VALIDATION_FAIL;
        var now = TimeUtils.utcNow();
        ProductFormula after = copyStatusSnapshot(current);
        after.setOptionValidationStatus(status);
        after.setOptionValidationMessage(messageKey);
        after.setOptionValidationTime(now);
        after.setLatestValidationStatus(overallStatus(current.getMaterialValidationStatus(), status, current.getSimulationValidationStatus()));
        after.setLatestValidationMessage(overallMessage(current.getMaterialValidationMessage(), messageKey, current.getSimulationValidationMessage()));
        after.setLatestValidationTime(now);
        formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, current.getFormulaId())
            .set(ProductFormula::getOptionValidationStatus, status)
            .set(ProductFormula::getOptionValidationMessage, messageKey)
            .set(ProductFormula::getOptionValidationTime, now)
            .set(ProductFormula::getLatestValidationStatus, after.getLatestValidationStatus())
            .set(ProductFormula::getLatestValidationMessage, after.getLatestValidationMessage())
            .set(ProductFormula::getLatestValidationTime, now));
        recordFormulaChange(current.getFormulaId(), current.getFormulaCode(),
            VALIDATION_PASS.equals(status) ? "VALIDATE_OPTION_PASS" : "VALIDATE_OPTION_FAIL", current, after);
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
        if (StringUtils.isNotBlank(materialMessage)) {
            return materialMessage;
        }
        if (StringUtils.isNotBlank(optionMessage)) {
            return optionMessage;
        }
        return simulationMessage;
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
        target.setMaterialValidationStatus(source.getMaterialValidationStatus());
        target.setMaterialValidationMessage(source.getMaterialValidationMessage());
        target.setMaterialValidationTime(source.getMaterialValidationTime());
        target.setOptionValidationStatus(source.getOptionValidationStatus());
        target.setOptionValidationMessage(source.getOptionValidationMessage());
        target.setOptionValidationTime(source.getOptionValidationTime());
        target.setSimulationValidationStatus(source.getSimulationValidationStatus());
        target.setSimulationValidationMessage(source.getSimulationValidationMessage());
        target.setSimulationValidationTime(source.getSimulationValidationTime());
        return target;
    }

    private void recordFormulaChange(Long formulaId, String formulaCode, String actionType, Object before, Object after) {
        changeLogService.record("FORMULA", "FORMULA", formulaId, formulaCode, actionType, before, after, null);
    }
}
