package com.bocoo.system.domain.vo;

import com.bocoo.system.domain.entity.SysTenantApply;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = SysTenantApply.class)
@Schema(description = "Tenant application view")
public class SysTenantApplyVo implements Serializable {

    private Long applyId;

    private Long tenantId;

    private String merchantName;

    private String companyName;

    private String contactFirstName;

    private String contactLastName;

    private String contactName;

    private String email;

    private String officePhone;

    private String mobilePhone;

    private String country;

    private String state;

    private String city;

    private String addressLine1;

    private String addressLine2;

    private String postalCode;

    private String remark;

    private String status;

    private String auditBy;

    private LocalDateTime auditTime;

    private String rejectReason;

    private LocalDateTime createTime;
}
