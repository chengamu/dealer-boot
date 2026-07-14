package com.bocoo.dealer.payment;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.domain.vo.RoleVO;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.mapper.SalesDocumentQueryMapper;
import com.bocoo.dealer.service.TestSaTokenContext;
import com.bocoo.pay.api.PaymentDocumentFilter;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesPaymentDocumentScopeResolverTest {
    @Mock private SalesDocumentQueryMapper mapper;
    private SalesPaymentDocumentScopeResolver resolver;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SalesDocument.class);
        TestSaTokenContext.install();
        resolver = new SalesPaymentDocumentScopeResolver(mapper);
    }

    @Test
    void merchantListKeepsCurrentBusinessScopeAndDataScopeMapper() {
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 7L, "seller");
        SalesDocument document = document(10L, 300001L);
        when(mapper.selectList(any())).thenAnswer(invocation -> {
            assertThat(TenantContextHolder.isIgnore()).isFalse();
            return List.of(document);
        });

        assertThat(resolver.accessibleDocumentIds(null)).containsExactly(10L);

        ArgumentCaptor<QueryWrapper<SalesDocument>> query = queryCaptor();
        verify(mapper).selectList(query.capture());
        assertThat(query.getValue().getSqlSegment()).contains("tenant_id", "business_origin");
        assertThat(query.getValue().getParamNameValuePairs().values())
            .contains(300001L, "MERCHANT");
    }

    @Test
    void platformFinanceCanQueryAcrossTenantsWithSubjectFilter() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 8L, 12L,
            "finance", List.of(role("platform_finance")));
        when(mapper.selectList(any())).thenAnswer(invocation -> {
            assertThat(TenantContextHolder.isIgnore()).isTrue();
            return List.of(document(11L, 300002L));
        });

        PaymentDocumentFilter filter = PaymentDocumentFilter.builder()
            .businessOrigin("INTERNAL").subjectId(31L).build();
        assertThat(resolver.accessibleDocumentIds(filter)).containsExactly(11L);

        ArgumentCaptor<QueryWrapper<SalesDocument>> query = queryCaptor();
        verify(mapper).selectList(query.capture());
        assertThat(query.getValue().getSqlSegment()).contains("business_origin", "sales_store_id");
        assertThat(query.getValue().getParamNameValuePairs().values()).contains("INTERNAL", 31L);
    }

    @Test
    void platformBusinessUserRemainsInCurrentInternalScope() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 9L, "internal-sales");
        when(mapper.selectList(any())).thenAnswer(invocation -> {
            assertThat(TenantContextHolder.isIgnore()).isFalse();
            return List.of();
        });

        assertThat(resolver.accessibleDocumentIds(null)).isEmpty();

        ArgumentCaptor<QueryWrapper<SalesDocument>> query = queryCaptor();
        verify(mapper).selectList(query.capture());
        assertThat(query.getValue().getSqlSegment()).contains("tenant_id", "business_origin");
        assertThat(query.getValue().getParamNameValuePairs().values()).contains(1L, "INTERNAL");
    }

    @Test
    void inaccessibleDocumentIsRejected() {
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 7L, "seller");
        when(mapper.selectOne(any(), eq(false))).thenReturn(null);

        assertThatThrownBy(() -> resolver.requireAccessible(99L))
            .isInstanceOf(ServiceException.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ArgumentCaptor<QueryWrapper<SalesDocument>> queryCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(QueryWrapper.class);
    }

    private RoleVO role(String key) {
        RoleVO role = new RoleVO();
        role.setRoleKey(key);
        return role;
    }

    private SalesDocument document(Long id, Long tenantId) {
        SalesDocument row = new SalesDocument();
        row.setSalesDocumentId(id);
        row.setTenantId(tenantId);
        row.setBusinessOrigin("MERCHANT");
        row.setDelFlag("0");
        return row;
    }
}
