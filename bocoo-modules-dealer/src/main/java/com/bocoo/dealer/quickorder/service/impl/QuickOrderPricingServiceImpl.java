package com.bocoo.dealer.quickorder.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderItemBo;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderItemVo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderVo;
import com.bocoo.dealer.quickorder.mapper.QuickOrderItemMapper;
import com.bocoo.dealer.quickorder.mapper.QuickOrderMapper;
import com.bocoo.dealer.quickorder.runtime.*;
import com.bocoo.dealer.quickorder.service.QuickOrderPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuickOrderPricingServiceImpl implements QuickOrderPricingService {
    private final QuickOrderMapper mapper;
    private final QuickOrderItemMapper itemMapper;
    private final QuickOrderAccess access;
    private final QuickOrderPricingEngine engine;
    private final QuickOrderRecalculator recalculator;
    private final QuickOrderQueryServiceImpl queryService;

    @Override
    @Lock4j(name = "quick-order", keys = {"#quickOrderId"})
    @Transactional(rollbackFor = Exception.class)
    public QuickOrderItemVo calculateItem(Long quickOrderId, QuickOrderItemBo bo) {
        QuickOrder order = access.loadDraft(quickOrderId);
        QuickOrderItem previous = existing(order, bo.getQuickOrderItemId());
        QuickOrderPricingResult result = engine.calculate(bo, order.getTenantId());
        QuickOrderItem item = result.item(); item.setQuickOrderId(quickOrderId); item.setTenantId(order.getTenantId());
        item.setLineNo(previous == null ? nextLine(order) : previous.getLineNo());
        if (item.getSortOrder() == null) item.setSortOrder(item.getLineNo() * 10);
        if (previous == null) itemMapper.insert(item); else itemMapper.updateById(item);
        refreshTotals(order, result.currencyCode());
        return queryService.queryById(quickOrderId).getItems().stream()
            .filter(row -> row.getQuickOrderItemId().equals(item.getQuickOrderItemId())).findFirst().orElseThrow();
    }

    @Override
    @Lock4j(name = "quick-order", keys = {"#quickOrderId"})
    @Transactional(rollbackFor = Exception.class)
    public QuickOrderVo calculateAll(Long quickOrderId) {
        QuickOrder order = access.loadDraft(quickOrderId);
        recalculator.recalculate(order);
        return queryService.queryById(quickOrderId);
    }

    private QuickOrderItem existing(QuickOrder order, Long itemId) {
        if (itemId == null) return null;
        QuickOrderItem row = itemMapper.selectOne(new QueryWrapper<QuickOrderItem>().eq("del_flag", "0")
            .eq("tenant_id", order.getTenantId()).eq("quick_order_id", order.getQuickOrderId())
            .eq("quick_order_item_id", itemId), false);
        if (row == null) throw ServiceException.ofMessageKey("dealer.quickOrder.item.notFound");
        return row;
    }

    private int nextLine(QuickOrder order) {
        return access.items(order.getQuickOrderId(), order.getTenantId()).stream()
            .map(QuickOrderItem::getLineNo).filter(java.util.Objects::nonNull).max(Integer::compareTo).orElse(0) + 1;
    }

    private void refreshTotals(QuickOrder order, String currency) {
        List<QuickOrderItem> items = access.items(order.getQuickOrderId(), order.getTenantId());
        QuickOrderTotals.apply(order, items, sameCurrency(order.getCurrencyCode(), currency)); mapper.updateById(order);
    }

    private String sameCurrency(String left, String right) {
        if (left == null) return right;
        if (right == null || left.equals(right)) return left;
        throw ServiceException.ofMessageKey("dealer.quickOrder.currency.mismatch");
    }
}
