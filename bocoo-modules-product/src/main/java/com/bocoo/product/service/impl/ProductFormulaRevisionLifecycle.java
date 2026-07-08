package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.product.domain.bo.ProductFormulaBo;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.vo.ProductFormulaVersionVo;
import com.bocoo.product.domain.vo.ProductFormulaVo;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import com.bocoo.product.service.ProductChangeLogService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductFormulaRevisionLifecycle extends ProductServiceSupport {

    private static final String STATUS_DRAFT = ProductFormulaServiceImpl.STATUS_DRAFT;
    private static final String STATUS_PENDING_REVIEW = ProductFormulaServiceImpl.STATUS_PENDING_REVIEW;
    private static final String STATUS_REJECTED = ProductFormulaServiceImpl.STATUS_REJECTED;
    private static final String STATUS_EFFECTIVE = ProductFormulaServiceImpl.STATUS_EFFECTIVE;
    private static final String VALIDATION_NOT_VALIDATED = ProductFormulaServiceImpl.VALIDATION_NOT_VALIDATED;

    private final ProductFormulaMapper formulaMapper;
    private final ProductFormulaVersionMapper versionMapper;
    private final ProductFormulaVersionReferenceGuard referenceGuard;
    private final ProductFormulaDraftNormalizer draftNormalizer;
    private final ProductFormulaRevisionSetupCopier setupCopier;
    private final ProductFormulaSnapshotJson snapshotJson;
    private final ProductChangeLogService changeLogService;

    @Transactional(rollbackFor = Exception.class)
    public Boolean startRevision(Long id) {
        ProductFormula current = requireFormula(id);
        if (!STATUS_EFFECTIVE.equals(current.getStatus()) || current.getCurrentVersionId() == null) {
            throw ServiceException.ofMessageKey("product.formula.startRevisionDenied");
        }
        ProductFormulaVersion version = requireVersion(id, current.getCurrentVersionId());
        setupCopier.restoreFromSnapshot(id, version.getSetupSnapshotJson());
        ProductFormula after = snapshotJson.copyStatusSnapshot(current);
        after.setStatus(STATUS_DRAFT);
        after.setDraftVersionNo(nextFormalVersionNo(id));
        resetValidation(after);
        boolean updated = formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, id)
            .eq(ProductFormula::getStatus, STATUS_EFFECTIVE)
            .set(ProductFormula::getStatus, STATUS_DRAFT)
            .set(ProductFormula::getDraftVersionNo, after.getDraftVersionNo())
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
            .set(ProductFormula::getSimulationValidationTime, null)
            .set(ProductFormula::getRejectReason, null)) > 0;
        if (updated) {
            recordFormulaChange(id, current.getFormulaCode(), "START_REVISION", current, after);
        }
        return updated;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean withdraw(Long id) {
        ProductFormula current = requireFormula(id);
        if (STATUS_PENDING_REVIEW.equals(current.getStatus())) {
            return withdrawPendingReview(current);
        }
        if (STATUS_EFFECTIVE.equals(current.getStatus())) {
            return withdrawEffective(current);
        }
        throw ServiceException.ofMessageKey("product.formula.withdrawDenied");
    }

    @Transactional(rollbackFor = Exception.class)
    public ProductFormulaVo copyFormula(Long id, ProductFormulaBo bo) {
        ProductFormula source = requireFormula(id);
        ProductFormulaBo target = copyBase(source, bo);
        draftNormalizer.normalize(target, true);
        draftNormalizer.validateUnique(target);
        ProductFormula entity = MapstructUtils.convert(target, ProductFormula.class);
        if (entity == null) {
            return null;
        }
        ProductEntityDefaults.prepareInsert(entity);
        formulaMapper.insert(entity);
        String sourceSnapshot = source.getCurrentVersionId() == null ? null
            : requireVersion(source.getFormulaId(), source.getCurrentVersionId()).getSetupSnapshotJson();
        ProductFormulaSetupRows rows = setupCopier.copyCurrentSetup(entity.getFormulaId(), source.getFormulaId(), sourceSnapshot);
        formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, entity.getFormulaId())
            .set(ProductFormula::getMaterialLineCount, rows.materials().size())
            .set(ProductFormula::getConfiguredFlag, !rows.materials().isEmpty()));
        recordFormulaChange(entity.getFormulaId(), entity.getFormulaCode(), "COPY", null, entity);
        return formulaMapper.selectVoById(entity.getFormulaId());
    }

    public List<ProductFormulaVersionVo> queryVersionHistory(Long formulaId) {
        return versionMapper.selectVoList(activeQuery(ProductFormulaVersion.class)
            .eq("formula_id", formulaId)
            .orderByDesc("version_no", "version_id"));
    }

    private Boolean withdrawPendingReview(ProductFormula current) {
        ProductFormulaVersion review = pendingReview(current.getFormulaId());
        markVersionRejected(review, "WITHDRAWN");
        ProductFormula after = snapshotJson.copyStatusSnapshot(current);
        after.setStatus(STATUS_DRAFT);
        after.setRejectReason(null);
        boolean updated = formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, current.getFormulaId())
            .eq(ProductFormula::getStatus, STATUS_PENDING_REVIEW)
            .set(ProductFormula::getStatus, STATUS_DRAFT)
            .set(ProductFormula::getRejectReason, null)) > 0;
        if (updated) {
            recordFormulaChange(current.getFormulaId(), current.getFormulaCode(), "WITHDRAW_REVIEW", current, after);
        }
        return updated;
    }

    private Boolean withdrawEffective(ProductFormula current) {
        if (current.getCurrentVersionId() == null) {
            throw ServiceException.ofMessageKey("product.formula.currentVersionRequired");
        }
        referenceGuard.assertNoBusinessReference(current.getCurrentVersionId());
        ProductFormulaVersion version = requireVersion(current.getFormulaId(), current.getCurrentVersionId());
        setupCopier.restoreFromSnapshot(current.getFormulaId(), version.getSetupSnapshotJson());
        markVersionRejected(version, "WITHDRAWN");
        ProductFormula after = snapshotJson.copyStatusSnapshot(current);
        after.setStatus(STATUS_DRAFT);
        after.setCurrentVersionId(null);
        after.setCurrentVersionNo(null);
        after.setCurrentVersionLabel(null);
        after.setDraftVersionNo(version.getVersionNo());
        boolean updated = formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, current.getFormulaId())
            .eq(ProductFormula::getStatus, STATUS_EFFECTIVE)
            .set(ProductFormula::getStatus, STATUS_DRAFT)
            .set(ProductFormula::getCurrentVersionId, null)
            .set(ProductFormula::getCurrentVersionNo, null)
            .set(ProductFormula::getCurrentVersionLabel, null)
            .set(ProductFormula::getDraftVersionNo, version.getVersionNo())
            .set(ProductFormula::getAuditBy, null)
            .set(ProductFormula::getAuditTime, null)
            .set(ProductFormula::getRejectReason, null)) > 0;
        if (updated) {
            recordFormulaChange(current.getFormulaId(), current.getFormulaCode(), "WITHDRAW_EFFECTIVE", current, after);
        }
        return updated;
    }

    private ProductFormulaBo copyBase(ProductFormula source, ProductFormulaBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getFormulaCode())) {
            throw ServiceException.ofMessageKey("product.formula.copyCodeRequired");
        }
        if (StringUtils.isBlank(bo.getFormulaName())) {
            throw ServiceException.ofMessageKey("product.formula.copyNameRequired");
        }
        ProductFormulaBo target = MapstructUtils.convert(source, ProductFormulaBo.class);
        target.setFormulaId(null);
        target.setFormulaCode(bo.getFormulaCode());
        target.setFormulaName(bo.getFormulaName());
        target.setStatus(STATUS_DRAFT);
        target.setCurrentVersionId(null);
        target.setCurrentVersionNo(null);
        target.setCurrentVersionLabel(null);
        target.setDraftVersionNo(1);
        target.setAuditBy(null);
        target.setAuditTime(null);
        target.setRejectReason(null);
        target.setRemark(bo.getRemark());
        resetValidation(target);
        return target;
    }

    private ProductFormula requireFormula(Long id) {
        ProductFormula current = id == null ? null : formulaMapper.selectById(id);
        if (current == null || !"0".equals(current.getDelFlag())) {
            throw ServiceException.ofMessageKey("product.base.edit.notFound");
        }
        return current;
    }

    private ProductFormulaVersion requireVersion(Long formulaId, Long versionId) {
        ProductFormulaVersion version = versionMapper.selectOne(activeQuery(ProductFormulaVersion.class)
            .eq("formula_id", formulaId)
            .eq("version_id", versionId));
        if (version == null) {
            throw ServiceException.ofMessageKey("product.formula.reviewSnapshotRequired");
        }
        return version;
    }

    private ProductFormulaVersion pendingReview(Long formulaId) {
        ProductFormulaVersion review = versionMapper.selectOne(activeQuery(ProductFormulaVersion.class)
            .eq("formula_id", formulaId)
            .eq("version_status", STATUS_PENDING_REVIEW)
            .orderByDesc("version_id")
            .last("limit 1"));
        if (review == null) {
            throw ServiceException.ofMessageKey("product.formula.reviewSnapshotRequired");
        }
        return review;
    }

    private int nextFormalVersionNo(Long formulaId) {
        ProductFormulaVersion latest = versionMapper.selectOne(activeQuery(ProductFormulaVersion.class)
            .eq("formula_id", formulaId)
            .in("version_status", List.of(STATUS_EFFECTIVE, ProductFormulaServiceImpl.STATUS_STOPPED))
            .orderByDesc("version_no")
            .last("limit 1"));
        return latest == null || latest.getVersionNo() == null ? 1 : latest.getVersionNo() + 1;
    }

    private void markVersionRejected(ProductFormulaVersion version, String reason) {
        versionMapper.update(null, new LambdaUpdateWrapper<ProductFormulaVersion>()
            .eq(ProductFormulaVersion::getVersionId, version.getVersionId())
            .set(ProductFormulaVersion::getVersionStatus, STATUS_REJECTED)
            .set(ProductFormulaVersion::getAuditBy, currentUsername())
            .set(ProductFormulaVersion::getAuditTime, TimeUtils.utcNow())
            .set(ProductFormulaVersion::getRejectReason, reason));
    }

    private void resetValidation(ProductFormulaBo target) {
        target.setLatestValidationStatus(VALIDATION_NOT_VALIDATED);
        target.setLatestValidationMessage(null);
        target.setLatestValidationTime(null);
        target.setMaterialValidationStatus(VALIDATION_NOT_VALIDATED);
        target.setMaterialValidationMessage(null);
        target.setMaterialValidationTime(null);
        target.setOptionValidationStatus(VALIDATION_NOT_VALIDATED);
        target.setOptionValidationMessage(null);
        target.setOptionValidationTime(null);
        target.setSimulationValidationStatus(VALIDATION_NOT_VALIDATED);
        target.setSimulationValidationMessage(null);
        target.setSimulationValidationTime(null);
    }

    private void resetValidation(ProductFormula target) {
        target.setLatestValidationStatus(VALIDATION_NOT_VALIDATED);
        target.setMaterialValidationStatus(VALIDATION_NOT_VALIDATED);
        target.setOptionValidationStatus(VALIDATION_NOT_VALIDATED);
        target.setSimulationValidationStatus(VALIDATION_NOT_VALIDATED);
    }

    private String currentUsername() {
        try {
            return LoginHelper.getUsername();
        } catch (Exception ignored) {
            return "system";
        }
    }

    private void recordFormulaChange(Long formulaId, String formulaCode, String actionType, Object before, Object after) {
        changeLogService.record("FORMULA", "FORMULA", formulaId, formulaCode, actionType, before, after, null);
    }
}
