package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.EngineeringPlanVersionBo;
import com.bocoo.product.domain.entity.EngineeringPlanVersion;
import com.bocoo.product.domain.vo.EngineeringPlanVersionVo;
import com.bocoo.product.mapper.EngineeringPlanVersionMapper;
import com.bocoo.product.service.EngineeringPlanVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EngineeringPlanVersionServiceImpl extends ProductServiceSupport implements EngineeringPlanVersionService {

    private final EngineeringPlanVersionMapper versionMapper;

    @Override
    public TableDataInfo<EngineeringPlanVersionVo> queryPageList(EngineeringPlanVersionBo query, PageQuery pageQuery) {
        QueryWrapper<EngineeringPlanVersion> q = activeQuery(EngineeringPlanVersion.class);
        if (query != null) {
            eq(q, "plan_id", query.getPlanId());
            eq(q, "version_id", query.getVersionId());
            like(q, "plan_code", query.getPlanCode());
            like(q, "version_no", query.getVersionNo());
            eq(q, "biz_status", query.getBizStatus());
            eq(q, "status", query.getStatus());
        }
        return page(versionMapper, pageQuery, q, wrapper -> wrapper.orderByDesc("update_time"));
    }

    @Override
    public EngineeringPlanVersionVo queryById(Long id) {
        return versionMapper.selectVoById(id);
    }

    @Override
    public Boolean save(EngineeringPlanVersionBo bo) {
        return saveEntity(versionMapper, bo, EngineeringPlanVersion::getVersionId);
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        return remove(versionMapper, ids);
    }
}
