package com.bocoo.merchant.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportCnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportEnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import com.bocoo.merchant.service.CustomerQuoteLifecycleService;
import com.bocoo.merchant.service.CustomerQuoteQueryService;
import com.bocoo.common.satoken.utils.LoginHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/customer/quotes")
public class CustomerQuoteLifecycleController extends BaseController {

    private final CustomerQuoteLifecycleService lifecycleService;
    private final CustomerQuoteQueryService queryService;

    @SaCheckPermission("customer:quote:edit")
    @Log(title = "确认客户报价", businessType = BusinessType.UPDATE)
    @PutMapping("/{id:\\d+}/confirm")
    public com.bocoo.common.core.domain.R<Void> confirm(@PathVariable Long id) {
        return toAjax(lifecycleService.confirm(LoginHelper.getTenantId(), id));
    }

    @SaCheckPermission("customer:quote:edit")
    @Log(title = "作废客户报价", businessType = BusinessType.UPDATE)
    @PutMapping("/{id:\\d+}/void")
    public com.bocoo.common.core.domain.R<Void> voidQuote(@PathVariable Long id) {
        return toAjax(lifecycleService.voidQuote(id));
    }

    @SaCheckPermission("customer:quote:export")
    @Log(title = "导出客户报价", businessType = BusinessType.EXPORT)
    @PostMapping("/{id:\\d+}/export")
    public void export(@PathVariable Long id, HttpServletResponse response) {
        CustomerQuoteVo quote = queryService.queryById(id);
        if ("EN_US".equals(quote.getQuoteLanguage())) {
            ExcelUtil.exportExcel(queryService.exportEn(id), "Quotation", CustomerQuoteExportEnVo.class, response);
            return;
        }
        ExcelUtil.exportExcel(queryService.exportCn(id), "客户报价", CustomerQuoteExportCnVo.class, response);
    }
}
