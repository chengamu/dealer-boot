package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductSyncOutbox;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 产品能力同步Outbox业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductSyncOutbox.class, reverseConvertGenerate = false)
@Schema(description = "产品能力同步Outbox业务对象")
public class ProductSyncOutboxBo extends BaseBo {

    /**
     * 产品能力同步OutboxID
     */
    @Schema(description = "产品能力同步OutboxID")
    private Long outboxId;

    /**
     * 发布包ID
     */
    @Schema(description = "发布包ID")
    private Long packageId;

    /**
     * 发布包编码
     */
    @Schema(description = "发布包编码")
    private String packageCode;

    /**
     * 目标系统：ORDER订单，ERP，MES
     */
    @Schema(description = "目标系统：ORDER订单，ERP，MES")
    private String targetSystem;

    /**
     * 事件类型
     */
    @Schema(description = "事件类型")
    private String eventType;

    /**
     * 负载哈希
     */
    @Schema(description = "负载哈希")
    private String payloadHash;

    /**
     * 同步负载JSON
     */
    @Schema(description = "同步负载JSON")
    private String payloadJson;

    /**
     * 同步状态：PENDING待同步，SUCCESS成功，FAILED失败
     */
    @Schema(description = "同步状态：PENDING待同步，SUCCESS成功，FAILED失败")
    private String syncStatus;

    /**
     * 重试次数
     */
    @Schema(description = "重试次数")
    private Integer retryCount;

    /**
     * 下次重试时间，UTC语义
     */
    @Schema(description = "下次重试时间，UTC语义")
    private LocalDateTime nextRetryTime;

    /**
     * 最后错误国际化key
     */
    @Schema(description = "最后错误国际化key")
    private String lastErrorKey;

    /**
     * 最后错误消息
     */
    @Schema(description = "最后错误消息")
    private String lastErrorMessage;

    /**
     * 状态：1正常，0停用
     */
    @Schema(description = "状态：1正常，0停用")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
