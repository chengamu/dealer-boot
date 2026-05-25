package com.bocoo.system.controller.system;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import com.bocoo.common.core.config.BocooConfig;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.exception.file.InvalidExtensionException;
import com.bocoo.common.core.utils.MessageUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.file.FileUploadUtils;
import com.bocoo.common.core.utils.file.MimeTypeUtils;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.system.domain.bo.SysUserBo;
import com.bocoo.system.domain.bo.SysUserProfileBo;
import com.bocoo.system.domain.vo.ProfileVo;
import com.bocoo.system.domain.vo.SysOssVo;
import com.bocoo.system.domain.vo.SysUserVo;
import com.bocoo.system.service.SysOssService;
import com.bocoo.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * 个人信息 业务处理
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user/profile")
@Tag(name = "个人信息管理", description = "个人信息业务处理接口")
public class SysProfileController extends BaseController {

    private final SysUserService userService;
    private final SysOssService sysOssService;
    private final BocooConfig bocooConfig;


    /**
     * 个人信息
     */
    @GetMapping
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的个人信息")
    public R<ProfileVo> profile() {
        SysUserVo user = userService.selectUserById(LoginHelper.getUserId());
        ProfileVo profileVo = new ProfileVo();
        profileVo.setUser(user);
        profileVo.setRoleGroup(userService.selectUserRoleGroup(user.getUserId()));
        profileVo.setPostGroup(userService.selectUserPostGroup(user.getUserId()));
        return R.ok(profileVo);
    }

    /**
     * 修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改用户信息", description = "修改当前用户的个人信息")
    public R<Void> updateProfile(
            @Parameter(description = "用户信息", required = true)
            @Validated @RequestBody SysUserProfileBo profile) {
        if(LoginHelper.getUsername().equals("show")){
            return R.fail(MessageUtils.message("demo.account.update.denied"));
        }
        SysUserBo user = BeanUtil.toBean(profile, SysUserBo.class);
        user.setUserId(LoginHelper.getUserId());
        String username = LoginHelper.getUsername();
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("修改用户'" + username + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("修改用户'" + username + "'失败，邮箱账号已存在");
        }
        if (userService.updateUserProfile(user) > 0) {
            return R.ok();
        }
        return R.fail(MessageUtils.message("profile.update.failed"));
    }

    /**
     * 重置密码
     *
     * @param newPassword 新密码
     * @param oldPassword 旧密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    @Operation(summary = "重置密码", description = "修改当前用户密码")
    public R<Void> updatePwd(
            @Parameter(description = "旧密码", required = true)
            String oldPassword,
            @Parameter(description = "新密码", required = true)
            String newPassword) {
        if(LoginHelper.getUsername().equals("show")){
            return R.fail(MessageUtils.message("demo.account.update.denied"));
        }
        SysUserVo user = userService.selectUserById(LoginHelper.getUserId());
        String password = user.getPassword();
        if (StringUtils.equals(oldPassword,newPassword)) {
            return R.fail(MessageUtils.message("password.same.denied"));
        }
        if (!BCrypt.checkpw(oldPassword, password)) {
            return R.fail(MessageUtils.message("password.old.invalid"));
        }
        if (userService.resetUserPwd(user.getUserId(), BCrypt.hashpw(newPassword)) > 0) {
            return R.ok();
        }
        return R.fail(MessageUtils.message("password.update.failed"));
    }

    /**
     * 头像上传
     *
     * @param avatarfile 用户头像
     */
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Map<String, Object>> avatar(@RequestPart("avatarfile") MultipartFile avatarfile) {
        if (!avatarfile.isEmpty()) {
            String extension = FileUtil.extName(avatarfile.getOriginalFilename());
            if (!StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION)) {
                return R.fail("文件格式不正确，请上传" + Arrays.toString(MimeTypeUtils.IMAGE_EXTENSION) + "格式");
            }
            SysOssVo oss = sysOssService.upload(avatarfile);
            String avatar = oss.getUrl();
            if (userService.updateUserAvatar(LoginHelper.getUsername(), avatar)) {
                return R.ok(MessageUtils.message("common.upload.success"), Map.of("imgUrl", avatar));
            }
        }
        return R.fail(MessageUtils.message("common.upload.failed"));
    }
}
