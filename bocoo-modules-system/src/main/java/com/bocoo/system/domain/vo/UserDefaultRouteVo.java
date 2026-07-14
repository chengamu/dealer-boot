package com.bocoo.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resolved login default route")
public class UserDefaultRouteVo {
    private Long defaultMenuId;
    private String defaultRoute;
    private String defaultRouteTitle;
}
