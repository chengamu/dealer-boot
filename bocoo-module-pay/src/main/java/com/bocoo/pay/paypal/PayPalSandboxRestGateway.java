package com.bocoo.pay.paypal;

import com.bocoo.common.core.exception.ServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PayPalSandboxRestGateway implements PayPalGateway {
    private final PayPalSandboxCredentials credentials;
    private final PayPalSandboxHttpClient http;
    private final PayPalAmountConverter amounts;

    @Override
    public PayPalOrderResult createOrder(PayPalCreateRequest request) {
        Map<String, Object> amount = Map.of("currency_code", request.getCurrency(),
            "value", amounts.major(request.getPrice()));
        Map<String, Object> unit = Map.of("amount", amount, "invoice_id", request.getInvoiceId(),
            "custom_id", request.getCustomId(), "description", request.getDescription());
        JsonNode node = http.post("/v2/checkout/orders",
            Map.of("intent", "CAPTURE", "purchase_units", List.of(unit)), request.getRequestId());
        return parseOrder(node);
    }

    @Override
    public PayPalOrderResult captureOrder(String paypalOrderId, String requestId) {
        return parseOrder(http.post("/v2/checkout/orders/" + paypalOrderId + "/capture", Map.of(), requestId));
    }

    @Override
    public PayPalOrderResult getOrder(String paypalOrderId) {
        return parseOrder(http.get("/v2/checkout/orders/" + paypalOrderId));
    }

    @Override
    public boolean verifyWebhook(PayPalWebhookRequest request) {
        Map<String, Object> body = Map.of(
            "auth_algo", request.getAuthAlgo(), "cert_url", request.getCertUrl(),
            "transmission_id", request.getTransmissionId(), "transmission_sig", request.getTransmissionSig(),
            "transmission_time", request.getTransmissionTime(), "webhook_id", credentials.webhookId(),
            "webhook_event", http.readTree(request.getRawBody()));
        JsonNode result = http.post("/v1/notifications/verify-webhook-signature", body, null);
        return "SUCCESS".equalsIgnoreCase(result.path("verification_status").asText());
    }

    private PayPalOrderResult parseOrder(JsonNode root) {
        if (root == null) throw new ServiceException("PayPal returned an empty response");
        JsonNode unit = root.path("purchase_units").path(0);
        JsonNode capture = unit.path("payments").path("captures").path(0);
        JsonNode amount = capture.isMissingNode() ? unit.path("amount") : capture.path("amount");
        String approval = null;
        for (JsonNode link : root.path("links")) {
            if ("approve".equals(link.path("rel").asText())) approval = link.path("href").asText();
        }
        return PayPalOrderResult.builder().orderId(root.path("id").asText())
            .captureId(capture.path("id").asText(null)).status(root.path("status").asText())
            .price(amounts.minor(amount.path("value").asText("0")))
            .currency(amount.path("currency_code").asText())
            .payeeMerchantId(unit.path("payee").path("merchant_id").asText(null))
            .approvalUrl(approval).build();
    }
}
