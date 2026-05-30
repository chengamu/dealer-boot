package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.xss.Xss;
import com.bocoo.common.sensitive.annotation.Sensitive;
import com.bocoo.common.sensitive.core.SensitiveStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户对象 sys_user
 *
 * @author CMX
 */

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "用户对象")
public class SysUser extends BaseEntity {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @TableId(value = "user_id")
    private Long userId;

    @Schema(description = "Tenant ID")
    private Long tenantId;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long deptId;

    /**
     * 用户账号
     */
    @Schema(description = "用户账号")
    @Xss(message = "{validation.user.username.xss}")
    @NotBlank(message = "{validation.user.username.required}")
    @Size(min = 0, max = 100, message = "{validation.user.username.max}")
    private String userName;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    @Xss(message = "{validation.user.nickname.xss}")
    @NotBlank(message = "{validation.user.nickname.required}")
    @Size(min = 0, max = 30, message = "{validation.user.nickname.max}")
    private String nickName;

    /**
     * 用户类型（sys_user系统用户）
     */
    @Schema(description = "用户类型（sys_user系统用户）")
    private String userType;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱")
    @Sensitive(strategy = SensitiveStrategy.EMAIL)
    @Email(message = "{validation.email.invalid}")
    @Size(min = 0, max = 50, message = "{validation.email.max}")
    private String email;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码")
    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String phonenumber;

    /**
     * 用户性别
     */
    @Schema(description = "用户性别")
    private String sex;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String avatar;

    /**
     * 密码
     */
    @Schema(description = "密码")
    @TableField(
        insertStrategy = FieldStrategy.NOT_EMPTY,
        updateStrategy = FieldStrategy.NOT_EMPTY,
        whereStrategy = FieldStrategy.NOT_EMPTY
    )
    private String password;

    @Schema(description = "Force password change on next login")
    private String forcePasswordChange;

    /**
     * 帐号状态（0正常 1停用）
     */
    @Schema(description = "帐号状态（0正常 1停用）")
    private String status;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @Schema(description = "删除标志（0代表存在 1代表删除）")
    @TableLogic
    private String delFlag;

    /**
     * 最后登录IP
     */
    @Schema(description = "最后登录IP")
    private String loginIp;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    private LocalDateTime loginDate;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


    @Schema(description = "附件1")
    private String fileUrl1;
    @Schema(description = "附件2")
    private String fileUrl2;
    @Schema(description = "附件3")
    private String fileUrl3;
    @Schema(description = "附件4")
    private String fileUrl4;
    @Schema(description = "附件ID1")
    private String ossId1;
    @Schema(description = "附件ID2")
    private String ossId2;
    @Schema(description = "附件ID3")
    private String ossId3;
    @Schema(description = "附件ID4")
    private String ossId4;

    /**
     * 部门对象
     */
    @Schema(description = "部门对象")
    @TableField(exist = false)
    private SysDept dept;

    /**
     * 角色对象
     */
    @Schema(description = "角色对象")
    @TableField(exist = false)
    private List<SysRole> roles;

    /**
     * 角色组
     */
    @Schema(description = "角色组")
    @TableField(exist = false)
    private Long[] roleIds;

    /**
     * 岗位组
     */
    @Schema(description = "岗位组")
    @TableField(exist = false)
    private Long[] postIds;

    /**
     * 数据权限 当前角色ID
     */
    @Schema(description = "数据权限 当前角色ID")
    @TableField(exist = false)
    private Long roleId;

    public SysUser(Long userId) {
        this.userId = userId;
    }

    public boolean isAdmin() {
        return UserConstants.ADMIN_ID.equals(this.userId);
    }

}
