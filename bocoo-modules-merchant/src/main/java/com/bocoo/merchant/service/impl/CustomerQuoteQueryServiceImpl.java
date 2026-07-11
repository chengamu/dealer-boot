package com.bocoo.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.merchant.domain.bo.CustomerQuoteBo;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportCnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportEnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import com.bocoo.merchant.mapper.CustomerQuoteItemMapper;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import com.bocoo.merchant.service.CustomerQuoteQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerQuoteQueryServiceImpl extends MerchantServiceSupport implements CustomerQuoteQueryService {

    private final CustomerQuoteMapper quoteMapper;
    private final CustomerQuoteItemMapper itemMapper;
    private final CustomerQuoteLoader loader;
    private final CustomerQuoteExportAssembler exportAssembler;

    @Override
    public TableDataInfo<CustomerQuoteVo> queryPageList(CustomerQuoteBo bo, PageQuery pageQuery) {
        QueryWrapper<CustomerQuote> query = activeQuery();
        query.eq("tenant_id", currentTenantId());
        if (bo != null) {
            like(query, "quote_no", bo.getQuoteNo());
            like(query, "project_name", bo.getProjectName());
            eq(query, "customer_id", bo.getCustomerId());
            eq(query, "status", bo.getStatus());
        }
        TableDataInfo<CustomerQuoteVo> result = page(quoteMapper, pageQuery, query,
            q -> q.orderByDesc("update_time", "create_time"));
        result.getRows().forEach(row -> row.setItemCount(itemMapper.selectCount(activeItems(row.getQuoteId())).intValue()));
        return result;
    }

    @Override
    public CustomerQuoteVo queryById(Long id) {
        return loader.load(id);
    }

    @Override
    public List<CustomerQuoteExportCnVo> exportCn(Long id) {
        CustomerQuoteVo quote = confirmedQuote(id);
        return exportAssembler.cnRows(quote);
    }

    @Override
    public List<CustomerQuoteExportEnVo> exportEn(Long id) {
        return exportAssembler.enRows(confirmedQuote(id));
    }

    private CustomerQuoteVo confirmedQuote(Long id) {
        CustomerQuoteVo quote = queryById(id);
        if (!"CONFIRMED".equals(quote.getStatus())) {
            throw ServiceException.ofMessageKey("customer.quote.export.confirmedOnly");
        }
        return quote;
    }

    private QueryWrapper<CustomerQuoteItem> activeItems(Long quoteId) {
        return this.<CustomerQuoteItem>activeQuery()
            .eq("tenant_id", currentTenantId()).eq("quote_id", quoteId);
    }
}
