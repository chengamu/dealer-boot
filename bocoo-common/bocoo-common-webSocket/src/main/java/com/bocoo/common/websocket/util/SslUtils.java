package com.bocoo.common.websocket.util;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.net.URL;
import java.security.KeyStore;

/**
 * SSL工具类
 *
 * 该类提供创建SSL上下文的功能，用于WebSocket服务器的SSL/TLS加密通信。
 * 参考了Spring Boot Netty SSL自定义器的实现。
 *
 * @author bocoo
 * @since 1.0.0
 */
public final class SslUtils {

    /**
     * 创建SSL上下文
     *
     * 根据提供的密钥库和信任库信息创建Netty的SslContext对象。
     *
     * @param keyPassword 密钥密码
     * @param keyStoreResource 密钥库资源路径
     * @param keyStoreType 密钥库类型
     * @param keyStorePassword 密钥库密码
     * @param trustStoreResource 信任库资源路径
     * @param trustStoreType 信任库类型
     * @param trustStorePassword 信任库密码
     * @return SslContext SSL上下文对象
     * @throws SSLException SSL异常
     */
    public static SslContext createSslContext(String keyPassword, String keyStoreResource, String keyStoreType, String keyStorePassword, String trustStoreResource, String trustStoreType, String trustStorePassword) throws SSLException {
        SslContextBuilder sslBuilder = SslContextBuilder
                .forServer(getKeyManagerFactory(keyStoreType, keyStoreResource, keyPassword, keyStorePassword))
                .trustManager(getTrustManagerFactory(trustStoreType, trustStoreResource, trustStorePassword));
        return sslBuilder.build();
    }

    /**
     * 获取密钥管理器工厂
     *
     * 根据密钥库信息创建并初始化密钥管理器工厂。
     *
     * @param type 密钥库类型
     * @param resource 密钥库资源路径
     * @param keyPassword 密钥密码
     * @param keyStorePassword 密钥库密码
     * @return KeyManagerFactory 密钥管理器工厂
     */
    private static KeyManagerFactory getKeyManagerFactory(String type, String resource, String keyPassword, String keyStorePassword) {
        try {
            KeyStore keyStore = loadKeyStore(type, resource, keyStorePassword);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            char[] keyPasswordBytes = (!StringUtils.isEmpty(keyPassword)
                    ? keyPassword.toCharArray() : null);
            if (keyPasswordBytes == null && !StringUtils.isEmpty(keyStorePassword)) {
                keyPasswordBytes = keyStorePassword.toCharArray();
            }
            keyManagerFactory.init(keyStore, keyPasswordBytes);
            return keyManagerFactory;
        } catch (Exception ex) {
            throw new IllegalStateException("创建密钥管理器工厂失败", ex);
        }
    }

    /**
     * 获取信任管理器工厂
     *
     * 根据信任库信息创建并初始化信任管理器工厂。
     *
     * @param trustStoreType 信任库类型
     * @param trustStoreResource 信任库资源路径
     * @param trustStorePassword 信任库密码
     * @return TrustManagerFactory 信任管理器工厂
     */
    private static TrustManagerFactory getTrustManagerFactory(String trustStoreType, String trustStoreResource, String trustStorePassword) {
        try {
            KeyStore store = loadKeyStore(trustStoreType, trustStoreResource, trustStorePassword);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(store);
            return trustManagerFactory;
        } catch (Exception ex) {
            throw new IllegalStateException("创建信任管理器工厂失败", ex);
        }
    }

    /**
     * 加载密钥库
     *
     * 从指定资源路径加载密钥库文件。
     *
     * @param type 密钥库类型
     * @param resource 密钥库资源路径
     * @param password 密钥库密码
     * @return KeyStore 密钥库对象
     * @throws Exception 异常
     */
    private static KeyStore loadKeyStore(String type, String resource, String password)
            throws Exception {
        type = (StringUtils.isEmpty(type) ? "JKS" : type);
        if (StringUtils.isEmpty(resource)) {
            return null;
        }
        KeyStore store = KeyStore.getInstance(type);
        URL url = ResourceUtils.getURL(resource);
        store.load(url.openStream(), StringUtils.isEmpty(password) ? null : password.toCharArray());
        return store;
    }
}
