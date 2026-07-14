package com.bocoo.pay.service.impl;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.api.PaymentDocumentScopeResolver;
import com.bocoo.pay.api.PaymentScopePage;
import com.bocoo.pay.api.PlatformPaymentPageResolver;
import com.bocoo.pay.domain.bo.PaymentOrderQueryBo;
import com.bocoo.pay.domain.vo.PaymentOrderSummaryVo;
import com.bocoo.pay.service.PayBankPaymentService;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PayOrderDetailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformPaymentOrderServiceImplTest extends PayServiceTestSupport {
    @Mock private PaymentOrderQuerySupport queries;
    @Mock private PaymentDocumentScopeSupport scopes;
    @Mock private PaymentDocumentScopeResolver documentScopes;
    @Mock private PlatformPaymentPageResolver pageResolver;
    @Mock private PayOrderDetailService details;
    @Mock private PayBankPaymentService bankPaymentService;
    @Mock private PayOperatorContext operator;

    @Test
    void listUsesCurrentPageIdsInsteadOfMaterializingAllDocuments() {
        PaymentOrderQueryBo query = new PaymentOrderQueryBo();
        PageQuery pageQuery = new PageQuery();
        PaymentScopePage scopePage = new PaymentScopePage(List.of(2L), 1);
        PaymentOrderSummaryVo row = PaymentOrderSummaryVo.builder().payOrderId(2L).salesDocumentId(7L).build();
        TableDataInfo<PaymentOrderSummaryVo> page = new TableDataInfo<>(List.of(row), 1);
        when(operator.isPlatform()).thenReturn(true);
        when(pageResolver.pagePaymentOrderIds(query, pageQuery)).thenReturn(scopePage);
        when(queries.pageByIds(scopePage)).thenReturn(page);
        when(scopes.required()).thenReturn(documentScopes);
        when(documentScopes.resolveFacts(List.of(7L))).thenReturn(Map.of());
        when(queries.enrich(page, Map.of())).thenReturn(page);
        PlatformPaymentOrderServiceImpl service = new PlatformPaymentOrderServiceImpl(
            queries, scopes, pageResolver, details, bankPaymentService, operator);

        assertThat(service.list(query, pageQuery).getRows()).hasSize(1);

        verify(documentScopes, never()).accessibleDocumentIds(org.mockito.ArgumentMatchers.any());
    }
}
