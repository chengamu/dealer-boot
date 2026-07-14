package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.merchant.domain.bo.CustomerQuoteBo;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportCnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportEnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import com.bocoo.merchant.service.PlatformCustomerQuoteQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformCustomerQuoteQueryServiceImpl extends MerchantServiceSupport
    implements PlatformCustomerQuoteQueryService {

    private final CustomerQuoteQuerySupport querySupport;
    private final CustomerQuoteLoader loader;
    private final CustomerQuoteExportAssembler exportAssembler;
    private final CustomerQuotePdfRenderer pdfRenderer;

    @Override
    public TableDataInfo<CustomerQuoteVo> queryPageList(CustomerQuoteBo bo, PageQuery pageQuery) {
        checkPlatformTenant();
        return querySupport.queryPlatform(bo, pageQuery);
    }

    @Override
    public CustomerQuoteVo queryById(Long id) {
        checkPlatformTenant();
        return loader.loadPlatform(id);
    }

    @Override
    public List<CustomerQuoteExportCnVo> exportCn(Long id) {
        return exportAssembler.cnRows(confirmed(id));
    }

    @Override
    public List<CustomerQuoteExportEnVo> exportEn(Long id) {
        return exportAssembler.enRows(confirmed(id));
    }

    @Override
    public byte[] pdf(Long id) {
        return pdfRenderer.render(confirmed(id));
    }

    private CustomerQuoteVo confirmed(Long id) {
        CustomerQuoteVo quote = queryById(id);
        if (!"CONFIRMED".equals(quote.getStatus())) {
            throw ServiceException.ofMessageKey("customer.quote.export.confirmedOnly");
        }
        return quote;
    }
}
