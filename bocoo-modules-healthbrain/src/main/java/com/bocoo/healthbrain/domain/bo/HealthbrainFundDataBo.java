package com.bocoo.healthbrain.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainFundData;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金数据业务对象 healthbrain_fund_data
 *
 * @author cmx
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HealthbrainFundData.class, reverseConvertGenerate = false)
@Schema(description = "资金数据业务对象")
public class HealthbrainFundDataBo extends BaseEntity {

    /**
     * 
     */
    @Schema(description = "")
    private Long id;

    /**
     * 资金类型ID
     */
    @Schema(description = "资金类型ID")
    private Long fundTypeId;

    /**
     * 年份
     */
    @Schema(description = "年份")
    private String year;

    /**
     * 资金数值
     */
    @Schema(description = "资金数值")
    private BigDecimal value;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
