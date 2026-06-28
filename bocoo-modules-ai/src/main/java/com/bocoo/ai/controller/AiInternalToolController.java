package com.bocoo.ai.controller;

import com.bocoo.ai.domain.bo.AiAuditReportBo;
import com.bocoo.ai.domain.bo.AiToolExecuteBo;
import com.bocoo.ai.domain.bo.AiUsageReportBo;
import com.bocoo.ai.service.AiServiceSignatureService;
import com.bocoo.ai.service.AiToolExecutionService;
import com.bocoo.ai.service.AiUsageService;
import com.bocoo.common.core.domain.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/ai")
@Tag(name = "AI 内部工具", description = "AI runtime internal callback APIs")
public class AiInternalToolController {

    private final AiServiceSignatureService signatureService;
    private final AiToolExecutionService toolExecutionService;
    private final AiUsageService usageService;
    private final ObjectMapper objectMapper;

    @PostMapping("/tools/execute")
    public R<Map<String, Object>> executeTool(
        @RequestBody(required = false) String body,
        HttpServletRequest request) throws Exception {
        String payload = body == null ? "{}" : body;
        signatureService.verify(request, payload);
        AiToolExecuteBo bo = objectMapper.readValue(payload, AiToolExecuteBo.class);
        return R.ok(toolExecutionService.execute(bo));
    }

    @PostMapping("/usage/report")
    public R<Void> reportUsage(
        @RequestBody(required = false) String body,
        HttpServletRequest request) throws Exception {
        String payload = body == null ? "{}" : body;
        signatureService.verify(request, payload);
        AiUsageReportBo bo = objectMapper.readValue(payload, AiUsageReportBo.class);
        usageService.reportUsage(bo);
        return R.ok();
    }

    @PostMapping("/audit/report")
    public R<Void> reportAudit(
        @RequestBody(required = false) String body,
        HttpServletRequest request) throws Exception {
        String payload = body == null ? "{}" : body;
        signatureService.verify(request, payload);
        AiAuditReportBo bo = objectMapper.readValue(payload, AiAuditReportBo.class);
        usageService.reportAudit(bo);
        return R.ok();
    }
}
