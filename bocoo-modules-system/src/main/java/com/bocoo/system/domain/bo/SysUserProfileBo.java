package com.bocoo.system.domain.bo;

import com.bocoo.common.core.constant.RegexConstants;
import com.bocoo.common.core.xss.Xss;
import com.bocoo.common.mybatis.core.domain.BaseBo;
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
public class SysUserProfileBo extends BaseBo {

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    @Xss(message = "{validation.user.nickname.xss}")
    @Size(min = 0, max = 30, message = "{validation.user.nickname.max}")
    private String nickName;

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
    @Pattern(regexp = RegexConstants.MOBILE, message = "{validation.phone.invalid}")
    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    @Schema(description = "用户性别（0男 1女 2未知）")
    private String sex;

}
