package com.bocoo.pay.domain.vo;

import com.bocoo.pay.domain.entity.PayOrderExtension;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class PayAttemptVo {
    Long id;
    String no;
    String channelCode;
    Integer status;
    String channelOrderNo;
    String channelCaptureNo;
    String bankTransferStatus;
    String bankPayerName;
    String bankReferenceNo;
    Long bankDeclaredPrice;
    String bankCurrency;
    Long bankProofMediaId;
    LocalDateTime bankTransferTime;
    LocalDateTime bankSubmittedTime;
    LocalDateTime bankReviewedTime;
    String bankRejectReason;
    LocalDateTime successTime;

    public static PayAttemptVo from(PayOrderExtension row) {
        return builder().id(row.getId()).no(row.getNo()).channelCode(row.getChannelCode()).status(row.getStatus())
            .channelOrderNo(row.getChannelOrderNo()).channelCaptureNo(row.getChannelCaptureNo())
            .bankTransferStatus(row.getBankTransferStatus()).bankPayerName(row.getBankPayerName())
            .bankReferenceNo(row.getBankReferenceNo()).bankDeclaredPrice(row.getBankDeclaredPrice())
            .bankCurrency(row.getBankCurrency()).bankProofMediaId(row.getBankProofMediaId())
            .bankTransferTime(row.getBankTransferTime()).bankSubmittedTime(row.getBankSubmittedTime())
            .bankReviewedTime(row.getBankReviewedTime()).bankRejectReason(row.getBankRejectReason())
            .successTime(row.getSuccessTime()).build();
    }
}
