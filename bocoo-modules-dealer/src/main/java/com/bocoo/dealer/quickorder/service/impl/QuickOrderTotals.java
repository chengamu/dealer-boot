package com.bocoo.dealer.quickorder.service.impl;

import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

final class QuickOrderTotals {
    private QuickOrderTotals() {
    }

    static void apply(QuickOrder order, List<QuickOrderItem> items, String currencyCode) {
        BigDecimal list = sum(items, QuickOrderItem::getListAmount);
        BigDecimal discount = sum(items, QuickOrderItem::getDiscountAmount);
        BigDecimal product = sum(items, QuickOrderItem::getProductAmount);
        BigDecimal shipping = sum(items, QuickOrderItem::getShippingAmount);
        order.setCurrencyCode(currencyCode); order.setListAmount(money(list));
        order.setDiscountAmount(money(discount)); order.setProductAmount(money(product));
        order.setShippingAmount(money(shipping)); order.setTaxAmount(money(order.getTaxAmount()));
        order.setTotalAmount(money(product.add(shipping).add(order.getTaxAmount())));
    }

    private static BigDecimal sum(List<QuickOrderItem> items,
                                  java.util.function.Function<QuickOrderItem, BigDecimal> getter) {
        return items.stream().map(getter).filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
