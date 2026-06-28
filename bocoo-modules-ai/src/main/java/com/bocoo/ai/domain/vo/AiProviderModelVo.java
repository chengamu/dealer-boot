package com.bocoo.ai.domain.vo;

import com.bocoo.ai.domain.entity.AiProviderModel;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = AiProviderModel.class)
public class AiProviderModelVo {
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
