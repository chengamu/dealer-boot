package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductFormulaOption.class, reverseConvertGenerate = false)
public class ProductFormulaOptionBo extends BaseBo {
    private Long optionId;
    private Long tenantId;
    private Long formulaId;
    private String optionRefKey;
    private String optionCode;
    private String optionNameCn;
    private String optionNameEn;
    private String sourceType;
    private String sourceScope;
    private String selectionMode;
    private String displayMode;
    private String defaultValueRefKey;
    private String defaultValueCode;
    private String defaultValueNameCn;
    private String visibilityMode;
    private String visibleConditionOptionRefKey;
    private String visibleConditionOptionCode;
    private String visibleConditionOptionNameCn;
    private String visibleConditionValueRefKey;
    private String visibleConditionValueCode;
    private String visibleConditionValueNameCn;
    private Boolean requiredFlag;
    private Boolean businessVisibleFlag;
    private Boolean helpEnabled;
    private String helpType;
    private String helpTitle;
    private String helpUrl;
    private String helpContent;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
