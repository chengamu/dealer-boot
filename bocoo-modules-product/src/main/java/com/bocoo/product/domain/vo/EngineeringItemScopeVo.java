package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.EngineeringItemScope;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工程构成项可选范围视图对象")
public class EngineeringItemScopeVo extends EngineeringItemScope {
}
