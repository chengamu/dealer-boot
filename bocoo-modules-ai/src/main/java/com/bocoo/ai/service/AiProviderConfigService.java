package com.bocoo.ai.service;

import com.bocoo.ai.domain.bo.AiProviderConfigBo;
import com.bocoo.ai.domain.bo.AiProviderCredentialBo;
import com.bocoo.ai.domain.bo.AiProviderModelBo;
import com.bocoo.ai.domain.bo.AiProviderModelQueryBo;
import com.bocoo.ai.domain.bo.AiProviderQueryBo;
import com.bocoo.ai.domain.entity.AiProviderConfig;
import com.bocoo.ai.domain.vo.AiProviderConfigVo;
import com.bocoo.ai.domain.vo.AiProviderModelVo;
import com.bocoo.ai.domain.vo.AiRuntimeProviderVo;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;

import java.util.List;

public interface AiProviderConfigService {

    TableDataInfo<AiProviderConfigVo> queryProviderPageList(AiProviderQueryBo bo, PageQuery pageQuery);

    List<AiProviderConfigVo> queryProviderOptions();

    AiProviderConfigVo queryProviderById(Long providerId);

    Boolean insertProvider(AiProviderConfigBo bo);

    Boolean updateProvider(AiProviderConfigBo bo);

    Boolean saveApiKey(Long providerId, AiProviderCredentialBo bo);

    TableDataInfo<AiProviderModelVo> queryModelPageList(AiProviderModelQueryBo bo, PageQuery pageQuery);

    Boolean insertModel(AiProviderModelBo bo);

    Boolean updateModel(AiProviderModelBo bo);

    Boolean setDefaultModel(Long modelId);

    AiRuntimeProviderVo runtimeProvider();

    AiProviderConfig findEnabledProvider();
}
