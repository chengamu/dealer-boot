package com.bocoo.system.domain.bo;

import com.bocoo.common.core.constant.RegexConstants;
import com.bocoo.common.core.xss.Xss;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.common.sensitive.annotation.Sensitive;
import com.bocoo.common.sensitive.core.SensitiveStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 个人信息业务处理
 *
 * @author CMX
 */

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "个人信息业务对象")
public class SysUserProfileBo extends BaseEntity {

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    @Xss(message = "用户昵称不能包含脚本字符")
    @Size(min = 0, max = 30, message = "用户昵称长度不能超过{max}个字符")
    private String nickName;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱")
    @Sensitive(strategy = SensitiveStrategy.EMAIL)
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过{max}个字符")
    private String email;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码")
    @Pattern(regexp = RegexConstants.MOBILE, message = "手机号格式不正确")
    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    @Schema(description = "用户性别（0男 1女 2未知）")
    private String sex;

}
