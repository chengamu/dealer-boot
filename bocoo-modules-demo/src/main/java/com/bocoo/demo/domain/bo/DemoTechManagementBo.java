package com.bocoo.demo.domain.bo;

import com.bocoo.common.core.validate.AddGroup;
import com.bocoo.common.core.validate.EditGroup;
import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.demo.domain.entity.DemoTechManagement;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 技术管理示例业务对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DemoTechManagement.class, reverseConvertGenerate = false)
@Schema(description = "技术管理示例业务对象")
public class DemoTechManagementBo extends BaseBo {

    @Schema(description = "技术管理示例ID")
    @NotNull(message = "{gen.validation.id.required}", groups = {EditGroup.class})
    private Long managementId;

    @Schema(description = "标题")
    @NotBlank(message = "{demo.techManagement.title.required}", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 100, message = "{demo.techManagement.title.max}", groups = {AddGroup.class, EditGroup.class})
    private String title;

    @Schema(description = "标签")
    @Size(max = 64, message = "{demo.techManagement.label.max}", groups = {AddGroup.class, EditGroup.class})
    private String label;

    @Schema(description = "自定义标签")
    @Size(max = 64, message = "{demo.techManagement.customLabel.max}", groups = {AddGroup.class, EditGroup.class})
    private String customLabel;

    @Schema(description = "发布有效期")
    @Size(max = 64, message = "{demo.techManagement.deadline.max}", groups = {AddGroup.class, EditGroup.class})
    private String deadline;

    @Schema(description = "详细描述")
    @Size(max = 500, message = "{demo.techManagement.description.max}", groups = {AddGroup.class, EditGroup.class})
    private String description;

    @Schema(description = "单位ID")
    private Long enterpriseId;

    @Schema(description = "联系人")
    @Size(max = 64, message = "{demo.techManagement.contact.max}", groups = {AddGroup.class, EditGroup.class})
    private String contact;

    @Schema(description = "联系方式")
    @Size(max = 64, message = "{demo.techManagement.contactInfo.max}", groups = {AddGroup.class, EditGroup.class})
    private String contactInfo;

    @Schema(description = "图片URL")
    @Size(max = 512, message = "{demo.techManagement.img.max}", groups = {AddGroup.class, EditGroup.class})
    private String img;

    @Schema(description = "状态：DRAFT草稿，PUBLISHED已发布，EXPIRED已过期")
    @NotBlank(message = "{gen.validation.status.required}", groups = {AddGroup.class, EditGroup.class})
    private String status;
}
