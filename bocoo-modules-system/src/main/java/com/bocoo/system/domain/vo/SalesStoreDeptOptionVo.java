package com.bocoo.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Sales store department option")
public class SalesStoreDeptOptionVo {
    private Long deptId;
    private String deptName;
    private boolean linked;
}
