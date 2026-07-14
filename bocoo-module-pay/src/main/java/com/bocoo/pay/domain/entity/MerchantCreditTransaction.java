package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant_credit_transaction")
public class MerchantCreditTransaction extends BaseEntity {
    @TableId(value = "credit_transaction_id")
    private Long creditTransactionId;
    private Long tenantId;
    private Long creditAccountId;
    private String transactionNo;
    private String transactionType;
    private String businessType;
    private Long businessId;
    private String businessNo;
    private BigDecimal amount;
    private BigDecimal beforeCreditLimit;
    private BigDecimal afterCreditLimit;
    private BigDecimal beforeUsedCredit;
    private BigDecimal afterUsedCredit;
    private String currency;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime occurredTime;
    private String paymentMethod;
    private String paymentReference;
    private LocalDateTime paidTime;
    private Long proofMediaId;
    private String idempotencyKey;
    private String remark;
}
