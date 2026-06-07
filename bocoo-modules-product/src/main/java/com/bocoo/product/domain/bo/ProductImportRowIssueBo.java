package com.bocoo.product.domain.bo;

import com.bocoo.product.domain.entity.ProductImportRowIssue;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 产品能力导入行问题业务对象
 */
@Data
@AutoMapper(target = ProductImportRowIssue.class, reverseConvertGenerate = false)
@Schema(description = "产品能力导入行问题业务对象")
public class ProductImportRowIssueBo {

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

}
