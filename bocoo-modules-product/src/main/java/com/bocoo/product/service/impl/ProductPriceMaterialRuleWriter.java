package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.bo.ProductPriceMaterialRuleBo;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceMaterial;
import com.bocoo.product.domain.entity.ProductPriceMaterialRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.mapper.ProductPriceMaterialRuleMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductPriceMaterialRuleWriter extends ProductServiceSupport {
    private final ProductPriceMaterialRuleMapper ruleMapper;
    private final ProductPriceMaterialRuleGuard ruleGuard;
    private final ProductPriceConditionSnapshotFactory conditionSnapshotFactory;

    List<ProductPriceMaterialRuleBo> prepare(ProductFormulaVersion version, List<ProductPriceMaterialRuleBo> rules) {
        List<ProductPriceMaterialRuleBo> prepared = (rules == null ? List.<ProductPriceMaterialRuleBo>of() : rules)
            .stream().map(this::copy).toList();
        for (ProductPriceMaterialRuleBo rule : prepared) {
            boolean defaultRule = Boolean.TRUE.equals(rule.getDefaultRuleFlag()) || "DEFAULT".equals(rule.getConditionType());
            rule.setDefaultRuleFlag(defaultRule);
            rule.setConditionType(defaultRule ? "DEFAULT" : "EXPRESSION");
            rule.setConditionExpression(defaultRule ? "DEFAULT" : rule.getConditionExpression());
            rule.setConditionText(defaultRule ? "默认规则" : StringUtils.blankToDefault(rule.getConditionText(), rule.getConditionExpression()));
            ProductPriceConditionSnapshotFactory.Snapshot snapshot = conditionSnapshotFactory.snapshot(
                version, rule.getConditionExpression(), rule.getConditionText());
            rule.setConditionJson(snapshot.json());
            rule.setConditionKey(snapshot.key());
        }
        return prepared;
    }

    void replace(ProductPriceSetting setting, ProductFormulaVersion version, ProductPriceMaterial material,
                 List<ProductPriceMaterialRuleBo> rules) {
        List<ProductPriceMaterialRuleBo> prepared = prepare(version, rules);
        ruleGuard.assertSavable(material, prepared);
        ruleMapper.delete(activeQuery(ProductPriceMaterialRule.class).eq("price_material_id", material.getPriceMaterialId()));
        int index = 0;
        for (ProductPriceMaterialRuleBo rule : prepared) {
            ProductPriceMaterialRule entity = toEntity(rule, setting, material, index++);
            ProductEntityDefaults.prepareInsert(entity);
            ruleMapper.insert(entity);
        }
    }

    private ProductPriceMaterialRuleBo copy(ProductPriceMaterialRuleBo source) {
        ProductPriceMaterialRuleBo target = new ProductPriceMaterialRuleBo();
        target.setConditionType(source.getConditionType());
        target.setConditionJson(source.getConditionJson());
        target.setConditionExpression(source.getConditionExpression());
        target.setConditionText(source.getConditionText());
        target.setConditionKey(source.getConditionKey());
        target.setUnitPrice(source.getUnitPrice());
        target.setPriceFormula(source.getPriceFormula());
        target.setDefaultRuleFlag(source.getDefaultRuleFlag());
        target.setSortOrder(source.getSortOrder());
        target.setRemark(source.getRemark());
        return target;
    }

    private ProductPriceMaterialRule toEntity(ProductPriceMaterialRuleBo source, ProductPriceSetting setting,
                                               ProductPriceMaterial material, int index) {
        ProductPriceMaterialRule target = new ProductPriceMaterialRule();
        target.setTenantId(setting.getTenantId());
        target.setPriceMaterialId(material.getPriceMaterialId());
        target.setPriceSettingId(setting.getPriceSettingId());
        target.setSaleProductId(setting.getSaleProductId());
        target.setFormulaVersionId(setting.getFormulaVersionId());
        target.setConditionType(source.getConditionType());
        target.setConditionJson(source.getConditionJson());
        target.setConditionExpression(source.getConditionExpression());
        target.setConditionText(source.getConditionText());
        target.setConditionKey(source.getConditionKey());
        target.setPriceMode("FORMULA");
        target.setUnitPrice(source.getUnitPrice());
        target.setPriceFormula(source.getPriceFormula());
        target.setDefaultRuleFlag(source.getDefaultRuleFlag());
        target.setStatus(STATUS_ENABLED);
        target.setSortOrder(source.getSortOrder() == null ? index : source.getSortOrder());
        target.setRemark(source.getRemark());
        return target;
    }
}
