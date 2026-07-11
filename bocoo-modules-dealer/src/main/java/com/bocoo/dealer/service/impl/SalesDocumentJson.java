package com.bocoo.dealer.service.impl;

import com.bocoo.common.json.utils.JsonUtils;
import com.bocoo.product.domain.vo.ProductPriceQuoteItemVo;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.List;

@Component
class SalesDocumentJson {
    String write(Object value) { return JsonUtils.toJsonString(value); }

    @SuppressWarnings("unchecked")
    Map<String, String> selections(String value) {
        if (value == null || value.isBlank()) return Collections.emptyMap();
        Map<?, ?> map = JsonUtils.parseObject(value, Map.class);
        if (map == null) return Collections.emptyMap();
        return map.entrySet().stream().collect(java.util.stream.Collectors.toMap(
            entry -> String.valueOf(entry.getKey()), entry -> String.valueOf(entry.getValue())));
    }

    List<ProductPriceQuoteItemVo> bomItems(String value) {
        return JsonUtils.parseArray(value, ProductPriceQuoteItemVo.class);
    }
}
