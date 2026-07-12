package com.bocoo.system.domain.vo;

import lombok.Data;

@Data
public class ServerSystemVo {
    private String hostName;
    private String osName;
    private String osVersion;
    private String architecture;
    private Long uptimeSeconds;
}
