package com.bocoo.product.service.impl;

import com.bocoo.product.domain.bo.ConfigEvaluationBo;
import com.bocoo.product.domain.bo.ConfigOptionBo;
import com.bocoo.product.domain.bo.ConfigQuestionBo;
import com.bocoo.product.domain.vo.ConfigEvaluationResultVo;
import com.bocoo.product.service.ConfigEvaluationEngine;
import com.bocoo.product.service.ConfigEvaluationService;
import com.bocoo.product.service.ConfigOptionService;
import com.bocoo.product.service.ConfigQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigEvaluationServiceImpl implements ConfigEvaluationService {

    private final ConfigQuestionService configQuestionService;
    private final ConfigOptionService configOptionService;
    private final ConfigEvaluationEngine configEvaluationEngine;

    @Override
    public ConfigEvaluationResultVo evaluate(ConfigEvaluationBo bo) {
        if (bo == null) {
            bo = new ConfigEvaluationBo();
        }
        ConfigQuestionBo questionBo = new ConfigQuestionBo();
        questionBo.setTemplateVersionId(bo.getTemplateVersionId());
        questionBo.setStatus("ENABLED");
        ConfigOptionBo optionBo = new ConfigOptionBo();
        optionBo.setTemplateVersionId(bo.getTemplateVersionId());
        optionBo.setStatus("ENABLED");
        return configEvaluationEngine.evaluate(bo, configQuestionService.queryList(questionBo), configOptionService.queryList(optionBo));
    }
}
