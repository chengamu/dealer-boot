package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant_profile")
@Schema(description = "Merchant profile")
public class MerchantProfile extends BaseEntity {

    @TableId(value = "merchant_id")
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
}
