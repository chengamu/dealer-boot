package com.bocoo.system.domain.vo;

import com.bocoo.system.domain.entity.SysOss;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * OSS对象存储视图对象 sys_oss
 *
 * @author CMX
 */
@Data
@AutoMapper(target = SysOss.class)
@Schema(description = "OSS对象存储视图对象")
public class SysOssVo {

    private static final long serialVersionUID = 1L;

    /**
     * 对象存储主键
     */
    @Schema(description = "对象存储主键")
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
     * URL地址
     */
    @Schema(description = "URL地址")
    private String url;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 上传人
     */
    @Schema(description = "上传人")
    private String createBy;

    /**
     * 服务商
     */
    @Schema(description = "服务商")
    private String service;

}
