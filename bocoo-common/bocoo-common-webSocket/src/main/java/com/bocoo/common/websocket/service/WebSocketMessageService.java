package com.bocoo.common.websocket.service;

import com.bocoo.common.websocket.autoconfigure.BocooWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * WebSocket消息服务类
 */
@Service
public class WebSocketMessageService {

    @Autowired
    private BocooWebSocket bocooWebSocket;

    /**
     * 向指定用户发送消息
     *
     * @param userId 用户ID
     * @param message 消息内容
     * @return 成功发送的会话数量
     */
    public int sendToUser(String userId, String message) {
        return bocooWebSocket.sendInfo(userId, message);
    }

    /**
     * 群发消息
     *
     * @param message 消息内容
     * @return 成功发送的会话数量
     */
    public int broadcastMessage(String message) {
        return bocooWebSocket.broadcast(message);
    }

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数量
     */
    public int getOnlineUserCount() {
        return bocooWebSocket.getOnlineNumber();
    }
}
