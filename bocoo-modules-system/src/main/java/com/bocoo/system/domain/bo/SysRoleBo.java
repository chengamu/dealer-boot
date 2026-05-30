package com.bocoo.system.domain.bo;

import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.system.domain.entity.SysRole;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 角色信息业务对象 sys_role
 *
 * @author CMX
 */

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysRole.class, reverseConvertGenerate = false)
@Schema(description = "角色信息业务对象")
public class SysRoleBo extends BaseBo {

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "Tenant ID")
    private Long tenantId;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    @NotBlank(message = "{validation.role.name.required}")
    @Size(min = 0, max = 30, message = "{validation.role.name.max}")
    private String roleName;

    /**
     * 角色权限字符串
     */
    @Schema(description = "角色权限字符串")
    @NotBlank(message = "{validation.role.key.required}")
    @Size(min = 0, max = 100, message = "{validation.role.key.max}")
    private String roleKey;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    @NotNull(message = "{validation.sort.required}")
    private Integer roleSort;

    /**
     * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）
     */
    @Schema(description = "数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）")
    private String dataScope;

    /**
     * 菜单树选择项是否关联显示
     */
    @Schema(description = "菜单树选择项是否关联显示")
    private Boolean menuCheckStrictly;

    /**
     * 部门树选择项是否关联显示
     */
    @Schema(description = "部门树选择项是否关联显示")
    private Boolean deptCheckStrictly;

    /**
     * 角色状态（0正常 1停用）
     */
    @Schema(description = "角色状态（0正常 1停用）")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 菜单组
     */
    @Schema(description = "菜单组")
    private Long[] menuIds;

    /**
     * 部门组（数据权限）
     */
    @Schema(description = "部门组（数据权限）")
    private Long[] deptIds;

    public SysRoleBo(Long roleId) {
        this.roleId = roleId;
    }

    public boolean isAdmin() {
        return UserConstants.ADMIN_ID.equals(this.roleId);
    }

}
