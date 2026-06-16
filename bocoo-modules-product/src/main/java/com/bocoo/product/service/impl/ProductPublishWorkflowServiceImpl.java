package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.product.domain.bo.PublishCheckBo;
import com.bocoo.product.domain.entity.ConfigQuestion;
import com.bocoo.product.domain.entity.PriceRuleItem;
import com.bocoo.product.domain.entity.ProductPublishPackage;
import com.bocoo.product.domain.entity.ProductSyncOutbox;
import com.bocoo.product.domain.entity.PublishApproval;
import com.bocoo.product.domain.entity.PublishCheckResult;
import com.bocoo.product.domain.vo.ProductSyncOutboxVo;
import com.bocoo.product.domain.vo.PublishCheckResultVo;
import com.bocoo.product.domain.vo.PublishCheckSummaryVo;
import com.bocoo.product.domain.vo.PublishExecutionResultVo;
import com.bocoo.product.mapper.ConfigQuestionMapper;
import com.bocoo.product.mapper.PriceRuleItemMapper;
import com.bocoo.product.mapper.ProductPublishPackageMapper;
import com.bocoo.product.mapper.ProductSyncOutboxMapper;
import com.bocoo.product.mapper.PublishApprovalMapper;
import com.bocoo.product.mapper.PublishCheckResultMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductPublishWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPublishWorkflowServiceImpl implements ProductPublishWorkflowService {

    private static final List<String> PUBLISH_TARGET_SYSTEMS = List.of("ORDER", "ERP", "MES");

    private final PublishCheckResultMapper checkResultMapper;
    private final PublishApprovalMapper approvalMapper;
    private final ProductPublishPackageMapper packageMapper;
    private final ProductSyncOutboxMapper outboxMapper;
    private final ConfigQuestionMapper questionMapper;
    private final PriceRuleItemMapper ruleItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PublishCheckSummaryVo check(PublishCheckBo bo) {
        if (bo == null) {
            bo = new PublishCheckBo();
        }
        PublishCheckSummaryVo summary = new PublishCheckSummaryVo();
        addCheck(summary, bo, "MODEL_IDENTITY", "产品身份", "Product Identity",
            bo.getProductModelId() != null || StringUtils.isNotBlank(bo.getProductModelCode()),
            "product.publish.model.required", "产品模型不能为空", "Product model is required", true);
        boolean hasTemplate = bo.getTemplateVersionId() != null;
        addCheck(summary, bo, "TEMPLATE_VERSION", "配置模板版本", "Template Version",
            hasTemplate, "product.publish.templateVersion.required", "配置模板版本不能为空", "Template version is required", true);
        if (hasTemplate) {
            long questionCount = questionMapper.selectCount(Wrappers.lambdaQuery(ConfigQuestion.class)
                .eq(ConfigQuestion::getTemplateVersionId, bo.getTemplateVersionId())
                .eq(ConfigQuestion::getStatus, "1"));
            addCheck(summary, bo, "TEMPLATE_QUESTION", "配置问题", "Template Questions",
                questionCount > 0, "product.publish.question.missing", "配置模板没有启用问题", "Template has no enabled questions", false);
        }
        boolean hasPrice = bo.getPricePlanVersionId() != null;
        addCheck(summary, bo, "PRICE_VERSION", "价格方案版本", "Price Version",
            hasPrice, "product.publish.priceVersion.required", "价格方案版本不能为空", "Price version is required", true);
        if (hasPrice) {
            long priceRuleCount = ruleItemMapper.selectCount(Wrappers.lambdaQuery(PriceRuleItem.class)
                .eq(PriceRuleItem::getPricePlanVersionId, bo.getPricePlanVersionId())
                .eq(PriceRuleItem::getStatus, "1"));
            addCheck(summary, bo, "PRICE_RULE", "价格规则", "Price Rules",
                priceRuleCount > 0, "product.publish.priceRule.missing", "价格方案没有启用规则项", "Price version has no enabled rule items", true);
        }
        if (summary.getBlockerCount() > 0) {
            summary.setResultStatus("BLOCKER");
        } else if (summary.getWarningCount() > 0) {
            summary.setResultStatus("WARNING");
        }
        return summary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PublishExecutionResultVo publish(PublishCheckBo bo) {
        PublishExecutionResultVo result = new PublishExecutionResultVo();
        PublishCheckSummaryVo summary = check(bo);
        result.setCheckSummary(summary);
        if (summary.getBlockerCount() > 0) {
            result.setResultStatus("BLOCKER");
            return result;
        }
        String snapshot = PublishSnapshotSupport.snapshotJson(bo);
        String hash = PublishSnapshotSupport.sha256(snapshot);
        PublishApproval approval = approvalMapper.selectOne(Wrappers.<PublishApproval>lambdaQuery()
            .eq(PublishApproval::getSnapshotHash, hash)
            .eq(PublishApproval::getApprovalStatus, "APPROVED")
            .eq(PublishApproval::getStatus, "1")
            .orderByDesc(PublishApproval::getApprovedTime)
            .last("limit 1"));
        if (approval == null) {
            throw ServiceException.ofMessageKey("product.publish.approval.required");
        }
        ProductPublishPackage existing = packageMapper.selectOne(Wrappers.<ProductPublishPackage>lambdaQuery()
            .eq(ProductPublishPackage::getPackageHash, hash)
            .eq(ProductPublishPackage::getPackageStatus, "PUBLISHED")
            .orderByDesc(ProductPublishPackage::getPublishedTime)
            .last("limit 1"));
        if (existing != null) {
            fillPublishedResult(result, existing, hash, snapshot);
            return result;
        }
        ProductPublishPackage entity = new ProductPublishPackage();
        entity.setPackageCode("PKG-" + TimeUtils.utcNow().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        entity.setPackageType("PRODUCT");
        entity.setProductModelId(bo.getProductModelId());
        entity.setProductModelCode(bo.getProductModelCode());
        entity.setSalesVariantId(bo.getSalesVariantId());
        entity.setSalesVariantCode(bo.getSalesVariantCode());
        entity.setTemplateVersionId(bo.getTemplateVersionId());
        entity.setTemplateVersionNo(bo.getTemplateVersionNo());
        entity.setPricePlanVersionId(bo.getPricePlanVersionId());
        entity.setPricePlanCode(bo.getPricePlanCode());
        entity.setPackageStatus("PUBLISHED");
        entity.setPackageHash(hash);
        entity.setPackageJson(snapshot);
        entity.setVersionSnapshotJson(snapshot);
        entity.setPublishedTime(TimeUtils.utcNow());
        ProductEntityDefaults.prepareInsert(entity);
        packageMapper.insert(entity);
        fillPublishedResult(result, entity, hash, snapshot);
        return result;
    }

    private void fillPublishedResult(PublishExecutionResultVo result, ProductPublishPackage entity, String hash, String snapshot) {
        List<ProductSyncOutboxVo> outboxes = createPublishOutboxes(entity, hash, snapshot);
        result.setResultStatus("PUBLISHED");
        result.setPackageInfo(packageMapper.selectVoById(entity.getPackageId()));
        result.setOutboxes(outboxes);
        if (!outboxes.isEmpty()) {
            result.setOutbox(outboxes.get(0));
        }
    }

    private List<ProductSyncOutboxVo> createPublishOutboxes(ProductPublishPackage entity, String hash, String snapshot) {
        List<ProductSyncOutboxVo> outboxes = new ArrayList<>();
        for (String targetSystem : PUBLISH_TARGET_SYSTEMS) {
            ProductSyncOutbox existing = outboxMapper.selectOne(Wrappers.<ProductSyncOutbox>lambdaQuery()
                .eq(ProductSyncOutbox::getPackageId, entity.getPackageId())
                .eq(ProductSyncOutbox::getTargetSystem, targetSystem)
                .eq(ProductSyncOutbox::getEventType, "PRODUCT_PACKAGE_PUBLISHED")
                .eq(ProductSyncOutbox::getPayloadHash, hash)
                .last("limit 1"));
            if (existing != null) {
                outboxes.add(outboxMapper.selectVoById(existing.getOutboxId()));
                continue;
            }
            ProductSyncOutbox outbox = new ProductSyncOutbox();
            outbox.setPackageId(entity.getPackageId());
            outbox.setPackageCode(entity.getPackageCode());
            outbox.setTargetSystem(targetSystem);
            outbox.setEventType("PRODUCT_PACKAGE_PUBLISHED");
            outbox.setPayloadHash(hash);
            outbox.setPayloadJson(snapshot);
            outbox.setSyncStatus("PENDING");
            outbox.setRetryCount(0);
            outbox.setStatus("1");
            ProductEntityDefaults.prepareInsert(outbox);
            outboxMapper.insert(outbox);
            outboxes.add(outboxMapper.selectVoById(outbox.getOutboxId()));
        }
        return outboxes;
    }

    private void addCheck(PublishCheckSummaryVo summary, PublishCheckBo bo, String code, String nameCn, String nameEn, boolean pass,
                          String messageKey, String messageCn, String messageEn, boolean blocker) {
        String evidence = PublishSnapshotSupport.snapshotJson(bo);
        PublishCheckResult entity = new PublishCheckResult();
        entity.setTargetType("PRODUCT");
        entity.setTargetId(bo.getProductModelId());
        entity.setTargetCode(bo.getProductModelCode());
        entity.setCheckCode(code);
        entity.setCheckNameCn(nameCn);
        entity.setCheckNameEn(nameEn);
        entity.setCheckLevel(pass ? "PASS" : (blocker ? "BLOCKER" : "WARNING"));
        entity.setCheckStatus(entity.getCheckLevel());
        entity.setMessageKey(pass ? "product.publish.check.pass" : messageKey);
        entity.setMessageCn(pass ? "检查通过" : messageCn);
        entity.setMessageEn(pass ? "Check passed" : messageEn);
        entity.setEvidenceJson(evidence);
        entity.setResolvedFlag(pass ? "1" : "0");
        entity.setStatus("1");
        checkResultMapper.delete(Wrappers.<PublishCheckResult>lambdaQuery()
            .eq(PublishCheckResult::getTargetType, entity.getTargetType())
            .eq(entity.getTargetId() != null, PublishCheckResult::getTargetId, entity.getTargetId())
            .eq(StringUtils.isNotBlank(entity.getTargetCode()), PublishCheckResult::getTargetCode, entity.getTargetCode())
            .eq(PublishCheckResult::getCheckCode, code)
            .eq(PublishCheckResult::getEvidenceJson, evidence));
        ProductEntityDefaults.prepareInsert(entity);
        checkResultMapper.insert(entity);
        PublishCheckResultVo vo = checkResultMapper.selectVoById(entity.getCheckId());
        summary.getResults().add(vo);
        if ("BLOCKER".equals(entity.getCheckLevel())) {
            summary.setBlockerCount(summary.getBlockerCount() + 1);
        } else if ("WARNING".equals(entity.getCheckLevel())) {
            summary.setWarningCount(summary.getWarningCount() + 1);
        } else {
            summary.setPassCount(summary.getPassCount() + 1);
        }
    }
}
