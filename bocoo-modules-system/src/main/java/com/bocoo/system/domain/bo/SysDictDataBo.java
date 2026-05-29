package com.bocoo.system.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.system.domain.entity.SysDictData;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 字典数据业务对象 sys_dict_data
 *
     * @author CMX
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysDictData.class, reverseConvertGenerate = false)
@Schema(description = "字典数据业务对象")
public class SysDictDataBo extends BaseEntity {

    /**
     * 字典编码
     */
    @Schema(description = "字典编码")
    private Long dictCode;

    /**
     * 字典排序
     */
    @Schema(description = "字典排序")
    private Integer dictSort;

    /**
     * 字典标签
     */
    @Schema(description = "字典标签")
    @NotBlank(message = "{validation.dict.label.required}")
    @Size(min = 0, max = 100, message = "{validation.dict.label.max}")
    private String dictLabel;

    /**
     * 字典键值
     */
    @Schema(description = "字典键值")
    @NotBlank(message = "{validation.dict.value.required}")
    @Size(min = 0, max = 100, message = "{validation.dict.value.max}")
    private String dictValue;

    /**
     * 字典类型
     */
    @Schema(description = "字典类型")
    @NotBlank(message = "{validation.dict.type.required}")
    @Size(min = 0, max = 100, message = "{validation.dict.type.max}")
    private String dictType;

    /**
     * 样式属性（其他样式扩展）
     */
    @Schema(description = "样式属性（其他样式扩展）")
    @Size(min = 0, max = 100, message = "{validation.css.class.max}")
    private String cssClass;

    /**
     * 表格回显样式
     */
    @Schema(description = "表格回显样式")
    private String listClass;

    /**
     * 是否默认（Y是 N否）
     */
    @Schema(description = "是否默认（Y是 N否）")
    private String isDefault;

    /**
     * 状态（0停用 1正常）
     */
    @Schema(description = "状态（0停用 1正常）")
    private String status;

    /**
     * 创建部门
     */
    @Schema(description = "创建部门")
    private LocalDateTime createDept;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
