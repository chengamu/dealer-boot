package com.bocoo.dealer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.domain.bo.SalesDocumentBo;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;
import com.bocoo.dealer.domain.vo.SalesDocumentExportVo;
import com.bocoo.dealer.mapper.SalesDocumentQueryMapper;
import com.bocoo.dealer.scope.PlatformSalesGuard;
import com.bocoo.dealer.service.PlatformSalesDocumentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformSalesDocumentQueryServiceImpl extends DealerServiceSupport
    implements PlatformSalesDocumentQueryService {
    private final SalesDocumentQueryMapper mapper;
    private final SalesDocumentAssembler assembler;
    private final SalesDocumentMetricsLoader metricsLoader;
    private final PlatformSalesGuard guard;
    private final SalesDocumentPdfRenderer renderer;
    private final SalesDocumentExportAssembler exportAssembler;

    @Override
    public TableDataInfo<SalesDocumentVo> queryPage(SalesDocumentBo bo, PageQuery pageQuery) {
        guard.requirePlatform();
        QueryWrapper<SalesDocument> query = SalesDocumentQueryCriteria.apply(active(), bo);
        TableDataInfo<SalesDocumentVo> result = ignoreTenant(() -> page(mapper, pageQuery, query,
            row -> row.orderByDesc("submitted_time", "sales_document_id")));
        metricsLoader.fill(result.getRows());
        return result;
    }

    @Override
    public SalesDocumentVo queryById(Long id) {
        guard.requirePlatform();
        SalesDocumentVo vo = ignoreTenant(() -> mapper.selectVoOne(this.<SalesDocument>active()
            .eq("sales_document_id", id), false));
        if (vo == null) throw ServiceException.ofMessageKey("dealer.sales.notFound");
        assembler.fill(vo);
        return vo;
    }

    @Override
    public byte[] pdf(Long id) {
        return renderer.render(queryById(id), "ORDER");
    }

    @Override
    public List<SalesDocumentExportVo> export(Long id) {
        return exportAssembler.rows(queryById(id));
    }
}
