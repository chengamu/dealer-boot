package com.bocoo.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_provider_config")
public class AiProviderConfig extends BaseEntity {

    @TableId(value = "provider_id")
    private Long providerId;

    private String providerCode;

    private String providerName;

    private String baseUrl;

    private String chatCompletionsPath;

    private String defaultModel;

    private Integer timeoutSeconds;

    private Boolean enabled;

    private String status;

    private String remark;
}
