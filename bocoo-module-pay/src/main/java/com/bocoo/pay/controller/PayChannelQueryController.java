package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.bocoo.common.core.domain.R;
import com.bocoo.pay.domain.vo.BankCollectionAccountVo;
import com.bocoo.pay.domain.vo.EnabledPayChannelVo;
import com.bocoo.pay.service.PayChannelQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pay/admin/channel")
public class PayChannelQueryController {
    private final PayChannelQueryService service;

    @SaCheckPermission(value = {"pay:channel:list", "pay:order:submit"}, mode = SaMode.OR)
    @GetMapping("/enabled")
    public R<List<EnabledPayChannelVo>> enabled(@RequestParam(required = false) Long payOrderId,
                                                 @RequestParam(required = false) Long appId) {
        return R.ok(service.enabledChannels(payOrderId, appId));
    }

    @SaCheckPermission(value = {"pay:channel:list", "pay:bank:submit"}, mode = SaMode.OR)
    @GetMapping({"/bank-account", "/bank-accounts"})
    public R<List<BankCollectionAccountVo>> bankAccounts(@RequestParam(required = false) Long payOrderId,
                                                          @RequestParam(required = false) Long appId,
                                                          @RequestParam(required = false) String currency) {
        return R.ok(service.bankAccounts(payOrderId, appId, currency));
    }
}
