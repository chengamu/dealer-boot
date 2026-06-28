package com.bocoo.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.ai.domain.bo.AiProviderConfigBo;
import com.bocoo.ai.domain.bo.AiProviderCredentialBo;
import com.bocoo.ai.domain.bo.AiProviderModelBo;
import com.bocoo.ai.domain.bo.AiServiceCredentialGenerateBo;
import com.bocoo.ai.domain.bo.AiUserQuotaBo;
import com.bocoo.ai.domain.entity.AiAuditSummary;
import com.bocoo.ai.domain.entity.AiUsageLedger;
import com.bocoo.ai.domain.vo.AiProviderConfigVo;
import com.bocoo.ai.domain.vo.AiProviderModelVo;
import com.bocoo.ai.domain.vo.AiServiceCredentialSecretVo;
import com.bocoo.ai.domain.vo.AiServiceCredentialVo;
import com.bocoo.ai.domain.vo.AiUserQuotaVo;
import com.bocoo.ai.service.AiProviderConfigService;
import com.bocoo.ai.service.AiServiceCredentialService;
import com.bocoo.ai.service.AiUsageService;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/admin")
@Tag(name = "AI 管理", description = "AI platform configuration APIs")
public class AiAdminController {

    private final AiServiceCredentialService credentialService;
    private final AiProviderConfigService providerConfigService;
    private final AiUsageService usageService;

    @SaCheckPermission("ai:credential:manage")
    @GetMapping("/service-credentials")
    public R<List<AiServiceCredentialVo>> serviceCredentials() {
        return R.ok(credentialService.list());
    }

    @SaCheckPermission("ai:credential:manage")
    @Log(title = "AI 服务密钥", businessType = BusinessType.INSERT)
    @PostMapping("/service-credentials/generate")
    public R<AiServiceCredentialSecretVo> generateServiceCredential(@RequestBody(required = false) AiServiceCredentialGenerateBo bo) {
        return R.ok(credentialService.generate(bo == null ? new AiServiceCredentialGenerateBo() : bo));
    }

    @SaCheckPermission("ai:credential:manage")
    @Log(title = "AI 服务密钥", businessType = BusinessType.UPDATE)
    @PutMapping("/service-credentials/{credentialId}/disable")
    public R<Void> disableServiceCredential(@PathVariable Long credentialId) {
        credentialService.disable(credentialId);
        return R.ok();
    }

    @SaCheckPermission("ai:provider:manage")
    @GetMapping("/providers")
    public R<List<AiProviderConfigVo>> providers() {
        return R.ok(providerConfigService.list());
    }

    @SaCheckPermission("ai:provider:manage")
    @Log(title = "AI Provider", businessType = BusinessType.UPDATE)
    @PutMapping("/providers")
    public R<Void> saveProvider(@RequestBody AiProviderConfigBo bo) {
        providerConfigService.save(bo);
        return R.ok();
    }

    @SaCheckPermission("ai:provider:manage")
    @Log(title = "AI Provider Key", businessType = BusinessType.UPDATE)
    @PostMapping("/providers/api-key")
    public R<Void> saveProviderApiKey(@RequestBody AiProviderCredentialBo bo) {
        providerConfigService.saveApiKey(bo);
        return R.ok();
    }

    @SaCheckPermission("ai:provider:manage")
    @GetMapping("/providers/{providerCode}/models")
    public R<List<AiProviderModelVo>> providerModels(@PathVariable String providerCode) {
        return R.ok(providerConfigService.listModels(providerCode));
    }

    @SaCheckPermission("ai:provider:manage")
    @Log(title = "AI Provider Model", businessType = BusinessType.UPDATE)
    @PutMapping("/providers/models")
    public R<Void> saveProviderModel(@RequestBody AiProviderModelBo bo) {
        providerConfigService.saveModel(bo);
        return R.ok();
    }

    @SaCheckPermission("ai:provider:manage")
    @Log(title = "AI Provider Default Model", businessType = BusinessType.UPDATE)
    @PutMapping("/providers/{providerCode}/models/{modelId}/default")
    public R<Void> setDefaultProviderModel(@PathVariable String providerCode, @PathVariable Long modelId) {
        providerConfigService.setDefaultModel(providerCode, modelId);
        return R.ok();
    }

    @SaCheckPermission("ai:quota:manage")
    @GetMapping("/user-quotas")
    public R<List<AiUserQuotaVo>> userQuotas(
        @RequestParam(required = false) Long tenantId,
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) Integer limit
    ) {
        return R.ok(usageService.listUserQuotas(tenantId, userId, limit));
    }

    @SaCheckPermission("ai:quota:manage")
    @Log(title = "AI 用户额度", businessType = BusinessType.UPDATE)
    @PutMapping("/user-quotas")
    public R<Void> saveUserQuota(@RequestBody AiUserQuotaBo bo) {
        usageService.saveUserQuota(bo);
        return R.ok();
    }

    @SaCheckPermission("ai:usage:view")
    @GetMapping("/usage-ledgers")
    public R<List<AiUsageLedger>> usageLedgers(
        @RequestParam(required = false) Long tenantId,
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) Integer limit
    ) {
        return R.ok(usageService.listUsageLedgers(tenantId, userId, limit));
    }

    @SaCheckPermission("ai:audit:view")
    @GetMapping("/audit-summaries")
    public R<List<AiAuditSummary>> auditSummaries(
        @RequestParam(required = false) Long tenantId,
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) Integer limit
    ) {
        return R.ok(usageService.listAuditSummaries(tenantId, userId, limit));
    }
}
