package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Payment wallet.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_wallet")
@Schema(description = "支付钱包")
public class PayWallet extends BaseEntity {

    @TableId(value = "id")
    @Schema(description = "钱包ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户类型")
    private String userType;

    @Schema(description = "余额，单位分")
    private Long balance;

    @Schema(description = "累计支出，单位分")
    private Long totalExpense;

    @Schema(description = "累计充值，单位分")
    private Long totalRecharge;
}
