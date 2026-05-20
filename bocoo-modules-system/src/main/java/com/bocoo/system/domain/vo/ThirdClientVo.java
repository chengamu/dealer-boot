package com.bocoo.system.domain.vo;

import com.bocoo.system.domain.entity.ThirdClient;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 第三方应用视图对象
 */
@Data
@AutoMapper(target = ThirdClient.class)
@Schema(description = "第三方应用视图对象")
public class ThirdClientVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "应用唯一标识")
    private String appKey;

    @Schema(description = "应用密钥")
    private String secretKey;

    @Schema(description = "应用名称")
    private String name;

    @Schema(description = "权限列表")
    private List<String> permissions;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
