package com.bocoo.dealer.quickorder.runtime;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderItemBo;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;
import com.bocoo.merchant.service.CustomerQuoteCatalogService;
import com.bocoo.product.domain.bo.ProductPriceQuoteBo;
import com.bocoo.product.domain.vo.ProductPriceQuoteVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.domain.vo.ProductSaleProductVo;
import com.bocoo.system.domain.vo.MerchantProfileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QuickOrderPricingEngine {
    private final CustomerQuoteCatalogService catalogService;
    private final QuickOrderOptionResolver optionResolver;
    private final QuickOrderShippingCalculator shippingCalculator;
    private final QuickOrderDiscountResolver discountResolver;
    private final QuickOrderJson json;

    public QuickOrderPricingSession openSession(Long tenantId) {
        return discountResolver.session(tenantId);
    }

    public QuickOrderPricingResult calculate(QuickOrderItemBo bo, Long tenantId) {
        return calculate(bo, openSession(tenantId));
    }

    public QuickOrderPricingResult calculate(QuickOrderItemBo bo, QuickOrderPricingSession session) {
        validate(bo);
        ProductPriceSetupVo setup = catalogService.querySetup(bo.getSaleProductId());
        ProductSaleProductVo product = setup.getSaleProduct();
        if (product == null || !"ENABLED".equalsIgnoreCase(product.getStatus())) {
            throw ServiceException.ofMessageKey("dealer.quickOrder.product.unavailable");
        }
        QuickOrderOptionResolution options = optionResolver.resolve(setup, bo.getSelectedOptionValues());
        ProductPriceQuoteVo price = catalogService.quote(bo.getSaleProductId(), priceRequest(bo, options));
        QuickOrderShippingResult shipping = shippingCalculator.calculate(price.getCurrencyCode(),
            bo.getOrderWidthInch(), bo.getOrderHeightInch(), options.motorized());
        MerchantProfileVo profile = session.profile();
        BigDecimal rate = discountResolver.resolve(session, product.getCategoryId(), product.getProductTypeCode());
        return new QuickOrderPricingResult(item(bo, product, price, options, shipping, rate),
            price.getCurrencyCode(), profile);
    }

    private QuickOrderItem item(QuickOrderItemBo bo, ProductSaleProductVo product, ProductPriceQuoteVo price,
                                QuickOrderOptionResolution options, QuickOrderShippingResult shipping,
                                BigDecimal rate) {
        QuickOrderItem row = base(bo);
        row.setSaleProductCode(product.getSaleProductCode()); row.setSaleProductName(product.getSaleProductName());
        row.setCategoryId(product.getCategoryId()); row.setCategoryCode(product.getCategoryCode());
        row.setCategoryNameCn(product.getCategoryNameCn()); row.setProductTypeCode(product.getProductTypeCode());
        row.setProductTypeNameCn(product.getProductTypeNameCn()); row.setFormulaId(product.getFormulaId());
        row.setFormulaVersionId(price.getFormulaVersionId()); row.setFormulaVersionLabel(product.getFormulaVersionLabel());
        row.setSelectedOptionsJson(json.write(options.selections()));
        row.setConfigurationSummaryCn(options.summaryCn()); row.setConfigurationSummaryEn(options.summaryEn());
        row.setCalculationStatus("PASS"); row.setListUnitAmount(money(price.getSingleAmount()));
        row.setListAmount(money(price.getTotalAmount())); row.setDiscountRate(rate);
        row.setProductAmount(money(row.getListAmount().multiply(rate)));
        row.setDiscountAmount(money(row.getListAmount().subtract(row.getProductAmount())));
        row.setUnitAmount(money(row.getProductAmount().divide(BigDecimal.valueOf(bo.getQuantity()),
            2, RoundingMode.HALF_UP)));
        row.setShippingTemplateId(shipping.templateId());
        row.setUnitShippingAmount(money(shipping.unitAmount()));
        row.setShippingAmount(money(shipping.unitAmount().multiply(BigDecimal.valueOf(bo.getQuantity()))));
        row.setLineAmount(money(row.getProductAmount().add(row.getShippingAmount())));
        row.setBomSnapshotJson(json.write(price.getItems()));
        row.setPricingSnapshotJson(pricingSnapshot(price, rate, row));
        row.setShippingSnapshotJson(shippingSnapshot(shipping, row));
        return row;
    }

    private QuickOrderItem base(QuickOrderItemBo bo) {
        QuickOrderItem row = new QuickOrderItem();
        row.setQuickOrderItemId(bo.getQuickOrderItemId()); row.setRoomLocation(bo.getRoomLocation());
        row.setSaleProductId(bo.getSaleProductId()); row.setOrderWidthInch(bo.getOrderWidthInch());
        row.setOrderHeightInch(bo.getOrderHeightInch()); row.setQuantity(bo.getQuantity());
        row.setSortOrder(bo.getSortOrder()); row.setRemark(bo.getRemark()); row.setDelFlag("0");
        return row;
    }

    private ProductPriceQuoteBo priceRequest(QuickOrderItemBo bo, QuickOrderOptionResolution options) {
        ProductPriceQuoteBo request = new ProductPriceQuoteBo();
        request.setOrderWidth(bo.getOrderWidthInch()); request.setOrderHeight(bo.getOrderHeightInch());
        request.setOrderQuantity(bo.getQuantity()); request.setSelectedOptionValues(options.selections());
        return request;
    }

    private String pricingSnapshot(ProductPriceQuoteVo price, BigDecimal rate, QuickOrderItem item) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("price", price); snapshot.put("discountRate", rate);
        snapshot.put("discountAmount", item.getDiscountAmount()); snapshot.put("productAmount", item.getProductAmount());
        return json.write(snapshot);
    }

    private String shippingSnapshot(QuickOrderShippingResult shipping, QuickOrderItem item) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("shipping", shipping); snapshot.put("shippingAmount", item.getShippingAmount());
        return json.write(snapshot);
    }

    private void validate(QuickOrderItemBo bo) {
        if (bo == null || bo.getSaleProductId() == null || bo.getOrderWidthInch() == null
            || bo.getOrderHeightInch() == null || bo.getOrderWidthInch().signum() <= 0
            || bo.getOrderHeightInch().signum() <= 0 || bo.getQuantity() == null || bo.getQuantity() < 1) {
            throw ServiceException.ofMessageKey("dealer.quickOrder.item.incomplete");
        }
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
