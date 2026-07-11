package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import com.bocoo.merchant.mapper.CustomerQuoteItemMapper;
import com.bocoo.product.service.ProductQuoteReferenceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class CustomerQuoteProductReferenceProvider implements ProductQuoteReferenceProvider {

    private final CustomerQuoteItemMapper itemMapper;

    @Override
    public long countSaleProductReferences(Long saleProductId) {
        return count("sale_product_id", saleProductId);
    }

    @Override
    public long countFormulaVersionReferences(Long formulaVersionId) {
        return count("formula_version_id", formulaVersionId);
    }

    @Override
    public long countShippingTemplateReferences(Long shippingTemplateId) {
        return count("shipping_template_id", shippingTemplateId);
    }

    private long count(String column, Long id) {
        if (id == null) {
            return 0;
        }
        Supplier<Long> query = () -> itemMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CustomerQuoteItem>()
            .eq("del_flag", "0").eq(column, id));
        return TenantContextHolder.callWithIgnore(query);
    }
}
