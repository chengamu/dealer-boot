package com.bocoo.product.domain.bo;

import com.bocoo.product.domain.entity.StandardSkuEngineering;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "标品固定工程配置业务对象")
public class StandardSkuEngineeringBo extends StandardSkuEngineering {
}
