package com.bocoo.merchant.mapper;

import com.bocoo.common.mybatis.annotation.DataColumn;
import com.bocoo.common.mybatis.annotation.DataPermission;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.merchant.domain.entity.CustomerProfile;
import com.bocoo.merchant.domain.vo.CustomerProfileVo;

@DataPermission({
    @DataColumn(key = "deptName", value = "dept_id"),
    @DataColumn(key = "userName", value = "owner_user_id")
})
public interface CustomerProfileMapper extends BaseMapperPlus<CustomerProfile, CustomerProfileVo> {
}
