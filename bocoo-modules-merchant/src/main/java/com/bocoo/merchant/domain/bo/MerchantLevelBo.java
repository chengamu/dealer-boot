package com.bocoo.merchant.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.merchant.domain.entity.MerchantLevel;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = MerchantLevel.class, reverseConvertGenerate = false)
@Schema(description = "商户等级业务对象")
public class MerchantLevelBo extends BaseBo {

    private Long levelId;
    private Long tenantId;

    @NotBlank(message = "{merchant.level.code.required}")
    @Size(max = 50, message = "{merchant.level.code.max}")
    private String levelCode;

    @NotBlank(message = "{merchant.level.name.required}")
    @Size(max = 100, message = "{merchant.level.name.max}")
    private String levelName;

    @NotNull(message = "{merchant.level.discount.required}")
    @DecimalMin(value = "0.0000", message = "{merchant.discount.rate.range}")
    @DecimalMax(value = "1.0000", message = "{merchant.discount.rate.range}")
    private BigDecimal defaultDiscountRate;

    @NotNull(message = "{merchant.level.credit.required}")
    @DecimalMin(value = "0.00", message = "{merchant.level.credit.range}")
    private BigDecimal defaultCreditLimit;

    private Boolean defaultFlag;
    private Integer sortOrder;
    private String status;
    private String delFlag;

    @Size(max = 500, message = "{merchant.common.remark.max}")
    private String remark;
}
