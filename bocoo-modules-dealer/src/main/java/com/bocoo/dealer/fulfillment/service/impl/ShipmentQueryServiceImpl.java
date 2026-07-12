package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentQueryBo;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.domain.vo.ProductionOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentVo;
import com.bocoo.dealer.fulfillment.service.ShipmentQueryService;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShipmentQueryServiceImpl implements ShipmentQueryService {
    private final SalesDocumentMapper documentMapper;
    private final FulfillmentAccessSupport access;
    private final FulfillmentOrderAssembler orderAssembler;
    private final ShipmentAggregationService aggregation;
    private final ShipmentPageMetricsLoader metricsLoader;
    private final ShipmentViewAssembler shipmentAssembler;

    @Override
    public TableDataInfo<ShipmentOrderVo> queryPage(ShipmentQueryBo bo, PageQuery pageQuery) {
        access.platformOnly();
        QueryWrapper<SalesDocument> query = query(bo).orderByDesc("production_complete_time", "sales_document_id");
        IPage<SalesDocument> page = access.ignoreTenant(() -> documentMapper.selectPage(pageQuery.build(), query));
        Map<ShipmentPageMetricsLoader.DocumentKey, ShipmentPageMetrics> metrics = metricsLoader.load(page.getRecords());
        TableDataInfo<ShipmentOrderVo> result = TableDataInfo.build(page.getRecords().stream()
            .map(document -> summary(document, metrics.get(new ShipmentPageMetricsLoader.DocumentKey(
                document.getTenantId(), document.getSalesDocumentId())))).toList());
        result.setTotal(page.getTotal());
        return result;
    }

    @Override
    public List<ShipmentVo> orderShipments(Long salesDocumentId) {
        SalesDocument document = access.document(salesDocumentId);
        return shipmentAssembler.byDocument(salesDocumentId, document.getTenantId());
    }

    @Override
    public ShipmentVo detail(Long shipmentId) {
        return shipmentAssembler.detail(access.shipment(shipmentId));
    }

    private QueryWrapper<SalesDocument> query(ShipmentQueryBo bo) {
        QueryWrapper<SalesDocument> query = new QueryWrapper<SalesDocument>().eq("del_flag", "0")
            .eq("document_status", "SUBMITTED").eq("production_status", "COMPLETED");
        if (bo == null) return query;
        return query.like(StringUtils.isNotBlank(bo.getOrderNo()), "order_no", bo.getOrderNo())
            .like(StringUtils.isNotBlank(bo.getMerchantName()), "merchant_name", bo.getMerchantName())
            .like(StringUtils.isNotBlank(bo.getCustomerName()), "customer_name", bo.getCustomerName())
            .eq(StringUtils.isNotBlank(bo.getShipmentStatus()), "shipment_status", bo.getShipmentStatus())
            .like(StringUtils.isNotBlank(bo.getCarrierName()), "carrier_name", bo.getCarrierName())
            .like(StringUtils.isNotBlank(bo.getTrackingNo()), "tracking_no", bo.getTrackingNo());
    }

    private ShipmentOrderVo summary(SalesDocument document, ShipmentPageMetrics metrics) {
        ShipmentAggregate value = metrics == null ? aggregation.aggregate(document) : metrics.shipment();
        ProductionOrderVo base = metrics == null ? orderAssembler.summary(document)
            : orderAssembler.summary(document, metrics.itemCount(), value.totalQuantity());
        ShipmentOrderVo vo = new ShipmentOrderVo();
        vo.setSalesDocumentId(base.getSalesDocumentId());
        vo.setTenantId(base.getTenantId());
        vo.setOrderNo(base.getOrderNo());
        vo.setSourceType(base.getSourceType());
        vo.setSourceNo(base.getSourceNo());
        vo.setMerchantName(base.getMerchantName());
        vo.setCustomerName(base.getCustomerName());
        vo.setProjectName(base.getProjectName());
        vo.setProductionStatus(base.getProductionStatus());
        vo.setProductionCompleteTime(base.getProductionCompleteTime());
        vo.setItemCount(base.getItemCount());
        vo.setTotalQuantity(value.totalQuantity());
        vo.setShipmentStatus(value.shipmentStatus());
        vo.setDispatchedQuantity(value.dispatchedQuantity());
        vo.setPackageCount(value.packageCount());
        vo.setShippedTime(document.getShippedTime());
        vo.setCarrierName(document.getCarrierName());
        vo.setTrackingNo(document.getTrackingNo());
        return vo;
    }
}
