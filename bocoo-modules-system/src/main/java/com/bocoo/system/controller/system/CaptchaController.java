package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.bocoo.common.core.constant.CacheConstants;
import com.bocoo.common.core.constant.Constants;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.utils.SpringUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.reflect.ReflectUtils;
import com.bocoo.common.ratelimiter.annotation.RateLimiter;
import com.bocoo.common.ratelimiter.enums.LimitType;
import com.bocoo.common.redis.utils.RedisUtils;
import com.bocoo.common.web.config.properties.CaptchaProperties;
import com.bocoo.common.web.enums.CaptchaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.provider.enumerate.SupplierType;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 验证码操作处理
 *
 * @author CMX
 */
@SaIgnore
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@Tag(name = "验证码管理", description = "验证码操作处理接口")
public class CaptchaController {

    private final CaptchaProperties captchaProperties;

    /**
     * 短信验证码
     *
     * @param phonenumber 用户手机号
     */
    @GetMapping("/captchaSms")
    @RateLimiter(count = 5, time = 60, limitType = LimitType.IP)
    @Operation(summary = "获取短信验证码", description = "发送短信验证码到指定手机号")
    public R<Void> smsCaptcha(
            @Parameter(description = "用户手机号", required = true)
            @NotBlank(message = "{user.phonenumber.not.blank}")
            String phonenumber) {
        String key = CacheConstants.SMS_LOGIN_CODE_KEY + phonenumber;
        String code = RandomUtil.randomNumbers(6);
        RedisUtils.setCacheObject(key, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        // 验证码模板id 自行处理 (查数据库或写死均可)
        String templateId = "";
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("code", code);
        SmsBlend smsBlend = SmsFactory.createSmsBlend(SupplierType.ALIBABA);
        SmsResponse smsResponse = smsBlend.sendMessage(phonenumber, templateId, map);
        if (!"OK".equals(smsResponse.getCode())) {
            log.error("验证码短信发送异常 => {}", smsResponse);
            return R.fail(smsResponse.getMessage());
        }
        return R.ok();
    }

    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    @RateLimiter(count = 20, time = 60, limitType = LimitType.IP)
    @Operation(summary = "生成图形验证码", description = "生成图形验证码并返回base64图像")
    public R<Map<String, Object>> getCode() {
        boolean captchaEnabled = captchaProperties.getEnable();
        if (!captchaEnabled) {
            return R.ok(Map.of("captchaEnabled", captchaEnabled));
        }
        // 保存验证码信息
        String uuid = IdUtil.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        // 生成验证码
        CaptchaType captchaType = captchaProperties.getType();
        boolean isMath = CaptchaType.MATH == captchaType;
        Integer length = isMath ? captchaProperties.getNumberLength() : captchaProperties.getCharLength();
        CodeGenerator codeGenerator = ReflectUtils.newInstance(captchaType.getClazz(), length);
        AbstractCaptcha captcha = SpringUtils.getBean(captchaProperties.getCategory().getClazz());
        captcha.setGenerator(codeGenerator);
        captcha.createCode();
        String code = captcha.getCode();
        if (isMath) {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(StringUtils.remove(code, "="));
            code = exp.getValue(String.class);
        }
        RedisUtils.setCacheObject(verifyKey, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        return R.ok(Map.of("uuid", uuid, "img", captcha.getImageBase64()));
    }

}
