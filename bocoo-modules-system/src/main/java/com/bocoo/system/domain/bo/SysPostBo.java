package com.bocoo.system.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
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
public class SysPostBo extends BaseBo {

    /**
     * 岗位ID
     */
    @Schema(description = "岗位ID")
    private Long postId;

    /**
     * 岗位编码
     */
    @Schema(description = "岗位编码")
    @NotBlank(message = "{validation.post.code.required}")
    @Size(min = 0, max = 64, message = "{validation.post.code.max}")
    private String postCode;

    /**
     * 岗位名称
     */
    @Schema(description = "岗位名称")
    @NotBlank(message = "{validation.post.name.required}")
    @Size(min = 0, max = 50, message = "{validation.post.name.max}")
    private String postName;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    @NotNull(message = "{validation.sort.required}")
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
