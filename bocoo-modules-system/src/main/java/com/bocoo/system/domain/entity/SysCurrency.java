package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_currency")
public class SysCurrency extends BaseEntity {

    @TableId(value = "currency_id")
    private Long currencyId;

    private String currencyCode;

    private String nameEn;

    private String nameZh;

    private String symbol;

    private Integer decimalPlaces;

    private String status;

    private Integer sort;
}
