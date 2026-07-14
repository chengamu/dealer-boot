package com.bocoo.dealer.dashboard.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SalesDashboardPeriod(
    LocalDateTime dataAsOf,
    LocalDate periodStart,
    LocalDate periodEnd,
    LocalDateTime monthStartUtc,
    LocalDateTime nextMonthStartUtc
) {
}
