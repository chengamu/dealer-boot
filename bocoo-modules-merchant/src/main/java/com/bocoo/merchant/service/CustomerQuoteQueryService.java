package com.bocoo.merchant.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.merchant.domain.bo.CustomerQuoteBo;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportCnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportEnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;

import java.util.List;

public interface CustomerQuoteQueryService {
    TableDataInfo<CustomerQuoteVo> queryPageList(CustomerQuoteBo bo, PageQuery pageQuery);

    CustomerQuoteVo queryById(Long id);

    List<CustomerQuoteExportCnVo> exportCn(Long id);

    List<CustomerQuoteExportEnVo> exportEn(Long id);
}
