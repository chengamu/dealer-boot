package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductFormulaVariable;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = ProductFormulaVariable.class)
public class ProductFormulaVariableVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long variableId;
    private Long tenantId;
    private Long formulaId;
    private String variableCode;
    private String variableName;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
