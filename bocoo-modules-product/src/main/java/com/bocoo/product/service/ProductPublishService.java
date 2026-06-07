package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.product.domain.bo.*;
import com.bocoo.product.domain.entity.*;
import com.bocoo.product.domain.vo.*;
import com.bocoo.product.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

/**
 * 产品发布服务。
 */
@Service
@RequiredArgsConstructor
public class ProductPublishService {

    private static final List<String> PUBLISH_TARGET_SYSTEMS = List.of("ORDER", "ERP", "MES");

    private final PublishCheckResultMapper checkIdMapper;
    private final PublishApprovalMapper approvalIdMapper;
    private final ProductPublishPackageMapper packageIdMapper;
    private final ProductSyncOutboxMapper outboxIdMapper;
    private final ConfigQuestionMapper questionIdMapper;
    private final PriceRuleItemMapper ruleItemIdMapper;

    public TableDataInfo<PublishCheckResultVo> queryPublishCheckResultPage(PublishCheckResultBo bo, PageQuery pageQuery) {
        Page<PublishCheckResultVo> result = checkIdMapper.selectVoPage(pageQuery.build(), buildPublishCheckResultWrapper(bo));
        return TableDataInfo.build(result);
    }

    public TableDataInfo<PublishCheckResultVo> queryGapTaskPage(PublishCheckResultBo bo, PageQuery pageQuery) {
        Page<PublishCheckResultVo> result = checkIdMapper.selectVoPage(pageQuery.build(), buildPublishCheckResultWrapper(bo)
            .in(PublishCheckResult::getCheckLevel, List.of("BLOCKER", "WARNING"))
            .eq(PublishCheckResult::getResolvedFlag, "0"));
        return TableDataInfo.build(result);
    }

    public java.util.List<PublishCheckResultVo> queryPublishCheckResultList(PublishCheckResultBo bo) {
        return checkIdMapper.selectVoList(buildPublishCheckResultWrapper(bo));
    }

    public PublishCheckResultVo getPublishCheckResultById(Long id) {
        return checkIdMapper.selectVoById(id);
    }

    public Boolean savePublishCheckResult(PublishCheckResultBo bo) {
        PublishCheckResult entity = MapstructUtils.convert(bo, PublishCheckResult.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getCheckId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return checkIdMapper.insert(entity) > 0;
        }
        return checkIdMapper.updateById(entity) > 0;
    }

    public Boolean removePublishCheckResultByIds(Long[] ids) {
        return checkIdMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean resolveGapTask(Long checkId) {
        PublishCheckResult entity = new PublishCheckResult();
        entity.setCheckId(checkId);
        entity.setResolvedFlag("1");
        entity.setCheckStatus("RESOLVED");
        return checkIdMapper.updateById(entity) > 0;
    }

    private LambdaQueryWrapper<PublishCheckResult> buildPublishCheckResultWrapper(PublishCheckResultBo bo) {
        if (bo == null) {
            bo = new PublishCheckResultBo();
        }
        LambdaQueryWrapper<PublishCheckResult> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getTargetType()), PublishCheckResult::getTargetType, bo.getTargetType());
        lqw.like(StringUtils.isNotBlank(bo.getTargetCode()), PublishCheckResult::getTargetCode, bo.getTargetCode());
        lqw.like(StringUtils.isNotBlank(bo.getCheckCode()), PublishCheckResult::getCheckCode, bo.getCheckCode());
        lqw.like(StringUtils.isNotBlank(bo.getCheckLevel()), PublishCheckResult::getCheckLevel, bo.getCheckLevel());
        lqw.like(StringUtils.isNotBlank(bo.getCheckStatus()), PublishCheckResult::getCheckStatus, bo.getCheckStatus());
        lqw.like(StringUtils.isNotBlank(bo.getResolvedFlag()), PublishCheckResult::getResolvedFlag, bo.getResolvedFlag());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), PublishCheckResult::getStatus, bo.getStatus());
        return lqw;
    }


    public TableDataInfo<PublishApprovalVo> queryPublishApprovalPage(PublishApprovalBo bo, PageQuery pageQuery) {
        Page<PublishApprovalVo> result = approvalIdMapper.selectVoPage(pageQuery.build(), buildPublishApprovalWrapper(bo));
        return TableDataInfo.build(result);
    }

    public java.util.List<PublishApprovalVo> queryPublishApprovalList(PublishApprovalBo bo) {
        return approvalIdMapper.selectVoList(buildPublishApprovalWrapper(bo));
    }

    public PublishApprovalVo getPublishApprovalById(Long id) {
        return approvalIdMapper.selectVoById(id);
    }

    public Boolean savePublishApproval(PublishApprovalBo bo) {
        PublishApproval entity = MapstructUtils.convert(bo, PublishApproval.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getApprovalId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return approvalIdMapper.insert(entity) > 0;
        }
        return approvalIdMapper.updateById(entity) > 0;
    }

    public Boolean removePublishApprovalByIds(Long[] ids) {
        return approvalIdMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean updatePublishApprovalStatus(Long approvalId, String approvalStatus, String approvalComment) {
        PublishApproval current = approvalIdMapper.selectById(approvalId);
        if (current == null) {
            throw ServiceException.ofMessageKey("product.publish.approval.notFound");
        }
        if (!"SUBMITTED".equals(current.getApprovalStatus())) {
            throw ServiceException.ofMessageKey("product.publish.approval.submittedOnly");
        }
        PublishApproval entity = new PublishApproval();
        entity.setApprovalId(approvalId);
        entity.setApprovalStatus(approvalStatus);
        entity.setApproverUserId(LoginHelper.getUserId());
        entity.setApproverName(LoginHelper.getUsername());
        entity.setApprovedTime(TimeUtils.utcNow());
        entity.setApprovalComment(approvalComment);
        entity.setStatus("1");
        return approvalIdMapper.updateById(entity) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public PublishApprovalVo submitPublishApproval(PublishCheckBo bo) {
        PublishCheckSummaryVo summary = check(bo);
        if (summary.getBlockerCount() > 0) {
            throw ServiceException.ofMessageKey("product.publish.approval.blocker");
        }
        String snapshot = snapshotJson(bo);
        String hash = sha256(snapshot);
        PublishApproval existing = approvalIdMapper.selectOne(Wrappers.<PublishApproval>lambdaQuery()
            .eq(PublishApproval::getSnapshotHash, hash)
            .eq(PublishApproval::getStatus, "1")
            .in(PublishApproval::getApprovalStatus, List.of("SUBMITTED", "APPROVED"))
            .orderByDesc(PublishApproval::getCreateTime)
            .last("limit 1"));
        if (existing != null) {
            return approvalIdMapper.selectVoById(existing.getApprovalId());
        }
        PublishApproval entity = new PublishApproval();
        entity.setTargetType("PRODUCT");
        entity.setTargetId(bo.getProductModelId());
        entity.setTargetCode(StringUtils.blankToDefault(bo.getProductModelCode(), bo.getSalesVariantCode()));
        entity.setApprovalStatus("SUBMITTED");
        entity.setSubmitterUserId(LoginHelper.getUserId());
        entity.setSubmitterName(LoginHelper.getUsername());
        entity.setSnapshotHash(hash);
        entity.setSnapshotJson(snapshot);
        entity.setStatus("1");
        ProductEntityDefaults.prepareInsert(entity);
        approvalIdMapper.insert(entity);
        return approvalIdMapper.selectVoById(entity.getApprovalId());
    }

    private LambdaQueryWrapper<PublishApproval> buildPublishApprovalWrapper(PublishApprovalBo bo) {
        if (bo == null) {
            bo = new PublishApprovalBo();
        }
        LambdaQueryWrapper<PublishApproval> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getTargetType()), PublishApproval::getTargetType, bo.getTargetType());
        lqw.like(StringUtils.isNotBlank(bo.getTargetCode()), PublishApproval::getTargetCode, bo.getTargetCode());
        lqw.like(StringUtils.isNotBlank(bo.getApprovalStatus()), PublishApproval::getApprovalStatus, bo.getApprovalStatus());
        lqw.like(StringUtils.isNotBlank(bo.getSubmitterName()), PublishApproval::getSubmitterName, bo.getSubmitterName());
        lqw.like(StringUtils.isNotBlank(bo.getApproverName()), PublishApproval::getApproverName, bo.getApproverName());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), PublishApproval::getStatus, bo.getStatus());
        return lqw;
    }


    public TableDataInfo<ProductPublishPackageVo> queryProductPublishPackagePage(ProductPublishPackageBo bo, PageQuery pageQuery) {
        Page<ProductPublishPackageVo> result = packageIdMapper.selectVoPage(pageQuery.build(), buildProductPublishPackageWrapper(bo));
        return TableDataInfo.build(result);
    }

    public java.util.List<ProductPublishPackageVo> queryProductPublishPackageList(ProductPublishPackageBo bo) {
        return packageIdMapper.selectVoList(buildProductPublishPackageWrapper(bo));
    }

    public ProductPublishPackageVo getProductPublishPackageById(Long id) {
        return packageIdMapper.selectVoById(id);
    }

    public Boolean saveProductPublishPackage(ProductPublishPackageBo bo) {
        ProductPublishPackage entity = MapstructUtils.convert(bo, ProductPublishPackage.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getPackageId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return packageIdMapper.insert(entity) > 0;
        }
        return packageIdMapper.updateById(entity) > 0;
    }

    public Boolean removeProductPublishPackageByIds(Long[] ids) {
        return packageIdMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    private LambdaQueryWrapper<ProductPublishPackage> buildProductPublishPackageWrapper(ProductPublishPackageBo bo) {
        if (bo == null) {
            bo = new ProductPublishPackageBo();
        }
        LambdaQueryWrapper<ProductPublishPackage> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getPackageCode()), ProductPublishPackage::getPackageCode, bo.getPackageCode());
        lqw.like(StringUtils.isNotBlank(bo.getPackageType()), ProductPublishPackage::getPackageType, bo.getPackageType());
        lqw.like(StringUtils.isNotBlank(bo.getProductModelCode()), ProductPublishPackage::getProductModelCode, bo.getProductModelCode());
        lqw.like(StringUtils.isNotBlank(bo.getSalesVariantCode()), ProductPublishPackage::getSalesVariantCode, bo.getSalesVariantCode());
        lqw.like(StringUtils.isNotBlank(bo.getPricePlanCode()), ProductPublishPackage::getPricePlanCode, bo.getPricePlanCode());
        lqw.like(StringUtils.isNotBlank(bo.getPackageStatus()), ProductPublishPackage::getPackageStatus, bo.getPackageStatus());
        lqw.like(StringUtils.isNotBlank(bo.getPackageHash()), ProductPublishPackage::getPackageHash, bo.getPackageHash());
        return lqw;
    }


    public TableDataInfo<ProductSyncOutboxVo> queryProductSyncOutboxPage(ProductSyncOutboxBo bo, PageQuery pageQuery) {
        Page<ProductSyncOutboxVo> result = outboxIdMapper.selectVoPage(pageQuery.build(), buildProductSyncOutboxWrapper(bo));
        return TableDataInfo.build(result);
    }

    public java.util.List<ProductSyncOutboxVo> queryProductSyncOutboxList(ProductSyncOutboxBo bo) {
        return outboxIdMapper.selectVoList(buildProductSyncOutboxWrapper(bo));
    }

    public ProductSyncOutboxVo getProductSyncOutboxById(Long id) {
        return outboxIdMapper.selectVoById(id);
    }

    public Boolean saveProductSyncOutbox(ProductSyncOutboxBo bo) {
        ProductSyncOutbox entity = MapstructUtils.convert(bo, ProductSyncOutbox.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getOutboxId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return outboxIdMapper.insert(entity) > 0;
        }
        return outboxIdMapper.updateById(entity) > 0;
    }

    public Boolean removeProductSyncOutboxByIds(Long[] ids) {
        return outboxIdMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean retryProductSyncOutbox(Long outboxId) {
        ProductSyncOutbox current = outboxIdMapper.selectById(outboxId);
        if (current == null) {
            return Boolean.FALSE;
        }
        ProductSyncOutbox entity = new ProductSyncOutbox();
        entity.setOutboxId(outboxId);
        entity.setSyncStatus("PENDING");
        entity.setRetryCount((current.getRetryCount() == null ? 0 : current.getRetryCount()) + 1);
        entity.setNextRetryTime(TimeUtils.utcNow());
        entity.setLastErrorKey(null);
        entity.setLastErrorMessage(null);
        return outboxIdMapper.updateById(entity) > 0;
    }

    private LambdaQueryWrapper<ProductSyncOutbox> buildProductSyncOutboxWrapper(ProductSyncOutboxBo bo) {
        if (bo == null) {
            bo = new ProductSyncOutboxBo();
        }
        LambdaQueryWrapper<ProductSyncOutbox> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getPackageCode()), ProductSyncOutbox::getPackageCode, bo.getPackageCode());
        lqw.like(StringUtils.isNotBlank(bo.getTargetSystem()), ProductSyncOutbox::getTargetSystem, bo.getTargetSystem());
        lqw.like(StringUtils.isNotBlank(bo.getEventType()), ProductSyncOutbox::getEventType, bo.getEventType());
        lqw.like(StringUtils.isNotBlank(bo.getSyncStatus()), ProductSyncOutbox::getSyncStatus, bo.getSyncStatus());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), ProductSyncOutbox::getStatus, bo.getStatus());
        lqw.orderByDesc(ProductSyncOutbox::getCreateTime);
        return lqw;
    }

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
            long questionCount = questionIdMapper.selectCount(Wrappers.lambdaQuery(ConfigQuestion.class)
                .eq(ConfigQuestion::getTemplateVersionId, bo.getTemplateVersionId())
                .eq(ConfigQuestion::getStatus, "1"));
            addCheck(summary, bo, "TEMPLATE_QUESTION", "配置问题", "Template Questions",
                questionCount > 0, "product.publish.question.missing", "配置模板没有启用问题", "Template has no enabled questions", false);
        }
        boolean hasPrice = bo.getPricePlanVersionId() != null;
        addCheck(summary, bo, "PRICE_VERSION", "价格方案版本", "Price Version",
            hasPrice, "product.publish.priceVersion.required", "价格方案版本不能为空", "Price version is required", true);
        if (hasPrice) {
            long priceRuleCount = ruleItemIdMapper.selectCount(Wrappers.lambdaQuery(PriceRuleItem.class)
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

    @Transactional(rollbackFor = Exception.class)
    public PublishExecutionResultVo publish(PublishCheckBo bo) {
        PublishExecutionResultVo result = new PublishExecutionResultVo();
        PublishCheckSummaryVo summary = check(bo);
        result.setCheckSummary(summary);
        if (summary.getBlockerCount() > 0) {
            result.setResultStatus("BLOCKER");
            return result;
        }
        String snapshot = snapshotJson(bo);
        String hash = sha256(snapshot);
        PublishApproval approval = approvalIdMapper.selectOne(Wrappers.<PublishApproval>lambdaQuery()
            .eq(PublishApproval::getSnapshotHash, hash)
            .eq(PublishApproval::getApprovalStatus, "APPROVED")
            .eq(PublishApproval::getStatus, "1")
            .orderByDesc(PublishApproval::getApprovedTime)
            .last("limit 1"));
        if (approval == null) {
            throw ServiceException.ofMessageKey("product.publish.approval.required");
        }
        ProductPublishPackage existing = packageIdMapper.selectOne(Wrappers.<ProductPublishPackage>lambdaQuery()
            .eq(ProductPublishPackage::getPackageHash, hash)
            .eq(ProductPublishPackage::getPackageStatus, "PUBLISHED")
            .orderByDesc(ProductPublishPackage::getPublishedTime)
            .last("limit 1"));
        if (existing != null) {
            List<ProductSyncOutboxVo> outboxes = createPublishOutboxes(existing, hash, snapshot);
            result.setResultStatus("PUBLISHED");
            result.setPackageInfo(packageIdMapper.selectVoById(existing.getPackageId()));
            result.setOutboxes(outboxes);
            if (!outboxes.isEmpty()) {
                result.setOutbox(outboxes.get(0));
            }
            return result;
        }
        ProductPublishPackage entity = new ProductPublishPackage();
        entity.setPackageCode("PKG-" + TimeUtils.utcNow().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
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
        packageIdMapper.insert(entity);
        List<ProductSyncOutboxVo> outboxes = createPublishOutboxes(entity, hash, snapshot);
        result.setResultStatus("PUBLISHED");
        result.setPackageInfo(packageIdMapper.selectVoById(entity.getPackageId()));
        result.setOutboxes(outboxes);
        if (!outboxes.isEmpty()) {
            result.setOutbox(outboxes.get(0));
        }
        return result;
    }

    private List<ProductSyncOutboxVo> createPublishOutboxes(ProductPublishPackage entity, String hash, String snapshot) {
        List<ProductSyncOutboxVo> outboxes = new ArrayList<>();
        for (String targetSystem : PUBLISH_TARGET_SYSTEMS) {
            ProductSyncOutbox existing = outboxIdMapper.selectOne(Wrappers.<ProductSyncOutbox>lambdaQuery()
                .eq(ProductSyncOutbox::getPackageId, entity.getPackageId())
                .eq(ProductSyncOutbox::getTargetSystem, targetSystem)
                .eq(ProductSyncOutbox::getEventType, "PRODUCT_PACKAGE_PUBLISHED")
                .eq(ProductSyncOutbox::getPayloadHash, hash)
                .last("limit 1"));
            if (existing != null) {
                outboxes.add(outboxIdMapper.selectVoById(existing.getOutboxId()));
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
            outboxIdMapper.insert(outbox);
            outboxes.add(outboxIdMapper.selectVoById(outbox.getOutboxId()));
        }
        return outboxes;
    }

    private void addCheck(PublishCheckSummaryVo summary, PublishCheckBo bo, String code, String nameCn, String nameEn, boolean pass,
                          String messageKey, String messageCn, String messageEn, boolean blocker) {
        String evidence = snapshotJson(bo);
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
        checkIdMapper.delete(Wrappers.<PublishCheckResult>lambdaQuery()
            .eq(PublishCheckResult::getTargetType, entity.getTargetType())
            .eq(entity.getTargetId() != null, PublishCheckResult::getTargetId, entity.getTargetId())
            .eq(StringUtils.isNotBlank(entity.getTargetCode()), PublishCheckResult::getTargetCode, entity.getTargetCode())
            .eq(PublishCheckResult::getCheckCode, code)
            .eq(PublishCheckResult::getEvidenceJson, evidence));
        ProductEntityDefaults.prepareInsert(entity);
        checkIdMapper.insert(entity);
        PublishCheckResultVo vo = checkIdMapper.selectVoById(entity.getCheckId());
        summary.getResults().add(vo);
        if ("BLOCKER".equals(entity.getCheckLevel())) {
            summary.setBlockerCount(summary.getBlockerCount() + 1);
        } else if ("WARNING".equals(entity.getCheckLevel())) {
            summary.setWarningCount(summary.getWarningCount() + 1);
        } else {
            summary.setPassCount(summary.getPassCount() + 1);
        }
    }

    private String snapshotJson(PublishCheckBo bo) {
        return "{\"productModelId\":" + value(bo.getProductModelId())
            + ",\"productModelCode\":\"" + text(bo.getProductModelCode())
            + "\",\"salesVariantId\":" + value(bo.getSalesVariantId())
            + ",\"salesVariantCode\":\"" + text(bo.getSalesVariantCode())
            + "\",\"templateVersionId\":" + value(bo.getTemplateVersionId())
            + ",\"templateVersionNo\":\"" + text(bo.getTemplateVersionNo())
            + "\",\"pricePlanVersionId\":" + value(bo.getPricePlanVersionId())
            + ",\"pricePlanCode\":\"" + text(bo.getPricePlanCode()) + "\"}";
    }

    private String value(Long value) {
        return value == null ? "null" : String.valueOf(value);
    }

    private String text(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
