package com.bocoo.pay.domain.bo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReconciliationCaseQueryBo {
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private String status = "OPEN";
    private String anomalyType;
    private String severity;
    private String businessOrigin;
    private Long subjectId;
    private String keyword;
}
