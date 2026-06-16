package com.bocoo.product.domain.bo;

import com.bocoo.product.domain.entity.EngineeringItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工程构成项业务对象")
public class EngineeringItemBo extends EngineeringItem {
}
