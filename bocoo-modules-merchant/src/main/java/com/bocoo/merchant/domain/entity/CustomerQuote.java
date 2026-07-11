package com.bocoo.merchant.domain.entity;

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
@TableName("customer_quote")
public class CustomerQuote extends BaseEntity {

    @TableId(value = "quote_id")
    private Long quoteId;
    private Long tenantId;
    private String quoteNo;
    private Long customerId;
    private String customerName;
    private String companyName;
    private String customerEmail;
    private String projectName;
    private String quoteLanguage;
    private LocalDate validUntil;
    private Long ownerUserId;
    private String ownerName;
    private String currencyCode;
    private String status;
    private BigDecimal productAmount;
    private BigDecimal shippingAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private Long confirmedById;
    private String confirmedBy;
    private LocalDateTime confirmedTime;
    private String delFlag;
    private String remark;
}
