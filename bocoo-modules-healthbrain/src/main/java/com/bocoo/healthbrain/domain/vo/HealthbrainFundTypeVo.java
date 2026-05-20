package com.bocoo.healthbrain.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainFundType;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 资金类型视图对象 healthbrain_fund_type
 *
 * @author cmx
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = HealthbrainFundType.class)
@Schema(description = "资金类型视图对象")
public class HealthbrainFundTypeVo {

    /**
     * 
     */
    @ExcelProperty(value = "")
    @Schema(description = "")
    private Long id;

    /**
     * 资金类型编码
     */
    @ExcelProperty(value = "资金类型编码")
    @Schema(description = "资金类型编码")
    private String code;

    /**
     * 资金类型名称
     */
    @ExcelProperty(value = "资金类型名称")
    @Schema(description = "资金类型名称")
    private String name;

    /**
     * 计量单位
     */
    @ExcelProperty(value = "计量单位")
    @Schema(description = "计量单位")
    private String unit;

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
