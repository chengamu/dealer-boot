package com.bocoo.dealer.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import com.bocoo.merchant.service.CustomerQuoteConversionSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerQuoteOrderCalculatorTest {
    @Mock private SalesDiscountResolver discounts;

    @Test
    void discountOnlyAppliesToFrozenProductAmount() {
        CustomerQuote quote = quote();
        CustomerQuoteItem item = item();
        when(discounts.profile(200L)).thenReturn(null);
        when(discounts.resolve(null, 10L, "CUSTOM_CURTAIN")).thenReturn(new BigDecimal("0.80"));

        var result = new CustomerQuoteOrderCalculator(discounts)
            .calculate(new CustomerQuoteConversionSnapshot(quote, List.of(item))).preview();

        assertThat(result.getListAmount()).isEqualByComparingTo("200.00");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("40.00");
        assertThat(result.getProductAmount()).isEqualByComparingTo("160.00");
        assertThat(result.getShippingAmount()).isEqualByComparingTo("30.00");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("190.00");
    }

    @Test
    void incompleteFrozenSnapshotCannotConvert() {
        CustomerQuoteItem item = item();
        item.setBomSnapshotJson(null);
        CustomerQuoteOrderCalculator calculator = new CustomerQuoteOrderCalculator(discounts);

        assertThatThrownBy(() -> calculator.calculate(new CustomerQuoteConversionSnapshot(quote(), List.of(item))))
            .isInstanceOf(ServiceException.class);
    }

    private CustomerQuote quote() {
        CustomerQuote row = new CustomerQuote();
        row.setQuoteId(1L); row.setTenantId(200L); row.setQuoteNo("QT-1"); row.setStatus("CONFIRMED");
        row.setCurrencyCode("USD"); row.setTotalAmount(new BigDecimal("230"));
        return row;
    }

    private CustomerQuoteItem item() {
        CustomerQuoteItem row = new CustomerQuoteItem();
        row.setQuoteItemId(11L); row.setLineNo(1); row.setCategoryId(10L);
        row.setProductTypeCode("CUSTOM_CURTAIN"); row.setFormulaVersionId(20L);
        row.setCalculationStatus("PASS"); row.setQuantity(2); row.setProductAmount(new BigDecimal("200"));
        row.setShippingTemplateId(30L); row.setShippingAmount(new BigDecimal("30"));
        row.setPricingSnapshotJson("{}"); row.setBomSnapshotJson("[]");
        return row;
    }
}
