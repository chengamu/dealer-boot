package com.bocoo.demo.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 技术管理示例对象 tech_management。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tech_management")
@Schema(description = "技术管理示例对象")
public class DemoTechManagement extends BaseEntity {

    @TableId(value = "management_id")
    @Schema(description = "技术管理示例ID")
    private Long managementId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "标签")
    private String label;

    @Schema(description = "自定义标签")
    private String customLabel;

    @Schema(description = "发布有效期")
    private String deadline;

    @Schema(description = "详细描述")
    private String description;

    @Schema(description = "单位ID")
    private Long enterpriseId;

    @Schema(description = "联系人")
    private String contact;

    @Schema(description = "联系方式")
    private String contactInfo;

    @Schema(description = "图片URL")
    private String img;

    @Schema(description = "状态：DRAFT草稿，PUBLISHED已发布，EXPIRED已过期")
    private String status;
}
