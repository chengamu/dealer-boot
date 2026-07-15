package com.bocoo.dealer.operations.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OperationsCurrencyAmountVo {

    private String currencyCode;
    private BigDecimal amount;
}
