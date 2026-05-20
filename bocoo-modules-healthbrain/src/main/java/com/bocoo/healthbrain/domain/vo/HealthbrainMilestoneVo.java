package com.bocoo.healthbrain.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainMilestone;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 里程碑数据视图对象 healthbrain_milestone
 *
 * @author cmx
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = HealthbrainMilestone.class)
@Schema(description = "里程碑数据视图对象")
public class HealthbrainMilestoneVo {

    /**
     * 
     */
    @ExcelProperty(value = "")
    @Schema(description = "")
    private Long id;

    /**
     * 里程碑时间
     */
    @ExcelProperty(value = "里程碑时间")
    @Schema(description = "里程碑时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime time;

    /**
     * 里程碑名称
     */
    @ExcelProperty(value = "里程碑名称")
    @Schema(description = "里程碑名称")
    private String name;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    @Schema(description = "备注")
    private String remark;

    @ExcelProperty(value = "显示状态")
    @Schema(description = "显示状态")
    private String visible;

    
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
