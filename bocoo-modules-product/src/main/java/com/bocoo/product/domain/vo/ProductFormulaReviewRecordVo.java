package com.bocoo.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品配方审核记录视图对象
 */
@Data
@Schema(description = "产品配方审核记录视图对象")
public class ProductFormulaReviewRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long versionId;
    private Long formulaId;
    private Integer versionNo;
    private String versionLabel;
    private String actionType;
    private String actionName;
    private String operatorName;
    private LocalDateTime operateTime;
    private String versionStatus;
    private String validationStatus;
    private String rejectReason;
    private String remark;
}
