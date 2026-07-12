package com.bocoo.dealer.quickorder.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderVo;

public interface QuickOrderQueryService {
    TableDataInfo<QuickOrderVo> queryPage(QuickOrderBo bo, PageQuery pageQuery);

    QuickOrderVo queryById(Long id);
}
