package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Payment notify log.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_notify_log")
@Schema(description = "支付通知日志")
public class PayNotifyLog extends BaseEntity {

    @TableId(value = "id")
    @Schema(description = "通知日志ID")
    private Long id;

    @Schema(description = "租户ID，默认等于收款方租户ID")
    private Long tenantId;

    @Schema(description = "通知任务ID")
    private Long taskId;

    @Schema(description = "第几次通知")
    private Integer notifyTimes;

    @Schema(description = "响应内容")
    private String response;

    @Schema(description = "通知状态")
    private Integer status;
}
