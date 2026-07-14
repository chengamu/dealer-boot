package com.bocoo.dealer.quickorder.service.impl;

import com.bocoo.dealer.quickorder.domain.vo.QuickOrderItemSummaryVo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderVo;
import com.bocoo.dealer.quickorder.mapper.QuickOrderItemMapper;
import com.bocoo.dealer.quickorder.runtime.QuickOrderJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuickOrderAssemblerTest {
    @Mock private QuickOrderItemMapper itemMapper;
    @Mock private QuickOrderJson json;

    @Test
    void fillSummariesUsesOneAggregateQueryAndDefaultsMissingRowsToZero() {
        QuickOrderVo first = order(1L, 101L);
        QuickOrderVo second = order(2L, 102L);
        when(itemMapper.selectSummaries(List.of(101L, 102L))).thenReturn(List.of(
            QuickOrderItemSummaryVo.builder().tenantId(1L).quickOrderId(101L)
                .itemTypeCount(3).totalQuantity(8).build()));
        QuickOrderAssembler assembler = new QuickOrderAssembler(itemMapper, json);

        assembler.fillSummaries(List.of(first, second));

        assertThat(first.getItemTypeCount()).isEqualTo(3);
        assertThat(first.getTotalQuantity()).isEqualTo(8);
        assertThat(second.getItemTypeCount()).isZero();
        assertThat(second.getTotalQuantity()).isZero();
        verify(itemMapper).selectSummaries(List.of(101L, 102L));
    }

    private QuickOrderVo order(Long tenantId, Long quickOrderId) {
        QuickOrderVo row = new QuickOrderVo();
        row.setTenantId(tenantId);
        row.setQuickOrderId(quickOrderId);
        return row;
    }
}
