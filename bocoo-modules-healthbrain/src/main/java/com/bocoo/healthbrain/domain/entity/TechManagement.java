package com.bocoo.healthbrain.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 技术管理对象 tech_management
 *
 * @author cmx
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tech_management")
@Schema(description = "技术管理对象")
public class TechManagement extends BaseEntity {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 标题
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 标签
     */
    @Schema(description = "标签")
    private String label;

    /**
     * 自定义标签
     */
    @Schema(description = "自定义标签")
    private String customLabel;

    /**
     * 发布有效期
     */
    @Schema(description = "发布有效期")
    private String deadline;


    /**
     * 详细描述
     */
    @Schema(description = "详细描述")
    private String description;

    /**
     * 单位ID
     */
    @Schema(description = "单位ID")
    private Long enterpriseId;

    /**
     * 联系人
     */
    @Schema(description = "联系人")
    private String contact;

    /**
     * 联系方式
     */
    @Schema(description = "联系方式")
    private String contactInfo;

    /**
     * 图片URL
     */
    @Schema(description = "图片URL")
    private String img;

    /**
     * 状态(DRAFT:草稿,PUBLISHED:已发布,EXPIRED:已过期)
     */
    @Schema(description = "状态(DRAFT:草稿,PUBLISHED:已发布,EXPIRED:已过期)")
    private String status;



}