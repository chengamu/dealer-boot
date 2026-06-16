package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.PriceRuleItemBo;
import com.bocoo.product.domain.vo.PriceRuleItemVo;

import java.util.List;

public interface PriceRuleItemService {

    TableDataInfo<PriceRuleItemVo> queryPageList(PriceRuleItemBo bo, PageQuery pageQuery);

    List<PriceRuleItemVo> queryList(PriceRuleItemBo bo);

    PriceRuleItemVo queryById(Long id);

    Boolean insertByBo(PriceRuleItemBo bo);

    Boolean updateByBo(PriceRuleItemBo bo);

    Boolean deleteWithValidByIds(Long[] ids);
}
