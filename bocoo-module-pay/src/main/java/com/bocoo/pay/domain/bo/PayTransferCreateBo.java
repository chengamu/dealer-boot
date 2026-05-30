package com.bocoo.pay.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Transfer creation command.
 */
@Data
@Schema(description = "转账创建参数")
public class PayTransferCreateBo {

    @Schema(description = "收款方平台/厂家租户ID")
    private Long payeeTenantId;

    @Schema(description = "支付应用ID")
    private Long appId;

    @Schema(description = "支付渠道ID")
    private Long channelId;

    @Schema(description = "支付渠道编码")
    private String channelCode;

    @Schema(description = "业务转账号")
    private String merchantTransferId;

    @Schema(description = "转账标题")
    private String subject;

    @Schema(description = "转账金额，单位分")
    private Long price;

    @Schema(description = "收款账号")
    private String userAccount;

    @Schema(description = "收款人姓名")
    private String userName;

    @Schema(description = "通知地址")
    private String notifyUrl;

    @Schema(description = "用户IP")
    private String userIp;

    @Schema(description = "渠道扩展参数JSON")
    private String channelExtras;
}
