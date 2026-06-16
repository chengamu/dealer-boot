package com.bocoo.product.service;

import com.bocoo.product.domain.bo.ConfigEvaluationBo;
import com.bocoo.product.domain.vo.ConfigEvaluationResultVo;

public interface ConfigEvaluationService {

    ConfigEvaluationResultVo evaluate(ConfigEvaluationBo bo);
}
