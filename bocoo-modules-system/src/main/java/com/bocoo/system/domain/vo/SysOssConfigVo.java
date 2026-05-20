package com.bocoo.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.bocoo.system.domain.entity.SysOssConfig;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * 对象存储配置视图对象 sys_oss_config
 *
 * @author CMX
 * @date 2021-08-13
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysOssConfig.class)
@Schema(description = "对象存储配置视图对象")
public class SysOssConfigVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主建
     */
    @Schema(description = "主建")
    private Long ossConfigId;

    /**
     * 配置key
     */
    @Schema(description = "配置key")
    private String configKey;

    /**
     * accessKey
     */
    @Schema(description = "accessKey")
    private String accessKey;

    /**
     * 秘钥
     */
    @Schema(description = "秘钥")
    private String secretKey;

    /**
     * 桶名称
     */
    @Schema(description = "桶名称")
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
     * 域
     */
    @Schema(description = "域")
    private String region;

    /**
     * 是否默认（0=是,1=否）
     */
    @Schema(description = "是否默认（0=是,1=否）")
    private String status;

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
    private String accessPolicy;

}
