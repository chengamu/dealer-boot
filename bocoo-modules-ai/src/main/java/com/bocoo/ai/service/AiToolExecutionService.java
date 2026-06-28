package com.bocoo.ai.service;

import com.bocoo.ai.domain.bo.AiToolExecuteBo;
import com.bocoo.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AiToolExecutionService {

    public Map<String, Object> execute(AiToolExecuteBo bo) {
        throw new ServiceException("AI business tool is not registered: " + bo.getToolCode(), 403);
    }
}
