package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配方配置项 pc_formula_option
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_formula_option")
@Schema(description = "配方配置项")
public class ProductFormulaOption extends BaseEntity {

    @TableId(value = "option_id")
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
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
