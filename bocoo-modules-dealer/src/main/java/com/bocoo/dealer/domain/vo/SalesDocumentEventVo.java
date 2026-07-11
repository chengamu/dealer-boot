package com.bocoo.dealer.domain.vo;

import com.bocoo.dealer.domain.entity.SalesDocumentEvent;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = SalesDocumentEvent.class)
public class SalesDocumentEventVo implements Serializable {
    private Long salesEventId;
    private String eventType;
    private String fromStatus;
    private String toStatus;
    private Long operatorId;
    private String operatorName;
    private String eventNote;
    private LocalDateTime occurredTime;
}
