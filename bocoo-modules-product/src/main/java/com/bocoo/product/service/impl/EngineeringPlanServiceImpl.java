package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.EngineeringPlanBo;
import com.bocoo.product.domain.entity.EngineeringPlan;
import com.bocoo.product.domain.vo.EngineeringPlanVo;
import com.bocoo.product.mapper.EngineeringPlanMapper;
import com.bocoo.product.service.EngineeringPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EngineeringPlanServiceImpl extends ProductServiceSupport implements EngineeringPlanService {

    private final EngineeringPlanMapper planMapper;

    @Override
    public TableDataInfo<EngineeringPlanVo> queryPageList(EngineeringPlanBo query, PageQuery pageQuery) {
        return page(planMapper, pageQuery, buildQueryWrapper(query), q -> q.orderByDesc("update_time"));
    }

    @Override
    public List<EngineeringPlanVo> queryList(EngineeringPlanBo query) {
        return planMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(query), q -> q.orderByDesc("update_time")));
    }

    @Override
    public EngineeringPlanVo queryById(Long id) {
        return planMapper.selectVoById(id);
    }

    @Override
    public Boolean save(EngineeringPlanBo bo) {
        return saveEntity(planMapper, bo, EngineeringPlan::getPlanId);
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        return remove(planMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return planMapper.update(null, new LambdaUpdateWrapper<EngineeringPlan>()
            .eq(EngineeringPlan::getPlanId, id)
            .set(EngineeringPlan::getStatus, status)) > 0;
    }

    private QueryWrapper<EngineeringPlan> buildQueryWrapper(EngineeringPlan query) {
        QueryWrapper<EngineeringPlan> q = activeQuery(EngineeringPlan.class);
        if (query != null) {
            like(q, "plan_code", query.getPlanCode());
            if (StringUtils.isNotBlank(query.getPlanNameCn())) {
                q.and(wrapper -> wrapper.like("plan_name_cn", query.getPlanNameCn()).or().like("plan_name_en", query.getPlanNameCn()));
            }
            like(q, "series_code", query.getSeriesCode());
            eq(q, "biz_status", query.getBizStatus());
            eq(q, "status", query.getStatus());
        }
        return q;
    }
}
