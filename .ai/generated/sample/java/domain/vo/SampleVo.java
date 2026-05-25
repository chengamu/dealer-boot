package com.bocoo.demo.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.demo.domain.entity.Sample;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 样本视图对象 sample_record
 *
 * @author validator
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Sample.class)
@Schema(description = "样本视图对象")
public class SampleVo {

    /**
     * 样本ID
     */
    @ExcelProperty(value = "样本ID")
    @Schema(description = "样本ID")
    private Long id;

    /**
     * 样本名称
     */
    @ExcelProperty(value = "样本名称")
    @Schema(description = "样本名称")
    private String sampleName;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态")
    @Schema(description = "状态")
    private String status;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;


    @ExcelProperty(value = "创建人")
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
