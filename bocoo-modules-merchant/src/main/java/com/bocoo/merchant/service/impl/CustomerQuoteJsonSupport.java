package com.bocoo.merchant.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
class CustomerQuoteJsonSupport {

    private final ObjectMapper objectMapper;

    String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    Map<String, String> readSelections(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, LinkedHashMap.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    String pricingSnapshot(Object price, Object shipping, boolean englishComplete) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("price", price);
        snapshot.put("englishComplete", englishComplete);
        return write(snapshot);
    }

    String shippingSnapshot(Object shipping, BigDecimal totalAmount, String currencyCode) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("shipping", shipping);
        snapshot.put("totalAmount", totalAmount);
        snapshot.put("currencyCode", currencyCode);
        return write(snapshot);
    }
}
