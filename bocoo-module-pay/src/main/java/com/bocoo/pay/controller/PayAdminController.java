package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayNotifyTask;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayRefund;
import com.bocoo.pay.domain.entity.PayWallet;
import com.bocoo.pay.service.PayAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Payment management query endpoints.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/pay/admin")
@Tag(name = "支付管理", description = "支付管理查询接口")
public class PayAdminController extends BaseController {

    private final PayAdminService payAdminService;

    @SaCheckPermission("pay:channel:list")
    @GetMapping("/channel/list")
    @Operation(summary = "查询支付渠道列表")
    public TableDataInfo<PayChannel> channelList(PayChannel query, PageQuery pageQuery) {
        return payAdminService.selectChannelPage(query, pageQuery);
    }

    @SaCheckPermission("pay:order:list")
    @GetMapping("/order/list")
    @Operation(summary = "查询支付订单列表")
    public TableDataInfo<PayOrder> orderList(PayOrder query, PageQuery pageQuery) {
        return payAdminService.selectOrderPage(query, pageQuery);
    }

    @SaCheckPermission("pay:refund:list")
    @GetMapping("/refund/list")
    @Operation(summary = "查询支付退款列表")
    public TableDataInfo<PayRefund> refundList(PayRefund query, PageQuery pageQuery) {
        return payAdminService.selectRefundPage(query, pageQuery);
    }

    @SaCheckPermission("pay:notify:list")
    @GetMapping("/notify/task/list")
    @Operation(summary = "查询支付通知任务列表")
    public TableDataInfo<PayNotifyTask> notifyTaskList(PayNotifyTask query, PageQuery pageQuery) {
        return payAdminService.selectNotifyTaskPage(query, pageQuery);
    }

    @SaCheckPermission("pay:wallet:list")
    @GetMapping("/wallet/list")
    @Operation(summary = "查询支付钱包列表")
    public TableDataInfo<PayWallet> walletList(PayWallet query, PageQuery pageQuery) {
        return payAdminService.selectWalletPage(query, pageQuery);
    }
}
