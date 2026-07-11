package com.bocoo.merchant.service.impl;

import com.bocoo.merchant.domain.vo.CustomerQuoteExportEnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteItemVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerQuoteExportAssemblerTest {

    @Test
    void englishExportUsesFrozenCustomerFacingSummaryAndAmounts() {
        CustomerQuoteItemVo item = new CustomerQuoteItemVo();
        item.setRoomLocation("Living Room");
        item.setSaleProductName("Zebra Shade");
        item.setSelectedOptionsSummaryEn("Fabric: Cream / System: Manual");
        item.setUnitAmount(new BigDecimal("120.00"));
        item.setShippingAmount(new BigDecimal("36.00"));
        item.setLineAmount(new BigDecimal("276.00"));

        CustomerQuoteVo quote = new CustomerQuoteVo();
        quote.setQuoteNo("QT-001");
        quote.setCustomerName("Emily Carter");
        quote.setTotalAmount(new BigDecimal("276.00"));
        quote.setItems(List.of(item));

        CustomerQuoteExportEnVo row = new CustomerQuoteExportAssembler().enRows(quote).get(0);

        assertThat(row.getConfiguration()).isEqualTo("Fabric: Cream / System: Manual");
        assertThat(row.getUnitAmount()).isEqualByComparingTo("120.00");
        assertThat(row.getShippingAmount()).isEqualByComparingTo("36.00");
        assertThat(row.getQuoteTotal()).isEqualByComparingTo("276.00");
    }
}
