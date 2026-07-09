package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductFormulaOptionValue.class, reverseConvertGenerate = false)
public class ProductFormulaOptionValueBo extends BaseBo {
    private Long optionValueId;
    private Long tenantId;
    private Long formulaId;
    private Long optionId;
    private String optionRefKey;
    private String optionCode;
    private String valueRefKey;
    private String valueCode;
    private String valueNameCn;
    private String valueNameEn;
    private Boolean defaultFlag;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
