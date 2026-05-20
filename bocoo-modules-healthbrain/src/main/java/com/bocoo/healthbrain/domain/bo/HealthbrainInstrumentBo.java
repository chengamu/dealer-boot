package com.bocoo.healthbrain.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainInstrument;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 医疗器械管理业务对象 healthbrain_instrument
 *
 * @author cmx
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HealthbrainInstrument.class, reverseConvertGenerate = false)
@Schema(description = "医疗器械管理业务对象")
public class HealthbrainInstrumentBo extends BaseEntity {

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
