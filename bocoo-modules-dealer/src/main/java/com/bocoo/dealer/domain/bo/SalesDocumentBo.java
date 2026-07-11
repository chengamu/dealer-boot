package com.bocoo.dealer.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SalesDocumentBo extends BaseEntity {
    private Long salesDocumentId;
    private String quoteNo;
    private String orderNo;
    @NotNull
    private Long customerId;
    private String customerName;
    private String projectName;
    private String customerPoNo;
    private LocalDate validUntil;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String documentStatus;
    private String paymentStatus;
    private String productionStatus;
    private String shipmentStatus;
    private String merchantName;
    private String remark;
    @Valid
    private List<SalesDocumentItemBo> items;
}
