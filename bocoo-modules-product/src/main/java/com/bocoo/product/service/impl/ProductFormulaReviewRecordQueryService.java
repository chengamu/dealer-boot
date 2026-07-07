package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.vo.ProductFormulaReviewRecordVo;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductFormulaReviewRecordQueryService extends ProductServiceSupport {

    private static final String STATUS_REJECTED = ProductFormulaServiceImpl.STATUS_REJECTED;
    private static final String ACTION_SUBMIT_REVIEW = "SUBMIT_REVIEW";
    private static final String ACTION_APPROVE_VERSION = "APPROVE_VERSION";
    private static final String ACTION_REJECT = "REJECT";

    private final ProductFormulaVersionMapper versionMapper;

    public List<ProductFormulaReviewRecordVo> queryReviewRecords(Long reviewId) {
        ProductFormulaVersion current = requireReviewSnapshot(reviewId);
        List<ProductFormulaVersion> versions = versionMapper.selectList(activeQuery(ProductFormulaVersion.class)
            .eq("formula_id", current.getFormulaId())
            .orderByDesc("version_no", "version_id"));
        List<ProductFormulaReviewRecordVo> records = new ArrayList<>();
        for (ProductFormulaVersion version : versions) {
            if (version.getAuditTime() != null) {
                records.add(auditRecord(version));
            }
            if (version.getSubmitTime() != null) {
                records.add(submitRecord(version));
            }
        }
        return records;
    }

    private ProductFormulaVersion requireReviewSnapshot(Long reviewId) {
        ProductFormulaVersion review = reviewId == null ? null : versionMapper.selectById(reviewId);
        if (review == null || !"0".equals(review.getDelFlag())) {
            throw ServiceException.ofMessageKey("product.formula.reviewSnapshotRequired");
        }
        return review;
    }

    private ProductFormulaReviewRecordVo submitRecord(ProductFormulaVersion version) {
        return record(version, ACTION_SUBMIT_REVIEW, "提交审核", version.getSubmitBy(), version.getSubmitTime(), "提交版本快照，等待审核");
    }

    private ProductFormulaReviewRecordVo auditRecord(ProductFormulaVersion version) {
        if (STATUS_REJECTED.equals(version.getVersionStatus())) {
            return record(version, ACTION_REJECT, "驳回", version.getAuditBy(), version.getAuditTime(), version.getRejectReason());
        }
        return record(version, ACTION_APPROVE_VERSION, "审核通过", version.getAuditBy(), version.getAuditTime(), "审核通过并生成生效版本");
    }

    private ProductFormulaReviewRecordVo record(ProductFormulaVersion version, String actionType, String actionName,
                                                String operatorName, LocalDateTime operateTime, String remark) {
        ProductFormulaReviewRecordVo vo = new ProductFormulaReviewRecordVo();
        vo.setVersionId(version.getVersionId());
        vo.setFormulaId(version.getFormulaId());
        vo.setVersionNo(version.getVersionNo());
        vo.setVersionLabel(version.getVersionLabel());
        vo.setActionType(actionType);
        vo.setActionName(actionName);
        vo.setOperatorName(operatorName);
        vo.setOperateTime(operateTime);
        vo.setVersionStatus(version.getVersionStatus());
        vo.setValidationStatus(version.getValidationStatus());
        vo.setRejectReason(version.getRejectReason());
        vo.setRemark(remark);
        return vo;
    }
}
