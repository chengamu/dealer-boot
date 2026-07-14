package com.bocoo.system.domain.vo;

import com.bocoo.system.domain.entity.SalesStore;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = SalesStore.class)
@Schema(description = "Sales store view")
public class SalesStoreVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long salesStoreId;
    private Long tenantId;
    private String storeCode;
    private String storeName;
    private Long deptId;
    private String deptName;
    private String contactName;
    private String contactPhone;
    private String country;
    private String state;
    private String city;
    private String addressLine1;
    private String addressLine2;
    private String postalCode;
    private String address;
    private String currencyCode;
    private BigDecimal creditLimit;
    private Integer paymentTermDays;
    private Long customerCount;
    private Long unfinishedOrderCount;
    private String status;
    private String remark;
    private LocalDateTime updateTime;
    private String updateBy;
}
