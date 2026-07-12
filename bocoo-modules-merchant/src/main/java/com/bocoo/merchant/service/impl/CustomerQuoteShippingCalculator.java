package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.vo.ProductShippingTemplateRuleVo;
import com.bocoo.product.domain.vo.ProductShippingTemplateVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.bocoo.product.service.ProductShippingRuleMatcher.match;

@Component
@RequiredArgsConstructor
class CustomerQuoteShippingCalculator {

    private static final BigDecimal SQUARE_INCHES_PER_SQFT = new BigDecimal("144");
    CustomerQuoteShippingResult calculate(CustomerQuotePricingSession session, String currencyCode, BigDecimal widthInch,
                                          BigDecimal heightInch, boolean motorized) {
        ProductShippingTemplateVo template = session.shipping(currencyCode);
        if (template == null) throw ServiceException.ofMessageKey("customer.quote.shipping.templateMissing");
        BigDecimal area = widthInch.multiply(heightInch)
            .divide(SQUARE_INCHES_PER_SQFT, 4, RoundingMode.HALF_UP);
        String feeCode = motorized ? "MOTORIZED" : "MANUAL";
        ProductShippingTemplateRuleVo rule = match(template.getRules(), feeCode, area);
        if (rule == null) {
            throw ServiceException.ofMessageKey("customer.quote.shipping.ruleMissing");
        }
        return new CustomerQuoteShippingResult(template.getShippingTemplateId(), template.getTemplateCode(),
            rule.getShippingTemplateRuleId(), feeCode, money(rule.getFeeAmount()));
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
