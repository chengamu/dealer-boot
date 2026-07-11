package com.bocoo.dealer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.dealer.domain.bo.CustomerQuoteConvertOrderBo;
import com.bocoo.dealer.domain.vo.CustomerQuoteOrderPreviewVo;
import com.bocoo.dealer.domain.vo.CustomerQuoteOrderResultVo;
import com.bocoo.dealer.service.CustomerQuoteOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customer/quotes")
public class CustomerQuoteOrderController {
    private final CustomerQuoteOrderService service;

    @SaCheckPermission("customer:quote:convert")
    @PostMapping("/{id:\\d+}/order-preview")
    public R<CustomerQuoteOrderPreviewVo> preview(@PathVariable Long id) {
        return R.ok(service.preview(id));
    }

    @SaCheckPermission("customer:quote:convert")
    @Log(title = "客户报价转销售订单", businessType = BusinessType.INSERT)
    @PostMapping("/{id:\\d+}/convert-order")
    public R<CustomerQuoteOrderResultVo> convert(@PathVariable Long id,
                                                 @Valid @RequestBody CustomerQuoteConvertOrderBo bo) {
        return R.ok(service.convert(id, bo));
    }
}
