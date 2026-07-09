package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = ProductFormulaOptionMaterial.class)
public class ProductFormulaOptionMaterialVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long optionMaterialId;
    private Long tenantId;
    private Long formulaId;
    private Long optionId;
    private Long optionValueId;
    private String optionRefKey;
    private String optionCode;
    private String valueRefKey;
    private String valueCode;
    private Long formulaMaterialId;
    private Long materialId;
    private String materialCode;
    private String materialNameCn;
    private Boolean requiredFlag;
    private Boolean defaultFlag;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
