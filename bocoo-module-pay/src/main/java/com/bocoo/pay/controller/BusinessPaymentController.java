package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.PaymentOrderQueryBo;
import com.bocoo.pay.domain.vo.PayOrderDetailVo;
import com.bocoo.pay.domain.vo.PaymentOrderSummaryVo;
import com.bocoo.pay.service.BusinessPaymentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/payments")
public class BusinessPaymentController {
    private final BusinessPaymentQueryService service;

    @SaCheckPermission("pay:order:list")
    @GetMapping("/list")
    public TableDataInfo<PaymentOrderSummaryVo> list(PaymentOrderQueryBo query, PageQuery pageQuery) {
        return service.list(query, pageQuery);
    }

    @SaCheckPermission("pay:order:query")
    @GetMapping("/{payOrderId}")
    public R<PayOrderDetailVo> detail(@PathVariable Long payOrderId) {
        return R.ok(service.detail(payOrderId));
    }
}
