package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.product.domain.bo.ProductShippingTemplateRuleBo;
import com.bocoo.product.domain.vo.ProductShippingTemplateRuleImportVo;
import com.bocoo.product.domain.vo.ProductShippingTemplateRuleVo;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Component
public class ProductShippingTemplateRuleImporter {

    List<ProductShippingTemplateRuleVo> importRules(InputStream inputStream) {
        List<ProductShippingTemplateRuleImportVo> rows = ExcelUtil.importExcel(
            inputStream, ProductShippingTemplateRuleImportVo.class);
        if (rows.isEmpty()) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.importEmpty");
        }
        return rows.stream().map(this::toRule).toList();
    }

    List<ProductShippingTemplateRuleImportVo> templateRows() {
        return List.of(
            new ProductShippingTemplateRuleImportVo("不带电", BigDecimal.ZERO, new BigDecimal("20"), new BigDecimal("18")),
            new ProductShippingTemplateRuleImportVo("不带电", new BigDecimal("20"), null, new BigDecimal("25")),
            new ProductShippingTemplateRuleImportVo("带电", BigDecimal.ZERO, new BigDecimal("20"), new BigDecimal("25")),
            new ProductShippingTemplateRuleImportVo("带电", new BigDecimal("20"), null, new BigDecimal("32"))
        );
    }

    private ProductShippingTemplateRuleVo toRule(ProductShippingTemplateRuleImportVo row) {
        ProductShippingTemplateRuleVo rule = new ProductShippingTemplateRuleVo();
        String code = conditionCode(row.getCondition());
        rule.setFeeCode(code);
        rule.setFeeName(ProductShippingRuleSupport.feeName(code));
        rule.setMinAreaSqft(row.getMinAreaSqft() == null ? BigDecimal.ZERO : row.getMinAreaSqft());
        rule.setMaxAreaSqft(row.getMaxAreaSqft());
        rule.setFeeAmount(row.getFeeAmount());
        return rule;
    }

    private String conditionCode(String condition) {
        String value = StringUtils.blankToDefault(condition, "").trim().toUpperCase(Locale.ROOT);
        if (value.equals("MOTORIZED") || value.contains("带电") && !value.contains("不带电")) {
            return ProductShippingRuleSupport.CODE_MOTORIZED;
        }
        if (value.equals("MANUAL") || value.contains("不带电")) {
            return ProductShippingRuleSupport.CODE_MANUAL;
        }
        throw ServiceException.ofMessageKey("product.shippingTemplate.importConditionInvalid");
    }
}
