package com.bocoo.product.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.product.domain.bo.ProductPriceMaterialBatchRuleBo;
import com.bocoo.product.domain.bo.ProductPriceMaterialRuleBo;
import com.bocoo.product.domain.bo.ProductPriceQuoteBo;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceMaterial;
import com.bocoo.product.domain.entity.ProductPriceMaterialRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.domain.vo.ProductPriceQuoteVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.domain.vo.ProductPriceValidationIssueVo;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import com.bocoo.product.mapper.ProductPriceMaterialMapper;
import com.bocoo.product.mapper.ProductPriceMaterialRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import com.bocoo.product.mapper.ProductSaleProductMapper;
import com.bocoo.product.service.ProductChangeLogService;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductPriceSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductPriceSettingServiceImpl extends ProductServiceSupport implements ProductPriceSettingService {
    private static final String VALIDATION_PASS = "PASS";
    private static final String VALIDATION_FAIL = "FAIL";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String PRICE_LOCK_NAME = "product-price-setting";

    private final ProductSaleProductMapper saleProductMapper;
    private final ProductPriceSettingMapper settingMapper;
    private final ProductPriceMaterialMapper materialMapper;
    private final ProductPriceMaterialRuleMapper ruleMapper;
    private final ProductFormulaVersionMapper versionMapper;
    private final ProductPriceSettingValidator validator;
    private final ProductPriceSnapshotReader snapshotReader;
    private final ProductPriceMaterialSyncService syncService;
    private final ProductPriceMaterialRuleWriter ruleWriter;
    private final ProductPriceMaterialVoAssembler voAssembler;
    private final ProductPricingEngine pricingEngine;
    private final ProductChangeLogService changeLogService;

    @Override
    @Lock4j(name = PRICE_LOCK_NAME, keys = {"#saleProductId"})
    @Transactional(rollbackFor = Exception.class)
    public ProductPriceSetupVo querySetup(Long saleProductId) {
        ProductSaleProduct saleProduct = requireSaleProduct(saleProductId);
        ProductPriceSetting setting = ensureSetting(saleProduct);
        ProductFormulaVersion version = requireVersion(saleProduct);
        List<ProductPriceMaterial> materials = queryMaterials(setting);
        List<ProductPriceMaterialRule> rules = queryRules(setting);
        ProductPriceSetupVo vo = new ProductPriceSetupVo();
        vo.setSaleProduct(saleProductMapper.selectVoById(saleProductId));
        vo.setSetting(settingMapper.selectVoById(setting.getPriceSettingId()));
        vo.setPriceMaterials(voAssembler.toMaterialVos(materials, rules));
        vo.setMaterialRules(ruleMapper.selectVoList(activeQuery(ProductPriceMaterialRule.class)
            .eq("price_setting_id", setting.getPriceSettingId()).orderByAsc("sort_order", "material_rule_id")));
        vo.setMaterialGroupCounts(snapshotReader.materialGroupCounts(version));
        vo.setFormulaMaterials(snapshotReader.formulaMaterials(version));
        vo.setFormulaOptions(snapshotReader.formulaOptions(version));
        vo.setFormulaOptionValues(snapshotReader.formulaOptionValues(version));
        vo.setFormulaOptionMaterials(snapshotReader.formulaOptionMaterials(version));
        vo.setIssues(validator.validate(version, materials, rules));
        return vo;
    }

    @Override
    @Lock4j(name = PRICE_LOCK_NAME, keys = {"#saleProductId"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateMaterialPrices(Long saleProductId, Boolean overwrite) {
        ProductSaleProduct saleProduct = requireEditableProduct(saleProductId);
        ProductPriceSetting setting = ensureSetting(saleProduct);
        List<ProductPriceMaterial> before = queryMaterials(setting);
        List<ProductPriceMaterial> generated = syncService.sync(setting, requireVersion(saleProduct), Boolean.TRUE.equals(overwrite));
        markNotReady(setting);
        recordChange(setting, "GENERATE_MATERIAL_PRICES", before, generated);
        return Boolean.TRUE;
    }

    @Override
    @Lock4j(name = PRICE_LOCK_NAME, keys = {"#saleProductId"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveMaterialRules(Long saleProductId, Long priceMaterialId, List<ProductPriceMaterialRuleBo> rules) {
        ProductSaleProduct saleProduct = requireEditableProduct(saleProductId);
        ProductPriceSetting setting = ensureSetting(saleProduct);
        ProductPriceMaterial material = requirePriceMaterial(setting, priceMaterialId);
        List<ProductPriceMaterialRule> before = rulesForMaterial(priceMaterialId);
        ruleWriter.replace(setting, requireVersion(saleProduct), material, rules);
        markNotReady(setting);
        recordChange(setting, "SAVE_MATERIAL_RULES", before, rules);
        return Boolean.TRUE;
    }

    @Override
    @Lock4j(name = PRICE_LOCK_NAME, keys = {"#saleProductId"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveMaterialRulesBatch(Long saleProductId, ProductPriceMaterialBatchRuleBo batch) {
        ProductSaleProduct saleProduct = requireEditableProduct(saleProductId);
        if (batch == null || batch.getPriceMaterialIds() == null || batch.getPriceMaterialIds().isEmpty()) {
            throw ServiceException.ofMessageKey("product.priceSetting.materialSelectionRequired");
        }
        ProductPriceSetting setting = ensureSetting(saleProduct);
        ProductFormulaVersion version = requireVersion(saleProduct);
        List<ProductPriceMaterial> materials = batch.getPriceMaterialIds().stream().distinct()
            .map(id -> requirePriceMaterial(setting, id)).toList();
        Set<String> groups = materials.stream()
            .map(row -> StringUtils.blankToDefault(row.getAttributeGroupCode(), "UNCLASSIFIED"))
            .collect(java.util.stream.Collectors.toSet());
        if (groups.size() > 1) {
            throw ServiceException.ofMessageKey("product.priceSetting.batchSameGroupRequired");
        }
        for (ProductPriceMaterial material : materials) {
            ruleWriter.replace(setting, version, material, batch.getRules());
        }
        markNotReady(setting);
        recordChange(setting, "BATCH_SAVE_MATERIAL_RULES", null, batch.getPriceMaterialIds());
        return Boolean.TRUE;
    }

    @Override
    @Lock4j(name = PRICE_LOCK_NAME, keys = {"#saleProductId"})
    @Transactional(rollbackFor = Exception.class)
    public List<ProductPriceValidationIssueVo> validatePrice(Long saleProductId) {
        ProductSaleProduct saleProduct = requireEditableProduct(saleProductId);
        ProductPriceSetting setting = ensureSetting(saleProduct);
        List<ProductPriceValidationIssueVo> issues = validator.validate(requireVersion(saleProduct), queryMaterials(setting), queryRules(setting));
        boolean passed = issues.isEmpty();
        settingMapper.update(null, new LambdaUpdateWrapper<ProductPriceSetting>()
            .eq(ProductPriceSetting::getPriceSettingId, setting.getPriceSettingId())
            .set(ProductPriceSetting::getValidationStatus, passed ? VALIDATION_PASS : VALIDATION_FAIL)
            .set(ProductPriceSetting::getValidationMessage, passed ? "product.priceSetting.validationPassed" : "product.priceSetting.validationFailed")
            .set(ProductPriceSetting::getValidationTime, TimeUtils.utcNow())
            .set(ProductPriceSetting::getStatus, passed ? ProductSaleProductServiceImpl.PRICE_READY : STATUS_DRAFT));
        saleProductMapper.update(null, new LambdaUpdateWrapper<ProductSaleProduct>()
            .eq(ProductSaleProduct::getSaleProductId, saleProductId)
            .set(ProductSaleProduct::getPriceStatus, passed ? ProductSaleProductServiceImpl.PRICE_READY : "WARNING"));
        recordChange(setting, "VALIDATE_PRICE", null, issues);
        return issues;
    }

    @Override
    @Lock4j(name = PRICE_LOCK_NAME, keys = {"#saleProductId"})
    public ProductPriceQuoteVo quote(Long saleProductId, ProductPriceQuoteBo quote) {
        ProductSaleProduct saleProduct = requireSaleProduct(saleProductId);
        if (!ProductSaleProductServiceImpl.PRICE_READY.equals(saleProduct.getPriceStatus())) {
            throw ServiceException.ofMessageKey("product.priceSetting.validationRequired");
        }
        ProductPriceSetting setting = requireSetting(saleProduct);
        return pricingEngine.quote(saleProduct, setting, requireVersion(saleProduct), quote);
    }

    private ProductSaleProduct requireEditableProduct(Long saleProductId) {
        ProductSaleProduct saleProduct = requireSaleProduct(saleProductId);
        if (STATUS_ENABLED.equals(saleProduct.getStatus())) {
            throw ServiceException.ofMessageKey("product.priceSetting.enabledEditDenied");
        }
        return saleProduct;
    }

    private ProductSaleProduct requireSaleProduct(Long saleProductId) {
        ProductSaleProduct saleProduct = saleProductId == null ? null : saleProductMapper.selectById(saleProductId);
        if (saleProduct == null || !"0".equals(StringUtils.blankToDefault(saleProduct.getDelFlag(), "0"))) {
            throw ServiceException.ofMessageKey("product.saleProduct.notFound");
        }
        return saleProduct;
    }

    private ProductFormulaVersion requireVersion(ProductSaleProduct product) {
        ProductFormulaVersion version = product.getFormulaVersionId() == null ? null : versionMapper.selectById(product.getFormulaVersionId());
        if (version == null) {
            throw ServiceException.ofMessageKey("product.saleProduct.formulaVersionRequired");
        }
        return version;
    }

    private ProductPriceSetting requireSetting(ProductSaleProduct product) {
        ProductPriceSetting setting = settingMapper.selectOne(activeQuery(ProductPriceSetting.class)
            .eq("sale_product_id", product.getSaleProductId()).eq("formula_version_id", product.getFormulaVersionId()).last("limit 1"));
        if (setting == null) {
            throw ServiceException.ofMessageKey("product.priceSetting.notFound");
        }
        return setting;
    }

    private ProductPriceSetting ensureSetting(ProductSaleProduct product) {
        ProductPriceSetting setting = settingMapper.selectOne(activeQuery(ProductPriceSetting.class)
            .eq("sale_product_id", product.getSaleProductId()).eq("formula_version_id", product.getFormulaVersionId()).last("limit 1"));
        if (setting == null) {
            setting = new ProductPriceSetting();
            setting.setTenantId(product.getTenantId());
            setting.setSaleProductId(product.getSaleProductId());
            setting.setCurrencyCode("USD");
            setting.setStatus(STATUS_DRAFT);
            setting.setValidationStatus(ProductSaleProductServiceImpl.PRICE_NOT_READY);
            ProductEntityDefaults.prepareInsert(setting);
        }
        setting.setSaleProductCode(product.getSaleProductCode());
        setting.setSaleProductName(product.getSaleProductName());
        setting.setFormulaId(product.getFormulaId());
        setting.setFormulaVersionId(product.getFormulaVersionId());
        setting.setFormulaVersionLabel(product.getFormulaVersionLabel());
        if (setting.getPriceSettingId() == null) {
            settingMapper.insert(setting);
        } else {
            settingMapper.updateById(setting);
        }
        return setting;
    }

    private List<ProductPriceMaterial> queryMaterials(ProductPriceSetting setting) {
        return materialMapper.selectList(activeQuery(ProductPriceMaterial.class)
            .eq("price_setting_id", setting.getPriceSettingId()).orderByAsc("sort_order", "price_material_id"));
    }

    private List<ProductPriceMaterialRule> queryRules(ProductPriceSetting setting) {
        return ruleMapper.selectList(activeQuery(ProductPriceMaterialRule.class)
            .eq("price_setting_id", setting.getPriceSettingId()).orderByAsc("sort_order", "material_rule_id"));
    }

    private List<ProductPriceMaterialRule> rulesForMaterial(Long priceMaterialId) {
        return ruleMapper.selectList(activeQuery(ProductPriceMaterialRule.class).eq("price_material_id", priceMaterialId));
    }

    private ProductPriceMaterial requirePriceMaterial(ProductPriceSetting setting, Long priceMaterialId) {
        ProductPriceMaterial material = priceMaterialId == null ? null : materialMapper.selectById(priceMaterialId);
        if (material == null || !setting.getPriceSettingId().equals(material.getPriceSettingId())) {
            throw ServiceException.ofMessageKey("product.priceSetting.materialPriceNotInFormulaVersion");
        }
        return material;
    }

    private void markNotReady(ProductPriceSetting setting) {
        settingMapper.update(null, new LambdaUpdateWrapper<ProductPriceSetting>()
            .eq(ProductPriceSetting::getPriceSettingId, setting.getPriceSettingId())
            .set(ProductPriceSetting::getValidationStatus, ProductSaleProductServiceImpl.PRICE_NOT_READY)
            .set(ProductPriceSetting::getValidationMessage, null).set(ProductPriceSetting::getValidationTime, null)
            .set(ProductPriceSetting::getStatus, STATUS_DRAFT));
        saleProductMapper.update(null, new LambdaUpdateWrapper<ProductSaleProduct>()
            .eq(ProductSaleProduct::getSaleProductId, setting.getSaleProductId())
            .set(ProductSaleProduct::getPriceStatus, ProductSaleProductServiceImpl.PRICE_NOT_READY));
    }

    private void recordChange(ProductPriceSetting setting, String action, Object before, Object after) {
        changeLogService.record("PRODUCT_PRICING", "PRICE_SETTING", setting.getPriceSettingId(),
            setting.getSaleProductCode(), action, changeLogPayload(before), changeLogPayload(after), null);
    }

    private Object changeLogPayload(Object value) {
        return value instanceof List<?> list ? Map.of("rowCount", list.size()) : value;
    }
}
