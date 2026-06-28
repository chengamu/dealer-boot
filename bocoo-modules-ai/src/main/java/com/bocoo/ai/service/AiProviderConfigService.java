package com.bocoo.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.ai.domain.bo.AiProviderConfigBo;
import com.bocoo.ai.domain.bo.AiProviderCredentialBo;
import com.bocoo.ai.domain.bo.AiProviderModelBo;
import com.bocoo.ai.domain.entity.AiProviderConfig;
import com.bocoo.ai.domain.entity.AiProviderCredential;
import com.bocoo.ai.domain.entity.AiProviderModel;
import com.bocoo.ai.domain.vo.AiProviderConfigVo;
import com.bocoo.ai.domain.vo.AiProviderModelVo;
import com.bocoo.ai.domain.vo.AiRuntimeProviderVo;
import com.bocoo.ai.mapper.AiProviderConfigMapper;
import com.bocoo.ai.mapper.AiProviderCredentialMapper;
import com.bocoo.ai.mapper.AiProviderModelMapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AiProviderConfigService {

    private static final String STATUS_ENABLED = "1";
    private static final String STATUS_DISABLED = "0";

    private final AiProviderConfigMapper providerConfigMapper;
    private final AiProviderCredentialMapper providerCredentialMapper;
    private final AiProviderModelMapper providerModelMapper;
    private final AiSecretCryptoService cryptoService;

    public List<AiProviderConfigVo> list() {
        return TenantContextHolder.callWithIgnore(() -> {
            List<AiProviderConfigVo> providers = providerConfigMapper.selectVoList(new LambdaQueryWrapper<AiProviderConfig>()
                .orderByAsc(AiProviderConfig::getProviderCode));
            providers.forEach(this::fillFingerprint);
            return providers;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(AiProviderConfigBo bo) {
        TenantContextHolder.runWithIgnore(() -> {
            if (StringUtils.isBlank(bo.getProviderCode())) {
                throw new ServiceException("Provider code cannot be blank");
            }
            AiProviderConfig entity = MapstructUtils.convert(bo, AiProviderConfig.class);
            entity.setStatus(StringUtils.isBlank(entity.getStatus()) ? STATUS_ENABLED : entity.getStatus());
            entity.setChatCompletionsPath(StringUtils.isBlank(entity.getChatCompletionsPath())
                ? "/chat/completions" : entity.getChatCompletionsPath());
            entity.setTimeoutSeconds(entity.getTimeoutSeconds() == null ? 120 : entity.getTimeoutSeconds());
            entity.setEnabled(Boolean.TRUE.equals(entity.getEnabled()));

            AiProviderConfig existing = findByCode(entity.getProviderCode());
            if (existing == null) {
                providerConfigMapper.insert(entity);
            } else {
                entity.setProviderId(existing.getProviderId());
                providerConfigMapper.updateById(entity);
            }
            if (Boolean.TRUE.equals(entity.getEnabled())) {
                providerConfigMapper.update(null, new LambdaUpdateWrapper<AiProviderConfig>()
                    .set(AiProviderConfig::getEnabled, false)
                    .ne(AiProviderConfig::getProviderId, entity.getProviderId()));
            }
        });
    }

    public void saveApiKey(AiProviderCredentialBo bo) {
        TenantContextHolder.runWithIgnore(() -> {
            AiProviderConfig provider = findRequiredByCode(bo.getProviderCode());
            if (StringUtils.isBlank(bo.getApiKey())) {
                throw new ServiceException("Provider API key cannot be blank");
            }
            providerCredentialMapper.update(null, new LambdaUpdateWrapper<AiProviderCredential>()
                .set(AiProviderCredential::getStatus, STATUS_DISABLED)
                .eq(AiProviderCredential::getProviderId, provider.getProviderId()));

            AiProviderCredential credential = new AiProviderCredential();
            credential.setProviderId(provider.getProviderId());
            credential.setApiKeyCiphertext(cryptoService.encrypt(bo.getApiKey()));
            credential.setKeyFingerprint(cryptoService.fingerprint(bo.getApiKey()));
            credential.setStatus(STATUS_ENABLED);
            credential.setRemark(bo.getRemark());
            providerCredentialMapper.insert(credential);
        });
    }

    public List<AiProviderModelVo> listModels(String providerCode) {
        return TenantContextHolder.callWithIgnore(() -> {
            AiProviderConfig provider = findRequiredByCode(providerCode);
            return providerModelMapper.selectVoList(new LambdaQueryWrapper<AiProviderModel>()
                .eq(AiProviderModel::getProviderId, provider.getProviderId())
                .orderByDesc(AiProviderModel::getDefaultModel)
                .orderByAsc(AiProviderModel::getModelCode));
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveModel(AiProviderModelBo bo) {
        TenantContextHolder.runWithIgnore(() -> {
            AiProviderConfig provider = findRequiredByCode(bo.getProviderCode());
            if (StringUtils.isBlank(bo.getModelCode())) {
                throw new ServiceException("Model code cannot be blank");
            }
            AiProviderModel entity = MapstructUtils.convert(bo, AiProviderModel.class);
            entity.setProviderId(provider.getProviderId());
            entity.setModelName(StringUtils.isBlank(entity.getModelName()) ? entity.getModelCode() : entity.getModelName());
            entity.setModelType(StringUtils.isBlank(entity.getModelType()) ? "CHAT" : entity.getModelType());
            entity.setStatus(StringUtils.isBlank(entity.getStatus()) ? STATUS_ENABLED : entity.getStatus());
            entity.setDefaultModel(Boolean.TRUE.equals(entity.getDefaultModel()));

            AiProviderModel existing = findModel(provider.getProviderId(), entity.getModelCode());
            if (existing == null) {
                providerModelMapper.insert(entity);
            } else {
                entity.setModelId(existing.getModelId());
                providerModelMapper.updateById(entity);
            }
            if (Boolean.TRUE.equals(entity.getDefaultModel())) {
                providerModelMapper.update(null, new LambdaUpdateWrapper<AiProviderModel>()
                    .set(AiProviderModel::getDefaultModel, false)
                    .eq(AiProviderModel::getProviderId, provider.getProviderId())
                    .ne(AiProviderModel::getModelId, entity.getModelId()));
                provider.setDefaultModel(entity.getModelCode());
                providerConfigMapper.updateById(provider);
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void setDefaultModel(String providerCode, Long modelId) {
        TenantContextHolder.runWithIgnore(() -> {
            AiProviderConfig provider = findRequiredByCode(providerCode);
            AiProviderModel model = providerModelMapper.selectById(modelId);
            if (model == null || !provider.getProviderId().equals(model.getProviderId())) {
                throw new ServiceException("AI provider model is not configured");
            }
            providerModelMapper.update(null, new LambdaUpdateWrapper<AiProviderModel>()
                .set(AiProviderModel::getDefaultModel, false)
                .eq(AiProviderModel::getProviderId, provider.getProviderId()));
            model.setDefaultModel(true);
            model.setStatus(STATUS_ENABLED);
            providerModelMapper.updateById(model);
            provider.setDefaultModel(model.getModelCode());
            providerConfigMapper.updateById(provider);
        });
    }

    public AiRuntimeProviderVo runtimeProvider() {
        return TenantContextHolder.callWithIgnore(() -> {
            AiProviderConfig provider = providerConfigMapper.selectOne(new LambdaQueryWrapper<AiProviderConfig>()
                .eq(AiProviderConfig::getEnabled, true)
                .eq(AiProviderConfig::getStatus, STATUS_ENABLED)
                .last("limit 1"), false);
            if (provider == null) {
                throw new ServiceException("No enabled AI provider is configured");
            }
            AiProviderCredential credential = activeCredential(provider.getProviderId());
            if (credential == null) {
                throw new ServiceException("AI provider API key is not configured");
            }
            credential.setLastUsedTime(TimeUtils.utcNow());
            providerCredentialMapper.updateById(credential);

            AiRuntimeProviderVo vo = new AiRuntimeProviderVo();
            vo.setProviderCode(provider.getProviderCode());
            vo.setBaseUrl(trimSlash(provider.getBaseUrl()));
            vo.setChatCompletionsUrl(trimSlash(provider.getBaseUrl()) + normalizePath(provider.getChatCompletionsPath()));
            vo.setApiKey(cryptoService.decrypt(credential.getApiKeyCiphertext()));
            vo.setModel(provider.getDefaultModel());
            vo.setTimeoutSeconds(provider.getTimeoutSeconds());
            return vo;
        });
    }

    public AiProviderConfig findEnabledProvider() {
        return TenantContextHolder.callWithIgnore(() ->
            providerConfigMapper.selectOne(new LambdaQueryWrapper<AiProviderConfig>()
                .eq(AiProviderConfig::getEnabled, true)
                .eq(AiProviderConfig::getStatus, STATUS_ENABLED)
                .last("limit 1"), false));
    }

    private AiProviderConfig findByCode(String providerCode) {
        return providerConfigMapper.selectOne(new LambdaQueryWrapper<AiProviderConfig>()
            .eq(AiProviderConfig::getProviderCode, providerCode), false);
    }

    private AiProviderConfig findRequiredByCode(String providerCode) {
        AiProviderConfig provider = findByCode(providerCode);
        if (provider == null) {
            throw new ServiceException("AI provider is not configured");
        }
        return provider;
    }

    private AiProviderModel findModel(Long providerId, String modelCode) {
        return providerModelMapper.selectOne(new LambdaQueryWrapper<AiProviderModel>()
            .eq(AiProviderModel::getProviderId, providerId)
            .eq(AiProviderModel::getModelCode, modelCode), false);
    }

    private AiProviderCredential activeCredential(Long providerId) {
        return providerCredentialMapper.selectOne(new LambdaQueryWrapper<AiProviderCredential>()
            .eq(AiProviderCredential::getProviderId, providerId)
            .eq(AiProviderCredential::getStatus, STATUS_ENABLED)
            .orderByDesc(AiProviderCredential::getCreateTime)
            .last("limit 1"), false);
    }

    private void fillFingerprint(AiProviderConfigVo vo) {
        if (vo == null || vo.getProviderId() == null) {
            return;
        }
        AiProviderCredential credential = activeCredential(vo.getProviderId());
        if (credential != null) {
            vo.setKeyFingerprint(credential.getKeyFingerprint());
        }
    }

    private String trimSlash(String value) {
        return value == null ? "" : value.replaceAll("/+$", "");
    }

    private String normalizePath(String value) {
        if (StringUtils.isBlank(value)) {
            return "/chat/completions";
        }
        return value.startsWith("/") ? value : "/" + value;
    }
}
