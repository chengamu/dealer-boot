package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 发布审批记录表 pc_publish_approval
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_publish_approval")
@Schema(description = "发布审批记录表")
public class PublishApproval extends BaseEntity {

    /**
     * 发布审批记录ID
     */
    @TableId(value = "approval_id")
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

}
