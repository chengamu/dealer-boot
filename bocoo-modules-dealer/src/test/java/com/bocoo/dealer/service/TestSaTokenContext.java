package com.bocoo.dealer.service;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.context.model.SaStorage;
import com.bocoo.common.core.domain.bo.LoginUser;
import com.bocoo.common.core.domain.vo.RoleVO;
import com.bocoo.common.satoken.utils.LoginHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public final class TestSaTokenContext implements SaTokenContext {
    private static final TestSaTokenContext INSTANCE = new TestSaTokenContext();
    private final ThreadLocal<TestSaStorage> storage = ThreadLocal.withInitial(TestSaStorage::new);

    private TestSaTokenContext() {
    }

    public static void install() {
        SaManager.setSaTokenContext(INSTANCE);
        INSTANCE.storage.get().clear();
    }

    public static void setLoginUser(String tenantType, Long tenantId, Long userId, String username) {
        setLoginUser(tenantType, tenantId, userId, null, username, List.of());
    }

    public static void setLoginUser(String tenantType, Long tenantId, Long userId, Long deptId,
                                    String username, List<RoleVO> roles) {
        LoginUser loginUser = new LoginUser();
        loginUser.setTenantType(tenantType);
        loginUser.setTenantId(tenantId);
        loginUser.setUserId(userId);
        loginUser.setDeptId(deptId);
        loginUser.setUsername(username);
        loginUser.setRoles(roles);
        INSTANCE.storage.get().set(LoginHelper.LOGIN_USER_KEY, loginUser).set(LoginHelper.USER_KEY, userId);
    }

    @Override public SaRequest getRequest() { throw new UnsupportedOperationException(); }
    @Override public SaResponse getResponse() { throw new UnsupportedOperationException(); }
    @Override public SaStorage getStorage() { return storage.get(); }
    @Override public boolean matchPath(String pattern, String path) { return false; }
    @Override public boolean isValid() { return true; }

    private static final class TestSaStorage implements SaStorage {
        private final Map<String, Object> values = new HashMap<>();
        @Override public Object getSource() { return values; }
        @Override public Object get(String key) { return values.get(key); }
        @Override public SaStorage set(String key, Object value) { values.put(key, value); return this; }
        @Override public SaStorage delete(String key) { values.remove(key); return this; }
        void clear() { values.clear(); }
    }
}
