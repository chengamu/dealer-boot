package com.bocoo.healthbrain.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainFundType;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 资金类型业务对象 healthbrain_fund_type
 *
 * @author cmx
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HealthbrainFundType.class, reverseConvertGenerate = false)
@Schema(description = "资金类型业务对象")
public class HealthbrainFundTypeBo extends BaseEntity {

    /**
     * 
     */
    @Schema(description = "")
    private Long id;

    /**
     * 资金类型编码
     */
    @Schema(description = "资金类型编码")
    private String code;

    /**
     * 资金类型名称
     */
    @Schema(description = "资金类型名称")
    private String name;

    /**
     * 计量单位
     */
    @Schema(description = "计量单位")
    private String unit;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
