package com.bocoo.healthbrain.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainEquipment;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 共享设备管理视图对象 healthbrain_equipment
 *
 * @author cmx
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = HealthbrainEquipment.class)
@Schema(description = "共享设备管理视图对象")
public class HealthbrainEquipmentVo {

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 设备名称
     */
    @ExcelProperty(value = "设备名称")
    @Schema(description = "设备名称")
    private String name;

    /**
     * 品牌
     */
    @ExcelProperty(value = "品牌")
    @Schema(description = "品牌")
    private String brand;

    /**
     * 规格型号
     */
    @ExcelProperty(value = "规格型号")
    @Schema(description = "规格型号")
    private String specification;

    /**
     * 数量
     */
    @ExcelProperty(value = "数量")
    @Schema(description = "数量")
    private Long number;

    /**
     * 服务内容
     */
    @ExcelProperty(value = "服务内容")
    @Schema(description = "服务内容")
    private String service;

    /**
     * 所属企业
     */
    @ExcelProperty(value = "所属企业")
    @Schema(description = "所属企业")
    private Long enterpriseId;

    /**
     * 企业名称
     */
    @ExcelProperty(value = "企业名称")
    @Schema(description = "企业名称")
    private String enterpriseName;

    /**
     * 使用率（百分比）
     */
    @ExcelProperty(value = "使用率")
    @Schema(description = "使用率（百分比）")
    private BigDecimal usageRate;

    
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
