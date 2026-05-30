package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Payment wallet recharge.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_wallet_recharge")
@Schema(description = "钱包充值")
public class PayWalletRecharge extends BaseEntity {

    @TableId(value = "id")
    @Schema(description = "充值ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "钱包ID")
    private Long walletId;

    @Schema(description = "充值单号")
    private String no;

    @Schema(description = "充值金额，单位分")
    private Long payPrice;

    @Schema(description = "赠送金额，单位分")
    private Long bonusPrice;

    @Schema(description = "支付订单ID")
    private Long payOrderId;

    @Schema(description = "支付状态")
    private Integer payStatus;
}
