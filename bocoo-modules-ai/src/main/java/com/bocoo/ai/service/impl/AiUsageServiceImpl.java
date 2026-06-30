package com.bocoo.ai.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.ai.domain.bo.AiAuditReportBo;
import com.bocoo.ai.domain.bo.AiAuditSummaryBo;
import com.bocoo.ai.domain.bo.AiUsageLedgerBo;
import com.bocoo.ai.domain.bo.AiUsageReportBo;
import com.bocoo.ai.domain.bo.AiUserQuotaBo;
import com.bocoo.ai.domain.entity.AiAuditSummary;
import com.bocoo.ai.domain.entity.AiUsageDaily;
import com.bocoo.ai.domain.entity.AiUsageLedger;
import com.bocoo.ai.domain.entity.AiUserQuota;
import com.bocoo.ai.domain.vo.AiAuditSummaryVo;
import com.bocoo.ai.domain.vo.AiQuotaVo;
import com.bocoo.ai.domain.vo.AiUsageLedgerVo;
import com.bocoo.ai.domain.vo.AiUserQuotaVo;
import com.bocoo.ai.mapper.AiAuditSummaryMapper;
import com.bocoo.ai.mapper.AiUsageDailyMapper;
import com.bocoo.ai.mapper.AiUsageLedgerMapper;
import com.bocoo.ai.mapper.AiUserQuotaMapper;
import com.bocoo.ai.service.AiUsageService;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AiUsageServiceImpl implements AiUsageService {

    private static final String STATUS_ENABLED = "1";
    private static final long DEFAULT_DAILY_REQUEST_LIMIT = 500L;
    private static final long DEFAULT_DAILY_TOKEN_LIMIT = 10_000_000L;
    private static final BigDecimal DEFAULT_DAILY_COST_LIMIT = BigDecimal.valueOf(5).setScale(2, RoundingMode.HALF_UP);

    private final AiUsageLedgerMapper usageLedgerMapper;
    private final AiAuditSummaryMapper auditSummaryMapper;
    private final AiUserQuotaMapper userQuotaMapper;
    private final AiUsageDailyMapper usageDailyMapper;
    private final SysUserMapper userMapper;

    @Override
    public TableDataInfo<AiUserQuotaVo> queryUserQuotaPageList(AiUserQuotaBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AiUserQuota> wrapper = new LambdaQueryWrapper<AiUserQuota>()
            .eq(AiUserQuota::getTenantId, resolveTenantId(bo.getTenantId()))
            .eq(bo.getUserId() != null, AiUserQuota::getUserId, bo.getUserId())
            .eq(StringUtils.isNotBlank(bo.getStatus()), AiUserQuota::getStatus, bo.getStatus());
        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            wrapper.orderByDesc(AiUserQuota::getUpdateTime).orderByDesc(AiUserQuota::getCreateTime);
        }
        Page<AiUserQuotaVo> page = userQuotaMapper.selectVoPage(pageQuery.build(), wrapper);
        fillUserInfo(page.getRecords(), AiUserQuotaVo::getUserId, this::setQuotaUserInfo);
        return TableDataInfo.build(page);
    }

    @Override
    public Boolean insertUserQuota(AiUserQuotaBo bo) {
        Long tenantId = resolveTenantId(bo.getTenantId());
        assertQuotaUnique(null, tenantId, bo.getUserId());
        applyQuotaDefaults(bo);
        AiUserQuota quota = MapstructUtils.convert(bo, AiUserQuota.class);
        if (quota == null) {
            throw ServiceException.ofMessageKey("ai.quota.invalid");
        }
        quota.setQuotaId(IdUtil.getSnowflakeNextId());
        quota.setTenantId(tenantId);
        quota.setStatus(StringUtils.isBlank(quota.getStatus()) ? STATUS_ENABLED : quota.getStatus());
        quota.setCreateBy(LoginHelper.getUsername());
        quota.setCreateTime(TimeUtils.utcNow());
        return userQuotaMapper.insert(quota) > 0;
    }

    @Override
    public Boolean updateUserQuota(AiUserQuotaBo bo) {
        if (bo.getQuotaId() == null) {
            throw ServiceException.ofMessageKey("ai.quota.id.required");
        }
        Long tenantId = resolveTenantId(bo.getTenantId());
        assertQuotaUnique(bo.getQuotaId(), tenantId, bo.getUserId());
        AiUserQuota quota = userQuotaMapper.selectById(bo.getQuotaId());
        if (quota == null) {
            throw ServiceException.ofMessageKey("ai.quota.notFound");
        }
        applyQuotaDefaults(bo);
        quota.setTenantId(tenantId);
        quota.setUserId(bo.getUserId());
        quota.setDailyRequestLimit(bo.getDailyRequestLimit());
        quota.setDailyTokenLimit(bo.getDailyTokenLimit());
        quota.setDailyCostLimit(bo.getDailyCostLimit());
        quota.setStatus(StringUtils.isBlank(bo.getStatus()) ? quota.getStatus() : bo.getStatus());
        quota.setUpdateBy(LoginHelper.getUsername());
        quota.setUpdateTime(TimeUtils.utcNow());
        return userQuotaMapper.updateById(quota) > 0;
    }

    @Override
    public TableDataInfo<AiUsageLedgerVo> queryUsageLedgerPageList(AiUsageLedgerBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AiUsageLedger> wrapper = new LambdaQueryWrapper<AiUsageLedger>()
            .eq(AiUsageLedger::getTenantId, resolveTenantId(bo.getTenantId()))
            .eq(bo.getUserId() != null, AiUsageLedger::getUserId, bo.getUserId())
            .eq(StringUtils.isNotBlank(bo.getProvider()), AiUsageLedger::getProvider, bo.getProvider())
            .eq(StringUtils.isNotBlank(bo.getModel()), AiUsageLedger::getModel, bo.getModel())
            .eq(StringUtils.isNotBlank(bo.getRequestId()), AiUsageLedger::getRequestId, bo.getRequestId())
            .eq(StringUtils.isNotBlank(bo.getStatus()), AiUsageLedger::getStatus, bo.getStatus());
        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            wrapper.orderByDesc(AiUsageLedger::getCreatedTime);
        }
        Page<AiUsageLedgerVo> page = usageLedgerMapper.selectVoPage(pageQuery.build(), wrapper);
        fillUserInfo(page.getRecords(), AiUsageLedgerVo::getUserId, this::setUsageUserInfo);
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<AiAuditSummaryVo> queryAuditSummaryPageList(AiAuditSummaryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AiAuditSummary> wrapper = new LambdaQueryWrapper<AiAuditSummary>()
            .eq(AiAuditSummary::getTenantId, resolveTenantId(bo.getTenantId()))
            .eq(bo.getUserId() != null, AiAuditSummary::getUserId, bo.getUserId())
            .eq(StringUtils.isNotBlank(bo.getActionType()), AiAuditSummary::getActionType, bo.getActionType())
            .eq(StringUtils.isNotBlank(bo.getToolCode()), AiAuditSummary::getToolCode, bo.getToolCode())
            .eq(StringUtils.isNotBlank(bo.getRiskLevel()), AiAuditSummary::getRiskLevel, bo.getRiskLevel())
            .eq(StringUtils.isNotBlank(bo.getApprovalStatus()), AiAuditSummary::getApprovalStatus, bo.getApprovalStatus())
            .eq(StringUtils.isNotBlank(bo.getRequestId()), AiAuditSummary::getRequestId, bo.getRequestId());
        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            wrapper.orderByDesc(AiAuditSummary::getCreatedTime);
        }
        Page<AiAuditSummaryVo> page = auditSummaryMapper.selectVoPage(pageQuery.build(), wrapper);
        fillUserInfo(page.getRecords(), AiAuditSummaryVo::getUserId, this::setAuditUserInfo);
        return TableDataInfo.build(page);
    }

    @Override
    public AiQuotaVo remainingQuotaForCurrentUser() {
        Long tenantId = LoginHelper.getTenantId();
        Long userId = LoginHelper.getUserId();
        if (tenantId == null || userId == null) {
            return emptyQuota();
        }
        return remainingQuota(tenantId, userId);
    }

    @Override
    public String quotaDenyReason(Long tenantId, Long userId) {
        return TenantContextHolder.callWithTenant(tenantId, () -> {
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
        });
    }

    @Override
    public void reportUsage(AiUsageReportBo bo) {
        TenantContextHolder.runWithTenant(bo.getTenantId(), () -> {
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
            ledger.setCreatedTime(TimeUtils.utcNow());
            usageLedgerMapper.insert(ledger);
            usageDailyMapper.increment(
                bo.getTenantId(),
                bo.getUserId(),
                LocalDate.now(TimeUtils.UTC),
                safeLong(bo.getInputTokens()),
                safeLong(bo.getOutputTokens()),
                safeDecimal(bo.getCostAmount())
            );
        });
    }

    @Override
    public void reportAudit(AiAuditReportBo bo) {
        TenantContextHolder.runWithTenant(bo.getTenantId(), () -> {
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
            audit.setCreatedTime(TimeUtils.utcNow());
            auditSummaryMapper.insert(audit);
        });
    }

    private void assertQuotaUnique(Long quotaId, Long tenantId, Long userId) {
        AiUserQuota existing = userQuotaMapper.selectOne(new LambdaQueryWrapper<AiUserQuota>()
            .eq(AiUserQuota::getTenantId, tenantId)
            .eq(AiUserQuota::getUserId, userId), false);
        if (existing != null && !existing.getQuotaId().equals(quotaId)) {
            throw ServiceException.ofMessageKey("ai.quota.user.exists");
        }
    }

    private void applyQuotaDefaults(AiUserQuotaBo bo) {
        if (bo.getDailyRequestLimit() == null) {
            bo.setDailyRequestLimit(DEFAULT_DAILY_REQUEST_LIMIT);
        }
        if (bo.getDailyTokenLimit() == null) {
            bo.setDailyTokenLimit(DEFAULT_DAILY_TOKEN_LIMIT);
        }
        if (bo.getDailyCostLimit() == null) {
            bo.setDailyCostLimit(DEFAULT_DAILY_COST_LIMIT);
        } else {
            bo.setDailyCostLimit(bo.getDailyCostLimit().setScale(2, RoundingMode.HALF_UP));
        }
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
            .eq(AiUserQuota::getStatus, STATUS_ENABLED), false);
    }

    private AiUsageDaily findTodayUsage(Long tenantId, Long userId) {
        AiUsageDaily usage = usageDailyMapper.selectOne(new LambdaQueryWrapper<AiUsageDaily>()
            .eq(AiUsageDaily::getTenantId, tenantId)
            .eq(AiUsageDaily::getUserId, userId)
            .eq(AiUsageDaily::getUsageDate, LocalDate.now(TimeUtils.UTC)), false);
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

    private Long resolveTenantId(Long requestedTenantId) {
        if (LoginHelper.isPlatformTenant() && requestedTenantId != null) {
            return requestedTenantId;
        }
        Long tenantId = LoginHelper.getTenantId();
        if (tenantId == null) {
            throw ServiceException.ofMessageKey("ai.tenant.required");
        }
        return tenantId;
    }

    private <T> void fillUserInfo(List<T> rows, Function<T, Long> userIdGetter, UserInfoSetter<T> setter) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        Set<Long> userIds = rows.stream()
            .map(userIdGetter)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, SysUser> userMap = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getUserId, SysUser::getUserName, SysUser::getNickName)
                .in(SysUser::getUserId, userIds))
            .stream()
            .collect(Collectors.toMap(SysUser::getUserId, Function.identity(), (left, right) -> left));
        rows.forEach(row -> {
            Long userId = userIdGetter.apply(row);
            SysUser user = userId == null ? null : userMap.getOrDefault(userId, null);
            if (user != null) {
                setter.set(row, user.getUserName(), user.getNickName());
            }
        });
    }

    private void setQuotaUserInfo(AiUserQuotaVo vo, String userName, String nickName) {
        vo.setUserName(userName);
        vo.setNickName(nickName);
    }

    private void setUsageUserInfo(AiUsageLedgerVo vo, String userName, String nickName) {
        vo.setUserName(userName);
        vo.setNickName(nickName);
    }

    private void setAuditUserInfo(AiAuditSummaryVo vo, String userName, String nickName) {
        vo.setUserName(userName);
        vo.setNickName(nickName);
    }

    @FunctionalInterface
    private interface UserInfoSetter<T> {
        void set(T row, String userName, String nickName);
    }
}
