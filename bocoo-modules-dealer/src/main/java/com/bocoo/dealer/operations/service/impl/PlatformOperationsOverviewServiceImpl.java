package com.bocoo.dealer.operations.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.operations.domain.bo.OperationsMerchantQueryBo;
import com.bocoo.dealer.operations.domain.vo.OperationsCurrencyAmountVo;
import com.bocoo.dealer.operations.domain.vo.OperationsMerchantLevelOptionVo;
import com.bocoo.dealer.operations.domain.vo.OperationsMerchantVo;
import com.bocoo.dealer.operations.domain.vo.OperationsOrderLifecycleVo;
import com.bocoo.dealer.operations.domain.vo.OperationsSummaryVo;
import com.bocoo.dealer.operations.mapper.PlatformOperationsOverviewMapper;
import com.bocoo.dealer.operations.service.PlatformOperationsOverviewService;
import com.bocoo.dealer.scope.PlatformSalesGuard;
import com.bocoo.system.domain.entity.MerchantProfile;
import com.bocoo.system.domain.entity.SysTenantApply;
import com.bocoo.system.mapper.MerchantProfileMapper;
import com.bocoo.system.mapper.SysTenantApplyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class PlatformOperationsOverviewServiceImpl implements PlatformOperationsOverviewService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ENABLED = "1";
    private static final String STATUS_DISABLED = "0";
    private static final String LEVEL_VIP = "VIP";

    private final MerchantProfileMapper merchantProfileMapper;
    private final SysTenantApplyMapper tenantApplyMapper;
    private final PlatformOperationsOverviewMapper operationsMapper;
    private final PlatformSalesGuard guard;

    @Override
    public OperationsSummaryVo summary() {
        guard.requirePlatform();
        return ignoreTenant(() -> {
            LocalDateTime dataAsOf = TimeUtils.utcNow();
            OperationsSummaryVo summary = new OperationsSummaryVo();
            LocalDateTime start = dataAsOf.minusDays(30);
            summary.setPendingApplicationCount(tenantApplyMapper.selectCount(new QueryWrapper<SysTenantApply>().eq("status", STATUS_PENDING)));
            summary.setEnabledMerchantCount(merchantProfileMapper.selectCount(new QueryWrapper<MerchantProfile>().eq("status", STATUS_ENABLED)));
            summary.setDisabledMerchantCount(merchantProfileMapper.selectCount(new QueryWrapper<MerchantProfile>().eq("status", STATUS_DISABLED)));
            summary.setVipMerchantCount(merchantProfileMapper.selectCount(new QueryWrapper<MerchantProfile>().eq("level_code", LEVEL_VIP)));
            summary.setOrderLifecycle(defaultLifecycle(operationsMapper.selectOrderLifecycle(start)));
            summary.setCurrencyAmounts(currencyAmounts(operationsMapper.selectCurrencyAmounts(start)));
            summary.setDataAsOf(dataAsOf);
            return summary;
        });
    }

    @Override
    public TableDataInfo<OperationsMerchantVo> merchantPage(OperationsMerchantQueryBo query, PageQuery pageQuery) {
        guard.requirePlatform();
        return ignoreTenant(() -> {
            IPage<MerchantProfile> page = merchantProfileMapper.selectPage(buildPage(pageQuery), profileQuery(query));
            Map<Long, Long> orderCounts = orderCounts(page.getRecords());
            List<OperationsMerchantVo> rows = page.getRecords().stream()
                .map(profile -> toMerchantVo(profile, orderCounts.getOrDefault(profile.getTenantId(), 0L)))
                .toList();
            return new TableDataInfo<>(rows, page.getTotal());
        });
    }

    @Override
    public List<OperationsMerchantLevelOptionVo> merchantLevelOptions(String status) {
        guard.requirePlatform();
        return ignoreTenant(() -> operationsMapper.selectMerchantLevelOptions(status));
    }

    @Override
    public TableDataInfo<OperationsMerchantVo> pendingMerchantApplications(OperationsMerchantQueryBo query, PageQuery pageQuery) {
        guard.requirePlatform();
        return ignoreTenant(() -> {
            Page<OperationsMerchantVo> page = operationsMapper.selectPendingApplications(buildPage(pageQuery), query);
            return new TableDataInfo<>(page.getRecords(), page.getTotal());
        });
    }

    private QueryWrapper<MerchantProfile> profileQuery(OperationsMerchantQueryBo query) {
        QueryWrapper<MerchantProfile> wrapper = new QueryWrapper<MerchantProfile>().orderByDesc("audit_time").orderByDesc("create_time");
        if (query == null) return wrapper;
        if (StringUtils.isNotBlank(query.getStatus())) wrapper.eq("status", query.getStatus());
        if (StringUtils.isNotBlank(query.getLevelCode())) wrapper.eq("level_code", query.getLevelCode());
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.and(item -> item.like("merchant_name", query.getKeyword()).or()
                .like("company_name", query.getKeyword()).or().like("contact_name", query.getKeyword()).or()
                .like("primary_email", query.getKeyword()));
        }
        return wrapper;
    }

    private Map<Long, Long> orderCounts(List<MerchantProfile> profiles) {
        List<Long> tenantIds = profiles.stream().map(MerchantProfile::getTenantId).filter(Objects::nonNull).toList();
        if (tenantIds.isEmpty()) return Map.of();
        return operationsMapper.selectMerchantOrderCounts(tenantIds).stream()
            .filter(row -> row.getTenantId() != null)
            .collect(java.util.stream.Collectors.toMap(OperationsMerchantVo::getTenantId,
                OperationsMerchantVo::getOrderCount, (left, right) -> right, LinkedHashMap::new));
    }

    private OperationsOrderLifecycleVo defaultLifecycle(OperationsOrderLifecycleVo lifecycle) {
        return lifecycle == null ? new OperationsOrderLifecycleVo() : lifecycle;
    }

    private Map<String, BigDecimal> currencyAmounts(List<OperationsCurrencyAmountVo> rows) {
        Map<String, BigDecimal> amounts = new LinkedHashMap<>();
        if (rows == null) return amounts;
        rows.forEach(row -> amounts.put(row.getCurrencyCode(),
            row.getAmount() == null ? BigDecimal.ZERO : row.getAmount()));
        return amounts;
    }

    private OperationsMerchantVo toMerchantVo(MerchantProfile profile, long orderCount) {
        OperationsMerchantVo row = new OperationsMerchantVo();
        row.setSource("profile");
        row.setMerchantId(profile.getMerchantId());
        row.setTenantId(profile.getTenantId());
        row.setMerchantName(profile.getMerchantName());
        row.setCompanyName(profile.getCompanyName());
        row.setContactName(profile.getContactName());
        row.setPrimaryEmail(profile.getPrimaryEmail());
        row.setOfficePhone(profile.getOfficePhone());
        row.setMobilePhone(profile.getMobilePhone());
        row.setCountry(profile.getCountry());
        row.setState(profile.getState());
        row.setCity(profile.getCity());
        row.setAddressLine1(profile.getAddressLine1());
        row.setAddressLine2(profile.getAddressLine2());
        row.setPostalCode(profile.getPostalCode());
        row.setStatus(profile.getStatus());
        row.setAuditStatus(profile.getAuditStatus());
        row.setAuditBy(profile.getAuditBy());
        row.setAuditTime(profile.getAuditTime());
        row.setLevelName(profile.getLevelName());
        row.setLevelCode(profile.getLevelCode());
        row.setDiscountRate(profile.getDiscountRate());
        row.setCreditLimit(profile.getCreditLimit());
        row.setOrderCount(orderCount);
        return row;
    }

    private <T> T ignoreTenant(Supplier<T> action) {
        return TenantContextHolder.callWithIgnore(action);
    }

    private <T> Page<T> buildPage(PageQuery pageQuery) {
        return (pageQuery == null ? new PageQuery() : pageQuery).build();
    }
}
