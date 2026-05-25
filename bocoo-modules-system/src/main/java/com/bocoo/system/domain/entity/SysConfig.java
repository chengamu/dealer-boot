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
import jakarta.validation.constraints.Size;

/**
 * 参数配置表 sys_config
 *
 * @author CMX
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
@ExcelIgnoreUnannotated
@Schema(description = "参数配置表")
public class SysConfig extends BaseEntity {

    /**
     * 参数主键
     */
    @Schema(description = "参数主键")
    @ExcelProperty(value = "参数主键")
    @TableId(value = "config_id")
    private Long configId;

    /**
     * 参数名称
     */
    @Schema(description = "参数名称")
    @ExcelProperty(value = "参数名称")
    @NotBlank(message = "{validation.config.name.required}")
    @Size(min = 0, max = 100, message = "{validation.config.name.max}")
    private String configName;

    /**
     * 参数键名
     */
    @Schema(description = "参数键名")
    @ExcelProperty(value = "参数键名")
    @NotBlank(message = "{validation.config.key.required}")
    @Size(min = 0, max = 100, message = "{validation.config.key.max}")
    private String configKey;

    /**
     * 参数键值
     */
    @Schema(description = "参数键值")
    @ExcelProperty(value = "参数键值")
    @NotBlank(message = "{validation.config.value.required}")
    @Size(min = 0, max = 1000, message = "{validation.config.value.max}")
    private String configValue;

    /**
     * 系统内置（Y是 N否）
     */
    @Schema(description = "系统内置（Y是 N否）")
    @ExcelProperty(value = "系统内置", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_yes_no")
    private String configType;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
