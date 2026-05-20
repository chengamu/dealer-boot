package com.bocoo.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户个人信息
 *
 * @author CMX
 */
@Data
@Schema(description = "用户个人信息")
public class ProfileVo {

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private SysUserVo user;

    /**
     * 用户所属角色组
     */
    @Schema(description = "用户所属角色组")
    private String roleGroup;

    /**
     * 用户所属岗位组
     */
    @Schema(description = "用户所属岗位组")
    private String postGroup;

}
