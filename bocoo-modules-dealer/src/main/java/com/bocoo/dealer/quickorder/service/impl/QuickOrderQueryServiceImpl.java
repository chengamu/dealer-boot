package com.bocoo.dealer.quickorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderVo;
import com.bocoo.dealer.quickorder.mapper.QuickOrderQueryMapper;
import com.bocoo.dealer.quickorder.service.QuickOrderQueryService;
import com.bocoo.dealer.scope.SalesBusinessScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuickOrderQueryServiceImpl implements QuickOrderQueryService {
    private final QuickOrderQueryMapper mapper;
    private final QuickOrderAssembler assembler;

    @Override
    public TableDataInfo<QuickOrderVo> queryPage(QuickOrderBo bo, PageQuery pageQuery) {
        SalesBusinessScope scope = SalesBusinessScope.current();
        QueryWrapper<QuickOrder> query = QuickOrderQueryCriteria.apply(new QueryWrapper<QuickOrder>()
            .eq("del_flag", "0").eq("tenant_id", scope.tenantId())
            .eq("business_origin", scope.businessOrigin()), bo);
        if (pageQuery == null || StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            query.orderByDesc("update_time", "create_time");
        }
        IPage<QuickOrderVo> page = mapper.selectVoPage(pageQuery.build(), query);
        page.getRecords().forEach(assembler::fillSummary);
        return TableDataInfo.build(page);
    }

    @Override
    public QuickOrderVo queryById(Long id) {
        SalesBusinessScope scope = SalesBusinessScope.current();
        QuickOrderVo vo = mapper.selectVoOne(new QueryWrapper<QuickOrder>().eq("del_flag", "0")
            .eq("tenant_id", scope.tenantId()).eq("business_origin", scope.businessOrigin())
            .eq("quick_order_id", id), false);
        if (vo == null) throw com.bocoo.common.core.exception.ServiceException
            .ofMessageKey("dealer.quickOrder.notFound");
        assembler.fillDetail(vo);
        return vo;
    }
}
