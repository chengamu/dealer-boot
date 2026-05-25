package com.bocoo.system.domain.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.excel.annotation.ExcelDictFormat;
import com.bocoo.common.excel.convert.ExcelDictConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 角色表 sys_role
 *
 * @author CMX
 */

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@ExcelIgnoreUnannotated
@Schema(description = "角色表")
public class SysRole extends BaseEntity {

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    @ExcelProperty(value = "角色序号")
    @TableId(value = "role_id")
    private Long roleId;

    @Schema(description = "Tenant ID")
    private Long tenantId;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    @ExcelProperty(value = "角色名称")
    @NotBlank(message = "{validation.role.name.required}")
    @Size(min = 0, max = 30, message = "{validation.role.name.max}")
    private String roleName;

    /**
     * 角色权限
     */
    @Schema(description = "角色权限")
    @ExcelProperty(value = "角色权限")
    @NotBlank(message = "{validation.role.key.required}")
    @Size(min = 0, max = 100, message = "{validation.role.key.max}")
    private String roleKey;

    /**
     * 角色排序
     */
    @Schema(description = "角色排序")
    @ExcelProperty(value = "角色排序")
    @NotNull(message = "{validation.sort.required}")
    private Integer roleSort;

    /**
     * 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限）
     */
    @Schema(description = "数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限）")
    @ExcelProperty(value = "数据范围", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "1=所有数据权限,2=自定义数据权限,3=本部门数据权限,4=本部门及以下数据权限,5=仅本人数据权限")
    private String dataScope;

    /**
     * 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示）
     */
    @Schema(description = "菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示）")
    private Boolean menuCheckStrictly;

    /**
     * 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ）
     */
    @Schema(description = "部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ）")
    private Boolean deptCheckStrictly;

    /**
     * 角色状态（0正常 1停用）
     */
    @Schema(description = "角色状态（0正常 1停用）")
    @ExcelProperty(value = "角色状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @Schema(description = "删除标志（0代表存在 1代表删除）")
    @TableLogic
    private String delFlag;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 用户是否存在此角色标识 默认不存在
     */
    @Schema(description = "用户是否存在此角色标识 默认不存在")
    @TableField(exist = false)
    private boolean flag = false;

    /**
     * 菜单组
     */
    @Schema(description = "菜单组")
    @TableField(exist = false)
    private Long[] menuIds;

    /**
     * 部门组（数据权限）
     */
    @Schema(description = "部门组（数据权限）")
    @TableField(exist = false)
    private Long[] deptIds;

    public SysRole(Long roleId) {
        this.roleId = roleId;
    }

    public boolean isAdmin() {
        return UserConstants.ADMIN_ID.equals(this.roleId);
    }
}
