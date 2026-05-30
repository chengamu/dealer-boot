package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Payment order submit extension.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_order_extension")
@Schema(description = "支付提交记录")
public class PayOrderExtension extends BaseEntity {

    @TableId(value = "id")
    @Schema(description = "提交记录ID")
    private Long id;

    @Schema(description = "租户ID，默认等于付款方租户ID用于租户拦截")
    private Long tenantId;

    @Schema(description = "付款方商户租户ID")
    private Long payerTenantId;

    @Schema(description = "收款方平台/厂家租户ID")
    private Long payeeTenantId;

    @Schema(description = "提交单号")
    private String no;

    @Schema(description = "支付订单ID")
    private Long orderId;

    @Schema(description = "支付渠道ID")
    private Long channelId;

    @Schema(description = "支付渠道编码")
    private String channelCode;

    @Schema(description = "用户IP")
    private String userIp;

    @Schema(description = "提交状态")
    private Integer status;

    @Schema(description = "渠道扩展参数JSON")
    private String channelExtras;

    @Schema(description = "渠道错误码")
    private String channelErrorCode;

    @Schema(description = "渠道错误信息")
    private String channelErrorMsg;

    @Schema(description = "渠道回调原始数据")
    private String channelNotifyData;
}
