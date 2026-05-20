package com.bocoo.system.domain.bo;

import com.bocoo.common.core.constant.RegexConstants;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.system.domain.entity.SysDictType;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型业务对象 sys_dict_type
 *
 * @author cmx
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysDictType.class, reverseConvertGenerate = false)
@Schema(description = "字典类型业务对象")
public class SysDictTypeBo extends BaseEntity {

    /**
     * 字典主键
     */
    @Schema(description = "字典主键")
    private Long dictId;

    /**
     * 字典名称
     */
    @Schema(description = "字典名称")
    @NotBlank(message = "字典名称不能为空")
    @Size(min = 0, max = 100, message = "字典类型名称长度不能超过{max}个字符")
    private String dictName;

    /**
     * 字典类型
     */
    @Schema(description = "字典类型")
    @NotBlank(message = "字典类型不能为空")
    @Size(min = 0, max = 100, message = "字典类型类型长度不能超过{max}个字符")
    @Pattern(regexp = RegexConstants.DICTIONARY_TYPE, message = "字典类型必须以字母开头，且只能为（小写字母，数字，下滑线）")
    private String dictType;

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
