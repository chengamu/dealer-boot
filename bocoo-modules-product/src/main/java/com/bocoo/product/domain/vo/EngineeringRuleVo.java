package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.EngineeringRule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工程能力规则视图对象")
public class EngineeringRuleVo extends EngineeringRule {
}
