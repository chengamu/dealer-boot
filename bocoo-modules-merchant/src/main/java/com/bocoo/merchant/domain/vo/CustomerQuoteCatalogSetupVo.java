package com.bocoo.merchant.domain.vo;

import com.bocoo.product.domain.vo.ProductFormulaOptionValueVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionVo;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class CustomerQuoteCatalogSetupVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ProductFormulaOptionVo> formulaOptions;
    private List<ProductFormulaOptionValueVo> formulaOptionValues;
}
