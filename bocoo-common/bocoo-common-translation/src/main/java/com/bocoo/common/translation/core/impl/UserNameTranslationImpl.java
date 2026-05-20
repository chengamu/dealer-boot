package com.bocoo.common.translation.core.impl;

import com.bocoo.common.core.service.UserService;
import com.bocoo.common.translation.annotation.TranslationType;
import com.bocoo.common.translation.constant.TransConstant;
import com.bocoo.common.translation.core.TranslationInterface;
import lombok.AllArgsConstructor;

/**
 * 用户名翻译实现
 *
 * @author Lion Li
 */
@AllArgsConstructor
@TranslationType(type = TransConstant.USER_ID_TO_NAME)
public class UserNameTranslationImpl implements TranslationInterface<String> {

    private final UserService userService;

    @Override
    public String translation(Object key, String other) {
        if (key instanceof Long id) {
            return userService.selectUserNameById(id);
        }
        return null;
    }
}
