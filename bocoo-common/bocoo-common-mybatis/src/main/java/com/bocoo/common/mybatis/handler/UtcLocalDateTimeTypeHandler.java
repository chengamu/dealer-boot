package com.bocoo.common.mybatis.handler;

import com.bocoo.common.mybatis.helper.JdbcUtcDateTimeHelper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@MappedTypes(LocalDateTime.class)
@MappedJdbcTypes(value = {JdbcType.TIMESTAMP, JdbcType.TIMESTAMP_WITH_TIMEZONE}, includeNullJdbcType = true)
public class UtcLocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType)
        throws SQLException {
        JdbcUtcDateTimeHelper.setUtcLocalDateTime(ps, i, parameter);
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return JdbcUtcDateTimeHelper.getUtcLocalDateTime(rs, columnName);
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return JdbcUtcDateTimeHelper.getUtcLocalDateTime(rs, columnIndex);
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return JdbcUtcDateTimeHelper.getUtcLocalDateTime(cs, columnIndex);
    }
}
