package com.bocoo.system.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.common.core.validate.AddGroup;
import com.bocoo.common.core.validate.EditGroup;
import com.bocoo.system.domain.entity.SysOssConfig;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对象存储配置业务对象 sys_oss_config
 *
 * @author CMX
 * @date 2021-08-13
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysOssConfig.class, reverseConvertGenerate = false)
@Schema(description = "对象存储配置业务对象")
public class SysOssConfigBo extends BaseEntity {

    /**
     * 主建
     */
    @Schema(description = "主建")
    @NotNull(message = "{validation.id.required}", groups = {EditGroup.class})
    private Long ossConfigId;

    /**
     * 配置key
     */
    @Schema(description = "配置key")
    @NotBlank(message = "{validation.oss.config.key.required}", groups = {AddGroup.class, EditGroup.class})
    @Size(min = 2, max = 100, message = "{validation.oss.config.key.size}")
    private String configKey;

    /**
     * accessKey
     */
    @Schema(description = "accessKey")
    @NotBlank(message = "{validation.oss.accessKey.required}", groups = {AddGroup.class, EditGroup.class})
    @Size(min = 2, max = 100, message = "{validation.oss.accessKey.size}")
    private String accessKey;

    /**
     * 秘钥
     */
    @Schema(description = "秘钥")
    @NotBlank(message = "{validation.oss.secretKey.required}", groups = {AddGroup.class, EditGroup.class})
    @Size(min = 2, max = 100, message = "{validation.oss.secretKey.size}")
    private String secretKey;

    /**
     * 桶名称
     */
    @Schema(description = "桶名称")
    @NotBlank(message = "{validation.oss.bucketName.required}", groups = {AddGroup.class, EditGroup.class})
    @Size(min = 2, max = 100, message = "{validation.oss.bucketName.size}")
    private String bucketName;

    /**
     * 前缀
     */
    @Schema(description = "前缀")
    private String prefix;

    /**
     * 访问站点
     */
    @Schema(description = "访问站点")
    @NotBlank(message = "{validation.oss.endpoint.required}", groups = {AddGroup.class, EditGroup.class})
    @Size(min = 2, max = 100, message = "{validation.oss.endpoint.size}")
    private String endpoint;

    /**
     * 自定义域名
     */
    @Schema(description = "自定义域名")
    private String domain;

    /**
     * 是否https（Y=是,N=否）
     */
    @Schema(description = "是否https（Y=是,N=否）")
    private String isHttps;

    /**
     * 是否默认（0=是,1=否）
     */
    @Schema(description = "是否默认（0=是,1=否）")
    private String status;

    /**
     * 域
     */
    @Schema(description = "域")
    private String region;

    /**
     * 扩展字段
     */
    @Schema(description = "扩展字段")
    private String ext1;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 桶权限类型(0private 1public 2custom)
     */
    @Schema(description = "桶权限类型(0private 1public 2custom)")
    @NotBlank(message = "{validation.oss.accessPolicy.required}", groups = {AddGroup.class, EditGroup.class})
    private String accessPolicy;

}
