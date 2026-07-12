package com.bocoo.dealer.quickorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderItemVo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderVo;
import com.bocoo.dealer.quickorder.mapper.QuickOrderItemMapper;
import com.bocoo.dealer.quickorder.mapper.QuickOrderMapper;
import com.bocoo.dealer.quickorder.runtime.QuickOrderJson;
import com.bocoo.dealer.quickorder.service.QuickOrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuickOrderQueryServiceImpl implements QuickOrderQueryService {
    private final QuickOrderMapper mapper;
    private final QuickOrderItemMapper itemMapper;
    private final QuickOrderAccess access;
    private final QuickOrderJson json;

    @Override
    public TableDataInfo<QuickOrderVo> queryPage(QuickOrderBo bo, PageQuery pageQuery) {
        QueryWrapper<QuickOrder> query = query(bo);
        if (pageQuery == null || StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            query.orderByDesc("update_time", "create_time");
        }
        IPage<QuickOrderVo> page = mapper.selectVoPage(pageQuery.build(), query);
        page.getRecords().forEach(this::fillSummary);
        return TableDataInfo.build(page);
    }

    @Override
    public QuickOrderVo queryById(Long id) {
        QuickOrder current = access.load(id);
        QuickOrderVo vo = mapper.selectVoById(current.getQuickOrderId());
        if (vo == null) throw ServiceException.ofMessageKey("dealer.quickOrder.notFound");
        List<QuickOrderItemVo> items = itemMapper.selectVoList(new QueryWrapper<QuickOrderItem>()
            .eq("del_flag", "0").eq("tenant_id", current.getTenantId()).eq("quick_order_id", id)
            .orderByAsc("sort_order", "line_no", "quick_order_item_id"));
        items.forEach(item -> {
            QuickOrderItem entity = itemMapper.selectById(item.getQuickOrderItemId());
            item.setSelectedOptionValues(json.readSelections(entity.getSelectedOptionsJson()));
        });
        vo.setItems(items); fillSummary(vo); return vo;
    }

    private QueryWrapper<QuickOrder> query(QuickOrderBo bo) {
        QueryWrapper<QuickOrder> query = new QueryWrapper<QuickOrder>().eq("del_flag", "0")
            .eq("tenant_id", access.tenantId());
        if (bo == null) return query;
        query.like(StringUtils.isNotBlank(bo.getQuickOrderNo()), "quick_order_no", bo.getQuickOrderNo());
        query.eq(bo.getCustomerId() != null, "customer_id", bo.getCustomerId());
        query.like(StringUtils.isNotBlank(bo.getCustomerName()), "customer_name", bo.getCustomerName());
        query.eq(StringUtils.isNotBlank(bo.getStatus()), "status", bo.getStatus());
        query.eq(StringUtils.isNotBlank(bo.getCreateBy()), "create_by", bo.getCreateBy());
        query.ge(bo.getUpdateTimeStart() != null, "update_time", bo.getUpdateTimeStart());
        query.le(bo.getUpdateTimeEnd() != null, "update_time", bo.getUpdateTimeEnd());
        return query;
    }

    private void fillSummary(QuickOrderVo vo) {
        List<QuickOrderItem> items = access.items(vo.getQuickOrderId(), access.tenantId());
        vo.setItemTypeCount((int) items.stream().map(QuickOrderItem::getSaleProductId).distinct().count());
        vo.setTotalQuantity(items.stream().map(QuickOrderItem::getQuantity).filter(java.util.Objects::nonNull)
            .reduce(0, Integer::sum));
    }
}
