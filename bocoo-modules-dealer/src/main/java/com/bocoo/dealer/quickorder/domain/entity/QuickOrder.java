package com.bocoo.dealer.quickorder.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dealer_quick_order")
public class QuickOrder extends BaseEntity {
    @TableId(value = "quick_order_id")
    private Long quickOrderId;
    private Long tenantId;
    private String businessOrigin;
    private Long salesStoreId;
    private Long deptId;
    private String quickOrderNo;
    private Long customerId;
    private String customerName;
    private String companyName;
    private String customerEmail;
    private String customerPhone;
    private Long ownerUserId;
    private String ownerName;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String customerPoNo;
    private String currencyCode;
    private BigDecimal listAmount;
    private BigDecimal discountAmount;
    private BigDecimal productAmount;
    private BigDecimal shippingAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String status;
    private Long salesDocumentId;
    private String orderNo;
    private Long submittedById;
    private String submittedBy;
    private LocalDateTime submittedTime;
    private String delFlag;
    private String remark;
}
