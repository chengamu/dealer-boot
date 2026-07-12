package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant_receivable")
public class MerchantReceivable extends BaseEntity {
    @TableId(value = "receivable_id")
    private Long receivableId;
    private Long tenantId;
    private Long merchantId;
    private String merchantName;
    private Long salesDocumentId;
    private Long payOrderId;
    private String receivableNo;
    private String salesOrderNo;
    private String payOrderNo;
    private Long creditAccountId;
    private BigDecimal receivableAmount;
    private BigDecimal repaidAmount;
    private BigDecimal outstandingAmount;
    private String currency;
    private String status;
    private LocalDateTime formedTime;
    private LocalDate dueDate;
    private LocalDateTime settledTime;
}
