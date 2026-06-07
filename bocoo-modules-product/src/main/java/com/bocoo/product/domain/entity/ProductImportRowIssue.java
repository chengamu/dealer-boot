package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品能力导入行问题表 pc_import_row_issue
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_import_row_issue")
@Schema(description = "产品能力导入行问题表")
public class ProductImportRowIssue extends BaseEntity {

    /**
     * 产品能力导入行问题ID
     */
    @TableId(value = "issue_id")
    @Schema(description = "产品能力导入行问题ID")
    private Long issueId;

    /**
     * 导入批次ID
     */
    @Schema(description = "导入批次ID")
    private Long batchId;

    /**
     * 行号
     */
    @Schema(description = "行号")
    private Integer rowNo;

    /**
     * 字段名
     */
    @Schema(description = "字段名")
    private String columnName;

    /**
     * 问题级别：ERROR错误，WARNING警告
     */
    @Schema(description = "问题级别：ERROR错误，WARNING警告")
    private String issueLevel;

    /**
     * 问题编码
     */
    @Schema(description = "问题编码")
    private String issueCode;

    /**
     * 问题消息
     */
    @Schema(description = "问题消息")
    private String issueMessage;

    /**
     * 原始行JSON
     */
    @Schema(description = "原始行JSON")
    private String rawRowJson;

    /**
     * 修正后行JSON
     */
    @Schema(description = "修正后行JSON")
    private String fixedRowJson;

    /**
     * 状态：1待处理，2已处理，0忽略
     */
    @Schema(description = "状态：1待处理，2已处理，0忽略")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
