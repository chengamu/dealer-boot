package com.bocoo.system.domain.bo;

import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.xss.Xss;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.system.domain.entity.SysDept;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.domain.entity.SysUser;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 用户信息业务对象 sys_user
 *
 * @author CMX
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysUser.class, reverseConvertGenerate = false)
@Schema(description = "用户信息业务对象")
public class SysUserBo extends BaseEntity {
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
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
    @Xss(message = "用户账号不能包含脚本字符")
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 0, max = 100, message = "用户账号长度不能超过{max}个字符")
    private String userName;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    @Xss(message = "用户昵称不能包含脚本字符")
    @NotBlank(message = "用户昵称不能为空")
    @Size(min = 0, max = 30, message = "用户昵称长度不能超过{max}个字符")
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
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过{max}个字符")
    private String email;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码")
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
    private String password;

    /**
     * 帐号状态（0正常 1停用）
     */
    @Schema(description = "帐号状态（0正常 1停用）")
    private String status;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @Schema(description = "删除标志（0代表存在 1代表删除）")
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
    private Date loginDate;

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
    private SysDept dept;

    /**
     * 角色对象
     */
    @Schema(description = "角色对象")
    private List<SysRole> roles;

    /**
     * 角色组
     */
    @Schema(description = "角色组")
    private Long[] roleIds;

    /**
     * 岗位组
     */
    @Schema(description = "岗位组")
    private Long[] postIds;

    /**
     * 数据权限 当前角色ID
     */
    @Schema(description = "数据权限 当前角色ID")
    private Long roleId;

    public SysUserBo(Long userId) {
        this.userId = userId;
    }

    public boolean isAdmin() {
        return UserConstants.ADMIN_ID.equals(this.userId);
    }
}
