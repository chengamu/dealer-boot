package com.bocoo.dealer.domain.vo;

import com.bocoo.product.domain.vo.ProductFormulaOptionValueVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionVo;
import com.bocoo.product.domain.vo.ProductSaleProductVo;
import lombok.Data;

import java.util.List;

@Data
public class SalesProductSetupVo {
    private ProductSaleProductVo saleProduct;
    private List<ProductFormulaOptionVo> options;
    private List<ProductFormulaOptionValueVo> optionValues;
}
