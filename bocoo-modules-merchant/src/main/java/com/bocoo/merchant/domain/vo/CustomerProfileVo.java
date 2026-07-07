package com.bocoo.merchant.domain.vo;

import com.bocoo.merchant.domain.entity.CustomerProfile;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = CustomerProfile.class)
public class CustomerProfileVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long customerId;
    private Long tenantId;
    private Long merchantId;
    private String merchantName;
    private String customerName;
    private String companyName;
    private String email;
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
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
