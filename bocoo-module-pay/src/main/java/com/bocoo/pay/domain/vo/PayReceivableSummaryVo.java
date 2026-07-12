package com.bocoo.pay.domain.vo;

import com.bocoo.pay.domain.entity.MerchantReceivable;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class PayReceivableSummaryVo {
    Long receivableId;
    String receivableNo;
    Long merchantId;
    String merchantName;
    Long salesDocumentId;
    Long payOrderId;
    String salesOrderNo;
    String payOrderNo;
    BigDecimal receivableAmount;
    BigDecimal repaidAmount;
    BigDecimal outstandingAmount;
    String currency;
    LocalDate dueDate;
    String status;
    LocalDateTime formedTime;
    LocalDateTime settledTime;

    public static PayReceivableSummaryVo from(MerchantReceivable row) {
        if (row == null) return null;
        return builder().receivableId(row.getReceivableId()).receivableNo(row.getReceivableNo())
            .merchantId(row.getMerchantId()).merchantName(row.getMerchantName())
            .salesDocumentId(row.getSalesDocumentId()).payOrderId(row.getPayOrderId())
            .salesOrderNo(row.getSalesOrderNo()).payOrderNo(row.getPayOrderNo())
            .receivableAmount(row.getReceivableAmount()).repaidAmount(row.getRepaidAmount())
            .outstandingAmount(row.getOutstandingAmount()).currency(row.getCurrency())
            .dueDate(row.getDueDate()).status(row.getStatus()).formedTime(row.getFormedTime())
            .settledTime(row.getSettledTime()).build();
    }
}
