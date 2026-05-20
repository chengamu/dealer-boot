package com.bocoo.healthbrain.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainDrug;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 创新药管理业务对象 healthbrain_drug
 *
 * @author cmx
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HealthbrainDrug.class, reverseConvertGenerate = false)
@Schema(description = "创新药管理业务对象")
public class HealthbrainDrugBo extends BaseEntity {

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
     * 研发管线名称
     */
    @Schema(description = "研发管线名称")
    private String name;

    /**
     * 适应症
     */
    @Schema(description = "适应症")
    private String indication;

    /**
     * 所处阶段
     */
    @Schema(description = "所处阶段")
    private String phase;

}
