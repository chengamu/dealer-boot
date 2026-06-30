package com.bocoo.ai.domain.bo;

import com.bocoo.ai.domain.entity.AiUsageLedger;
import com.bocoo.common.mybatis.core.domain.BaseBo;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AiUsageLedger.class, reverseConvertGenerate = false)
@Schema(description = "AI 用量流水查询对象")
public class AiUsageLedgerBo extends BaseBo {

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "Provider")
    private String provider;

    @Schema(description = "模型")
    private String model;

    @Schema(description = "请求ID")
    private String requestId;

    @Schema(description = "状态")
    private String status;
}
