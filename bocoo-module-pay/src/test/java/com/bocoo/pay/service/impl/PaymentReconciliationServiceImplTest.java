package com.bocoo.pay.service.impl;

import com.bocoo.pay.domain.entity.PayReconciliationCase;
import com.bocoo.pay.enums.ReconciliationActionType;
import com.bocoo.pay.enums.ReconciliationAnomalyType;
import com.bocoo.pay.mapper.PayReconciliationActionMapper;
import com.bocoo.pay.mapper.PayReconciliationCaseMapper;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PayOrderDetailService;
import com.bocoo.pay.service.PayPalPaymentService;
import com.bocoo.pay.service.PaymentSuccessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentReconciliationServiceImplTest extends PayServiceTestSupport {
    @Mock PayReconciliationCaseMapper caseMapper;
    @Mock PayReconciliationActionMapper actionMapper;
    @Mock ReconciliationCaseRepository cases;
    @Mock ReconciliationDetector detector;
    @Mock ReconciliationActionWriter actions;
    @Mock PayOrderDetailService details;
    @Mock PayPalPaymentService payPalService;
    @Mock PaymentSuccessService successService;
    @Mock PayOperatorContext operator;

    @Test
    void repairOrderDelegatesToExistingCallbackRepairAndRescans() {
        PayReconciliationCase row = new PayReconciliationCase();
        row.setCaseId(7L);
        row.setPayOrderId(9L);
        row.setStatus("OPEN");
        row.setAnomalyType(ReconciliationAnomalyType.PAYMENT_SUCCESS_ORDER_UNPAID.name());
        when(operator.isPlatform()).thenReturn(true);
        when(cases.required(7L)).thenReturn(row);
        when(successService.repair(9L)).thenReturn(true);
        when(detector.scan(9L, ReconciliationActionType.REPAIR_ORDER, "confirmed facts")).thenReturn(row);
        PaymentReconciliationServiceImpl service = service();

        assertThat(service.repairOrder(7L, "confirmed facts").getCaseId()).isEqualTo(7L);
        verify(successService).repair(9L);
        verify(detector).scan(9L, ReconciliationActionType.REPAIR_ORDER, "confirmed facts");
    }

    private PaymentReconciliationServiceImpl service() {
        return new PaymentReconciliationServiceImpl(caseMapper, actionMapper, cases, detector, actions, details,
            payPalService, successService, operator);
    }
}
