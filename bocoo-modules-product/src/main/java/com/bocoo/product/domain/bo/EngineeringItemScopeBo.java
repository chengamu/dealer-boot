package com.bocoo.product.domain.bo;

import com.bocoo.product.domain.entity.EngineeringItemScope;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工程构成项可选范围业务对象")
public class EngineeringItemScopeBo extends EngineeringItemScope {
}
