package com.bocoo.merchant.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.merchant.domain.bo.CustomerQuoteBo;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportCnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportEnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import com.bocoo.merchant.service.PlatformCustomerQuoteQueryService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/platform-sales/quotes")
public class PlatformCustomerQuoteController {

    private final PlatformCustomerQuoteQueryService queryService;

    @SaCheckPermission("platform:sales:quote:list")
    @GetMapping("/list")
    public TableDataInfo<CustomerQuoteVo> list(CustomerQuoteBo bo, PageQuery pageQuery) {
        return queryService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("platform:sales:quote:query")
    @GetMapping("/{id:\\d+}")
    public R<CustomerQuoteVo> get(@PathVariable Long id) {
        return R.ok(queryService.queryById(id));
    }

    @SaCheckPermission("platform:sales:quote:document")
    @GetMapping("/{id:\\d+}/pdf")
    public void pdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        byte[] bytes = queryService.pdf(id);
        String filename = URLEncoder.encode("quotation-" + id + ".pdf", StandardCharsets.UTF_8);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + filename);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    @SaCheckPermission("platform:sales:quote:export")
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
