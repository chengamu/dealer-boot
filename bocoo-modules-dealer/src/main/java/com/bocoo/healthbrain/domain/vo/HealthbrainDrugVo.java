package com.bocoo.healthbrain.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainDrug;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 创新药管理视图对象 healthbrain_drug
 *
 * @author cmx
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = HealthbrainDrug.class)
@Schema(description = "创新药管理视图对象")
public class HealthbrainDrugVo {

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
     * 研发管线名称
     */
    @ExcelProperty(value = "研发管线名称")
    @Schema(description = "研发管线名称")
    private String name;

    /**
     * 适应症
     */
    @ExcelProperty(value = "适应症")
    @Schema(description = "适应症")
    private String indication;

    /**
     * 所处阶段
     */
    @ExcelProperty(value = "所处阶段")
    @Schema(description = "所处阶段")
    private String phase;

    
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
