package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门表 sys_dept
 *
 * @author CMX
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
@Schema(description = "部门表")
public class SysDept extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    @TableId(value = "dept_id")
    private Long deptId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    @NotBlank(message = "{validation.dept.name.required}")
    @Size(min = 0, max = 30, message = "{validation.dept.name.max}")
    private String deptName;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    @NotNull(message = "{validation.sort.required}")
    private Integer orderNum;

    /**
     * 负责人
     */
    @Schema(description = "负责人")
    private String leader;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    @Size(min = 0, max = 11, message = "{validation.phone.max}")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    @Email(message = "{validation.email.invalid}")
    @Size(min = 0, max = 50, message = "{validation.email.max}")
    private String email;

    /**
     * 部门状态:0正常,1停用
     */
    @Schema(description = "部门状态:0正常,1停用")
    private String status;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @Schema(description = "删除标志（0代表存在 1代表删除）")
    @TableLogic
    private String delFlag;

    /**
     * 祖级列表
     */
    @Schema(description = "祖级列表")
    private String ancestors;

    /**
     * 父菜单名称
     */
    @Schema(description = "父菜单名称")
    @TableField(exist = false)
    private String parentName;

    /**
     * 父菜单ID
     */
    @Schema(description = "父菜单ID")
    private Long parentId;

    /**
     * 子部门
     */
    @Schema(description = "子部门")
    @TableField(exist = false)
    private List<SysDept> children = new ArrayList<>();

}
