package com.bocoo.healthbrain.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainMilestone;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 里程碑数据业务对象 healthbrain_milestone
 *
 * @author cmx
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HealthbrainMilestone.class, reverseConvertGenerate = false)
@Schema(description = "里程碑数据业务对象")
public class HealthbrainMilestoneBo extends BaseEntity {

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

    @Schema(description = "显示状态")
    private String visible;
}
