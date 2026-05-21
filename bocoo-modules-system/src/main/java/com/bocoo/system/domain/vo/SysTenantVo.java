package com.bocoo.system.domain.vo;

import com.bocoo.system.domain.entity.SysTenant;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@AutoMapper(target = SysTenant.class)
@Schema(description = "Tenant view")
public class SysTenantVo implements Serializable {

    private Long tenantId;

    private String tenantName;

    private String tenantType;

    private String contactName;

    private String contactEmail;

    private String country;

    private String status;

    private String remark;
}
