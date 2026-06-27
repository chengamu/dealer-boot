package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductFormula;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 产品配方业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductFormula.class, reverseConvertGenerate = false)
@Schema(description = "产品配方业务对象")
public class ProductFormulaBo extends BaseBo {

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
    private java.time.LocalDateTime latestValidationTime;
    private String status;
    private String auditBy;
    private java.time.LocalDateTime auditTime;
    private String rejectReason;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
}
