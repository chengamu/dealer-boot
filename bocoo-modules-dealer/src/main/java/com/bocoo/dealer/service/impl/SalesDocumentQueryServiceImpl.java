package com.bocoo.dealer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.domain.bo.SalesDocumentBo;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.vo.SalesDocumentEventVo;
import com.bocoo.dealer.domain.vo.SalesDocumentItemMetricsVo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;
import com.bocoo.dealer.mapper.SalesDocumentEventMapper;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.dealer.scope.SalesAccessScope;
import com.bocoo.dealer.service.SalesDocumentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesDocumentQueryServiceImpl extends DealerServiceSupport implements SalesDocumentQueryService {
    private final SalesDocumentMapper mapper;
    private final SalesDocumentItemMapper itemMapper;
    private final SalesDocumentEventMapper eventMapper;
    private final SalesDocumentAssembler assembler;

    @Override
    public TableDataInfo<SalesDocumentVo> queryPage(SalesDocumentBo bo, PageQuery pageQuery) {
        QueryWrapper<SalesDocument> q = active();
        applyAccessScope(q);
        if (bo != null) {
            q.eq(bo.getSourceType() != null, "source_type", bo.getSourceType());
            q.like(bo.getSourceNo() != null, "source_no", bo.getSourceNo());
            q.like(bo.getQuoteNo() != null, "quote_no", bo.getQuoteNo());
            q.like(bo.getOrderNo() != null, "order_no", bo.getOrderNo());
            q.eq(bo.getCustomerId() != null, "customer_id", bo.getCustomerId());
            q.like(bo.getCustomerName() != null, "customer_name", bo.getCustomerName());
            q.like(bo.getMerchantName() != null, "merchant_name", bo.getMerchantName());
            q.eq(bo.getDocumentStatus() != null, "document_status", bo.getDocumentStatus());
            q.eq(bo.getPaymentStatus() != null, "payment_status", bo.getPaymentStatus());
            q.eq(bo.getProductionStatus() != null, "production_status", bo.getProductionStatus());
            q.eq(bo.getShipmentStatus() != null, "shipment_status", bo.getShipmentStatus());
            q.eq(bo.getOwnerUserId() != null, "owner_user_id", bo.getOwnerUserId());
        }
        TableDataInfo<SalesDocumentVo> result = ignoreTenant(() -> page(mapper, pageQuery, q,
            row -> row.orderByDesc("update_time", "create_time")));
        Map<DocumentKey, Integer> counts = itemCounts(result.getRows());
        result.getRows().forEach(row -> row.setItemCount(counts.getOrDefault(
            new DocumentKey(row.getTenantId(), row.getSalesDocumentId()), 0)));
        return result;
    }

    @Override
    public SalesDocumentVo queryById(Long id) {
        SalesDocumentVo vo = ignoreTenant(() -> mapper.selectVoOne(accessQuery(id), false));
        if (vo == null) throw ServiceException.ofMessageKey("dealer.sales.notFound");
        assembler.fill(vo);
        return vo;
    }

    @Override
    public List<SalesDocumentEventVo> events(Long id) {
        SalesDocumentVo document = queryById(id);
        return ignoreTenant(() -> eventMapper.selectVoList(this.<com.bocoo.dealer.domain.entity.SalesDocumentEvent>active().eq("tenant_id", document.getTenantId())
            .eq("sales_document_id", id).orderByDesc("occurred_time", "sales_event_id")));
    }

    @Override
    public List<SalesDocumentVo> customerHistory(Long customerId) {
        QueryWrapper<SalesDocument> q = this.<SalesDocument>active().eq("customer_id", customerId);
        applyAccessScope(q);
        return ignoreTenant(() -> mapper.selectVoList(q.orderByDesc("update_time")));
    }

    private QueryWrapper<SalesDocument> accessQuery(Long id) {
        QueryWrapper<SalesDocument> q = this.<SalesDocument>active().eq("sales_document_id", id);
        applyAccessScope(q);
        return q;
    }

    private void applyAccessScope(QueryWrapper<SalesDocument> query) {
        SalesAccessScope scope = SalesAccessScope.resolve(LoginHelper.isPlatformTenant(), LoginHelper.isAdmin(),
            LoginHelper.getLoginUser() == null ? null : LoginHelper.getLoginUser().getRolePermission());
        if (!scope.crossTenant()) query.eq("tenant_id", tenantId());
        if (scope.ownerOnly()) query.eq("owner_user_id", LoginHelper.getUserId());
    }

    private Map<DocumentKey, Integer> itemCounts(List<SalesDocumentVo> rows) {
        if (rows.isEmpty()) return Map.of();
        List<Long> tenantIds = rows.stream().map(SalesDocumentVo::getTenantId).distinct().toList();
        List<Long> documentIds = rows.stream().map(SalesDocumentVo::getSalesDocumentId).toList();
        List<SalesDocumentItemMetricsVo> metrics = ignoreTenant(() -> itemMapper.selectItemCounts(tenantIds, documentIds));
        return metrics.stream().collect(Collectors.toMap(
            item -> new DocumentKey(item.getTenantId(), item.getSalesDocumentId()),
            SalesDocumentItemMetricsVo::getItemCount));
    }

    private record DocumentKey(Long tenantId, Long documentId) {
    }
}
