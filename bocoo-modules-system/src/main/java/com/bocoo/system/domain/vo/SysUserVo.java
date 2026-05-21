package com.bocoo.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.bocoo.common.sensitive.annotation.Sensitive;
import com.bocoo.common.sensitive.core.SensitiveStrategy;
import com.bocoo.common.translation.annotation.Translation;
import com.bocoo.common.translation.constant.TransConstant;
import com.bocoo.system.domain.entity.SysUser;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 用户信息视图对象
 *
 * @author CMX
 */
@Data
@AutoMapper(target = SysUser.class)
@Schema(description = "用户信息视图对象")
public class SysUserVo implements Serializable {

    private static final long serialVersionUID = 1L;

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
    private String userName;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
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
    @JsonIgnore
    @JsonProperty
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
     * 部门名
     */
    @Schema(description = "部门名")
    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "deptId")
    private String deptName;

    /**
     * 角色对象
     */
    @Schema(description = "角色对象")
    private List<SysRoleVo> roles;

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

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
