package com.bocoo.dealer.dashboard.mapper;

import com.bocoo.common.mybatis.annotation.DataColumn;
import com.bocoo.common.mybatis.annotation.DataPermission;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;

@DataPermission({
    @DataColumn(key = "deptName", value = "dept_id"),
    @DataColumn(key = "userName", value = "owner_user_id")
})
public interface SalesDashboardQuoteMapper extends BaseMapperPlus<CustomerQuote, CustomerQuoteVo> {
}
