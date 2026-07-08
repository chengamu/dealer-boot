package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.product.domain.bo.ProductPriceFabricRuleBo;
import com.bocoo.product.domain.bo.ProductPriceFeeRuleBo;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceFabricRule;
import com.bocoo.product.domain.entity.ProductPriceFeeRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.domain.vo.ProductPriceFeeRuleVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.domain.vo.ProductPriceValidationIssueVo;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import com.bocoo.product.mapper.ProductPriceFabricRuleMapper;
import com.bocoo.product.mapper.ProductPriceFeeRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import com.bocoo.product.mapper.ProductSaleProductMapper;
import com.bocoo.product.service.ProductChangeLogService;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductPriceSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPriceSettingServiceImpl extends ProductServiceSupport implements ProductPriceSettingService {

    private static final String VALIDATION_PASS = "PASS";
    private static final String VALIDATION_FAIL = "FAIL";
    private static final String STATUS_DRAFT = "DRAFT";

    private final ProductSaleProductMapper saleProductMapper;
    private final ProductPriceSettingMapper settingMapper;
    private final ProductPriceFabricRuleMapper fabricRuleMapper;
    private final ProductPriceFeeRuleMapper feeRuleMapper;
    private final ProductFormulaVersionMapper versionMapper;
    private final ProductPriceSettingValidator validator;
    private final ProductPriceSnapshotReader snapshotReader;
    private final ProductPriceFabricCandidateFactory fabricCandidateFactory;
    private final ProductPriceFabricRuleGenerator fabricRuleGenerator;
    private final ProductPriceFabricRuleGuard fabricRuleGuard;
    private final ProductPriceShippingRuleFactory shippingRuleFactory;
    private final ProductChangeLogService changeLogService;

    @Override
    public ProductPriceSetupVo querySetup(Long saleProductId) {
        ProductSaleProduct saleProduct = requireSaleProduct(saleProductId);
        ProductPriceSetting setting = ensureSetting(saleProduct);
        ProductFormulaVersion version = versionMapper.selectById(saleProduct.getFormulaVersionId());
        ProductPriceSetupVo vo = new ProductPriceSetupVo();
        vo.setSaleProduct(saleProductMapper.selectVoById(saleProductId));
        vo.setSetting(settingMapper.selectVoById(setting.getPriceSettingId()));
        vo.setFabricRules(fabricRuleMapper.selectVoList(activeQuery(ProductPriceFabricRule.class)
            .eq("price_setting_id", setting.getPriceSettingId()).orderByAsc("sort_order", "fabric_rule_id")));
        List<ProductPriceFeeRuleVo> feeRules = feeRuleMapper.selectVoList(activeQuery(ProductPriceFeeRule.class)
            .eq("price_setting_id", setting.getPriceSettingId()).orderByAsc("sort_order", "fee_rule_id"));
        vo.setFeeRules(feeRules.isEmpty() ? shippingRuleFactory.defaultVos(setting) : feeRules);
        vo.setFabricCandidates(fabricCandidateFactory.candidates(version));
        vo.setFabricPriceColumns(snapshotReader.optionCombinations(version));
        vo.setMaterialGroupCounts(snapshotReader.materialGroupCounts(version));
        vo.setIssues(validator.validate(version,
            fabricRuleMapper.selectList(activeQuery(ProductPriceFabricRule.class).eq("price_setting_id", setting.getPriceSettingId())),
            feeRuleMapper.selectList(activeQuery(ProductPriceFeeRule.class).eq("price_setting_id", setting.getPriceSettingId()))));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateFabricPrices(Long saleProductId, Boolean overwrite) {
        ProductSaleProduct saleProduct = requireSaleProduct(saleProductId);
        assertPriceEditable(saleProduct);
        ProductPriceSetting setting = ensureSetting(saleProduct);
        ProductFormulaVersion version = versionMapper.selectById(saleProduct.getFormulaVersionId());
        List<ProductPriceFabricRule> before = fabricRuleMapper.selectList(activeQuery(ProductPriceFabricRule.class)
            .eq("price_setting_id", setting.getPriceSettingId()));
        List<ProductPriceFabricRule> previous = previousFabricRules(setting);
        List<ProductPriceFabricRule> generated = fabricRuleGenerator.generate(setting, snapshotReader.fabricMaterials(version),
            snapshotReader.optionCombinations(version), before, previous, Boolean.TRUE.equals(overwrite));
        fabricRuleMapper.delete(activeQuery(ProductPriceFabricRule.class).eq("price_setting_id", setting.getPriceSettingId()));
        for (ProductPriceFabricRule rule : generated) {
            ProductEntityDefaults.prepareInsert(rule);
            fabricRuleMapper.insert(rule);
        }
        markNotReady(setting);
        recordChange(setting, "GENERATE_FABRIC_PRICES", before, generated);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveFabricRules(Long saleProductId, List<ProductPriceFabricRuleBo> rules) {
        ProductSaleProduct saleProduct = requireSaleProduct(saleProductId);
        assertPriceEditable(saleProduct);
        ProductPriceSetting setting = ensureSetting(saleProduct);
        ProductFormulaVersion version = versionMapper.selectById(saleProduct.getFormulaVersionId());
        fabricRuleGuard.assertSavable(version, rules);
        List<ProductPriceFabricRule> before = fabricRuleMapper.selectList(activeQuery(ProductPriceFabricRule.class)
            .eq("price_setting_id", setting.getPriceSettingId()));
        fabricRuleMapper.delete(activeQuery(ProductPriceFabricRule.class).eq("price_setting_id", setting.getPriceSettingId()));
        int index = 0;
        for (ProductPriceFabricRuleBo rule : rules == null ? List.<ProductPriceFabricRuleBo>of() : rules) {
            ProductPriceFabricRule entity = toFabricRule(rule, setting, index++);
            ProductEntityDefaults.prepareInsert(entity);
            fabricRuleMapper.insert(entity);
        }
        markNotReady(setting);
        recordChange(setting, "SAVE_FABRIC_RULES", before, rules);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveFeeRules(Long saleProductId, List<ProductPriceFeeRuleBo> rules) {
        ProductSaleProduct saleProduct = requireSaleProduct(saleProductId);
        assertPriceEditable(saleProduct);
        ProductPriceSetting setting = ensureSetting(saleProduct);
        List<ProductPriceFeeRule> before = feeRuleMapper.selectList(activeQuery(ProductPriceFeeRule.class)
            .eq("price_setting_id", setting.getPriceSettingId()));
        feeRuleMapper.delete(activeQuery(ProductPriceFeeRule.class).eq("price_setting_id", setting.getPriceSettingId()));
        int index = 0;
        for (ProductPriceFeeRuleBo rule : rules == null ? List.<ProductPriceFeeRuleBo>of() : rules) {
            ProductPriceFeeRule entity = shippingRuleFactory.fromBo(rule, setting, index++);
            ProductEntityDefaults.prepareInsert(entity);
            feeRuleMapper.insert(entity);
        }
        markNotReady(setting);
        recordChange(setting, "SAVE_FEE_RULES", before, rules);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ProductPriceValidationIssueVo> validatePrice(Long saleProductId) {
        ProductSaleProduct saleProduct = requireSaleProduct(saleProductId);
        ProductPriceSetting setting = ensureSetting(saleProduct);
        ProductFormulaVersion version = versionMapper.selectById(saleProduct.getFormulaVersionId());
        List<ProductPriceValidationIssueVo> issues = validator.validate(version,
            fabricRuleMapper.selectList(activeQuery(ProductPriceFabricRule.class).eq("price_setting_id", setting.getPriceSettingId())),
            feeRuleMapper.selectList(activeQuery(ProductPriceFeeRule.class).eq("price_setting_id", setting.getPriceSettingId())));
        String validationStatus = issues.isEmpty() ? VALIDATION_PASS : VALIDATION_FAIL;
        String priceStatus = issues.isEmpty() ? ProductSaleProductServiceImpl.PRICE_READY : "WARNING";
        settingMapper.update(null, new LambdaUpdateWrapper<ProductPriceSetting>()
            .eq(ProductPriceSetting::getPriceSettingId, setting.getPriceSettingId())
            .set(ProductPriceSetting::getValidationStatus, validationStatus)
            .set(ProductPriceSetting::getValidationMessage, issues.isEmpty() ? "product.priceSetting.validationPassed" : "product.priceSetting.validationFailed")
            .set(ProductPriceSetting::getValidationTime, TimeUtils.utcNow())
            .set(ProductPriceSetting::getStatus, issues.isEmpty() ? ProductSaleProductServiceImpl.PRICE_READY : STATUS_DRAFT));
        saleProductMapper.update(null, new LambdaUpdateWrapper<ProductSaleProduct>()
            .eq(ProductSaleProduct::getSaleProductId, saleProductId)
            .set(ProductSaleProduct::getPriceStatus, priceStatus));
        recordChange(setting, "VALIDATE_PRICE", null, issues);
        return issues;
    }

    private ProductSaleProduct requireSaleProduct(Long saleProductId) {
        ProductSaleProduct saleProduct = saleProductId == null ? null : saleProductMapper.selectById(saleProductId);
        if (saleProduct == null || !"0".equals(StringUtils.blankToDefault(saleProduct.getDelFlag(), "0"))) {
            throw ServiceException.ofMessageKey("product.saleProduct.notFound");
        }
        return saleProduct;
    }

    private void assertPriceEditable(ProductSaleProduct saleProduct) {
        if (STATUS_ENABLED.equals(saleProduct.getStatus())) {
            throw ServiceException.ofMessageKey("product.priceSetting.enabledEditDenied");
        }
    }

    private ProductPriceSetting ensureSetting(ProductSaleProduct saleProduct) {
        ProductPriceSetting setting = settingMapper.selectOne(activeQuery(ProductPriceSetting.class)
            .eq("sale_product_id", saleProduct.getSaleProductId())
            .eq("formula_version_id", saleProduct.getFormulaVersionId())
            .last("limit 1"));
        if (setting == null) {
            setting = new ProductPriceSetting();
            setting.setTenantId(saleProduct.getTenantId());
            setting.setSaleProductId(saleProduct.getSaleProductId());
            setting.setCurrencyCode("USD");
            setting.setStatus(STATUS_DRAFT);
            setting.setValidationStatus(ProductSaleProductServiceImpl.PRICE_NOT_READY);
            ProductEntityDefaults.prepareInsert(setting);
        }
        setting.setSaleProductCode(saleProduct.getSaleProductCode());
        setting.setSaleProductName(saleProduct.getSaleProductName());
        setting.setFormulaId(saleProduct.getFormulaId());
        setting.setFormulaVersionId(saleProduct.getFormulaVersionId());
        setting.setFormulaVersionLabel(saleProduct.getFormulaVersionLabel());
        if (setting.getPriceSettingId() == null) {
            settingMapper.insert(setting);
        } else {
            settingMapper.updateById(setting);
        }
        return setting;
    }

    private List<ProductPriceFabricRule> previousFabricRules(ProductPriceSetting setting) {
        ProductPriceSetting previous = settingMapper.selectOne(activeQuery(ProductPriceSetting.class)
            .eq("sale_product_id", setting.getSaleProductId())
            .ne("formula_version_id", setting.getFormulaVersionId())
            .orderByDesc("update_time")
            .last("limit 1"));
        if (previous == null) {
            return List.of();
        }
        return fabricRuleMapper.selectList(activeQuery(ProductPriceFabricRule.class)
            .eq("price_setting_id", previous.getPriceSettingId()));
    }

    private void markNotReady(ProductPriceSetting setting) {
        settingMapper.update(null, new LambdaUpdateWrapper<ProductPriceSetting>()
            .eq(ProductPriceSetting::getPriceSettingId, setting.getPriceSettingId())
            .set(ProductPriceSetting::getValidationStatus, ProductSaleProductServiceImpl.PRICE_NOT_READY)
            .set(ProductPriceSetting::getValidationMessage, null)
            .set(ProductPriceSetting::getValidationTime, null)
            .set(ProductPriceSetting::getStatus, STATUS_DRAFT));
        saleProductMapper.update(null, new LambdaUpdateWrapper<ProductSaleProduct>()
            .eq(ProductSaleProduct::getSaleProductId, setting.getSaleProductId())
            .set(ProductSaleProduct::getPriceStatus, ProductSaleProductServiceImpl.PRICE_NOT_READY));
    }

    private ProductPriceFabricRule toFabricRule(ProductPriceFabricRuleBo bo, ProductPriceSetting setting, int index) {
        ProductPriceFabricRule entity = new ProductPriceFabricRule();
        entity.setTenantId(setting.getTenantId());
        entity.setPriceSettingId(setting.getPriceSettingId());
        entity.setSaleProductId(setting.getSaleProductId());
        entity.setFormulaVersionId(setting.getFormulaVersionId());
        entity.setMaterialId(bo.getMaterialId());
        entity.setMaterialCode(bo.getMaterialCode());
        entity.setMaterialNameCn(bo.getMaterialNameCn());
        entity.setUnitCode(bo.getUnitCode());
        entity.setOptionCombinationKey(StringUtils.blankToDefault(bo.getOptionCombinationKey(), "DEFAULT"));
        entity.setOptionCombinationName(StringUtils.blankToDefault(bo.getOptionCombinationName(), "默认"));
        entity.setPriceMode("FORMULA");
        entity.setBasePrice(bo.getBasePrice());
        entity.setAreaFormula(bo.getAreaFormula());
        entity.setMinBillArea(bo.getMinBillArea());
        entity.setLossRate(bo.getLossRate());
        entity.setStatus(STATUS_ENABLED);
        entity.setSortOrder(bo.getSortOrder() == null ? index : bo.getSortOrder());
        entity.setRemark(bo.getRemark());
        return entity;
    }

    private void recordChange(ProductPriceSetting setting, String action, Object before, Object after) {
        changeLogService.record("PRODUCT_PRICING", "PRICE_SETTING", setting.getPriceSettingId(),
            setting.getSaleProductCode(), action, before, after, null);
    }
}
