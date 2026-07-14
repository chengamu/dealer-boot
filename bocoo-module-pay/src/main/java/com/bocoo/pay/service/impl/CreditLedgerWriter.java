package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import com.bocoo.pay.domain.entity.MerchantCreditTransaction;
import com.bocoo.pay.domain.entity.MerchantReceivable;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.bo.CreditRepayBo;
import com.bocoo.pay.enums.CreditTransactionType;
import com.bocoo.pay.enums.ReceivableStatus;
import com.bocoo.pay.mapper.MerchantCreditTransactionMapper;
import com.bocoo.pay.mapper.MerchantReceivableMapper;
import com.bocoo.pay.service.PayOperatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@Component
@RequiredArgsConstructor
class CreditLedgerWriter {
    private final MerchantCreditTransactionMapper transactionMapper;
    private final MerchantReceivableMapper receivableMapper;
    private final PayOperatorContext operator;

    MerchantReceivable createReceivable(MerchantCreditAccount account, PayOrder order,
                                         Long salesDocumentId, Integer creditTermDays) {
        BigDecimal amount = BigDecimal.valueOf(order.getPrice(), 2);
        MerchantReceivable row = new MerchantReceivable();
        row.setBusinessOrigin(account.getBusinessOrigin());
        row.setTenantId(account.getTenantId());
        row.setSalesStoreId(account.getSalesStoreId());
        row.setMerchantId(account.getMerchantId());
        row.setMerchantName(account.getMerchantName());
        row.setSalesDocumentId(salesDocumentId);
        row.setSalesOrderNo(order.getSalesOrderNo());
        row.setPayOrderId(order.getId());
        row.setPayOrderNo(order.getNo());
        row.setCreditAccountId(account.getCreditAccountId());
        row.setReceivableNo(no("AR"));
        row.setReceivableAmount(amount);
        row.setRepaidAmount(BigDecimal.ZERO.setScale(2));
        row.setOutstandingAmount(amount);
        row.setCurrency(order.getCurrency());
        row.setStatus(ReceivableStatus.UNPAID.name());
        row.setFormedTime(TimeUtils.utcNow());
        row.setDueDate(row.getFormedTime().toLocalDate().plusDays(creditTermDays));
        receivableMapper.insert(row);
        return row;
    }

    void transaction(MerchantCreditAccount account, CreditTransactionType type, Long businessId,
                     String businessNo, BigDecimal amount, BigDecimal beforeLimit, BigDecimal afterLimit,
                     BigDecimal beforeUsed, BigDecimal afterUsed, String reason) {
        MerchantCreditTransaction row = new MerchantCreditTransaction();
        row.setTenantId(account.getTenantId());
        row.setCreditAccountId(account.getCreditAccountId());
        row.setTransactionNo(no("CT"));
        row.setTransactionType(type.name());
        row.setBusinessType(type == CreditTransactionType.OCCUPY ? "PAY_ORDER"
            : type == CreditTransactionType.REPAY ? "RECEIVABLE" : "CREDIT_ACCOUNT");
        row.setBusinessId(businessId);
        row.setBusinessNo(businessNo);
        row.setAmount(amount.abs());
        row.setBeforeCreditLimit(beforeLimit);
        row.setAfterCreditLimit(afterLimit);
        row.setBeforeUsedCredit(beforeUsed);
        row.setAfterUsedCredit(afterUsed);
        row.setCurrency(account.getCurrency());
        row.setOperatorId(operator.userId());
        row.setOperatorName(operator.username());
        row.setOccurredTime(TimeUtils.utcNow());
        row.setRemark(reason);
        transactionMapper.insert(row);
    }

    void repayment(MerchantCreditAccount account, MerchantReceivable receivable, CreditRepayBo bo,
                   BigDecimal beforeUsed, BigDecimal afterUsed) {
        MerchantCreditTransaction row = base(account, CreditTransactionType.REPAY, receivable.getReceivableId(),
            receivable.getReceivableNo(), bo.getAmount(), beforeUsed, afterUsed, bo.getReason());
        row.setPaymentMethod(bo.getMethod());
        row.setPaymentReference(bo.getReference());
        row.setPaidTime(bo.getPaidTime());
        row.setProofMediaId(bo.getProofMediaId());
        row.setIdempotencyKey(bo.getIdempotencyKey());
        transactionMapper.insert(row);
    }

    MerchantCreditTransaction findRepayment(Long receivableId, String idempotencyKey) {
        return transactionMapper.selectOne(new LambdaQueryWrapper<MerchantCreditTransaction>()
            .eq(MerchantCreditTransaction::getTransactionType, CreditTransactionType.REPAY.name())
            .eq(MerchantCreditTransaction::getBusinessId, receivableId)
            .eq(MerchantCreditTransaction::getIdempotencyKey, idempotencyKey), false);
    }

    private MerchantCreditTransaction base(MerchantCreditAccount account, CreditTransactionType type,
                                            Long businessId, String businessNo, BigDecimal amount,
                                            BigDecimal beforeUsed, BigDecimal afterUsed, String reason) {
        MerchantCreditTransaction row = new MerchantCreditTransaction();
        row.setTenantId(account.getTenantId());
        row.setCreditAccountId(account.getCreditAccountId());
        row.setTransactionNo(no("CT"));
        row.setTransactionType(type.name());
        row.setBusinessType("RECEIVABLE");
        row.setBusinessId(businessId);
        row.setBusinessNo(businessNo);
        row.setAmount(amount);
        row.setBeforeCreditLimit(account.getCreditLimit());
        row.setAfterCreditLimit(account.getCreditLimit());
        row.setBeforeUsedCredit(beforeUsed);
        row.setAfterUsedCredit(afterUsed);
        row.setCurrency(account.getCurrency());
        row.setOperatorId(operator.userId());
        row.setOperatorName(operator.username());
        row.setOccurredTime(TimeUtils.utcNow());
        row.setRemark(reason);
        return row;
    }

    private String no(String prefix) {
        return prefix + TimeUtils.utcNow().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + IdWorker.getId();
    }
}
