package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Payment notify task.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_notify_task")
@Schema(description = "支付通知任务")
public class PayNotifyTask extends BaseEntity {

    @TableId(value = "id")
    @Schema(description = "通知任务ID")
    private Long id;

    @Schema(description = "租户ID，默认等于收款方租户ID")
    private Long tenantId;

    @Schema(description = "付款方商户租户ID")
    private Long payerTenantId;

    @Schema(description = "收款方平台/厂家租户ID")
    private Long payeeTenantId;

    @Schema(description = "支付应用ID")
    private Long appId;

    @Schema(description = "通知类型")
    private Integer type;

    @Schema(description = "业务数据ID")
    private Long dataId;

    @Schema(description = "业务订单号")
    private String merchantOrderId;

    @Schema(description = "业务退款号")
    private String merchantRefundId;

    @Schema(description = "业务转账号")
    private String merchantTransferId;

    @Schema(description = "通知状态")
    private Integer status;

    @Schema(description = "下次通知时间，UTC")
    private LocalDateTime nextNotifyTime;

    @Schema(description = "最后执行时间，UTC")
    private LocalDateTime lastExecuteTime;

    @Schema(description = "已通知次数")
    private Integer notifyTimes;

    @Schema(description = "最大通知次数")
    private Integer maxNotifyTimes;

    @Schema(description = "通知地址")
    private String notifyUrl;
}
