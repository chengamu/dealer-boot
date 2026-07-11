package com.bocoo.merchant.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.merchant.domain.bo.CustomerQuoteEmailBo;
import com.bocoo.merchant.service.CustomerQuoteArtifactService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customer/quotes")
public class CustomerQuoteArtifactController {
    private final CustomerQuoteArtifactService service;

    @SaCheckPermission("customer:quote:document")
    @GetMapping("/{id:\\d+}/pdf")
    public void pdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        byte[] bytes = service.pdf(id);
        String filename = URLEncoder.encode("quotation-" + id + ".pdf", StandardCharsets.UTF_8);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + filename);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    @SaCheckPermission("customer:quote:email")
    @Log(title = "发送客户报价", businessType = BusinessType.OTHER)
    @PostMapping("/{id:\\d+}/email")
    public R<String> email(@PathVariable Long id, @Valid @RequestBody CustomerQuoteEmailBo bo) {
        return R.ok(service.sendEmail(id, bo));
    }
}
