package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.entity.ProductPriceFabric;
import com.bocoo.product.domain.entity.ProductPriceFabricRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.domain.vo.ProductMaterialVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.domain.vo.ProductPriceValidationIssueVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductPriceSettingServiceTest extends ProductPriceSettingServiceTestSupport {

    @Test
    void enabledSaleProductCannotChangePriceRules() {
        ProductSaleProduct product = saleProduct("ENABLED");
        when(saleProductMapper.selectById(9001L)).thenReturn(product);

        assertThatThrownBy(() -> priceSettingService.saveFabricRules(9001L, 9201L, List.of(fabricRuleBo())))
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
        when(fabricMapper.selectById(9201L)).thenReturn(priceFabric());
        when(fabricRuleMapper.selectList(any())).thenReturn(List.of());

        assertThat(priceSettingService.saveFabricRules(9001L, 9201L, List.of(fabricRuleBo()))).isTrue();

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
        when(fabricMapper.selectList(any())).thenReturn(List.of());
        when(fabricRuleMapper.selectVoList(any())).thenReturn(List.of());
        when(fabricRuleMapper.selectList(any())).thenReturn(List.of());

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
        when(fabricMapper.selectList(any())).thenReturn(List.of(priceFabric()));
        when(fabricRuleMapper.selectList(any())).thenReturn(List.of(fabricRule()));

        List<ProductPriceValidationIssueVo> issues = priceSettingService.validatePrice(9001L);

        assertThat(issues).isEmpty();
        verify(settingMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        verify(saleProductMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        verify(changeLogService).record("PRODUCT_PRICING", "PRICE_SETTING", 9101L,
            "SP-001", "VALIDATE_PRICE", null, Map.of("rowCount", 0), null);
    }

    @Test
    void validatePriceAllowsExplicitFormulaWhenUnitPriceIsZero() {
        ProductSaleProduct product = saleProduct("DISABLED");
        ProductPriceSetting setting = setting();
        ProductPriceFabricRule rule = fabricRule();
        rule.setUnitPrice(BigDecimal.ZERO);
        rule.setPriceFormula("50 * MAX(drop * width / 144, 1)");
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(versionMapper.selectById(7001L)).thenReturn(effectiveVersion());
        when(fabricMapper.selectList(any())).thenReturn(List.of(priceFabric()));
        when(fabricRuleMapper.selectList(any())).thenReturn(List.of(rule));

        List<ProductPriceValidationIssueVo> issues = priceSettingService.validatePrice(9001L);

        assertThat(issues).isEmpty();
    }

    @Test
    void validatePriceRejectsZeroResultFormula() {
        ProductSaleProduct product = saleProduct("DISABLED");
        ProductPriceSetting setting = setting();
        ProductPriceFabricRule rule = fabricRule();
        rule.setUnitPrice(BigDecimal.ZERO);
        rule.setPriceFormula("0.00 * MAX(drop * width / 144, 1)");
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(versionMapper.selectById(7001L)).thenReturn(effectiveVersion());
        when(fabricMapper.selectList(any())).thenReturn(List.of(priceFabric()));
        when(fabricRuleMapper.selectList(any())).thenReturn(List.of(rule));

        List<ProductPriceValidationIssueVo> issues = priceSettingService.validatePrice(9001L);

        assertThat(issues).extracting(ProductPriceValidationIssueVo::getMessageKey)
            .contains("product.priceSetting.priceFormulaInvalid");
    }

    @Test
    void generateFabricPricesRemovesDuplicateFabricRows() {
        ProductSaleProduct product = saleProduct("DISABLED");
        ProductPriceSetting setting = setting();
        ProductPriceFabric first = priceFabric(9201L);
        ProductPriceFabric duplicate = priceFabric(9202L);
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(versionMapper.selectById(7001L)).thenReturn(effectiveVersion());
        when(fabricMapper.selectList(any()))
            .thenReturn(List.of(first, duplicate))
            .thenReturn(List.of(first, duplicate))
            .thenReturn(List.of(first))
            .thenReturn(List.of(first))
            .thenReturn(List.of(first));
        when(settingMapper.selectList(any())).thenReturn(List.of(setting));
        when(fabricRuleMapper.selectList(any())).thenReturn(List.of());
        when(fabricRuleMapper.selectCount(any())).thenReturn(1L);

        assertThat(priceSettingService.generateFabricPrices(9001L, false)).isTrue();

        verify(fabricMapper).delete(any());
        verify(fabricMapper).updateById(any(ProductPriceFabric.class));
    }

}
