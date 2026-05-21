package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant")
@Schema(description = "Tenant")
public class SysTenant extends BaseEntity {

    @TableId(value = "tenant_id")
    private Long tenantId;

    private String tenantName;

    private String tenantType;

    private String contactName;

    private String contactEmail;

    private String country;

    private String status;

    private String remark;
}
