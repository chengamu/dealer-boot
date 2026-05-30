package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.domain.bo.PayTransferCreateBo;
import com.bocoo.pay.domain.entity.PayTransfer;
import com.bocoo.pay.enums.PayTransferStatus;
import com.bocoo.pay.mapper.PayTransferMapper;
import com.bocoo.pay.service.PayTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment transfer service implementation.
 */
@RequiredArgsConstructor
@Service
public class PayTransferServiceImpl implements PayTransferService {

    private final PayTransferMapper payTransferMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayTransfer createTransfer(PayTransferCreateBo bo) {
        validateCreateTransfer(bo);
        PayTransfer existed = payTransferMapper.selectOne(new LambdaQueryWrapper<PayTransfer>()
            .eq(PayTransfer::getAppId, bo.getAppId())
            .eq(PayTransfer::getMerchantTransferId, bo.getMerchantTransferId()), false);
        if (existed != null) {
            return existed;
        }

        PayTransfer transfer = new PayTransfer();
        transfer.setTenantId(bo.getPayeeTenantId());
        transfer.setPayeeTenantId(bo.getPayeeTenantId());
        transfer.setNo(buildNo("PT"));
        transfer.setAppId(bo.getAppId());
        transfer.setChannelId(bo.getChannelId());
        transfer.setChannelCode(bo.getChannelCode());
        transfer.setMerchantTransferId(bo.getMerchantTransferId());
        transfer.setSubject(bo.getSubject());
        transfer.setPrice(bo.getPrice());
        transfer.setUserAccount(bo.getUserAccount());
        transfer.setUserName(bo.getUserName());
        transfer.setStatus(PayTransferStatus.WAITING.getStatus());
        transfer.setNotifyUrl(bo.getNotifyUrl());
        transfer.setUserIp(bo.getUserIp());
        transfer.setChannelExtras(StringUtils.blankToDefault(bo.getChannelExtras(), "{}"));
        payTransferMapper.insert(transfer);
        return transfer;
    }

    private void validateCreateTransfer(PayTransferCreateBo bo) {
        if (bo.getPayeeTenantId() == null || bo.getPayeeTenantId() <= 0) {
            throw new ServiceException("Transfer payee tenant is required");
        }
        if (bo.getAppId() == null || bo.getAppId() <= 0) {
            throw new ServiceException("Transfer app is required");
        }
        if (bo.getChannelId() == null || bo.getChannelId() <= 0) {
            throw new ServiceException("Transfer channel is required");
        }
        if (StringUtils.isBlank(bo.getChannelCode())) {
            throw new ServiceException("Transfer channel code is required");
        }
        if (StringUtils.isBlank(bo.getMerchantTransferId())) {
            throw new ServiceException("Merchant transfer id is required");
        }
        if (StringUtils.isBlank(bo.getSubject())) {
            throw new ServiceException("Transfer subject is required");
        }
        if (bo.getPrice() == null || bo.getPrice() <= 0) {
            throw new ServiceException("Transfer price is invalid");
        }
        if (StringUtils.isBlank(bo.getUserAccount())) {
            throw new ServiceException("Transfer user account is required");
        }
    }

    private String buildNo(String prefix) {
        return prefix + TimeUtils.utcNow().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
            + IdWorker.getId();
    }
}
