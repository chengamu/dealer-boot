package com.bocoo.dealer.quickorder.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderItemBo;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;
import com.bocoo.dealer.quickorder.mapper.QuickOrderItemMapper;
import com.bocoo.dealer.quickorder.mapper.QuickOrderMapper;
import com.bocoo.dealer.quickorder.runtime.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
class QuickOrderRecalculator {
    private final QuickOrderMapper mapper;
    private final QuickOrderItemMapper itemMapper;
    private final QuickOrderAccess access;
    private final QuickOrderPricingEngine engine;
    private final QuickOrderJson json;

    QuickOrderRecalculation recalculate(QuickOrder order) {
        List<QuickOrderItem> current = access.items(order.getQuickOrderId(), order.getTenantId());
        if (current.isEmpty()) throw ServiceException.ofMessageKey("dealer.quickOrder.items.required");
        QuickOrderPricingSession session = engine.openSession(order.getTenantId());
        List<QuickOrderItem> calculated = new ArrayList<>();
        String currency = null;
        for (QuickOrderItem source : current) {
            QuickOrderPricingResult result = engine.calculate(toBo(source), session);
            currency = sameCurrency(currency, result.currencyCode());
            QuickOrderItem item = result.item(); item.setQuickOrderId(order.getQuickOrderId());
            item.setQuickOrderItemId(source.getQuickOrderItemId()); item.setTenantId(order.getTenantId());
            item.setLineNo(source.getLineNo()); itemMapper.updateById(item); calculated.add(item);
        }
        QuickOrderTotals.apply(order, calculated, currency); mapper.updateById(order);
        return new QuickOrderRecalculation(order, calculated, session.profile());
    }

    private QuickOrderItemBo toBo(QuickOrderItem source) {
        QuickOrderItemBo bo = new QuickOrderItemBo(); bo.setQuickOrderItemId(source.getQuickOrderItemId());
        bo.setRoomLocation(source.getRoomLocation()); bo.setSaleProductId(source.getSaleProductId());
        bo.setOrderWidthInch(source.getOrderWidthInch()); bo.setOrderHeightInch(source.getOrderHeightInch());
        bo.setQuantity(source.getQuantity()); bo.setSelectedOptionValues(json.readSelections(source.getSelectedOptionsJson()));
        bo.setSortOrder(source.getSortOrder()); bo.setRemark(source.getRemark()); return bo;
    }

    private String sameCurrency(String left, String right) {
        if (left == null) return right;
        if (left.equals(right)) return left;
        throw ServiceException.ofMessageKey("dealer.quickOrder.currency.mismatch");
    }
}
