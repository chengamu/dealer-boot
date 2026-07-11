package com.bocoo.dealer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.dealer.domain.bo.SalesDocumentEmailBo;
import com.bocoo.dealer.domain.vo.SalesDocumentExportVo;
import com.bocoo.dealer.service.SalesDocumentArtifactService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/sales-documents")
public class SalesDocumentArtifactController {
    private final SalesDocumentArtifactService service;

    @SaCheckPermission("dealer:sales:document")
    @GetMapping("/{id:\\d+}/pdf")
    public void pdf(@PathVariable Long id, @RequestParam(defaultValue = "ORDER") String type,
                    HttpServletResponse response) throws IOException {
        byte[] bytes = service.pdf(id, type);
        String filename = URLEncoder.encode(type.toLowerCase() + "-" + id + ".pdf", StandardCharsets.UTF_8);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + filename);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    @SaCheckPermission("dealer:sales:email")
    @PostMapping("/{id:\\d+}/email")
    public R<String> email(@PathVariable Long id, @Valid @RequestBody SalesDocumentEmailBo bo) {
        return R.ok(service.sendEmail(id, bo));
    }

    @SaCheckPermission("dealer:sales:document")
    @PostMapping("/{id:\\d+}/export")
    public void export(@PathVariable Long id, HttpServletResponse response) {
        ExcelUtil.exportExcel(service.export(id), "Sales Document", SalesDocumentExportVo.class, response);
    }
}
