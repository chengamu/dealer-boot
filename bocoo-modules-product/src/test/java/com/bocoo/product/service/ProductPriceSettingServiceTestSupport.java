package com.bocoo.product.service;

import com.bocoo.product.domain.bo.ProductPriceFabricRuleBo;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceFabric;
import com.bocoo.product.domain.entity.ProductPriceFabricRule;
import com.bocoo.product.domain.entity.ProductPriceFeeRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.domain.entity.ProductShippingTemplate;
import com.bocoo.product.domain.entity.ProductShippingTemplateRule;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import com.bocoo.product.mapper.ProductPriceFabricMapper;
import com.bocoo.product.mapper.ProductPriceFabricRuleMapper;
import com.bocoo.product.mapper.ProductPriceFeeRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import com.bocoo.product.mapper.ProductSaleProductMapper;
import com.bocoo.product.mapper.ProductShippingTemplateMapper;
import com.bocoo.product.mapper.ProductShippingTemplateRuleMapper;
import com.bocoo.product.service.impl.ProductPriceFabricCandidateFactory;
import com.bocoo.product.service.impl.ProductPriceFabricRuleGuard;
import com.bocoo.product.service.impl.ProductPriceFabricSyncService;
import com.bocoo.product.service.impl.ProductPriceFabricVoAssembler;
import com.bocoo.product.service.impl.ProductPriceSettingServiceImpl;
import com.bocoo.product.service.impl.ProductPriceSettingValidator;
import com.bocoo.product.service.impl.ProductPriceShippingRuleFactory;
import com.bocoo.product.service.impl.ProductPriceShippingTemplateImporter;
import com.bocoo.product.service.impl.ProductPriceSnapshotReader;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;

abstract class ProductPriceSettingServiceTestSupport {

    @Mock
    protected ProductSaleProductMapper saleProductMapper;
    @Mock
    protected ProductPriceSettingMapper settingMapper;
    @Mock
    protected ProductPriceFabricMapper fabricMapper;
    @Mock
    protected ProductPriceFabricRuleMapper fabricRuleMapper;
    @Mock
    protected ProductPriceFeeRuleMapper feeRuleMapper;
    @Mock
    protected ProductFormulaVersionMapper versionMapper;
    @Mock
    protected ProductShippingTemplateMapper shippingTemplateMapper;
    @Mock
    protected ProductShippingTemplateRuleMapper shippingTemplateRuleMapper;
    @Mock
    protected ProductChangeLogService changeLogService;

    protected ProductPriceSettingServiceImpl priceSettingService;

    @BeforeEach
    void setUpPriceSettingService() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        ProductPriceSnapshotReader snapshotReader = new ProductPriceSnapshotReader();
        ProductPriceFabricSyncService fabricSyncService =
            new ProductPriceFabricSyncService(fabricMapper, fabricRuleMapper, settingMapper, snapshotReader);
        ProductPriceShippingRuleFactory shippingRuleFactory = new ProductPriceShippingRuleFactory();
        ProductPriceShippingTemplateImporter shippingTemplateImporter =
            new ProductPriceShippingTemplateImporter(shippingTemplateMapper, shippingTemplateRuleMapper, feeRuleMapper, shippingRuleFactory);
        priceSettingService = new ProductPriceSettingServiceImpl(
            saleProductMapper,
            settingMapper,
            fabricMapper,
            fabricRuleMapper,
            feeRuleMapper,
            versionMapper,
            new ProductPriceSettingValidator(snapshotReader),
            snapshotReader,
            new ProductPriceFabricCandidateFactory(snapshotReader),
            fabricSyncService,
            new ProductPriceFabricRuleGuard(),
            shippingRuleFactory,
            new ProductPriceFabricVoAssembler(),
            shippingTemplateImporter,
            changeLogService
        );
    }

    protected ProductSaleProduct saleProduct(String status) {
        ProductSaleProduct product = new ProductSaleProduct();
        product.setTenantId(1L);
        product.setSaleProductId(9001L);
        product.setSaleProductCode("SP-001");
        product.setSaleProductName("斑马帘");
        product.setFormulaId(3001L);
        product.setFormulaVersionId(7001L);
        product.setFormulaVersionLabel("V1");
        product.setStatus(status);
        product.setDelFlag("0");
        return product;
    }

    protected ProductPriceSetting setting() {
        ProductPriceSetting setting = new ProductPriceSetting();
        setting.setTenantId(1L);
        setting.setPriceSettingId(9101L);
        setting.setSaleProductId(9001L);
        setting.setSaleProductCode("SP-001");
        setting.setFormulaId(3001L);
        setting.setFormulaVersionId(7001L);
        setting.setFormulaVersionLabel("V1");
        setting.setCurrencyCode("USD");
        setting.setDelFlag("0");
        return setting;
    }

    protected ProductFormulaVersion effectiveVersion() {
        ProductFormulaVersion version = new ProductFormulaVersion();
        version.setVersionId(7001L);
        version.setVersionStatus("EFFECTIVE");
        version.setSetupSnapshotJson("""
            {"materials":[{"materialId":4001,"attributeGroupCode":"FABRIC","materialCode":"MAT-FABRIC","materialNameCn":"斑马帘面料","unitCode":"m2","specModelText":"宽幅 290","createById":1,"createTime":"2026-07-08T14:24:18","updateTime":"2026-07-08T14:24:18"}],"priceSnapshot":[{"materialCode":"MAT-FABRIC","salesPrice":12.30,"unitPrice":8.20}]}
            """);
        return version;
    }

    protected ProductPriceFabricRuleBo fabricRuleBo() {
        ProductPriceFabricRuleBo bo = new ProductPriceFabricRuleBo();
        bo.setPriceFabricId(9201L);
        bo.setConditionType("DEFAULT");
        bo.setConditionExpression("DEFAULT");
        bo.setConditionText("默认规则");
        bo.setConditionKey("DEFAULT");
        bo.setPriceMode("FORMULA");
        bo.setUnitPrice(new BigDecimal("12.50"));
        bo.setPriceFormula("unitPrice * MAX(drop * width / 144, 1)");
        bo.setDefaultRuleFlag(Boolean.TRUE);
        bo.setStatus("ENABLED");
        return bo;
    }

    protected ProductPriceFabric priceFabric() {
        return priceFabric(9201L);
    }

    protected ProductPriceFabric priceFabric(Long id) {
        ProductPriceFabric fabric = new ProductPriceFabric();
        fabric.setPriceFabricId(id);
        fabric.setPriceSettingId(9101L);
        fabric.setMaterialCode("MAT-FABRIC");
        fabric.setMaterialNameCn("斑马帘面料");
        fabric.setUnitCode("m2");
        fabric.setStatus("ENABLED");
        return fabric;
    }

    protected ProductPriceFabricRule fabricRule() {
        ProductPriceFabricRule rule = new ProductPriceFabricRule();
        rule.setPriceFabricId(9201L);
        rule.setConditionType("DEFAULT");
        rule.setConditionExpression("DEFAULT");
        rule.setConditionText("默认规则");
        rule.setConditionKey("DEFAULT");
        rule.setPriceMode("FORMULA");
        rule.setUnitPrice(new BigDecimal("12.50"));
        rule.setPriceFormula("unitPrice * MAX(drop * width / 144, 1)");
        rule.setDefaultRuleFlag(Boolean.TRUE);
        rule.setStatus("ENABLED");
        return rule;
    }

    protected List<ProductPriceFeeRule> shippingRules() {
        ProductPriceFeeRule manual = feeRule("MANUAL", "不带电邮费", "18 + MAX(width - 60, 0) * 0.35");
        ProductPriceFeeRule motorized = feeRule("MOTORIZED", "带电邮费", "25 + MAX(width - 60, 0) * 0.45");
        return List.of(manual, motorized);
    }

    protected ProductShippingTemplate shippingTemplate() {
        ProductShippingTemplate template = new ProductShippingTemplate();
        template.setShippingTemplateId(9301L);
        template.setTemplateCode("SHIP-US");
        template.setTemplateName("美国邮费模板");
        template.setCurrencyCode("USD");
        template.setStatus("ENABLED");
        template.setDelFlag("0");
        return template;
    }

    protected ProductShippingTemplateRule shippingTemplateRule(String code, String formula) {
        ProductShippingTemplateRule rule = new ProductShippingTemplateRule();
        rule.setShippingTemplateRuleId("MANUAL".equals(code) ? 9311L : 9312L);
        rule.setFeeCode(code);
        rule.setFeeName("MANUAL".equals(code) ? "不带电邮费" : "带电邮费");
        rule.setMinAreaSqft(BigDecimal.ZERO);
        rule.setFormulaText(formula);
        return rule;
    }

    private ProductPriceFeeRule feeRule(String code, String name, String formula) {
        ProductPriceFeeRule rule = new ProductPriceFeeRule();
        rule.setFeeCode(code);
        rule.setFeeName(name);
        rule.setFeeMode("FORMULA");
        rule.setFormulaText(formula);
        rule.setStatus("ENABLED");
        return rule;
    }
}
