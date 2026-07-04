package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_formula_variable")
@Schema(description = "配方内部变量")
public class ProductFormulaVariable extends BaseEntity {

    @TableId(value = "variable_id")
    private Long variableId;

    private Long tenantId;
    private Long formulaId;
    private String variableKey;
    private String variableCode;
    private String variableName;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
