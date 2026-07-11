package com.bocoo.dealer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import com.bocoo.product.service.ProductQuoteReferenceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalesProductReferenceProvider implements ProductQuoteReferenceProvider {
    private final SalesDocumentItemMapper mapper;
    @Override public long countSaleProductReferences(Long id) { return count("sale_product_id", id); }
    @Override public long countFormulaVersionReferences(Long id) { return count("formula_version_id", id); }
    @Override public long countShippingTemplateReferences(Long id) { return count("shipping_template_id", id); }
    private long count(String column, Long id) {
        return TenantContextHolder.callWithIgnore(() -> mapper.selectCount(new QueryWrapper<SalesDocumentItem>()
            .eq("del_flag", "0").eq(column, id)));
    }
}
