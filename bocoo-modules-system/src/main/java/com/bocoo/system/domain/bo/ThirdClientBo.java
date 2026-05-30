package com.bocoo.system.domain.bo;


import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.system.domain.entity.SysConfig;
import com.bocoo.system.domain.entity.ThirdClient;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 第三方应用业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ThirdClient.class, reverseConvertGenerate = false)
@Schema(description = "第三方应用业务对象")
public class ThirdClientBo extends BaseBo {

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
}
