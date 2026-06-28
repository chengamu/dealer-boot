package com.bocoo.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_provider_model")
public class AiProviderModel extends BaseEntity {

    @TableId(value = "model_id")
    private Long modelId;

    private Long providerId;

    private String modelCode;

    private String modelName;

    private String modelType;

    private Integer contextWindow;

    private BigDecimal inputPrice;

    private BigDecimal outputPrice;

    private Boolean defaultModel;

    private String status;

    private String remark;
}
