package com.bocoo.common.mybatis.helper;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * JDBC datetime conversion helpers for UTC-based LocalDateTime fields.
 */
public final class JdbcUtcDateTimeHelper {

    private JdbcUtcDateTimeHelper() {
    }

    public static void setUtcLocalDateTime(PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
        ps.setObject(index, value.atOffset(ZoneOffset.UTC));
    }

    public static LocalDateTime getUtcLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        try {
            return toUtcLocalDateTime(rs.getObject(columnName, OffsetDateTime.class));
        } catch (SQLException ex) {
            return toUtcLocalDateTime(rs.getObject(columnName));
        }
    }

    public static LocalDateTime getUtcLocalDateTime(ResultSet rs, int columnIndex) throws SQLException {
        try {
            return toUtcLocalDateTime(rs.getObject(columnIndex, OffsetDateTime.class));
        } catch (SQLException ex) {
            return toUtcLocalDateTime(rs.getObject(columnIndex));
        }
    }

    public static LocalDateTime getUtcLocalDateTime(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            return toUtcLocalDateTime(cs.getObject(columnIndex, OffsetDateTime.class));
        } catch (SQLException ex) {
            return toUtcLocalDateTime(cs.getObject(columnIndex));
        }
    }

    public static LocalDateTime toUtcLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        throw new IllegalArgumentException("Unsupported LocalDateTime value type: " + value.getClass());
    }
}
