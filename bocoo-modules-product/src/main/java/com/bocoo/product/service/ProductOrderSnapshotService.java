package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.json.utils.JsonUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.OrderSnapshotBuildBo;
import com.bocoo.product.domain.entity.ProductPublishPackage;
import com.bocoo.product.domain.entity.ProductSnapshotInstance;
import com.bocoo.product.domain.vo.OrderProductSnapshotVo;
import com.bocoo.product.domain.vo.ProductSnapshotInstanceVo;
import com.bocoo.product.mapper.ProductPublishPackageMapper;
import com.bocoo.product.mapper.ProductSnapshotInstanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 产品能力快照构建服务。
 *
 * 快照实例统一归产品能力中心保存，订单、ERP、MES 等来源系统只保留快照引用。
 */
@Service
@RequiredArgsConstructor
public class ProductOrderSnapshotService {

    private static final String DEFAULT_SOURCE_SYSTEM = "ORDER";
    private static final String DEFAULT_SOURCE_BIZ_TYPE = "ORDER_LINE";
    private static final String SNAPSHOT_STATUS_BUILT = "BUILT";

    private final ProductPublishPackageMapper packageIdMapper;
    private final ProductSnapshotInstanceMapper snapshotInstanceMapper;

    public OrderProductSnapshotVo buildSnapshot(OrderSnapshotBuildBo bo) {
        OrderProductSnapshotVo result = new OrderProductSnapshotVo();
        if (bo == null || (bo.getPackageId() == null && StringUtils.isBlank(bo.getPackageCode()))) {
            result.setResultStatus("BLOCKER");
            result.getBlockers().add("product.orderSnapshot.package.required");
            return result;
        }
        ProductPublishPackage packageInfo = bo.getPackageId() != null
            ? packageIdMapper.selectById(bo.getPackageId())
            : packageIdMapper.selectOne(Wrappers.lambdaQuery(ProductPublishPackage.class)
                .eq(ProductPublishPackage::getPackageCode, bo.getPackageCode())
                .last("limit 1"));
        if (packageInfo == null) {
            result.setResultStatus("BLOCKER");
            result.getBlockers().add("product.orderSnapshot.package.notFound");
            return result;
        }
        result.setSourceSystem(normalizeSourceSystem(bo));
        result.setSourceBizType(normalizeSourceBizType(bo));
        result.setSourceBizNo(normalizeSourceBizNo(bo));
        result.setSourceBizLineNo(bo.getSourceBizLineNo());
        result.setCustomerCode(bo.getCustomerCode());
        result.setPackageId(packageInfo.getPackageId());
        result.setPackageCode(packageInfo.getPackageCode());
        result.setPackageHash(packageInfo.getPackageHash());
        result.setProductModelCode(packageInfo.getProductModelCode());
        result.setSalesVariantCode(packageInfo.getSalesVariantCode());
        result.setTemplateVersionId(packageInfo.getTemplateVersionId());
        result.setTemplateVersionNo(packageInfo.getTemplateVersionNo());
        result.setPricePlanVersionId(packageInfo.getPricePlanVersionId());
        result.setPricePlanCode(packageInfo.getPricePlanCode());
        result.setSelectedOptions(bo.getSelectedOptions());
        result.setInputValues(bo.getInputValues());
        result.setBuiltTime(TimeUtils.utcNow());
        String snapshot = snapshotJson(bo, packageInfo);
        result.setSnapshotJson(snapshot);
        result.setSnapshotHash(sha256(snapshot));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderProductSnapshotVo buildAndSaveSnapshot(OrderSnapshotBuildBo bo) {
        OrderProductSnapshotVo snapshot = buildSnapshot(bo);
        if (!SNAPSHOT_STATUS_BUILT.equals(snapshot.getResultStatus())) {
            throw ServiceException.ofMessageKey(snapshot.getBlockers().isEmpty()
                ? "product.orderSnapshot.build.blocked"
                : snapshot.getBlockers().get(0));
        }
        ProductSnapshotInstance existed = findExistingSnapshot(snapshot);
        if (existed != null) {
            snapshot.setSnapshotId(existed.getSnapshotId());
            return snapshot;
        }
        ProductSnapshotInstance entity = new ProductSnapshotInstance();
        entity.setSourceSystem(snapshot.getSourceSystem());
        entity.setSourceBizType(snapshot.getSourceBizType());
        entity.setSourceBizNo(snapshot.getSourceBizNo());
        entity.setSourceBizLineNo(snapshot.getSourceBizLineNo());
        entity.setCustomerCode(snapshot.getCustomerCode());
        entity.setPackageId(snapshot.getPackageId());
        entity.setPackageCode(snapshot.getPackageCode());
        entity.setPackageHash(snapshot.getPackageHash());
        entity.setProductModelCode(snapshot.getProductModelCode());
        entity.setSalesVariantCode(snapshot.getSalesVariantCode());
        entity.setTemplateVersionId(snapshot.getTemplateVersionId());
        entity.setTemplateVersionNo(snapshot.getTemplateVersionNo());
        entity.setPricePlanVersionId(snapshot.getPricePlanVersionId());
        entity.setPricePlanCode(snapshot.getPricePlanCode());
        entity.setSelectedOptionsJson(JsonUtils.toJsonString(snapshot.getSelectedOptions()));
        entity.setInputValuesJson(JsonUtils.toJsonString(snapshot.getInputValues()));
        entity.setSnapshotJson(snapshot.getSnapshotJson());
        entity.setSnapshotHash(snapshot.getSnapshotHash());
        entity.setSnapshotStatus(SNAPSHOT_STATUS_BUILT);
        entity.setBuiltTime(snapshot.getBuiltTime());
        ProductEntityDefaults.prepareInsert(entity);
        snapshotInstanceMapper.insert(entity);
        snapshot.setSnapshotId(entity.getSnapshotId());
        return snapshot;
    }

    public TableDataInfo<ProductSnapshotInstanceVo> querySnapshotInstancePage(ProductSnapshotInstance query, PageQuery pageQuery) {
        Page<ProductSnapshotInstanceVo> result = snapshotInstanceMapper.selectVoPage(pageQuery.build(), buildSnapshotInstanceWrapper(query));
        return TableDataInfo.build(result);
    }

    public ProductSnapshotInstanceVo getSnapshotInstanceById(Long snapshotId) {
        return snapshotInstanceMapper.selectVoById(snapshotId);
    }

    private String snapshotJson(OrderSnapshotBuildBo bo, ProductPublishPackage packageInfo) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("sourceSystem", normalizeSourceSystem(bo));
        snapshot.put("sourceBizType", normalizeSourceBizType(bo));
        snapshot.put("sourceBizNo", normalizeSourceBizNo(bo));
        snapshot.put("sourceBizLineNo", bo.getSourceBizLineNo());
        snapshot.put("orderNo", bo.getOrderNo());
        snapshot.put("customerCode", bo.getCustomerCode());
        snapshot.put("packageId", packageInfo.getPackageId());
        snapshot.put("packageCode", packageInfo.getPackageCode());
        snapshot.put("packageHash", packageInfo.getPackageHash());
        snapshot.put("productModelCode", packageInfo.getProductModelCode());
        snapshot.put("salesVariantCode", packageInfo.getSalesVariantCode());
        snapshot.put("templateVersionId", packageInfo.getTemplateVersionId());
        snapshot.put("templateVersionNo", packageInfo.getTemplateVersionNo());
        snapshot.put("pricePlanVersionId", packageInfo.getPricePlanVersionId());
        snapshot.put("pricePlanCode", packageInfo.getPricePlanCode());
        snapshot.put("selectedOptions", bo.getSelectedOptions());
        snapshot.put("inputValues", bo.getInputValues());
        return JsonUtils.toJsonString(snapshot);
    }

    private ProductSnapshotInstance findExistingSnapshot(OrderProductSnapshotVo snapshot) {
        if (StringUtils.isBlank(snapshot.getSourceBizNo()) || StringUtils.isBlank(snapshot.getSnapshotHash())) {
            return null;
        }
        return snapshotInstanceMapper.selectOne(Wrappers.lambdaQuery(ProductSnapshotInstance.class)
            .eq(ProductSnapshotInstance::getSourceSystem, snapshot.getSourceSystem())
            .eq(ProductSnapshotInstance::getSourceBizType, snapshot.getSourceBizType())
            .eq(ProductSnapshotInstance::getSourceBizNo, snapshot.getSourceBizNo())
            .eq(StringUtils.isNotBlank(snapshot.getSourceBizLineNo()), ProductSnapshotInstance::getSourceBizLineNo, snapshot.getSourceBizLineNo())
            .eq(ProductSnapshotInstance::getSnapshotHash, snapshot.getSnapshotHash())
            .last("limit 1"), false);
    }

    private LambdaQueryWrapper<ProductSnapshotInstance> buildSnapshotInstanceWrapper(ProductSnapshotInstance query) {
        if (query == null) {
            query = new ProductSnapshotInstance();
        }
        LambdaQueryWrapper<ProductSnapshotInstance> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(query.getSourceSystem()), ProductSnapshotInstance::getSourceSystem, query.getSourceSystem());
        lqw.like(StringUtils.isNotBlank(query.getSourceBizType()), ProductSnapshotInstance::getSourceBizType, query.getSourceBizType());
        lqw.like(StringUtils.isNotBlank(query.getSourceBizNo()), ProductSnapshotInstance::getSourceBizNo, query.getSourceBizNo());
        lqw.like(StringUtils.isNotBlank(query.getSourceBizLineNo()), ProductSnapshotInstance::getSourceBizLineNo, query.getSourceBizLineNo());
        lqw.like(StringUtils.isNotBlank(query.getCustomerCode()), ProductSnapshotInstance::getCustomerCode, query.getCustomerCode());
        lqw.like(StringUtils.isNotBlank(query.getPackageCode()), ProductSnapshotInstance::getPackageCode, query.getPackageCode());
        lqw.like(StringUtils.isNotBlank(query.getSnapshotHash()), ProductSnapshotInstance::getSnapshotHash, query.getSnapshotHash());
        lqw.like(StringUtils.isNotBlank(query.getSnapshotStatus()), ProductSnapshotInstance::getSnapshotStatus, query.getSnapshotStatus());
        lqw.orderByDesc(ProductSnapshotInstance::getBuiltTime);
        return lqw;
    }

    private String normalizeSourceSystem(OrderSnapshotBuildBo bo) {
        return StringUtils.blankToDefault(bo.getSourceSystem(), DEFAULT_SOURCE_SYSTEM);
    }

    private String normalizeSourceBizType(OrderSnapshotBuildBo bo) {
        return StringUtils.blankToDefault(bo.getSourceBizType(), DEFAULT_SOURCE_BIZ_TYPE);
    }

    private String normalizeSourceBizNo(OrderSnapshotBuildBo bo) {
        return StringUtils.blankToDefault(bo.getSourceBizNo(), bo.getOrderNo());
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
