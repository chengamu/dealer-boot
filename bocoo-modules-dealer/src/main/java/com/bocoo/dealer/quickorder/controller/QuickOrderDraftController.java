package com.bocoo.dealer.quickorder.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.validate.AddGroup;
import com.bocoo.common.core.validate.EditGroup;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo;
import com.bocoo.dealer.quickorder.service.QuickOrderDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/quick-orders")
public class QuickOrderDraftController extends BaseController {
    private final QuickOrderDraftService service;

    @Log(title = "Quick Order", businessType = BusinessType.INSERT)
    @SaCheckPermission("dealer:quick-order:add")
    @PostMapping
    public R<Long> add(@Validated(AddGroup.class) @RequestBody QuickOrderBo bo) {
        return R.ok(service.insert(bo));
    }

    @Log(title = "Quick Order", businessType = BusinessType.UPDATE)
    @SaCheckPermission("dealer:quick-order:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody QuickOrderBo bo) {
        return toAjax(service.update(bo));
    }

    @Log(title = "Quick Order", businessType = BusinessType.DELETE)
    @SaCheckPermission("dealer:quick-order:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(service.delete(ids));
    }

    @Log(title = "Quick Order Copy", businessType = BusinessType.INSERT)
    @SaCheckPermission("dealer:quick-order:add")
    @PostMapping("/{id:\\d+}/copy")
    public R<Long> copy(@PathVariable Long id) {
        return R.ok(service.copy(id));
    }
}
