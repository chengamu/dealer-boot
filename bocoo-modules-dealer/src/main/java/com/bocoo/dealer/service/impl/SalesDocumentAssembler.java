package com.bocoo.dealer.service.impl;

import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.domain.vo.SalesDocumentItemVo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;
import com.bocoo.dealer.mapper.SalesDocumentEventMapper;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class SalesDocumentAssembler extends DealerServiceSupport {
    private final SalesDocumentItemMapper itemMapper;
    private final SalesDocumentEventMapper eventMapper;
    private final SalesDocumentJson json;

    void fill(SalesDocumentVo vo) {
        if (vo == null) return;
        List<SalesDocumentItem> items = ignoreTenant(() -> itemMapper.selectList(this.<SalesDocumentItem>active()
            .eq("sales_document_id", vo.getSalesDocumentId()).eq("tenant_id", vo.getTenantId())
            .orderByAsc("line_no", "sort_order")));
        vo.setItems(items.stream().map(this::itemVo).toList());
        vo.setItemCount(items.size());
        vo.setEvents(ignoreTenant(() -> eventMapper.selectVoList(this.<com.bocoo.dealer.domain.entity.SalesDocumentEvent>active()
            .eq("sales_document_id", vo.getSalesDocumentId()).eq("tenant_id", vo.getTenantId())
            .orderByDesc("occurred_time", "sales_event_id"))));
    }

    SalesDocumentItemVo itemVo(SalesDocumentItem item) {
        SalesDocumentItemVo vo = MapstructUtils.convert(item, SalesDocumentItemVo.class);
        if (vo != null) vo.setSelectedOptionValues(json.selections(item.getSelectedOptionsJson()));
        return vo;
    }
}
