package com.bocoo.common.mybatis.helper;

import cn.hutool.core.convert.Convert;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 数据库助手
 *
 * @author Lion Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataBaseHelper {

    public static String findInSet(Object var1, String var2) {
        String var = Convert.toStr(var1);
        return "concat(',', %s, ',') like '%%,%s,%%'".formatted(var2, var);
    }
}
