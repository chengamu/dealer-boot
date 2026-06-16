package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.EngineeringPlanVersion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工程方案版本视图对象")
public class EngineeringPlanVersionVo extends EngineeringPlanVersion {
}
