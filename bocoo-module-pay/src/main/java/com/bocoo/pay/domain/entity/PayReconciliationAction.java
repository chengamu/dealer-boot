package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_reconciliation_action")
public class PayReconciliationAction extends BaseEntity {
    @TableId(value = "action_id")
    private Long actionId;
    private Long caseId;
    private String actionType;
    private String beforeSnapshotJson;
    private String afterSnapshotJson;
    private String result;
    private String resultCode;
    private String resultMessage;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime occurredTime;
    private String reason;
}
