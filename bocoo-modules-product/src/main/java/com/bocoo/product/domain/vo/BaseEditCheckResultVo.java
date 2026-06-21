package com.bocoo.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 基础资料修改前检查结果。
 */
@Data
@Schema(description = "基础资料修改前检查结果")
public class BaseEditCheckResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否允许普通修改")
    private Boolean editable;

    @Schema(description = "不可编辑原因")
    private String reason;

    @Schema(description = "不可编辑原因 i18n key")
    private String reasonKey;

    @Schema(description = "当前状态")
    private String status;

    @Schema(description = "影响范围摘要")
    private List<String> impactSummary = new ArrayList<>();
}
