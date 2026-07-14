package com.bocoo.pay.mapper;

import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import org.apache.ibatis.annotations.Insert;

public interface MerchantCreditAccountMapper extends BaseMapperPlus<MerchantCreditAccount, MerchantCreditAccount> {

    @Insert("""
        INSERT INTO merchant_credit_account (
            credit_account_id, business_origin, tenant_id, sales_store_id, merchant_id, merchant_name,
            currency, credit_limit, used_credit, status, version,
            create_by_id, create_by, create_time, update_by, update_time
        ) VALUES (
            #{creditAccountId}, #{businessOrigin}, #{tenantId}, #{salesStoreId}, #{merchantId}, #{merchantName},
            #{currency}, #{creditLimit}, #{usedCredit}, #{status}, #{version},
            #{createById}, #{createBy}, #{createTime}, #{updateBy}, #{updateTime}
        )
        ON CONFLICT DO NOTHING
        """)
    int insertIgnoreConflict(MerchantCreditAccount account);
}
