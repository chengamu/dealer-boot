package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductFormulaVersion;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品配方版本视图对象
 */
@Data
@AutoMapper(target = ProductFormulaVersion.class)
@Schema(description = "产品配方版本视图对象")
public class ProductFormulaVersionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long versionId;
    private Long tenantId;
    private Long formulaId;
    private Integer versionNo;
    private String versionLabel;
    private String versionStatus;
    private String formulaSnapshotJson;
    private String setupSnapshotJson;
    private String validationStatus;
    private String validationReportJson;
    private String submitBy;
    private LocalDateTime submitTime;
    private String auditBy;
    private LocalDateTime auditTime;
    private String rejectReason;
    private String delFlag;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
