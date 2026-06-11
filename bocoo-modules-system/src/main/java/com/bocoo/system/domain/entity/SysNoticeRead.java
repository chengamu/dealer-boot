package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户公告阅读记录 sys_notice_read
 *
 * @author CMX
 */
@Data
@TableName("sys_notice_read")
@Schema(description = "用户公告阅读记录")
public class SysNoticeRead implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "阅读记录ID")
    @TableId(value = "read_id")
    private Long readId;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "公告ID")
    private Long noticeId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

}
