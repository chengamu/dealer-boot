package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.CreditRepayBo;
import com.bocoo.pay.domain.bo.ReceivableQueryBo;
import com.bocoo.pay.domain.vo.PayReceivableSummaryVo;
import com.bocoo.pay.service.PlatformReceivableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/platform-finance/receivables")
public class PlatformReceivableController {
    private final PlatformReceivableService service;

    @SaCheckPermission("platform:finance:receivable:list")
    @GetMapping("/list")
    public TableDataInfo<PayReceivableSummaryVo> list(ReceivableQueryBo query, PageQuery pageQuery) {
        return service.list(query, pageQuery);
    }

    @SaCheckPermission("platform:finance:receivable:query")
    @GetMapping("/{receivableId}")
    public R<PayReceivableSummaryVo> detail(@PathVariable Long receivableId) {
        return R.ok(service.detail(receivableId));
    }

    @SaCheckPermission("platform:finance:receivable:repay")
    @PostMapping("/{receivableId}/repay")
    public R<PayReceivableSummaryVo> repay(@PathVariable Long receivableId, @Valid @RequestBody CreditRepayBo bo) {
        return R.ok(service.repay(receivableId, bo));
    }
}
