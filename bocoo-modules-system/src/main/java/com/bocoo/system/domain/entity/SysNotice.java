package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.common.core.xss.Xss;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/**
 * 通知公告表 sys_notice
 *
 * @author CMX
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
@Schema(description = "通知公告表")
public class SysNotice extends BaseEntity {

    /**
     * 公告ID
     */
    @Schema(description = "公告ID")
    @TableId(value = "notice_id")
    private Long noticeId;

    /**
     * 公告标题
     */
    @Schema(description = "公告标题")
    @Xss(message = "{validation.notice.title.xss}")
    @NotBlank(message = "{validation.notice.title.required}")
    @Size(min = 0, max = 50, message = "{validation.notice.title.max}")
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    @Schema(description = "公告类型（1通知 2公告）")
    private String noticeType;

    /**
     * 公告内容
     */
    @Schema(description = "公告内容")
    private String noticeContent;

    /**
     * 公告状态（0正常 1关闭）
     */
    @Schema(description = "公告状态（0正常 1关闭）")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
