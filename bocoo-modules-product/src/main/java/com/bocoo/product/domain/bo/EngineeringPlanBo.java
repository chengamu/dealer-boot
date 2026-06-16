package com.bocoo.product.domain.bo;

import com.bocoo.product.domain.entity.EngineeringPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工程方案业务对象")
public class EngineeringPlanBo extends EngineeringPlan {
}
