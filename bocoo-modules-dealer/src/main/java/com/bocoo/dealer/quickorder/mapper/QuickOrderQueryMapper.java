package com.bocoo.dealer.quickorder.mapper;

import com.bocoo.common.mybatis.annotation.DataColumn;
import com.bocoo.common.mybatis.annotation.DataPermission;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderVo;

@DataPermission({
    @DataColumn(key = "deptName", value = "dept_id"),
    @DataColumn(key = "userName", value = "owner_user_id")
})
public interface QuickOrderQueryMapper extends BaseMapperPlus<QuickOrder, QuickOrderVo> {
}
