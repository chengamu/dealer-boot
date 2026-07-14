package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.api.PaymentDocumentFacts;
import com.bocoo.pay.api.PaymentScopePage;
import com.bocoo.pay.domain.bo.PaymentOrderQueryBo;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.vo.PaymentOrderSummaryVo;
import com.bocoo.pay.mapper.PayOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class PaymentOrderQuerySupport {
    private final PayOrderMapper mapper;

    TableDataInfo<PaymentOrderSummaryVo> page(PaymentOrderQueryBo query, PageQuery pageQuery,
                                              Collection<Long> documentIds) {
        if (documentIds != null && documentIds.isEmpty()) return new TableDataInfo<>(List.of(), 0);
        Page<PayOrder> page = TenantContextHolder.callWithIgnore(() -> mapper.selectPage(pageQuery.build(),
            wrapper(query, documentIds)));
        return convert(page, Map.of());
    }

    TableDataInfo<PaymentOrderSummaryVo> pageByIds(PaymentScopePage scopePage) {
        if (scopePage == null || scopePage.ids().isEmpty()) {
            return new TableDataInfo<>(List.of(), scopePage == null ? 0 : scopePage.total());
        }
        Map<Long, PayOrder> byId = TenantContextHolder.callWithIgnore(() -> mapper.selectBatchIds(scopePage.ids()))
            .stream().collect(Collectors.toMap(PayOrder::getId, Function.identity()));
        List<PaymentOrderSummaryVo> rows = scopePage.ids().stream().map(byId::get).filter(java.util.Objects::nonNull)
            .map(row -> PaymentOrderSummaryVo.from(row, null)).toList();
        return new TableDataInfo<>(rows, scopePage.total());
    }

    TableDataInfo<PaymentOrderSummaryVo> enrich(TableDataInfo<PaymentOrderSummaryVo> page,
                                                Map<Long, PaymentDocumentFacts> facts) {
        List<PaymentOrderSummaryVo> rows = page.getRows().stream().map(row -> copy(row, facts.get(row.getSalesDocumentId()))).toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    private LambdaQueryWrapper<PayOrder> wrapper(PaymentOrderQueryBo query, Collection<Long> documentIds) {
        return new LambdaQueryWrapper<PayOrder>()
            .in(documentIds != null, PayOrder::getSalesDocumentId, documentIds)
            .ge(query.getBeginTime() != null, PayOrder::getCreateTime, query.getBeginTime())
            .le(query.getEndTime() != null, PayOrder::getCreateTime, query.getEndTime())
            .and(StringUtils.isNotBlank(query.getKeyword()), q -> q.eq(PayOrder::getNo, query.getKeyword())
                .or().eq(PayOrder::getSalesOrderNo, query.getKeyword()))
            .eq(StringUtils.isNotBlank(query.getChannelCode()), PayOrder::getChannelCode, query.getChannelCode())
            .eq(query.getStatus() != null, PayOrder::getStatus, query.getStatus())
            .orderByDesc(PayOrder::getCreateTime).orderByDesc(PayOrder::getId);
    }

    private TableDataInfo<PaymentOrderSummaryVo> convert(Page<PayOrder> page, Map<Long, PaymentDocumentFacts> facts) {
        List<PaymentOrderSummaryVo> rows = page.getRecords().stream()
            .map(row -> PaymentOrderSummaryVo.from(row, facts.get(row.getSalesDocumentId()))).toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    private PaymentOrderSummaryVo copy(PaymentOrderSummaryVo row, PaymentDocumentFacts facts) {
        if (facts == null) return row;
        Long subjectId = "INTERNAL".equals(facts.getBusinessOrigin()) ? facts.getSalesStoreId() : facts.getTenantId();
        return PaymentOrderSummaryVo.builder().payOrderId(row.getPayOrderId()).payOrderNo(row.getPayOrderNo())
            .salesDocumentId(row.getSalesDocumentId()).salesOrderNo(row.getSalesOrderNo())
            .businessOrigin(facts.getBusinessOrigin()).subjectId(subjectId).subjectName(facts.getSubjectName())
            .customerName(row.getCustomerName()).channelCode(row.getChannelCode()).price(row.getPrice())
            .currency(row.getCurrency()).status(row.getStatus()).channelOrderNo(row.getChannelOrderNo())
            .createTime(row.getCreateTime()).successTime(row.getSuccessTime()).build();
    }
}
