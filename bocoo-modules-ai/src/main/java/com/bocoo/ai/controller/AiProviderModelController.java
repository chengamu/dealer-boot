package com.bocoo.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.ai.domain.bo.AiProviderModelBo;
import com.bocoo.ai.domain.bo.AiProviderModelQueryBo;
import com.bocoo.ai.domain.vo.AiProviderModelVo;
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

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/admin/provider-models")
@Tag(name = "AI Provider 模型", description = "AI 模型配置")
public class AiProviderModelController extends BaseController {

    private final AiProviderConfigService providerConfigService;

    @SaCheckPermission("ai:model:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询 AI 模型")
    public TableDataInfo<AiProviderModelVo> list(AiProviderModelQueryBo bo, PageQuery pageQuery) {
        return providerConfigService.queryModelPageList(bo, pageQuery);
    }

    @SaCheckPermission("ai:model:add")
    @Log(title = "AI 模型", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增 AI 模型")
    public R<Void> add(@Valid @RequestBody AiProviderModelBo bo) {
        return toAjax(providerConfigService.insertModel(bo));
    }

    @SaCheckPermission("ai:model:edit")
    @Log(title = "AI 模型", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改 AI 模型")
    public R<Void> edit(@Valid @RequestBody AiProviderModelBo bo) {
        return toAjax(providerConfigService.updateModel(bo));
    }

    @SaCheckPermission("ai:model:default")
    @Log(title = "AI 默认模型", businessType = BusinessType.UPDATE)
    @PutMapping("/{modelId}/default")
    @Operation(summary = "设置默认 AI 模型")
    public R<Void> setDefault(@PathVariable Long modelId) {
        return toAjax(providerConfigService.setDefaultModel(modelId));
    }
}
