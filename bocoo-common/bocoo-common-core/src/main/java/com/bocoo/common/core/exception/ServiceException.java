package com.bocoo.common.core.exception;

import com.bocoo.common.core.utils.MessageUtils;

/**
 * 业务异常
 *
 * @author ruoyi
 */
public final class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 国际化消息键
     */
    private String messageKey;

    /**
     * 国际化消息参数
     */
    private Object[] args;

    /**
     * 错误明细，内部调试错误
     * <p>
     * 和 {@link CommonResult#getDetailMessage()} 一致的设计
     */
    private String detailMessage;

    /**
     * 空构造方法，避免反序列化问题
     */
    public ServiceException() {
    }

    public ServiceException(String message) {
        this.message = message;
    }

    public ServiceException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public ServiceException(String message, Integer code,String detailMessage) {
        this.message = message;
        this.code = code;
        this.detailMessage = detailMessage;
    }

    public static ServiceException ofMessageKey(String messageKey, Object... args) {
        ServiceException exception = new ServiceException();
        exception.messageKey = messageKey;
        exception.args = args;
        return exception;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    @Override
    public String getMessage() {
        if (messageKey != null && !messageKey.isEmpty()) {
            return MessageUtils.message(messageKey, args);
        }
        return message;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }

    public Integer getCode() {
        return code;
    }

    public ServiceException setCode(Integer code) {
        this.code = code;
        return this;
    }

    public ServiceException setMessage(String message) {
        this.message = message;
        this.messageKey = null;
        this.args = null;
        return this;
    }

    public ServiceException setMessageKey(String messageKey, Object... args) {
        this.messageKey = messageKey;
        this.args = args;
        return this;
    }

    public ServiceException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }
}
