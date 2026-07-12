package com.bocoo.dealer.quickorder.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.dealer.payment.SalesPaymentOrderLinker;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderSubmitBo;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.mapper.QuickOrderMapper;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderSubmitResultVo;
import com.bocoo.dealer.quickorder.service.QuickOrderSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class QuickOrderSubmissionServiceImpl implements QuickOrderSubmissionService {
    private final QuickOrderMapper mapper;
    private final SalesDocumentMapper documentMapper;
    private final SalesDocumentItemMapper documentItemMapper;
    private final QuickOrderAccess access;
    private final QuickOrderRecalculator recalculator;
    private final QuickOrderSalesDocumentFactory factory;
    private final SalesPaymentOrderLinker paymentOrderLinker;

    @Override
    @Lock4j(name = "quick-order", keys = {"#quickOrderId"})
    @Transactional(rollbackFor = Exception.class)
    public QuickOrderSubmitResultVo submit(Long quickOrderId, QuickOrderSubmitBo bo) {
        QuickOrder source = access.load(quickOrderId);
        if (source.getSalesDocumentId() != null) return existing(source);
        if (!"DRAFT".equals(source.getStatus())) {
            throw ServiceException.ofMessageKey("dealer.quickOrder.draftOnly");
        }
        validateHeader(source);
        QuickOrderRecalculation calculation = recalculator.recalculate(source);
        if (bo.getExpectedTotalAmount().setScale(2, RoundingMode.HALF_UP)
            .compareTo(source.getTotalAmount()) != 0) {
            throw ServiceException.ofMessageKey("dealer.quickOrder.amountChanged");
        }
        SalesDocument document = factory.header(source, calculation.profile());
        documentMapper.insert(document);
        calculation.items().forEach(item -> {
            var target = factory.item(source, item);
            target.setSalesDocumentId(document.getSalesDocumentId()); documentItemMapper.insert(target);
        });
        paymentOrderLinker.initialize(document);
        if (!markOrdered(source, document)) {
            throw ServiceException.ofMessageKey("dealer.quickOrder.concurrent");
        }
        return result(document);
    }

    private boolean markOrdered(QuickOrder source, SalesDocument document) {
        return mapper.update(null, new LambdaUpdateWrapper<QuickOrder>()
            .eq(QuickOrder::getQuickOrderId, source.getQuickOrderId())
            .eq(QuickOrder::getTenantId, source.getTenantId()).eq(QuickOrder::getStatus, "DRAFT")
            .isNull(QuickOrder::getSalesDocumentId).eq(QuickOrder::getDelFlag, "0")
            .set(QuickOrder::getStatus, "ORDERED")
            .set(QuickOrder::getSalesDocumentId, document.getSalesDocumentId())
            .set(QuickOrder::getOrderNo, document.getOrderNo())
            .set(QuickOrder::getSubmittedById, LoginHelper.getUserId())
            .set(QuickOrder::getSubmittedBy, LoginHelper.getUsername())
            .set(QuickOrder::getSubmittedTime, TimeUtils.utcNow())) > 0;
    }

    private QuickOrderSubmitResultVo existing(QuickOrder source) {
        SalesDocument row = documentMapper.selectOne(new QueryWrapper<SalesDocument>().eq("del_flag", "0")
            .eq("tenant_id", source.getTenantId()).eq("sales_document_id", source.getSalesDocumentId()), false);
        if (row == null) throw ServiceException.ofMessageKey("dealer.sales.notFound");
        return result(row);
    }

    private QuickOrderSubmitResultVo result(SalesDocument row) {
        return new QuickOrderSubmitResultVo(row.getSalesDocumentId(), row.getOrderNo(), row.getTotalAmount());
    }

    private void validateHeader(QuickOrder source) {
        if (source.getCustomerId() == null || StringUtils.isBlank(source.getCustomerName())
            || StringUtils.isBlank(source.getRecipientName()) || StringUtils.isBlank(source.getRecipientPhone())
            || StringUtils.isBlank(source.getShippingAddress())) {
            throw ServiceException.ofMessageKey("dealer.quickOrder.review.incomplete");
        }
    }
}
