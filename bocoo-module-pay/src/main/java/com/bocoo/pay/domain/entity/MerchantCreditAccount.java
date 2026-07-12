package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant_credit_account")
public class MerchantCreditAccount extends BaseEntity {
    @TableId(value = "credit_account_id")
    private Long creditAccountId;
    private Long tenantId;
    private Long merchantId;
    private String merchantName;
    private BigDecimal creditLimit;
    private BigDecimal usedCredit;
    private String currency;
    private String status;
    private Integer version;
    private Long frozenById;
    private String frozenBy;
    private LocalDateTime frozenTime;
    private String frozenReason;
}
