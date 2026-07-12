package com.bocoo.dealer.quickorder.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderSubmitBo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderSubmitResultVo;
import com.bocoo.dealer.quickorder.service.QuickOrderSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/quick-orders")
public class QuickOrderSubmitController extends BaseController {
    private final QuickOrderSubmissionService service;

    @Log(title = "Quick Order Submit", businessType = BusinessType.OTHER)
    @SaCheckPermission("dealer:quick-order:submit")
    @PostMapping("/{id:\\d+}/submit")
    public R<QuickOrderSubmitResultVo> submit(@PathVariable Long id,
                                              @Valid @RequestBody QuickOrderSubmitBo bo) {
        return R.ok(service.submit(id, bo));
    }
}
