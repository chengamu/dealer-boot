package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Payment transfer.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_transfer")
@Schema(description = "支付转账")
public class PayTransfer extends BaseEntity {

    @TableId(value = "id")
    @Schema(description = "转账ID")
    private Long id;

    @Schema(description = "租户ID，默认等于收款方平台/厂家租户ID")
    private Long tenantId;

    @Schema(description = "收款方平台/厂家租户ID")
    private Long payeeTenantId;

    @Schema(description = "转账单号")
    private String no;

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

    @Schema(description = "转账状态")
    private Integer status;

    @Schema(description = "成功时间，UTC")
    private LocalDateTime successTime;

    @Schema(description = "通知地址")
    private String notifyUrl;

    @Schema(description = "用户IP")
    private String userIp;

    @Schema(description = "渠道扩展参数JSON")
    private String channelExtras;

    @Schema(description = "渠道转账号")
    private String channelTransferNo;

    @Schema(description = "渠道错误码")
    private String channelErrorCode;

    @Schema(description = "渠道错误信息")
    private String channelErrorMsg;

    @Schema(description = "渠道回调原始数据")
    private String channelNotifyData;

    @Schema(description = "渠道扩展包信息")
    private String channelPackageInfo;
}
