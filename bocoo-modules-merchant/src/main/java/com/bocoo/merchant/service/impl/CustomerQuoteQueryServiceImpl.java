package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.merchant.domain.bo.CustomerQuoteBo;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportCnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportEnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import com.bocoo.merchant.service.CustomerQuoteQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerQuoteQueryServiceImpl extends MerchantServiceSupport implements CustomerQuoteQueryService {

    private final CustomerQuoteLoader loader;
    private final CustomerQuoteExportAssembler exportAssembler;
    private final CustomerQuoteQuerySupport querySupport;
    private final SalesOwnershipResolver ownershipResolver;

    @Override
    public TableDataInfo<CustomerQuoteVo> queryPageList(CustomerQuoteBo bo, PageQuery pageQuery) {
        return querySupport.queryBusiness(bo, pageQuery, currentTenantId(),
            ownershipResolver.currentBusinessOrigin());
    }

    @Override
    public CustomerQuoteVo queryById(Long id) {
        return loader.loadBusiness(id);
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

}
