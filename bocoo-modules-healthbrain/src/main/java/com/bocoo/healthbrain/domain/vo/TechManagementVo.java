package com.bocoo.healthbrain.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.TechManagement;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 技术管理视图对象 tech_management
 *
 * @author cmx
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = TechManagement.class)
@Schema(description = "技术管理视图对象")
public class TechManagementVo {

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 标题
     */
    @ExcelProperty(value = "标题")
    @Schema(description = "标题")
    private String title;

    /**
     * 标签
     */
    @ExcelProperty(value = "标签")
    @Schema(description = "标签")
    private String label;

    /**
     * 自定义标签
     */
    @ExcelProperty(value = "自定义标签")
    @Schema(description = "自定义标签")
    private String customLabel;

    /**
     * 发布有效期
     */
    @ExcelProperty(value = "发布有效期")
    @Schema(description = "发布有效期")
    private String deadline;


    /**
     * 详细描述
     */
    @ExcelProperty(value = "详细描述")
    @Schema(description = "详细描述")
    private String description;

    /**
     * 单位ID
     */
    @ExcelProperty(value = "单位ID")
    @Schema(description = "单位ID")
    private Long enterpriseId;

    /**
     * 联系人
     */
    @ExcelProperty(value = "联系人")
    @Schema(description = "联系人")
    private String contact;

    /**
     * 联系方式
     */
    @ExcelProperty(value = "联系方式")
    @Schema(description = "联系方式")
    private String contactInfo;

    /**
     * 图片URL
     */
    @ExcelProperty(value = "图片URL")
    @Schema(description = "图片URL")
    private String img;

    /**
     * 状态(DRAFT:草稿,PUBLISHED:已发布,EXPIRED:已过期)
     */
    @ExcelProperty(value = "状态(DRAFT:草稿,PUBLISHED:已发布,EXPIRED:已过期)")
    @Schema(description = "状态(DRAFT:草稿,PUBLISHED:已发布,EXPIRED:已过期)")
    private String status;

    
    @ExcelProperty(value = "创建人")
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
