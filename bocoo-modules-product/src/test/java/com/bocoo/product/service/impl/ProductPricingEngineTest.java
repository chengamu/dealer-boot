package com.bocoo.product.service.impl;

import com.bocoo.product.domain.bo.ProductPriceQuoteBo;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceMaterial;
import com.bocoo.product.domain.entity.ProductPriceMaterialRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.domain.vo.ProductFormulaSimulationItemVo;
import com.bocoo.product.domain.vo.ProductFormulaSimulationVo;
import com.bocoo.product.domain.vo.ProductPriceQuoteVo;
import com.bocoo.product.mapper.ProductPriceMaterialMapper;
import com.bocoo.product.mapper.ProductPriceMaterialRuleMapper;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductPricingEngineTest {

    @Mock
    private ProductPriceMaterialMapper materialMapper;
    @Mock
    private ProductPriceMaterialRuleMapper ruleMapper;
    @Mock
    private ProductPriceSnapshotReader snapshotReader;
    @Mock
    private ProductFormulaSimulationEngine simulationEngine;

    @Test
    void pricesFabricByAreaAndOtherMaterialsByFormulaUsage() {
        ProductFormulaSetupVo setup = new ProductFormulaSetupVo();
        setup.setOptions(List.of());
        setup.setOptionValues(List.of());
        when(snapshotReader.formulaSetup(any())).thenReturn(setup);
        when(simulationEngine.run(any(), any(), any(), any(), isNull())).thenReturn(simulation());
        when(materialMapper.selectList(any())).thenReturn(List.of(
            material(9201L, 2001L, "DUPLICATE-CODE"), material(9202L, 2002L, "DUPLICATE-CODE")));
        when(ruleMapper.selectList(any())).thenReturn(List.of(
            rule(9201L, new BigDecimal("10"), "unitPrice * MAX(areaM2, 1)"),
            rule(9202L, new BigDecimal("3"), "unitPrice * usageQty")));

        ProductPriceQuoteVo quote = new ProductPricingEngine(
            materialMapper, ruleMapper, snapshotReader, simulationEngine)
            .quote(product(), setting(), version(), request());

        assertThat(quote.getSingleAmount()).isEqualByComparingTo("16.00");
        assertThat(quote.getItems()).extracting(item -> item.getAmount().toPlainString())
            .containsExactly("10.00", "6.00");
    }

    @Test
    void rejectsIncompleteQuoteInputBeforeSimulation() {
        ProductPriceQuoteBo invalid = new ProductPriceQuoteBo();
        assertThatThrownBy(() -> new ProductPricingEngine(
            materialMapper, ruleMapper, snapshotReader, simulationEngine)
            .quote(product(), setting(), version(), invalid))
            .isInstanceOf(com.bocoo.common.core.exception.ServiceException.class);
    }

    private ProductFormulaSimulationVo simulation() {
        ProductFormulaSimulationVo result = new ProductFormulaSimulationVo();
        result.setStatus("PASS");
        result.setItems(List.of(
            item(2001L, "DUPLICATE-CODE", "1"),
            item(2002L, "DUPLICATE-CODE", "2")));
        return result;
    }

    private ProductFormulaSimulationItemVo item(Long materialId, String code, String usage) {
        ProductFormulaSimulationItemVo item = new ProductFormulaSimulationItemVo();
        item.setMaterialId(materialId);
        item.setMaterialCode(code);
        item.setUsageQty(new BigDecimal(usage));
        return item;
    }

    private ProductPriceMaterial material(Long id, Long materialId, String code) {
        ProductPriceMaterial material = new ProductPriceMaterial();
        material.setPriceMaterialId(id);
        material.setMaterialId(materialId);
        material.setMaterialCode(code);
        material.setMaterialNameCn(code);
        return material;
    }

    private ProductPriceMaterialRule rule(Long materialId, BigDecimal unitPrice, String formula) {
        ProductPriceMaterialRule rule = new ProductPriceMaterialRule();
        rule.setPriceMaterialId(materialId);
        rule.setConditionType("DEFAULT");
        rule.setConditionExpression("DEFAULT");
        rule.setConditionText("默认规则");
        rule.setDefaultRuleFlag(true);
        rule.setUnitPrice(unitPrice);
        rule.setPriceFormula(formula);
        rule.setSortOrder(0);
        return rule;
    }

    private ProductSaleProduct product() {
        ProductSaleProduct product = new ProductSaleProduct();
        product.setSaleProductId(8001L);
        product.setFormulaId(3001L);
        return product;
    }

    private ProductPriceSetting setting() {
        ProductPriceSetting setting = new ProductPriceSetting();
        setting.setPriceSettingId(9101L);
        setting.setCurrencyCode("USD");
        return setting;
    }

    private ProductFormulaVersion version() {
        ProductFormulaVersion version = new ProductFormulaVersion();
        version.setVersionId(7001L);
        return version;
    }

    private ProductPriceQuoteBo request() {
        ProductPriceQuoteBo request = new ProductPriceQuoteBo();
        request.setOrderWidth(new BigDecimal("20"));
        request.setOrderHeight(new BigDecimal("72"));
        request.setOrderQuantity(1);
        return request;
    }
}
