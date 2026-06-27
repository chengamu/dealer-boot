package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductFormula;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品配方视图对象
 */
@Data
@AutoMapper(target = ProductFormula.class)
@Schema(description = "产品配方视图对象")
public class ProductFormulaVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long formulaId;
    private Long tenantId;
    private String formulaCode;
    private String formulaName;
    private Long categoryId;
    private String categoryCode;
    private String categoryNameCn;
    private String productTypeCode;
    private String productTypeNameCn;
    private BigDecimal maxWidthInch;
    private BigDecimal maxHeightInch;
    private String sizeSummary;
    private Integer materialLineCount;
    private Boolean configuredFlag;
    private Long currentVersionId;
    private Integer currentVersionNo;
    private String currentVersionLabel;
    private Integer draftVersionNo;
    private String latestValidationStatus;
    private String latestValidationMessage;
    private LocalDateTime latestValidationTime;
    private String status;
    private String auditBy;
    private LocalDateTime auditTime;
    private String rejectReason;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
