package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.domain.bo.PayWalletChangeBo;
import com.bocoo.pay.domain.entity.PayWallet;
import com.bocoo.pay.domain.entity.PayWalletTransaction;
import com.bocoo.pay.mapper.PayWalletMapper;
import com.bocoo.pay.mapper.PayWalletTransactionMapper;
import com.bocoo.pay.service.PayWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment wallet service implementation.
 */
@RequiredArgsConstructor
@Service
public class PayWalletServiceImpl implements PayWalletService {

    private final PayWalletMapper payWalletMapper;
    private final PayWalletTransactionMapper payWalletTransactionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayWallet getOrCreateWallet(Long tenantId, Long userId, String userType) {
        validateWalletOwner(tenantId, userId, userType);
        PayWallet wallet = findWallet(tenantId, userId, userType);
        if (wallet != null) {
            return wallet;
        }

        PayWallet created = new PayWallet();
        created.setTenantId(tenantId);
        created.setUserId(userId);
        created.setUserType(userType);
        created.setBalance(0L);
        created.setTotalExpense(0L);
        created.setTotalRecharge(0L);
        try {
            payWalletMapper.insert(created);
            return created;
        } catch (DuplicateKeyException ex) {
            PayWallet existed = findWallet(tenantId, userId, userType);
            if (existed != null) {
                return existed;
            }
            throw ex;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayWalletTransaction changeBalance(PayWalletChangeBo bo) {
        validateChange(bo);
        PayWallet wallet = payWalletMapper.selectById(bo.getWalletId());
        if (wallet == null) {
            throw new ServiceException("Wallet does not exist");
        }

        long oldBalance = zeroIfNull(wallet.getBalance());
        long price = bo.getPrice();
        long newBalance = oldBalance + price;
        if (newBalance < 0) {
            throw new ServiceException("Wallet balance is insufficient");
        }
        long newTotalRecharge = zeroIfNull(wallet.getTotalRecharge()) + Math.max(price, 0L);
        long newTotalExpense = zeroIfNull(wallet.getTotalExpense()) + Math.max(-price, 0L);

        int rows = payWalletMapper.update(null, new LambdaUpdateWrapper<PayWallet>()
            .set(PayWallet::getBalance, newBalance)
            .set(PayWallet::getTotalRecharge, newTotalRecharge)
            .set(PayWallet::getTotalExpense, newTotalExpense)
            .eq(PayWallet::getId, wallet.getId())
            .eq(PayWallet::getBalance, oldBalance));
        if (rows == 0) {
            throw new ServiceException("Wallet balance changed, please retry");
        }

        PayWalletTransaction transaction = new PayWalletTransaction();
        transaction.setTenantId(wallet.getTenantId());
        transaction.setWalletId(wallet.getId());
        transaction.setNo(buildNo("WT"));
        transaction.setTitle(bo.getTitle());
        transaction.setPrice(price);
        transaction.setBalance(newBalance);
        transaction.setBizType(bo.getBizType());
        transaction.setBizId(bo.getBizId());
        transaction.setRemark(bo.getRemark());
        payWalletTransactionMapper.insert(transaction);
        return transaction;
    }

    private PayWallet findWallet(Long tenantId, Long userId, String userType) {
        return payWalletMapper.selectOne(new LambdaQueryWrapper<PayWallet>()
            .eq(PayWallet::getTenantId, tenantId)
            .eq(PayWallet::getUserId, userId)
            .eq(PayWallet::getUserType, userType), false);
    }

    private void validateWalletOwner(Long tenantId, Long userId, String userType) {
        if (tenantId == null || tenantId <= 0) {
            throw new ServiceException("Wallet tenant is required");
        }
        if (userId == null || userId <= 0) {
            throw new ServiceException("Wallet user is required");
        }
        if (StringUtils.isBlank(userType)) {
            throw new ServiceException("Wallet user type is required");
        }
    }

    private void validateChange(PayWalletChangeBo bo) {
        if (bo.getWalletId() == null || bo.getWalletId() <= 0) {
            throw new ServiceException("Wallet id is required");
        }
        if (bo.getPrice() == null || bo.getPrice() == 0) {
            throw new ServiceException("Wallet change price is required");
        }
        if (StringUtils.isBlank(bo.getTitle())) {
            throw new ServiceException("Wallet transaction title is required");
        }
        if (bo.getBizType() == null) {
            throw new ServiceException("Wallet transaction biz type is required");
        }
        if (StringUtils.isBlank(bo.getBizId())) {
            throw new ServiceException("Wallet transaction biz id is required");
        }
    }

    private long zeroIfNull(Long value) {
        return value == null ? 0L : value;
    }

    private String buildNo(String prefix) {
        return prefix + TimeUtils.utcNow().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
            + IdWorker.getId();
    }
}
