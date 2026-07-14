package com.bocoo.pay.domain.vo;

import com.bocoo.pay.api.PaymentDocumentFacts;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class BankTransferSummaryVo {
    Long extensionId;
    Long payOrderId;
    String payOrderNo;
    Long salesDocumentId;
    String salesOrderNo;
    String businessOrigin;
    String subjectName;
    String payerName;
    String referenceNo;
    Long declaredPrice;
    String currency;
    Long proofMediaId;
    String status;
    LocalDateTime transferTime;
    LocalDateTime submittedTime;
    LocalDateTime reviewedTime;
    String rejectReason;

    public static BankTransferSummaryVo from(PayOrderExtension row, PayOrder order, PaymentDocumentFacts facts) {
        return builder().extensionId(row.getId()).payOrderId(order.getId()).payOrderNo(order.getNo())
            .salesDocumentId(order.getSalesDocumentId()).salesOrderNo(order.getSalesOrderNo())
            .businessOrigin(facts == null ? null : facts.getBusinessOrigin())
            .subjectName(facts == null ? null : facts.getSubjectName()).payerName(row.getBankPayerName())
            .referenceNo(row.getBankReferenceNo()).declaredPrice(row.getBankDeclaredPrice())
            .currency(row.getBankCurrency()).proofMediaId(row.getBankProofMediaId())
            .status(row.getBankTransferStatus()).transferTime(row.getBankTransferTime())
            .submittedTime(row.getBankSubmittedTime()).reviewedTime(row.getBankReviewedTime())
            .rejectReason(row.getBankRejectReason()).build();
    }
}
