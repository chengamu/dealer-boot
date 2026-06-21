package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.StandardSkuEngineeringBo;
import com.bocoo.product.domain.entity.StandardSkuEngineering;
import com.bocoo.product.domain.vo.StandardSkuEngineeringVo;
import com.bocoo.product.mapper.StandardSkuEngineeringMapper;
import com.bocoo.product.service.StandardSkuEngineeringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StandardSkuEngineeringServiceImpl extends ProductServiceSupport implements StandardSkuEngineeringService {

    private final StandardSkuEngineeringMapper standardSkuEngineeringMapper;

    @Override
    public TableDataInfo<StandardSkuEngineeringVo> queryPageList(StandardSkuEngineeringBo query, PageQuery pageQuery) {
        QueryWrapper<StandardSkuEngineering> q = activeQuery(StandardSkuEngineering.class);
        if (query != null) {
            eq(q, "version_id", query.getVersionId());
            eq(q, "status", query.getStatus());
            like(q, "standard_sku_code", query.getStandardSkuCode());
            if (StringUtils.isNotBlank(query.getStandardSkuNameCn())) {
                q.and(wrapper -> wrapper.like("standard_sku_name_cn", query.getStandardSkuNameCn()).or().like("standard_sku_name_en", query.getStandardSkuNameCn()));
            }
        }
        return page(standardSkuEngineeringMapper, pageQuery, q, wrapper -> wrapper.orderByDesc("update_time").orderByAsc("standard_sku_code"));
    }

    @Override
    public StandardSkuEngineeringVo queryById(Long id) {
        return standardSkuEngineeringMapper.selectVoById(id);
    }

    @Override
    public Boolean save(StandardSkuEngineeringBo bo) {
        return saveEntity(standardSkuEngineeringMapper, bo, StandardSkuEngineering::getSkuEngineeringId);
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        return remove(standardSkuEngineeringMapper, ids);
    }
}
