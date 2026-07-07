package com.bocoo.merchant.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.merchant.domain.bo.MerchantLevelDiscountBo;
import com.bocoo.merchant.domain.vo.MerchantLevelDiscountVo;

import java.util.List;

public interface MerchantLevelDiscountService {

    TableDataInfo<MerchantLevelDiscountVo> queryPageList(MerchantLevelDiscountBo bo, PageQuery pageQuery);

    List<MerchantLevelDiscountVo> queryList(MerchantLevelDiscountBo bo);

    MerchantLevelDiscountVo queryById(Long id);

    Boolean insertByBo(MerchantLevelDiscountBo bo);

    Boolean updateByBo(MerchantLevelDiscountBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);
}
