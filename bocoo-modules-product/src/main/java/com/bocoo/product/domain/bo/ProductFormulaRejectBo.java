package com.bocoo.product.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 产品配方驳回业务对象
 */
@Data
@Schema(description = "产品配方驳回业务对象")
public class ProductFormulaRejectBo {

    @NotBlank(message = "驳回原因不能为空")
    @Schema(description = "驳回原因")
    private String rejectReason;
}
