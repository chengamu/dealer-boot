package com.bocoo.dealer.mapper;

import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.domain.vo.SalesDocumentItemVo;
import com.bocoo.dealer.domain.vo.SalesDocumentItemMetricsVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SalesDocumentItemMapper extends BaseMapperPlus<SalesDocumentItem, SalesDocumentItemVo> {
    List<SalesDocumentItemMetricsVo> selectItemCounts(@Param("tenantIds") List<Long> tenantIds,
                                                      @Param("documentIds") List<Long> documentIds);

    List<SalesDocumentItemMetricsVo> selectPageMetrics(@Param("tenantIds") List<Long> tenantIds,
                                                       @Param("documentIds") List<Long> documentIds);
}
