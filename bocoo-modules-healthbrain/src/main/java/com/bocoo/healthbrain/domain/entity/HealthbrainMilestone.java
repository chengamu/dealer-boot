package com.bocoo.healthbrain.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Date;
import java.time.LocalDateTime;

/**
 * 里程碑数据对象 healthbrain_milestone
 *
 * @author cmx
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("healthbrain_milestone")
@Schema(description = "里程碑数据对象")
public class HealthbrainMilestone extends BaseEntity {

    /**
     * 
     */
    @Schema(description = "")
    private Long id;

    /**
     * 里程碑时间
     */
    @Schema(description = "里程碑时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    /**
     * 里程碑名称
     */
    @Schema(description = "里程碑名称")
    private String name;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


    /**
     * 显示状态
     */
    @Schema(description = "显示状态")
    private String visible;



}
