package com.bocoo.merchant.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.merchant.domain.entity.CustomerProfile;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CustomerProfile.class, reverseConvertGenerate = false)
@Schema(description = "客户资料业务对象")
public class CustomerProfileBo extends BaseBo {

    private Long customerId;
    private Long tenantId;
    private Long merchantId;
    private String merchantName;

    @NotBlank(message = "{customer.profile.name.required}")
    @Size(max = 120, message = "{customer.profile.name.max}")
    private String customerName;

    @Size(max = 120, message = "{customer.profile.company.max}")
    private String companyName;

    @NotBlank(message = "{customer.profile.email.required}")
    @Email(message = "{customer.profile.email.invalid}")
    @Size(max = 120, message = "{customer.profile.email.max}")
    private String email;

    @Size(max = 50, message = "{customer.profile.phone.max}")
    private String phone;

    private String customerType;
    private String country;
    private String state;
    private String city;
    private String addressLine1;
    private String addressLine2;
    private String postalCode;
    private Long ownerUserId;
    private String ownerName;
    private String status;
    private String delFlag;

    @Size(max = 500, message = "{merchant.common.remark.max}")
    private String remark;
}
