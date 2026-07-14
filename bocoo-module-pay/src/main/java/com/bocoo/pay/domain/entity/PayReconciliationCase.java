package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_reconciliation_case")
public class PayReconciliationCase extends BaseEntity {
    @TableId(value = "case_id")
    private Long caseId;
    private String caseNo;
    private String businessOrigin;
    private Long tenantId;
    private Long salesStoreId;
    private Long payOrderId;
    private Long extensionId;
    private Long webhookEventId;
    private Long salesDocumentId;
    private String anomalyType;
    private String severity;
    private String status;
    private LocalDateTime detectedTime;
    private LocalDateTime lastCheckedTime;
    private String diagnosisCode;
    private String diagnosisMessage;
    private String expectedSnapshotJson;
    private String actualSnapshotJson;
    private Long resolvedById;
    private String resolvedBy;
    private LocalDateTime resolvedTime;
    private String resolutionCode;
    private Integer version;
}
