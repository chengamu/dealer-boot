package com.bocoo.dealer.quickorder.domain.vo;

import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AutoMapper(target = QuickOrder.class)
public class QuickOrderVo {
    private Long quickOrderId;
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
    private Integer itemTypeCount;
    private Integer totalQuantity;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String remark;
    private List<QuickOrderItemVo> items = new ArrayList<>();
}
