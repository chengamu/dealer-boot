package com.bocoo.system.domain.vo;

import lombok.Data;

@Data
public class ServerCpuVo {
    private Integer logicalCores;
    private Double usagePercent;
    private Double userPercent;
    private Double systemPercent;
    private Double waitPercent;
}
