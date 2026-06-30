package com.bocoo.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.ai.domain.bo.AiUserQuotaBo;
import com.bocoo.ai.domain.vo.AiUserQuotaVo;
import com.bocoo.ai.service.AiUsageService;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/admin/user-quotas")
@Tag(name = "AI 用户额度", description = "AI 用户用量额度")
public class AiUserQuotaController extends BaseController {

    private final AiUsageService usageService;

    @SaCheckPermission("ai:quota:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询 AI 用户额度")
    public TableDataInfo<AiUserQuotaVo> list(AiUserQuotaBo bo, PageQuery pageQuery) {
        return usageService.queryUserQuotaPageList(bo, pageQuery);
    }

    @SaCheckPermission("ai:quota:add")
    @Log(title = "AI 用户额度", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增 AI 用户额度")
    public R<Void> add(@Valid @RequestBody AiUserQuotaBo bo) {
        return toAjax(usageService.insertUserQuota(bo));
    }

    @SaCheckPermission("ai:quota:edit")
    @Log(title = "AI 用户额度", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改 AI 用户额度")
    public R<Void> edit(@Valid @RequestBody AiUserQuotaBo bo) {
        return toAjax(usageService.updateUserQuota(bo));
    }
}
