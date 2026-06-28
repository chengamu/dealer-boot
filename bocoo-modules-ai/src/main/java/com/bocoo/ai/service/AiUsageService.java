package com.bocoo.ai.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.ai.domain.bo.AiAuditReportBo;
import com.bocoo.ai.domain.bo.AiUserQuotaBo;
import com.bocoo.ai.domain.bo.AiUsageReportBo;
import com.bocoo.ai.domain.entity.AiAuditSummary;
import com.bocoo.ai.domain.entity.AiUsageDaily;
import com.bocoo.ai.domain.entity.AiUserQuota;
import com.bocoo.ai.domain.entity.AiUsageLedger;
import com.bocoo.ai.domain.vo.AiQuotaVo;
import com.bocoo.ai.domain.vo.AiUserQuotaVo;
import com.bocoo.ai.mapper.AiAuditSummaryMapper;
import com.bocoo.ai.mapper.AiUsageDailyMapper;
import com.bocoo.ai.mapper.AiUserQuotaMapper;
import com.bocoo.ai.mapper.AiUsageLedgerMapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AiUsageService {

    private final AiUsageLedgerMapper usageLedgerMapper;
    private final AiAuditSummaryMapper auditSummaryMapper;
    private final AiUserQuotaMapper userQuotaMapper;
    private final AiUsageDailyMapper usageDailyMapper;

    public List<AiUserQuotaVo> listUserQuotas(Long tenantId, Long userId, Integer limit) {
        return userQuotaMapper.selectList(new LambdaQueryWrapper<AiUserQuota>()
                .eq(AiUserQuota::getTenantId, resolveTenantId(tenantId))
                .eq(userId != null, AiUserQuota::getUserId, userId)
                .orderByDesc(AiUserQuota::getUpdateTime)
                .orderByDesc(AiUserQuota::getCreateTime)
                .last(limitClause(limit)))
            .stream()
            .map(this::toUserQuotaVo)
            .toList();
    }

    public void saveUserQuota(AiUserQuotaBo bo) {
        Long tenantId = resolveTenantId(bo.getTenantId());
        if (bo.getUserId() == null) {
            throw new ServiceException("User ID is required");
        }
        AiUserQuota existing = userQuotaMapper.selectOne(new LambdaQueryWrapper<AiUserQuota>()
            .eq(AiUserQuota::getTenantId, tenantId)
            .eq(AiUserQuota::getUserId, bo.getUserId()), false);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (existing == null) {
            AiUserQuota quota = new AiUserQuota();
            quota.setQuotaId(IdUtil.getSnowflakeNextId());
            quota.setTenantId(tenantId);
            quota.setUserId(bo.getUserId());
            quota.setDailyRequestLimit(bo.getDailyRequestLimit());
            quota.setDailyTokenLimit(bo.getDailyTokenLimit());
            quota.setDailyCostLimit(bo.getDailyCostLimit());
            quota.setStatus(bo.getStatus() == null ? "1" : bo.getStatus());
            quota.setCreateBy(LoginHelper.getUsername());
            quota.setCreateTime(now);
            userQuotaMapper.insert(quota);
            return;
        }
        existing.setDailyRequestLimit(bo.getDailyRequestLimit());
        existing.setDailyTokenLimit(bo.getDailyTokenLimit());
        existing.setDailyCostLimit(bo.getDailyCostLimit());
        existing.setStatus(bo.getStatus() == null ? existing.getStatus() : bo.getStatus());
        existing.setUpdateBy(LoginHelper.getUsername());
        existing.setUpdateTime(now);
        userQuotaMapper.updateById(existing);
    }

    public List<AiUsageLedger> listUsageLedgers(Long tenantId, Long userId, Integer limit) {
        return usageLedgerMapper.selectList(new LambdaQueryWrapper<AiUsageLedger>()
            .eq(AiUsageLedger::getTenantId, resolveTenantId(tenantId))
            .eq(userId != null, AiUsageLedger::getUserId, userId)
            .orderByDesc(AiUsageLedger::getCreatedTime)
            .last(limitClause(limit)));
    }

    public List<AiAuditSummary> listAuditSummaries(Long tenantId, Long userId, Integer limit) {
        return auditSummaryMapper.selectList(new LambdaQueryWrapper<AiAuditSummary>()
            .eq(AiAuditSummary::getTenantId, resolveTenantId(tenantId))
            .eq(userId != null, AiAuditSummary::getUserId, userId)
            .orderByDesc(AiAuditSummary::getCreatedTime)
            .last(limitClause(limit)));
    }

    public AiQuotaVo remainingQuotaForCurrentUser() {
        Long tenantId = LoginHelper.getTenantId();
        Long userId = LoginHelper.getUserId();
        if (tenantId == null || userId == null) {
            return emptyQuota();
        }
        return remainingQuota(tenantId, userId);
    }

    public String quotaDenyReason(Long tenantId, Long userId) {
        AiUserQuota quota = findActiveQuota(tenantId, userId);
        if (quota == null) {
            return null;
        }
        AiUsageDaily usage = findTodayUsage(tenantId, userId);
        if (quota.getDailyRequestLimit() != null && safeLong(usage.getRequestCount()) >= quota.getDailyRequestLimit()) {
            return "Daily request quota exceeded";
        }
        long usedTokens = safeLong(usage.getInputTokens()) + safeLong(usage.getOutputTokens());
        if (quota.getDailyTokenLimit() != null && usedTokens >= quota.getDailyTokenLimit()) {
            return "Daily token quota exceeded";
        }
        if (quota.getDailyCostLimit() != null && safeDecimal(usage.getCostAmount()).compareTo(quota.getDailyCostLimit()) >= 0) {
            return "Daily cost quota exceeded";
        }
        return null;
    }

    public void reportUsage(AiUsageReportBo bo) {
        if (bo.getProviderCallId() != null && existsUsage(bo.getProviderCallId())) {
            return;
        }
        AiUsageLedger ledger = new AiUsageLedger();
        ledger.setUsageId(IdUtil.getSnowflakeNextId());
        ledger.setTenantId(bo.getTenantId());
        ledger.setUserId(bo.getUserId());
        ledger.setSessionId(bo.getSessionId());
        ledger.setRequestId(bo.getRequestId());
        ledger.setProviderCallId(bo.getProviderCallId());
        ledger.setProvider(bo.getProvider());
        ledger.setModel(bo.getModel());
        ledger.setInputTokens(bo.getInputTokens());
        ledger.setOutputTokens(bo.getOutputTokens());
        ledger.setCostAmount(bo.getCostAmount());
        ledger.setLatencyMs(bo.getLatencyMs());
        ledger.setStatus(bo.getStatus());
        ledger.setCreatedTime(LocalDateTime.now(ZoneOffset.UTC));
        usageLedgerMapper.insert(ledger);
        usageDailyMapper.increment(
            bo.getTenantId(),
            bo.getUserId(),
            LocalDate.now(ZoneOffset.UTC),
            safeLong(bo.getInputTokens()),
            safeLong(bo.getOutputTokens()),
            safeDecimal(bo.getCostAmount())
        );
    }

    public void reportAudit(AiAuditReportBo bo) {
        AiAuditSummary audit = new AiAuditSummary();
        audit.setAuditId(IdUtil.getSnowflakeNextId());
        audit.setTenantId(bo.getTenantId());
        audit.setUserId(bo.getUserId());
        audit.setSessionId(bo.getSessionId());
        audit.setRequestId(bo.getRequestId());
        audit.setActionType(bo.getActionType());
        audit.setToolCode(bo.getToolCode());
        audit.setBusinessTarget(bo.getBusinessTarget());
        audit.setRiskLevel(bo.getRiskLevel());
        audit.setApprovalStatus(bo.getApprovalStatus());
        audit.setStatus(bo.getStatus());
        audit.setCreatedTime(LocalDateTime.now(ZoneOffset.UTC));
        auditSummaryMapper.insert(audit);
    }

    private boolean existsUsage(String providerCallId) {
        Long count = usageLedgerMapper.selectCount(new LambdaQueryWrapper<AiUsageLedger>()
            .eq(AiUsageLedger::getProviderCallId, providerCallId));
        return count != null && count > 0;
    }

    private AiQuotaVo remainingQuota(Long tenantId, Long userId) {
        AiUserQuota quota = findActiveQuota(tenantId, userId);
        if (quota == null) {
            return emptyQuota();
        }
        AiUsageDaily usage = findTodayUsage(tenantId, userId);
        AiQuotaVo vo = new AiQuotaVo();
        vo.setDailyRequestRemaining(remaining(quota.getDailyRequestLimit(), safeLong(usage.getRequestCount())));
        vo.setDailyTokenRemaining(remaining(quota.getDailyTokenLimit(), safeLong(usage.getInputTokens()) + safeLong(usage.getOutputTokens())));
        vo.setDailyCostRemaining(remaining(quota.getDailyCostLimit(), safeDecimal(usage.getCostAmount())));
        return vo;
    }

    private AiQuotaVo emptyQuota() {
        AiQuotaVo quota = new AiQuotaVo();
        quota.setDailyRequestRemaining(null);
        quota.setDailyTokenRemaining(null);
        quota.setDailyCostRemaining(null);
        return quota;
    }

    private AiUserQuota findActiveQuota(Long tenantId, Long userId) {
        return userQuotaMapper.selectOne(new LambdaQueryWrapper<AiUserQuota>()
            .eq(AiUserQuota::getTenantId, tenantId)
            .eq(AiUserQuota::getUserId, userId)
            .eq(AiUserQuota::getStatus, "1"), false);
    }

    private AiUsageDaily findTodayUsage(Long tenantId, Long userId) {
        AiUsageDaily usage = usageDailyMapper.selectOne(new LambdaQueryWrapper<AiUsageDaily>()
            .eq(AiUsageDaily::getTenantId, tenantId)
            .eq(AiUsageDaily::getUserId, userId)
            .eq(AiUsageDaily::getUsageDate, LocalDate.now(ZoneOffset.UTC)), false);
        if (usage != null) {
            return usage;
        }
        AiUsageDaily empty = new AiUsageDaily();
        empty.setRequestCount(0L);
        empty.setInputTokens(0L);
        empty.setOutputTokens(0L);
        empty.setCostAmount(BigDecimal.ZERO);
        return empty;
    }

    private Long remaining(Long limit, long used) {
        return limit == null ? null : Math.max(0, limit - used);
    }

    private BigDecimal remaining(BigDecimal limit, BigDecimal used) {
        return limit == null ? null : limit.subtract(used).max(BigDecimal.ZERO);
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private AiUserQuotaVo toUserQuotaVo(AiUserQuota quota) {
        AiUserQuotaVo vo = new AiUserQuotaVo();
        BeanUtils.copyProperties(quota, vo);
        return vo;
    }

    private Long resolveTenantId(Long requestedTenantId) {
        if (LoginHelper.isPlatformTenant() && requestedTenantId != null) {
            return requestedTenantId;
        }
        Long tenantId = LoginHelper.getTenantId();
        if (tenantId == null) {
            throw new ServiceException("Tenant ID is required");
        }
        return tenantId;
    }

    private String limitClause(Integer limit) {
        int value = limit == null ? 200 : Math.max(1, Math.min(limit, 500));
        return "LIMIT " + value;
    }
}
