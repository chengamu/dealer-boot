package com.bocoo.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.common.excel.annotation.ExcelDictFormat;
import com.bocoo.common.excel.convert.ExcelDictConvert;
import com.bocoo.system.domain.entity.SysPost;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位信息视图对象 sys_post
 *
 * @author CMX
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysPost.class)
@Schema(description = "岗位信息视图对象")
public class SysPostVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 岗位ID
     */
    @Schema(description = "岗位ID")
    @ExcelProperty(value = "岗位序号")
    private Long postId;

    /**
     * 岗位编码
     */
    @Schema(description = "岗位编码")
    @ExcelProperty(value = "岗位编码")
    private String postCode;

    /**
     * 岗位名称
     */
    @Schema(description = "岗位名称")
    @ExcelProperty(value = "岗位名称")
    private String postName;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    @ExcelProperty(value = "岗位排序")
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
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @ExcelProperty(value = "创建时间")
    private LocalDateTime createTime;

}
