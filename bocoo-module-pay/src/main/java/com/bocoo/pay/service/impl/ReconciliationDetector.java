package com.bocoo.pay.service.impl;

import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.json.utils.JsonUtils;
import com.bocoo.pay.api.PaymentDocumentFacts;
import com.bocoo.pay.api.PaymentReconciliationFacts;
import com.bocoo.pay.api.PaymentReconciliationFactsResolver;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayReconciliationCase;
import com.bocoo.pay.enums.PayOrderStatus;
import com.bocoo.pay.enums.ReconciliationActionType;
import com.bocoo.pay.enums.ReconciliationAnomalyType;
import com.bocoo.pay.enums.ReconciliationCaseStatus;
import com.bocoo.pay.enums.ReconciliationSeverity;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.service.PayOperatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
class ReconciliationDetector {
    private final PayOrderMapper orderMapper;
    private final ObjectProvider<PaymentReconciliationFactsResolver> factResolvers;
    private final PaymentDocumentScopeSupport documentScopes;
    private final ReconciliationCaseRepository cases;
    private final ReconciliationActionWriter actions;
    private final PayOperatorContext operator;

    @Transactional(rollbackFor = Exception.class)
    PayReconciliationCase scan(Long payOrderId, ReconciliationActionType actionType, String reason) {
        PayOrder order = TenantContextHolder.callWithIgnore(() -> orderMapper.selectById(payOrderId));
        if (order == null) throw new ServiceException("Payment order does not exist");
        PaymentReconciliationFacts facts = resolver().resolve(order.getSalesDocumentId());
        ReconciliationFinding finding = diagnose(order, facts);
        if (finding == null) {
            resolveOpen(order.getId(), reason);
            return null;
        }
        PaymentDocumentFacts document = documentScopes.required().resolveFacts(List.of(order.getSalesDocumentId()))
            .get(order.getSalesDocumentId());
        PayReconciliationCase row = cases.upsert(order, document, finding);
        actions.write(row, actionType, finding.expectedJson(), finding.actualJson(), "SUCCESS", finding.code(),
            finding.message(), reason);
        return row;
    }

    private ReconciliationFinding diagnose(PayOrder order, PaymentReconciliationFacts facts) {
        String actual = JsonUtils.toJsonString(Map.of("payOrderId", order.getId(), "status", order.getStatus(),
            "price", order.getPrice(), "currency", order.getCurrency()));
        String expected = JsonUtils.toJsonString(facts);
        if (facts == null || !facts.isExists()) {
            return finding(ReconciliationAnomalyType.PAYMENT_LINK_MISSING, ReconciliationSeverity.WARNING,
                "sales_document_missing", "Sales document is missing", expected, actual);
        }
        if (facts.getPayOrderId() == null || !facts.getPayOrderId().equals(order.getId())) {
            return finding(ReconciliationAnomalyType.PAYMENT_LINK_MISSING, ReconciliationSeverity.WARNING,
                "payment_link_mismatch", "Sales document links another payment order", expected, actual);
        }
        if (!sameMoney(order, facts) || !order.getCurrency().equalsIgnoreCase(facts.getCurrency())) {
            return finding(ReconciliationAnomalyType.PAYMENT_FACT_MISMATCH, ReconciliationSeverity.CRITICAL,
                "payment_fact_mismatch", "Payment amount or currency differs from the sales document", expected, actual);
        }
        if (PayOrderStatus.SUCCESS.getStatus().equals(order.getStatus()) && !"PAID".equals(facts.getPaymentStatus())) {
            return finding(ReconciliationAnomalyType.PAYMENT_SUCCESS_ORDER_UNPAID, ReconciliationSeverity.WARNING,
                "order_payment_not_written", "Payment succeeded but the sales document is unpaid", expected, actual);
        }
        return null;
    }

    private boolean sameMoney(PayOrder order, PaymentReconciliationFacts facts) {
        if (facts.getTotalAmount() == null) return false;
        try {
            return facts.getTotalAmount().movePointRight(2).longValueExact() == order.getPrice();
        } catch (ArithmeticException ex) {
            return false;
        }
    }

    private void resolveOpen(Long payOrderId, String reason) {
        for (PayReconciliationCase row : cases.openByPayment(payOrderId)) {
            String before = row.getActualSnapshotJson();
            cases.finish(row, ReconciliationCaseStatus.RESOLVED, "facts_consistent", operator.userId(), operator.username());
            actions.write(row, ReconciliationActionType.AUTO_RESOLVE, before, before, "SUCCESS", "facts_consistent",
                "Payment and sales document facts are consistent", reason);
        }
    }

    private PaymentReconciliationFactsResolver resolver() {
        PaymentReconciliationFactsResolver resolver = factResolvers.getIfUnique();
        if (resolver == null) throw new ServiceException("Payment reconciliation facts resolver is unavailable");
        return resolver;
    }

    private ReconciliationFinding finding(ReconciliationAnomalyType type, ReconciliationSeverity severity,
                                           String code, String message, String expected, String actual) {
        return new ReconciliationFinding(type, severity, code, message, expected, actual);
    }
}
