package com.bocoo.healthbrain.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 共享设备管理对象 healthbrain_equipment
 *
 * @author cmx
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("healthbrain_equipment")
@Schema(description = "共享设备管理对象")
public class HealthbrainEquipment extends BaseEntity {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    private String name;

    /**
     * 品牌
     */
    @Schema(description = "品牌")
    private String brand;

    /**
     * 规格型号
     */
    @Schema(description = "规格型号")
    private String specification;

    /**
     * 数量
     */
    @Schema(description = "数量")
    private Long number;

    /**
     * 服务内容
     */
    @Schema(description = "服务内容")
    private String service;

    /**
     * 所属企业
     */
    @Schema(description = "所属企业")
    private Long enterpriseId;

    /**
     * 企业名称
     */
    @Schema(description = "企业名称")
    private String enterpriseName;

    /**
     * 使用率（百分比）
     */
    @Schema(description = "使用率（百分比）")
    private BigDecimal usageRate;

    /**
     * 创建者
     */
    @Schema(description = "创建者")
    private String createBy;



}