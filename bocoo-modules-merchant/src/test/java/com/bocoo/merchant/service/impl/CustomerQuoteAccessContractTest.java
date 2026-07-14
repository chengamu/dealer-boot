package com.bocoo.merchant.service.impl;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.mybatis.annotation.DataPermission;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.merchant.controller.PlatformCustomerQuoteController;
import com.bocoo.merchant.domain.bo.CustomerQuoteBo;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.mapper.CustomerProfileMapper;
import com.bocoo.merchant.mapper.CustomerQuoteItemMapper;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import com.bocoo.merchant.service.TestSaTokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerQuoteAccessContractTest {

    @Mock
    private CustomerQuoteMapper quoteMapper;
    @Mock
    private CustomerQuoteItemMapper itemMapper;
    @Mock
    private CustomerQuoteQuerySupport querySupport;
    @Mock
    private CustomerQuoteLoader loader;
    @Mock
    private CustomerQuoteExportAssembler exportAssembler;
    @Mock
    private CustomerQuotePdfRenderer pdfRenderer;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
    }

    @Test
    void businessQueryAlwaysAddsTenantAndOrigin() {
        CustomerQuoteQuerySupport support = new CustomerQuoteQuerySupport(quoteMapper, itemMapper);

        QueryWrapper<CustomerQuote> query = support.buildBusinessQuery(null, 300L, "MERCHANT");

        assertThat(query.getSqlSegment()).contains("tenant_id", "business_origin");
        assertThat(query.getParamNameValuePairs().values()).contains(300L, "MERCHANT");
    }

    @Test
    void profileAndQuoteMappersUseDepartmentAndOwnerDataScope() {
        assertDataScope(CustomerProfileMapper.class);
        assertDataScope(CustomerQuoteMapper.class);
    }

    @Test
    void merchantCannotUsePlatformQuoteService() {
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300L, 9L, "merchant-sales", 31L);
        PlatformCustomerQuoteQueryServiceImpl service = platformService();

        assertThatThrownBy(() -> service.queryById(1L)).isInstanceOf(ServiceException.class);
        verify(loader, never()).loadPlatform(any());
    }

    @Test
    void platformControllerUsesDedicatedPermissionFamily() throws Exception {
        assertPermission("list", "platform:sales:quote:list", CustomerQuoteBo.class, PageQuery.class);
        assertPermission("get", "platform:sales:quote:query", Long.class);
        assertPermission("pdf", "platform:sales:quote:document", Long.class, jakarta.servlet.http.HttpServletResponse.class);
        assertPermission("export", "platform:sales:quote:export", Long.class, jakarta.servlet.http.HttpServletResponse.class);
    }

    private void assertDataScope(Class<?> mapperType) {
        DataPermission permission = mapperType.getAnnotation(DataPermission.class);
        assertThat(permission).isNotNull();
        assertThat(Arrays.stream(permission.value()).flatMap(column -> Arrays.stream(column.value())))
            .containsExactlyInAnyOrder("dept_id", "owner_user_id");
    }

    private void assertPermission(String methodName, String expected, Class<?>... parameterTypes) throws Exception {
        Method method = PlatformCustomerQuoteController.class.getMethod(methodName, parameterTypes);
        assertThat(method.getAnnotation(SaCheckPermission.class).value()).containsExactly(expected);
    }

    private PlatformCustomerQuoteQueryServiceImpl platformService() {
        return new PlatformCustomerQuoteQueryServiceImpl(
            querySupport, loader, exportAssembler, pdfRenderer);
    }
}
