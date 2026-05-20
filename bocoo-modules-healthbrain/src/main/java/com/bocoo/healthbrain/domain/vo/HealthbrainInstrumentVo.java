package com.bocoo.healthbrain.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainInstrument;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 医疗器械管理视图对象 healthbrain_instrument
 *
 * @author cmx
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = HealthbrainInstrument.class)
@Schema(description = "医疗器械管理视图对象")
public class HealthbrainInstrumentVo {

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 企业名称
     */
    @ExcelProperty(value = "企业名称")
    @Schema(description = "企业名称")
    private String enterpriseId;

    /**
     * 医疗器械名称
     */
    @ExcelProperty(value = "医疗器械名称")
    @Schema(description = "医疗器械名称")
    private String name;

    /**
     * 注册编号
     */
    @ExcelProperty(value = "注册编号")
    @Schema(description = "注册编号")
    private String number;

    /**
     * 等级
     */
    @ExcelProperty(value = "等级")
    @Schema(description = "等级")
    private String level;

    
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
