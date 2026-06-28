package com.bocoo.ai.controller;

import com.bocoo.ai.domain.bo.AiAuthzCheckBo;
import com.bocoo.ai.domain.vo.AiRuntimeAuthzVo;
import com.bocoo.ai.domain.vo.AiRuntimeProviderVo;
import com.bocoo.ai.service.AiAccessService;
import com.bocoo.ai.service.AiProviderConfigService;
import com.bocoo.ai.service.AiServiceSignatureService;
import com.bocoo.common.core.domain.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/ai")
public class AiInternalRuntimeController {

    private final AiServiceSignatureService signatureService;
    private final AiAccessService accessService;
    private final AiProviderConfigService providerConfigService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/authz/check", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<AiRuntimeAuthzVo> checkAuthz(@RequestBody(required = false) String body, HttpServletRequest request) throws Exception {
        String payload = body == null ? "{}" : body;
        signatureService.verify(request, payload);
        AiAuthzCheckBo bo = objectMapper.readValue(payload, AiAuthzCheckBo.class);
        return R.ok(accessService.checkRuntimeAccess(bo));
    }

    @GetMapping("/runtime-config/provider")
    public R<AiRuntimeProviderVo> runtimeProvider(HttpServletRequest request) {
        signatureService.verify(request, "");
        return R.ok(providerConfigService.runtimeProvider());
    }
}
