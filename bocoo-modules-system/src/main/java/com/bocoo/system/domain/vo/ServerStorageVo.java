package com.bocoo.system.domain.vo;

import lombok.Data;

@Data
public class ServerStorageVo {
    private Long totalBytes;
    private Long usedBytes;
    private Long availableBytes;
    private Double usagePercent;
}
