package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductImportRowIssue;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品能力导入行问题视图对象
 */
@Data
@AutoMapper(target = ProductImportRowIssue.class)
@Schema(description = "产品能力导入行问题视图对象")
public class ProductImportRowIssueVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "产品能力导入行问题ID")
    private Long issueId;

    @Schema(description = "导入批次ID")
    private Long batchId;

    @Schema(description = "行号")
    private Integer rowNo;

    @Schema(description = "字段名")
    private String columnName;

    @Schema(description = "问题级别：ERROR错误，WARNING警告")
    private String issueLevel;

    @Schema(description = "问题编码")
    private String issueCode;

    @Schema(description = "问题消息")
    private String issueMessage;

    @Schema(description = "原始行JSON")
    private String rawRowJson;

    @Schema(description = "修正后行JSON")
    private String fixedRowJson;

    @Schema(description = "状态：1待处理，2已处理，0忽略")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间，UTC语义")
    private LocalDateTime createTime;

    @Schema(description = "更新时间，UTC语义")
    private LocalDateTime updateTime;

}
