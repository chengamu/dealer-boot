package com.bocoo.common.core.utils.file;

import com.bocoo.common.core.config.BocooConfig;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.constant.Constants;
import com.bocoo.common.core.utils.ApiClient;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.io.*;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

/**
 * 图片处理工具类（统一处理 Base64 / URL / 本地路径）
 * 使用 ApiClient 下载网络图片
 * cmx
 */
@Component
public class ImageUtils implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    private static ApiClient getApiClient() {
        return applicationContext.getBean(ApiClient.class);
    }

    private static BocooConfig getBocooConfig() {
        return applicationContext.getBean(BocooConfig.class);
    }

    // =================== 原有接口 ===================

    public static byte[] getImageBytes(String imagePathOrBase64) {
        return getImageBytes(imagePathOrBase64, -1, -1);
    }

    public static byte[] getImage(String imagePath) {
        return getImage(imagePath, -1, -1);
    }

    public static InputStream getFile(String imagePath) {
        if (StringUtils.isEmpty(imagePath)) return null;
        try {
            byte[] result = readFile(imagePath);
            if (result == null) return null;
            return new ByteArrayInputStream(result);
        } catch (Exception e) {
            log.error("获取图片异常，路径：{}", imagePath, e);
            return null;
        }
    }

    public static String getImageBase64(String imagePathOrBase64) {
        byte[] bytes = getImageBytes(imagePathOrBase64);
        if (bytes == null || bytes.length == 0) return "";
        return Base64.getEncoder().encodeToString(bytes);
    }

    // =================== 新增压缩重载 ===================

    public static byte[] getImageBytes(String imagePathOrBase64, int maxKB, int targetWidth) {
        if (StringUtils.isEmpty(imagePathOrBase64)) return new byte[0];

        byte[] bytes;
        if (!imagePathOrBase64.startsWith("http") && !imagePathOrBase64.startsWith("/")) {
            try {
                bytes = Base64.getDecoder().decode(imagePathOrBase64);
            } catch (IllegalArgumentException e) {
                log.error("Base64 解码失败", e);
                return new byte[0];
            }
        } else {
            bytes = getImage(imagePathOrBase64, maxKB, targetWidth);
        }

        if (maxKB > 0 || targetWidth > 0) {
            try {
                bytes = compressToJpeg(bytes, maxKB, targetWidth);
            } catch (IOException e) {
                log.error("图片压缩失败", e);
            }
        }

        return bytes;
    }

    public static byte[] getImage(String imagePath, int maxKB, int targetWidth) {
        if (StringUtils.isEmpty(imagePath)) return new byte[0];

        try (InputStream is = getFile(imagePath)) {
            if (is == null) return new byte[0];
            byte[] bytes = IOUtils.toByteArray(is);

            if (maxKB > 0 || targetWidth > 0) {
                bytes = compressToJpeg(bytes, maxKB, targetWidth);
            }

            return bytes;
        } catch (Exception e) {
            log.error("图片加载异常，路径：{}", imagePath, e);
            return new byte[0];
        }
    }

    // =================== 压缩核心方法 ===================

    private static byte[] compressToJpeg(byte[] imageBytes, int maxKB, int targetWidth) throws IOException {
        if (imageBytes == null || imageBytes.length == 0) return imageBytes;

        BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (original == null) return imageBytes;

        // 等比缩放
        if (targetWidth > 0 && original.getWidth() > targetWidth) {
            int newHeight = targetWidth * original.getHeight() / original.getWidth();
            BufferedImage resized = new BufferedImage(targetWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, targetWidth, newHeight, null);
            g.dispose();
            original = resized;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        float quality = 0.9f;
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) return imageBytes;
        ImageWriter writer = writers.next();

        while (true) {
            baos.reset();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            writer.setOutput(new MemoryCacheImageOutputStream(baos));
            writer.write(null, new IIOImage(original, null, null), param);
            writer.dispose();

            if ((maxKB <= 0 || baos.size() / 1024 <= maxKB) || quality <= 0.1f) break;
            quality -= 0.1f;
        }

        return baos.toByteArray();
    }

    // =================== readFile 保留 ApiClient 支持 ===================

    public static byte[] readFile(String path) {
        if (StringUtils.isEmpty(path)) return null;

        try {
            if (path.startsWith("http")) {
                ApiClient apiClient = getApiClient();
                CompletableFuture<InputStream> future = apiClient.downloadFileStream(path, null);
                try (InputStream in = future.get(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
                    return out.toByteArray();
                }
            } else {
                BocooConfig config = getBocooConfig();
                if (config == null || config.getProfile() == null) return null;
                String downloadPath = config.getProfile() + StringUtils.substringAfter(path, Constants.RESOURCE_PREFIX);
                try (InputStream in = new FileInputStream(downloadPath)) {
                    return IOUtils.toByteArray(in);
                }
            }
        } catch (Exception e) {
            log.error("获取文件路径异常，路径：{}", path, e);
            return null;
        }
    }
}
