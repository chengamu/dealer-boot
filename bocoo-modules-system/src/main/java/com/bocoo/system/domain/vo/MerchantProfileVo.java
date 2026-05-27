package com.bocoo.system.domain.vo;

import com.bocoo.system.domain.entity.MerchantProfile;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = MerchantProfile.class)
@Schema(description = "Merchant profile view")
public class MerchantProfileVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long merchantId;

    private Long tenantId;

    private String merchantName;

    private String companyName;

    private String contactFirstName;

    private String contactLastName;

    private String contactName;

    private String primaryEmail;

    private String officePhone;

    private String mobilePhone;

    private String country;

    private String state;

    private String city;

    private String addressLine1;

    private String addressLine2;

    private String postalCode;

    private String status;

    private String auditStatus;

    private String auditBy;

    private Long auditById;

    private LocalDateTime auditTime;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
