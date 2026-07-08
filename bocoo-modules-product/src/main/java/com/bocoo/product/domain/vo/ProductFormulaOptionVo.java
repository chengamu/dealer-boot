package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductFormulaOption;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = ProductFormulaOption.class)
public class ProductFormulaOptionVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long optionId;
    private Long tenantId;
    private Long formulaId;
    private String optionCode;
    private String optionNameCn;
    private String optionNameEn;
    private String sourceType;
    private String sourceScope;
    private String selectionMode;
    private String displayMode;
    private String defaultValueCode;
    private String defaultValueNameCn;
    private String visibilityMode;
    private String visibleConditionOptionCode;
    private String visibleConditionOptionNameCn;
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
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
