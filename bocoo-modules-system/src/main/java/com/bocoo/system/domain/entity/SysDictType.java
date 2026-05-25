package com.bocoo.system.domain.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.excel.annotation.ExcelDictFormat;
import com.bocoo.common.excel.convert.ExcelDictConvert;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 字典类型表 sys_dict_type
 *
 * @author CMX
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
@ExcelIgnoreUnannotated
@Schema(description = "字典类型表")
public class SysDictType extends BaseEntity {

    /**
     * 字典主键
     */
    @Schema(description = "字典主键")
    @ExcelProperty(value = "字典主键")
    @TableId(value = "dict_id")
    private Long dictId;

    /**
     * 字典名称
     */
    @Schema(description = "字典名称")
    @ExcelProperty(value = "字典名称")
    @NotBlank(message = "{validation.dict.name.required}")
    @Size(min = 0, max = 100, message = "{validation.dict.name.max}")
    private String dictName;

    /**
     * 字典类型
     */
    @Schema(description = "字典类型")
    @ExcelProperty(value = "字典类型")
    @NotBlank(message = "{validation.dict.type.required}")
    @Size(min = 0, max = 100, message = "{validation.dict.type.max}")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "{validation.dict.type.pattern}")
    private String dictType;

    /**
     * 状态（0正常 1停用）
     */
    @Schema(description = "状态（0正常 1停用）")
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
