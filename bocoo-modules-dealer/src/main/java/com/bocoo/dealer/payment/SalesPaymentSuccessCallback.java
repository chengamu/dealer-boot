package com.bocoo.dealer.payment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentEvent;
import com.bocoo.dealer.mapper.SalesDocumentEventMapper;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.pay.api.PaymentSuccessCallback;
import com.bocoo.pay.api.PaymentSuccessCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SalesPaymentSuccessCallback implements PaymentSuccessCallback {
    private final SalesDocumentMapper documentMapper;
    private final SalesDocumentEventMapper eventMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPayment(PaymentSuccessCommand command) {
        SalesDocument document = TenantContextHolder.callWithIgnore(() -> documentMapper.selectOne(
            new LambdaQueryWrapper<SalesDocument>()
                .eq(SalesDocument::getTenantId, command.getPayerTenantId())
                .eq(SalesDocument::getOrderNo, command.getMerchantOrderId())
                .eq(SalesDocument::getDelFlag, "0"), false));
        if (document == null) return;
        validate(document, command);
        if ("PAID".equals(document.getPaymentStatus())) return;
        int rows = TenantContextHolder.callWithIgnore(() -> documentMapper.update(null,
            new LambdaUpdateWrapper<SalesDocument>()
                .set(SalesDocument::getPaymentStatus, "PAID")
                .set(SalesDocument::getPaymentMethod, command.getMethod())
                .set(SalesDocument::getPaidAmount, BigDecimal.valueOf(command.getPaidPrice(), 2))
                .set(SalesDocument::getPaymentReference, command.getChannelOrderNo())
                .set(SalesDocument::getPaymentConfirmedById, LoginHelper.getUserId())
                .set(SalesDocument::getPaymentConfirmedBy, operatorName())
                .set(SalesDocument::getPaidTime, command.getPaidTime())
                .eq(SalesDocument::getSalesDocumentId, document.getSalesDocumentId())
                .eq(SalesDocument::getTenantId, document.getTenantId())
                .eq(SalesDocument::getDocumentStatus, "SUBMITTED")
                .eq(SalesDocument::getPaymentStatus, "UNPAID")
                .eq(SalesDocument::getPayOrderId, command.getPayOrderId())));
        if (rows != 1) throw new ServiceException("Sales payment was changed concurrently");
        recordEvent(document, command);
    }

    private void validate(SalesDocument document, PaymentSuccessCommand command) {
        if (document.getPayOrderId() == null || !document.getPayOrderId().equals(command.getPayOrderId())) {
            throw new ServiceException("Sales document is linked to another payment order");
        }
        BigDecimal paid = BigDecimal.valueOf(command.getPaidPrice(), 2);
        if (document.getTotalAmount().compareTo(paid) != 0
            || !StringUtils.equalsIgnoreCase(document.getCurrencyCode(), command.getCurrency())) {
            throw new ServiceException("Payment amount or currency does not match the sales document");
        }
        if ("PAID".equals(document.getPaymentStatus())
            && !StringUtils.equals(document.getPayOrderNo(), command.getPayOrderNo())) {
            throw new ServiceException("Sales document already has another successful payment");
        }
    }

    private void recordEvent(SalesDocument document, PaymentSuccessCommand command) {
        SalesDocumentEvent event = new SalesDocumentEvent();
        event.setSalesDocumentId(document.getSalesDocumentId());
        event.setTenantId(document.getTenantId());
        event.setEventType("PAYMENT_CONFIRMED");
        event.setFromStatus("UNPAID");
        event.setToStatus("PAID");
        event.setOperatorId(LoginHelper.getUserId());
        event.setOperatorName(operatorName());
        event.setEventNote(command.getMethod() + ":" + command.getPayOrderNo());
        event.setOccurredTime(command.getPaidTime() == null ? TimeUtils.utcNow() : command.getPaidTime());
        TenantContextHolder.runWithIgnore(() -> eventMapper.insert(event));
    }

    private String operatorName() {
        return LoginHelper.getUserId() == null ? "PAYMENT_SYSTEM" : LoginHelper.getUsername();
    }
}
