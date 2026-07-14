package com.bocoo.dealer.quickorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;
import com.bocoo.dealer.quickorder.mapper.QuickOrderItemMapper;
import com.bocoo.dealer.quickorder.mapper.QuickOrderQueryMapper;
import com.bocoo.dealer.scope.SalesBusinessScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class QuickOrderAccess {
    private final QuickOrderQueryMapper mapper;
    private final QuickOrderItemMapper itemMapper;

    Long tenantId() {
        Long tenantId = LoginHelper.getTenantId();
        if (tenantId == null) throw ServiceException.ofMessageKey("tenant.context.missing");
        return tenantId;
    }

    QuickOrder load(Long id) {
        SalesBusinessScope scope = SalesBusinessScope.current();
        QuickOrder row = mapper.selectOne(new QueryWrapper<QuickOrder>().eq("del_flag", "0")
            .eq("tenant_id", scope.tenantId()).eq("business_origin", scope.businessOrigin())
            .eq("quick_order_id", id), false);
        if (row == null) throw ServiceException.ofMessageKey("dealer.quickOrder.notFound");
        return row;
    }

    QuickOrder loadDraft(Long id) {
        QuickOrder row = load(id);
        if (!"DRAFT".equals(row.getStatus())) {
            throw ServiceException.ofMessageKey("dealer.quickOrder.draftOnly");
        }
        return row;
    }

    List<QuickOrderItem> items(Long id, Long tenantId) {
        return itemMapper.selectList(new QueryWrapper<QuickOrderItem>().eq("del_flag", "0")
            .eq("tenant_id", tenantId).eq("quick_order_id", id)
            .orderByAsc("sort_order", "line_no", "quick_order_item_id"));
    }
}
