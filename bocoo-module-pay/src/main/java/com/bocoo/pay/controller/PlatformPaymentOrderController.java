package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.PaySupplementBo;
import com.bocoo.pay.domain.bo.PaymentOrderQueryBo;
import com.bocoo.pay.domain.vo.PayAttemptVo;
import com.bocoo.pay.domain.vo.PayOrderDetailVo;
import com.bocoo.pay.domain.vo.PaymentOrderSummaryVo;
import com.bocoo.pay.service.PlatformPaymentOrderService;
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
@RequestMapping("/platform-finance/payment-orders")
public class PlatformPaymentOrderController {
    private final PlatformPaymentOrderService service;

    @SaCheckPermission("platform:finance:payment:list")
    @GetMapping("/list")
    public TableDataInfo<PaymentOrderSummaryVo> list(PaymentOrderQueryBo query, PageQuery pageQuery) {
        return service.list(query, pageQuery);
    }

    @SaCheckPermission("platform:finance:payment:query")
    @GetMapping("/{payOrderId}")
    public R<PayOrderDetailVo> detail(@PathVariable Long payOrderId) {
        return R.ok(service.detail(payOrderId));
    }

    @SaCheckPermission("platform:finance:payment:supplement")
    @PostMapping("/{payOrderId}/supplement")
    public R<PayAttemptVo> supplement(@PathVariable Long payOrderId, @Valid @RequestBody PaySupplementBo bo) {
        return R.ok(service.supplement(payOrderId, bo));
    }
}
