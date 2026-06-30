package com.bocoo.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.ai.domain.bo.AiServiceCredentialBo;
import com.bocoo.ai.domain.bo.AiServiceCredentialGenerateBo;
import com.bocoo.ai.domain.vo.AiServiceCredentialSecretVo;
import com.bocoo.ai.domain.vo.AiServiceCredentialVo;
import com.bocoo.ai.service.AiServiceCredentialService;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/admin/service-credentials")
@Tag(name = "AI 服务密钥", description = "AI Runtime 调 Java 的服务密钥管理")
public class AiServiceCredentialController extends BaseController {

    private final AiServiceCredentialService credentialService;

    @SaCheckPermission("ai:credential:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询 AI 服务密钥")
    public TableDataInfo<AiServiceCredentialVo> list(AiServiceCredentialBo bo, PageQuery pageQuery) {
        return credentialService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("ai:credential:generate")
    @Log(title = "AI 服务密钥", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    @Operation(summary = "生成 AI 服务密钥")
    public R<AiServiceCredentialSecretVo> generate(@RequestBody(required = false) AiServiceCredentialGenerateBo bo) {
        return R.ok(credentialService.generate(bo == null ? new AiServiceCredentialGenerateBo() : bo));
    }

    @SaCheckPermission("ai:credential:enable")
    @Log(title = "AI 服务密钥", businessType = BusinessType.UPDATE)
    @PutMapping("/{credentialId}/enable")
    @Operation(summary = "启用 AI 服务密钥")
    public R<Void> enable(@PathVariable Long credentialId) {
        return toAjax(credentialService.enable(credentialId));
    }

    @SaCheckPermission("ai:credential:disable")
    @Log(title = "AI 服务密钥", businessType = BusinessType.UPDATE)
    @PutMapping("/{credentialId}/disable")
    @Operation(summary = "禁用 AI 服务密钥")
    public R<Void> disable(@PathVariable Long credentialId) {
        return toAjax(credentialService.disable(credentialId));
    }

    @SaCheckPermission("ai:credential:remove")
    @Log(title = "AI 服务密钥", businessType = BusinessType.DELETE)
    @DeleteMapping("/{credentialId}")
    @Operation(summary = "删除 AI 服务密钥")
    public R<Void> delete(@PathVariable Long credentialId) {
        return toAjax(credentialService.delete(credentialId));
    }
}
