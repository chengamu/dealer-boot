package com.bocoo.pay.domain.vo;

import com.bocoo.pay.api.PaymentDocumentFacts;
import com.bocoo.pay.domain.entity.PayOrder;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class PaymentOrderSummaryVo {
    Long payOrderId;
    String payOrderNo;
    Long salesDocumentId;
    String salesOrderNo;
    String businessOrigin;
    Long subjectId;
    String subjectName;
    String customerName;
    String channelCode;
    Long price;
    String currency;
    Integer status;
    String channelOrderNo;
    LocalDateTime createTime;
    LocalDateTime successTime;

    public static PaymentOrderSummaryVo from(PayOrder row, PaymentDocumentFacts facts) {
        Long subjectId = facts == null ? null
            : "INTERNAL".equals(facts.getBusinessOrigin()) ? facts.getSalesStoreId() : facts.getTenantId();
        return builder().payOrderId(row.getId()).payOrderNo(row.getNo())
            .salesDocumentId(row.getSalesDocumentId()).salesOrderNo(row.getSalesOrderNo())
            .businessOrigin(facts == null ? null : facts.getBusinessOrigin()).subjectId(subjectId)
            .subjectName(facts == null ? null : facts.getSubjectName()).customerName(row.getCustomerName())
            .channelCode(row.getChannelCode()).price(row.getPrice()).currency(row.getCurrency())
            .status(row.getStatus()).channelOrderNo(row.getChannelOrderNo())
            .createTime(row.getCreateTime()).successTime(row.getSuccessTime()).build();
    }
}
