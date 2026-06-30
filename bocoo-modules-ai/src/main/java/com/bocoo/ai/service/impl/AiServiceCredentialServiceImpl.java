package com.bocoo.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.ai.domain.bo.AiServiceCredentialBo;
import com.bocoo.ai.domain.bo.AiServiceCredentialGenerateBo;
import com.bocoo.ai.domain.entity.AiServiceCredential;
import com.bocoo.ai.domain.vo.AiServiceCredentialSecretVo;
import com.bocoo.ai.domain.vo.AiServiceCredentialVo;
import com.bocoo.ai.mapper.AiServiceCredentialMapper;
import com.bocoo.ai.service.AiSecretCryptoService;
import com.bocoo.ai.service.AiServiceCredentialService;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AiServiceCredentialServiceImpl implements AiServiceCredentialService {

    private static final String STATUS_ENABLED = "1";
    private static final String STATUS_DISABLED = "0";

    private final AiServiceCredentialMapper credentialMapper;
    private final AiSecretCryptoService cryptoService;

    @Override
    public TableDataInfo<AiServiceCredentialVo> queryPageList(AiServiceCredentialBo bo, PageQuery pageQuery) {
        return TenantContextHolder.callWithIgnore(() -> {
            LambdaQueryWrapper<AiServiceCredential> wrapper = buildQueryWrapper(bo);
            if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
                wrapper.orderByDesc(AiServiceCredential::getCreateTime);
            }
            Page<AiServiceCredentialVo> page = credentialMapper.selectVoPage(pageQuery.build(), wrapper);
            return TableDataInfo.build(page);
        });
    }

    @Override
    public AiServiceCredentialSecretVo generate(AiServiceCredentialGenerateBo bo) {
        return TenantContextHolder.callWithIgnore(() -> {
            String serviceName = StringUtils.isBlank(bo.getServiceName()) ? "ai-runtime" : bo.getServiceName().trim();
            String secret = cryptoService.generateSecret();
            AiServiceCredential entity = new AiServiceCredential();
            entity.setServiceName(serviceName);
            entity.setKeyVersion(cryptoService.generateVersion());
            entity.setSecretCiphertext(cryptoService.encrypt(secret));
            entity.setSecretFingerprint(cryptoService.fingerprint(secret));
            entity.setStatus(STATUS_ENABLED);
            entity.setRemark(bo.getRemark());
            if (credentialMapper.insert(entity) <= 0) {
                throw ServiceException.ofMessageKey("ai.serviceCredential.generate.failed");
            }
            AiServiceCredentialSecretVo result = new AiServiceCredentialSecretVo();
            result.setCredential(MapstructUtils.convert(entity, AiServiceCredentialVo.class));
            result.setSecret(secret);
            return result;
        });
    }

    @Override
    public Boolean enable(Long credentialId) {
        return updateStatus(credentialId, STATUS_ENABLED);
    }

    @Override
    public Boolean disable(Long credentialId) {
        return updateStatus(credentialId, STATUS_DISABLED);
    }

    @Override
    public Boolean delete(Long credentialId) {
        return TenantContextHolder.callWithIgnore(() -> {
            AiServiceCredential entity = credentialMapper.selectById(credentialId);
            if (entity == null) {
                throw ServiceException.ofMessageKey("ai.serviceCredential.notFound");
            }
            return credentialMapper.deleteById(credentialId) > 0;
        });
    }

    @Override
    public String findActiveSecret(String serviceName, String keyVersion) {
        return TenantContextHolder.callWithIgnore(() -> {
            AiServiceCredential credential = credentialMapper.selectOne(new LambdaQueryWrapper<AiServiceCredential>()
                .eq(AiServiceCredential::getServiceName, serviceName)
                .eq(AiServiceCredential::getKeyVersion, keyVersion)
                .eq(AiServiceCredential::getStatus, STATUS_ENABLED), false);
            if (credential == null) {
                throw ServiceException.ofMessageKey("ai.serviceCredential.invalid").setCode(401);
            }
            return cryptoService.decrypt(credential.getSecretCiphertext());
        });
    }

    @Override
    public void markUsed(String serviceName, String keyVersion) {
        TenantContextHolder.runWithIgnore(() -> {
            AiServiceCredential credential = credentialMapper.selectOne(new LambdaQueryWrapper<AiServiceCredential>()
                .eq(AiServiceCredential::getServiceName, serviceName)
                .eq(AiServiceCredential::getKeyVersion, keyVersion)
                .eq(AiServiceCredential::getStatus, STATUS_ENABLED), false);
            if (credential == null) {
                return;
            }
            credential.setLastUsedTime(TimeUtils.utcNow());
            credentialMapper.updateById(credential);
        });
    }

    private LambdaQueryWrapper<AiServiceCredential> buildQueryWrapper(AiServiceCredentialBo bo) {
        return new LambdaQueryWrapper<AiServiceCredential>()
            .like(StringUtils.isNotBlank(bo.getServiceName()), AiServiceCredential::getServiceName, bo.getServiceName())
            .eq(StringUtils.isNotBlank(bo.getStatus()), AiServiceCredential::getStatus, bo.getStatus());
    }

    private Boolean updateStatus(Long credentialId, String status) {
        return TenantContextHolder.callWithIgnore(() -> {
            AiServiceCredential entity = credentialMapper.selectById(credentialId);
            if (entity == null) {
                throw ServiceException.ofMessageKey("ai.serviceCredential.notFound");
            }
            entity.setStatus(status);
            entity.setUpdateBy(LoginHelper.getUsername());
            entity.setUpdateTime(TimeUtils.utcNow());
            return credentialMapper.updateById(entity) > 0;
        });
    }
}
