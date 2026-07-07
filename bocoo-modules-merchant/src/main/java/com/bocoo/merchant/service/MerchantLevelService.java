package com.bocoo.merchant.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.merchant.domain.bo.MerchantLevelBo;
import com.bocoo.merchant.domain.vo.MerchantLevelVo;

import java.util.List;

public interface MerchantLevelService {

    TableDataInfo<MerchantLevelVo> queryPageList(MerchantLevelBo bo, PageQuery pageQuery);

    List<MerchantLevelVo> queryList(MerchantLevelBo bo);

    MerchantLevelVo queryById(Long id);

    Boolean insertByBo(MerchantLevelBo bo);

    Boolean updateByBo(MerchantLevelBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);
}
