package com.bocoo.merchant.domain.vo;

import com.bocoo.merchant.domain.entity.CustomerQuote;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AutoMapper(target = CustomerQuote.class)
public class CustomerQuoteVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long quoteId;
    private Long tenantId;
    private String businessOrigin;
    private Long salesStoreId;
    private Long deptId;
    private String quoteNo;
    private Long customerId;
    private String customerName;
    private String companyName;
    private String customerEmail;
    private String customerPhone;
    private String projectName;
    private String customerPoNo;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String quoteLanguage;
    private LocalDate validUntil;
    private Long ownerUserId;
    private String ownerName;
    private String currencyCode;
    private String status;
    private BigDecimal productAmount;
    private BigDecimal shippingAmount;
    private BigDecimal totalAmount;
    private Long confirmedById;
    private String confirmedBy;
    private LocalDateTime confirmedTime;
    private Long salesDocumentId;
    private String orderNo;
    private Long convertedById;
    private String convertedBy;
    private LocalDateTime convertedTime;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private Integer itemCount;
    private List<CustomerQuoteItemVo> items = new ArrayList<>();
}
