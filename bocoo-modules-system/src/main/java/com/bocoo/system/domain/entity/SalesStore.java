package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sales_store")
public class SalesStore extends BaseEntity {

    @TableId(value = "sales_store_id")
    private Long salesStoreId;
    private Long tenantId;
    private String storeCode;
    private String storeName;
    private Long deptId;
    private String contactName;
    private String contactPhone;
    private String country;
    private String state;
    private String city;
    private String addressLine1;
    private String addressLine2;
    private String postalCode;
    private String currencyCode;
    private BigDecimal creditLimit;
    private Integer paymentTermDays;
    private String status;
    @TableLogic
    private String delFlag;
    private String remark;
}
