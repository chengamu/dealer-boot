package com.bocoo.system.domain.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.excel.annotation.ExcelDictFormat;
import com.bocoo.common.excel.convert.ExcelDictConvert;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 岗位表 sys_post
 *
 * @author CMX
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_post")
@ExcelIgnoreUnannotated
@Schema(description = "岗位表")
public class SysPost extends BaseEntity {

    /**
     * 岗位序号
     */
    @Schema(description = "岗位序号")
    @ExcelProperty(value = "岗位序号")
    @TableId(value = "post_id")
    private Long postId;

    /**
     * 岗位编码
     */
    @Schema(description = "岗位编码")
    @ExcelProperty(value = "岗位编码")
    @NotBlank(message = "岗位编码不能为空")
    @Size(min = 0, max = 64, message = "岗位编码长度不能超过{max}个字符")
    private String postCode;

    /**
     * 岗位名称
     */
    @Schema(description = "岗位名称")
    @ExcelProperty(value = "岗位名称")
    @NotBlank(message = "岗位名称不能为空")
    @Size(min = 0, max = 50, message = "岗位名称长度不能超过{max}个字符")
    private String postName;

    /**
     * 岗位排序
     */
    @Schema(description = "岗位排序")
    @ExcelProperty(value = "岗位排序")
    @NotNull(message = "显示顺序不能为空")
    private Integer postSort;

    /**
     * 状态（0停用 1正常）
     */
    @Schema(description = "状态（0停用 1正常）")
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 用户是否存在此岗位标识 默认不存在
     */
    @Schema(description = "用户是否存在此岗位标识 默认不存在")
    @TableField(exist = false)
    private boolean flag = false;

}
