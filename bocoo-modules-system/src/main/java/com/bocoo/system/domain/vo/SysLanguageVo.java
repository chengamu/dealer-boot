package com.bocoo.system.domain.vo;

import com.bocoo.system.domain.entity.SysLanguage;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serializable;

@Data
@AutoMapper(target = SysLanguage.class)
public class SysLanguageVo implements Serializable {

    private Long languageId;

    private String languageCode;

    private String nameEn;

    private String nameNative;

    private String name;
}
