package com.bocoo.healthbrain.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainPolicy;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 政策管理视图对象 healthbrain_policy
 *
 * @author cmx
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = HealthbrainPolicy.class)
@Schema(description = "政策管理视图对象")
public class HealthbrainPolicyVo {

    /**
     * 
     */
    @ExcelProperty(value = "")
    @Schema(description = "")
    private Long id;

    /**
     * 政策类型
     */
    @ExcelProperty(value = "政策类型")
    @Schema(description = "政策类型")
    private String type;

    /**
     * 政策名称
     */
    @ExcelProperty(value = "政策名称")
    @Schema(description = "政策名称")
    private String name;

    /**
     * 政策内容
     */
    @ExcelProperty(value = "政策内容")
    @Schema(description = "政策内容")
    private String content;

    
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
