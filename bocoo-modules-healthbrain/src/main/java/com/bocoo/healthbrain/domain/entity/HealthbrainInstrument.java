package com.bocoo.healthbrain.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 医疗器械管理对象 healthbrain_instrument
 *
 * @author cmx
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("healthbrain_instrument")
@Schema(description = "医疗器械管理对象")
public class HealthbrainInstrument extends BaseEntity {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 企业名称
     */
    @Schema(description = "企业名称")
    private String enterpriseId;

    /**
     * 医疗器械名称
     */
    @Schema(description = "医疗器械名称")
    private String name;

    /**
     * 注册编号
     */
    @Schema(description = "注册编号")
    private String number;

    /**
     * 等级
     */
    @Schema(description = "等级")
    private String level;



}