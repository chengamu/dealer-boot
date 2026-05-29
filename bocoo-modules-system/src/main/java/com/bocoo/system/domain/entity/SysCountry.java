package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_country")
public class SysCountry extends BaseEntity {

    @TableId(value = "country_id")
    private Long countryId;

    private String countryCode;

    private String nameEn;

    private String nameZh;

    private String status;

    private Integer sort;
}
