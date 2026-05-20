package com.bocoo.healthbrain.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainPlatform;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 平台管理业务对象 healthbrain_platform
 *
 * @author cmx
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HealthbrainPlatform.class, reverseConvertGenerate = false)
@Schema(description = "平台管理业务对象")
public class HealthbrainPlatformBo extends BaseEntity {

    /**
     * 
     */
    @Schema(description = "")
    private Long id;

    /**
     * 平台类型
     */
    @Schema(description = "平台类型")
    private String type;

    /**
     * 平台名称
     */
    @Schema(description = "平台名称")
    private String name;

    /**
     * 平台描述
     */
    @Schema(description = "平台描述")
    private String description;

}
