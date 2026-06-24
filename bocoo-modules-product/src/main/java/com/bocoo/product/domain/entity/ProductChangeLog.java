package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 产品业务变更流水 pc_change_log
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_change_log")
@Schema(description = "产品业务变更流水")
public class ProductChangeLog extends BaseEntity {

    @TableId(value = "change_log_id")
    @Schema(description = "变更流水ID")
    private Long changeLogId;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "业务模块")
    private String bizModule;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务对象ID")
    private Long bizId;

    @Schema(description = "业务对象编码")
    private String bizCode;

    @Schema(description = "动作类型")
    private String actionType;

    @Schema(description = "动作名称")
    private String actionName;

    @Schema(description = "变更前JSON")
    private String beforeJson;

    @Schema(description = "变更后JSON")
    private String afterJson;

    @Schema(description = "字段差异JSON")
    private String diffJson;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人")
    private String operatorName;

    @Schema(description = "操作时间")
    private LocalDateTime operateTime;

    @Schema(description = "备注")
    private String remark;
}
