package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.ReconciliationActionBo;
import com.bocoo.pay.domain.bo.ReconciliationCaseQueryBo;
import com.bocoo.pay.domain.vo.ReconciliationCaseDetailVo;
import com.bocoo.pay.domain.vo.ReconciliationCaseVo;
import com.bocoo.pay.service.PaymentReconciliationService;
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
@RequestMapping("/platform-finance/reconciliation-cases")
public class PlatformReconciliationController {
    private final PaymentReconciliationService service;

    @SaCheckPermission("platform:finance:reconciliation:list")
    @GetMapping("/list")
    public TableDataInfo<ReconciliationCaseVo> list(ReconciliationCaseQueryBo query, PageQuery pageQuery) {
        return service.list(query, pageQuery);
    }

    @SaCheckPermission("platform:finance:reconciliation:query")
    @GetMapping("/{caseId}")
    public R<ReconciliationCaseDetailVo> detail(@PathVariable Long caseId) {
        return R.ok(service.detail(caseId));
    }

    @SaCheckPermission("platform:finance:reconciliation:rescan")
    @PostMapping("/scan/{payOrderId}")
    public R<ReconciliationCaseVo> scan(@PathVariable Long payOrderId) {
        return R.ok(service.scanPayment(payOrderId));
    }

    @SaCheckPermission("platform:finance:reconciliation:rescan")
    @PostMapping("/{caseId}/rescan")
    public R<ReconciliationCaseVo> rescan(@PathVariable Long caseId, @Valid @RequestBody ReconciliationActionBo bo) {
        return R.ok(service.rescan(caseId, bo.getReason()));
    }

    @SaCheckPermission("platform:finance:reconciliation:channel")
    @PostMapping("/{caseId}/reconcile-channel")
    public R<ReconciliationCaseVo> reconcileChannel(@PathVariable Long caseId,
                                                      @Valid @RequestBody ReconciliationActionBo bo) {
        return R.ok(service.reconcileChannel(caseId, bo.getReason()));
    }

    @SaCheckPermission("platform:finance:reconciliation:repair")
    @PostMapping("/{caseId}/repair-order")
    public R<ReconciliationCaseVo> repairOrder(@PathVariable Long caseId,
                                                @Valid @RequestBody ReconciliationActionBo bo) {
        return R.ok(service.repairOrder(caseId, bo.getReason()));
    }

    @SaCheckPermission("platform:finance:reconciliation:ignore")
    @PostMapping("/{caseId}/ignore")
    public R<ReconciliationCaseVo> ignore(@PathVariable Long caseId,
                                           @Valid @RequestBody ReconciliationActionBo bo) {
        return R.ok(service.ignore(caseId, bo.getReason()));
    }
}
