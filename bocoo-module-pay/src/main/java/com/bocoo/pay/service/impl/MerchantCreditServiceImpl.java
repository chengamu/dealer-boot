package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.domain.bo.CreditAdjustBo;
import com.bocoo.pay.domain.bo.CreditFreezeBo;
import com.bocoo.pay.domain.bo.CreditOccupyBo;
import com.bocoo.pay.domain.bo.CreditRepayBo;
import com.bocoo.pay.domain.credit.CreditSubject;
import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import com.bocoo.pay.domain.entity.MerchantCreditTransaction;
import com.bocoo.pay.domain.entity.MerchantReceivable;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.enums.CreditAccountStatus;
import com.bocoo.pay.enums.CreditTransactionType;
import com.bocoo.pay.enums.PayChannelCode;
import com.bocoo.pay.enums.PayOrderStatus;
import com.bocoo.pay.enums.ReceivableStatus;
import com.bocoo.pay.mapper.MerchantReceivableMapper;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.service.MerchantCreditService;
import com.bocoo.pay.service.PayChannelService;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PaymentSuccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class MerchantCreditServiceImpl implements MerchantCreditService {
    private final CreditAccountRepository accounts;
    private final CreditLedgerWriter ledger;
    private final MerchantReceivableMapper receivableMapper;
    private final PayOrderMapper orderMapper;
    private final PayOrderExtensionMapper extensionMapper;
    private final PayChannelService channelService;
    private final PaymentSuccessService successService;
    private final PayOperatorContext operator;
    private final CreditSubjectSupport subjects;

    @Override
    public MerchantCreditAccount getAccount(Long merchantTenantId) {
        if (!operator.isPlatform() && !operator.tenantId().equals(merchantTenantId)) {
            throw new ServiceException("Credit account does not belong to the current tenant");
        }
        CreditSubject subject = CreditSubject.merchant(merchantTenantId, null, null, "USD");
        MerchantCreditAccount account = operator.isPlatform()
            ? TenantContextHolder.callWithIgnore(() -> accounts.find(subject)) : accounts.find(subject);
        if (account == null) throw new ServiceException("Merchant credit account does not exist");
        return account;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantReceivable occupy(CreditOccupyBo bo) {
        PayOrder order = orderMapper.selectById(bo.getPayOrderId());
        if (order == null) throw new ServiceException("Payment order does not exist");
        if (!operator.tenantId().equals(order.getPayerTenantId()) || !PayOrderStatus.isWaiting(order.getStatus())) {
            throw new ServiceException("Payment order cannot use the current tenant credit account");
        }
        MerchantReceivable existing = receivableMapper.selectOne(new LambdaQueryWrapper<MerchantReceivable>()
            .eq(MerchantReceivable::getPayOrderId, order.getId()), false);
        if (existing != null) return existing;
        CreditSubject subject = subjects.forDocument(bo.getSalesDocumentId(), order.getCurrency(), bo);
        MerchantCreditAccount account = accounts.getOrCreate(subject, bo);
        if (!CreditAccountStatus.NORMAL.name().equals(account.getStatus()) || bo.getCreditTermDays() <= 0) {
            throw new ServiceException("Credit account or credit term is not available");
        }
        boolean overdue = receivableMapper.selectCount(new LambdaQueryWrapper<MerchantReceivable>()
            .eq(MerchantReceivable::getBusinessOrigin, account.getBusinessOrigin())
            .eq(MerchantReceivable::getCreditAccountId, account.getCreditAccountId())
            .eq(MerchantReceivable::getStatus, ReceivableStatus.OVERDUE.name())) > 0;
        if (!StringUtils.equalsIgnoreCase(account.getCurrency(), order.getCurrency()) || overdue) {
            throw new ServiceException("Credit currency does not match or an overdue receivable exists");
        }
        BigDecimal amount = BigDecimal.valueOf(order.getPrice(), 2);
        BigDecimal beforeUsed = account.getUsedCredit();
        BigDecimal afterUsed = beforeUsed.add(amount);
        if (afterUsed.compareTo(account.getCreditLimit()) > 0) throw new ServiceException("Available credit is insufficient");
        PayChannel channel = channelService.getEnabledChannel(order.getPayeeTenantId(), order.getAppId(),
            PayChannelCode.CREDIT_LIMIT.getCode());
        PayOrderExtension attempt = new PayOrderExtension();
        attempt.setTenantId(order.getTenantId());
        attempt.setPayerTenantId(order.getPayerTenantId());
        attempt.setPayeeTenantId(order.getPayeeTenantId());
        attempt.setNo("PE" + TimeUtils.utcNow().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + IdWorker.getId());
        attempt.setOrderId(order.getId());
        attempt.setChannelId(channel.getId());
        attempt.setChannelCode(channel.getCode());
        attempt.setStatus(PayOrderStatus.PROCESSING.getStatus());
        attempt.setBankDeclaredPrice(order.getPrice());
        attempt.setBankCurrency(order.getCurrency());
        attempt.setChannelExtras("{}");
        extensionMapper.insert(attempt);
        int orderRows = orderMapper.update(null, new LambdaUpdateWrapper<PayOrder>()
            .set(PayOrder::getStatus, PayOrderStatus.PROCESSING.getStatus())
            .set(PayOrder::getChannelId, channel.getId()).set(PayOrder::getChannelCode, channel.getCode())
            .set(PayOrder::getExtensionId, attempt.getId()).eq(PayOrder::getId, order.getId())
            .eq(PayOrder::getStatus, PayOrderStatus.WAITING.getStatus()));
        if (orderRows != 1) throw new ServiceException("Payment order was changed concurrently");
        accounts.changeUsed(account, afterUsed);
        MerchantReceivable receivable = ledger.createReceivable(account, order, bo.getSalesDocumentId(), bo.getCreditTermDays());
        ledger.transaction(account, CreditTransactionType.OCCUPY, order.getId(), order.getNo(), amount,
            account.getCreditLimit(), account.getCreditLimit(), beforeUsed, afterUsed, "Credit payment occupied");
        successService.confirm(attempt.getId(), receivable.getReceivableNo(), order.getPrice(), order.getCurrency(),
            TimeUtils.utcNow(), "credit_limit_occupied");
        return receivable;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantReceivable repay(Long receivableId, String reason) {
        requirePlatform();
        if (StringUtils.isBlank(reason)) throw new ServiceException("Repayment reason is required");
        return TenantContextHolder.callWithIgnore(() -> {
            MerchantReceivable row = receivableMapper.selectById(receivableId);
            if (row == null) throw new ServiceException("Receivable does not exist");
            CreditRepayBo bo = new CreditRepayBo();
            bo.setAmount(row.getOutstandingAmount());
            bo.setPaidTime(TimeUtils.utcNow());
            bo.setMethod("LEGACY_MANUAL");
            bo.setReference("LEGACY-" + receivableId);
            bo.setReason(reason);
            bo.setIdempotencyKey("legacy-repay-" + receivableId);
            return repayPlatform(receivableId, bo);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantReceivable repay(Long receivableId, CreditRepayBo bo) {
        requirePlatform();
        return TenantContextHolder.callWithIgnore(() -> repayPlatform(receivableId, bo));
    }

    private MerchantReceivable repayPlatform(Long receivableId, CreditRepayBo bo) {
        BigDecimal amount = exactMoney(bo.getAmount());
        MerchantCreditTransaction prior = ledger.findRepayment(receivableId, bo.getIdempotencyKey());
        if (prior != null) {
            if (prior.getAmount().compareTo(amount) != 0) throw new ServiceException("Idempotency key was used with another amount");
            return receivableMapper.selectById(receivableId);
        }
        MerchantReceivable row = receivableMapper.selectById(receivableId);
        if (row == null || (!ReceivableStatus.UNPAID.name().equals(row.getStatus())
            && !ReceivableStatus.PARTIALLY_PAID.name().equals(row.getStatus())
            && !ReceivableStatus.OVERDUE.name().equals(row.getStatus()))) {
            throw new ServiceException("Receivable cannot be repaid");
        }
        if (amount.signum() <= 0 || amount.compareTo(row.getOutstandingAmount()) > 0) {
            throw new ServiceException("Repayment amount exceeds outstanding amount");
        }
        MerchantCreditAccount account = accounts.byId(row.getCreditAccountId());
        if (account == null) throw new ServiceException("Merchant credit account does not exist");
        BigDecimal afterOutstanding = row.getOutstandingAmount().subtract(amount);
        BigDecimal afterRepaid = row.getRepaidAmount().add(amount);
        BigDecimal afterUsed = account.getUsedCredit().subtract(amount);
        if (afterUsed.signum() < 0) throw new ServiceException("Credit account used amount is invalid");
        boolean settled = afterOutstanding.signum() == 0;
        String afterStatus = settled ? ReceivableStatus.SETTLED.name()
            : ReceivableStatus.OVERDUE.name().equals(row.getStatus()) ? row.getStatus() : ReceivableStatus.PARTIALLY_PAID.name();
        var now = TimeUtils.utcNow();
        int rows = receivableMapper.update(null, new LambdaUpdateWrapper<MerchantReceivable>()
            .set(MerchantReceivable::getRepaidAmount, afterRepaid)
            .set(MerchantReceivable::getOutstandingAmount, afterOutstanding)
            .set(MerchantReceivable::getStatus, afterStatus)
            .set(MerchantReceivable::getSettledTime, settled ? now : null)
            .eq(MerchantReceivable::getReceivableId, row.getReceivableId())
            .eq(MerchantReceivable::getStatus, row.getStatus())
            .eq(MerchantReceivable::getRepaidAmount, row.getRepaidAmount())
            .eq(MerchantReceivable::getOutstandingAmount, row.getOutstandingAmount()));
        if (rows != 1) throw new ServiceException("Receivable was changed concurrently");
        accounts.changeUsed(account, afterUsed);
        bo.setAmount(amount);
        ledger.repayment(account, row, bo, account.getUsedCredit(), afterUsed);
        row.setRepaidAmount(afterRepaid);
        row.setOutstandingAmount(afterOutstanding);
        row.setStatus(afterStatus);
        row.setSettledTime(settled ? now : null);
        return row;
    }

    private BigDecimal exactMoney(BigDecimal amount) {
        if (amount == null) throw new ServiceException("Repayment amount is required");
        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new ServiceException("Repayment amount must use currency precision");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantCreditAccount adjust(Long accountId, CreditAdjustBo bo) {
        requirePlatform();
        return TenantContextHolder.callWithIgnore(() -> adjustPlatform(accountId, bo));
    }

    private MerchantCreditAccount adjustPlatform(Long accountId, CreditAdjustBo bo) {
        MerchantCreditAccount account = accounts.byId(accountId);
        BigDecimal afterLimit = account.getCreditLimit().add(bo.getAmount());
        if (afterLimit.compareTo(account.getUsedCredit()) < 0 || afterLimit.signum() < 0) {
            throw new ServiceException("Adjusted credit limit cannot be below used credit");
        }
        accounts.changeLimit(account, afterLimit);
        ledger.transaction(account, CreditTransactionType.ADJUST, accountId, null, bo.getAmount(),
            account.getCreditLimit(), afterLimit, account.getUsedCredit(), account.getUsedCredit(), bo.getReason());
        account.setCreditLimit(afterLimit);
        return account;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantCreditAccount freeze(Long accountId, CreditFreezeBo bo) {
        requirePlatform();
        return TenantContextHolder.callWithIgnore(() -> freezePlatform(accountId, bo));
    }

    private MerchantCreditAccount freezePlatform(Long accountId, CreditFreezeBo bo) {
        MerchantCreditAccount account = accounts.byId(accountId);
        String status = Boolean.TRUE.equals(bo.getFrozen()) ? CreditAccountStatus.FROZEN.name() : CreditAccountStatus.NORMAL.name();
        CreditTransactionType type = Boolean.TRUE.equals(bo.getFrozen())
            ? CreditTransactionType.FREEZE : CreditTransactionType.UNFREEZE;
        accounts.changeStatus(account, status, Boolean.TRUE.equals(bo.getFrozen()), bo.getReason());
        ledger.transaction(account, type, accountId, null, BigDecimal.ZERO.setScale(2),
            account.getCreditLimit(), account.getCreditLimit(), account.getUsedCredit(), account.getUsedCredit(), bo.getReason());
        account.setStatus(status);
        return account;
    }

    private void requirePlatform() {
        if (!operator.isPlatform()) throw new ServiceException("Platform tenant is required");
    }
}
