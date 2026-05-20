package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OSS对象存储对象
 *
 * @author CMX
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_oss")
@Schema(description = "OSS对象存储对象")
public class SysOss extends BaseEntity {

    /**
     * 对象存储主键
     */
    @Schema(description = "对象存储主键")
    @TableId(value = "oss_id")
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
     * 服务商
     */
    @Schema(description = "服务商")
    private String service;


    /**
     * 部门
     */
    @Schema(description = "部门ID")
    private Long deptId;
}
