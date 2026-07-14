package com.bocoo.dealer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.domain.bo.SalesDocumentBo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;
import com.bocoo.dealer.domain.vo.SalesDocumentExportVo;
import com.bocoo.dealer.service.PlatformSalesDocumentQueryService;
import com.bocoo.common.excel.utils.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/platform-sales/orders")
public class PlatformSalesDocumentQueryController {
    private final PlatformSalesDocumentQueryService service;

    @SaCheckPermission("platform:sales:order:list")
    @GetMapping("/list")
    public TableDataInfo<SalesDocumentVo> list(SalesDocumentBo bo, PageQuery pageQuery) {
        return service.queryPage(bo, pageQuery);
    }

    @SaCheckPermission("platform:sales:order:query")
    @GetMapping("/{id:\\d+}")
    public R<SalesDocumentVo> get(@PathVariable Long id) {
        return R.ok(service.queryById(id));
    }

    @SaCheckPermission("platform:sales:order:document")
    @GetMapping("/{id:\\d+}/pdf")
    public void pdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        byte[] bytes = service.pdf(id);
        String filename = URLEncoder.encode("order-" + id + ".pdf", StandardCharsets.UTF_8);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + filename);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    @SaCheckPermission("platform:sales:order:export")
    @org.springframework.web.bind.annotation.PostMapping("/{id:\\d+}/export")
    public void export(@PathVariable Long id, HttpServletResponse response) {
        ExcelUtil.exportExcel(service.export(id), "Sales Document", SalesDocumentExportVo.class, response);
    }
}
