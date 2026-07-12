package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.core.service.I18nService;
import com.bocoo.common.core.utils.SpringUtils;
import com.bocoo.pay.domain.bo.CreditAccountQueryBo;
import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import com.bocoo.pay.domain.vo.CreditAccountSummaryVo;
import com.bocoo.pay.mapper.MerchantCreditAccountMapper;
import com.bocoo.pay.mapper.MerchantCreditTransactionMapper;
import com.bocoo.pay.mapper.MerchantReceivableMapper;
import com.bocoo.pay.service.PayOperatorContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayCreditQueryServiceImplTest extends PayServiceTestSupport {
    @Mock
    private MerchantCreditAccountMapper accountMapper;
    @Mock
    private MerchantCreditTransactionMapper transactionMapper;
    @Mock
    private MerchantReceivableMapper receivableMapper;
    @Mock
    private PayOperatorContext operator;

    @BeforeAll
    static void initializeMessageContext() {
        I18nService i18n = org.mockito.Mockito.mock(I18nService.class);
        when(i18n.get(any(java.util.Locale.class), any(String.class), any(Object[].class))).thenReturn("success");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean(I18nService.class, () -> i18n);
        context.register(SpringUtils.class);
        context.refresh();
    }

    @Test
    void accountPageConvertsRowsAndPreservesTotal() {
        MerchantCreditAccount account = new MerchantCreditAccount();
        account.setCreditAccountId(11L);
        account.setTenantId(22L);
        account.setMerchantName("Dealer A");
        account.setCreditLimit(new BigDecimal("1000.00"));
        account.setUsedCredit(new BigDecimal("125.00"));
        account.setCurrency("USD");
        account.setStatus("NORMAL");
        Page<MerchantCreditAccount> page = new Page<>(1, 20, 37);
        page.setRecords(List.of(account));
        when(operator.isPlatform()).thenReturn(true);
        when(accountMapper.selectPage(any(), any())).thenReturn(page);
        PayCreditQueryServiceImpl service = new PayCreditQueryServiceImpl(
            accountMapper, transactionMapper, receivableMapper, operator);

        TableDataInfo<CreditAccountSummaryVo> result = service.accounts(new CreditAccountQueryBo(), new PageQuery());

        assertThat(result.getTotal()).isEqualTo(37);
        assertThat(result.getRows()).singleElement().satisfies(row -> {
            assertThat(row.getCreditAccountId()).isEqualTo(11L);
            assertThat(row.getAvailableCredit()).isEqualByComparingTo("875.00");
        });
    }
}
