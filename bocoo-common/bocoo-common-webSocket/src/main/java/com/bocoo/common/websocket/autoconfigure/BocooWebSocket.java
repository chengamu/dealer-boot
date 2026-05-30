package com.bocoo.common.websocket.autoconfigure;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.common.core.domain.bo.LoginUser;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import com.bocoo.common.websocket.annotation.*;
import com.bocoo.common.websocket.pojo.Session;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket服务端点实现类
 * <p>
 * 该类实现了基于Netty的WebSocket服务器功能，包括用户认证、连接管理、消息处理等核心功能。
 * 使用Sa-Token进行用户身份验证，只通过token参数即可完成用户识别和认证。
 *
 * @author bocoo
 * @since 1.0.0
 */
@ServerEndpoint(path = "/ws", port = "${websocket.port}", readerIdleTimeSeconds = "55", corsOrigins = "*")
@Slf4j
@Component
public class BocooWebSocket {

    /**
     * 用户会话池，存储每个用户的所有WebSocket会话
     * key: 用户ID
     * value: 该用户的所有会话队列
     */
    @Getter
    private static final ConcurrentHashMap<String, ConcurrentLinkedQueue<Session>> sessionPools = new ConcurrentHashMap<>();

    /**
     * TOKEN到会话的映射，确保每个TOKEN的唯一性
     * key: TOKEN
     * value: Session
     */
    private static final ConcurrentHashMap<String, Session> tokenSessionMap = new ConcurrentHashMap<>();

    /**
     * 在线用户数量计数器
     */
    private static final AtomicInteger onlineCount = new AtomicInteger(0);

    private static String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "<empty>";
        }
        int length = token.length();
        if (length <= 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." + token.substring(length - 4);
    }

    /**
     * WebSocket握手前处理方法
     * <p>
     * 在WebSocket连接建立之前进行用户身份验证，只有通过验证的用户才能建立连接。
     * 如果TOKEN已存在连接，则拒绝新的连接请求。
     *
     * @param session WebSocket会话对象
     * @param headers HTTP请求头
     * @param reqMap  请求参数映射
     */
    @BeforeHandshake
    public void handshake(Session session, HttpHeaders headers, @RequestParam MultiValueMap<String, String> reqMap) {
        try {
            List<String> tokenList = reqMap.get("token");

            if (ObjectUtil.isEmpty(tokenList)) {
                log.error("未提供认证信息");
                session.close();
                return;
            }

            String token = tokenList.get(0);

            LoginUser loginUser = getLoginUser(token);
            if (loginUser == null) {
                log.error("用户认证失败, token:{}", maskToken(token));
                session.close();
                return;
            }

            // 检查TOKEN是否已存在连接
            if (tokenSessionMap.containsKey(token)) {
                log.warn("TOKEN {} 已存在活动连接，拒绝新的连接请求", maskToken(token));
                // 发送明确的拒绝消息
                try {
                    session.sendText("{\"type\":\"error\",\"message\":\"该账户已在其他地方登录，拒绝重复连接\"}");
                } catch (Exception e) {
                    log.error("发送拒绝消息失败", e);
                }
                session.close();
                return;
            }

            session.setAttribute("userId", loginUser.getUserId().toString());
            session.setAttribute("token", token);
            session.setAttribute("authenticated", true);

            log.info("用户认证成功: userId={}, token={}", loginUser.getUserId(), maskToken(token));
        } catch (Exception e) {
            log.error("连接认证错误:{}", e.getMessage(), e);
            session.close();
        }
    }

    /**
     * WebSocket连接建立处理方法
     * <p>
     * 当WebSocket连接成功建立时调用此方法，将用户会话添加到会话池中，
     * 并更新在线用户数量统计。
     *
     * @param session WebSocket会话对象
     * @throws IOException IO异常
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        if (!isAuthenticated(session)) {
            log.error("连接建立时用户认证失败");
            session.close();
            return;
        }

        String token = session.getAttribute("token");
        String userId = session.getAttribute("userId");

        // 再次检查TOKEN是否已存在连接（双重检查，防止并发问题）
        if (tokenSessionMap.containsKey(token)) {
            log.warn("TOKEN {} 已存在活动连接，拒绝新的连接请求", maskToken(token));
            try {
                session.sendText("{\"type\":\"error\",\"message\":\"该账户已在其他地方登录，拒绝重复连接\"}");
            } catch (Exception e) {
                log.error("发送拒绝消息失败", e);
            }
            session.close();
            return;
        }

        // 注册TOKEN和会话的映射关系
        tokenSessionMap.put(token, session);
        addUserSession(userId, session);
        onlineCount.incrementAndGet();
        log.info("用户{}:加入webSocket！sessionid:{}, token:{}, 当前在线人数：{}",
                userId, session.id(), maskToken(token), getOnlineNumber());
    }

    /**
     * WebSocket连接关闭处理方法
     * <p>
     * 当WebSocket连接关闭时调用此方法，从会话池中移除用户会话，
     * 并更新在线用户数量统计。
     *
     * @param session WebSocket会话对象
     * @throws IOException IO异常
     */
    @OnClose
    public void onClose(Session session) throws IOException {
        String userId = session.getAttribute("userId");
        String token = session.getAttribute("token");

        if (userId != null && token != null) {
            // 移除TOKEN和会话的映射关系
            tokenSessionMap.remove(token);
            removeUserSession(userId, session);
            onlineCount.decrementAndGet();
            log.info("用户{}:断开连接,sessionId:{}, token:{},当前在线客户端数:{}",
                    userId, session.id(), maskToken(token), getOnlineNumber());
        } else {
            log.warn("未认证连接断开, sessionId:{}", session.id());
        }
    }

    /**
     * WebSocket异常处理方法
     * <p>
     * 当WebSocket连接发生异常时调用此方法，记录异常信息并清理相关资源。
     *
     * @param session   WebSocket会话对象
     * @param throwable 异常对象
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("连接出现错误：{}", throwable.getMessage(), throwable);
        try {
            String userId = session.getAttribute("userId");
            String token = session.getAttribute("token");

            if (userId != null && token != null) {
                // 移除TOKEN和会话的映射关系
                tokenSessionMap.remove(token);
                removeUserSession(userId, session);
                onlineCount.decrementAndGet();
                log.info("用户{}:异常断开连接,sessionId:{}, token:{}", userId, session.id(), maskToken(token));
            } else {
                log.warn("未认证连接出现错误, sessionId:{}", session.id());
            }
        } catch (Exception e) {
            log.error("清理异常连接失败", e);
        }
    }

    /**
     * 文本消息处理方法
     * <p>
     * 当接收到客户端发送的文本消息时调用此方法，处理具体业务逻辑。
     *
     * @param session WebSocket会话对象
     * @param message 接收到的文本消息
     */
    @OnMessage
    public void OnMessage(Session session, String message) {

        if (!isAuthenticated(session)) return;


        String token = session.getAttribute("token");
        if (token != null) {
            // 续签token有效期
            StpUtil.renewTimeout(token, StpUtil.getTokenTimeout(token));
        }

        if ("PING".equals(message)) {
            session.sendText("PONG");
            return;
        }

        log.info("接收到消息:{}", message);
        session.sendText("消息已收到: " + message);
    }

    /**
     * 二进制消息处理方法
     * <p>
     * 当接收到客户端发送的二进制消息时调用此方法，处理具体业务逻辑。
     *
     * @param session WebSocket会话对象
     * @param bytes   接收到的二进制数据
     */
    @OnBinary
    public void onBinary(Session session, byte[] bytes) {
        if (!isAuthenticated(session)) return;

        for (byte b : bytes) {
            log.trace("接收到二进制数据: {}", b);
        }
        session.sendBinary(bytes);
    }

    /**
     * 事件处理方法
     * <p>
     * 当接收到Netty底层事件时调用此方法，如心跳检测事件等。
     *
     * @param session WebSocket会话对象
     * @param evt     事件对象
     */
    @OnEvent
    public void onEvent(Session session, Object evt) {
        if (!isAuthenticated(session)) return;

        log.debug("接收到Netty事件: {}, 来自sessionId:{}", JSONObject.toJSONString(evt), session.id());
        if (evt instanceof IdleStateEvent idleStateEvent) {
            switch (idleStateEvent.state()) {
                case READER_IDLE -> log.debug("读空闲");
                case WRITER_IDLE -> log.debug("写空闲");
                case ALL_IDLE -> log.debug("全部空闲");
            }
        }
    }

    /**
     * 发送消息给指定会话
     * <p>
     * 向指定的WebSocket会话发送文本消息，确保会话处于活跃状态。
     *
     * @param session WebSocket会话对象
     * @param message 要发送的消息
     * @return 发送是否成功
     */
    public boolean sendMessage(Session session, String message) {
        if (session != null && session.channel().isActive()) {
            synchronized (session) {
                try {
                    log.debug("发送数据：{}", message);
                    session.sendText(message);
                    return true;
                } catch (Exception e) {
                    log.error("发送消息失败, sessionId:{}, 消息:{}", session.id(), message, e);
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 发送消息给指定用户
     * <p>
     * 向指定用户的所有WebSocket会话发送文本消息（支持多设备）。
     *
     * @param userId  用户ID
     * @param message 要发送的消息
     * @return 成功发送的会话数量
     */
    public int sendInfo(String userId, String message) {
        ConcurrentLinkedQueue<Session> sessions = sessionPools.get(userId);
        if (sessions == null) {
            log.warn("用户{}不在线，无法发送消息", userId);
            return 0;
        }

        int successCount = 0;
        try {
            // 创建副本避免并发修改异常
            ConcurrentLinkedQueue<Session> sessionsCopy = new ConcurrentLinkedQueue<>(sessions);
            for (Session session : sessionsCopy) {
                // 检查会话是否仍然有效
                String token = session.getAttribute("token");
                if (token != null && tokenSessionMap.get(token) == session && session.channel().isActive()) {
                    if (sendMessage(session, message)) {
                        successCount++;
                    }
                } else {
                    // 会话已失效，从用户会话池中移除
                    removeUserSession(userId, session);
                }
            }
            log.debug("向用户{}发送消息成功{}个会话", userId, successCount);
        } catch (Exception e) {
            log.error("发送消息给用户 {} 出错", userId, e);
        }

        return successCount;
    }

    /**
     * 发送消息给指定TOKEN
     * <p>
     * 向指定TOKEN的WebSocket会话发送文本消息。
     *
     * @param token   TOKEN
     * @param message 要发送的消息
     * @return 发送是否成功
     */
    public boolean sendToToken(String token, String message) {
        Session session = tokenSessionMap.get(token);
        if (session == null) {
            log.warn("TOKEN {} 不在线，无法发送消息", maskToken(token));
            return false;
        }

        if (session.channel().isActive()) {
            return sendMessage(session, message);
        } else {
            log.warn("TOKEN {} 的会话已失效，无法发送消息", maskToken(token));
            // 清理失效的会话
            String userId = session.getAttribute("userId");
            if (userId != null) {
                removeUserSession(userId, session);
            }
            tokenSessionMap.remove(token);
            return false;
        }
    }

    /**
     * 群发消息
     * <p>
     * 向所有在线用户的WebSocket会话发送文本消息。
     *
     * @param message 要发送的消息
     * @return 成功发送的会话数量
     */
    public int broadcast(String message) {
        int successCount = 0;

        for (ConcurrentLinkedQueue<Session> sessions : sessionPools.values()) {
            try {
                // 创建副本避免并发修改异常
                ConcurrentLinkedQueue<Session> sessionsCopy = new ConcurrentLinkedQueue<>(sessions);
                for (Session session : sessionsCopy) {
                    // 检查会话是否仍然有效
                    String token = session.getAttribute("token");
                    if (token != null && tokenSessionMap.get(token) == session && session.channel().isActive()) {
                        if (sendMessage(session, message)) {
                            successCount++;
                        }
                    } else {
                        // 会话已失效，从用户会话池中移除
                        String userId = session.getAttribute("userId");
                        if (userId != null) {
                            removeUserSession(userId, session);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("群发消息出错", e);
            }
        }

        log.debug("群发消息成功{}个会话", successCount);
        return successCount;
    }

    /**
     * 添加用户会话到会话池
     * <p>
     * 将指定用户的WebSocket会话添加到会话池中，用于后续消息推送。
     *
     * @param userId  用户ID
     * @param session WebSocket会话对象
     */
    public void addUserSession(String userId, Session session) {
        sessionPools.compute(userId, (key, sessions) -> {
            if (sessions == null) {
                sessions = new ConcurrentLinkedQueue<>();
            }
            sessions.add(session);
            return sessions;
        });
    }

    /**
     * 从会话池中移除用户会话
     * <p>
     * 从会话池中移除指定用户的WebSocket会话，释放相关资源。
     *
     * @param userId  用户ID
     * @param session WebSocket会话对象
     */
    public static void removeUserSession(String userId, Session session) {
        sessionPools.computeIfPresent(userId, (key, sessions) -> {
            sessions.removeIf(s -> s.id().equals(session.id()));
            // 同时清理TOKEN映射
            String token = session.getAttribute("token");
            if (token != null) {
                tokenSessionMap.remove(token);
            }
            return sessions.isEmpty() ? null : sessions;
        });
    }

    /**
     * 获取当前在线用户数量
     * <p>
     * 获取当前WebSocket在线用户数量。
     *
     * @return 在线用户数量
     */
    public Integer getOnlineNumber() {
        return onlineCount.get();
    }

    /**
     * 获取指定TOKEN的会话
     *
     * @param token TOKEN
     * @return Session对象，如果不存在返回null
     */
    public static Session getSessionByToken(String token) {
        return tokenSessionMap.get(token);
    }

    /**
     * 通过token获取登录用户信息
     * <p>
     * 使用Sa-Token验证token有效性并获取对应的用户信息。
     *
     * @param token 访问令牌
     * @return 登录用户信息，如果验证失败返回null
     */
    private LoginUser getLoginUser(String token) {
        try {
            LoginUser loginUser = LoginHelper.getLoginUser(token);
            if (!ObjectUtil.isNotNull(loginUser)) {
                log.warn("Token无效或已过期, token={}", maskToken(token));
                return null;
            }
            return loginUser;
        } catch (Exception e) {
            log.error("获取用户信息失败, token={}", maskToken(token), e);
            return null;
        }
    }

    /**
     * 统一认证检查
     * <p>
     * 检查WebSocket会话是否已通过身份验证，并重新验证token有效性。
     *
     * @param session WebSocket会话对象
     * @return 认证通过返回true，否则返回false
     */
    private boolean isAuthenticated(Session session) {
        String token = session.getAttribute("token");
        if (token == null) {
            log.error("用户认证失败, token为空");
            try {
                session.close();
            } catch (Exception e) {
                log.error("关闭未认证连接失败", e);
            }
            return false;
        }

        try {
            // 重新验证token有效性
            LoginUser loginUser = LoginHelper.getLoginUser(token);
            if (loginUser == null) {
                log.error("用户认证失败, token已过期或无效");
                session.close();
                return false;
            }

            // 更新认证状态
            session.setAttribute("authenticated", true);
            session.setAttribute("userId", loginUser.getUserId().toString());
            return true;
        } catch (Exception e) {
            log.error("验证用户身份时发生错误, token={}", maskToken(token), e);
            try {
                session.close();
            } catch (Exception closeException) {
                log.error("关闭连接失败", closeException);
            }
            return false;
        }
    }

}
