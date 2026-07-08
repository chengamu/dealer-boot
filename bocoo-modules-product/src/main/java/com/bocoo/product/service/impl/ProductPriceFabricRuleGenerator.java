package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductPriceFabricRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.vo.ProductPriceOptionCombinationVo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProductPriceFabricRuleGenerator {

    static final String DEFAULT_FORMULA = "unitPrice * MAX(drop * 2.54 * width * 2.54 / 10000, 1)";

    public List<ProductPriceFabricRule> generate(ProductPriceSetting setting,
                                                 List<Map<String, Object>> fabrics,
                                                 List<ProductPriceOptionCombinationVo> combinations,
                                                 List<ProductPriceFabricRule> currentRules,
                                                 List<ProductPriceFabricRule> previousRules,
                                                 boolean overwrite) {
        Map<String, ProductPriceFabricRule> currentByKey = currentRules.stream()
            .collect(Collectors.toMap(this::key, Function.identity(), (left, right) -> left));
        Map<String, ProductPriceFabricRule> previousByKey = previousRules.stream()
            .collect(Collectors.toMap(this::key, Function.identity(), (left, right) -> left));
        String formula = firstFormula(currentRules, previousRules);
        List<ProductPriceFabricRule> result = new ArrayList<>();
        int sort = 0;
        for (Map<String, Object> fabric : fabrics) {
            for (ProductPriceOptionCombinationVo combination : combinations) {
                String key = key(text(fabric.get("materialCode")), combination.getOptionCombinationKey());
                ProductPriceFabricRule source = overwrite ? previousByKey.get(key) : currentByKey.getOrDefault(key, previousByKey.get(key));
                result.add(rule(setting, fabric, combination, source, formula, sort++));
            }
        }
        return result;
    }

    private ProductPriceFabricRule rule(ProductPriceSetting setting, Map<String, Object> fabric,
                                        ProductPriceOptionCombinationVo combination, ProductPriceFabricRule source,
                                        String formula, int sort) {
        ProductPriceFabricRule rule = new ProductPriceFabricRule();
        rule.setTenantId(setting.getTenantId());
        rule.setPriceSettingId(setting.getPriceSettingId());
        rule.setSaleProductId(setting.getSaleProductId());
        rule.setFormulaVersionId(setting.getFormulaVersionId());
        rule.setMaterialId(toLong(fabric.get("materialId")));
        rule.setMaterialCode(text(fabric.get("materialCode")));
        rule.setMaterialNameCn(text(fabric.get("materialNameCn")));
        rule.setUnitCode(text(fabric.get("unitCode")));
        rule.setOptionCombinationKey(combination.getOptionCombinationKey());
        rule.setOptionCombinationName(combination.getOptionCombinationName());
        rule.setPriceMode("FORMULA");
        rule.setBasePrice(source == null ? BigDecimal.ZERO : source.getBasePrice());
        rule.setAreaFormula(StringUtils.blankToDefault(source == null ? null : source.getAreaFormula(), formula));
        rule.setMinBillArea(BigDecimal.ONE);
        rule.setLossRate(BigDecimal.ZERO);
        rule.setStatus(ProductServiceSupport.STATUS_ENABLED);
        rule.setSortOrder(sort);
        return rule;
    }

    private String firstFormula(List<ProductPriceFabricRule> currentRules, List<ProductPriceFabricRule> previousRules) {
        return currentRules.stream().map(ProductPriceFabricRule::getAreaFormula).filter(StringUtils::isNotBlank).findFirst()
            .or(() -> previousRules.stream().map(ProductPriceFabricRule::getAreaFormula).filter(StringUtils::isNotBlank).findFirst())
            .orElse(DEFAULT_FORMULA);
    }

    private String key(ProductPriceFabricRule rule) {
        return key(rule.getMaterialCode(), rule.getOptionCombinationKey());
    }

    private String key(String materialCode, String combinationKey) {
        return StringUtils.blankToDefault(materialCode, "") + "||" + StringUtils.blankToDefault(combinationKey, "DEFAULT");
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = text(value);
        return StringUtils.isBlank(text) ? null : Long.valueOf(text);
    }

    private String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return "null".equals(text) ? null : text;
    }
}
