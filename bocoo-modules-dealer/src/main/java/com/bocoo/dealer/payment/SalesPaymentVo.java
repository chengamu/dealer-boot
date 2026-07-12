package com.bocoo.dealer.payment;

import com.bocoo.pay.domain.vo.CreditAccountSummaryVo;
import com.bocoo.pay.domain.vo.PayAttemptVo;
import com.bocoo.pay.domain.vo.PayOrderStatusVo;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class SalesPaymentVo {
    Long salesDocumentId;
    String orderNo;
    Long tenantId;
    String customerName;
    String projectName;
    BigDecimal totalAmount;
    String currency;
    String documentStatus;
    String paymentStatus;
    PayOrderStatusVo payOrder;
    List<PayAttemptVo> attempts;
    CreditAccountSummaryVo creditAccount;
}
