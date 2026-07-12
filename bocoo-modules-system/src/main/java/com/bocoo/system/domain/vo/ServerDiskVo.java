package com.bocoo.system.domain.vo;

import lombok.Data;

@Data
public class ServerDiskVo {
    private String name;
    private String mount;
    private String type;
    private Long totalBytes;
    private Long usedBytes;
    private Long availableBytes;
    private Double usagePercent;
}
