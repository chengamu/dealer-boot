package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductFormulaVariable;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductFormulaVariable.class, reverseConvertGenerate = false)
public class ProductFormulaVariableBo extends BaseBo {
    private Long variableId;
    private Long tenantId;
    private Long formulaId;
    private String variableCode;
    private String variableName;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
