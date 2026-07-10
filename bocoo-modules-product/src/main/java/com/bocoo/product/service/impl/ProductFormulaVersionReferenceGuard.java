package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.entity.ProductPriceFabric;
import com.bocoo.product.domain.entity.ProductPriceFabricRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.mapper.ProductPriceFabricMapper;
import com.bocoo.product.mapper.ProductPriceFabricRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import com.bocoo.product.mapper.ProductSaleProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ProductFormulaVersionReferenceGuard extends ProductServiceSupport {

    private final ProductSaleProductMapper saleProductMapper;
    private final ProductPriceSettingMapper priceSettingMapper;
    private final ProductPriceFabricMapper priceFabricMapper;
    private final ProductPriceFabricRuleMapper priceFabricRuleMapper;

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
            || countPriceFabrics(formulaVersionId) > 0
            || countPriceFabricRules(formulaVersionId) > 0;
    }

    private long countSaleProducts(Long formulaVersionId) {
        return saleProductMapper.selectCount(activeQuery(ProductSaleProduct.class)
            .eq("formula_version_id", formulaVersionId));
    }

    private long countPriceSettings(Long formulaVersionId) {
        return priceSettingMapper.selectCount(activeQuery(ProductPriceSetting.class)
            .eq("formula_version_id", formulaVersionId));
    }

    private long countPriceFabrics(Long formulaVersionId) {
        return priceFabricMapper.selectCount(activeQuery(ProductPriceFabric.class)
            .eq("formula_version_id", formulaVersionId));
    }

    private long countPriceFabricRules(Long formulaVersionId) {
        return priceFabricRuleMapper.selectCount(activeQuery(ProductPriceFabricRule.class)
            .eq("formula_version_id", formulaVersionId));
    }

}
