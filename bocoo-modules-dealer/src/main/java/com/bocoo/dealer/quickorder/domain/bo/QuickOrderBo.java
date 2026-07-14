package com.bocoo.dealer.quickorder.domain.bo;

import com.bocoo.common.core.validate.EditGroup;
import com.bocoo.common.mybatis.core.domain.BaseBo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuickOrderBo extends BaseBo {
    @NotNull(message = "{dealer.quickOrder.id.required}", groups = EditGroup.class)
    private Long quickOrderId;
    private Long tenantId;
    private String businessOrigin;
    private Long salesStoreId;
    private Long deptId;
    private Long ownerUserId;
    private String quickOrderNo;
    private Long customerId;
    private String customerName;
    private String status;
    private String createBy;
    private LocalDateTime updateTimeStart;
    private LocalDateTime updateTimeEnd;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String customerPoNo;
    private String remark;
    private List<QuickOrderItemBo> items;
}
