package com.bocoo.dealer.quickorder.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderVo;
import com.bocoo.dealer.quickorder.domain.vo.PlatformQuickOrderExportVo;

import java.util.List;

public interface PlatformQuickOrderQueryService {
    TableDataInfo<QuickOrderVo> queryPage(QuickOrderBo bo, PageQuery pageQuery);

    QuickOrderVo queryById(Long id);

    List<PlatformQuickOrderExportVo> export(QuickOrderBo bo);
}
