package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.EngineeringCheckCaseBo;
import com.bocoo.product.domain.entity.EngineeringCheckCase;
import com.bocoo.product.domain.vo.EngineeringCheckCaseVo;
import com.bocoo.product.mapper.EngineeringCheckCaseMapper;
import com.bocoo.product.service.EngineeringCheckCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EngineeringCheckCaseServiceImpl extends ProductServiceSupport implements EngineeringCheckCaseService {

    private final EngineeringCheckCaseMapper checkCaseMapper;

    @Override
    public TableDataInfo<EngineeringCheckCaseVo> queryPageList(EngineeringCheckCaseBo query, PageQuery pageQuery) {
        QueryWrapper<EngineeringCheckCase> q = activeQuery(EngineeringCheckCase.class);
        if (query != null) {
            eq(q, "version_id", query.getVersionId());
            eq(q, "status", query.getStatus());
            like(q, "case_code", query.getCaseCode());
            if (StringUtils.isNotBlank(query.getCaseNameCn())) {
                q.and(wrapper -> wrapper.like("case_name_cn", query.getCaseNameCn()).or().like("case_name_en", query.getCaseNameCn()));
            }
        }
        return page(checkCaseMapper, pageQuery, q.orderByAsc("sort_order").orderByDesc("update_time"));
    }

    @Override
    public EngineeringCheckCaseVo queryById(Long id) {
        return checkCaseMapper.selectVoById(id);
    }

    @Override
    public Boolean save(EngineeringCheckCaseBo bo) {
        return saveEntity(checkCaseMapper, bo, EngineeringCheckCase::getCheckCaseId);
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        return remove(checkCaseMapper, ids);
    }
}
