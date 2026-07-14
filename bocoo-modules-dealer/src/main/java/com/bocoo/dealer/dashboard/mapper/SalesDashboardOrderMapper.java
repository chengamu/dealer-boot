package com.bocoo.dealer.dashboard.mapper;

import com.bocoo.common.mybatis.annotation.DataColumn;
import com.bocoo.common.mybatis.annotation.DataPermission;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;

@DataPermission({
    @DataColumn(key = "deptName", value = "dept_id"),
    @DataColumn(key = "userName", value = "owner_user_id")
})
public interface SalesDashboardOrderMapper extends BaseMapperPlus<SalesDocument, SalesDocumentVo> {
}
