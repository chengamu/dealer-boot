package com.bocoo.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 引用检查结果视图对象。
 */
@Data
@Schema(description = "引用检查结果视图对象")
public class ReferenceCheckResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否允许操作")
    private Boolean allowed = Boolean.TRUE;

    @Schema(description = "引用数量")
    private Long referenceCount = 0L;

    @Schema(description = "阻断原因消息键")
    private String blockerReasonKey;

    @Schema(description = "引用摘要")
    private List<String> referenceSummaries = new ArrayList<>();
}
