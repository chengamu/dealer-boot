package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.CreditTransactionQueryBo;
import com.bocoo.pay.domain.bo.ReceivableQueryBo;
import com.bocoo.pay.domain.credit.CreditSubject;
import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import com.bocoo.pay.domain.entity.MerchantCreditTransaction;
import com.bocoo.pay.domain.entity.MerchantReceivable;
import com.bocoo.pay.domain.vo.CreditAccountSummaryVo;
import com.bocoo.pay.domain.vo.CreditTransactionVo;
import com.bocoo.pay.domain.vo.PayReceivableSummaryVo;
import com.bocoo.pay.mapper.MerchantCreditTransactionMapper;
import com.bocoo.pay.mapper.MerchantReceivableMapper;
import com.bocoo.pay.service.BusinessCreditQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessCreditQueryServiceImpl implements BusinessCreditQueryService {
    private static final String CURRENCY_USD = "USD";
    private final CreditSubjectSupport subjects;
    private final CreditAccountRepository accounts;
    private final MerchantCreditTransactionMapper transactionMapper;
    private final MerchantReceivableMapper receivableMapper;

    @Override
    public CreditAccountSummaryVo account() {
        return CreditAccountSummaryVo.from(requiredAccount());
    }

    @Override
    public TableDataInfo<CreditTransactionVo> transactions(CreditTransactionQueryBo query, PageQuery pageQuery) {
        MerchantCreditAccount account = requiredAccount();
        Page<MerchantCreditTransaction> page = transactionMapper.selectPage(pageQuery.build(),
            new LambdaQueryWrapper<MerchantCreditTransaction>()
                .eq(MerchantCreditTransaction::getCreditAccountId, account.getCreditAccountId())
                .eq(StringUtils.isNotBlank(query.getTransactionType()), MerchantCreditTransaction::getTransactionType,
                    query.getTransactionType())
                .eq(StringUtils.isNotBlank(query.getBusinessNo()), MerchantCreditTransaction::getBusinessNo,
                    query.getBusinessNo())
                .orderByDesc(MerchantCreditTransaction::getOccurredTime)
                .orderByDesc(MerchantCreditTransaction::getCreditTransactionId));
        List<CreditTransactionVo> rows = page.getRecords().stream().map(CreditTransactionVo::from).toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    @Override
    public TableDataInfo<PayReceivableSummaryVo> receivables(ReceivableQueryBo query, PageQuery pageQuery) {
        MerchantCreditAccount account = requiredAccount();
        Page<MerchantReceivable> page = receivableMapper.selectPage(pageQuery.build(), receivableQuery(account, query)
            .orderByAsc(MerchantReceivable::getDueDate).orderByAsc(MerchantReceivable::getReceivableId));
        List<PayReceivableSummaryVo> rows = page.getRecords().stream().map(PayReceivableSummaryVo::from).toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    @Override
    public PayReceivableSummaryVo receivable(Long receivableId) {
        MerchantCreditAccount account = requiredAccount();
        MerchantReceivable row = receivableMapper.selectOne(new LambdaQueryWrapper<MerchantReceivable>()
            .eq(MerchantReceivable::getReceivableId, receivableId)
            .eq(MerchantReceivable::getCreditAccountId, account.getCreditAccountId()), false);
        if (row == null) throw new ServiceException("Receivable does not exist");
        return PayReceivableSummaryVo.from(row);
    }

    private MerchantCreditAccount requiredAccount() {
        CreditSubject subject = subjects.current(CURRENCY_USD);
        MerchantCreditAccount account = accounts.find(subject);
        if (account == null) throw new ServiceException("Credit account does not exist");
        return account;
    }

    private LambdaQueryWrapper<MerchantReceivable> receivableQuery(MerchantCreditAccount account, ReceivableQueryBo query) {
        return new LambdaQueryWrapper<MerchantReceivable>()
            .eq(MerchantReceivable::getCreditAccountId, account.getCreditAccountId())
            .eq(StringUtils.isNotBlank(query.getSalesOrderNo()), MerchantReceivable::getSalesOrderNo, query.getSalesOrderNo())
            .eq(StringUtils.isNotBlank(query.getPayOrderNo()), MerchantReceivable::getPayOrderNo, query.getPayOrderNo())
            .eq(StringUtils.isNotBlank(query.getStatus()), MerchantReceivable::getStatus, query.getStatus());
    }
}
