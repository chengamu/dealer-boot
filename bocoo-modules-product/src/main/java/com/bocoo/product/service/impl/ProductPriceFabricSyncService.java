package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceFabric;
import com.bocoo.product.domain.entity.ProductPriceFabricRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.mapper.ProductPriceFabricMapper;
import com.bocoo.product.mapper.ProductPriceFabricRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductPriceFabricSyncService extends ProductServiceSupport {
    static final String DEFAULT_PRICE_FORMULA = "unitPrice * MAX(drop * 2.54 * width * 2.54 / 10000, 1)";

    private final ProductPriceFabricMapper fabricMapper;
    private final ProductPriceFabricRuleMapper ruleMapper;
    private final ProductPriceSettingMapper settingMapper;
    private final ProductPriceSnapshotReader snapshotReader;

    public List<ProductPriceFabric> sync(ProductPriceSetting setting, ProductFormulaVersion version, boolean overwrite) {
        ruleMapper.delete(activeQuery(ProductPriceFabricRule.class)
            .eq("price_setting_id", setting.getPriceSettingId())
            .isNull("price_fabric_id"));
        List<Map<String, Object>> fabricMaterials = snapshotReader.fabricMaterials(version);
        Set<String> targetCodes = fabricMaterials.stream()
            .map(material -> text(material.get("materialCode")))
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());
        if (overwrite) {
            ruleMapper.delete(activeQuery(ProductPriceFabricRule.class).eq("price_setting_id", setting.getPriceSettingId()));
            fabricMapper.delete(activeQuery(ProductPriceFabric.class).eq("price_setting_id", setting.getPriceSettingId()));
        } else {
            cleanupStaleFabrics(setting, targetCodes);
        }
        Map<String, ProductPriceFabric> currentByCode = fabricMapper.selectList(activeQuery(ProductPriceFabric.class)
                .eq("price_setting_id", setting.getPriceSettingId()))
            .stream().collect(Collectors.toMap(ProductPriceFabric::getMaterialCode, Function.identity(), (left, right) -> left));
        Map<String, ProductPriceFabricRule> reusableRules = reusableDefaultRules(setting);
        Map<String, Map<String, Object>> priceSnapshot = snapshotReader.priceSnapshotByMaterialCode(version);
        int sort = 0;
        for (Map<String, Object> material : fabricMaterials) {
            String materialCode = text(material.get("materialCode"));
            if (StringUtils.isBlank(materialCode)) {
                continue;
            }
            ProductPriceFabric fabric = currentByCode.get(materialCode);
            if (fabric == null) {
                fabric = createFabric(setting, material, sort);
                fabricMapper.insert(fabric);
            } else {
                fabric.setSortOrder(sort);
                fabric.setMaterialNameCn(text(material.get("materialNameCn")));
                fabric.setUnitCode(text(material.get("unitCode")));
                fabricMapper.updateById(fabric);
            }
            ensureDefaultRule(setting, fabric, reusableRules.get(materialCode), priceSnapshot.get(materialCode), sort);
            sort++;
        }
        return fabricMapper.selectList(activeQuery(ProductPriceFabric.class)
            .eq("price_setting_id", setting.getPriceSettingId()).orderByAsc("sort_order", "price_fabric_id"));
    }

    private void cleanupStaleFabrics(ProductPriceSetting setting, Set<String> targetCodes) {
        List<ProductPriceFabric> existing = fabricMapper.selectList(activeQuery(ProductPriceFabric.class)
            .eq("price_setting_id", setting.getPriceSettingId()).orderByAsc("sort_order", "price_fabric_id"));
        Set<String> seen = new HashSet<>();
        List<Long> removeIds = new ArrayList<>();
        for (ProductPriceFabric fabric : existing) {
            String code = fabric.getMaterialCode();
            if (fabric.getPriceFabricId() == null) {
                continue;
            }
            if (StringUtils.isBlank(code) || !targetCodes.contains(code) || !seen.add(code)) {
                removeIds.add(fabric.getPriceFabricId());
            }
        }
        if (removeIds.isEmpty()) {
            return;
        }
        ruleMapper.delete(activeQuery(ProductPriceFabricRule.class).in("price_fabric_id", removeIds));
        fabricMapper.delete(activeQuery(ProductPriceFabric.class).in("price_fabric_id", removeIds));
    }

    private void ensureDefaultRule(ProductPriceSetting setting, ProductPriceFabric fabric, ProductPriceFabricRule source,
                                   Map<String, Object> priceSnapshot, int sort) {
        Long count = ruleMapper.selectCount(activeQuery(ProductPriceFabricRule.class)
            .eq("price_fabric_id", fabric.getPriceFabricId())
            .eq("default_rule_flag", true));
        if (count != null && count > 0) {
            return;
        }
        ProductPriceFabricRule rule = new ProductPriceFabricRule();
        rule.setTenantId(setting.getTenantId());
        rule.setPriceSettingId(setting.getPriceSettingId());
        rule.setPriceFabricId(fabric.getPriceFabricId());
        rule.setSaleProductId(setting.getSaleProductId());
        rule.setFormulaVersionId(setting.getFormulaVersionId());
        rule.setConditionType("DEFAULT");
        rule.setConditionExpression("DEFAULT");
        rule.setConditionText("默认规则");
        rule.setConditionKey("DEFAULT");
        rule.setPriceMode("FORMULA");
        rule.setUnitPrice(source == null ? unitPrice(priceSnapshot) : source.getUnitPrice());
        rule.setPriceFormula(StringUtils.blankToDefault(source == null ? null : source.getPriceFormula(), DEFAULT_PRICE_FORMULA));
        rule.setDefaultRuleFlag(Boolean.TRUE);
        rule.setStatus(STATUS_ENABLED);
        rule.setSortOrder(sort);
        rule.setRemark(source == null ? null : source.getRemark());
        ProductEntityDefaults.prepareInsert(rule);
        ruleMapper.insert(rule);
    }

    private Map<String, ProductPriceFabricRule> reusableDefaultRules(ProductPriceSetting setting) {
        QueryWrapper<ProductPriceSetting> settingQuery = activeQuery(ProductPriceSetting.class)
            .eq("sale_product_id", setting.getSaleProductId()).orderByDesc("update_time");
        List<Long> settingIds = settingMapper.selectList(settingQuery).stream()
            .map(ProductPriceSetting::getPriceSettingId).toList();
        if (settingIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, ProductPriceFabric> fabrics = fabricMapper.selectList(activeQuery(ProductPriceFabric.class)
                .in("price_setting_id", settingIds))
            .stream().collect(Collectors.toMap(ProductPriceFabric::getPriceFabricId, Function.identity(), (left, right) -> left));
        return ruleMapper.selectList(activeQuery(ProductPriceFabricRule.class)
                .in("price_setting_id", settingIds)
                .eq("default_rule_flag", true)
                .orderByDesc("update_time"))
            .stream()
            .filter(rule -> fabrics.containsKey(rule.getPriceFabricId()))
            .collect(Collectors.toMap(rule -> fabrics.get(rule.getPriceFabricId()).getMaterialCode(), Function.identity(), (left, right) -> left));
    }

    private ProductPriceFabric createFabric(ProductPriceSetting setting, Map<String, Object> material, int sort) {
        ProductPriceFabric fabric = new ProductPriceFabric();
        fabric.setTenantId(setting.getTenantId());
        fabric.setPriceSettingId(setting.getPriceSettingId());
        fabric.setSaleProductId(setting.getSaleProductId());
        fabric.setFormulaVersionId(setting.getFormulaVersionId());
        fabric.setMaterialId(toLong(material.get("materialId")));
        fabric.setMaterialCode(text(material.get("materialCode")));
        fabric.setMaterialNameCn(text(material.get("materialNameCn")));
        fabric.setUnitCode(text(material.get("unitCode")));
        fabric.setStatus(STATUS_ENABLED);
        fabric.setSortOrder(sort);
        ProductEntityDefaults.prepareInsert(fabric);
        return fabric;
    }

    private BigDecimal unitPrice(Map<String, Object> row) {
        Object value = row == null ? null : row.get("salesPrice");
        if (value == null && row != null) {
            value = row.get("unitPrice");
        }
        return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
    }

    private Long toLong(Object value) {
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
