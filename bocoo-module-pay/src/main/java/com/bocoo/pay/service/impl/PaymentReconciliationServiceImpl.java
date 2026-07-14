package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.ReconciliationCaseQueryBo;
import com.bocoo.pay.domain.entity.PayReconciliationAction;
import com.bocoo.pay.domain.entity.PayReconciliationCase;
import com.bocoo.pay.domain.vo.ReconciliationCaseDetailVo;
import com.bocoo.pay.domain.vo.ReconciliationActionVo;
import com.bocoo.pay.domain.vo.ReconciliationCaseVo;
import com.bocoo.pay.enums.ReconciliationActionType;
import com.bocoo.pay.enums.ReconciliationAnomalyType;
import com.bocoo.pay.enums.ReconciliationCaseStatus;
import com.bocoo.pay.enums.ReconciliationSeverity;
import com.bocoo.pay.mapper.PayReconciliationActionMapper;
import com.bocoo.pay.mapper.PayReconciliationCaseMapper;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PayOrderDetailService;
import com.bocoo.pay.service.PayPalPaymentService;
import com.bocoo.pay.service.PaymentReconciliationService;
import com.bocoo.pay.service.PaymentSuccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentReconciliationServiceImpl implements PaymentReconciliationService {
    private final PayReconciliationCaseMapper caseMapper;
    private final PayReconciliationActionMapper actionMapper;
    private final ReconciliationCaseRepository cases;
    private final ReconciliationDetector detector;
    private final ReconciliationActionWriter actions;
    private final PayOrderDetailService paymentDetails;
    private final PayPalPaymentService payPalService;
    private final PaymentSuccessService successService;
    private final PayOperatorContext operator;

    @Override
    public TableDataInfo<ReconciliationCaseVo> list(ReconciliationCaseQueryBo query, PageQuery pageQuery) {
        requirePlatform();
        QueryWrapper<PayReconciliationCase> wrapper = new QueryWrapper<PayReconciliationCase>()
            .ge(query.getBeginTime() != null, "detected_time", query.getBeginTime())
            .le(query.getEndTime() != null, "detected_time", query.getEndTime())
            .eq(StringUtils.isNotBlank(query.getStatus()), "status", query.getStatus())
            .eq(StringUtils.isNotBlank(query.getAnomalyType()), "anomaly_type", query.getAnomalyType())
            .eq(StringUtils.isNotBlank(query.getSeverity()), "severity", query.getSeverity())
            .eq(StringUtils.isNotBlank(query.getBusinessOrigin()), "business_origin", query.getBusinessOrigin())
            .and(query.getSubjectId() != null, q -> q.eq("tenant_id", query.getSubjectId())
                .or().eq("sales_store_id", query.getSubjectId()))
            .and(StringUtils.isNotBlank(query.getKeyword()), q -> q.eq("case_no", query.getKeyword())
                .or().eq("pay_order_id", query.getKeyword()).or().eq("sales_document_id", query.getKeyword()));
        if (ReconciliationCaseStatus.OPEN.name().equals(query.getStatus())) {
            wrapper.orderByAsc("CASE WHEN severity = 'CRITICAL' THEN 0 ELSE 1 END")
                .orderByAsc("detected_time", "case_id");
        } else {
            wrapper.orderByDesc("resolved_time", "case_id");
        }
        Page<PayReconciliationCase> page = TenantContextHolder.callWithIgnore(() -> caseMapper.selectPage(pageQuery.build(), wrapper));
        List<ReconciliationCaseVo> rows = page.getRecords().stream().map(ReconciliationCaseVo::from).toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    @Override
    public ReconciliationCaseDetailVo detail(Long caseId) {
        requirePlatform();
        PayReconciliationCase row = TenantContextHolder.callWithIgnore(() -> cases.required(caseId));
        List<PayReconciliationAction> history = TenantContextHolder.callWithIgnore(() -> actionMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PayReconciliationAction>()
                .eq(PayReconciliationAction::getCaseId, caseId)
                .orderByAsc(PayReconciliationAction::getOccurredTime).orderByAsc(PayReconciliationAction::getActionId)));
        return ReconciliationCaseDetailVo.builder().reconciliationCase(ReconciliationCaseVo.from(row))
            .payment(paymentDetails.getDetail(row.getPayOrderId()))
            .actions(history.stream().map(ReconciliationActionVo::from).toList()).build();
    }

    @Override
    public ReconciliationCaseVo scanPayment(Long payOrderId) {
        requirePlatform();
        return ReconciliationCaseVo.from(TenantContextHolder.callWithIgnore(
            () -> detector.scan(payOrderId, ReconciliationActionType.DETECT, "platform_scan")));
    }

    @Override
    public ReconciliationCaseVo rescan(Long caseId, String reason) {
        PayReconciliationCase row = requiredOpen(caseId, reason);
        PayReconciliationCase result = TenantContextHolder.callWithIgnore(
            () -> detector.scan(row.getPayOrderId(), ReconciliationActionType.RESCAN, reason));
        return ReconciliationCaseVo.from(result == null
            ? TenantContextHolder.callWithIgnore(() -> cases.required(caseId)) : result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReconciliationCaseVo reconcileChannel(Long caseId, String reason) {
        PayReconciliationCase row = requiredOpen(caseId, reason);
        if (!ReconciliationAnomalyType.CHANNEL_SUCCESS_LOCAL_PENDING.name().equals(row.getAnomalyType())
            && !ReconciliationAnomalyType.WEBHOOK_PROCESS_FAILED.name().equals(row.getAnomalyType())) {
            throw new ServiceException("This case cannot reconcile a payment channel");
        }
        return TenantContextHolder.callWithIgnore(() -> {
            payPalService.reconcile(row.getPayOrderId());
            PayReconciliationCase result = detector.scan(row.getPayOrderId(), ReconciliationActionType.RECONCILE_CHANNEL, reason);
            return ReconciliationCaseVo.from(result == null ? cases.required(caseId) : result);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReconciliationCaseVo repairOrder(Long caseId, String reason) {
        PayReconciliationCase row = requiredOpen(caseId, reason);
        if (!ReconciliationAnomalyType.PAYMENT_SUCCESS_ORDER_UNPAID.name().equals(row.getAnomalyType())) {
            throw new ServiceException("This case cannot repair an order payment callback");
        }
        return TenantContextHolder.callWithIgnore(() -> {
            successService.repair(row.getPayOrderId());
            PayReconciliationCase result = detector.scan(row.getPayOrderId(), ReconciliationActionType.REPAIR_ORDER, reason);
            return ReconciliationCaseVo.from(result == null ? cases.required(caseId) : result);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReconciliationCaseVo ignore(Long caseId, String reason) {
        PayReconciliationCase row = requiredOpen(caseId, reason);
        if (ReconciliationSeverity.CRITICAL.name().equals(row.getSeverity())) {
            throw new ServiceException("Critical reconciliation cases cannot be ignored");
        }
        return TenantContextHolder.callWithIgnore(() -> {
            String before = row.getActualSnapshotJson();
            cases.finish(row, ReconciliationCaseStatus.IGNORED, "platform_ignored", operator.userId(), operator.username());
            actions.write(row, ReconciliationActionType.IGNORE, before, before, "SUCCESS", "platform_ignored",
                "Case ignored without changing payment facts", reason);
            return ReconciliationCaseVo.from(row);
        });
    }

    private PayReconciliationCase requiredOpen(Long caseId, String reason) {
        requirePlatform();
        if (StringUtils.isBlank(reason)) throw new ServiceException("Action reason is required");
        PayReconciliationCase row = TenantContextHolder.callWithIgnore(() -> cases.required(caseId));
        if (!ReconciliationCaseStatus.OPEN.name().equals(row.getStatus())) {
            throw new ServiceException("Reconciliation case is already closed");
        }
        return row;
    }

    private void requirePlatform() {
        if (!operator.isPlatform()) throw new ServiceException("Platform tenant is required");
    }
}
