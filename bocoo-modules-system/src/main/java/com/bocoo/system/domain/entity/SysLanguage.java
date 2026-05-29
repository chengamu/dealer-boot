package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_language")
public class SysLanguage extends BaseEntity {

    @TableId(value = "language_id")
    private Long languageId;

    private String languageCode;

    private String nameEn;

    private String nameNative;

    private String status;

    private Integer sort;
}
