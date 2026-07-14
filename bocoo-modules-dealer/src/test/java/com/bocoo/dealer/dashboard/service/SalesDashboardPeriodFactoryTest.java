package com.bocoo.dealer.dashboard.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class SalesDashboardPeriodFactoryTest {
    @Test
    void businessMonthBoundariesAreConvertedToUtc() {
        SalesDashboardPeriod period = SalesDashboardPeriodFactory.create(
            LocalDateTime.of(2026, 7, 13, 4, 30), ZoneId.of("Asia/Shanghai"));

        assertThat(period.periodStart()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(period.periodEnd()).isEqualTo(LocalDate.of(2026, 7, 13));
        assertThat(period.monthStartUtc()).isEqualTo(LocalDateTime.of(2026, 6, 30, 16, 0));
        assertThat(period.nextMonthStartUtc()).isEqualTo(LocalDateTime.of(2026, 7, 31, 16, 0));
    }
}
