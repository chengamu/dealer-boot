package com.bocoo.common.core.constant;

/**
 * 缓存的key 常量
 *
 * @author ruoyi
 */
public interface CacheConstants {

    /**
     * 在线用户 redis key
     */
    String ONLINE_TOKEN_KEY = "online_tokens:";

    /**
     * 验证码 redis key
     */
    String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * SMS login captcha redis key
     */
    String SMS_LOGIN_CODE_KEY = "captcha_codes:sms_login:";

    /**
     * Email login captcha redis key
     */
    String EMAIL_LOGIN_CODE_KEY = "captcha_codes:email_login:";

    /**
     * 参数管理 cache key
     */
    String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    String PWD_ERR_CNT_KEY = "pwd_err_cnt:";
}
