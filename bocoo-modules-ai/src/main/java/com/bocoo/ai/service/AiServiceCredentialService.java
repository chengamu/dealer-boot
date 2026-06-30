package com.bocoo.ai.service;

import com.bocoo.ai.domain.bo.AiServiceCredentialBo;
import com.bocoo.ai.domain.bo.AiServiceCredentialGenerateBo;
import com.bocoo.ai.domain.vo.AiServiceCredentialSecretVo;
import com.bocoo.ai.domain.vo.AiServiceCredentialVo;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;

public interface AiServiceCredentialService {

    TableDataInfo<AiServiceCredentialVo> queryPageList(AiServiceCredentialBo bo, PageQuery pageQuery);

    AiServiceCredentialSecretVo generate(AiServiceCredentialGenerateBo bo);

    Boolean enable(Long credentialId);

    Boolean disable(Long credentialId);

    Boolean delete(Long credentialId);

    String findActiveSecret(String serviceName, String keyVersion);

    void markUsed(String serviceName, String keyVersion);
}
