package com.bocoo.dealer.service.impl;

import com.bocoo.dealer.domain.vo.SalesDocumentItemMetricsVo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class SalesDocumentMetricsLoader extends DealerServiceSupport {
    private final SalesDocumentItemMapper itemMapper;

    void fill(List<SalesDocumentVo> rows) {
        if (rows.isEmpty()) return;
        List<Long> tenantIds = rows.stream().map(SalesDocumentVo::getTenantId).distinct().toList();
        List<Long> documentIds = rows.stream().map(SalesDocumentVo::getSalesDocumentId).toList();
        List<SalesDocumentItemMetricsVo> values = ignoreTenant(() ->
            itemMapper.selectItemCounts(tenantIds, documentIds));
        Map<DocumentKey, Integer> counts = values.stream().collect(Collectors.toMap(
            item -> new DocumentKey(item.getTenantId(), item.getSalesDocumentId()),
            SalesDocumentItemMetricsVo::getItemCount));
        rows.forEach(row -> row.setItemCount(counts.getOrDefault(
            new DocumentKey(row.getTenantId(), row.getSalesDocumentId()), 0)));
    }

    private record DocumentKey(Long tenantId, Long documentId) {
    }
}
