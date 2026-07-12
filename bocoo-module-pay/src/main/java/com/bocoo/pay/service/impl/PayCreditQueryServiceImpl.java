package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.CreditAccountQueryBo;
import com.bocoo.pay.domain.bo.CreditTransactionQueryBo;
import com.bocoo.pay.domain.bo.ReceivableQueryBo;
import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import com.bocoo.pay.domain.entity.MerchantCreditTransaction;
import com.bocoo.pay.domain.entity.MerchantReceivable;
import com.bocoo.pay.domain.vo.CreditAccountSummaryVo;
import com.bocoo.pay.domain.vo.CreditTransactionVo;
import com.bocoo.pay.domain.vo.PayReceivableSummaryVo;
import com.bocoo.pay.mapper.MerchantCreditAccountMapper;
import com.bocoo.pay.mapper.MerchantCreditTransactionMapper;
import com.bocoo.pay.mapper.MerchantReceivableMapper;
import com.bocoo.pay.service.PayCreditQueryService;
import com.bocoo.pay.service.PayOperatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PayCreditQueryServiceImpl implements PayCreditQueryService {
    private final MerchantCreditAccountMapper accountMapper;
    private final MerchantCreditTransactionMapper transactionMapper;
    private final MerchantReceivableMapper receivableMapper;
    private final PayOperatorContext operator;

    @Override
    public TableDataInfo<CreditAccountSummaryVo> accounts(CreditAccountQueryBo query, PageQuery pageQuery) {
        Page<MerchantCreditAccount> page = TenantContextHolder.callWithIgnore(() -> accountMapper.selectPage(pageQuery.build(),
            new LambdaQueryWrapper<MerchantCreditAccount>()
                .eq(!operator.isPlatform(), MerchantCreditAccount::getTenantId, operator.tenantId())
                .eq(query.getMerchantId() != null, MerchantCreditAccount::getMerchantId, query.getMerchantId())
                .like(StringUtils.isNotBlank(query.getMerchantName()), MerchantCreditAccount::getMerchantName, query.getMerchantName())
                .eq(StringUtils.isNotBlank(query.getCurrency()), MerchantCreditAccount::getCurrency, query.getCurrency())
                .eq(StringUtils.isNotBlank(query.getStatus()), MerchantCreditAccount::getStatus, query.getStatus())
                .orderByDesc(MerchantCreditAccount::getUpdateTime).orderByDesc(MerchantCreditAccount::getCreditAccountId)));
        return result(page, CreditAccountSummaryVo::from);
    }

    @Override
    public TableDataInfo<CreditTransactionVo> transactions(CreditTransactionQueryBo query, PageQuery pageQuery) {
        Page<MerchantCreditTransaction> page = TenantContextHolder.callWithIgnore(() -> transactionMapper.selectPage(pageQuery.build(),
            new LambdaQueryWrapper<MerchantCreditTransaction>()
                .eq(!operator.isPlatform(), MerchantCreditTransaction::getTenantId, operator.tenantId())
                .eq(query.getCreditAccountId() != null, MerchantCreditTransaction::getCreditAccountId, query.getCreditAccountId())
                .eq(StringUtils.isNotBlank(query.getTransactionNo()), MerchantCreditTransaction::getTransactionNo, query.getTransactionNo())
                .eq(StringUtils.isNotBlank(query.getTransactionType()), MerchantCreditTransaction::getTransactionType, query.getTransactionType())
                .eq(StringUtils.isNotBlank(query.getBusinessNo()), MerchantCreditTransaction::getBusinessNo, query.getBusinessNo())
                .orderByDesc(MerchantCreditTransaction::getOccurredTime).orderByDesc(MerchantCreditTransaction::getCreditTransactionId)));
        return result(page, CreditTransactionVo::from);
    }

    @Override
    public TableDataInfo<PayReceivableSummaryVo> receivables(ReceivableQueryBo query, PageQuery pageQuery) {
        Page<MerchantReceivable> page = TenantContextHolder.callWithIgnore(() -> receivableMapper.selectPage(pageQuery.build(),
            new LambdaQueryWrapper<MerchantReceivable>()
                .eq(!operator.isPlatform(), MerchantReceivable::getTenantId, operator.tenantId())
                .eq(query.getMerchantId() != null, MerchantReceivable::getMerchantId, query.getMerchantId())
                .like(StringUtils.isNotBlank(query.getMerchantName()), MerchantReceivable::getMerchantName, query.getMerchantName())
                .eq(StringUtils.isNotBlank(query.getSalesOrderNo()), MerchantReceivable::getSalesOrderNo, query.getSalesOrderNo())
                .eq(StringUtils.isNotBlank(query.getPayOrderNo()), MerchantReceivable::getPayOrderNo, query.getPayOrderNo())
                .eq(StringUtils.isNotBlank(query.getStatus()), MerchantReceivable::getStatus, query.getStatus())
                .orderByDesc(MerchantReceivable::getFormedTime).orderByDesc(MerchantReceivable::getReceivableId)));
        return result(page, PayReceivableSummaryVo::from);
    }

    private <E, V> TableDataInfo<V> result(Page<E> page, Function<E, V> converter) {
        List<V> rows = page.getRecords().stream().map(converter).toList();
        TableDataInfo<V> result = TableDataInfo.build(rows);
        result.setTotal(page.getTotal());
        return result;
    }
}
