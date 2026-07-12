package com.bocoo.merchant.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.merchant.domain.entity.MerchantLevelDiscount;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = MerchantLevelDiscount.class, reverseConvertGenerate = false)
@Schema(description = "商户等级产品折扣业务对象")
public class MerchantLevelDiscountBo extends BaseBo {

    private Long discountId;
    private Long tenantId;

    @NotNull(message = "{merchant.level.id.required}")
    private Long levelId;
    private String levelCode;
    private String levelName;

    @NotNull(message = "{merchant.discount.category.required}")
    private Long categoryId;
    private String categoryCode;
    private String categoryNameCn;

    @NotBlank(message = "{merchant.discount.productType.required}")
    private String productTypeCode;
    private String productTypeNameCn;

    @NotNull(message = "{merchant.discount.rate.required}")
    @DecimalMin(value = "0.0000", message = "{merchant.discount.rate.range}")
    @DecimalMax(value = "1.0000", message = "{merchant.discount.rate.range}")
    @Digits(integer = 1, fraction = 4, message = "{merchant.discount.rate.range}")
    private BigDecimal discountRate;

    private Integer sortOrder;
    private String status;
    private String delFlag;

    @Size(max = 500, message = "{merchant.common.remark.max}")
    private String remark;
}
