package com.bocoo.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Sales store disable reference check")
public class SalesStoreReferenceVo {
    private long customerCount;
    private long unfinishedOrderCount;

    public boolean isDisableAllowed() {
        return unfinishedOrderCount == 0;
    }

    public static SalesStoreReferenceVo empty() {
        return new SalesStoreReferenceVo(0, 0);
    }
}
