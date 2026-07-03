package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配方配置项可选值 pc_formula_option_value
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_formula_option_value")
@Schema(description = "配方配置项可选值")
public class ProductFormulaOptionValue extends BaseEntity {

    @TableId(value = "option_value_id")
    private Long optionValueId;

    private Long tenantId;
    private Long formulaId;
    private Long optionId;
    private String optionCode;
    private String valueCode;
    private String valueNameCn;
    private String valueNameEn;
    private Boolean defaultFlag;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
