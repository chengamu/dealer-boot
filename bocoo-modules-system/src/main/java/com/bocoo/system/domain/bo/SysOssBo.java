package com.bocoo.system.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.system.domain.entity.SysOss;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OSS对象存储分页查询对象 sys_oss
 *
 * @author CMX
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysOss.class, reverseConvertGenerate = false)
@Schema(description = "OSS对象存储业务对象")
public class SysOssBo extends BaseBo {

    /**
     * ossId
     */
    @Schema(description = "ossId")
    private Long ossId;

    /**
     * 文件名
     */
    @Schema(description = "文件名")
    private String fileName;

    /**
     * 原名
     */
    @Schema(description = "原名")
    private String originalName;

    /**
     * 文件后缀名
     */
    @Schema(description = "文件后缀名")
    private String fileSuffix;

    /**
     * 创建者
     */
    @Schema(description = "创建者")
    private String createBy;

    /**
     * URL地址
     */
    @Schema(description = "URL地址")
    private String url;

    /**
     * 服务商
     */
    @Schema(description = "服务商")
    private String service;

}
