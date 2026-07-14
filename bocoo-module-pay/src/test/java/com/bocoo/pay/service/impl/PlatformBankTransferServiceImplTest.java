package com.bocoo.pay.service.impl;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.api.PaymentScopePage;
import com.bocoo.pay.api.PlatformPaymentPageResolver;
import com.bocoo.pay.domain.bo.BankTransferQueryBo;
import com.bocoo.pay.domain.vo.BankTransferSummaryVo;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.service.PayBankPaymentService;
import com.bocoo.pay.service.PayOperatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformBankTransferServiceImplTest extends PayServiceTestSupport {
    @Mock private PayOrderExtensionMapper extensionMapper;
    @Mock private PayOrderMapper orderMapper;
    @Mock private PayBankPaymentService bankPaymentService;
    @Mock private PaymentDocumentScopeSupport scopes;
    @Mock private PlatformPaymentPageResolver pageResolver;
    @Mock private BankTransferQuerySupport queries;
    @Mock private PayOperatorContext operator;

    @Test
    void listUsesDatabasePageInsteadOfMaterializingAllDocumentsAndOrders() {
        BankTransferQueryBo query = new BankTransferQueryBo();
        PageQuery pageQuery = new PageQuery();
        PaymentScopePage scopePage = new PaymentScopePage(List.of(2L), 1);
        TableDataInfo<BankTransferSummaryVo> page = new TableDataInfo<>(List.of(
            BankTransferSummaryVo.builder().extensionId(2L).build()), 1);
        when(operator.isPlatform()).thenReturn(true);
        when(pageResolver.pageBankTransferIds(query, pageQuery)).thenReturn(scopePage);
        when(queries.pageByIds(scopePage)).thenReturn(page);
        PlatformBankTransferServiceImpl service = new PlatformBankTransferServiceImpl(extensionMapper, orderMapper,
            bankPaymentService, scopes, pageResolver, queries, operator);

        assertThat(service.list(query, pageQuery).getRows()).hasSize(1);

        verify(scopes, never()).required();
        verify(orderMapper, never()).selectList(org.mockito.ArgumentMatchers.any());
    }
}
