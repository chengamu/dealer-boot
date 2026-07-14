package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色和菜单关联 sys_role_menu
 *
 * @author CMX
 */

@Data
@TableName("sys_role_menu")
@Schema(description = "角色和菜单关联")
public class SysRoleMenu {

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    @TableId(type = IdType.INPUT)
    private Long roleId;

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID")
    private Long menuId;

    @Schema(description = "Tenant ID")
    private Long tenantId;

}
