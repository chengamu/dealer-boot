package com.bocoo.pay.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Wallet balance change command.
 */
@Data
@Schema(description = "钱包余额变更参数")
public class PayWalletChangeBo {

    @Schema(description = "钱包ID")
    private Long walletId;

    @Schema(description = "金额，单位分；正数入账，负数出账")
    private Long price;

    @Schema(description = "流水标题")
    private String title;

    @Schema(description = "业务类型")
    private Integer bizType;

    @Schema(description = "业务ID")
    private String bizId;

    @Schema(description = "备注")
    private String remark;
}
