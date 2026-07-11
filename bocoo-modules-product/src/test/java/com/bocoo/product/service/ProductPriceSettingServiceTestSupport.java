package com.bocoo.product.service;

import com.bocoo.product.domain.bo.ProductPriceMaterialRuleBo;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceMaterial;
import com.bocoo.product.domain.entity.ProductPriceMaterialRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import com.bocoo.product.mapper.ProductPriceMaterialMapper;
import com.bocoo.product.mapper.ProductPriceMaterialRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import com.bocoo.product.mapper.ProductSaleProductMapper;
import com.bocoo.product.service.impl.ProductPriceMaterialRuleGuard;
import com.bocoo.product.service.impl.ProductPriceMaterialRuleWriter;
import com.bocoo.product.service.impl.ProductPriceMaterialSyncService;
import com.bocoo.product.service.impl.ProductPriceMaterialVoAssembler;
import com.bocoo.product.service.impl.ProductPriceConditionSnapshotFactory;
import com.bocoo.product.service.impl.ProductPricingEngine;
import com.bocoo.product.service.impl.ProductPriceSettingServiceImpl;
import com.bocoo.product.service.impl.ProductPriceSettingValidator;
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
    protected ProductPriceMaterialMapper materialMapper;
    @Mock
    protected ProductPriceMaterialRuleMapper materialRuleMapper;
    @Mock
    protected ProductFormulaVersionMapper versionMapper;
    @Mock
    protected ProductChangeLogService changeLogService;

    protected ProductPriceSettingServiceImpl priceSettingService;

    @BeforeEach
    void setUpPriceSettingService() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        ProductPriceSnapshotReader snapshotReader = new ProductPriceSnapshotReader();
        ProductPriceConditionSnapshotFactory conditionSnapshotFactory =
            new ProductPriceConditionSnapshotFactory(snapshotReader);
        ProductPriceMaterialSyncService materialSyncService =
            new ProductPriceMaterialSyncService(
                materialMapper, materialRuleMapper, settingMapper, snapshotReader, conditionSnapshotFactory);
        ProductPriceMaterialRuleWriter ruleWriter = new ProductPriceMaterialRuleWriter(
            materialRuleMapper,
            new ProductPriceMaterialRuleGuard(),
            conditionSnapshotFactory
        );
        priceSettingService = new ProductPriceSettingServiceImpl(
            saleProductMapper,
            settingMapper,
            materialMapper,
            materialRuleMapper,
            versionMapper,
            new ProductPriceSettingValidator(snapshotReader, conditionSnapshotFactory),
            snapshotReader,
            materialSyncService,
            ruleWriter,
            new ProductPriceMaterialVoAssembler(),
            org.mockito.Mockito.mock(ProductPricingEngine.class),
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
            {"materials":[{"materialId":4001,"attributeGroupCode":"FABRIC","materialCode":"MAT-FABRIC","materialNameCn":"斑马帘面料","unitCode":"m2","specModelText":"宽幅 290"}],"priceSnapshot":[{"materialCode":"MAT-FABRIC","salesPrice":12.30,"unitPrice":8.20}]}
            """);
        return version;
    }

    protected ProductFormulaVersion allMaterialVersion() {
        ProductFormulaVersion version = effectiveVersion();
        version.setSetupSnapshotJson("""
            {"materials":[{"materialId":4001,"attributeGroupCode":"FABRIC","materialCode":"MAT-FABRIC","materialNameCn":"斑马帘面料","unitCode":"m2","specModelText":"宽幅 290"},{"materialId":4002,"attributeGroupCode":"ALUMINUM","materialCode":"MAT-BAR","materialNameCn":"下杆","unitCode":"m"}],"priceSnapshot":[{"materialCode":"MAT-FABRIC","salesPrice":12.30,"unitPrice":8.20},{"materialCode":"MAT-BAR","salesPrice":3.50,"unitPrice":2.00}]}
            """);
        return version;
    }

    protected ProductPriceMaterialRuleBo materialRuleBo() {
        ProductPriceMaterialRuleBo bo = new ProductPriceMaterialRuleBo();
        bo.setPriceMaterialId(9201L);
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

    protected ProductPriceMaterial priceMaterial() {
        return priceMaterial(9201L);
    }

    protected ProductPriceMaterial priceMaterial(Long id) {
        ProductPriceMaterial material = new ProductPriceMaterial();
        material.setPriceMaterialId(id);
        material.setPriceSettingId(9101L);
        material.setMaterialId(4001L);
        material.setAttributeGroupCode("FABRIC");
        material.setMaterialCode("MAT-FABRIC");
        material.setMaterialNameCn("斑马帘面料");
        material.setUnitCode("m2");
        material.setStatus("ENABLED");
        return material;
    }

    protected ProductPriceMaterialRule materialRule() {
        ProductPriceMaterialRule rule = new ProductPriceMaterialRule();
        rule.setPriceMaterialId(9201L);
        rule.setConditionType("DEFAULT");
        rule.setConditionJson("{\"type\":\"DEFAULT\"}");
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

}
