package com.bocoo.system.domain.vo;

import com.bocoo.system.domain.entity.SysCountry;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serializable;

@Data
@AutoMapper(target = SysCountry.class)
public class SysCountryVo implements Serializable {

    private Long countryId;

    private String countryCode;

    private String nameEn;

    private String nameZh;

    private String name;
}
