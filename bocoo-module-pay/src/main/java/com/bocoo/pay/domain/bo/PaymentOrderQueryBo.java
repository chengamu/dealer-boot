package com.bocoo.pay.domain.bo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentOrderQueryBo {
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private String businessOrigin;
    private Long subjectId;
    private String keyword;
    private String channelCode;
    private Integer status;
}
