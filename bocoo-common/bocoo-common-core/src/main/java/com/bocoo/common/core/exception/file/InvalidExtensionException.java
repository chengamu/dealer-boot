package com.bocoo.common.core.exception.file;

import java.io.Serial;
import java.util.Arrays;

/**
 * 文件上传 误异常类
 * 
 * @author cmx
 */
public class InvalidExtensionException extends FileUploadException
{
    @Serial
    private static final long serialVersionUID = 1L;

    private final String[] allowedExtension;
    private final String extension;
    private final String filename;

    /**
     * 构造一个InvalidExtensionException实例
     *
     * @param allowedExtension 允许的文件扩展名数组
     * @param extension 实际上传文件的扩展名
     * @param filename 上传的文件名
     */
    public InvalidExtensionException(String[] allowedExtension, String extension, String filename)
    {
        super(buildMessage(allowedExtension, extension, filename));
        // 防御性拷贝
        this.allowedExtension = allowedExtension != null ? allowedExtension.clone() : null;
        this.extension = extension;
        this.filename = filename;
    }

    /**
     * 构建错误信息
     *
     * @param allowedExtension 允许的文件扩展名数组
     * @param extension 实际上传文件的扩展名
     * @param filename 上传的文件名
     * @return 错误信息字符串
     */
    private static String buildMessage(String[] allowedExtension, String extension, String filename) {
        StringBuilder sb = new StringBuilder();
        sb.append("文件[").append(filename).append("]后缀[").append(extension).append("]不正确，请上传")
          .append(Arrays.toString(allowedExtension)).append("格式");
        return sb.toString();
    }

    /**
     * 获取允许的文件扩展名数组（返回副本以防止外部修改）
     *
     * @return 允许的文件扩展名数组副本
     */
    public String[] getAllowedExtension()
    {
        // 返回副本以防止外部修改
        return allowedExtension != null ? allowedExtension.clone() : null;
    }

    /**
     * 获取实际上传文件的扩展名
     *
     * @return 文件扩展名
     */
    public String getExtension()
    {
        return extension;
    }

    /**
     * 获取上传的文件名
     *
     * @return 文件名
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * 图片文件扩展名无效异常类
     */
    public static class InvalidImageExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        /**
         * 构造一个InvalidImageExtensionException实例
         *
         * @param allowedExtension 允许的文件扩展名数组
         * @param extension 实际上传文件的扩展名
         * @param filename 上传的文件名
         */
        public InvalidImageExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }

    /**
     * Flash文件扩展名无效异常类
     */
    public static class InvalidFlashExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        /**
         * 构造一个InvalidFlashExtensionException实例
         *
         * @param allowedExtension 允许的文件扩展名数组
         * @param extension 实际上传文件的扩展名
         * @param filename 上传的文件名
         */
        public InvalidFlashExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }

    /**
     * 媒体文件扩展名无效异常类
     */
    public static class InvalidMediaExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        /**
         * 构造一个InvalidMediaExtensionException实例
         *
         * @param allowedExtension 允许的文件扩展名数组
         * @param extension 实际上传文件的扩展名
         * @param filename 上传的文件名
         */
        public InvalidMediaExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }

    /**
     * 视频文件扩展名无效异常类
     */
    public static class InvalidVideoExtensionException extends InvalidExtensionException
    {
        private static final long serialVersionUID = 1L;

        /**
         * 构造一个InvalidVideoExtensionException实例
         *
         * @param allowedExtension 允许的文件扩展名数组
         * @param extension 实际上传文件的扩展名
         * @param filename 上传的文件名
         */
        public InvalidVideoExtensionException(String[] allowedExtension, String extension, String filename)
        {
            super(allowedExtension, extension, filename);
        }
    }
}
