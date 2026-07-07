package com.bocoo.merchant.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.merchant.domain.bo.CustomerProfileBo;
import com.bocoo.merchant.domain.vo.CustomerOwnerOptionVo;
import com.bocoo.merchant.domain.vo.CustomerProfileVo;

import java.util.List;

public interface CustomerProfileService {

    TableDataInfo<CustomerProfileVo> queryPageList(CustomerProfileBo bo, PageQuery pageQuery);

    TableDataInfo<CustomerProfileVo> queryPlatformPageList(CustomerProfileBo bo, PageQuery pageQuery);

    List<CustomerProfileVo> queryOptions(CustomerProfileBo bo);

    List<CustomerOwnerOptionVo> queryOwnerOptions(Long tenantId);

    CustomerProfileVo queryById(Long id);

    CustomerProfileVo queryPlatformById(Long id);

    Boolean insertByBo(CustomerProfileBo bo);

    Boolean updateByBo(CustomerProfileBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);
}
