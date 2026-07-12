package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.pay.paypal.PayPalWebhookRequest;
import com.bocoo.pay.service.PayPalPaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pay/notify/paypal")
@SaIgnore
public class PayPalWebhookController {
    private final PayPalPaymentService service;
    private final ObjectMapper objectMapper;

    @PostMapping
    public R<Void> notify(@RequestHeader HttpHeaders headers, @RequestBody String rawBody) {
        if (!service.handleWebhook(request(headers, rawBody))) {
            throw new ServiceException("PayPal webhook signature verification failed");
        }
        return R.ok();
    }

    private PayPalWebhookRequest request(HttpHeaders headers, String rawBody) {
        JsonNode root = read(rawBody);
        JsonNode resource = root.path("resource");
        JsonNode amount = resource.path("amount");
        String orderId = resource.path("supplementary_data").path("related_ids").path("order_id").asText();
        return PayPalWebhookRequest.builder().eventId(root.path("id").asText())
            .eventType(root.path("event_type").asText()).paypalOrderId(orderId)
            .captureId(resource.path("id").asText()).price(minor(amount.path("value").asText("0")))
            .currency(amount.path("currency_code").asText())
            .transmissionId(headers.getFirst("PAYPAL-TRANSMISSION-ID"))
            .transmissionTime(headers.getFirst("PAYPAL-TRANSMISSION-TIME"))
            .transmissionSig(headers.getFirst("PAYPAL-TRANSMISSION-SIG"))
            .certUrl(headers.getFirst("PAYPAL-CERT-URL")).authAlgo(headers.getFirst("PAYPAL-AUTH-ALGO"))
            .rawBody(rawBody).build();
    }

    private JsonNode read(String rawBody) {
        try {
            return objectMapper.readTree(rawBody);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid PayPal webhook body");
        }
    }

    private Long minor(String amount) {
        return new BigDecimal(amount).movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
    }
}
