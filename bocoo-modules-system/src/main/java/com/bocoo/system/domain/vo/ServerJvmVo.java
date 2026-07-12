package com.bocoo.system.domain.vo;

import lombok.Data;

import java.time.Instant;

@Data
public class ServerJvmVo {
    private Long usedBytes;
    private Long committedBytes;
    private Long maxBytes;
    private Double usagePercent;
    private String vmName;
    private String javaVersion;
    private Instant startTime;
    private Long uptimeSeconds;
    private Integer threadCount;
}
