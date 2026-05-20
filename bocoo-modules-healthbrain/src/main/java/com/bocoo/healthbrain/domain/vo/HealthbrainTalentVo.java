package com.bocoo.healthbrain.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainTalent;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 人才管理视图对象 healthbrain_talent
 *
 * @author cmx
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = HealthbrainTalent.class)
@Schema(description = "人才管理视图对象")
public class HealthbrainTalentVo {

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 人才类别
     */
    @ExcelProperty(value = "人才类别")
    @Schema(description = "人才类别")
    private String type;

    /**
     * 人才姓名
     */
    @ExcelProperty(value = "人才姓名")
    @Schema(description = "人才姓名")
    private String name;

    /**
     * 擅长领域
     */
    @ExcelProperty(value = "擅长领域")
    @Schema(description = "擅长领域")
    private String development;

    /**
     * 工作单位(联盟内)
     */
    @ExcelProperty(value = "工作单位(联盟内)")
    @Schema(description = "工作单位(联盟内)")
    private Long enterpriseId;

    /**
     * 工作单位(联盟外)
     */
    @ExcelProperty(value = "工作单位(联盟外)")
    @Schema(description = "工作单位(联盟外)")
    private String enterprise;

    /**
     * 职务
     */
    @ExcelProperty(value = "职务")
    @Schema(description = "职务")
    private String position;

    /**
     * 个人荣誉
     */
    @ExcelProperty(value = "个人荣誉")
    @Schema(description = "个人荣誉")
    private String honor;

    /**
     * 工作/科研经历/重要成果
     */
    @ExcelProperty(value = "工作/科研经历/重要成果")
    @Schema(description = "工作/科研经历/重要成果")
    private String experience;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    @Schema(description = "备注")
    private String remark;

    /**
     * 头像URL
     */
    @ExcelProperty(value = "头像URL")
    @Schema(description = "头像URL")
    private String avatar;

    
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
