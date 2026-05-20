package com.bocoo.healthbrain.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainPolicy;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 政策管理业务对象 healthbrain_policy
 *
 * @author cmx
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HealthbrainPolicy.class, reverseConvertGenerate = false)
@Schema(description = "政策管理业务对象")
public class HealthbrainPolicyBo extends BaseEntity {

    /**
     * 
     */
    @Schema(description = "")
    private Long id;

    /**
     * 政策类型
     */
    @Schema(description = "政策类型")
    private String type;

    /**
     * 政策名称
     */
    @Schema(description = "政策名称")
    private String name;

    /**
     * 政策内容
     */
    @Schema(description = "政策内容")
    private String content;

}
