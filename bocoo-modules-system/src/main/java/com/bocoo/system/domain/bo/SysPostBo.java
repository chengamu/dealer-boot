package com.bocoo.system.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.system.domain.entity.SysPost;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位信息业务对象 sys_post
 *
 * @author CMX
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysPost.class, reverseConvertGenerate = false)
@Schema(description = "岗位信息业务对象")
public class SysPostBo extends BaseEntity {

    /**
     * 岗位ID
     */
    @Schema(description = "岗位ID")
    private Long postId;

    /**
     * 岗位编码
     */
    @Schema(description = "岗位编码")
    @NotBlank(message = "岗位编码不能为空")
    @Size(min = 0, max = 64, message = "岗位编码长度不能超过{max}个字符")
    private String postCode;

    /**
     * 岗位名称
     */
    @Schema(description = "岗位名称")
    @NotBlank(message = "岗位名称不能为空")
    @Size(min = 0, max = 50, message = "岗位名称长度不能超过{max}个字符")
    private String postName;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    @NotNull(message = "显示顺序不能为空")
    private Integer postSort;

    /**
     * 状态（0停用 1正常）
     */
    @Schema(description = "状态（0停用 1正常）")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
