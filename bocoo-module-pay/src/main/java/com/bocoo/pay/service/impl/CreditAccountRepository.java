package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.domain.bo.CreditOccupyBo;
import com.bocoo.pay.domain.credit.CreditSubject;
import com.bocoo.pay.domain.credit.CreditSubjectType;
import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import com.bocoo.pay.mapper.MerchantCreditAccountMapper;
import com.bocoo.pay.service.PayOperatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
class CreditAccountRepository {
    private final MerchantCreditAccountMapper mapper;
    private final PayOperatorContext operator;

    MerchantCreditAccount find(CreditSubject subject) {
        LambdaQueryWrapper<MerchantCreditAccount> query = new LambdaQueryWrapper<MerchantCreditAccount>()
            .eq(MerchantCreditAccount::getBusinessOrigin, subject.type().name())
            .eq(MerchantCreditAccount::getCurrency, subject.currency());
        if (subject.type() == CreditSubjectType.MERCHANT) {
            query.eq(MerchantCreditAccount::getTenantId, subject.tenantId())
                .isNull(MerchantCreditAccount::getSalesStoreId);
        } else {
            query.eq(MerchantCreditAccount::getSalesStoreId, subject.salesStoreId());
        }
        return mapper.selectOne(query, false);
    }

    MerchantCreditAccount getOrCreate(CreditSubject subject, CreditOccupyBo bo) {
        MerchantCreditAccount account = find(subject);
        if (account != null) return account;
        account = new MerchantCreditAccount();
        account.setBusinessOrigin(subject.type().name());
        account.setTenantId(subject.tenantId());
        account.setSalesStoreId(subject.salesStoreId());
        account.setMerchantId(subject.merchantId());
        account.setMerchantName(subject.subjectName());
        account.setCreditLimit(bo.getConfiguredCreditLimit());
        account.setUsedCredit(BigDecimal.ZERO.setScale(2));
        account.setCurrency(subject.currency());
        account.setStatus("NORMAL");
        account.setVersion(0);
        account.setCreditAccountId(IdWorker.getId());
        account.setCreateById(operator.userId());
        account.setCreateBy(operator.username());
        account.setCreateTime(TimeUtils.utcNow());
        account.setUpdateBy(operator.username());
        account.setUpdateTime(account.getCreateTime());
        if (mapper.insertIgnoreConflict(account) == 1) return account;
        MerchantCreditAccount existing = find(subject);
        if (existing != null) return existing;
        throw new ServiceException("Credit account conflict could not be resolved");
    }

    MerchantCreditAccount byId(Long accountId) {
        MerchantCreditAccount account = mapper.selectById(accountId);
        if (account == null) throw new ServiceException("Merchant credit account does not exist");
        return account;
    }

    void changeUsed(MerchantCreditAccount account, BigDecimal afterUsed) {
        int rows = mapper.update(null, new LambdaUpdateWrapper<MerchantCreditAccount>()
            .set(MerchantCreditAccount::getUsedCredit, afterUsed).setSql("version = version + 1")
            .eq(MerchantCreditAccount::getCreditAccountId, account.getCreditAccountId())
            .eq(MerchantCreditAccount::getUsedCredit, account.getUsedCredit())
            .eq(MerchantCreditAccount::getVersion, account.getVersion()));
        if (rows != 1) throw new ServiceException("Credit account was changed concurrently");
    }

    void changeLimit(MerchantCreditAccount account, BigDecimal afterLimit) {
        int rows = mapper.update(null, new LambdaUpdateWrapper<MerchantCreditAccount>()
            .set(MerchantCreditAccount::getCreditLimit, afterLimit).setSql("version = version + 1")
            .eq(MerchantCreditAccount::getCreditAccountId, account.getCreditAccountId())
            .eq(MerchantCreditAccount::getCreditLimit, account.getCreditLimit())
            .eq(MerchantCreditAccount::getVersion, account.getVersion()));
        if (rows != 1) throw new ServiceException("Credit account was changed concurrently");
    }

    void changeStatus(MerchantCreditAccount account, String status, boolean frozen, String reason) {
        int rows = mapper.update(null, new LambdaUpdateWrapper<MerchantCreditAccount>()
            .set(MerchantCreditAccount::getStatus, status).setSql("version = version + 1")
            .set(MerchantCreditAccount::getFrozenById, frozen ? operator.userId() : null)
            .set(MerchantCreditAccount::getFrozenBy, frozen ? operator.username() : null)
            .set(MerchantCreditAccount::getFrozenTime, frozen ? com.bocoo.common.core.utils.TimeUtils.utcNow() : null)
            .set(MerchantCreditAccount::getFrozenReason, frozen ? reason : null)
            .eq(MerchantCreditAccount::getCreditAccountId, account.getCreditAccountId())
            .eq(MerchantCreditAccount::getStatus, account.getStatus())
            .eq(MerchantCreditAccount::getVersion, account.getVersion()));
        if (rows != 1) throw new ServiceException("Credit account was changed concurrently");
    }
}
