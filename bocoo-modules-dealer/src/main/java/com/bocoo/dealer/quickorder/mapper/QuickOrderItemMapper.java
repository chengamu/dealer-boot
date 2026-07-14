package com.bocoo.dealer.quickorder.mapper;

import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderItemSummaryVo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderItemVo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface QuickOrderItemMapper extends BaseMapperPlus<QuickOrderItem, QuickOrderItemVo> {
    List<QuickOrderItemSummaryVo> selectSummaries(@Param("quickOrderIds") Collection<Long> quickOrderIds);
}
