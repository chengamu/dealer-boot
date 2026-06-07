package com.bocoo.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品能力工作台配置进度视图对象
 */
@Data
@Schema(description = "产品能力工作台配置进度视图对象")
public class WorkbenchProgressVo {

    /**
     * 产品模型ID
     */
    @Schema(description = "产品模型ID")
    private Long modelId;

    /**
     * 产品模型编码
     */
    @Schema(description = "产品模型编码")
    private String modelCode;

    /**
     * 产品模型名称
     */
    @Schema(description = "产品模型名称")
    private String modelName;

    /**
     * 产品分类名称
     */
    @Schema(description = "产品分类名称")
    private String categoryName;

    /**
     * 配置模板状态
     */
    @Schema(description = "配置模板状态")
    private String templateStatus;

    /**
     * 价格状态
     */
    @Schema(description = "价格状态")
    private String priceStatus;

    /**
     * 资料状态
     */
    @Schema(description = "资料状态")
    private String assetStatus;

    /**
     * 发布状态
     */
    @Schema(description = "发布状态")
    private String publishStatus;

    /**
     * 阻断问题数量
     */
    @Schema(description = "阻断问题数量")
    private Integer blockerCount;

    /**
     * 警告问题数量
     */
    @Schema(description = "警告问题数量")
    private Integer warningCount;

    /**
     * 更新时间，UTC语义
     */
    @Schema(description = "更新时间，UTC语义")
    private LocalDateTime updatedTime;

}
