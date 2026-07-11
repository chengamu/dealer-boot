package com.bocoo.dealer.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.bo.SalesDocumentBo;
import com.bocoo.dealer.domain.bo.SalesDocumentItemBo;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.domain.vo.SalesDocumentItemVo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.dealer.service.SalesDocumentDraftService;
import com.bocoo.dealer.service.SalesDocumentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesDocumentDraftServiceImpl extends DealerServiceSupport implements SalesDocumentDraftService {
    private final SalesDocumentMapper mapper;
    private final SalesDocumentItemMapper itemMapper;
    private final SalesDocumentHeaderFactory headerFactory;
    private final SalesDocumentItemWriter itemWriter;
    private final SalesDocumentCalculator calculator;
    private final SalesDocumentAssembler assembler;
    private final SalesDocumentQueryService queryService;
    private final SalesDocumentEventRecorder events;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insert(SalesDocumentBo bo) {
        SalesDocument document = headerFactory.create(bo);
        mapper.insert(document);
        applyTotals(document, itemWriter.replace(document.getSalesDocumentId(), document.getTenantId(), bo.getItems()));
        mapper.updateById(document);
        events.record(document.getSalesDocumentId(), document.getTenantId(), "CREATE", null, "DRAFT", null);
        return document.getSalesDocumentId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SalesDocumentBo bo) {
        SalesDocument current = draft(bo.getSalesDocumentId());
        SalesDocument document = headerFactory.update(current, bo);
        applyTotals(document, itemWriter.replace(document.getSalesDocumentId(), document.getTenantId(), bo.getItems()));
        boolean result = mapper.updateById(document) > 0;
        if (result) events.record(document.getSalesDocumentId(), document.getTenantId(), "UPDATE", "DRAFT", "DRAFT", null);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copy(Long id) {
        SalesDocumentVo source = queryService.queryById(id);
        SalesDocumentBo bo = copyBo(source);
        return insert(bo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long[] ids) {
        for (Long id : Arrays.stream(ids).distinct().toList()) {
            SalesDocument row = draft(id);
            itemMapper.delete(this.<SalesDocumentItem>active().eq("tenant_id", row.getTenantId()).eq("sales_document_id", id));
            events.record(id, row.getTenantId(), "DELETE", "DRAFT", null, null);
        }
        return mapper.delete(this.<SalesDocument>active().eq("tenant_id", tenantId()).in("sales_document_id", Arrays.asList(ids))) > 0;
    }

    @Override
    public SalesDocumentItemVo calculateItem(SalesDocumentItemBo bo) {
        return assembler.itemVo(calculator.calculate(bo, tenantId()).item());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalesDocumentVo calculateAll(Long id) {
        SalesDocument document = draft(id);
        applyTotals(document, itemWriter.replace(id, document.getTenantId(), itemWriter.current(id, document.getTenantId())));
        mapper.updateById(document);
        events.record(id, document.getTenantId(), "CALCULATE", "DRAFT", "DRAFT", null);
        return queryService.queryById(id);
    }

    private SalesDocument draft(Long id) {
        SalesDocument row = mapper.selectOne(this.<SalesDocument>active().eq("tenant_id", tenantId()).eq("sales_document_id", id), false);
        if (row == null) throw ServiceException.ofMessageKey("dealer.sales.notFound");
        if (!"DRAFT".equals(row.getDocumentStatus())) throw ServiceException.ofMessageKey("dealer.sales.draftOnly");
        return row;
    }

    private void applyTotals(SalesDocument row, SalesDocumentItemWriter.SalesTotals totals) {
        row.setCurrencyCode(totals.currency() == null ? "USD" : totals.currency());
        row.setListAmount(totals.listAmount()); row.setDiscountAmount(totals.discountAmount());
        row.setProductAmount(totals.productAmount()); row.setShippingAmount(totals.shippingAmount());
        row.setTaxAmount(BigDecimal.ZERO.setScale(2)); row.setTotalAmount(totals.totalAmount());
        row.setDiscountRate(totals.listAmount().signum() == 0 ? BigDecimal.ONE : totals.productAmount()
            .divide(totals.listAmount(), 4, RoundingMode.HALF_UP));
    }

    private SalesDocumentBo copyBo(SalesDocumentVo source) {
        SalesDocumentBo bo = new SalesDocumentBo();
        bo.setCustomerId(source.getCustomerId()); bo.setProjectName(source.getProjectName());
        bo.setCustomerPoNo(source.getCustomerPoNo()); bo.setValidUntil(source.getValidUntil());
        bo.setRecipientName(source.getRecipientName()); bo.setRecipientPhone(source.getRecipientPhone());
        bo.setShippingAddress(source.getShippingAddress()); bo.setRemark(source.getRemark());
        bo.setItems(source.getItems().stream().map(this::copyItem).toList());
        return bo;
    }

    private SalesDocumentItemBo copyItem(SalesDocumentItemVo row) {
        SalesDocumentItemBo bo = new SalesDocumentItemBo();
        bo.setRoomLocation(row.getRoomLocation()); bo.setSaleProductId(row.getSaleProductId());
        bo.setOrderWidthInch(row.getOrderWidthInch()); bo.setOrderHeightInch(row.getOrderHeightInch());
        bo.setQuantity(row.getQuantity()); bo.setSelectedOptionValues(row.getSelectedOptionValues());
        bo.setSortOrder(row.getSortOrder()); bo.setRemark(row.getRemark());
        return bo;
    }
}
