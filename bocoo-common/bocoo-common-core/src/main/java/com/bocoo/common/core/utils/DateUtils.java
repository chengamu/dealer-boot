package com.bocoo.common.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 时间工具类
 *
 * Legacy/local-time helper. Methods that format, parse, or convert through
 * Date/SimpleDateFormat/DateFormatUtils/ZoneId.systemDefault() depend on the
 * JVM default time zone and must not be used for persisted business/API time.
 * Use {@link TimeUtils} for explicit UTC semantics.
 *
 * @author ruoyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    public static final String YYYY = "yyyy";

    public static final String YYYY_MM = "yyyy-MM";

    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static final String[] PARSE_PATTERNS = {
        "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
        "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
        "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * 获取当前Date型日期
     *
     * Legacy/local helper. Do not use for persisted business/API time.
     *
     * @return Date() 当前日期
     */
    @Deprecated(since = "2026-05-25")
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * Legacy/local helper. Uses the JVM default time zone.
     *
     * @return String
     */
    @Deprecated(since = "2026-05-25")
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    /**
     * Legacy/local helper. Uses the JVM default time zone.
     */
    @Deprecated(since = "2026-05-25")
    public static String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * Legacy/local helper. Uses the JVM default time zone.
     */
    @Deprecated(since = "2026-05-25")
    public static String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    /**
     * Legacy/local helper. Uses the JVM default time zone.
     */
    @Deprecated(since = "2026-05-25")
    public static String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    /**
     * Legacy/local helper. Uses the JVM default time zone.
     */
    @Deprecated(since = "2026-05-25")
    public static String dateTime(final Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    /**
     * Legacy/local helper. Uses the JVM default time zone.
     */
    @Deprecated(since = "2026-05-25")
    public static String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * Legacy/local helper. Uses the JVM default time zone.
     */
    @Deprecated(since = "2026-05-25")
    public static Date dateTime(final String format, final String ts) {
        try {
            return new SimpleDateFormat(format).parse(ts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     *
     * Upload/OSS file-path helper. Uses UTC day explicitly.
     */
    public static String datePath() {
        return TimeUtils.formatUtc(TimeUtils.utcNow(), DATE_PATH_FORMATTER);
    }

    /**
     * 日期路径 即年/月/日 如20180808
     *
     * Legacy/local helper. Uses the JVM default time zone.
     */
    @Deprecated(since = "2026-05-25")
    public static String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式
     *
     * Legacy/local helper. Prefer explicit UTC parsing in {@link TimeUtils}
     * for business/API time.
     */
    @Deprecated(since = "2026-05-25")
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), PARSE_PATTERNS);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算相差天数
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     * 计算两个时间差
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 增加 LocalDateTime ==> Date
     *
     * Legacy/local helper. Uses the JVM default time zone.
     */
    @Deprecated(since = "2026-05-25")
    public static Date toDate(LocalDateTime temporalAccessor) {
        ZonedDateTime zdt = temporalAccessor.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    /**
     * 增加 LocalDate ==> Date
     *
     * Legacy/local helper. Uses the JVM default time zone.
     */
    @Deprecated(since = "2026-05-25")
    public static Date toDate(LocalDate temporalAccessor) {
        LocalDateTime localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0));
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }
}
