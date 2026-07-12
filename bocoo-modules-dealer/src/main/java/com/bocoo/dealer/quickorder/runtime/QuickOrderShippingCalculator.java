package com.bocoo.dealer.quickorder.runtime;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.merchant.service.CustomerQuoteCatalogService;
import com.bocoo.product.domain.bo.ProductShippingTemplateBo;
import com.bocoo.product.domain.vo.ProductShippingTemplateRuleVo;
import com.bocoo.product.domain.vo.ProductShippingTemplateVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.bocoo.product.service.ProductShippingRuleMatcher.match;

@Component
@RequiredArgsConstructor
class QuickOrderShippingCalculator {
    private static final BigDecimal SQ_IN_PER_SQ_FT = new BigDecimal("144");
    private final CustomerQuoteCatalogService catalogService;

    QuickOrderShippingResult calculate(String currencyCode, BigDecimal width, BigDecimal height,
                                       boolean motorized) {
        ProductShippingTemplateBo query = new ProductShippingTemplateBo();
        query.setCurrencyCode(currencyCode); query.setStatus("ENABLED");
        List<ProductShippingTemplateVo> templates = catalogService.queryShippingTemplates(query);
        if (templates.isEmpty()) throw ServiceException.ofMessageKey("dealer.quickOrder.shipping.missing");
        ProductShippingTemplateVo template = catalogService.queryShippingTemplate(
            templates.get(0).getShippingTemplateId());
        BigDecimal area = width.multiply(height).divide(SQ_IN_PER_SQ_FT, 6, RoundingMode.HALF_UP);
        String feeCode = motorized ? "MOTORIZED" : "MANUAL";
        ProductShippingTemplateRuleVo rule = match(template.getRules(), feeCode, area);
        if (rule == null) throw ServiceException.ofMessageKey("dealer.quickOrder.shipping.ruleMissing");
        return new QuickOrderShippingResult(template.getShippingTemplateId(), template.getTemplateCode(),
            rule.getShippingTemplateRuleId(), feeCode, money(rule.getFeeAmount()));
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
