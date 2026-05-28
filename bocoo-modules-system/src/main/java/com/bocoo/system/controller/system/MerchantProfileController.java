package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.system.domain.bo.MerchantProfileBo;
import com.bocoo.system.domain.vo.MerchantProfileVo;
import com.bocoo.system.service.MerchantProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/merchant/profile")
@Tag(name = "Merchant profile", description = "Merchant profile APIs")
public class MerchantProfileController extends BaseController {

    private final MerchantProfileService merchantProfileService;

    @SaCheckPermission("system:merchant:profile:list")
    @GetMapping("/list")
    @Operation(summary = "List merchant profiles")
    public TableDataInfo<MerchantProfileVo> list(MerchantProfileBo bo, PageQuery pageQuery) {
        return merchantProfileService.selectPage(bo, pageQuery);
    }

    @SaCheckPermission("merchant:profile:query")
    @GetMapping("/current")
    @Operation(summary = "Get current merchant profile")
    public R<MerchantProfileVo> current() {
        return R.ok(merchantProfileService.selectCurrent());
    }

    @SaCheckPermission("merchant:profile:edit")
    @Log(title = "Merchant profile", businessType = BusinessType.UPDATE)
    @PutMapping("/current")
    @Operation(summary = "Update current merchant profile")
    public R<Void> updateCurrent(@Validated @RequestBody MerchantProfileBo bo) {
        return toAjax(merchantProfileService.updateCurrent(bo));
    }

    @SaCheckPermission("system:merchant:profile:query")
    @GetMapping("/{merchantId}")
    @Operation(summary = "Get merchant profile")
    public R<MerchantProfileVo> get(@PathVariable("merchantId") Long merchantId) {
        return R.ok(merchantProfileService.selectById(merchantId));
    }

    @SaCheckPermission("system:merchant:profile:edit")
    @Log(title = "Merchant profile", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "Update merchant profile editable fields")
    public R<Void> update(@Validated @RequestBody MerchantProfileBo bo) {
        return toAjax(merchantProfileService.updateById(bo));
    }
}
