package com.bocoo.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.ai.domain.bo.AiProviderConfigBo;
import com.bocoo.ai.domain.bo.AiProviderCredentialBo;
import com.bocoo.ai.domain.bo.AiProviderQueryBo;
import com.bocoo.ai.domain.vo.AiProviderConfigVo;
import com.bocoo.ai.service.AiProviderConfigService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/admin/providers")
@Tag(name = "AI Provider", description = "AI 模型渠道配置")
public class AiProviderController extends BaseController {

    private final AiProviderConfigService providerConfigService;

    @SaCheckPermission("ai:provider:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询 AI Provider")
    public TableDataInfo<AiProviderConfigVo> list(AiProviderQueryBo bo, PageQuery pageQuery) {
        return providerConfigService.queryProviderPageList(bo, pageQuery);
    }

    @SaCheckPermission("ai:provider:list")
    @GetMapping("/options")
    @Operation(summary = "查询 AI Provider 选项")
    public R<List<AiProviderConfigVo>> options() {
        return R.ok(providerConfigService.queryProviderOptions());
    }

    @SaCheckPermission("ai:provider:list")
    @GetMapping("/{providerId}")
    @Operation(summary = "查询 AI Provider 详情")
    public R<AiProviderConfigVo> getInfo(@PathVariable Long providerId) {
        return R.ok(providerConfigService.queryProviderById(providerId));
    }

    @SaCheckPermission("ai:provider:add")
    @Log(title = "AI Provider", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增 AI Provider")
    public R<Void> add(@Valid @RequestBody AiProviderConfigBo bo) {
        return toAjax(providerConfigService.insertProvider(bo));
    }

    @SaCheckPermission("ai:provider:edit")
    @Log(title = "AI Provider", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改 AI Provider")
    public R<Void> edit(@Valid @RequestBody AiProviderConfigBo bo) {
        return toAjax(providerConfigService.updateProvider(bo));
    }

    @SaCheckPermission("ai:provider:key")
    @Log(title = "AI Provider Key", businessType = BusinessType.UPDATE)
    @PutMapping("/{providerId}/api-key")
    @Operation(summary = "更新 AI Provider API Key")
    public R<Void> saveApiKey(@PathVariable Long providerId, @Valid @RequestBody AiProviderCredentialBo bo) {
        return toAjax(providerConfigService.saveApiKey(providerId, bo));
    }
}
