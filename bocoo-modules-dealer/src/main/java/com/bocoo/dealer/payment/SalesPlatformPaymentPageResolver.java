package com.bocoo.dealer.payment;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.dealer.mapper.SalesPaymentPageMapper;
import com.bocoo.dealer.scope.SalesBusinessScope;
import com.bocoo.pay.api.PaymentScopePage;
import com.bocoo.pay.api.PlatformPaymentPageResolver;
import com.bocoo.pay.domain.bo.BankTransferQueryBo;
import com.bocoo.pay.domain.bo.PaymentOrderQueryBo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class SalesPlatformPaymentPageResolver implements PlatformPaymentPageResolver {
    private final SalesPaymentPageMapper mapper;
    private final SalesPaymentDocumentScopeResolver documentScopes;

    @Override
    public PaymentScopePage pagePaymentOrderIds(PaymentOrderQueryBo query, PageQuery pageQuery) {
        SalesBusinessScope scope = documentScopes.canCrossTenant() ? null : SalesBusinessScope.current();
        Page<Long> page = select(() -> mapper.selectPaymentOrderIds(pageQuery.build(), query,
            scope == null ? null : scope.tenantId(), scope == null ? null : scope.businessOrigin()));
        return new PaymentScopePage(page.getRecords(), page.getTotal());
    }

    @Override
    public PaymentScopePage pageBankTransferIds(BankTransferQueryBo query, PageQuery pageQuery) {
        SalesBusinessScope scope = documentScopes.canCrossTenant() ? null : SalesBusinessScope.current();
        Page<Long> page = select(() -> mapper.selectBankTransferIds(pageQuery.build(), query,
            scope == null ? null : scope.tenantId(), scope == null ? null : scope.businessOrigin()));
        return new PaymentScopePage(page.getRecords(), page.getTotal());
    }

    private <T> T select(Supplier<T> supplier) {
        return documentScopes.canCrossTenant() ? TenantContextHolder.callWithIgnore(supplier) : supplier.get();
    }
}
