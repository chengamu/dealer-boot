package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductPriceFeeRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductShippingTemplate;
import com.bocoo.product.domain.entity.ProductShippingTemplateRule;
import com.bocoo.product.mapper.ProductPriceFeeRuleMapper;
import com.bocoo.product.mapper.ProductShippingTemplateMapper;
import com.bocoo.product.mapper.ProductShippingTemplateRuleMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductPriceShippingTemplateImporter extends ProductServiceSupport {

    private final ProductShippingTemplateMapper templateMapper;
    private final ProductShippingTemplateRuleMapper ruleMapper;
    private final ProductPriceFeeRuleMapper feeRuleMapper;
    private final ProductPriceShippingRuleFactory shippingRuleFactory;

    ImportResult importTemplate(ProductPriceSetting setting, Long shippingTemplateId) {
        ProductShippingTemplate template = requireTemplate(shippingTemplateId);
        List<ProductShippingTemplateRule> rules = queryRules(template);
        validateRules(rules);
        List<ProductPriceFeeRule> before = feeRuleMapper.selectList(activeQuery(ProductPriceFeeRule.class)
            .eq("price_setting_id", setting.getPriceSettingId()));
        feeRuleMapper.delete(activeQuery(ProductPriceFeeRule.class)
            .eq("price_setting_id", setting.getPriceSettingId()));
        int index = 0;
        for (ProductShippingTemplateRule rule : rules) {
            ProductPriceFeeRule entity = shippingRuleFactory.fromTemplateRule(template, rule, setting, index++);
            ProductEntityDefaults.prepareInsert(entity);
            feeRuleMapper.insert(entity);
        }
        return new ImportResult(before, template.getTemplateCode(), rules.size());
    }

    private ProductShippingTemplate requireTemplate(Long shippingTemplateId) {
        ProductShippingTemplate template = shippingTemplateId == null ? null : templateMapper.selectById(shippingTemplateId);
        if (template == null || !"0".equals(StringUtils.blankToDefault(template.getDelFlag(), "0"))
            || !STATUS_ENABLED.equals(template.getStatus())) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.enabledRequired");
        }
        return template;
    }

    private List<ProductShippingTemplateRule> queryRules(ProductShippingTemplate template) {
        return ruleMapper.selectList(activeQuery(ProductShippingTemplateRule.class)
            .eq("shipping_template_id", template.getShippingTemplateId())
            .orderByAsc("fee_code", "min_area_sqft", "sort_order", "shipping_template_rule_id"));
    }

    private void validateRules(List<ProductShippingTemplateRule> rules) {
        Set<String> codes = rules.stream().map(ProductShippingTemplateRule::getFeeCode).collect(Collectors.toSet());
        if (!codes.contains(ProductPriceShippingRuleFactory.CODE_MANUAL)
            || !codes.contains(ProductPriceShippingRuleFactory.CODE_MOTORIZED)) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.ruleRequired");
        }
        for (ProductShippingTemplateRule rule : rules) {
            if (!ProductPriceExpressionValidator.isShippingFormulaValid(rule.getFormulaText())) {
                throw ServiceException.ofMessageKey("product.shippingTemplate.formulaInvalid");
            }
        }
    }

    record ImportResult(List<ProductPriceFeeRule> before, String templateCode, int rowCount) {
    }
}
