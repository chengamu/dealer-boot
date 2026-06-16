package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.StandardSkuEngineering;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "标品固定工程配置视图对象")
public class StandardSkuEngineeringVo extends StandardSkuEngineering {
}
