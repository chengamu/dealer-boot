package com.bocoo.ai.domain.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiProviderModelBo {
    private Long modelId;
    private String providerCode;
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
