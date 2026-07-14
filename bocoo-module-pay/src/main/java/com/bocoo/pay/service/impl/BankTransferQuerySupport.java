package com.bocoo.pay.service.impl;

import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.api.PaymentScopePage;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.domain.vo.BankTransferSummaryVo;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class BankTransferQuerySupport {
    private final PayOrderExtensionMapper extensionMapper;
    private final PayOrderMapper orderMapper;
    private final PaymentDocumentScopeSupport scopes;

    TableDataInfo<BankTransferSummaryVo> pageByIds(PaymentScopePage scopePage) {
        if (scopePage.ids().isEmpty()) return new TableDataInfo<>(List.of(), scopePage.total());
        List<PayOrderExtension> records = TenantContextHolder.callWithIgnore(
            () -> extensionMapper.selectBatchIds(scopePage.ids()));
        Map<Long, PayOrderExtension> recordsById = records.stream()
            .collect(Collectors.toMap(PayOrderExtension::getId, Function.identity()));
        List<PayOrderExtension> orderedRecords = scopePage.ids().stream().map(recordsById::get)
            .filter(java.util.Objects::nonNull).toList();
        List<Long> orderIds = orderedRecords.stream().map(PayOrderExtension::getOrderId).distinct().toList();
        Map<Long, PayOrder> orders = orderIds.isEmpty() ? Map.of() : TenantContextHolder.callWithIgnore(
            () -> orderMapper.selectBatchIds(orderIds)).stream()
            .collect(Collectors.toMap(PayOrder::getId, Function.identity()));
        List<Long> documentIds = orderedRecords.stream().map(row -> orders.get(row.getOrderId()))
            .filter(java.util.Objects::nonNull).map(PayOrder::getSalesDocumentId).distinct().toList();
        var facts = scopes.required().resolveFacts(documentIds);
        List<BankTransferSummaryVo> rows = orderedRecords.stream().map(row -> {
            PayOrder order = orders.get(row.getOrderId());
            return order == null ? null : BankTransferSummaryVo.from(row, order, facts.get(order.getSalesDocumentId()));
        }).filter(java.util.Objects::nonNull).toList();
        return new TableDataInfo<>(rows, scopePage.total());
    }
}
