package com.bocoo.common.core.exception.file;

import java.io.Serial;

/**
 * 文件上传异常类
 *
 * @author cmx
 */
public class FileUploadException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public FileUploadException() {
        super();
    }

    public FileUploadException(final String msg) {
        super(msg);
    }

    public FileUploadException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
