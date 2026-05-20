package com.bocoo.healthbrain.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainFundData;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 资金数据视图对象 healthbrain_fund_data
 *
 * @author cmx
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = HealthbrainFundData.class)
@Schema(description = "资金数据视图对象")
public class HealthbrainFundDataVo {

    /**
     * 
     */
    @ExcelProperty(value = "")
    @Schema(description = "")
    private Long id;

    /**
     * 资金类型ID
     */
    @ExcelProperty(value = "资金类型ID")
    @Schema(description = "资金类型ID")
    private Long fundTypeId;

    /**
     * 年份
     */
    @ExcelProperty(value = "年份")
    @Schema(description = "年份")
    private String year;

    /**
     * 资金数值
     */
    @ExcelProperty(value = "资金数值")
    @Schema(description = "资金数值")
    private BigDecimal value;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    @Schema(description = "备注")
    private String remark;

    
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
