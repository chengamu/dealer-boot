package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.lock.annotation.Lock4j;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.entity.ProductPriceMaterial;
import com.bocoo.product.domain.entity.ProductPriceMaterialRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.domain.bo.ProductPriceMaterialBatchRuleBo;
import com.bocoo.product.domain.vo.ProductFormulaMaterialVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.domain.vo.ProductPriceValidationIssueVo;
import com.bocoo.product.service.impl.ProductPriceSettingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    void allPriceWorkspaceOperationsShareTheSameBusinessLock() {
        Set<String> guardedMethods = Set.of(
            "querySetup", "generateMaterialPrices", "saveMaterialRules",
            "saveMaterialRulesBatch", "validatePrice", "quote");

        assertThat(Arrays.stream(ProductPriceSettingServiceImpl.class.getDeclaredMethods())
            .filter(method -> guardedMethods.contains(method.getName())).toList())
            .hasSize(guardedMethods.size())
            .allSatisfy(method -> {
                Lock4j lock = method.getAnnotation(Lock4j.class);
                assertThat(lock).isNotNull();
                assertThat(lock.name()).isEqualTo("product-price-setting");
                assertThat(lock.keys()).containsExactly("#saleProductId");
            });
    }

    @Test
    void enabledSaleProductCannotChangePriceRules() {
        ProductSaleProduct product = saleProduct("ENABLED");
        when(saleProductMapper.selectById(9001L)).thenReturn(product);

        assertThatThrownBy(() -> priceSettingService.saveMaterialRules(9001L, 9201L, List.of(materialRuleBo())))
            .isInstanceOf(ServiceException.class);

        verify(materialRuleMapper, never()).delete(any());
        verify(materialRuleMapper, never()).insert(any());
    }

    @Test
    void savingMaterialRulesResetsPriceReadiness() {
        ProductSaleProduct product = saleProduct("DISABLED");
        ProductPriceSetting setting = setting();
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(versionMapper.selectById(7001L)).thenReturn(effectiveVersion());
        when(materialMapper.selectById(9201L)).thenReturn(priceMaterial());
        when(materialRuleMapper.selectList(any())).thenReturn(List.of());

        assertThat(priceSettingService.saveMaterialRules(9001L, 9201L, List.of(materialRuleBo()))).isTrue();

        verify(materialRuleMapper).delete(any());
        verify(materialRuleMapper).insert(any(ProductPriceMaterialRule.class));
        verify(settingMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        verify(saleProductMapper).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void querySetupReturnsAllFormulaMaterialsFromVersionSnapshot() {
        ProductSaleProduct product = saleProduct("DISABLED");
        ProductPriceSetting setting = setting();
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(versionMapper.selectById(7001L)).thenReturn(allMaterialVersion());
        when(materialMapper.selectList(any())).thenReturn(List.of());
        when(materialRuleMapper.selectVoList(any())).thenReturn(List.of());
        when(materialRuleMapper.selectList(any())).thenReturn(List.of());

        ProductPriceSetupVo setup = priceSettingService.querySetup(9001L);

        assertThat(setup.getFormulaMaterials())
            .extracting(ProductFormulaMaterialVo::getMaterialCode)
            .containsExactly("MAT-FABRIC", "MAT-BAR");
    }

    @Test
    void prepareRuntimeOnlyReadsValidatedConfiguration() {
        ProductSaleProduct product = saleProduct("ENABLED");
        product.setPriceStatus("READY");
        ProductPriceSetting setting = setting();
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(versionMapper.selectById(7001L)).thenReturn(effectiveVersion());
        when(materialMapper.selectList(any())).thenReturn(List.of(priceMaterial()));
        when(materialRuleMapper.selectList(any())).thenReturn(List.of(materialRule()));

        ProductPriceRuntimeContext runtime = priceSettingService.prepareRuntime(9001L);

        assertThat(runtime.saleProductId()).isEqualTo(9001L);
        assertThat(runtime.formulaVersionId()).isEqualTo(7001L);
        assertThat(runtime.currencyCode()).isEqualTo("USD");
        verify(settingMapper, never()).insert(any());
        verify(settingMapper, never()).updateById(any());
        verify(materialMapper, never()).insert(any());
        verify(materialRuleMapper, never()).insert(any());
    }

    @Test
    void validatePriceMarksSaleProductReadyWhenRulesPass() {
        ProductSaleProduct product = saleProduct("DISABLED");
        ProductPriceSetting setting = setting();
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(versionMapper.selectById(7001L)).thenReturn(effectiveVersion());
        when(materialMapper.selectList(any())).thenReturn(List.of(priceMaterial()));
        when(materialRuleMapper.selectList(any())).thenReturn(List.of(materialRule()));

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
        ProductPriceMaterialRule rule = materialRule();
        rule.setUnitPrice(BigDecimal.ZERO);
        rule.setPriceFormula("50 * MAX(drop * width / 144, 1)");
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(versionMapper.selectById(7001L)).thenReturn(effectiveVersion());
        when(materialMapper.selectList(any())).thenReturn(List.of(priceMaterial()));
        when(materialRuleMapper.selectList(any())).thenReturn(List.of(rule));

        List<ProductPriceValidationIssueVo> issues = priceSettingService.validatePrice(9001L);

        assertThat(issues).isEmpty();
    }

    @Test
    void validatePriceRejectsZeroResultFormula() {
        ProductSaleProduct product = saleProduct("DISABLED");
        ProductPriceSetting setting = setting();
        ProductPriceMaterialRule rule = materialRule();
        rule.setUnitPrice(BigDecimal.ZERO);
        rule.setPriceFormula("0.00 * MAX(drop * width / 144, 1)");
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(versionMapper.selectById(7001L)).thenReturn(effectiveVersion());
        when(materialMapper.selectList(any())).thenReturn(List.of(priceMaterial()));
        when(materialRuleMapper.selectList(any())).thenReturn(List.of(rule));

        List<ProductPriceValidationIssueVo> issues = priceSettingService.validatePrice(9001L);

        assertThat(issues).extracting(ProductPriceValidationIssueVo::getMessageKey)
            .contains("product.priceSetting.priceFormulaInvalid");
    }

    @Test
    void generateMaterialPricesRemovesDuplicateMaterialRows() {
        ProductSaleProduct product = saleProduct("DISABLED");
        ProductPriceSetting setting = setting();
        ProductPriceMaterial first = priceMaterial(9201L);
        ProductPriceMaterial duplicate = priceMaterial(9202L);
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(versionMapper.selectById(7001L)).thenReturn(effectiveVersion());
        when(materialMapper.selectList(any()))
            .thenReturn(List.of(first, duplicate))
            .thenReturn(List.of(first, duplicate))
            .thenReturn(List.of(first))
            .thenReturn(List.of(first))
            .thenReturn(List.of(first));
        when(settingMapper.selectList(any())).thenReturn(List.of(setting));
        when(materialRuleMapper.selectList(any())).thenReturn(List.of());

        assertThat(priceSettingService.generateMaterialPrices(9001L, false)).isTrue();

        verify(materialMapper).delete(any());
        verify(materialMapper).updateById(any(ProductPriceMaterial.class));
    }

    @Test
    void batchPricingRejectsMixedMaterialGroupsBeforeWriting() {
        ProductSaleProduct product = saleProduct("DISABLED");
        ProductPriceMaterial fabric = priceMaterial(9201L);
        ProductPriceMaterial aluminum = priceMaterial(9202L);
        aluminum.setMaterialId(4002L);
        aluminum.setMaterialCode("MAT-BAR");
        aluminum.setAttributeGroupCode("ALUMINUM");
        ProductPriceMaterialBatchRuleBo batch = new ProductPriceMaterialBatchRuleBo();
        batch.setPriceMaterialIds(List.of(9201L, 9202L));
        batch.setRules(List.of(materialRuleBo()));
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(settingMapper.selectOne(any())).thenReturn(setting());
        when(versionMapper.selectById(7001L)).thenReturn(effectiveVersion());
        when(materialMapper.selectById(9201L)).thenReturn(fabric);
        when(materialMapper.selectById(9202L)).thenReturn(aluminum);

        assertThatThrownBy(() -> priceSettingService.saveMaterialRulesBatch(9001L, batch))
            .isInstanceOf(ServiceException.class);

        verify(materialRuleMapper, never()).delete(any());
        verify(materialRuleMapper, never()).insert(any());
    }

}
