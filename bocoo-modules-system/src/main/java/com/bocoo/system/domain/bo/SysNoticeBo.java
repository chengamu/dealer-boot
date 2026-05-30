package com.bocoo.system.domain.bo;

import com.bocoo.common.core.xss.Xss;
import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.system.domain.entity.SysNotice;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告业务对象 sys_notice
 *
 * @author CMX
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysNotice.class, reverseConvertGenerate = false)
@Schema(description = "通知公告业务对象")
public class SysNoticeBo extends BaseBo {

    /**
     * 公告ID
     */
    @Schema(description = "公告ID")
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
     * 创建者
     */
    @Schema(description = "创建者")
    private String createBy;

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

    /**
     * 创建人名称
     */
    @Schema(description = "创建人名称")
    private String createByName;

}
