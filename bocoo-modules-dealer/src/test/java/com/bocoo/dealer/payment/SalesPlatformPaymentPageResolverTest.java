package com.bocoo.dealer.payment;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.dealer.mapper.SalesPaymentPageMapper;
import com.bocoo.pay.domain.bo.BankTransferQueryBo;
import com.bocoo.pay.domain.bo.PaymentOrderQueryBo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesPlatformPaymentPageResolverTest {
    @Mock
    private SalesPaymentPageMapper mapper;
    @Mock
    private SalesPaymentDocumentScopeResolver documentScopes;

    @Test
    void paymentOrdersArePagedInsideTheDatabaseScope() {
        PaymentOrderQueryBo query = new PaymentOrderQueryBo();
        query.setBusinessOrigin("INTERNAL");
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(2);
        pageQuery.setPageSize(10);
        when(documentScopes.canCrossTenant()).thenReturn(true);
        when(mapper.selectPaymentOrderIds(any(), eq(query), isNull(), isNull())).thenAnswer(invocation -> {
            assertThat(TenantContextHolder.isIgnore()).isTrue();
            Page<Long> page = invocation.getArgument(0);
            page.setRecords(List.of(22L, 21L));
            page.setTotal(25);
            return page;
        });
        SalesPlatformPaymentPageResolver resolver = new SalesPlatformPaymentPageResolver(mapper, documentScopes);

        var result = resolver.pagePaymentOrderIds(query, pageQuery);

        assertThat(result.ids()).containsExactly(22L, 21L);
        assertThat(result.total()).isEqualTo(25);
    }

    @Test
    void bankTransfersArePagedInsideTheDatabaseScope() {
        BankTransferQueryBo query = new BankTransferQueryBo();
        PageQuery pageQuery = new PageQuery();
        when(documentScopes.canCrossTenant()).thenReturn(true);
        when(mapper.selectBankTransferIds(any(), eq(query), isNull(), isNull())).thenAnswer(invocation -> {
            assertThat(TenantContextHolder.isIgnore()).isTrue();
            Page<Long> page = invocation.getArgument(0);
            page.setRecords(List.of(32L, 31L));
            page.setTotal(12);
            return page;
        });
        SalesPlatformPaymentPageResolver resolver = new SalesPlatformPaymentPageResolver(mapper, documentScopes);

        var result = resolver.pageBankTransferIds(query, pageQuery);

        assertThat(result.ids()).containsExactly(32L, 31L);
        assertThat(result.total()).isEqualTo(12);
    }
}
