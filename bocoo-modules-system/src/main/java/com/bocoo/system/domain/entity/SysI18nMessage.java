package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 国际化消息表 sys_i18n_message
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_i18n_message")
@Schema(description = "国际化消息表")
public class SysI18nMessage extends BaseEntity {

    @Schema(description = "消息ID")
    @TableId(value = "message_id")
    private Long messageId;

    @Schema(description = "消息键")
    private String messageKey;

    @Schema(description = "语言")
    private String locale;

    @Schema(description = "消息内容")
    private String messageValue;

}
