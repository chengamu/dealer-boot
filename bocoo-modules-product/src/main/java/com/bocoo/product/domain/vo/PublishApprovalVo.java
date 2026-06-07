package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.PublishApproval;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 发布审批记录视图对象
 */
@Data
@AutoMapper(target = PublishApproval.class)
@Schema(description = "发布审批记录视图对象")
public class PublishApprovalVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 发布审批记录ID
     */
    @Schema(description = "发布审批记录ID")
    private Long approvalId;

    /**
     * 审批对象类型
     */
    @Schema(description = "审批对象类型")
    private String targetType;

    /**
     * 审批对象ID
     */
    @Schema(description = "审批对象ID")
    private Long targetId;

    /**
     * 审批对象编码
     */
    @Schema(description = "审批对象编码")
    private String targetCode;

    /**
     * 审批状态：SUBMITTED已提交，APPROVED已通过，REJECTED已拒绝
     */
    @Schema(description = "审批状态：SUBMITTED已提交，APPROVED已通过，REJECTED已拒绝")
    private String approvalStatus;

    /**
     * 提交人用户ID
     */
    @Schema(description = "提交人用户ID")
    private Long submitterUserId;

    /**
     * 提交人名称
     */
    @Schema(description = "提交人名称")
    private String submitterName;

    /**
     * 审批人用户ID
     */
    @Schema(description = "审批人用户ID")
    private Long approverUserId;

    /**
     * 审批人名称
     */
    @Schema(description = "审批人名称")
    private String approverName;

    /**
     * 审批时间，UTC语义
     */
    @Schema(description = "审批时间，UTC语义")
    private LocalDateTime approvedTime;

    /**
     * 审批意见
     */
    @Schema(description = "审批意见")
    private String approvalComment;

    /**
     * 审批对象快照哈希
     */
    @Schema(description = "审批对象快照哈希")
    private String snapshotHash;

    /**
     * 审批对象快照JSON
     */
    @Schema(description = "审批对象快照JSON")
    private String snapshotJson;

    /**
     * 状态：1正常，0停用
     */
    @Schema(description = "状态：1正常，0停用")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建时间，UTC语义
     */
    @Schema(description = "创建时间，UTC语义")
    private LocalDateTime createTime;

    /**
     * 更新时间，UTC语义
     */
    @Schema(description = "更新时间，UTC语义")
    private LocalDateTime updateTime;
}
