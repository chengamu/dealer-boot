package com.bocoo.merchant.mapper;

import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import com.bocoo.merchant.domain.vo.CustomerQuoteItemVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteItemCountVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerQuoteItemMapper extends BaseMapperPlus<CustomerQuoteItem, CustomerQuoteItemVo> {
    List<CustomerQuoteItemCountVo> selectItemCounts(@Param("tenantIds") List<Long> tenantIds,
                                                    @Param("quoteIds") List<Long> quoteIds);
}
