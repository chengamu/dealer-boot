package com.bocoo.healthbrain.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainPlatform;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 平台管理视图对象 healthbrain_platform
 *
 * @author cmx
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = HealthbrainPlatform.class)
@Schema(description = "平台管理视图对象")
public class HealthbrainPlatformVo {

    /**
     * 
     */
    @ExcelProperty(value = "")
    @Schema(description = "")
    private Long id;

    /**
     * 平台类型
     */
    @ExcelProperty(value = "平台类型")
    @Schema(description = "平台类型")
    private String type;

    /**
     * 平台名称
     */
    @ExcelProperty(value = "平台名称")
    @Schema(description = "平台名称")
    private String name;

    /**
     * 平台描述
     */
    @ExcelProperty(value = "平台描述")
    @Schema(description = "平台描述")
    private String description;

    
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
