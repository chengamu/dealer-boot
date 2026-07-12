package com.bocoo.pay.service.impl;

import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.vo.BankCollectionAccountVo;
import com.bocoo.pay.mapper.PayChannelMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.service.PayOperatorContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayChannelQueryServiceImplTest extends PayServiceTestSupport {
    @Mock
    private PayChannelMapper channelMapper;
    @Mock
    private PayOrderMapper orderMapper;
    @Mock
    private PayOperatorContext operator;

    @Test
    void bankAccountsAreOrderScopedFilteredAndMapped() {
        PayOrder order = new PayOrder();
        order.setId(10L);
        order.setPayerTenantId(20L);
        order.setPayeeTenantId(1L);
        order.setAppId(30L);
        PayChannel channel = new PayChannel();
        channel.setId(40L);
        channel.setConfig("{\"secret\":\"hidden\",\"bankAccounts\":["
            + "{\"id\":\"usd-1\",\"bankName\":\"Demo Bank\",\"accountHolder\":\"Bocoo\","
            + "\"accountNumber\":\"1234 5678 9012\",\"currency\":\"USD\",\"enabled\":true},"
            + "{\"id\":\"eur-1\",\"accountNumber\":\"9999\",\"currency\":\"EUR\"}]}" );
        when(operator.tenantId()).thenReturn(20L);
        when(orderMapper.selectById(10L)).thenReturn(order);
        when(channelMapper.selectList(any())).thenReturn(List.of(channel));
        PayChannelQueryServiceImpl service = new PayChannelQueryServiceImpl(channelMapper, orderMapper, operator,
            new BankAccountConfigParser(new ObjectMapper()));

        List<BankCollectionAccountVo> result = service.bankAccounts(10L, null, "USD");

        assertThat(result).singleElement().satisfies(account -> {
            assertThat(account.getBankAccountId()).isEqualTo("usd-1");
            assertThat(account.getAccountName()).isEqualTo("Bocoo");
            assertThat(account.getAccountNumber()).isEqualTo("1234 5678 9012");
            assertThat(account.getAccountNumberMasked()).isEqualTo("****9012");
        });
    }
}
