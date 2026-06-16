package com.bocoo.product.domain.bo;

import com.bocoo.product.domain.entity.EngineeringOutputRule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工程输出规则业务对象")
public class EngineeringOutputRuleBo extends EngineeringOutputRule {
}
