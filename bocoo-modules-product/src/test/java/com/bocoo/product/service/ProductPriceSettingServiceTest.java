package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductPriceFabricRuleBo;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceFabricRule;
import com.bocoo.product.domain.entity.ProductPriceFeeRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.domain.vo.ProductMaterialVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.domain.vo.ProductPriceValidationIssueVo;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import com.bocoo.product.mapper.ProductPriceFabricRuleMapper;
import com.bocoo.product.mapper.ProductPriceFeeRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import com.bocoo.product.mapper.ProductSaleProductMapper;
import com.bocoo.product.service.impl.ProductPriceSettingServiceImpl;
import com.bocoo.product.service.impl.ProductPriceSettingValidator;
import com.bocoo.product.service.impl.ProductPriceFabricCandidateFactory;
import com.bocoo.product.service.impl.ProductPriceFabricRuleGenerator;
import com.bocoo.product.service.impl.ProductPriceFabricRuleGuard;
import com.bocoo.product.service.impl.ProductPriceShippingRuleFactory;
import com.bocoo.product.service.impl.ProductPriceSnapshotReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductPriceSettingServiceTest {

    @Mock
    private ProductSaleProductMapper saleProductMapper;
    @Mock
    private ProductPriceSettingMapper settingMapper;
    @Mock
    private ProductPriceFabricRuleMapper fabricRuleMapper;
    @Mock
    private ProductPriceFeeRuleMapper feeRuleMapper;
    @Mock
    private ProductFormulaVersionMapper versionMapper;
    @Mock
    private ProductChangeLogService changeLogService;

    private ProductPriceSettingServiceImpl priceSettingService;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        ProductPriceSnapshotReader snapshotReader = new ProductPriceSnapshotReader();
        priceSettingService = new ProductPriceSettingServiceImpl(
            saleProductMapper,
            settingMapper,
            fabricRuleMapper,
            feeRuleMapper,
            versionMapper,
            new ProductPriceSettingValidator(snapshotReader),
            snapshotReader,
            new ProductPriceFabricCandidateFactory(snapshotReader),
            new ProductPriceFabricRuleGenerator(),
            new ProductPriceFabricRuleGuard(snapshotReader),
            new ProductPriceShippingRuleFactory(),
            changeLogService
        );
    }

    @Test
    void enabledSaleProductCannotChangePriceRules() {
        ProductSaleProduct product = saleProduct("ENABLED");
        when(saleProductMapper.selectById(9001L)).thenReturn(product);

        assertThatThrownBy(() -> priceSettingService.saveFabricRules(9001L, List.of(fabricRuleBo())))
            .isInstanceOf(ServiceException.class);

        verify(fabricRuleMapper, never()).delete(any());
        verify(fabricRuleMapper, never()).insert(any());
    }

    @Test
    void savingFabricRulesResetsPriceReadiness() {
        ProductSaleProduct product = saleProduct("DISABLED");
        ProductPriceSetting setting = setting();
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(versionMapper.selectById(7001L)).thenReturn(effectiveVersion());
        when(fabricRuleMapper.selectList(any())).thenReturn(List.of());

        assertThat(priceSettingService.saveFabricRules(9001L, List.of(fabricRuleBo()))).isTrue();

        verify(fabricRuleMapper).delete(any());
        verify(fabricRuleMapper).insert(any(ProductPriceFabricRule.class));
        verify(settingMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        verify(saleProductMapper).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void querySetupReturnsFabricCandidatesFromFormulaVersionSnapshot() {
        ProductSaleProduct product = saleProduct("DISABLED");
        ProductPriceSetting setting = setting();
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(versionMapper.selectById(7001L)).thenReturn(effectiveVersion());
        when(fabricRuleMapper.selectVoList(any())).thenReturn(List.of());
        when(feeRuleMapper.selectVoList(any())).thenReturn(List.of());
        when(fabricRuleMapper.selectList(any())).thenReturn(List.of());
        when(feeRuleMapper.selectList(any())).thenReturn(List.of());

        ProductPriceSetupVo setup = priceSettingService.querySetup(9001L);

        assertThat(setup.getFabricCandidates())
            .extracting(ProductMaterialVo::getMaterialCode)
            .containsExactly("MAT-FABRIC");
        assertThat(setup.getFabricCandidates().get(0).getSalesPrice())
            .isEqualByComparingTo("12.30");
    }

    @Test
    void validatePriceMarksSaleProductReadyWhenRulesPass() {
        ProductSaleProduct product = saleProduct("DISABLED");
        ProductPriceSetting setting = setting();
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(versionMapper.selectById(7001L)).thenReturn(effectiveVersion());
        when(fabricRuleMapper.selectList(any())).thenReturn(List.of(fabricRule()));
        when(feeRuleMapper.selectList(any())).thenReturn(shippingRules());

        List<ProductPriceValidationIssueVo> issues = priceSettingService.validatePrice(9001L);

        assertThat(issues).isEmpty();
        verify(settingMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        verify(saleProductMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        verify(changeLogService).record("PRODUCT_PRICING", "PRICE_SETTING", 9101L,
            "SP-001", "VALIDATE_PRICE", null, issues, null);
    }

    private ProductSaleProduct saleProduct(String status) {
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

    private ProductPriceSetting setting() {
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

    private ProductFormulaVersion effectiveVersion() {
        ProductFormulaVersion version = new ProductFormulaVersion();
        version.setVersionId(7001L);
        version.setVersionStatus("EFFECTIVE");
        version.setSetupSnapshotJson("""
            {"materials":[{"materialId":4001,"attributeGroupCode":"FABRIC","materialCode":"MAT-FABRIC","materialNameCn":"斑马帘面料","unitCode":"m2","specModelText":"宽幅 290"}],"priceSnapshot":[{"materialCode":"MAT-FABRIC","salesPrice":12.30,"unitPrice":8.20}]}
            """);
        return version;
    }

    private ProductPriceFabricRuleBo fabricRuleBo() {
        ProductPriceFabricRuleBo bo = new ProductPriceFabricRuleBo();
        bo.setMaterialId(4001L);
        bo.setMaterialCode("MAT-FABRIC");
        bo.setMaterialNameCn("斑马帘面料");
        bo.setUnitCode("m2");
        bo.setOptionCombinationKey("DEFAULT");
        bo.setOptionCombinationName("默认");
        bo.setPriceMode("FORMULA");
        bo.setBasePrice(new BigDecimal("12.50"));
        bo.setAreaFormula("(width*height)/144");
        bo.setStatus("ENABLED");
        return bo;
    }

    private ProductPriceFabricRule fabricRule() {
        ProductPriceFabricRule rule = new ProductPriceFabricRule();
        rule.setMaterialCode("MAT-FABRIC");
        rule.setOptionCombinationKey("DEFAULT");
        rule.setOptionCombinationName("默认");
        rule.setPriceMode("FORMULA");
        rule.setBasePrice(new BigDecimal("12.50"));
        rule.setAreaFormula("(width*height)/144");
        rule.setStatus("ENABLED");
        return rule;
    }

    private List<ProductPriceFeeRule> shippingRules() {
        ProductPriceFeeRule manual = feeRule("MANUAL", "不带电邮费", "18 + MAX(width - 60, 0) * 0.35");
        ProductPriceFeeRule motorized = feeRule("MOTORIZED", "带电邮费", "25 + MAX(width - 60, 0) * 0.45");
        return List.of(manual, motorized);
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
