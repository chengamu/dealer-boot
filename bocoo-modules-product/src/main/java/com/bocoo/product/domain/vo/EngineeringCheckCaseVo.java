package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.EngineeringCheckCase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工程规则测试用例视图对象")
public class EngineeringCheckCaseVo extends EngineeringCheckCase {
}
