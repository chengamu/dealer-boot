package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.EngineeringPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工程方案视图对象")
public class EngineeringPlanVo extends EngineeringPlan {
}
