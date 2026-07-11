package com.bocoo.merchant.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerQuoteBo extends BaseBo {
    private Long quoteId;
    private String quoteNo;
    @NotNull(message = "{customer.quote.customer.required}")
    private Long customerId;
    @NotBlank(message = "{customer.quote.project.required}")
    private String projectName;
    private String customerPoNo;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String quoteLanguage;
    private LocalDate validUntil;
    private Long ownerUserId;
    private String status;
    private Long salesDocumentId;
    private String remark;
    private List<CustomerQuoteItemBo> items = new ArrayList<>();
}
