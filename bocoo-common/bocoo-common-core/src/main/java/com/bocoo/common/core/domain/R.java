package com.bocoo.common.core.domain;

import com.bocoo.common.core.constant.HttpStatus;
import com.bocoo.common.core.utils.MessageUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应信息主体
 *
 * @author cmx
 */
@Data
@NoArgsConstructor
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功状态码
     */
    public static final int SUCCESS = 200;

    /**
     * 失败状态码
     */
    public static final int FAIL = 500;

    /**
     * 响应状态码
     */
    private int code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应详细信息
     */
    private String detailMessage;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 返回成功响应（无数据）
     *
     * @param <T> 泛型类型
     * @return 成功响应对象
     */
    public static <T> R<T> ok() {
        return restResult(null, SUCCESS, MessageUtils.message("common.success"));
    }

    /**
     * 返回成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  泛型类型
     * @return 成功响应对象
     */
    public static <T> R<T> ok(T data) {
        return restResult(data, SUCCESS, MessageUtils.message("common.success"));
    }

    /**
     * 返回成功响应（带消息）
     *
     * @param msg 响应消息
     * @param <T> 泛型类型
     * @return 成功响应对象
     */
    public static <T> R<T> ok(String msg) {
        return restResult(null, SUCCESS, msg);
    }

    /**
     * 返回成功响应（带消息和数据）
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @param <T>  泛型类型
     * @return 成功响应对象
     */
    public static <T> R<T> ok(String msg, T data) {
        return restResult(data, SUCCESS, msg);
    }

    /**
     * 返回失败响应（无数据）
     *
     * @param <T> 泛型类型
     * @return 失败响应对象
     */
    public static <T> R<T> fail() {
        return restResult(null, FAIL, MessageUtils.message("common.failed"));
    }

    /**
     * 返回失败响应（带消息）
     *
     * @param msg 响应消息
     * @param <T> 泛型类型
     * @return 失败响应对象
     */
    public static <T> R<T> fail(String msg) {
        return restResult(null, FAIL, msg);
    }

    /**
     * 返回失败响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  泛型类型
     * @return 失败响应对象
     */
    public static <T> R<T> fail(T data) {
        return restResult(data, FAIL, MessageUtils.message("common.failed"));
    }

    /**
     * 返回失败响应（带消息和数据）
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @param <T>  泛型类型
     * @return 失败响应对象
     */
    public static <T> R<T> fail(String msg, T data) {
        return restResult(data, FAIL, msg);
    }

    /**
     * 返回自定义状态码的失败响应（无数据）
     *
     * @param code 状态码
     * @param msg  响应消息
     * @param <T>  泛型类型
     * @return 失败响应对象
     */
    public static <T> R<T> fail(int code, String msg) {
        return restResult(null, code, msg);
    }

    /**
     * 返回自定义状态码的失败响应（带详细信息）
     *
     * @param code           状态码
     * @param msg            响应消息
     * @param detailMessage  详细错误信息
     * @param <T>            泛型类型
     * @return 失败响应对象
     */
    public static <T> R<T> fail(int code, String msg,String detailMessage) {
        return restResult(null, code, msg,detailMessage);
    }

    /**
     * 返回警告消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static <T> R<T> warn(String msg) {
        return restResult(null, HttpStatus.WARN, msg);
    }

    /**
     * 返回警告消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static <T> R<T> warn(String msg, T data) {
        return restResult(data, HttpStatus.WARN, msg);
    }

    /**
     * 构建响应结果对象（不包含详细信息）
     *
     * @param data 响应数据
     * @param code 响应状态码
     * @param msg  响应消息
     * @param <T>  泛型类型
     * @return 响应结果对象
     */
    private static <T> R<T> restResult(T data, int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMsg(msg);
        return r;
    }

    /**
     * 构建响应结果对象（包含详细信息）
     *
     * @param data           响应数据
     * @param code           响应状态码
     * @param msg            响应消息
     * @param detailMessage  详细错误信息
     * @param <T>            泛型类型
     * @return 响应结果对象
     */
    private static <T> R<T> restResult(T data, int code, String msg,String detailMessage) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMsg(msg);
        r.setDetailMessage(detailMessage);
        return r;
    }

    /**
     * 判断响应是否为错误状态
     *
     * @param ret 响应对象
     * @param <T> 泛型类型
     * @return 是否为错误状态
     */
    public static <T> Boolean isError(R<T> ret) {
        return !isSuccess(ret);
    }

    /**
     * 判断响应是否为成功状态
     *
     * @param ret 响应对象
     * @param <T> 泛型类型
     * @return 是否为成功状态
     */
    public static <T> Boolean isSuccess(R<T> ret) {
        return R.SUCCESS == ret.getCode();
    }
}
