package com.bocoo.product.service;

import com.bocoo.product.domain.vo.ProductShippingTemplateRuleVo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductShippingRuleMatcherTest {

    @Test
    void boundaryBelongsToNextRange() {
        ProductShippingTemplateRuleVo first = rule(1L, "0", "20", 1);
        ProductShippingTemplateRuleVo second = rule(2L, "20", "40", 2);
        ProductShippingTemplateRuleVo last = rule(3L, "40", null, 3);

        assertThat(ProductShippingRuleMatcher.match(List.of(first, second, last), "MANUAL", new BigDecimal("19.9999"))
            .getShippingTemplateRuleId()).isEqualTo(1L);
        assertThat(ProductShippingRuleMatcher.match(List.of(first, second, last), "MANUAL", new BigDecimal("20"))
            .getShippingTemplateRuleId()).isEqualTo(2L);
        assertThat(ProductShippingRuleMatcher.match(List.of(first, second, last), "MANUAL", new BigDecimal("40"))
            .getShippingTemplateRuleId()).isEqualTo(3L);
    }

    private ProductShippingTemplateRuleVo rule(Long id, String min, String max, int sort) {
        ProductShippingTemplateRuleVo row = new ProductShippingTemplateRuleVo();
        row.setShippingTemplateRuleId(id);
        row.setFeeCode("MANUAL");
        row.setMinAreaSqft(new BigDecimal(min));
        row.setMaxAreaSqft(max == null ? null : new BigDecimal(max));
        row.setSortOrder(sort);
        return row;
    }
}
