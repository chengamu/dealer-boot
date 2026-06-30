package com.bocoo.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.ai.domain.bo.AiAuditSummaryBo;
import com.bocoo.ai.domain.vo.AiAuditSummaryVo;
import com.bocoo.ai.service.AiUsageService;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/admin/audit-summaries")
@Tag(name = "AI 审计查看", description = "AI 审计摘要")
public class AiAuditSummaryController {

    private final AiUsageService usageService;

    @SaCheckPermission("ai:audit:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询 AI 审计摘要")
    public TableDataInfo<AiAuditSummaryVo> list(AiAuditSummaryBo bo, PageQuery pageQuery) {
        return usageService.queryAuditSummaryPageList(bo, pageQuery);
    }
}
