package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 产品配方审核版本 pc_formula_version
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_formula_version")
@Schema(description = "产品配方审核版本")
public class ProductFormulaVersion extends BaseEntity {

    @TableId(value = "version_id")
    @Schema(description = "版本ID")
    private Long versionId;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "配方ID")
    private Long formulaId;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "版本展示号")
    private String versionLabel;

    @Schema(description = "版本状态")
    private String versionStatus;

    @Schema(description = "配方主表快照JSON")
    private String formulaSnapshotJson;

    @Schema(description = "配方设置快照JSON")
    private String setupSnapshotJson;

    @Schema(description = "校验状态")
    private String validationStatus;

    @Schema(description = "校验报告JSON")
    private String validationReportJson;

    @Schema(description = "提交人")
    private String submitBy;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "审核人")
    private String auditBy;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "删除标志：0存在，2删除")
    private String delFlag;

    @Schema(description = "备注")
    private String remark;
}
