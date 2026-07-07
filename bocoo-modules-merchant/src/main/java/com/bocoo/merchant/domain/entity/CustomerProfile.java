package com.bocoo.merchant.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_profile")
@Schema(description = "客户资料")
public class CustomerProfile extends BaseEntity {

    @TableId(value = "customer_id")
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
}
