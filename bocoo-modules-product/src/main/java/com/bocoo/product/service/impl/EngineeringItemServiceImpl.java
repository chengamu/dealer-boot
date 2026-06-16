package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.EngineeringItemBo;
import com.bocoo.product.domain.entity.EngineeringItem;
import com.bocoo.product.domain.vo.EngineeringItemVo;
import com.bocoo.product.mapper.EngineeringItemMapper;
import com.bocoo.product.service.EngineeringItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EngineeringItemServiceImpl extends ProductServiceSupport implements EngineeringItemService {

    private final EngineeringItemMapper itemMapper;

    @Override
    public TableDataInfo<EngineeringItemVo> queryPageList(EngineeringItemBo query, PageQuery pageQuery) {
        QueryWrapper<EngineeringItem> q = activeQuery(EngineeringItem.class);
        if (query != null) {
            eq(q, "version_id", query.getVersionId());
            eq(q, "status", query.getStatus());
            like(q, "item_code", query.getItemCode());
            if (StringUtils.isNotBlank(query.getItemNameCn())) {
                q.and(wrapper -> wrapper.like("item_name_cn", query.getItemNameCn()).or().like("item_name_en", query.getItemNameCn()));
            }
        }
        return page(itemMapper, pageQuery, q.orderByAsc("sort_order").orderByDesc("update_time"));
    }

    @Override
    public EngineeringItemVo queryById(Long id) {
        return itemMapper.selectVoById(id);
    }

    @Override
    public Boolean save(EngineeringItemBo bo) {
        return saveEntity(itemMapper, bo, EngineeringItem::getItemId);
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        return remove(itemMapper, ids);
    }
}
