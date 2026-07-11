package com.bocoo.dealer.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dealer_sales_document_event")
public class SalesDocumentEvent extends BaseEntity {
    @TableId(value = "sales_event_id")
    private Long salesEventId;
    private Long salesDocumentId;
    private Long tenantId;
    private String eventType;
    private String fromStatus;
    private String toStatus;
    private Long operatorId;
    private String operatorName;
    private String eventNote;
    private LocalDateTime occurredTime;
}
