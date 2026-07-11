package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceMaterial;
import com.bocoo.product.domain.entity.ProductPriceMaterialRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.mapper.ProductPriceMaterialMapper;
import com.bocoo.product.mapper.ProductPriceMaterialRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductPriceMaterialSyncService extends ProductServiceSupport {
    static final String AREA_PRICE_FORMULA = "unitPrice * MAX(areaM2, 1)";
    static final String USAGE_PRICE_FORMULA = "unitPrice * usageQty";

    private final ProductPriceMaterialMapper materialMapper;
    private final ProductPriceMaterialRuleMapper ruleMapper;
    private final ProductPriceSettingMapper settingMapper;
    private final ProductPriceSnapshotReader snapshotReader;
    private final ProductPriceConditionSnapshotFactory conditionSnapshotFactory;

    public List<ProductPriceMaterial> sync(ProductPriceSetting setting, ProductFormulaVersion version, boolean overwrite) {
        ruleMapper.delete(activeQuery(ProductPriceMaterialRule.class)
            .eq("price_setting_id", setting.getPriceSettingId()).isNull("price_material_id"));
        List<Map<String, Object>> snapshotMaterials = snapshotReader.priceMaterials(version);
        Set<String> targetKeys = snapshotMaterials.stream().map(this::materialKey)
            .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        if (overwrite) {
            ruleMapper.delete(activeQuery(ProductPriceMaterialRule.class).eq("price_setting_id", setting.getPriceSettingId()));
            materialMapper.delete(activeQuery(ProductPriceMaterial.class).eq("price_setting_id", setting.getPriceSettingId()));
        } else {
            cleanupStaleMaterials(setting, targetKeys);
        }
        Map<String, ProductPriceMaterial> currentByKey = materialMapper.selectList(activeQuery(ProductPriceMaterial.class)
                .eq("price_setting_id", setting.getPriceSettingId()))
            .stream().collect(Collectors.toMap(this::materialKey, Function.identity(), (left, right) -> left));
        Set<Long> materialIdsWithRules = ruleMapper.selectList(activeQuery(ProductPriceMaterialRule.class)
                .eq("price_setting_id", setting.getPriceSettingId()))
            .stream().map(ProductPriceMaterialRule::getPriceMaterialId)
            .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<String, List<ProductPriceMaterialRule>> reusableRules = reusableRules(setting);
        Map<String, Map<String, Object>> priceSnapshot = snapshotReader.priceSnapshotByMaterialCode(version);
        int sort = 0;
        for (Map<String, Object> source : snapshotMaterials) {
            String key = materialKey(source);
            if (StringUtils.isBlank(key)) {
                continue;
            }
            ProductPriceMaterial material = currentByKey.get(key);
            if (material == null) {
                material = createMaterial(setting, source, sort);
                materialMapper.insert(material);
            } else {
                applySnapshot(material, source, sort);
                materialMapper.updateById(material);
            }
            if (!materialIdsWithRules.contains(material.getPriceMaterialId())) {
                ensureRules(setting, version, material, reusableRules.get(key), priceSnapshot.get(material.getMaterialCode()));
                materialIdsWithRules.add(material.getPriceMaterialId());
            }
            sort++;
        }
        return materialMapper.selectList(activeQuery(ProductPriceMaterial.class)
            .eq("price_setting_id", setting.getPriceSettingId()).orderByAsc("sort_order", "price_material_id"));
    }

    private void cleanupStaleMaterials(ProductPriceSetting setting, Set<String> targetKeys) {
        List<ProductPriceMaterial> existing = materialMapper.selectList(activeQuery(ProductPriceMaterial.class)
            .eq("price_setting_id", setting.getPriceSettingId()).orderByAsc("sort_order", "price_material_id"));
        Set<String> seen = new HashSet<>();
        List<Long> removeIds = existing.stream()
            .filter(row -> row.getPriceMaterialId() != null)
            .filter(row -> StringUtils.isBlank(materialKey(row)) || !targetKeys.contains(materialKey(row)) || !seen.add(materialKey(row)))
            .map(ProductPriceMaterial::getPriceMaterialId).toList();
        if (removeIds.isEmpty()) {
            return;
        }
        ruleMapper.delete(activeQuery(ProductPriceMaterialRule.class).in("price_material_id", removeIds));
        materialMapper.delete(activeQuery(ProductPriceMaterial.class).in("price_material_id", removeIds));
    }

    private void ensureRules(ProductPriceSetting setting, ProductFormulaVersion version, ProductPriceMaterial material,
                             List<ProductPriceMaterialRule> sources, Map<String, Object> priceSnapshot) {
        if (sources != null && !sources.isEmpty()) {
            int index = 0;
            for (ProductPriceMaterialRule source : sources) {
                ProductPriceMaterialRule copied = copyRule(setting, material, source, index);
                if (refreshCondition(version, copied)) {
                    insertRule(copied);
                    index++;
                }
            }
            if (index > 0) return;
        }
        ProductPriceMaterialRule rule = baseRule(setting, material);
        rule.setConditionType("DEFAULT");
        rule.setConditionJson(ProductFormulaConditionJsonFactory.defaultCondition());
        rule.setConditionExpression("DEFAULT");
        rule.setConditionText("默认规则");
        rule.setConditionKey("DEFAULT");
        rule.setUnitPrice(unitPrice(priceSnapshot));
        rule.setPriceFormula(defaultFormula(material));
        rule.setDefaultRuleFlag(Boolean.TRUE);
        rule.setSortOrder(0);
        insertRule(rule);
    }

    private Map<String, List<ProductPriceMaterialRule>> reusableRules(ProductPriceSetting current) {
        List<Long> settingIds = settingMapper.selectList(activeQuery(ProductPriceSetting.class)
                .eq("sale_product_id", current.getSaleProductId())
                .ne("price_setting_id", current.getPriceSettingId())
                .orderByDesc("update_time", "price_setting_id"))
            .stream().map(ProductPriceSetting::getPriceSettingId).toList();
        Map<String, List<ProductPriceMaterialRule>> result = new LinkedHashMap<>();
        for (Long settingId : settingIds) {
            List<ProductPriceMaterial> materials = materialMapper.selectList(activeQuery(ProductPriceMaterial.class)
                .eq("price_setting_id", settingId).orderByAsc("sort_order", "price_material_id"));
            if (materials.isEmpty()) {
                continue;
            }
            Map<Long, List<ProductPriceMaterialRule>> rulesByMaterial = ruleMapper.selectList(activeQuery(ProductPriceMaterialRule.class)
                    .eq("price_setting_id", settingId).orderByAsc("sort_order", "material_rule_id"))
                .stream().collect(Collectors.groupingBy(ProductPriceMaterialRule::getPriceMaterialId));
            for (ProductPriceMaterial material : materials) {
                List<ProductPriceMaterialRule> rules = rulesByMaterial.getOrDefault(material.getPriceMaterialId(), List.of());
                if (!rules.isEmpty()) {
                    result.putIfAbsent(materialKey(material), rules);
                }
            }
        }
        return result;
    }

    private ProductPriceMaterial createMaterial(ProductPriceSetting setting, Map<String, Object> source, int sort) {
        ProductPriceMaterial material = new ProductPriceMaterial();
        material.setTenantId(setting.getTenantId());
        material.setPriceSettingId(setting.getPriceSettingId());
        material.setSaleProductId(setting.getSaleProductId());
        material.setFormulaVersionId(setting.getFormulaVersionId());
        material.setStatus(STATUS_ENABLED);
        applySnapshot(material, source, sort);
        ProductEntityDefaults.prepareInsert(material);
        return material;
    }

    private void applySnapshot(ProductPriceMaterial target, Map<String, Object> source, int sort) {
        target.setFormulaMaterialId(toLong(source.get("formulaMaterialId")));
        target.setMaterialId(toLong(source.get("materialId")));
        target.setMaterialCode(text(source.get("materialCode")));
        target.setMaterialNameCn(text(source.get("materialNameCn")));
        target.setSpecModelText(text(source.get("specModelText")));
        target.setAttributeGroupCode(text(source.get("attributeGroupCode")));
        target.setAttributeGroupNameCn(text(source.get("attributeGroupNameCn")));
        target.setMaterialTypeCode(text(source.get("materialTypeCode")));
        target.setMaterialTypeNameCn(text(source.get("materialTypeNameCn")));
        target.setUnitCode(text(source.get("unitCode")));
        target.setSortOrder(sort);
    }

    private ProductPriceMaterialRule copyRule(ProductPriceSetting setting, ProductPriceMaterial material,
                                               ProductPriceMaterialRule source, int sort) {
        ProductPriceMaterialRule target = baseRule(setting, material);
        target.setConditionType(source.getConditionType());
        target.setConditionJson(source.getConditionJson());
        target.setConditionExpression(source.getConditionExpression());
        target.setConditionText(source.getConditionText());
        target.setConditionKey(source.getConditionKey());
        target.setUnitPrice(source.getUnitPrice());
        target.setPriceFormula(source.getPriceFormula());
        target.setDefaultRuleFlag(source.getDefaultRuleFlag());
        target.setSortOrder(sort);
        target.setRemark(source.getRemark());
        return target;
    }

    private boolean refreshCondition(ProductFormulaVersion version, ProductPriceMaterialRule rule) {
        try {
            ProductPriceConditionSnapshotFactory.Snapshot snapshot = conditionSnapshotFactory.snapshot(
                version, rule.getConditionExpression(), rule.getConditionText());
            rule.setConditionJson(snapshot.json());
            rule.setConditionKey(snapshot.key());
            return true;
        } catch (ServiceException ignored) {
            return false;
        }
    }

    private ProductPriceMaterialRule baseRule(ProductPriceSetting setting, ProductPriceMaterial material) {
        ProductPriceMaterialRule rule = new ProductPriceMaterialRule();
        rule.setTenantId(setting.getTenantId());
        rule.setPriceSettingId(setting.getPriceSettingId());
        rule.setPriceMaterialId(material.getPriceMaterialId());
        rule.setSaleProductId(setting.getSaleProductId());
        rule.setFormulaVersionId(setting.getFormulaVersionId());
        rule.setPriceMode("FORMULA");
        rule.setStatus(STATUS_ENABLED);
        return rule;
    }

    private void insertRule(ProductPriceMaterialRule rule) {
        ProductEntityDefaults.prepareInsert(rule);
        ruleMapper.insert(rule);
    }

    private String defaultFormula(ProductPriceMaterial material) {
        String groupCode = StringUtils.blankToDefault(material.getAttributeGroupCode(), material.getMaterialTypeCode());
        return "FABRIC".equalsIgnoreCase(groupCode) ? AREA_PRICE_FORMULA : USAGE_PRICE_FORMULA;
    }

    private String materialKey(Map<String, Object> source) {
        Long materialId = toLong(source.get("materialId"));
        return materialId == null ? codeKey(text(source.get("materialCode"))) : "ID:" + materialId;
    }

    private String materialKey(ProductPriceMaterial material) {
        return material.getMaterialId() == null ? codeKey(material.getMaterialCode()) : "ID:" + material.getMaterialId();
    }

    private String codeKey(String code) {
        return StringUtils.isBlank(code) ? null : "CODE:" + code.trim();
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
        String valueText = text(value);
        return StringUtils.isBlank(valueText) ? null : Long.valueOf(valueText);
    }

    private String text(Object value) {
        return value == null || "null".equals(String.valueOf(value)) ? null : String.valueOf(value);
    }
}
