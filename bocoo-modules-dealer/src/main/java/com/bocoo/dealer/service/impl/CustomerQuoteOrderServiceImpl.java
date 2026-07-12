package com.bocoo.dealer.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.bo.CustomerQuoteConvertOrderBo;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.domain.vo.CustomerQuoteOrderPreviewVo;
import com.bocoo.dealer.domain.vo.CustomerQuoteOrderResultVo;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.dealer.payment.SalesPaymentOrderLinker;
import com.bocoo.dealer.service.CustomerQuoteOrderService;
import com.bocoo.merchant.service.CustomerQuoteConversionSnapshot;
import com.bocoo.merchant.service.CustomerQuoteConversionSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CustomerQuoteOrderServiceImpl extends DealerServiceSupport implements CustomerQuoteOrderService {
    private final CustomerQuoteConversionSupport quoteSupport;
    private final CustomerQuoteOrderCalculator calculator;
    private final CustomerQuoteOrderFactory factory;
    private final SalesDocumentMapper documentMapper;
    private final SalesDocumentItemMapper itemMapper;
    private final SalesDocumentEventRecorder events;
    private final SalesPaymentOrderLinker paymentOrderLinker;

    @Override
    public CustomerQuoteOrderPreviewVo preview(Long quoteId) {
        CustomerQuoteConversionSnapshot snapshot = quoteSupport.load(quoteId);
        if (snapshot.quote().getSalesDocumentId() != null) {
            throw ServiceException.ofMessageKey("customer.quote.order.converted");
        }
        return calculator.calculate(snapshot).preview();
    }

    @Override
    @Lock4j(name = "customer-quote-convert", keys = {"#quoteId"})
    @Transactional(rollbackFor = Exception.class)
    public CustomerQuoteOrderResultVo convert(Long quoteId, CustomerQuoteConvertOrderBo bo) {
        CustomerQuoteConversionSnapshot snapshot = quoteSupport.load(quoteId);
        if (snapshot.quote().getSalesDocumentId() != null) {
            return existing(snapshot.quote().getSalesDocumentId());
        }
        CustomerQuoteOrderCalculation calculation = calculator.calculate(snapshot);
        if (bo.getExpectedTotalAmount().setScale(2, RoundingMode.HALF_UP)
            .compareTo(calculation.preview().getTotalAmount()) != 0) {
            throw ServiceException.ofMessageKey("customer.quote.order.amountChanged");
        }
        SalesDocument document = factory.header(snapshot.quote(), calculation, bo);
        documentMapper.insert(document);
        for (var source : snapshot.items()) {
            SalesDocumentItem item = factory.item(snapshot.quote(), source,
                calculation.discountRates().get(source.getQuoteItemId()));
            item.setSalesDocumentId(document.getSalesDocumentId());
            item.setTenantId(document.getTenantId());
            itemMapper.insert(item);
        }
        paymentOrderLinker.initialize(document);
        events.record(document.getSalesDocumentId(), document.getTenantId(), "ORDER_CREATED_FROM_QUOTE",
            "CONFIRMED", "SUBMITTED", snapshot.quote().getQuoteNo());
        if (!quoteSupport.markConverted(quoteId, document.getSalesDocumentId(), document.getOrderNo())) {
            throw ServiceException.ofMessageKey("customer.quote.order.concurrent");
        }
        return result(document);
    }

    private CustomerQuoteOrderResultVo existing(Long id) {
        SalesDocument row = documentMapper.selectOne(this.<SalesDocument>active()
            .eq("tenant_id", tenantId()).eq("sales_document_id", id), false);
        if (row == null) throw ServiceException.ofMessageKey("dealer.sales.notFound");
        return result(row);
    }

    private CustomerQuoteOrderResultVo result(SalesDocument row) {
        return new CustomerQuoteOrderResultVo(row.getSalesDocumentId(), row.getOrderNo(), row.getTotalAmount());
    }
}
