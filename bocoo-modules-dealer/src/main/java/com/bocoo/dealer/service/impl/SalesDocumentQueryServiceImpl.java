package com.bocoo.dealer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.domain.bo.SalesDocumentBo;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.vo.SalesDocumentEventVo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;
import com.bocoo.dealer.mapper.SalesDocumentEventMapper;
import com.bocoo.dealer.mapper.SalesDocumentQueryMapper;
import com.bocoo.dealer.scope.SalesBusinessScope;
import com.bocoo.dealer.service.SalesDocumentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesDocumentQueryServiceImpl extends DealerServiceSupport implements SalesDocumentQueryService {
    private final SalesDocumentQueryMapper mapper;
    private final SalesDocumentEventMapper eventMapper;
    private final SalesDocumentAssembler assembler;
    private final SalesDocumentMetricsLoader metricsLoader;

    @Override
    public TableDataInfo<SalesDocumentVo> queryPage(SalesDocumentBo bo, PageQuery pageQuery) {
        SalesBusinessScope scope = SalesBusinessScope.current();
        QueryWrapper<SalesDocument> q = SalesDocumentQueryCriteria.apply(this.<SalesDocument>active()
            .eq("tenant_id", scope.tenantId()).eq("business_origin", scope.businessOrigin()), bo);
        TableDataInfo<SalesDocumentVo> result = page(mapper, pageQuery, q,
            row -> row.orderByDesc("submitted_time", "sales_document_id"));
        metricsLoader.fill(result.getRows());
        return result;
    }

    @Override
    public SalesDocumentVo queryById(Long id) {
        SalesDocumentVo vo = mapper.selectVoOne(accessQuery(id), false);
        if (vo == null) throw ServiceException.ofMessageKey("dealer.sales.notFound");
        assembler.fill(vo);
        return vo;
    }

    @Override
    public List<SalesDocumentEventVo> events(Long id) {
        SalesDocumentVo document = queryById(id);
        return eventMapper.selectVoList(this.<com.bocoo.dealer.domain.entity.SalesDocumentEvent>active().eq("tenant_id", document.getTenantId())
            .eq("sales_document_id", id).orderByDesc("occurred_time", "sales_event_id"));
    }

    @Override
    public List<SalesDocumentVo> customerHistory(Long customerId) {
        SalesBusinessScope scope = SalesBusinessScope.current();
        QueryWrapper<SalesDocument> q = this.<SalesDocument>active().eq("customer_id", customerId)
            .eq("tenant_id", scope.tenantId()).eq("business_origin", scope.businessOrigin());
        return mapper.selectVoList(q.orderByDesc("submitted_time", "sales_document_id"));
    }

    private QueryWrapper<SalesDocument> accessQuery(Long id) {
        SalesBusinessScope scope = SalesBusinessScope.current();
        return this.<SalesDocument>active().eq("sales_document_id", id)
            .eq("tenant_id", scope.tenantId()).eq("business_origin", scope.businessOrigin());
    }
}
