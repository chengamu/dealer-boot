package com.bocoo.product.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceOptionCombinationVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String optionCombinationKey;
    private String optionCombinationName;
}
