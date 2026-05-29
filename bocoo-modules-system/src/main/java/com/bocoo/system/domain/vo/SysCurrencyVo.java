package com.bocoo.system.domain.vo;

import com.bocoo.system.domain.entity.SysCurrency;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serializable;

@Data
@AutoMapper(target = SysCurrency.class)
public class SysCurrencyVo implements Serializable {

    private Long currencyId;

    private String currencyCode;

    private String nameEn;

    private String nameZh;

    private String name;

    private String symbol;

    private Integer decimalPlaces;
}
