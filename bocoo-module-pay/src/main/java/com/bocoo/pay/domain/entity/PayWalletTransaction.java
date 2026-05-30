package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Payment wallet transaction.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_wallet_transaction")
@Schema(description = "钱包流水")
public class PayWalletTransaction extends BaseEntity {

    @TableId(value = "id")
    @Schema(description = "流水ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "钱包ID")
    private Long walletId;

    @Schema(description = "流水号")
    private String no;

    @Schema(description = "流水标题")
    private String title;

    @Schema(description = "金额，单位分")
    private Long price;

    @Schema(description = "余额，单位分")
    private Long balance;

    @Schema(description = "业务类型")
    private Integer bizType;

    @Schema(description = "业务ID")
    private String bizId;

    @Schema(description = "备注")
    private String remark;
}
