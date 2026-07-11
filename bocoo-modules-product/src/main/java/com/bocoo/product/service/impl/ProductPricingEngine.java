package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.bo.ProductFormulaSimulationBo;
import com.bocoo.product.domain.bo.ProductPriceQuoteBo;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceMaterial;
import com.bocoo.product.domain.entity.ProductPriceMaterialRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.domain.vo.ProductFormulaOptionValueVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionVo;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.domain.vo.ProductFormulaSimulationItemVo;
import com.bocoo.product.domain.vo.ProductFormulaSimulationVo;
import com.bocoo.product.domain.vo.ProductPriceQuoteItemVo;
import com.bocoo.product.domain.vo.ProductPriceQuoteVo;
import com.bocoo.product.mapper.ProductPriceMaterialMapper;
import com.bocoo.product.mapper.ProductPriceMaterialRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductPricingEngine extends ProductServiceSupport {
    private final ProductPriceMaterialMapper materialMapper;
    private final ProductPriceMaterialRuleMapper ruleMapper;
    private final ProductPriceSnapshotReader snapshotReader;
    private final ProductFormulaSimulationEngine simulationEngine;

    public ProductPriceQuoteVo quote(ProductSaleProduct saleProduct, ProductPriceSetting setting,
                                     ProductFormulaVersion version, ProductPriceQuoteBo request) {
        ProductFormulaSetupVo setup = snapshotReader.formulaSetup(version);
        ProductPriceQuoteBo input = request == null ? new ProductPriceQuoteBo() : request;
        validateInput(input);
        Map<String, String> selections = normalizeSelections(setup, input.getSelectedOptionValues());
        ProductFormulaSimulationVo simulation = simulate(saleProduct, setup, input, selections);
        if (!"PASS".equals(simulation.getStatus())) {
            throw ServiceException.ofMessageKey(StringUtils.blankToDefault(simulation.getMessage(), "product.priceSetting.quoteInvalid"));
        }
        List<ProductPriceMaterial> priceMaterials = materialMapper.selectList(activeQuery(ProductPriceMaterial.class)
            .eq("price_setting_id", setting.getPriceSettingId()));
        Map<Long, ProductPriceMaterial> priceByMaterialId = priceMaterials.stream()
            .filter(row -> row.getMaterialId() != null)
            .collect(Collectors.toMap(ProductPriceMaterial::getMaterialId, Function.identity(), (left, right) -> left));
        Map<String, ProductPriceMaterial> priceByCode = priceMaterials.stream()
            .filter(row -> StringUtils.isNotBlank(row.getMaterialCode()))
            .collect(Collectors.toMap(ProductPriceMaterial::getMaterialCode, Function.identity(), (left, right) -> left));
        Map<Long, List<ProductPriceMaterialRule>> rulesByMaterial = ruleMapper.selectList(activeQuery(ProductPriceMaterialRule.class)
                .eq("price_setting_id", setting.getPriceSettingId()).orderByAsc("sort_order", "material_rule_id"))
            .stream().collect(Collectors.groupingBy(ProductPriceMaterialRule::getPriceMaterialId));
        Map<String, Object> baseContext = priceContext(saleProduct, setup, input, selections);
        List<ProductPriceQuoteItemVo> items = simulation.getItems().stream()
            .map(item -> quoteItem(item, priceByMaterialId, priceByCode, rulesByMaterial, baseContext)).toList();
        BigDecimal singleAmount = items.stream().map(ProductPriceQuoteItemVo::getAmount)
            .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        int quantity = input.getOrderQuantity() == null ? 1 : input.getOrderQuantity();
        ProductPriceQuoteVo result = new ProductPriceQuoteVo();
        result.setSaleProductId(saleProduct.getSaleProductId());
        result.setFormulaVersionId(version.getVersionId());
        result.setCurrencyCode(setting.getCurrencyCode());
        result.setOrderQuantity(quantity);
        result.setSingleAmount(singleAmount);
        result.setTotalAmount(singleAmount.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP));
        result.setItems(items);
        return result;
    }

    private void validateInput(ProductPriceQuoteBo input) {
        if (input.getOrderWidth() == null || input.getOrderWidth().signum() <= 0
            || input.getOrderHeight() == null || input.getOrderHeight().signum() <= 0
            || input.getOrderQuantity() == null || input.getOrderQuantity() <= 0) {
            throw ServiceException.ofMessageKey("product.priceSetting.quoteInvalid");
        }
    }

    private ProductFormulaSimulationVo simulate(ProductSaleProduct saleProduct, ProductFormulaSetupVo setup,
                                                ProductPriceQuoteBo input, Map<String, String> selections) {
        ProductFormula formula = new ProductFormula();
        formula.setFormulaId(saleProduct.getFormulaId());
        formula.setProductTypeCode(saleProduct.getProductTypeCode());
        formula.setMinWidthInch(saleProduct.getMinWidthInch());
        formula.setMinHeightInch(saleProduct.getMinHeightInch());
        formula.setMaxWidthInch(saleProduct.getMaxWidthInch());
        formula.setMaxHeightInch(saleProduct.getMaxHeightInch());
        ProductFormulaSimulationBo simulationBo = new ProductFormulaSimulationBo();
        simulationBo.setOrderWidth(input.getOrderWidth());
        simulationBo.setOrderHeight(input.getOrderHeight());
        simulationBo.setOrderQuantity(1);
        simulationBo.setSelectedOptionValues(selections);
        return simulationEngine.run(saleProduct.getFormulaId(), formula, setup, simulationBo, null);
    }

    private ProductPriceQuoteItemVo quoteItem(ProductFormulaSimulationItemVo item,
                                              Map<Long, ProductPriceMaterial> priceByMaterialId,
                                              Map<String, ProductPriceMaterial> priceByCode,
                                              Map<Long, List<ProductPriceMaterialRule>> rulesByMaterial,
                                              Map<String, Object> baseContext) {
        ProductPriceMaterial material = item.getMaterialId() == null ? null : priceByMaterialId.get(item.getMaterialId());
        if (material == null) {
            material = priceByCode.get(item.getMaterialCode());
        }
        if (material == null) {
            throw ServiceException.ofMessageKey("product.priceSetting.materialPriceMissing");
        }
        ProductPriceMaterialRule rule = matchRule(rulesByMaterial.getOrDefault(material.getPriceMaterialId(), List.of()), baseContext);
        if (rule == null) {
            throw ServiceException.ofMessageKey("product.priceSetting.defaultMaterialPriceRequired");
        }
        Map<String, Object> context = new HashMap<>(baseContext);
        context.put("unitPrice", rule.getUnitPrice() == null ? 0D : rule.getUnitPrice().doubleValue());
        context.put("usageQty", item.getUsageQty() == null ? 0D : item.getUsageQty().doubleValue());
        BigDecimal amount;
        try {
            amount = ProductPriceExpressionValidator.evaluatePrice(rule.getPriceFormula(), context).setScale(2, RoundingMode.HALF_UP);
        } catch (RuntimeException ex) {
            throw ServiceException.ofMessageKey("product.priceSetting.priceFormulaInvalid");
        }
        ProductPriceQuoteItemVo result = new ProductPriceQuoteItemVo();
        result.setPriceMaterialId(material.getPriceMaterialId());
        result.setMaterialId(material.getMaterialId());
        result.setMaterialCode(material.getMaterialCode());
        result.setMaterialNameCn(material.getMaterialNameCn());
        result.setAttributeGroupNameCn(material.getAttributeGroupNameCn());
        result.setUnitCode(material.getUnitCode());
        result.setUsageQty(item.getUsageQty());
        result.setUnitPrice(rule.getUnitPrice());
        result.setMatchedConditionText(rule.getConditionText());
        result.setPriceFormula(rule.getPriceFormula());
        result.setAmount(amount);
        return result;
    }

    private ProductPriceMaterialRule matchRule(List<ProductPriceMaterialRule> rules, Map<String, Object> context) {
        List<ProductPriceMaterialRule> sorted = rules.stream()
            .sorted(Comparator.comparing(ProductPriceMaterialRule::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
            .toList();
        for (ProductPriceMaterialRule rule : sorted) {
            if (Boolean.TRUE.equals(rule.getDefaultRuleFlag()) || "DEFAULT".equals(rule.getConditionType())) {
                continue;
            }
            try {
                if (ProductPriceExpressionValidator.evaluateCondition(rule.getConditionExpression(), context)) {
                    return rule;
                }
            } catch (RuntimeException ex) {
                throw ServiceException.ofMessageKey("product.priceSetting.conditionInvalid");
            }
        }
        return sorted.stream().filter(rule -> Boolean.TRUE.equals(rule.getDefaultRuleFlag())
            || "DEFAULT".equals(rule.getConditionType())).findFirst().orElse(null);
    }

    private Map<String, Object> priceContext(ProductSaleProduct product, ProductFormulaSetupVo setup,
                                             ProductPriceQuoteBo input, Map<String, String> selections) {
        double width = input.getOrderWidth().doubleValue();
        double drop = input.getOrderHeight().doubleValue();
        double widthCm = width * 2.54D;
        double dropCm = drop * 2.54D;
        Map<String, Object> context = new HashMap<>();
        context.put("width", width);
        context.put("drop", drop);
        context.put("widthCm", widthCm);
        context.put("dropCm", dropCm);
        context.put("areaM2", widthCm * dropCm / 10000D);
        context.put("areaSqft", width * drop / 144D);
        context.put("orderWidthIn", width);
        context.put("orderHeightIn", drop);
        context.put("orderWidthCm", widthCm);
        context.put("orderHeightCm", dropCm);
        context.put("orderAreaM2", widthCm * dropCm / 10000D);
        context.put("productType", product.getProductTypeCode());
        addOptionContext(context, setup, selections);
        return context;
    }

    private void addOptionContext(Map<String, Object> context, ProductFormulaSetupVo setup, Map<String, String> selections) {
        Map<String, ProductFormulaOptionVo> options = setup.getOptions().stream()
            .collect(Collectors.toMap(ProductFormulaOptionVo::getOptionCode, Function.identity(), (left, right) -> left));
        Map<String, ProductFormulaOptionValueVo> values = setup.getOptionValues().stream()
            .collect(Collectors.toMap(row -> row.getOptionCode() + "|" + row.getValueCode(), Function.identity(), (left, right) -> left));
        selections.forEach((optionCode, selected) -> {
            context.put("option_" + optionCode, selected);
            ProductFormulaOptionVo option = options.get(optionCode);
            if (option == null || StringUtils.isBlank(option.getOptionRefKey())) {
                return;
            }
            String refs = ProductFormulaSimulationSelections.selectedValues(selected).stream()
                .map(code -> values.get(optionCode + "|" + code)).filter(Objects::nonNull)
                .map(value -> StringUtils.blankToDefault(value.getValueRefKey(), value.getValueCode()))
                .collect(Collectors.joining(","));
            context.put("option_" + option.getOptionRefKey(), refs);
        });
    }

    private Map<String, String> normalizeSelections(ProductFormulaSetupVo setup, Map<String, String> source) {
        Map<String, String> input = source == null ? Map.of() : source;
        Map<String, ProductFormulaOptionVo> optionsByRef = setup.getOptions().stream()
            .filter(row -> StringUtils.isNotBlank(row.getOptionRefKey()))
            .collect(Collectors.toMap(ProductFormulaOptionVo::getOptionRefKey, Function.identity(), (left, right) -> left));
        Map<String, ProductFormulaOptionValueVo> valuesByRef = setup.getOptionValues().stream()
            .filter(row -> StringUtils.isNotBlank(row.getValueRefKey()))
            .collect(Collectors.toMap(ProductFormulaOptionValueVo::getValueRefKey, Function.identity(), (left, right) -> left));
        Map<String, String> result = new LinkedHashMap<>();
        input.forEach((key, value) -> {
            ProductFormulaOptionVo option = optionsByRef.get(key);
            String optionCode = option == null ? key : option.getOptionCode();
            String valueCodes = ProductFormulaSimulationSelections.selectedValues(value).stream()
                .map(token -> valuesByRef.containsKey(token) ? valuesByRef.get(token).getValueCode() : token)
                .collect(Collectors.joining(","));
            result.put(optionCode, valueCodes);
        });
        return result;
    }
}
