package com.bocoo.merchant.service.impl;

import com.bocoo.merchant.domain.bo.CustomerQuoteItemBo;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import com.bocoo.merchant.service.CustomerQuoteCatalogService;
import com.bocoo.product.domain.bo.ProductPriceQuoteBo;
import com.bocoo.product.domain.vo.ProductPriceQuoteVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.domain.vo.ProductSaleProductVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerQuoteCalculatorTest {

    @Mock
    private CustomerQuoteCatalogService catalogService;
    @Mock
    private CustomerQuoteOptionSnapshotBuilder optionBuilder;
    @Mock
    private CustomerQuoteShippingCalculator shippingCalculator;

    private CustomerQuoteCalculator calculator;

    @BeforeEach
    void setUp() {
        CustomerQuoteJsonSupport jsonSupport = new CustomerQuoteJsonSupport(new ObjectMapper());
        CustomerQuoteItemFactory itemFactory = new CustomerQuoteItemFactory(catalogService, jsonSupport);
        calculator = new CustomerQuoteCalculator(catalogService, optionBuilder, shippingCalculator, itemFactory);
    }

    @Test
    void calculateMultipliesProductAndShippingByQuantity() {
        ProductPriceSetupVo setup = new ProductPriceSetupVo();
        ProductSaleProductVo product = new ProductSaleProductVo();
        product.setSaleProductId(10L);
        product.setSaleProductCode("SP-10");
        product.setSaleProductName("Zebra Shade");
        product.setFormulaId(20L);
        product.setFormulaVersionId(30L);
        product.setFormulaVersionLabel("V1");
        product.setStatus("ENABLED");
        setup.setSaleProduct(product);
        when(catalogService.querySetup(10L)).thenReturn(setup);

        ProductPriceQuoteVo price = new ProductPriceQuoteVo();
        price.setFormulaVersionId(30L);
        price.setCurrencyCode("USD");
        price.setSingleAmount(new BigDecimal("100"));
        price.setTotalAmount(new BigDecimal("200"));
        when(catalogService.quote(eq(10L), any(ProductPriceQuoteBo.class))).thenReturn(price);
        when(optionBuilder.build(eq(setup), any())).thenReturn(
            new CustomerQuoteOptionSnapshot(Map.of("SYSTEM", "MOTOR"), "系统：电机", "System: Motor", true, true));
        when(shippingCalculator.calculate("USD", new BigDecimal("48"), new BigDecimal("60"), true))
            .thenReturn(new CustomerQuoteShippingResult(1L, "SHIP-1", 2L, "MOTORIZED", new BigDecimal("15")));

        CustomerQuoteItemBo bo = new CustomerQuoteItemBo();
        bo.setSaleProductId(10L);
        bo.setOrderWidthInch(new BigDecimal("48"));
        bo.setOrderHeightInch(new BigDecimal("60"));
        bo.setQuantity(2);
        bo.setSelectedOptionValues(Map.of("SYSTEM", "MOTOR"));

        CustomerQuoteItem result = calculator.calculate(bo).item();

        assertThat(result.getUnitAmount()).isEqualByComparingTo("100.00");
        assertThat(result.getProductAmount()).isEqualByComparingTo("200.00");
        assertThat(result.getShippingAmount()).isEqualByComparingTo("30.00");
        assertThat(result.getShippingTemplateId()).isEqualTo(1L);
        assertThat(result.getLineAmount()).isEqualByComparingTo("230.00");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void failedKeepsDraftLineWhenProductSetupCannotBeLoaded() {
        CustomerQuoteItemBo bo = new CustomerQuoteItemBo();
        bo.setSaleProductId(99L);
        bo.setQuantity(3);
        doThrow(new IllegalStateException("missing setup")).when(catalogService).querySetup(99L);

        CustomerQuoteItem result = calculator.failed(bo, "calculation failed");

        assertThat(result.getSaleProductId()).isEqualTo(99L);
        assertThat(result.getQuantity()).isEqualTo(3);
        assertThat(result.getCalculationStatus()).isEqualTo("FAIL");
        assertThat(result.getCalculationMessage()).isEqualTo("calculation failed");
    }
}
