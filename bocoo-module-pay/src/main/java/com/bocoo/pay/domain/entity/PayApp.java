package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Payment application.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_app")
@Schema(description = "支付应用")
public class PayApp extends BaseEntity {

    @TableId(value = "id")
    @Schema(description = "应用ID")
    private Long id;

    @Schema(description = "租户ID，当前为收款方平台/厂家租户")
    private Long tenantId;

    @Schema(description = "应用标识")
    private String appKey;

    @Schema(description = "应用名称")
    private String name;

    @Schema(description = "状态：1正常，0停用")
    private String status;

    @Schema(description = "支付通知地址")
    private String orderNotifyUrl;

    @Schema(description = "退款通知地址")
    private String refundNotifyUrl;

    @Schema(description = "转账通知地址")
    private String transferNotifyUrl;

    @Schema(description = "备注")
    private String remark;
}
