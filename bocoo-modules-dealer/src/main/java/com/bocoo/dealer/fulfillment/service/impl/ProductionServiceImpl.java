package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.fulfillment.domain.bo.ProductionQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.FulfillmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ProductionOrderVo;
import com.bocoo.dealer.fulfillment.service.ProductionService;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionServiceImpl implements ProductionService {
    private final SalesDocumentMapper documentMapper;
    private final FulfillmentAccessSupport access;
    private final FulfillmentOrderAssembler assembler;
    private final FulfillmentEventRecorder events;

    @Override
    public TableDataInfo<ProductionOrderVo> queryPage(ProductionQueryBo bo, PageQuery pageQuery) {
        access.platformOnly();
        QueryWrapper<SalesDocument> query = productionQuery(bo).orderByDesc("paid_time", "sales_document_id");
        IPage<SalesDocument> page = access.ignoreTenant(() -> documentMapper.selectPage(pageQuery.build(), query));
        TableDataInfo<ProductionOrderVo> result = TableDataInfo.build(page.getRecords().stream()
            .map(assembler::summary).toList());
        result.setTotal(page.getTotal());
        return result;
    }

    @Override
    public FulfillmentOrderVo detail(Long salesDocumentId) {
        SalesDocument row = access.document(salesDocumentId);
        if (!"PAID".equals(row.getPaymentStatus())) {
            throw ServiceException.ofMessageKey("dealer.fulfillment.productionUnavailable");
        }
        return assembler.detail(row);
    }

    @Override
    @Lock4j(name = "sales-document-lifecycle", keys = {"#salesDocumentId"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean start(Long salesDocumentId) {
        access.platformOnly();
        SalesDocument row = access.document(salesDocumentId);
        if (!"SUBMITTED".equals(row.getDocumentStatus()) || !"PAID".equals(row.getPaymentStatus())
            || !"PENDING".equals(row.getProductionStatus())) {
            throw ServiceException.ofMessageKey("dealer.fulfillment.productionStartDenied");
        }
        verifySnapshots(assembler.items(row));
        boolean updated = access.ignoreTenant(() -> documentMapper.update(null,
            baseUpdate(row).eq(SalesDocument::getDocumentStatus, "SUBMITTED")
                .eq(SalesDocument::getPaymentStatus, "PAID")
                .eq(SalesDocument::getProductionStatus, "PENDING")
                .set(SalesDocument::getProductionStatus, "IN_PRODUCTION")
                .set(SalesDocument::getProductionStartTime, TimeUtils.utcNow()))) > 0;
        if (!updated) throw ServiceException.ofMessageKey("dealer.fulfillment.productionStartDenied");
        events.record(salesDocumentId, row.getTenantId(), "PRODUCTION_STARTED", "PENDING", "IN_PRODUCTION", null);
        return Boolean.TRUE;
    }

    @Override
    @Lock4j(name = "sales-document-lifecycle", keys = {"#salesDocumentId"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean complete(Long salesDocumentId, String note) {
        access.platformOnly();
        SalesDocument row = access.document(salesDocumentId);
        if (!"SUBMITTED".equals(row.getDocumentStatus()) || !"IN_PRODUCTION".equals(row.getProductionStatus())) {
            throw ServiceException.ofMessageKey("dealer.fulfillment.productionCompleteDenied");
        }
        boolean updated = access.ignoreTenant(() -> documentMapper.update(null,
            baseUpdate(row).eq(SalesDocument::getProductionStatus, "IN_PRODUCTION")
                .set(SalesDocument::getProductionStatus, "COMPLETED")
                .set(SalesDocument::getProductionCompleteTime, TimeUtils.utcNow()))) > 0;
        if (!updated) throw ServiceException.ofMessageKey("dealer.fulfillment.productionCompleteDenied");
        events.record(salesDocumentId, row.getTenantId(), "PRODUCTION_COMPLETED", "IN_PRODUCTION", "COMPLETED", note);
        return Boolean.TRUE;
    }

    private QueryWrapper<SalesDocument> productionQuery(ProductionQueryBo bo) {
        QueryWrapper<SalesDocument> query = new QueryWrapper<SalesDocument>()
            .eq("del_flag", "0").eq("document_status", "SUBMITTED").eq("payment_status", "PAID");
        if (bo == null) return query;
        return query.like(StringUtils.isNotBlank(bo.getOrderNo()), "order_no", bo.getOrderNo())
            .like(StringUtils.isNotBlank(bo.getSourceNo()), "source_no", bo.getSourceNo())
            .like(StringUtils.isNotBlank(bo.getMerchantName()), "merchant_name", bo.getMerchantName())
            .like(StringUtils.isNotBlank(bo.getCustomerName()), "customer_name", bo.getCustomerName())
            .eq(StringUtils.isNotBlank(bo.getProductionStatus()), "production_status", bo.getProductionStatus())
            .eq(StringUtils.isNotBlank(bo.getPaymentMethod()), "payment_method", bo.getPaymentMethod());
    }

    private void verifySnapshots(List<SalesDocumentItem> items) {
        boolean invalid = items.isEmpty() || items.stream().anyMatch(item -> item.getFormulaVersionId() == null
            || StringUtils.isBlank(item.getSelectedOptionsJson()) || StringUtils.isBlank(item.getBomSnapshotJson()));
        if (invalid) throw ServiceException.ofMessageKey("dealer.fulfillment.productionSnapshotIncomplete");
    }

    private LambdaUpdateWrapper<SalesDocument> baseUpdate(SalesDocument row) {
        return new LambdaUpdateWrapper<SalesDocument>().eq(SalesDocument::getSalesDocumentId, row.getSalesDocumentId())
            .eq(SalesDocument::getTenantId, row.getTenantId()).eq(SalesDocument::getDelFlag, "0");
    }
}
