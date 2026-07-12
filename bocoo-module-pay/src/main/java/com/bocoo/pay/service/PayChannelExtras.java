package com.bocoo.pay.service;

import com.bocoo.common.core.exception.ServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PayChannelExtras {
    private final ObjectMapper objectMapper;

    public String bankRemark(String remark) {
        return json(Map.of("merchantRemark", remark == null ? "" : remark));
    }

    public String supplementReason(String reason) {
        return json(Map.of("supplementReason", reason));
    }

    private String json(Map<String, String> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new ServiceException("Payment channel metadata is invalid");
        }
    }
}
