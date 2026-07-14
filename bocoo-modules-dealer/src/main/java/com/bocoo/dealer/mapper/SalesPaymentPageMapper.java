package com.bocoo.dealer.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.pay.domain.bo.BankTransferQueryBo;
import com.bocoo.pay.domain.bo.PaymentOrderQueryBo;
import org.apache.ibatis.annotations.Param;

public interface SalesPaymentPageMapper {
    Page<Long> selectPaymentOrderIds(@Param("page") Page<Long> page,
                                     @Param("query") PaymentOrderQueryBo query,
                                     @Param("scopeTenantId") Long scopeTenantId,
                                     @Param("scopeBusinessOrigin") String scopeBusinessOrigin);

    Page<Long> selectBankTransferIds(@Param("page") Page<Long> page,
                                     @Param("query") BankTransferQueryBo query,
                                     @Param("scopeTenantId") Long scopeTenantId,
                                     @Param("scopeBusinessOrigin") String scopeBusinessOrigin);
}
