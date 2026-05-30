package com.bocoo.pay.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.bocoo.common.core.constant.TenantConstants;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.pay.domain.bo.PayOrderCreateBo;
import com.bocoo.pay.domain.bo.PaySubmitBo;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.vo.PaySubmitVo;
import com.bocoo.pay.enums.PayChannelCode;
import com.bocoo.pay.service.PayOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Local payment smoke endpoint. This controller is not active in prod.
 */
@Profile({"dev", "local"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/pay/mock")
@Tag(name = "支付 Mock Smoke", description = "支付 Mock Smoke 接口")
public class PayMockSmokeController {

    private static final String DEFAULT_APP_KEY = "platform-default";

    private final PayOrderService payOrderService;

    @PostMapping("/smoke")
    @Operation(summary = "执行 Mock 支付 smoke", description = "创建支付单并使用 Mock 渠道完成支付")
    public R<Map<String, Object>> smoke(@RequestBody(required = false) PayOrderCreateBo bo) {
        StpUtil.checkLogin();
        PayOrderCreateBo request = prepareRequest(bo == null ? new PayOrderCreateBo() : bo);
        PayOrder order = payOrderService.createOrder(request);

        PaySubmitBo submitBo = new PaySubmitBo();
        submitBo.setOrderId(order.getId());
        submitBo.setChannelCode(PayChannelCode.MOCK.getCode());
        PaySubmitVo submitResult = payOrderService.submitOrder(submitBo);

        PayOrder latest = payOrderService.getOrderByNo(order.getNo());
        return R.ok(Map.of(
            "orderId", latest.getId(),
            "orderNo", latest.getNo(),
            "status", latest.getStatus(),
            "submit", submitResult
        ));
    }

    private PayOrderCreateBo prepareRequest(PayOrderCreateBo bo) {
        if (bo.getPayerTenantId() == null) {
            bo.setPayerTenantId(LoginHelper.getTenantId());
        }
        if (bo.getPayeeTenantId() == null) {
            bo.setPayeeTenantId(TenantConstants.PLATFORM_TENANT_ID);
        }
        if (StringUtils.isBlank(bo.getAppKey())) {
            bo.setAppKey(DEFAULT_APP_KEY);
        }
        if (StringUtils.isBlank(bo.getMerchantOrderId())) {
            bo.setMerchantOrderId("SMOKE-" + System.currentTimeMillis());
        }
        if (StringUtils.isBlank(bo.getSubject())) {
            bo.setSubject("Mock payment smoke");
        }
        if (bo.getPrice() == null) {
            bo.setPrice(1L);
        }
        return bo;
    }
}
