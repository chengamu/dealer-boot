package com.bocoo.pay.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Payment submission result.
 */
@Data
@Schema(description = "提交支付结果")
public class PaySubmitVo {

    @Schema(description = "支付提交记录ID")
    private Long extensionId;

    @Schema(description = "支付订单号")
    private String orderNo;

    @Schema(description = "支付渠道编码")
    private String channelCode;

    @Schema(description = "支付跳转地址或渠道表单")
    private String displayContent;

    @Schema(description = "渠道原始响应JSON")
    private String rawData;
}
