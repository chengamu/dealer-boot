package com.bocoo.product.domain.bo;

import com.bocoo.product.domain.entity.ProductImportBatch;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品能力导入批次业务对象
 */
@Data
@AutoMapper(target = ProductImportBatch.class, reverseConvertGenerate = false)
@Schema(description = "产品能力导入批次业务对象")
public class ProductImportBatchBo {

    @Schema(description = "产品能力导入批次ID")
    private Long batchId;

    @Schema(description = "导入批次编码")
    private String batchCode;

    @Schema(description = "导入类型：MATERIAL物料，COMPONENT组件，OPTION答案，PRICE价格，MEDIA资料")
    private String importType;

    @Schema(description = "来源系统：MANUAL人工，ERP，MES，LEGACY老系统")
    private String sourceSystem;

    @Schema(description = "原始文件名")
    private String sourceFileName;

    @Schema(description = "原始文件地址")
    private String sourceFileUrl;

    @Schema(description = "目标对象类型：PRODUCT_MODEL产品模型，TEMPLATE配置模板，PRICE_PLAN价格方案")
    private String targetObjectType;

    @Schema(description = "目标对象编码")
    private String targetObjectCode;

    @Schema(description = "导入状态：DRAFT草稿，PARSED已解析，VALIDATED已校验，COMMITTED已提交，FAILED失败，CANCELED已取消")
    private String importStatus;

    @Schema(description = "总行数")
    private Integer totalRows;

    @Schema(description = "成功行数")
    private Integer successRows;

    @Schema(description = "警告行数")
    private Integer warningRows;

    @Schema(description = "失败行数")
    private Integer failedRows;

    @Schema(description = "字段映射JSON")
    private String mappingJson;

    @Schema(description = "导入预览JSON")
    private String previewJson;

    @Schema(description = "错误汇总JSON")
    private String errorSummaryJson;

    @Schema(description = "导入开始时间，UTC语义")
    private LocalDateTime startedTime;

    @Schema(description = "导入完成时间，UTC语义")
    private LocalDateTime finishedTime;

    @Schema(description = "备注")
    private String remark;

}
