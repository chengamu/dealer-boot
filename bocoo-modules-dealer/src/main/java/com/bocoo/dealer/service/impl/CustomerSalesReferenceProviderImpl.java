package com.bocoo.dealer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.merchant.service.CustomerSalesReferenceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerSalesReferenceProviderImpl implements CustomerSalesReferenceProvider {
    private final SalesDocumentMapper mapper;
    @Override
    public long countByCustomer(Long tenantId, Long customerId) {
        return TenantContextHolder.callWithIgnore(() -> mapper.selectCount(new QueryWrapper<SalesDocument>()
            .eq("del_flag", "0").eq("tenant_id", tenantId).eq("customer_id", customerId)));
    }
}
