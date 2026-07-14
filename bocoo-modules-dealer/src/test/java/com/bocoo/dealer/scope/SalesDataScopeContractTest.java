package com.bocoo.dealer.scope;

import com.bocoo.common.mybatis.annotation.DataPermission;
import com.bocoo.common.mybatis.enums.DataScopeType;
import com.bocoo.dealer.mapper.SalesDocumentQueryMapper;
import com.bocoo.dealer.quickorder.mapper.QuickOrderQueryMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SalesDataScopeContractTest {
    @ParameterizedTest
    @EnumSource(DataScopeType.class)
    void allFiveDataScopesHaveRequiredSalesColumn(DataScopeType type) {
        assertScopeSupported(QuickOrderQueryMapper.class, type);
        assertScopeSupported(SalesDocumentQueryMapper.class, type);
    }

    private void assertScopeSupported(Class<?> mapperType, DataScopeType type) {
        DataPermission permission = mapperType.getAnnotation(DataPermission.class);
        Set<String> keys = Arrays.stream(permission.value())
            .flatMap(column -> Arrays.stream(column.key())).collect(Collectors.toSet());
        Set<String> columns = Arrays.stream(permission.value())
            .flatMap(column -> Arrays.stream(column.value())).collect(Collectors.toSet());
        if (type == DataScopeType.ALL) {
            assertThat(type.getSqlTemplate()).isBlank();
        } else if (type == DataScopeType.SELF) {
            assertThat(keys).contains("userName");
            assertThat(columns).contains("owner_user_id");
            assertThat(type.getSqlTemplate()).contains("#userName");
        } else {
            assertThat(keys).contains("deptName");
            assertThat(columns).contains("dept_id");
            assertThat(type.getSqlTemplate()).contains("#deptName");
        }
    }
}
