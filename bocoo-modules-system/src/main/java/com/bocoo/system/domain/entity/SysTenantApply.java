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
@TableName("sys_tenant_apply")
@Schema(description = "Tenant application")
public class SysTenantApply extends BaseEntity {

    @TableId(value = "apply_id")
    private Long applyId;

    private Long tenantId;

    private String merchantName;

    private String contactName;

    private String email;

    private String country;

    private String remark;

    private String status;

    private String auditBy;

    private Long auditById;

    private LocalDateTime auditTime;

    private String rejectReason;
}
