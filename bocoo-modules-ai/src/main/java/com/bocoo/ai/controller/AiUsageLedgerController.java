package com.bocoo.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.ai.domain.bo.AiUsageLedgerBo;
import com.bocoo.ai.domain.vo.AiUsageLedgerVo;
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
@RequestMapping("/ai/admin/usage-ledgers")
@Tag(name = "AI 用量查看", description = "AI 用量流水")
public class AiUsageLedgerController {

    private final AiUsageService usageService;

    @SaCheckPermission("ai:usage:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询 AI 用量流水")
    public TableDataInfo<AiUsageLedgerVo> list(AiUsageLedgerBo bo, PageQuery pageQuery) {
        return usageService.queryUsageLedgerPageList(bo, pageQuery);
    }
}
