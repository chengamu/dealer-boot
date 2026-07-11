package com.bocoo.dealer.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.bo.SalesDocumentItemBo;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.product.domain.bo.ProductPriceQuoteBo;
import com.bocoo.product.domain.vo.*;
import com.bocoo.system.domain.vo.MerchantProfileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static com.bocoo.product.service.ProductShippingRuleMatcher.match;

@Component
@RequiredArgsConstructor
class SalesDocumentCalculator {
    private static final BigDecimal SQ_IN_PER_SQ_FT = new BigDecimal("144");
    private final SalesCatalogFacade catalog;
    private final SalesDiscountResolver discounts;
    private final SalesDocumentJson json;

    CalculatedLine calculate(SalesDocumentItemBo bo, Long tenantId) {
        validate(bo);
        ProductPriceSetupVo setup = catalog.setup(bo.getSaleProductId());
        ProductSaleProductVo product = setup.getSaleProduct();
        if (product == null || !"ENABLED".equals(product.getStatus())) {
            throw ServiceException.ofMessageKey("dealer.sales.productUnavailable");
        }
        ProductPriceQuoteBo request = new ProductPriceQuoteBo();
        request.setOrderWidth(bo.getOrderWidthInch());
        request.setOrderHeight(bo.getOrderHeightInch());
        request.setOrderQuantity(bo.getQuantity());
        request.setSelectedOptionValues(bo.getSelectedOptionValues());
        ProductPriceQuoteVo price = catalog.quote(bo.getSaleProductId(), request);
        MerchantProfileVo profile = discounts.profile(tenantId);
        BigDecimal rate = discounts.resolve(profile, product.getCategoryId(), product.getProductTypeCode());
        BigDecimal listAmount = money(price.getTotalAmount());
        BigDecimal productAmount = money(listAmount.multiply(rate));
        ShippingMatch shipping = shipping(price.getCurrencyCode(), bo, motorized(setup, bo.getSelectedOptionValues()));
        SalesDocumentItem item = new SalesDocumentItem();
        item.setRoomLocation(bo.getRoomLocation());
        item.setSaleProductId(product.getSaleProductId());
        item.setSaleProductCode(product.getSaleProductCode());
        item.setSaleProductName(product.getSaleProductName());
        item.setCategoryId(product.getCategoryId());
        item.setCategoryCode(product.getCategoryCode());
        item.setCategoryNameCn(product.getCategoryNameCn());
        item.setProductTypeCode(product.getProductTypeCode());
        item.setProductTypeNameCn(product.getProductTypeNameCn());
        item.setFormulaId(product.getFormulaId());
        item.setFormulaVersionId(price.getFormulaVersionId());
        item.setFormulaVersionLabel(product.getFormulaVersionLabel());
        item.setOrderWidthInch(bo.getOrderWidthInch());
        item.setOrderHeightInch(bo.getOrderHeightInch());
        item.setQuantity(bo.getQuantity());
        item.setSelectedOptionsJson(json.write(bo.getSelectedOptionValues()));
        item.setConfigurationSummary(summary(setup, bo.getSelectedOptionValues()));
        item.setCalculationStatus("PASS");
        item.setListUnitAmount(money(price.getSingleAmount()));
        item.setListAmount(listAmount);
        item.setDiscountRate(rate);
        item.setUnitAmount(money(productAmount.divide(BigDecimal.valueOf(bo.getQuantity()), 2, RoundingMode.HALF_UP)));
        item.setProductAmount(productAmount);
        item.setShippingTemplateId(shipping.templateId());
        item.setShippingAmount(money(shipping.amount().multiply(BigDecimal.valueOf(bo.getQuantity()))));
        item.setLineAmount(money(productAmount.add(item.getShippingAmount())));
        item.setBomSnapshotJson(json.write(price.getItems()));
        item.setPricingSnapshotJson(json.write(price));
        item.setShippingSnapshotJson(json.write(shipping));
        item.setSortOrder(bo.getSortOrder());
        item.setRemark(bo.getRemark());
        item.setDelFlag("0");
        return new CalculatedLine(item, price.getCurrencyCode());
    }

    private ShippingMatch shipping(String currency, SalesDocumentItemBo bo, boolean motorized) {
        ProductShippingTemplateVo template = catalog.shippingTemplate(currency);
        if (template == null) throw ServiceException.ofMessageKey("dealer.sales.shippingTemplateMissing");
        BigDecimal area = bo.getOrderWidthInch().multiply(bo.getOrderHeightInch())
            .divide(SQ_IN_PER_SQ_FT, 4, RoundingMode.HALF_UP);
        String feeCode = motorized ? "MOTORIZED" : "MANUAL";
        ProductShippingTemplateRuleVo rule = match(template.getRules(), feeCode, area);
        if (rule == null) throw ServiceException.ofMessageKey("dealer.sales.shippingRuleMissing");
        return new ShippingMatch(template.getShippingTemplateId(), template.getTemplateCode(),
            rule.getShippingTemplateRuleId(), feeCode, money(rule.getFeeAmount()));
    }

    private boolean motorized(ProductPriceSetupVo setup, Map<String, String> selections) {
        if (selections == null || setup.getFormulaOptionMaterials() == null) return false;
        Map<Long, ProductFormulaMaterialVo> materials = setup.getFormulaMaterials().stream()
            .collect(java.util.stream.Collectors.toMap(ProductFormulaMaterialVo::getMaterialId, row -> row));
        return setup.getFormulaOptionMaterials().stream()
            .filter(link -> selections.containsValue(link.getValueCode()) || selections.containsValue(link.getValueRefKey()))
            .map(link -> materials.get(link.getMaterialId()))
            .anyMatch(row -> row != null && "MOTOR".equalsIgnoreCase(row.getMaterialTypeCode()));
    }

    private String summary(ProductPriceSetupVo setup, Map<String, String> selections) {
        if (selections == null || selections.isEmpty()) return "";
        java.util.List<ProductFormulaOptionVo> options = setup.getFormulaOptions() == null ? java.util.List.of() : setup.getFormulaOptions();
        java.util.List<ProductFormulaOptionValueVo> values = setup.getFormulaOptionValues() == null ? java.util.List.of() : setup.getFormulaOptionValues();
        Map<String, String> optionNames = options.stream().collect(java.util.stream.Collectors.toMap(
            ProductFormulaOptionVo::getOptionCode, row -> row.getOptionNameEn() == null ? row.getOptionNameCn() : row.getOptionNameEn(), (a, b) -> a));
        Map<String, String> valueNames = values.stream().collect(java.util.stream.Collectors.toMap(
            row -> row.getOptionCode() + "\u0000" + row.getValueCode(),
            row -> row.getValueNameEn() == null ? row.getValueNameCn() : row.getValueNameEn(), (a, b) -> a));
        return selections.entrySet().stream().map(entry -> optionNames.getOrDefault(entry.getKey(), entry.getKey()) + "="
                + valueNames.getOrDefault(entry.getKey() + "\u0000" + entry.getValue(), entry.getValue()))
            .collect(java.util.stream.Collectors.joining(" / "));
    }

    private void validate(SalesDocumentItemBo bo) {
        if (bo == null || bo.getSaleProductId() == null || bo.getOrderWidthInch() == null
            || bo.getOrderHeightInch() == null || bo.getQuantity() == null || bo.getQuantity() < 1
            || bo.getOrderWidthInch().signum() <= 0 || bo.getOrderHeightInch().signum() <= 0) {
            throw ServiceException.ofMessageKey("dealer.sales.itemIncomplete");
        }
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    record ShippingMatch(Long templateId, String templateCode, Long ruleId, String feeCode, BigDecimal amount) {}
    record CalculatedLine(SalesDocumentItem item, String currencyCode) {}
}
