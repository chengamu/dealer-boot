package com.bocoo.product.service.impl;

import com.bocoo.product.domain.entity.EngineeringPlanVersion;
import com.bocoo.product.mapper.EngineeringPlanMapper;
import com.bocoo.product.mapper.EngineeringPlanVersionMapper;
import com.bocoo.product.service.EngineeringWorkbenchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EngineeringWorkbenchServiceImpl implements EngineeringWorkbenchService {

    private final EngineeringPlanMapper planMapper;
    private final EngineeringPlanVersionMapper versionMapper;
    private final EngineeringDataReader dataReader;

    @Override
    public Map<String, Object> load(Long versionId) {
        Map<String, Object> data = new LinkedHashMap<>();
        EngineeringPlanVersion version = versionMapper.selectById(versionId);
        data.put("version", version);
        data.put("plan", version == null ? null : planMapper.selectById(version.getPlanId()));
        data.put("items", dataReader.items(versionId));
        data.put("scopes", dataReader.scopes(versionId));
        data.put("rules", dataReader.rules(versionId));
        data.put("outputRules", dataReader.outputRules(versionId));
        data.put("standardSkus", dataReader.standardSkus(versionId));
        data.put("checkCases", dataReader.checkCases(versionId));
        return data;
    }
}
