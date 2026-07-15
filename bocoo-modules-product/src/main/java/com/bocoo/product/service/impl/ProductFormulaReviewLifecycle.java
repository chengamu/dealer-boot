package com.bocoo.product.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.vo.ProductFormulaVersionVo;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import com.bocoo.product.service.ProductChangeLogService;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductFormulaSetupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductFormulaReviewLifecycle extends ProductServiceSupport {

    private static final String STATUS_DRAFT = ProductFormulaServiceImpl.STATUS_DRAFT;
    private static final String STATUS_PENDING_REVIEW = ProductFormulaServiceImpl.STATUS_PENDING_REVIEW;
    private static final String STATUS_REJECTED = ProductFormulaServiceImpl.STATUS_REJECTED;
    private static final String STATUS_EFFECTIVE = ProductFormulaServiceImpl.STATUS_EFFECTIVE;
    private static final String STATUS_STOPPED = ProductFormulaServiceImpl.STATUS_STOPPED;
    private static final String VALIDATION_PASS = ProductFormulaServiceImpl.VALIDATION_PASS;

    private final ProductFormulaMapper formulaMapper;
    private final ProductFormulaVersionMapper versionMapper;
    private final ProductFormulaSetupService setupService;
    private final ProductChangeLogService changeLogService;
    private final ProductFormulaSnapshotJson snapshotJson;
    private final ProductFormulaVersionReferenceGuard versionReferenceGuard;

    @Transactional(rollbackFor = Exception.class)
    public Boolean submitReview(Long id) {
        ProductFormula current = requireFormula(id);
        if (!STATUS_DRAFT.equals(current.getStatus()) && !STATUS_REJECTED.equals(current.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.submitReviewDenied");
        }
        if (!Boolean.TRUE.equals(current.getConfiguredFlag()) || current.getMaterialLineCount() == null || current.getMaterialLineCount() <= 0) {
            throw ServiceException.ofMessageKey("product.formula.notConfigured");
        }
        if (!VALIDATION_PASS.equals(current.getLatestValidationStatus())) {
            throw ServiceException.ofMessageKey("product.formula.validationRequired");
        }
        assertStagePassed(current.getMaterialValidationStatus(), "product.formula.materialValidationRequired");
        assertStagePassed(current.getOptionValidationStatus(), "product.formula.optionValidationRequired");
        assertStagePassed(current.getSimulationValidationStatus(), "product.formula.simulationValidationRequired");
        if (versionMapper.selectCount(activeQuery(ProductFormulaVersion.class)
            .eq("formula_id", id)
            .eq("version_status", STATUS_PENDING_REVIEW)) > 0) {
            throw ServiceException.ofMessageKey("product.formula.reviewSnapshotInvalid");
        }
        ProductFormulaVersion review = buildReviewSnapshot(current, setupService.snapshot(id));
        ProductEntityDefaults.prepareInsert(review);
        if (versionMapper.insert(review) <= 0) {
            return Boolean.FALSE;
        }
        ProductFormula after = snapshotJson.copyStatusSnapshot(current);
        after.setStatus(STATUS_PENDING_REVIEW);
        boolean updated = formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, id)
            .in(ProductFormula::getStatus, List.of(STATUS_DRAFT, STATUS_REJECTED))
            .set(ProductFormula::getStatus, STATUS_PENDING_REVIEW)
            .set(ProductFormula::getRejectReason, null)) > 0;
        if (!updated) {
            throw ServiceException.ofMessageKey("product.formula.submitReviewDenied");
        }
        recordFormulaChange(id, current.getFormulaCode(), "SUBMIT_REVIEW", current, after);
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(Long id) {
        ProductFormula current = requireFormula(id);
        if (!STATUS_PENDING_REVIEW.equals(current.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.approveDenied");
        }
        ProductFormulaVersion version = requirePendingReviewVersion(id);
        String auditBy = currentUsername();
        LocalDateTime auditTime = TimeUtils.utcNow();
        versionMapper.update(null, new LambdaUpdateWrapper<ProductFormulaVersion>()
            .eq(ProductFormulaVersion::getVersionId, version.getVersionId())
            .set(ProductFormulaVersion::getVersionStatus, STATUS_EFFECTIVE)
            .set(ProductFormulaVersion::getAuditBy, auditBy)
            .set(ProductFormulaVersion::getAuditTime, auditTime)
            .set(ProductFormulaVersion::getRejectReason, null));
        ProductFormula after = snapshotJson.copyStatusSnapshot(current);
        after.setStatus(STATUS_EFFECTIVE);
        after.setAuditBy(auditBy);
        after.setAuditTime(auditTime);
        after.setRejectReason(null);
        after.setCurrentVersionId(version.getVersionId());
        after.setCurrentVersionNo(version.getVersionNo());
        after.setCurrentVersionLabel(version.getVersionLabel());
        boolean updated = formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, id)
            .set(ProductFormula::getStatus, STATUS_EFFECTIVE)
            .set(ProductFormula::getAuditBy, auditBy)
            .set(ProductFormula::getAuditTime, auditTime)
            .set(ProductFormula::getRejectReason, null)
            .set(ProductFormula::getCurrentVersionId, version.getVersionId())
            .set(ProductFormula::getCurrentVersionNo, version.getVersionNo())
            .set(ProductFormula::getCurrentVersionLabel, version.getVersionLabel())) > 0;
        if (updated) {
            recordFormulaChange(id, current.getFormulaCode(), "APPROVE_VERSION", current, after);
        }
        return updated;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean reject(Long id, String rejectReason) {
        ProductFormula current = requireFormula(id);
        if (!STATUS_PENDING_REVIEW.equals(current.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.rejectDenied");
        }
        if (StringUtils.isBlank(rejectReason)) {
            throw ServiceException.ofMessageKey("product.formula.rejectReasonRequired");
        }
        ProductFormulaVersion review = requirePendingReviewVersion(id);
        versionMapper.update(null, new LambdaUpdateWrapper<ProductFormulaVersion>()
            .eq(ProductFormulaVersion::getVersionId, review.getVersionId())
            .set(ProductFormulaVersion::getVersionStatus, STATUS_REJECTED)
            .set(ProductFormulaVersion::getAuditBy, currentUsername())
            .set(ProductFormulaVersion::getAuditTime, TimeUtils.utcNow())
            .set(ProductFormulaVersion::getRejectReason, rejectReason.trim()));
        return updateFormulaStatus(current, STATUS_REJECTED, "REJECT", rejectReason.trim());
    }

    public ProductFormulaVersionVo queryReviewById(Long reviewId) {
        return versionMapper.selectVoById(reviewId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean approveReview(Long reviewId) {
        ProductFormulaVersion review = requireReview(reviewId);
        return approve(review.getFormulaId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean rejectReview(Long reviewId, String rejectReason) {
        ProductFormulaVersion review = requireReview(reviewId);
        return reject(review.getFormulaId(), rejectReason);
    }

    @Transactional(rollbackFor = Exception.class)
    @Lock4j(name = "product-formula-status", keys = {"#id"})
    public Boolean stop(Long id) {
        ProductFormula current = requireFormula(id);
        if (!STATUS_EFFECTIVE.equals(current.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.stopDenied");
        }
        versionReferenceGuard.assertNoEnabledSaleProductByFormula(current.getFormulaId());
        ProductFormula after = snapshotJson.copyStatusSnapshot(current);
        after.setStatus(STATUS_STOPPED);
        boolean updated = formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, id)
            .set(ProductFormula::getStatus, STATUS_STOPPED)) > 0;
        if (updated) {
            markCurrentVersionStopped(current);
            recordFormulaChange(id, current.getFormulaCode(), "STOP", current, after);
        }
        return updated;
    }

    public List<ProductFormulaVersionVo> queryVersions(Long formulaId) {
        return versionMapper.selectVoList(activeQuery(ProductFormulaVersion.class)
            .eq("formula_id", formulaId)
            .in("version_status", List.of(STATUS_EFFECTIVE, STATUS_STOPPED))
            .orderByDesc("version_no", "version_id"));
    }

    public ProductFormulaVersionVo queryVersionById(Long formulaId, Long versionId) {
        return versionMapper.selectVoOne(activeQuery(ProductFormulaVersion.class)
            .eq("formula_id", formulaId)
            .eq("version_id", versionId));
    }

    private Boolean updateFormulaStatus(ProductFormula current, String status, String actionType, String rejectReason) {
        ProductFormula after = snapshotJson.copyStatusSnapshot(current);
        after.setStatus(status);
        after.setRejectReason(rejectReason);
        boolean updated = formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, current.getFormulaId())
            .set(ProductFormula::getStatus, status)
            .set(ProductFormula::getRejectReason, rejectReason)) > 0;
        if (updated) {
            recordFormulaChange(current.getFormulaId(), current.getFormulaCode(), actionType, current, after);
        }
        return updated;
    }

    private ProductFormulaVersion buildReviewSnapshot(ProductFormula current, Map<String, Object> setupSnapshot) {
        int versionNo = nextVersionNo(current.getFormulaId());
        ProductFormulaVersion version = new ProductFormulaVersion();
        version.setTenantId(current.getTenantId());
        version.setFormulaId(current.getFormulaId());
        version.setVersionNo(versionNo);
        version.setVersionLabel(versionLabel(versionNo));
        version.setVersionStatus(STATUS_PENDING_REVIEW);
        version.setFormulaSnapshotJson(snapshotJson.toJson(current));
        version.setSetupSnapshotJson(snapshotJson.toJson(setupSnapshot));
        version.setValidationStatus(VALIDATION_PASS);
        Map<String, Object> validationReport = new LinkedHashMap<>();
        validationReport.put("status", VALIDATION_PASS);
        validationReport.put("materialStatus", current.getMaterialValidationStatus());
        validationReport.put("optionStatus", current.getOptionValidationStatus());
        validationReport.put("simulationStatus", current.getSimulationValidationStatus());
        validationReport.put("message", "product.formula.validationPassed");
        version.setValidationReportJson(snapshotJson.toJson(validationReport));
        version.setSubmitBy(currentUsername());
        version.setSubmitTime(TimeUtils.utcNow());
        return version;
    }

    private int nextVersionNo(Long formulaId) {
        ProductFormulaVersion latest = versionMapper.selectOne(activeQuery(ProductFormulaVersion.class)
            .eq("formula_id", formulaId)
            .in("version_status", List.of(STATUS_EFFECTIVE, STATUS_STOPPED))
            .orderByDesc("version_no")
            .last("limit 1"));
        return latest == null || latest.getVersionNo() == null ? 1 : latest.getVersionNo() + 1;
    }

    private void markCurrentVersionStopped(ProductFormula current) {
        if (current.getCurrentVersionId() == null) {
            return;
        }
        versionMapper.update(null, new LambdaUpdateWrapper<ProductFormulaVersion>()
            .eq(ProductFormulaVersion::getVersionId, current.getCurrentVersionId())
            .set(ProductFormulaVersion::getVersionStatus, STATUS_STOPPED));
    }

    private ProductFormula requireFormula(Long id) {
        ProductFormula current = id == null ? null : formulaMapper.selectActiveByIdForUpdate(id);
        if (current == null) {
            throw ServiceException.ofMessageKey("product.base.edit.notFound");
        }
        return current;
    }

    private ProductFormulaVersion requirePendingReviewVersion(Long formulaId) {
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

    private ProductFormulaVersion requireReview(Long reviewId) {
        ProductFormulaVersion review = reviewId == null ? null : versionMapper.selectById(reviewId);
        if (review == null || !"0".equals(review.getDelFlag())) {
            throw ServiceException.ofMessageKey("product.formula.reviewSnapshotRequired");
        }
        if (!STATUS_PENDING_REVIEW.equals(review.getVersionStatus())) {
            throw ServiceException.ofMessageKey("product.formula.reviewSnapshotInvalid");
        }
        return review;
    }

    private void assertStagePassed(String status, String messageKey) {
        if (!VALIDATION_PASS.equals(status)) {
            throw ServiceException.ofMessageKey(messageKey);
        }
    }

    private String versionLabel(Integer versionNo) {
        return "V" + versionNo;
    }

    private String currentUsername() {
        try {
            return LoginHelper.getUsername();
        } catch (Exception ignored) {
            return "system";
        }
    }

    private void recordFormulaChange(Long formulaId, String formulaCode, String actionType,
                                     Object before, Object after) {
        changeLogService.record("FORMULA", "FORMULA", formulaId, formulaCode, actionType, before, after, null);
    }
}
