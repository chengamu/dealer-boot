package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Payment wallet recharge package.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_wallet_recharge_package")
@Schema(description = "钱包充值套餐")
public class PayWalletRechargePackage extends BaseEntity {

    @TableId(value = "id")
    @Schema(description = "套餐ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "套餐名称")
    private String name;

    @Schema(description = "支付金额，单位分")
    private Long payPrice;

    @Schema(description = "赠送金额，单位分")
    private Long bonusPrice;

    @Schema(description = "状态：1正常，0停用")
    private String status;
}
