package com.bocoo.ai.service;

import cn.dev33.satoken.stp.StpUtil;
import com.bocoo.ai.config.AiAssistantProperties;
import com.bocoo.ai.domain.bo.AiAuthzCheckBo;
import com.bocoo.ai.domain.entity.AiProviderConfig;
import com.bocoo.ai.domain.vo.AiBootstrapVo;
import com.bocoo.ai.domain.vo.AiRuntimeAuthzVo;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AiAccessService {

    public static final String ASSISTANT_USE_PERMISSION = "ai:assistant:use";

    private final AiAssistantProperties properties;
    private final AiRuntimeTokenService tokenService;
    private final AiProviderConfigService providerConfigService;
    private final AiUsageService usageService;

    public AiBootstrapVo bootstrap() {
        AiBootstrapVo vo = new AiBootstrapVo();
        boolean enabled = properties.isEnabled() && canUseAssistant();
        vo.setEnabled(enabled);
        vo.setModel(resolveModel());
        vo.setPageAgentBaseUrl(properties.getPageAgentBaseUrl());
        vo.setApiKey(enabled ? tokenService.issueChannelToken() : null);
        vo.setFeatures(enabled ? List.of("page-agent", "guide", "draft-fill") : List.of());
        vo.setPermissions(enabled ? List.of(ASSISTANT_USE_PERMISSION) : List.of());
        vo.setQuota(usageService.remainingQuotaForCurrentUser());
        return vo;
    }

    public void assertCanUseAssistant() {
        if (!properties.isEnabled()) {
            throw new ServiceException("AI assistant is disabled");
        }
        if (!canUseAssistant()) {
            throw new ServiceException("No AI assistant permission", 403);
        }
    }

    private boolean canUseAssistant() {
        if (!LoginHelper.isLogin()) {
            return false;
        }
        return LoginHelper.isAdmin() || StpUtil.hasPermission(ASSISTANT_USE_PERMISSION);
    }

    public AiRuntimeAuthzVo checkRuntimeAccess(AiAuthzCheckBo bo) {
        AiRuntimeAuthzVo vo = new AiRuntimeAuthzVo();
        if (!properties.isEnabled()) {
            vo.setAllowed(false);
            vo.setReason("AI assistant is disabled");
            return vo;
        }
        Map<String, Object> claims = tokenService.verifyChannelToken(bo.getChannelToken());
        boolean admin = Boolean.parseBoolean(String.valueOf(claims.getOrDefault("admin", false)));
        boolean hasPermission = admin || hasPermission(claims.get("permissions"));
        if (!hasPermission) {
            vo.setAllowed(false);
            vo.setReason("No AI assistant permission");
            return vo;
        }
        Long tenantId = longClaim(claims.get("tenantId"));
        Long userId = longClaim(claims.get("userId"));
        String denyReason = usageService.quotaDenyReason(tenantId, userId);
        if (denyReason != null) {
            vo.setAllowed(false);
            vo.setReason(denyReason);
            return vo;
        }
        AiProviderConfig provider = providerConfigService.findEnabledProvider();
        vo.setAllowed(true);
        vo.setReason("ok");
        vo.setTenantId(tenantId);
        vo.setUserId(userId);
        vo.setUsername(String.valueOf(claims.getOrDefault("username", "")));
        vo.setProviderCode(provider == null ? null : provider.getProviderCode());
        vo.setModel(provider == null ? properties.getDefaultModel() : provider.getDefaultModel());
        return vo;
    }

    private String resolveModel() {
        AiProviderConfig provider = providerConfigService.findEnabledProvider();
        return provider == null ? properties.getDefaultModel() : provider.getDefaultModel();
    }

    private boolean hasPermission(Object permissionsValue) {
        if (permissionsValue instanceof List<?> permissions) {
            return permissions.stream().map(String::valueOf).anyMatch(ASSISTANT_USE_PERMISSION::equals);
        }
        return false;
    }

    private Long longClaim(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
