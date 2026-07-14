package com.bocoo.dealer.quickorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderItemSummaryVo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderItemVo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderVo;
import com.bocoo.dealer.quickorder.mapper.QuickOrderItemMapper;
import com.bocoo.dealer.quickorder.runtime.QuickOrderJson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class QuickOrderAssembler {
    private final QuickOrderItemMapper itemMapper;
    private final QuickOrderJson json;

    void fillSummary(QuickOrderVo vo) {
        fillSummary(vo, items(vo));
    }

    void fillSummaries(List<QuickOrderVo> orders) {
        if (orders == null || orders.isEmpty()) return;
        List<Long> ids = orders.stream().map(QuickOrderVo::getQuickOrderId).distinct().toList();
        List<QuickOrderItemSummaryVo> rows = TenantContextHolder.callWithIgnore(() -> itemMapper.selectSummaries(ids));
        Map<SummaryKey, QuickOrderItemSummaryVo> summaries = rows.stream().collect(Collectors.toMap(
            row -> new SummaryKey(row.getTenantId(), row.getQuickOrderId()), Function.identity()));
        orders.forEach(order -> {
            QuickOrderItemSummaryVo summary = summaries.get(new SummaryKey(order.getTenantId(), order.getQuickOrderId()));
            order.setItemTypeCount(summary == null ? 0 : summary.getItemTypeCount());
            order.setTotalQuantity(summary == null ? 0 : summary.getTotalQuantity());
        });
    }

    void fillDetail(QuickOrderVo vo) {
        List<QuickOrderItem> rows = items(vo);
        List<QuickOrderItemVo> items = rows.stream().map(row -> {
            QuickOrderItemVo item = com.bocoo.common.core.utils.MapstructUtils.convert(row, QuickOrderItemVo.class);
            if (item != null) item.setSelectedOptionValues(json.readSelections(row.getSelectedOptionsJson()));
            return item;
        }).toList();
        vo.setItems(items);
        fillSummary(vo, rows);
    }

    private List<QuickOrderItem> items(QuickOrderVo vo) {
        return TenantContextHolder.callWithIgnore(() -> itemMapper.selectList(new QueryWrapper<QuickOrderItem>()
            .eq("del_flag", "0").eq("tenant_id", vo.getTenantId())
            .eq("quick_order_id", vo.getQuickOrderId())
            .orderByAsc("sort_order", "line_no", "quick_order_item_id")));
    }

    private void fillSummary(QuickOrderVo vo, List<QuickOrderItem> items) {
        vo.setItemTypeCount((int) items.stream().map(QuickOrderItem::getSaleProductId).distinct().count());
        vo.setTotalQuantity(items.stream().map(QuickOrderItem::getQuantity).filter(Objects::nonNull)
            .reduce(0, Integer::sum));
    }

    private record SummaryKey(Long tenantId, Long quickOrderId) {
    }
}
