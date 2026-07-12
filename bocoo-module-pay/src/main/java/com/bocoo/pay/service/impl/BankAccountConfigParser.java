package com.bocoo.pay.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.pay.domain.vo.BankCollectionAccountVo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
class BankAccountConfigParser {
    private final ObjectMapper objectMapper;

    List<BankCollectionAccountVo> parse(String config, String currency) {
        if (StringUtils.isBlank(config)) return List.of();
        try {
            JsonNode root = objectMapper.readTree(config);
            JsonNode rows = root.path("bankAccounts");
            if (!rows.isArray()) rows = root.path("accounts");
            if (!rows.isArray()) return List.of();
            List<BankCollectionAccountVo> result = new ArrayList<>();
            rows.forEach(row -> add(result, row, currency));
            return result;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private void add(List<BankCollectionAccountVo> result, JsonNode row, String currency) {
        String rowCurrency = text(row, "currency");
        if (row.has("enabled") && !row.path("enabled").asBoolean()) return;
        if (StringUtils.isNotBlank(currency) && !StringUtils.equalsIgnoreCase(currency, rowCurrency)) return;
        String accountNumber = first(row, "accountNumber", "iban");
        if (StringUtils.isBlank(accountNumber)) return;
        result.add(BankCollectionAccountVo.builder().bankAccountId(first(row, "bankAccountId", "id"))
            .bankName(text(row, "bankName")).accountName(first(row, "accountName", "accountHolder"))
            .accountNumber(accountNumber).accountNumberMasked(mask(accountNumber))
            .swiftCode(first(row, "swiftCode", "swift")).routingNumber(text(row, "routingNumber"))
            .currency(rowCurrency).remark(text(row, "remark")).build());
    }

    private String first(JsonNode row, String first, String second) {
        String value = text(row, first);
        return StringUtils.isNotBlank(value) ? value : text(row, second);
    }

    private String text(JsonNode row, String field) {
        JsonNode value = row.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String mask(String accountNumber) {
        String compact = accountNumber.replaceAll("\\s+", "");
        return compact.length() <= 4 ? compact : "****" + compact.substring(compact.length() - 4);
    }
}
