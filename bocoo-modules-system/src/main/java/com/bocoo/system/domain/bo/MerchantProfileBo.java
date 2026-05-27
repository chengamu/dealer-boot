package com.bocoo.system.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.system.domain.entity.MerchantProfile;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = MerchantProfile.class, reverseConvertGenerate = false)
@Schema(description = "Merchant profile business object")
public class MerchantProfileBo extends BaseEntity {

    private Long merchantId;

    private Long tenantId;

    @Size(max = 100, message = "{merchant.profile.merchantName.max}")
    private String merchantName;

    @Size(max = 100, message = "{merchant.profile.companyName.max}")
    private String companyName;

    @Size(max = 50, message = "{merchant.profile.contactFirstName.max}")
    private String contactFirstName;

    @Size(max = 50, message = "{merchant.profile.contactLastName.max}")
    private String contactLastName;

    @Size(max = 100, message = "{merchant.profile.contactName.max}")
    private String contactName;

    @Email(message = "{merchant.profile.primaryEmail.invalid}")
    @Size(max = 100, message = "{merchant.profile.primaryEmail.max}")
    private String primaryEmail;

    @Size(max = 50, message = "{merchant.profile.officePhone.max}")
    private String officePhone;

    @Size(max = 50, message = "{merchant.profile.mobilePhone.max}")
    private String mobilePhone;

    @Size(max = 50, message = "{merchant.profile.country.max}")
    private String country;

    @Size(max = 50, message = "{merchant.profile.state.max}")
    private String state;

    @Size(max = 50, message = "{merchant.profile.city.max}")
    private String city;

    @Size(max = 255, message = "{merchant.profile.addressLine.max}")
    private String addressLine1;

    @Size(max = 255, message = "{merchant.profile.addressLine.max}")
    private String addressLine2;

    @Size(max = 20, message = "{merchant.profile.postalCode.max}")
    private String postalCode;

    private String status;

    private String auditStatus;

    @Size(max = 500, message = "{merchant.profile.remark.max}")
    private String remark;
}
