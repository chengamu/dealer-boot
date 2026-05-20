package com.bocoo.healthbrain.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainTalent;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 人才管理业务对象 healthbrain_talent
 *
 * @author cmx
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HealthbrainTalent.class, reverseConvertGenerate = false)
@Schema(description = "人才管理业务对象")
public class HealthbrainTalentBo extends BaseEntity {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 人才类别
     */
    @Schema(description = "人才类别")
    private String type;

    /**
     * 人才姓名
     */
    @Schema(description = "人才姓名")
    private String name;

    /**
     * 擅长领域
     */
    @Schema(description = "擅长领域")
    private String development;

    /**
     * 工作单位(联盟内)
     */
    @Schema(description = "工作单位(联盟内)")
    private Long enterpriseId;

    /**
     * 工作单位(联盟外)
     */
    @Schema(description = "工作单位(联盟外)")
    private String enterprise;

    /**
     * 职务
     */
    @Schema(description = "职务")
    private String position;

    /**
     * 个人荣誉
     */
    @Schema(description = "个人荣誉")
    private String honor;

    /**
     * 工作/科研经历/重要成果
     */
    @Schema(description = "工作/科研经历/重要成果")
    private String experience;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatar;

}
