package com.bocoo.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.ai.domain.bo.AiProviderConfigBo;
import com.bocoo.ai.domain.bo.AiProviderCredentialBo;
import com.bocoo.ai.domain.bo.AiProviderModelBo;
import com.bocoo.ai.domain.bo.AiProviderModelQueryBo;
import com.bocoo.ai.domain.bo.AiProviderQueryBo;
import com.bocoo.ai.domain.entity.AiProviderConfig;
import com.bocoo.ai.domain.entity.AiProviderCredential;
import com.bocoo.ai.domain.entity.AiProviderModel;
import com.bocoo.ai.domain.vo.AiProviderConfigVo;
import com.bocoo.ai.domain.vo.AiProviderModelVo;
import com.bocoo.ai.domain.vo.AiRuntimeProviderVo;
import com.bocoo.ai.mapper.AiProviderConfigMapper;
import com.bocoo.ai.mapper.AiProviderCredentialMapper;
import com.bocoo.ai.mapper.AiProviderModelMapper;
import com.bocoo.ai.service.AiProviderConfigService;
import com.bocoo.ai.service.AiSecretCryptoService;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AiProviderConfigServiceImpl implements AiProviderConfigService {

    private static final String STATUS_ENABLED = "1";
    private static final String STATUS_DISABLED = "0";
    private static final String UNCONFIGURED_MODEL = "UNCONFIGURED";

    private final AiProviderConfigMapper providerConfigMapper;
    private final AiProviderCredentialMapper providerCredentialMapper;
    private final AiProviderModelMapper providerModelMapper;
    private final AiSecretCryptoService cryptoService;

    @Override
    public TableDataInfo<AiProviderConfigVo> queryProviderPageList(AiProviderQueryBo bo, PageQuery pageQuery) {
        return TenantContextHolder.callWithIgnore(() -> {
            LambdaQueryWrapper<AiProviderConfig> wrapper = buildProviderQueryWrapper(bo);
            if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
                wrapper.orderByDesc(AiProviderConfig::getEnabled).orderByAsc(AiProviderConfig::getProviderCode);
            }
            Page<AiProviderConfigVo> page = providerConfigMapper.selectVoPage(pageQuery.build(), wrapper);
            page.getRecords().forEach(this::fillFingerprint);
            return TableDataInfo.build(page);
        });
    }

    @Override
    public List<AiProviderConfigVo> queryProviderOptions() {
        return TenantContextHolder.callWithIgnore(() -> {
            List<AiProviderConfigVo> providers = providerConfigMapper.selectVoList(new LambdaQueryWrapper<AiProviderConfig>()
                .eq(AiProviderConfig::getStatus, STATUS_ENABLED)
                .orderByDesc(AiProviderConfig::getEnabled)
                .orderByAsc(AiProviderConfig::getProviderCode));
            providers.forEach(this::fillFingerprint);
            return providers;
        });
    }

    @Override
    public AiProviderConfigVo queryProviderById(Long providerId) {
        return TenantContextHolder.callWithIgnore(() -> {
            AiProviderConfigVo vo = providerConfigMapper.selectVoById(providerId);
            fillFingerprint(vo);
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertProvider(AiProviderConfigBo bo) {
        return TenantContextHolder.callWithIgnore(() -> {
            validateProviderCodeUnique(bo);
            AiProviderConfig entity = toProviderEntity(bo);
            boolean success = providerConfigMapper.insert(entity) > 0;
            normalizeSingleEnabledProvider(entity);
            return success;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProvider(AiProviderConfigBo bo) {
        return TenantContextHolder.callWithIgnore(() -> {
            if (bo.getProviderId() == null) {
                throw ServiceException.ofMessageKey("ai.provider.id.required");
            }
            validateProviderCodeUnique(bo);
            AiProviderConfig entity = toProviderEntity(bo);
            boolean success = providerConfigMapper.updateById(entity) > 0;
            normalizeSingleEnabledProvider(entity);
            return success;
        });
    }

    @Override
    public Boolean saveApiKey(Long providerId, AiProviderCredentialBo bo) {
        return TenantContextHolder.callWithIgnore(() -> {
            AiProviderConfig provider = providerConfigMapper.selectById(providerId);
            if (provider == null) {
                throw ServiceException.ofMessageKey("ai.provider.notConfigured");
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
            return providerCredentialMapper.insert(credential) > 0;
        });
    }

    @Override
    public TableDataInfo<AiProviderModelVo> queryModelPageList(AiProviderModelQueryBo bo, PageQuery pageQuery) {
        return TenantContextHolder.callWithIgnore(() -> {
            LambdaQueryWrapper<AiProviderModel> wrapper = buildModelQueryWrapper(bo);
            if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
                wrapper.orderByDesc(AiProviderModel::getDefaultModel).orderByAsc(AiProviderModel::getModelCode);
            }
            Page<AiProviderModelVo> page = providerModelMapper.selectVoPage(pageQuery.build(), wrapper);
            return TableDataInfo.build(page);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertModel(AiProviderModelBo bo) {
        return TenantContextHolder.callWithIgnore(() -> {
            AiProviderConfig provider = findRequiredByCode(bo.getProviderCode());
            validateModelCodeUnique(provider.getProviderId(), bo);
            AiProviderModel entity = toModelEntity(bo, provider.getProviderId());
            boolean success = providerModelMapper.insert(entity) > 0;
            normalizeDefaultModel(provider, entity);
            return success;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateModel(AiProviderModelBo bo) {
        return TenantContextHolder.callWithIgnore(() -> {
            if (bo.getModelId() == null) {
                throw ServiceException.ofMessageKey("ai.model.id.required");
            }
            AiProviderConfig provider = findRequiredByCode(bo.getProviderCode());
            validateModelCodeUnique(provider.getProviderId(), bo);
            AiProviderModel entity = toModelEntity(bo, provider.getProviderId());
            boolean success = providerModelMapper.updateById(entity) > 0;
            normalizeDefaultModel(provider, entity);
            return success;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setDefaultModel(Long modelId) {
        return TenantContextHolder.callWithIgnore(() -> {
            AiProviderModel model = providerModelMapper.selectById(modelId);
            if (model == null) {
                throw ServiceException.ofMessageKey("ai.provider.model.notConfigured");
            }
            AiProviderConfig provider = providerConfigMapper.selectById(model.getProviderId());
            if (provider == null) {
                throw ServiceException.ofMessageKey("ai.provider.notConfigured");
            }
            providerModelMapper.update(null, new LambdaUpdateWrapper<AiProviderModel>()
                .set(AiProviderModel::getDefaultModel, false)
                .eq(AiProviderModel::getProviderId, provider.getProviderId()));
            model.setDefaultModel(true);
            model.setStatus(STATUS_ENABLED);
            providerModelMapper.updateById(model);
            provider.setDefaultModel(model.getModelCode());
            return providerConfigMapper.updateById(provider) > 0;
        });
    }

    @Override
    public AiRuntimeProviderVo runtimeProvider() {
        return TenantContextHolder.callWithIgnore(() -> {
            AiProviderConfig provider = providerConfigMapper.selectOne(new LambdaQueryWrapper<AiProviderConfig>()
                .eq(AiProviderConfig::getEnabled, true)
                .eq(AiProviderConfig::getStatus, STATUS_ENABLED)
                .last("limit 1"), false);
            if (provider == null) {
                throw ServiceException.ofMessageKey("ai.provider.enabled.missing");
            }
            AiProviderCredential credential = activeCredential(provider.getProviderId());
            if (credential == null) {
                throw ServiceException.ofMessageKey("ai.provider.apiKey.missing");
            }
            credential.setLastUsedTime(TimeUtils.utcNow());
            providerCredentialMapper.updateById(credential);

            AiRuntimeProviderVo vo = new AiRuntimeProviderVo();
            vo.setProviderCode(provider.getProviderCode());
            vo.setBaseUrl(trimSlash(provider.getBaseUrl()));
            vo.setChatCompletionsUrl(trimSlash(provider.getBaseUrl()) + normalizePath(provider.getChatCompletionsPath()));
            vo.setApiKey(cryptoService.decrypt(credential.getApiKeyCiphertext()));
            vo.setModel(resolveRuntimeModel(provider));
            vo.setTimeoutSeconds(provider.getTimeoutSeconds());
            return vo;
        });
    }

    @Override
    public AiProviderConfig findEnabledProvider() {
        return TenantContextHolder.callWithIgnore(() ->
            providerConfigMapper.selectOne(new LambdaQueryWrapper<AiProviderConfig>()
                .eq(AiProviderConfig::getEnabled, true)
                .eq(AiProviderConfig::getStatus, STATUS_ENABLED)
                .last("limit 1"), false));
    }

    private LambdaQueryWrapper<AiProviderConfig> buildProviderQueryWrapper(AiProviderQueryBo bo) {
        return new LambdaQueryWrapper<AiProviderConfig>()
            .like(StringUtils.isNotBlank(bo.getProviderCode()), AiProviderConfig::getProviderCode, bo.getProviderCode())
            .like(StringUtils.isNotBlank(bo.getProviderName()), AiProviderConfig::getProviderName, bo.getProviderName())
            .eq(bo.getEnabled() != null, AiProviderConfig::getEnabled, bo.getEnabled())
            .eq(StringUtils.isNotBlank(bo.getStatus()), AiProviderConfig::getStatus, bo.getStatus());
    }

    private LambdaQueryWrapper<AiProviderModel> buildModelQueryWrapper(AiProviderModelQueryBo bo) {
        Long providerId = null;
        if (StringUtils.isNotBlank(bo.getProviderCode())) {
            AiProviderConfig provider = findRequiredByCode(bo.getProviderCode());
            providerId = provider.getProviderId();
        }
        return new LambdaQueryWrapper<AiProviderModel>()
            .eq(providerId != null, AiProviderModel::getProviderId, providerId)
            .like(StringUtils.isNotBlank(bo.getModelCode()), AiProviderModel::getModelCode, bo.getModelCode())
            .like(StringUtils.isNotBlank(bo.getModelName()), AiProviderModel::getModelName, bo.getModelName())
            .eq(StringUtils.isNotBlank(bo.getModelType()), AiProviderModel::getModelType, bo.getModelType())
            .eq(StringUtils.isNotBlank(bo.getStatus()), AiProviderModel::getStatus, bo.getStatus());
    }

    private AiProviderConfig toProviderEntity(AiProviderConfigBo bo) {
        AiProviderConfig entity = MapstructUtils.convert(bo, AiProviderConfig.class);
        if (entity == null) {
            throw ServiceException.ofMessageKey("ai.provider.invalid");
        }
        entity.setStatus(StringUtils.isBlank(entity.getStatus()) ? STATUS_ENABLED : entity.getStatus());
        entity.setChatCompletionsPath(StringUtils.isBlank(entity.getChatCompletionsPath()) ? "/chat/completions" : entity.getChatCompletionsPath());
        entity.setTimeoutSeconds(entity.getTimeoutSeconds() == null ? 120 : entity.getTimeoutSeconds());
        entity.setEnabled(Boolean.TRUE.equals(entity.getEnabled()));
        if (StringUtils.isBlank(entity.getDefaultModel())) {
            entity.setDefaultModel(existingDefaultModel(entity.getProviderId()));
        }
        return entity;
    }

    private AiProviderModel toModelEntity(AiProviderModelBo bo, Long providerId) {
        AiProviderModel entity = MapstructUtils.convert(bo, AiProviderModel.class);
        if (entity == null) {
            throw ServiceException.ofMessageKey("ai.model.invalid");
        }
        entity.setProviderId(providerId);
        entity.setModelName(StringUtils.isBlank(entity.getModelName()) ? entity.getModelCode() : entity.getModelName());
        entity.setModelType(StringUtils.isBlank(entity.getModelType()) ? "CHAT" : entity.getModelType().toUpperCase());
        entity.setStatus(StringUtils.isBlank(entity.getStatus()) ? STATUS_ENABLED : entity.getStatus());
        entity.setDefaultModel(Boolean.TRUE.equals(entity.getDefaultModel()));
        if (Boolean.TRUE.equals(entity.getDefaultModel())) {
            entity.setStatus(STATUS_ENABLED);
        }
        return entity;
    }

    private void normalizeSingleEnabledProvider(AiProviderConfig entity) {
        if (Boolean.TRUE.equals(entity.getEnabled())) {
            providerConfigMapper.update(null, new LambdaUpdateWrapper<AiProviderConfig>()
                .set(AiProviderConfig::getEnabled, false)
                .ne(AiProviderConfig::getProviderId, entity.getProviderId()));
        }
    }

    private void normalizeDefaultModel(AiProviderConfig provider, AiProviderModel entity) {
        if (Boolean.TRUE.equals(entity.getDefaultModel())) {
            providerModelMapper.update(null, new LambdaUpdateWrapper<AiProviderModel>()
                .set(AiProviderModel::getDefaultModel, false)
                .eq(AiProviderModel::getProviderId, provider.getProviderId())
                .ne(AiProviderModel::getModelId, entity.getModelId()));
            provider.setDefaultModel(entity.getModelCode());
            providerConfigMapper.updateById(provider);
        }
    }

    private void validateProviderCodeUnique(AiProviderConfigBo bo) {
        AiProviderConfig existing = providerConfigMapper.selectOne(new LambdaQueryWrapper<AiProviderConfig>()
            .eq(AiProviderConfig::getProviderCode, bo.getProviderCode()), false);
        if (existing != null && !existing.getProviderId().equals(bo.getProviderId())) {
            throw ServiceException.ofMessageKey("ai.provider.code.exists");
        }
    }

    private void validateModelCodeUnique(Long providerId, AiProviderModelBo bo) {
        AiProviderModel existing = providerModelMapper.selectOne(new LambdaQueryWrapper<AiProviderModel>()
            .eq(AiProviderModel::getProviderId, providerId)
            .eq(AiProviderModel::getModelCode, bo.getModelCode()), false);
        if (existing != null && !existing.getModelId().equals(bo.getModelId())) {
            throw ServiceException.ofMessageKey("ai.model.code.exists");
        }
    }

    private AiProviderConfig findRequiredByCode(String providerCode) {
        AiProviderConfig provider = providerConfigMapper.selectOne(new LambdaQueryWrapper<AiProviderConfig>()
            .eq(AiProviderConfig::getProviderCode, providerCode), false);
        if (provider == null) {
            throw ServiceException.ofMessageKey("ai.provider.notConfigured");
        }
        return provider;
    }

    private AiProviderCredential activeCredential(Long providerId) {
        return providerCredentialMapper.selectOne(new LambdaQueryWrapper<AiProviderCredential>()
            .eq(AiProviderCredential::getProviderId, providerId)
            .eq(AiProviderCredential::getStatus, STATUS_ENABLED)
            .orderByDesc(AiProviderCredential::getCreateTime)
            .last("limit 1"), false);
    }

    private String existingDefaultModel(Long providerId) {
        if (providerId != null) {
            AiProviderConfig existing = providerConfigMapper.selectById(providerId);
            if (existing != null && StringUtils.isNotBlank(existing.getDefaultModel())) {
                return existing.getDefaultModel();
            }
        }
        return UNCONFIGURED_MODEL;
    }

    private String resolveRuntimeModel(AiProviderConfig provider) {
        AiProviderModel defaultModel = providerModelMapper.selectOne(new LambdaQueryWrapper<AiProviderModel>()
            .eq(AiProviderModel::getProviderId, provider.getProviderId())
            .eq(AiProviderModel::getDefaultModel, true)
            .eq(AiProviderModel::getStatus, STATUS_ENABLED)
            .last("limit 1"), false);
        if (defaultModel != null && StringUtils.isNotBlank(defaultModel.getModelCode())) {
            return defaultModel.getModelCode();
        }
        if (StringUtils.isBlank(provider.getDefaultModel()) || UNCONFIGURED_MODEL.equals(provider.getDefaultModel())) {
            throw ServiceException.ofMessageKey("ai.provider.defaultModel.missing");
        }
        return provider.getDefaultModel();
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
