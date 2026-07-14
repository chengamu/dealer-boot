package com.bocoo.dealer.dashboard.service;

import com.bocoo.common.core.utils.TimeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class SalesDashboardPeriodFactory {
    private final ZoneId businessZone;

    public SalesDashboardPeriodFactory(@Value("${bocoo.business-time-zone:Asia/Shanghai}") String businessZone) {
        this.businessZone = ZoneId.of(businessZone);
    }

    public SalesDashboardPeriod current() {
        return create(TimeUtils.utcNow(), businessZone);
    }

    static SalesDashboardPeriod create(LocalDateTime nowUtc, ZoneId businessZone) {
        LocalDate businessToday = nowUtc.atZone(TimeUtils.UTC).withZoneSameInstant(businessZone).toLocalDate();
        LocalDate monthStart = businessToday.withDayOfMonth(1);
        return new SalesDashboardPeriod(nowUtc, monthStart, businessToday,
            toUtc(monthStart, businessZone), toUtc(monthStart.plusMonths(1), businessZone));
    }

    private static LocalDateTime toUtc(LocalDate date, ZoneId businessZone) {
        return date.atStartOfDay(businessZone).withZoneSameInstant(TimeUtils.UTC).toLocalDateTime();
    }
}
