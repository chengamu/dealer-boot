package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.idempotent.annotation.RepeatSubmit;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.ratelimiter.annotation.RateLimiter;
import com.bocoo.common.ratelimiter.enums.LimitType;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.system.domain.bo.MerchantEmailCodeBo;
import com.bocoo.system.domain.bo.SysTenantApplyBo;
import com.bocoo.system.domain.vo.SysTenantApplyVo;
import com.bocoo.system.service.SysTenantApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RestController
@Tag(name = "Tenant application", description = "Merchant tenant application APIs")
public class SysTenantApplyController extends BaseController {

    private final SysTenantApplyService tenantApplyService;

    @SaIgnore
    @PostMapping("/merchant/applications/email-code")
    @RateLimiter(count = 5, time = 60, limitType = LimitType.IP)
    @Operation(summary = "Send merchant application email verification code")
    public R<Void> sendEmailCode(@Validated @RequestBody MerchantEmailCodeBo bo) {
        tenantApplyService.sendEmailCode(bo.getEmail());
        return R.ok();
    }

    @SaIgnore
    @PostMapping("/merchant/applications")
    @RateLimiter(count = 3, time = 60, limitType = LimitType.IP)
    @RepeatSubmit()
    @Operation(summary = "Submit merchant application")
    public R<Void> submit(@Validated @RequestBody SysTenantApplyBo bo) {
        tenantApplyService.submit(bo);
        return R.ok();
    }

    @SaCheckPermission("system:tenant:application:list")
    @GetMapping("/system/tenant/applications")
    @Operation(summary = "List merchant applications")
    public TableDataInfo<SysTenantApplyVo> list(SysTenantApplyBo bo, PageQuery pageQuery) {
        return tenantApplyService.selectPage(bo, pageQuery);
    }

    @SaCheckPermission("system:tenant:application:query")
    @GetMapping("/system/tenant/applications/{id}")
    @Operation(summary = "Get merchant application")
    public R<SysTenantApplyVo> get(@PathVariable("id") Long id) {
        return R.ok(tenantApplyService.selectById(id));
    }

    @SaCheckPermission("system:tenant:application:audit")
    @PostMapping("/system/tenant/applications/{id}/approve")
    @RepeatSubmit()
    @Operation(summary = "Approve merchant application")
    public R<Void> approve(@PathVariable("id") Long id) {
        tenantApplyService.approve(id);
        return R.ok();
    }

    @SaCheckPermission("system:tenant:application:audit")
    @PostMapping("/system/tenant/applications/{id}/reject")
    @RepeatSubmit()
    @Operation(summary = "Reject merchant application")
    public R<Void> reject(@PathVariable("id") Long id, @RequestBody SysTenantApplyBo bo) {
        tenantApplyService.reject(id, bo);
        return R.ok();
    }
}
