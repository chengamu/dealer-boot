package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.entity.ProductPriceMaterial;
import com.bocoo.product.domain.entity.ProductPriceMaterialRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.mapper.ProductPriceMaterialMapper;
import com.bocoo.product.mapper.ProductPriceMaterialRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import com.bocoo.product.mapper.ProductSaleProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ProductFormulaVersionReferenceGuard extends ProductServiceSupport {

    private final ProductSaleProductMapper saleProductMapper;
    private final ProductPriceSettingMapper priceSettingMapper;
    private final ProductPriceMaterialMapper priceMaterialMapper;
    private final ProductPriceMaterialRuleMapper priceMaterialRuleMapper;
    private final ProductQuoteReferenceGuard quoteReferenceGuard;

    void assertNoBusinessReference(Long formulaVersionId) {
        if (formulaVersionId == null) {
            return;
        }
        if (hasReference(formulaVersionId)) {
            throw ServiceException.ofMessageKey("product.formula.withdrawReferenced");
        }
    }

    private boolean hasReference(Long formulaVersionId) {
        return countSaleProducts(formulaVersionId) > 0
            || countPriceSettings(formulaVersionId) > 0
            || countPriceMaterials(formulaVersionId) > 0
            || countPriceMaterialRules(formulaVersionId) > 0
            || quoteReferenceGuard.countFormulaVersionReferences(formulaVersionId) > 0;
    }

    private long countSaleProducts(Long formulaVersionId) {
        return saleProductMapper.selectCount(activeQuery(ProductSaleProduct.class)
            .eq("formula_version_id", formulaVersionId));
    }

    private long countPriceSettings(Long formulaVersionId) {
        return priceSettingMapper.selectCount(activeQuery(ProductPriceSetting.class)
            .eq("formula_version_id", formulaVersionId));
    }

    private long countPriceMaterials(Long formulaVersionId) {
        return priceMaterialMapper.selectCount(activeQuery(ProductPriceMaterial.class)
            .eq("formula_version_id", formulaVersionId));
    }

    private long countPriceMaterialRules(Long formulaVersionId) {
        return priceMaterialRuleMapper.selectCount(activeQuery(ProductPriceMaterialRule.class)
            .eq("formula_version_id", formulaVersionId));
    }

}
