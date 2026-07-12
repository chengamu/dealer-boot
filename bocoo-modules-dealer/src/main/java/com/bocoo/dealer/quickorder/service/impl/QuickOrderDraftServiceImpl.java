package com.bocoo.dealer.quickorder.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;
import com.bocoo.dealer.quickorder.mapper.QuickOrderItemMapper;
import com.bocoo.dealer.quickorder.mapper.QuickOrderMapper;
import com.bocoo.dealer.quickorder.service.QuickOrderDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuickOrderDraftServiceImpl implements QuickOrderDraftService {
    private final QuickOrderMapper mapper;
    private final QuickOrderItemMapper itemMapper;
    private final QuickOrderAccess access;
    private final QuickOrderHeaderFactory headerFactory;
    private final QuickOrderItemWriter itemWriter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insert(QuickOrderBo bo) {
        QuickOrder row = headerFactory.create(bo, access.tenantId());
        mapper.insert(row);
        QuickOrderItemWriteResult result = itemWriter.replace(row.getQuickOrderId(), row.getTenantId(), bo.getItems());
        QuickOrderTotals.apply(row, result.items(), result.currencyCode()); mapper.updateById(row);
        return row.getQuickOrderId();
    }

    @Override
    @Lock4j(name = "quick-order", keys = {"#bo.quickOrderId"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(QuickOrderBo bo) {
        QuickOrder row = access.loadDraft(bo.getQuickOrderId());
        headerFactory.update(row, bo);
        if (bo.getItems() != null) {
            QuickOrderItemWriteResult result = itemWriter.replace(row.getQuickOrderId(), row.getTenantId(), bo.getItems());
            QuickOrderTotals.apply(row, result.items(), result.currencyCode());
        }
        return mapper.updateById(row) > 0;
    }

    @Override
    @Lock4j(name = "quick-order", keys = {"#ids"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long[] ids) {
        List<Long> distinct = Arrays.stream(ids).distinct().toList();
        for (Long id : distinct) {
            QuickOrder row = access.loadDraft(id);
            itemMapper.delete(new QueryWrapper<QuickOrderItem>().eq("del_flag", "0")
                .eq("tenant_id", row.getTenantId()).eq("quick_order_id", id));
        }
        return mapper.delete(new QueryWrapper<QuickOrder>().eq("del_flag", "0")
            .eq("tenant_id", access.tenantId()).in("quick_order_id", distinct)) > 0;
    }

    @Override
    @Lock4j(name = "quick-order", keys = {"#id"})
    @Transactional(rollbackFor = Exception.class)
    public Long copy(Long id) {
        QuickOrder source = access.load(id);
        QuickOrderBo bo = copyHeader(source);
        QuickOrder target = headerFactory.create(bo, source.getTenantId());
        mapper.insert(target);
        itemWriter.copyPending(target.getQuickOrderId(), target.getTenantId(), access.items(id, source.getTenantId()));
        QuickOrderTotals.apply(target, List.of(), source.getCurrencyCode()); mapper.updateById(target);
        return target.getQuickOrderId();
    }

    private QuickOrderBo copyHeader(QuickOrder source) {
        QuickOrderBo bo = new QuickOrderBo();
        bo.setCustomerId(source.getCustomerId()); bo.setRecipientName(source.getRecipientName());
        bo.setRecipientPhone(source.getRecipientPhone()); bo.setShippingAddress(source.getShippingAddress());
        bo.setCustomerPoNo(source.getCustomerPoNo()); bo.setRemark(source.getRemark()); return bo;
    }

}
