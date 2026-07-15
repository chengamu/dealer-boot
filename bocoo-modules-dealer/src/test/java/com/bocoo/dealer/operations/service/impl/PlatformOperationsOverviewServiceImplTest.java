package com.bocoo.dealer.operations.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.operations.domain.bo.OperationsMerchantQueryBo;
import com.bocoo.dealer.operations.domain.vo.OperationsCurrencyAmountVo;
import com.bocoo.dealer.operations.domain.vo.OperationsMerchantLevelOptionVo;
import com.bocoo.dealer.operations.domain.vo.OperationsMerchantVo;
import com.bocoo.dealer.operations.domain.vo.OperationsOrderLifecycleVo;
import com.bocoo.dealer.operations.domain.vo.OperationsSummaryVo;
import com.bocoo.dealer.operations.mapper.PlatformOperationsOverviewMapper;
import com.bocoo.dealer.scope.PlatformSalesGuard;
import com.bocoo.dealer.service.TestSaTokenContext;
import com.bocoo.system.domain.entity.MerchantProfile;
import com.bocoo.system.mapper.MerchantProfileMapper;
import com.bocoo.system.mapper.SysTenantApplyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformOperationsOverviewServiceImplTest {

    @Mock
    private MerchantProfileMapper merchantProfileMapper;
    @Mock
    private SysTenantApplyMapper tenantApplyMapper;
    @Mock
    private PlatformOperationsOverviewMapper operationsMapper;

    private PlatformOperationsOverviewServiceImpl service;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
        service = new PlatformOperationsOverviewServiceImpl(
            merchantProfileMapper,
            tenantApplyMapper,
            operationsMapper,
            new PlatformSalesGuard()
        );
    }

    @Test
    void merchantCannotEnterAnyPlatformOperationsEndpoint() {
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 7L, "seller");

        assertThatThrownBy(() -> service.summary()).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> service.merchantPage(null, null)).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> service.merchantLevelOptions("ENABLED")).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> service.pendingMerchantApplications(null, null)).isInstanceOf(ServiceException.class);

        verifyNoInteractions(merchantProfileMapper, tenantApplyMapper, operationsMapper);
    }

    @Test
    void summaryUsesDatabaseAggregatesForLifecycleAndCurrencyAmounts() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 8L, "operator");
        when(tenantApplyMapper.selectCount(any())).thenReturn(3L);
        when(merchantProfileMapper.selectCount(any())).thenReturn(12L, 4L, 2L);
        when(operationsMapper.selectOrderLifecycle(any())).thenReturn(lifecycle(9, 4, 3, 2, 1));
        when(operationsMapper.selectCurrencyAmounts(any())).thenReturn(List.of(
            currency("USD", new BigDecimal("123.45")),
            currency("CNY", new BigDecimal("66.00"))
        ));

        OperationsSummaryVo summary = service.summary();

        assertThat(summary.getPendingApplicationCount()).isEqualTo(3L);
        assertThat(summary.getEnabledMerchantCount()).isEqualTo(12L);
        assertThat(summary.getDisabledMerchantCount()).isEqualTo(4L);
        assertThat(summary.getVipMerchantCount()).isEqualTo(2L);
        assertThat(summary.getOrderLifecycle().getSubmittedCount()).isEqualTo(9L);
        assertThat(summary.getOrderLifecycle().getUnpaidCount()).isEqualTo(4L);
        assertThat(summary.getOrderLifecycle().getProductionCount()).isEqualTo(3L);
        assertThat(summary.getOrderLifecycle().getShippedCount()).isEqualTo(2L);
        assertThat(summary.getOrderLifecycle().getCompletedCount()).isEqualTo(1L);
        assertThat(summary.getCurrencyAmounts()).containsEntry("USD", new BigDecimal("123.45"))
            .containsEntry("CNY", new BigDecimal("66.00"));
        assertThat(summary.getDataAsOf()).isNotNull();

        verify(operationsMapper).selectOrderLifecycle(any());
        verify(operationsMapper).selectCurrencyAmounts(any());
    }

    @Test
    void merchantPageUsesDatabaseOrderCounts() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 8L, "operator");
        Page<MerchantProfile> page = new Page<>(1, 10);
        page.setRecords(List.of(profile(11L, 201L, "Alpha"), profile(12L, 202L, "Beta")));
        page.setTotal(2L);
        when(merchantProfileMapper.selectPage(any(), any())).thenReturn(page);
        when(operationsMapper.selectMerchantOrderCounts(eq(List.of(201L, 202L)))).thenReturn(List.of(
            orderCount(201L, 5L)
        ));

        TableDataInfo<OperationsMerchantVo> result = service.merchantPage(new OperationsMerchantQueryBo(), new PageQuery());

        assertThat(result.getTotal()).isEqualTo(2L);
        assertThat(result.getRows()).extracting(OperationsMerchantVo::getOrderCount).containsExactly(5L, 0L);
        verify(operationsMapper).selectMerchantOrderCounts(eq(List.of(201L, 202L)));
    }

    @Test
    void levelOptionsUseOperationsReadOnlyContract() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 8L, "operator");
        OperationsMerchantLevelOptionVo option = new OperationsMerchantLevelOptionVo();
        option.setLevelId(9L);
        option.setLevelCode("VIP");
        option.setLevelName("VIP");
        when(operationsMapper.selectMerchantLevelOptions("ENABLED")).thenReturn(List.of(option));

        assertThat(service.merchantLevelOptions("ENABLED")).containsExactly(option);
    }

    @Test
    void pendingApplicationsReturnOperationsMerchantRows() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 8L, "operator");
        OperationsMerchantVo row = new OperationsMerchantVo();
        row.setSource("application");
        row.setApplicationId(101L);
        row.setMerchantName("Acme");
        Page<OperationsMerchantVo> page = new Page<>(1, 10);
        page.setRecords(List.of(row));
        page.setTotal(1L);
        when(operationsMapper.selectPendingApplications(any(), any())).thenReturn(page);

        TableDataInfo<OperationsMerchantVo> result = service.pendingMerchantApplications(new OperationsMerchantQueryBo(), new PageQuery());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRows()).containsExactly(row);
    }

    private MerchantProfile profile(Long merchantId, Long tenantId, String merchantName) {
        MerchantProfile row = new MerchantProfile();
        row.setMerchantId(merchantId);
        row.setTenantId(tenantId);
        row.setMerchantName(merchantName);
        return row;
    }

    private OperationsOrderLifecycleVo lifecycle(long submitted, long unpaid, long production, long shipped, long completed) {
        OperationsOrderLifecycleVo row = new OperationsOrderLifecycleVo();
        row.setSubmittedCount(submitted);
        row.setUnpaidCount(unpaid);
        row.setProductionCount(production);
        row.setShippedCount(shipped);
        row.setCompletedCount(completed);
        return row;
    }

    private OperationsCurrencyAmountVo currency(String currencyCode, BigDecimal amount) {
        OperationsCurrencyAmountVo row = new OperationsCurrencyAmountVo();
        row.setCurrencyCode(currencyCode);
        row.setAmount(amount);
        return row;
    }

    private OperationsMerchantVo orderCount(Long tenantId, long orderCount) {
        OperationsMerchantVo row = new OperationsMerchantVo();
        row.setTenantId(tenantId);
        row.setOrderCount(orderCount);
        return row;
    }
}
