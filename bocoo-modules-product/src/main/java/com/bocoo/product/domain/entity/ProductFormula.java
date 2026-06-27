package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品配方主表 pc_formula
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_formula")
@Schema(description = "产品配方主表")
public class ProductFormula extends BaseEntity {

    @TableId(value = "formula_id")
    @Schema(description = "配方ID")
    private Long formulaId;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "配方编号")
    private String formulaCode;

    @Schema(description = "配方名称")
    private String formulaName;

    @Schema(description = "产品分类ID")
    private Long categoryId;

    @Schema(description = "产品分类编码")
    private String categoryCode;

    @Schema(description = "产品分类中文名称")
    private String categoryNameCn;

    @Schema(description = "产品类型编码")
    private String productTypeCode;

    @Schema(description = "产品类型中文名称")
    private String productTypeNameCn;

    @Schema(description = "最大宽度，英寸")
    private BigDecimal maxWidthInch;

    @Schema(description = "最大高度，英寸")
    private BigDecimal maxHeightInch;

    @Schema(description = "尺寸摘要")
    private String sizeSummary;

    @Schema(description = "物料行数")
    private Integer materialLineCount;

    @Schema(description = "是否已配置")
    private Boolean configuredFlag;

    @Schema(description = "当前生效版本ID")
    private Long currentVersionId;

    @Schema(description = "当前生效版本号")
    private Integer currentVersionNo;

    @Schema(description = "当前生效版本展示号")
    private String currentVersionLabel;

    @Schema(description = "草稿版本预览号")
    private Integer draftVersionNo;

    @Schema(description = "最近校验状态：NOT_VALIDATED、PASS、FAIL")
    private String latestValidationStatus;

    @Schema(description = "最近校验消息")
    private String latestValidationMessage;

    @Schema(description = "最近校验时间")
    private LocalDateTime latestValidationTime;

    @Schema(description = "状态：DRAFT草稿、PENDING_REVIEW待审核、REJECTED驳回、EFFECTIVE生效、STOPPED停用")
    private String status;

    @Schema(description = "审核人")
    private String auditBy;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "删除标志：0存在，2删除")
    private String delFlag;

    @Schema(description = "备注")
    private String remark;
}
