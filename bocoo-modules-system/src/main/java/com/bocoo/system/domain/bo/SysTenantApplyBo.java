package com.bocoo.system.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.system.domain.entity.SysTenantApply;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysTenantApply.class, reverseConvertGenerate = false)
@Schema(description = "Tenant application request")
public class SysTenantApplyBo extends BaseEntity {

    private Long applyId;

    @NotBlank(message = "{tenant.apply.merchantName.required}")
    @Size(max = 100, message = "{tenant.apply.merchantName.max}")
    private String merchantName;

    @Size(max = 100, message = "{tenant.apply.companyName.max}")
    private String companyName;

    @Size(max = 50, message = "{tenant.apply.contactFirstName.max}")
    private String contactFirstName;

    @Size(max = 50, message = "{tenant.apply.contactLastName.max}")
    private String contactLastName;

    @Size(max = 100, message = "{tenant.apply.contactName.max}")
    private String contactName;

    @NotBlank(message = "{tenant.apply.email.required}")
    @Email(message = "{tenant.apply.email.invalid}")
    @Size(max = 100, message = "{tenant.apply.email.max}")
    private String email;

    @Size(max = 50, message = "{tenant.apply.officePhone.max}")
    private String officePhone;

    @Size(max = 50, message = "{tenant.apply.mobilePhone.max}")
    private String mobilePhone;

    @NotBlank(message = "{tenant.apply.country.required}")
    @Size(max = 50, message = "{tenant.apply.country.max}")
    private String country;

    @Size(max = 50, message = "{tenant.apply.state.max}")
    private String state;

    @Size(max = 50, message = "{tenant.apply.city.max}")
    private String city;

    @Size(max = 255, message = "{tenant.apply.addressLine.max}")
    private String addressLine1;

    @Size(max = 255, message = "{tenant.apply.addressLine.max}")
    private String addressLine2;

    @Size(max = 20, message = "{tenant.apply.postalCode.max}")
    private String postalCode;

    @Size(max = 500, message = "{tenant.apply.remark.max}")
    private String remark;

    private String status;

    private String rejectReason;

    @Size(max = 6, message = "{tenant.apply.verificationCode.invalid}")
    private String verificationCode;

    private Boolean termsAccepted;
}
