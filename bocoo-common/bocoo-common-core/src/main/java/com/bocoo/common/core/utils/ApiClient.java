/**
 * ApiClient 是一个基于 OkHttp 的高性能异步 HTTP 客户端工具类。
 * 提供了完整的 HTTP 操作支持，包括 GET/POST 请求、文件上传下载、全局请求头管理等功能。
 * 集成了连接池、线程池、重试机制、响应体大小限制等生产级特性，适用于高并发场景。
 */
package com.bocoo.common.core.utils;

import com.bocoo.common.core.config.ApiClientProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Component
public class ApiClient {

    /**
     * 请求体类型枚举，定义了支持的四种数据格式：
     * - JSON: JSON 格式数据
     * - XML: XML 格式数据
     * - FORM: 表单格式数据（key-value 对）
     * - BINARY: 二进制数据（文件、字节流等）
     */
    public enum RequestType { JSON, XML, FORM, BINARY }

    /** 配置属性注入，包含连接超时、线程池等配置参数 */
    private final ApiClientProperties properties;

    /** OkHttpClient 实例，用于执行 HTTP 请求，配置了连接池、超时等参数 */
    private final OkHttpClient client;

    /** 自定义线程池执行器，用于提交异步任务，提高并发处理能力 */
    private final ThreadPoolExecutor executor;

    /** OkHttp 内部调度器使用的线程池，用于管理 HTTP 请求的调度 */
    private final ExecutorService dispatcherExecutor;

    /** 全局请求头集合，使用 ConcurrentHashMap 保证线程安全，所有请求都会携带这些头 */
    private final Map<String, String> globalHeaders = new ConcurrentHashMap<>();

    /**
     * 构造函数初始化 OkHttpClient 和线程池（生产环境优化版）
     *
     * @param properties 配置属性对象，包含连接超时、读写超时、线程池参数等配置信息
     */
    public ApiClient(ApiClientProperties properties) {
        this.properties = properties;

        // 创建 OkHttp 调度器并配置客户端参数
        Dispatcher dispatcher = new Dispatcher();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSec()))  // 连接超时时间
                .readTimeout(Duration.ofSeconds(properties.getReadTimeoutSec()))        // 读取超时时间
                .writeTimeout(Duration.ofSeconds(properties.getWriteTimeoutSec()))      // 写入超时时间
                .retryOnConnectionFailure(false) // 禁用 OkHttp 自带重试，由自定义拦截器处理
                .followRedirects(true)           // 自动跟随重定向
                .followSslRedirects(true)        // 自动跟随 SSL 重定向
                .connectionPool(new ConnectionPool(  // 连接池配置
                        properties.getMaxIdleConnections(),     // 最大空闲连接数
                        properties.getKeepAliveMinutes(), TimeUnit.MINUTES)) // 连接保持时间
                .dispatcher(dispatcher)          // 设置调度器
                .addInterceptor(new RetryAndSizeLimitInterceptor()) // 添加自定义拦截器
                .build();

        this.dispatcherExecutor = dispatcher.executorService();

        // 初始化自定义线程池，用于处理异步任务
        this.executor = new ThreadPoolExecutor(
                properties.getCorePoolSize(),      // 核心线程数
                properties.getMaxPoolSize(),       // 最大线程数
                properties.getKeepAliveSeconds(), TimeUnit.SECONDS,  // 空闲线程存活时间
                new LinkedBlockingQueue<>(properties.getQueueCapacity()), // 任务队列
                (r, e) -> {  // 拒绝策略
                    // 如果是 CompletableFutureTask 类型，通知 future 任务被拒绝
                    if (r instanceof CompletableFutureTask) {
                        ((CompletableFutureTask<?>) r)
                                .future.completeExceptionally(new RejectedExecutionException("任务被线程池拒绝"));
                    }
                    // 记录拒绝日志
                    if (properties.isEnableLogging()) log.error("线程池任务被拒绝: {}", r);
                }
        );

        // 初始化完成日志
        if (properties.isEnableLogging()) {
            log.info("生产环境 ApiClient 初始化完成: 线程池核心/最大 = {}/{}, 队列 = {}, 连接池 = {}条/{}分钟",
                    properties.getCorePoolSize(), properties.getMaxPoolSize(),
                    properties.getQueueCapacity(),
                    properties.getMaxIdleConnections(),
                    properties.getKeepAliveMinutes()
            );
        }
    }

    /**
     * 初始化方法，在 Bean 创建后调用，打印初始化信息
     */
    @PostConstruct
    public void init() {
        if (properties.isEnableLogging()) {
            log.info("ApiClient 初始化完成, maxResponseSize={} 字节, logging={}",
                    properties.getMaxResponseSize(), properties.isEnableLogging());
        }
    }

    /* ---------------- 全局 Header 管理 ---------------- */

    /**
     * 设置全局请求头，所有请求都会携带这些头信息
     *
     * @param key   请求头键
     * @param value 请求头值
     */
    public void setGlobalHeader(String key, String value) { globalHeaders.put(key, value); }

    /**
     * 移除指定的全局请求头
     *
     * @param key 请求头键
     */
    public void removeGlobalHeader(String key) { globalHeaders.remove(key); }

    /**
     * 清空所有全局请求头
     */
    public void clearGlobalHeaders() { globalHeaders.clear(); }

    /* ---------------- GET 请求 ---------------- */

    /**
     * 发起异步 GET 请求，默认不携带额外请求头
     *
     * @param url 请求地址
     * @return CompletableFuture<String> 响应内容的异步包装
     */
    public CompletableFuture<String> getAsync(String url) { return getAsync(url, null); }

    /**
     * 发起异步 GET 请求，可携带自定义请求头
     *
     * @param url     请求地址
     * @param headers 自定义请求头映射
     * @return CompletableFuture<String> 响应内容的异步包装
     */
    public CompletableFuture<String> getAsync(String url, Map<String, String> headers) {
        Map<String, String> merged = mergeHeaders(headers);  // 合并全局和局部请求头
        Request.Builder builder = new Request.Builder().url(url).get();  // 构建 GET 请求
        merged.forEach(builder::addHeader);  // 添加请求头
        return executeAsync(builder.build(), "GET " + url);  // 异步执行请求
    }

    /* ---------------- POST 请求 ---------------- */

    /**
     * 发起异步 POST 请求，默认不携带额外请求头
     *
     * @param url  请求地址
     * @param data 请求体数据
     * @param type 请求体类型（JSON/XML/FORM/BINARY）
     * @return CompletableFuture<String> 响应内容的异步包装
     */
    public CompletableFuture<String> postAsync(String url, Object data, RequestType type) {
        return postAsync(url, data, type, null);
    }

    /**
     * 发起异步 POST 请求，可携带自定义请求头
     *
     * @param url     请求地址
     * @param data    请求体数据
     * @param type    请求体类型（JSON/XML/FORM/BINARY）
     * @param headers 自定义请求头映射
     * @return CompletableFuture<String> 响应内容的异步包装
     */
    public CompletableFuture<String> postAsync(String url, Object data, RequestType type, Map<String,String> headers) {
        RequestBody body = createRequestBody(data, type);    // 根据类型创建请求体
        Map<String,String> merged = mergeHeaders(headers);   // 合并请求头
        Request.Builder builder = new Request.Builder().url(url).post(body);  // 构建 POST 请求
        merged.forEach(builder::addHeader);  // 添加请求头
        return executeAsync(builder.build(), "POST " + url);  // 异步执行请求
    }

    /* ---------------- 文件上传 ---------------- */

    /**
     * 异步上传文件，使用默认字段名 "file"
     *
     * @param url     请求地址
     * @param file    要上传的文件对象
     * @param headers 自定义请求头映射
     * @return CompletableFuture<String> 响应内容的异步包装
     */
    public CompletableFuture<String> uploadFile(String url, File file, Map<String,String> headers) {
        return uploadFile(url, "file", file, headers);
    }

    /**
     * 异步上传文件，可指定字段名
     *
     * @param url       请求地址
     * @param fieldName 表单字段名称
     * @param file      要上传的文件对象
     * @param headers   自定义请求头映射
     * @return CompletableFuture<String> 响应内容的异步包装
     */
    public CompletableFuture<String> uploadFile(String url, String fieldName, File file, Map<String,String> headers) {
        return uploadFileInternal(url, fieldName, file, headers);
    }

    /* ---------------- 大文件流式上传 ---------------- */

    /**
     * 异步大文件流式上传（与 uploadFile 方法功能一致，保留用于语义区分）
     *
     * @param url       请求地址
     * @param fieldName 表单字段名称
     * @param file      要上传的文件对象
     * @param headers   自定义请求头映射
     * @return CompletableFuture<String> 响应内容的异步包装
     */
    public CompletableFuture<String> uploadLargeFile(String url, String fieldName, File file, Map<String,String> headers) {
        return uploadFileInternal(url, fieldName, file, headers);
    }

    /**
     * 内部通用文件上传方法，构建并执行文件上传请求
     *
     * @param url       请求地址
     * @param fieldName 表单字段名称
     * @param file      要上传的文件对象
     * @param headers   自定义请求头映射
     * @return CompletableFuture<String> 响应内容的异步包装
     */
    private CompletableFuture<String> uploadFileInternal(String url, String fieldName, File file, Map<String,String> headers) {
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));  // 创建文件请求体
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)  // 设置为表单类型
                .addFormDataPart(fieldName, file.getName(), fileBody)  // 添加文件部分
                .build();
        Map<String,String> merged = mergeHeaders(headers);  // 合并请求头
        Request.Builder builder = new Request.Builder().url(url).post(requestBody);  // 构建 POST 请求
        merged.forEach(builder::addHeader);  // 添加请求头
        return executeAsync(builder.build(), "UPLOAD " + url);  // 异步执行请求
    }

    /* ---------------- 大文件流式下载 ---------------- */

    /**
     * 异步下载文件并返回 InputStream 流，适用于大文件下载场景
     *
     * @param url     请求地址
     * @param headers 自定义请求头映射
     * @return CompletableFuture<InputStream> 文件输入流的异步包装
     */
    public CompletableFuture<InputStream> downloadFileStream(String url, Map<String, String> headers) {
        Map<String, String> merged = mergeHeaders(headers);  // 合并请求头
        Request.Builder builder = new Request.Builder().url(url).get();  // 构建 GET 请求
        merged.forEach(builder::addHeader);  // 添加请求头

        CompletableFuture<InputStream> future = new CompletableFuture<>();
        // 提交任务到线程池执行
        executor.submit(new CompletableFutureTask<>(future) {
            @Override
            public void run() {
                Response response = null;
                try {
                    response = client.newCall(builder.build()).execute();  // 执行请求
                    if (!response.isSuccessful()) {  // 检查响应状态
                        future.completeExceptionally(new IOException("下载失败，状态码: " + response.code()));
                        return;
                    }
                    ResponseBody body = response.body();
                    if (body == null) {  // 检查响应体
                        future.completeExceptionally(new IOException("响应体为空"));
                        return;
                    }
                    future.complete(body.byteStream());  // 返回输入流
                } catch (IOException e) {
                    future.completeExceptionally(e);  // 异常处理
                    if (response != null) {
                        response.close();  // 关闭响应
                    }
                }
            }
        });
        return future;
    }

    /* ---------------- 创建 RequestBody ---------------- */

    /**
     * 根据请求类型创建对应的 RequestBody 对象
     *
     * @param data 请求体数据
     * @param type 请求体类型（JSON/XML/FORM/BINARY）
     * @return RequestBody 构造好的请求体对象
     * @throws IllegalArgumentException 当数据格式不匹配或类型不支持时抛出异常
     */
    @SuppressWarnings("unchecked")
    private RequestBody createRequestBody(Object data, RequestType type) {
        switch (type) {
            case JSON:
                // JSON 类型：直接转换为字符串并设置媒体类型
                return RequestBody.create(data.toString(), MediaType.parse("application/json; charset=utf-8"));
            case XML:
                // XML 类型：直接转换为字符串并设置媒体类型
                return RequestBody.create(data.toString(), MediaType.parse("application/xml; charset=utf-8"));
            case FORM:
                // FORM 类型：需要是 Map<String,String> 格式
                if (!(data instanceof Map)) throw new IllegalArgumentException("Form 数据必须为 Map<String,String>");
                Map<String,String> map = (Map<String,String>) data;
                StringJoiner joiner = new StringJoiner("&");
                // 对每个键值对进行 URL 编码并拼接
                map.forEach((k,v) -> {
                    try {
                        joiner.add(URLEncoder.encode(k, StandardCharsets.UTF_8) + "=" +
                                URLEncoder.encode(v, StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("编码异常，key: " + k + ", value: " + v, e);
                    }
                });
                return RequestBody.create(joiner.toString(),
                        MediaType.parse("application/x-www-form-urlencoded"));
            case BINARY:
                // BINARY 类型：支持 byte[] 和 File 两种格式
                if (data instanceof byte[]) return RequestBody.create((byte[]) data,
                        MediaType.parse("application/octet-stream"));
                else if (data instanceof File) return RequestBody.create((File) data,
                        MediaType.parse("application/octet-stream"));
                throw new IllegalArgumentException("Unsupported binary data type: " + data.getClass());
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    /* ---------------- 合并 Header ---------------- */

    /**
     * 合并全局请求头和局部请求头，局部请求头会覆盖同名的全局请求头
     *
     * @param headers 局部请求头映射
     * @return 合并后的不可变 Map 映射
     */
    private Map<String,String> mergeHeaders(Map<String,String> headers) {
        Map<String,String> merged = new HashMap<>(globalHeaders);  // 先复制全局请求头
        if (headers != null) merged.putAll(headers);  // 再合并局部请求头（覆盖同名）
        return Collections.unmodifiableMap(merged);   // 返回不可变映射
    }

    /* ---------------- 执行请求（简化版，重试交给拦截器） ---------------- */

    /**
     * 执行异步 HTTP 请求并将响应字符串封装进 CompletableFuture 返回
     *
     * @param request    已构建的 Request 对象
     * @param actionDesc 请求描述信息，用于日志记录
     * @return CompletableFuture<String> 响应字符串的异步包装
     */
    private CompletableFuture<String> executeAsync(Request request, String actionDesc) {
        CompletableFuture<String> future = new CompletableFuture<>();
        // 提交任务到线程池执行
        executor.submit(new CompletableFutureTask<>(future) {
            @Override
            public void run() {
                try (Response response = client.newCall(request).execute()) {  // 执行请求
                    ResponseBody body = response.body();
                    // 将响应体转换为字符串并完成 future
                    future.complete(body != null ? body.string() : null);
                } catch (Throwable t) {
                    // 异常情况下完成 exceptionally
                    future.completeExceptionally(t);
                }
            }
        });
        return future;
    }

    /* ---------------- 优雅关闭 ---------------- */

    /**
     * 在 Bean 销毁前优雅关闭线程池和连接资源，防止资源泄露
     */
    @PreDestroy
    public void shutdown() {
        if (properties.isEnableLogging()) log.info("正在关闭 ApiClient 的线程池...");
        executor.shutdown();
        try {
            // 等待线程池任务完成，最多等待5秒
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                if (properties.isEnableLogging()) log.warn("线程池未在5秒内关闭，强制关闭...");
                executor.shutdownNow();  // 强制关闭
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }

        // 关闭调度器线程池
        dispatcherExecutor.shutdown();
        try {
            if (!dispatcherExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                dispatcherExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            dispatcherExecutor.shutdownNow();
        }

        // 清空连接池
        client.connectionPool().evictAll();
        if (properties.isEnableLogging()) log.info("ApiClient 已关闭");
    }

    /* ---------------- CompletableFutureTask 用于拒绝策略 ---------------- */

    /**
     * 抽象任务类，用于封装 CompletableFuture 的 Runnable 任务，
     * 便于在线程池拒绝策略中处理异常。
     *
     * @param <V> CompletableFuture 泛型类型
     */
    private static abstract class CompletableFutureTask<V> implements Runnable {
        final CompletableFuture<V> future;
        protected CompletableFutureTask(CompletableFuture<V> future) { this.future = future; }
    }

    /* ---------------- 拦截器：重试 + 响应体大小限制 ---------------- */

    /**
     * 拦截器类：实现请求重试机制和响应体大小限制检查
     * <p>
     * 重试策略采用指数退避算法，最大重试次数由配置决定；
     * 若响应体超过设定的最大字节数，则抛出异常拒绝处理。
     * </p>
     */
    private class RetryAndSizeLimitInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            IOException lastException = null;

            // 根据配置进行重试
            for (int attempt = 1; attempt <= properties.getMaxRetries(); attempt++) {
                try {
                    Response response = chain.proceed(request);  // 执行请求
                    ResponseBody body = response.body();

                    // 检查响应体大小限制
                    if (body != null && body.contentLength() > properties.getMaxResponseSize()) {
                        response.close();
                        throw new IOException("响应体过大: " + body.contentLength() + " 字节");
                    }

                    if (properties.isEnableLogging()) {
                        log.info("API调用 [{}] 成功, attempt={}", request.url(), attempt);
                    }

                    return response;  // 成功则返回响应
                } catch (IOException ex) {
                    lastException = ex;
                    if (properties.isEnableLogging()) {
                        log.warn("API调用 [{}] 异常, attempt={}", request.url(), attempt, ex);
                    }

                    if (attempt == properties.getMaxRetries()) break;  // 达到最大重试次数则退出

                    // 指数退避策略
                    try {
                        long sleepTime = Math.min(properties.getBaseRetryIntervalMs() * (1L << (attempt - 1)),
                                properties.getMaxRetryIntervalMs());
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("请求被中断", ie);
                    }
                }
            }
            // 抛出最后一次异常或默认异常
            throw lastException != null ? lastException : new IOException("未知异常");
        }
    }
}
