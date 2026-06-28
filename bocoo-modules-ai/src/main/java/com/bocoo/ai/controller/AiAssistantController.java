package com.bocoo.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.ai.domain.vo.AiBootstrapVo;
import com.bocoo.ai.service.AiAccessService;
import com.bocoo.common.core.domain.R;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
@Tag(name = "AI 助手", description = "AI assistant proxy APIs")
public class AiAssistantController {

    private final AiAccessService accessService;

    @GetMapping("/bootstrap")
    public R<AiBootstrapVo> bootstrap() {
        return R.ok(accessService.bootstrap());
    }

    @SaCheckPermission(AiAccessService.ASSISTANT_USE_PERMISSION)
    @PostMapping(value = "/page-agent/v1/chat/completions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void pageAgentChat(@RequestBody(required = false) String body, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_GONE, "Java AI proxy is disabled. Use /ai-runtime/page-agent/v1/chat/completions.");
    }
}
