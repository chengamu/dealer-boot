package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductSaleProductEnableGuard extends ProductServiceSupport {

    private static final String STATUS_EFFECTIVE = ProductFormulaServiceImpl.STATUS_EFFECTIVE;

    private final ProductFormulaMapper formulaMapper;
    private final ProductFormulaVersionMapper versionMapper;

    void assertCanEnable(ProductSaleProduct product) {
        ProductFormula formula = formulaMapper.selectActiveByIdForUpdate(product.getFormulaId());
        if (formula == null || !STATUS_EFFECTIVE.equals(formula.getStatus())) {
            throw ServiceException.ofMessageKey("product.saleProduct.formulaEffectiveRequired");
        }
        ProductFormulaVersion version = product.getFormulaVersionId() == null ? null : versionMapper.selectById(product.getFormulaVersionId());
        if (version == null
            || !"0".equals(StringUtils.blankToDefault(version.getDelFlag(), "0"))
            || !STATUS_EFFECTIVE.equals(version.getVersionStatus())) {
            throw ServiceException.ofMessageKey("product.saleProduct.formulaEffectiveRequired");
        }
    }
}
