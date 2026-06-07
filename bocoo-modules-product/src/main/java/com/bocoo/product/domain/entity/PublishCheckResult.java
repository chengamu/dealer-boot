package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发布检查结果表 pc_publish_check_result
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_publish_check_result")
@Schema(description = "发布检查结果表")
public class PublishCheckResult extends BaseEntity {

    /**
     * 发布检查结果ID
     */
    @TableId(value = "check_id")
    @Schema(description = "发布检查结果ID")
    private Long checkId;

    /**
     * 检查对象类型
     */
    @Schema(description = "检查对象类型")
    private String targetType;

    /**
     * 检查对象ID
     */
    @Schema(description = "检查对象ID")
    private Long targetId;

    /**
     * 检查对象编码
     */
    @Schema(description = "检查对象编码")
    private String targetCode;

    /**
     * 检查项编码
     */
    @Schema(description = "检查项编码")
    private String checkCode;

    /**
     * 检查项中文名称
     */
    @Schema(description = "检查项中文名称")
    private String checkNameCn;

    /**
     * 检查项英文名称
     */
    @Schema(description = "检查项英文名称")
    private String checkNameEn;

    /**
     * 检查等级：PASS通过，WARNING警告，BLOCKER阻断
     */
    @Schema(description = "检查等级：PASS通过，WARNING警告，BLOCKER阻断")
    private String checkLevel;

    /**
     * 检查状态：PASS通过，WARNING警告，BLOCKER阻断
     */
    @Schema(description = "检查状态：PASS通过，WARNING警告，BLOCKER阻断")
    private String checkStatus;

    /**
     * 消息国际化key
     */
    @Schema(description = "消息国际化key")
    private String messageKey;

    /**
     * 中文消息
     */
    @Schema(description = "中文消息")
    private String messageCn;

    /**
     * 英文消息
     */
    @Schema(description = "英文消息")
    private String messageEn;

    /**
     * 检查证据快照JSON
     */
    @Schema(description = "检查证据快照JSON")
    private String evidenceJson;

    /**
     * 是否已处理：1是，0否
     */
    @Schema(description = "是否已处理：1是，0否")
    private String resolvedFlag;

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
