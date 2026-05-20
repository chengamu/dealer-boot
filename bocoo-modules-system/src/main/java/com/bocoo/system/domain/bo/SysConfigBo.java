package com.bocoo.system.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.system.domain.entity.SysConfig;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 参数配置业务对象 sys_config
 *
 * @author cmx
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysConfig.class, reverseConvertGenerate = false)
@Schema(description = "参数配置业务对象")
public class SysConfigBo extends BaseEntity {

    /**
     * 参数主键
     */
    @Schema(description = "参数主键")
    private Long configId;

    /**
     * 参数名称
     */
    @Schema(description = "参数名称")
    @NotBlank(message = "参数名称不能为空")
    @Size(min = 0, max = 100, message = "参数名称不能超过{max}个字符")
    private String configName;

    /**
     * 参数键名
     */
    @Schema(description = "参数键名")
    @NotBlank(message = "参数键名不能为空")
    @Size(min = 0, max = 100, message = "参数键名长度不能超过{max}个字符")
    private String configKey;

    /**
     * 参数键值
     */
    @Schema(description = "参数键值")
    @NotBlank(message = "参数键值不能为空")
    @Size(min = 0, max = 1000, message = "参数键值长度不能超过{max}个字符")
    private String configValue;

    /**
     * 系统内置（Y是 N否）
     */
    @Schema(description = "系统内置（Y是 N否）")
    private String configType;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
