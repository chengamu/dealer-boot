package com.bocoo.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.ai.domain.bo.AiServiceCredentialGenerateBo;
import com.bocoo.ai.domain.entity.AiServiceCredential;
import com.bocoo.ai.domain.vo.AiServiceCredentialSecretVo;
import com.bocoo.ai.domain.vo.AiServiceCredentialVo;
import com.bocoo.ai.mapper.AiServiceCredentialMapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AiServiceCredentialService {

    private static final String STATUS_ENABLED = "1";
    private static final String STATUS_DISABLED = "0";

    private final AiServiceCredentialMapper credentialMapper;
    private final AiSecretCryptoService cryptoService;

    public List<AiServiceCredentialVo> list() {
        return TenantContextHolder.callWithIgnore(() ->
            credentialMapper.selectVoList(new LambdaQueryWrapper<AiServiceCredential>()
                .orderByDesc(AiServiceCredential::getCreateTime)));
    }

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
                throw new ServiceException("Failed to generate AI service credential");
            }
            AiServiceCredentialSecretVo result = new AiServiceCredentialSecretVo();
            result.setCredential(MapstructUtils.convert(entity, AiServiceCredentialVo.class));
            result.setSecret(secret);
            return result;
        });
    }

    public void disable(Long credentialId) {
        TenantContextHolder.runWithIgnore(() -> {
            AiServiceCredential entity = credentialMapper.selectById(credentialId);
            if (entity == null) {
                throw new ServiceException("AI service credential not found");
            }
            entity.setStatus(STATUS_DISABLED);
            credentialMapper.updateById(entity);
        });
    }

    public String findActiveSecret(String serviceName, String keyVersion) {
        return TenantContextHolder.callWithIgnore(() -> {
            AiServiceCredential credential = credentialMapper.selectOne(new LambdaQueryWrapper<AiServiceCredential>()
                .eq(AiServiceCredential::getServiceName, serviceName)
                .eq(AiServiceCredential::getKeyVersion, keyVersion)
                .eq(AiServiceCredential::getStatus, STATUS_ENABLED), false);
            if (credential == null) {
                throw new ServiceException("Invalid AI service credential", 401);
            }
            return cryptoService.decrypt(credential.getSecretCiphertext());
        });
    }

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
}
