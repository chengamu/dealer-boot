package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户和角色关联 sys_user_role
 *
 * @author CMX
 */

@Data
@TableName("sys_user_role")
@Schema(description = "用户和角色关联")
public class SysUserRole {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @TableId(type = IdType.INPUT)
    private Long userId;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    private Long roleId;

}
