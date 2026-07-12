package com.bocoo.dealer.payment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.constant.TenantConstants;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.pay.domain.bo.PayOrderCreateBo;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class SalesPaymentOrderLinker {
    private static final String SALES_APP_KEY = "platform-default";
    private final SalesDocumentMapper documentMapper;
    private final PayOrderService orderService;

    SalesPaymentSnapshot getExisting(Long salesDocumentId) {
        SalesDocument document = loadDocument(salesDocumentId);
        PayOrder order = findOrder(document);
        if (order == null) throw new ServiceException("Payment order does not exist");
        return new SalesPaymentSnapshot(document, order);
    }

    SalesPaymentSnapshot getOrCreate(Long salesDocumentId) {
        SalesDocument document = loadDocument(salesDocumentId);
        if (!"SUBMITTED".equals(document.getDocumentStatus())) {
            throw new ServiceException("Sales document cannot enter payment");
        }
        PayOrder order = findOrder(document);
        if (order == null) order = create(document);
        link(document, order);
        return new SalesPaymentSnapshot(document, order);
    }

    public void initialize(SalesDocument document) {
        TenantContextHolder.runWithIgnore(() -> {
            PayOrder order = orderService.getOrderByMerchantOrderId(document.getTenantId(), document.getOrderNo());
            if (order == null) order = create(document);
            link(document, order);
        });
    }

    private SalesDocument loadDocument(Long salesDocumentId) {
        var query = new LambdaQueryWrapper<SalesDocument>()
            .eq(SalesDocument::getSalesDocumentId, salesDocumentId)
            .eq(!LoginHelper.isPlatformTenant(), SalesDocument::getTenantId, LoginHelper.getTenantId())
            .eq(SalesDocument::getDelFlag, "0");
        SalesDocument document = LoginHelper.isPlatformTenant()
            ? TenantContextHolder.callWithIgnore(() -> documentMapper.selectOne(query, false))
            : documentMapper.selectOne(query, false);
        if (document == null) throw new ServiceException("Sales document does not exist");
        return document;
    }

    private PayOrder findOrder(SalesDocument document) {
        return LoginHelper.isPlatformTenant()
            ? TenantContextHolder.callWithIgnore(() -> orderService.getOrderByMerchantOrderId(
                document.getTenantId(), document.getOrderNo()))
            : orderService.getOrderByMerchantOrderId(document.getTenantId(), document.getOrderNo());
    }

    private PayOrder create(SalesDocument document) {
        if (!"UNPAID".equals(document.getPaymentStatus())) {
            throw new ServiceException("Paid sales document has no linked payment order");
        }
        PayOrderCreateBo bo = new PayOrderCreateBo();
        bo.setPayerTenantId(document.getTenantId());
        bo.setPayeeTenantId(TenantConstants.PLATFORM_TENANT_ID);
        bo.setAppKey(SALES_APP_KEY);
        bo.setMerchantOrderId(document.getOrderNo());
        bo.setSalesDocumentId(document.getSalesDocumentId());
        bo.setSalesOrderNo(document.getOrderNo());
        bo.setMerchantId(document.getMerchantId());
        bo.setMerchantName(document.getMerchantName());
        bo.setCustomerId(document.getCustomerId());
        bo.setCustomerName(document.getCustomerName());
        bo.setSubject("Sales order " + document.getOrderNo());
        bo.setBody(document.getProjectName());
        bo.setPrice(document.getTotalAmount().movePointRight(2)
            .setScale(0, RoundingMode.UNNECESSARY).longValueExact());
        bo.setCurrency(document.getCurrencyCode());
        bo.setUserId(LoginHelper.getUserId());
        bo.setUserType("MERCHANT");
        bo.setExpireTime(TimeUtils.utcNow().plusDays(7));
        return orderService.createOrder(bo);
    }

    private void link(SalesDocument document, PayOrder order) {
        if (document.getPayOrderId() != null && !document.getPayOrderId().equals(order.getId())) {
            throw new ServiceException("Sales document is linked to another payment order");
        }
        if (document.getPayOrderId() != null) return;
        int rows = TenantContextHolder.callWithIgnore(() -> documentMapper.update(null,
            new LambdaUpdateWrapper<SalesDocument>()
                .set(SalesDocument::getPayOrderId, order.getId()).set(SalesDocument::getPayOrderNo, order.getNo())
                .eq(SalesDocument::getSalesDocumentId, document.getSalesDocumentId())
                .eq(SalesDocument::getTenantId, document.getTenantId()).isNull(SalesDocument::getPayOrderId)));
        if (rows != 1) throw new ServiceException("Sales payment order was linked concurrently");
        document.setPayOrderId(order.getId());
        document.setPayOrderNo(order.getNo());
    }
}
