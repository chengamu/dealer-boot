package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 产品能力导入批次表 pc_import_batch
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_import_batch")
@Schema(description = "产品能力导入批次表")
public class ProductImportBatch extends BaseEntity {

    /**
     * 产品能力导入批次ID
     */
    @TableId(value = "batch_id")
    @Schema(description = "产品能力导入批次ID")
    private Long batchId;

    /**
     * 导入批次编码
     */
    @Schema(description = "导入批次编码")
    private String batchCode;

    /**
     * 导入类型：MATERIAL物料，COMPONENT组件，OPTION答案，PRICE价格，MEDIA资料
     */
    @Schema(description = "导入类型：MATERIAL物料，COMPONENT组件，OPTION答案，PRICE价格，MEDIA资料")
    private String importType;

    /**
     * 来源系统：MANUAL人工，ERP，MES，LEGACY老系统
     */
    @Schema(description = "来源系统：MANUAL人工，ERP，MES，LEGACY老系统")
    private String sourceSystem;

    /**
     * 原始文件名
     */
    @Schema(description = "原始文件名")
    private String sourceFileName;

    /**
     * 原始文件地址
     */
    @Schema(description = "原始文件地址")
    private String sourceFileUrl;

    /**
     * 目标对象类型：PRODUCT_MODEL产品模型，TEMPLATE配置模板，PRICE_PLAN价格方案
     */
    @Schema(description = "目标对象类型：PRODUCT_MODEL产品模型，TEMPLATE配置模板，PRICE_PLAN价格方案")
    private String targetObjectType;

    /**
     * 目标对象编码
     */
    @Schema(description = "目标对象编码")
    private String targetObjectCode;

    /**
     * 导入状态：DRAFT草稿，PARSED已解析，VALIDATED已校验，COMMITTED已提交，FAILED失败，CANCELED已取消
     */
    @Schema(description = "导入状态：DRAFT草稿，PARSED已解析，VALIDATED已校验，COMMITTED已提交，FAILED失败，CANCELED已取消")
    private String importStatus;

    /**
     * 总行数
     */
    @Schema(description = "总行数")
    private Integer totalRows;

    /**
     * 成功行数
     */
    @Schema(description = "成功行数")
    private Integer successRows;

    /**
     * 警告行数
     */
    @Schema(description = "警告行数")
    private Integer warningRows;

    /**
     * 失败行数
     */
    @Schema(description = "失败行数")
    private Integer failedRows;

    /**
     * 字段映射JSON
     */
    @Schema(description = "字段映射JSON")
    private String mappingJson;

    /**
     * 导入预览JSON
     */
    @Schema(description = "导入预览JSON")
    private String previewJson;

    /**
     * 错误汇总JSON
     */
    @Schema(description = "错误汇总JSON")
    private String errorSummaryJson;

    /**
     * 导入开始时间，UTC语义
     */
    @Schema(description = "导入开始时间，UTC语义")
    private LocalDateTime startedTime;

    /**
     * 导入完成时间，UTC语义
     */
    @Schema(description = "导入完成时间，UTC语义")
    private LocalDateTime finishedTime;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
