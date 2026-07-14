package com.bocoo.pay.domain.bo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BankTransferQueryBo {
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private String businessOrigin;
    private Long subjectId;
    private String keyword;
    private String status = "PENDING_REVIEW";
}
