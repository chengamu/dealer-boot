package com.bocoo.system.domain.bo;

import com.bocoo.common.core.constant.RegexConstants;
import com.bocoo.common.mybatis.core.domain.BaseBo;
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
public class SysDictTypeBo extends BaseBo {

    /**
     * 字典主键
     */
    @Schema(description = "字典主键")
    private Long dictId;

    /**
     * 字典名称
     */
    @Schema(description = "字典名称")
    @NotBlank(message = "{validation.dict.name.required}")
    @Size(min = 0, max = 100, message = "{validation.dict.name.max}")
    private String dictName;

    /**
     * 字典类型
     */
    @Schema(description = "字典类型")
    @NotBlank(message = "{validation.dict.type.required}")
    @Size(min = 0, max = 100, message = "{validation.dict.type.max}")
    @Pattern(regexp = RegexConstants.DICTIONARY_TYPE, message = "{validation.dict.type.pattern}")
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
