package com.bocoo.system.service;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.extra.spring.SpringUtil;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.enums.UserType;
import com.bocoo.common.core.exception.user.UserException;
import com.bocoo.common.core.service.I18nService;
import com.bocoo.common.log.event.LogininforEvent;
import com.bocoo.common.redis.utils.RedisUtils;
import com.bocoo.common.web.config.properties.CaptchaProperties;
import com.bocoo.system.domain.entity.SysTenant;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.domain.vo.SysUserVo;
import com.bocoo.system.mapper.SysTenantApplyMapper;
import com.bocoo.system.mapper.SysTenantMapper;
import com.bocoo.system.mapper.SysUserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RBucketAsync;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SysLoginServiceThirdLoginTest {

    private static final Long PLATFORM_TENANT_ID = 1L;
    private static final Long MERCHANT_TENANT_ID = 2001L;
    private static final Long USER_ID = 3001L;
    private static final String USERNAME = "merchant-owner";

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private CaptchaProperties captchaProperties;
    @Mock
    private SysPermissionService permissionService;
    @Mock
    private SysRoleService roleService;
    @Mock
    private SysDeptService deptService;
    @Mock
    private SysTenantMapper tenantMapper;
    @Mock
    private SysTenantApplyMapper tenantApplyMapper;
    @Mock
    private MerchantProfileService merchantProfileService;
    @Mock
    private GoogleAuthService googleAuthService;

    private final ApplicationContext applicationContext = mock(ApplicationContext.class);
    private final I18nService i18nService = mock(I18nService.class);
    private final RedissonClient redissonClient = mock(RedissonClient.class);
    private final RBucket<Object> bucket = mock(RBucket.class);
    private final RBatch batch = mock(RBatch.class);
    private final RBucketAsync<Object> asyncBucket = mock(RBucketAsync.class);
    private SysLoginService loginService;

    @BeforeAll
    void initializeRedisUtils() {
        when(applicationContext.getBean(RedissonClient.class)).thenReturn(redissonClient);
        when(applicationContext.getBean(I18nService.class)).thenReturn(i18nService);
        new SpringUtil().setApplicationContext(applicationContext);
        assertThat(RedisUtils.getClient()).isSameAs(redissonClient);
    }

    @BeforeEach
    void setUp() {
        reset(applicationContext, redissonClient, bucket, batch, asyncBucket);
        stubInfrastructure();
        installSaTokenContext();
        TenantContextHolder.setTenantId(PLATFORM_TENANT_ID);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        loginService = new SysLoginService(
            userMapper,
            captchaProperties,
            permissionService,
            roleService,
            deptService,
            tenantMapper,
            tenantApplyMapper,
            merchantProfileService,
            googleAuthService
        );
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void thirdLoginRunsCompleteLoginInMerchantTenantContext() {
        stubUserLookup(merchantUser());
        stubLoginUserBuild();
        when(userMapper.updateById(any(SysUser.class))).thenAnswer(invocation -> {
            assertMerchantContext();
            return 1;
        });

        String token = loginService.thirdLogin(USERNAME, "password");

        assertThat(token).isNotBlank();
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(PLATFORM_TENANT_ID);
        verify(applicationContext).publishEvent(any(LogininforEvent.class));
        verify(userMapper).updateById(any(SysUser.class));
    }

    @Test
    void thirdLoginKeepsUserNotExistsBehavior() {
        stubUserLookup(null);

        assertThatThrownBy(() -> loginService.thirdLogin("missing-user", "password"))
            .isInstanceOf(UserException.class);

        assertThat(TenantContextHolder.getTenantId()).isEqualTo(PLATFORM_TENANT_ID);
        verifyNoInteractions(permissionService, roleService, tenantMapper, merchantProfileService);
        verify(userMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    void thirdLoginKeepsPasswordErrorInMerchantTenantContext() {
        stubUserLookup(merchantUser());

        assertThatThrownBy(() -> loginService.thirdLogin(USERNAME, "wrong-password"))
            .isInstanceOf(UserException.class);

        assertThat(TenantContextHolder.getTenantId()).isEqualTo(PLATFORM_TENANT_ID);
        verify(applicationContext).publishEvent(any(LogininforEvent.class));
        verifyNoInteractions(permissionService, roleService, tenantMapper, merchantProfileService);
        verify(userMapper, never()).updateById(any(SysUser.class));
    }

    private void stubInfrastructure() {
        when(applicationContext.getBean(RedissonClient.class)).thenReturn(redissonClient);
        when(applicationContext.getBean(I18nService.class)).thenReturn(i18nService);
        doAnswer(invocation -> {
            assertMerchantContext();
            return null;
        }).when(applicationContext).publishEvent(any(LogininforEvent.class));
        when(redissonClient.getBucket(anyString())).thenAnswer(invocation -> {
            assertMerchantContext();
            return bucket;
        });
        when(bucket.get()).thenReturn(null);
        when(bucket.delete()).thenReturn(true);
        when(redissonClient.createBatch()).thenAnswer(invocation -> {
            assertMerchantContext();
            return batch;
        });
        when(batch.getBucket(anyString())).thenReturn(asyncBucket);
    }

    private void installSaTokenContext() {
        Map<String, Object> values = new HashMap<>();
        SaStorage storage = mock(SaStorage.class);
        lenient().when(storage.getSource()).thenReturn(values);
        lenient().when(storage.get(anyString())).thenAnswer(invocation -> {
            assertMerchantContext();
            return values.get(invocation.getArgument(0));
        });
        lenient().when(storage.set(anyString(), any())).thenAnswer(invocation -> {
            assertMerchantContext();
            values.put(invocation.getArgument(0), invocation.getArgument(1));
            return storage;
        });
        lenient().when(storage.delete(anyString())).thenAnswer(invocation -> {
            assertMerchantContext();
            values.remove(invocation.getArgument(0));
            return storage;
        });
        SaTokenContext context = mock(SaTokenContext.class);
        lenient().when(context.getStorage()).thenReturn(storage);
        lenient().when(context.getRequest()).thenReturn(mock(SaRequest.class));
        lenient().when(context.getResponse()).thenReturn(mock(SaResponse.class));
        lenient().when(context.isValid()).thenReturn(true);
        SaManager.setConfig(new SaTokenConfig());
        SaManager.setSaTokenDao(new SaTokenDaoDefaultImpl());
        SaManager.setSaTokenContext(context);
    }

    private void stubUserLookup(SysUserVo user) {
        when(userMapper.selectVoList(any())).thenAnswer(invocation -> {
            assertThat(TenantContextHolder.isIgnore()).isTrue();
            assertThat(TenantContextHolder.getTenantId()).isNull();
            return user == null ? List.of() : List.of(user);
        });
    }

    private void stubLoginUserBuild() {
        SysTenant tenant = new SysTenant();
        tenant.setTenantId(MERCHANT_TENANT_ID);
        tenant.setTenantType(TenantType.MERCHANT.getCode());
        when(tenantMapper.selectById(MERCHANT_TENANT_ID)).thenAnswer(invocation -> {
            assertMerchantContext();
            return tenant;
        });
        when(merchantProfileService.selectByTenantId(MERCHANT_TENANT_ID)).thenAnswer(invocation -> {
            assertMerchantContext();
            return null;
        });
        when(permissionService.getMenuPermission(USER_ID)).thenAnswer(invocation -> {
            assertMerchantContext();
            return Set.of("system:user:list");
        });
        when(permissionService.getRolePermission(USER_ID)).thenAnswer(invocation -> {
            assertMerchantContext();
            return Set.of("merchant-owner");
        });
        when(roleService.selectRolesByUserId(USER_ID)).thenAnswer(invocation -> {
            assertMerchantContext();
            return List.of();
        });
    }

    private SysUserVo merchantUser() {
        SysUserVo user = new SysUserVo();
        user.setUserId(USER_ID);
        user.setTenantId(MERCHANT_TENANT_ID);
        user.setUserName(USERNAME);
        user.setUserType(UserType.SYS_USER.getUserType());
        user.setPassword(BCrypt.hashpw("password"));
        return user;
    }

    private static void assertMerchantContext() {
        assertThat(TenantContextHolder.isIgnore()).isFalse();
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(MERCHANT_TENANT_ID);
    }
}
