package com.bocoo.demo.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.demo.domain.entity.DemoTechManagement;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 技术管理示例视图对象。
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DemoTechManagement.class)
@Schema(description = "技术管理示例视图对象")
public class DemoTechManagementVo {

    @ExcelProperty(value = "技术管理示例ID")
    @Schema(description = "技术管理示例ID")
    private Long managementId;

    @ExcelProperty(value = "标题")
    @Schema(description = "标题")
    private String title;

    @ExcelProperty(value = "标签")
    @Schema(description = "标签")
    private String label;

    @ExcelProperty(value = "自定义标签")
    @Schema(description = "自定义标签")
    private String customLabel;

    @ExcelProperty(value = "发布有效期")
    @Schema(description = "发布有效期")
    private String deadline;

    @ExcelProperty(value = "详细描述")
    @Schema(description = "详细描述")
    private String description;

    @ExcelProperty(value = "单位ID")
    @Schema(description = "单位ID")
    private Long enterpriseId;

    @ExcelProperty(value = "联系人")
    @Schema(description = "联系人")
    private String contact;

    @ExcelProperty(value = "联系方式")
    @Schema(description = "联系方式")
    private String contactInfo;

    @ExcelProperty(value = "图片URL")
    @Schema(description = "图片URL")
    private String img;

    @ExcelProperty(value = "状态")
    @Schema(description = "状态：DRAFT草稿，PUBLISHED已发布，EXPIRED已过期")
    private String status;

    @ExcelProperty(value = "创建人")
    @Schema(description = "创建人")
    private String createBy;

    @ExcelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
