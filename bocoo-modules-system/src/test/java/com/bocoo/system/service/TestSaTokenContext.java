package com.bocoo.system.service;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.context.model.SaStorage;
import com.bocoo.common.core.domain.bo.LoginUser;
import com.bocoo.common.satoken.utils.LoginHelper;

import java.util.HashMap;
import java.util.Map;

final class TestSaTokenContext implements SaTokenContext {

    private static final TestSaTokenContext INSTANCE = new TestSaTokenContext();

    private final ThreadLocal<TestSaStorage> storage = ThreadLocal.withInitial(TestSaStorage::new);

    private TestSaTokenContext() {
    }

    static void install() {
        SaManager.setSaTokenContext(INSTANCE);
        INSTANCE.storage.get().clear();
    }

    static void setLoginUser(String tenantType, Long tenantId, Long userId, String username) {
        LoginUser loginUser = new LoginUser();
        loginUser.setTenantType(tenantType);
        loginUser.setTenantId(tenantId);
        loginUser.setUserId(userId);
        loginUser.setUsername(username);
        INSTANCE.storage.get()
            .set(LoginHelper.LOGIN_USER_KEY, loginUser)
            .set(LoginHelper.USER_KEY, userId);
    }

    @Override
    public SaRequest getRequest() {
        throw new UnsupportedOperationException("Request is not needed in service tests");
    }

    @Override
    public SaResponse getResponse() {
        throw new UnsupportedOperationException("Response is not needed in service tests");
    }

    @Override
    public SaStorage getStorage() {
        return storage.get();
    }

    @Override
    public boolean matchPath(String pattern, String path) {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    private static final class TestSaStorage implements SaStorage {

        private final Map<String, Object> values = new HashMap<>();

        @Override
        public Object getSource() {
            return values;
        }

        @Override
        public Object get(String key) {
            return values.get(key);
        }

        @Override
        public SaStorage set(String key, Object value) {
            values.put(key, value);
            return this;
        }

        @Override
        public SaStorage delete(String key) {
            values.remove(key);
            return this;
        }

        void clear() {
            values.clear();
        }
    }
}
