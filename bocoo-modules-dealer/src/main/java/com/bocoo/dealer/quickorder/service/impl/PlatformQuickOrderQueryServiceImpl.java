package com.bocoo.dealer.quickorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderVo;
import com.bocoo.dealer.quickorder.domain.vo.PlatformQuickOrderExportVo;
import com.bocoo.dealer.quickorder.mapper.QuickOrderQueryMapper;
import com.bocoo.dealer.quickorder.service.PlatformQuickOrderQueryService;
import com.bocoo.dealer.scope.PlatformSalesGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformQuickOrderQueryServiceImpl implements PlatformQuickOrderQueryService {
    private static final int SUMMARY_BATCH_SIZE = 500;

    private final QuickOrderQueryMapper mapper;
    private final QuickOrderAssembler assembler;
    private final PlatformSalesGuard guard;

    @Override
    public TableDataInfo<QuickOrderVo> queryPage(QuickOrderBo bo, PageQuery pageQuery) {
        guard.requirePlatform();
        QueryWrapper<QuickOrder> query = QuickOrderQueryCriteria.apply(
            new QueryWrapper<QuickOrder>().eq("del_flag", "0"), bo);
        if (pageQuery == null || StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            query.orderByDesc("update_time", "quick_order_id");
        }
        IPage<QuickOrderVo> page = TenantContextHolder.callWithIgnore(() ->
            mapper.selectVoPage(pageQuery.build(), query));
        assembler.fillSummaries(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Override
    public QuickOrderVo queryById(Long id) {
        guard.requirePlatform();
        QuickOrderVo vo = TenantContextHolder.callWithIgnore(() -> mapper.selectVoOne(
            new QueryWrapper<QuickOrder>().eq("del_flag", "0").eq("quick_order_id", id), false));
        if (vo == null) throw ServiceException.ofMessageKey("dealer.quickOrder.notFound");
        assembler.fillDetail(vo);
        return vo;
    }

    @Override
    public List<PlatformQuickOrderExportVo> export(QuickOrderBo bo) {
        guard.requirePlatform();
        QueryWrapper<QuickOrder> query = QuickOrderQueryCriteria.apply(
            new QueryWrapper<QuickOrder>().eq("del_flag", "0"), bo)
            .orderByDesc("update_time", "quick_order_id");
        return exportBatches(query);
    }

    private List<PlatformQuickOrderExportVo> exportBatches(QueryWrapper<QuickOrder> query) {
        List<PlatformQuickOrderExportVo> result = new ArrayList<>();
        for (long pageNum = 1; ; pageNum++) {
            Page<QuickOrder> batch = new Page<>(pageNum, SUMMARY_BATCH_SIZE, false);
            IPage<QuickOrderVo> page = TenantContextHolder.callWithIgnore(() -> mapper.selectVoPage(batch, query));
            List<QuickOrderVo> rows = page.getRecords();
            if (rows.isEmpty()) break;
            assembler.fillSummaries(rows);
            rows.stream().map(this::exportRow).forEach(result::add);
            if (rows.size() < SUMMARY_BATCH_SIZE) break;
        }
        return result;
    }

    private PlatformQuickOrderExportVo exportRow(QuickOrderVo row) {
        PlatformQuickOrderExportVo value = new PlatformQuickOrderExportVo();
        value.setQuickOrderNo(row.getQuickOrderNo()); value.setBusinessOrigin(row.getBusinessOrigin());
        value.setTenantId(row.getTenantId()); value.setSalesStoreId(row.getSalesStoreId());
        value.setCustomerName(row.getCustomerName()); value.setOwnerName(row.getOwnerName());
        value.setStatus(row.getStatus()); value.setItemTypeCount(row.getItemTypeCount());
        value.setTotalQuantity(row.getTotalQuantity()); value.setTotalAmount(row.getTotalAmount());
        value.setOrderNo(row.getOrderNo()); value.setUpdateTime(row.getUpdateTime());
        return value;
    }
}
