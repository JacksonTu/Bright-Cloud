package com.tml.server.msg.websocket;

import com.alibaba.fastjson.JSONObject;
import com.tml.common.core.entity.constant.WebsocketConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author JacksonTu
 * @version 1.0
 * @description 公用websocket工具类
 * @since 2020/10/15 16:53
 */
@Slf4j
@Component
@ServerEndpoint("/websocket/{userId}")
public class CommonWebSocket {

    private Session session;

    private static CopyOnWriteArraySet<CommonWebSocket> webSockets =new CopyOnWriteArraySet<>();
    private static Map<String,Session> sessionPool = new HashMap<String,Session>();

    /**
     * 连接建立成功
     * @param session
     * @param userId
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId){
        try {
            this.session = session;
            webSockets.add(this);
            sessionPool.put(userId, session);
            log.info("【websocket消息】有新的连接，总数为: {}",webSockets.size());
        } catch (Exception e) {
        }
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose() {
        try {
            webSockets.remove(this);
            log.info("【websocket消息】连接断开，总数为: {}",webSockets.size());
        } catch (Exception e) {
        }
    }

    /**
     * 收到客户端消息
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
        log.info("【websocket消息】收到客户端消息: {}",message);
        JSONObject obj = new JSONObject();
        obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_CHECK);//业务类型
        obj.put(WebsocketConstant.MSG_TXT, "心跳响应");//消息内容
        session.getAsyncRemote().sendText(obj.toJSONString());
    }

    /**
     * 广播消息
     * @param message
     */
    public void sendAllMessage(String message) {
        log.info("【websocket消息】广播消息: {}",message);
        for(CommonWebSocket webSocket : webSockets) {
            try {
                if(webSocket.session.isOpen()) {
                    webSocket.session.getAsyncRemote().sendText(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 单点消息
     * @param userId
     * @param message
     */
    public void sendOneMessage(String userId, String message) {
        Session session = sessionPool.get(userId);
        if (session != null&&session.isOpen()) {
            try {
                log.info("【websocket消息】广播消息: {}",message);
                session.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 多人单点消息
     * @param userIds
     * @param message
     */
    public void sendMoreMessage(String[] userIds, String message) {
        for(String userId:userIds) {
            Session session = sessionPool.get(userId);
            if (session != null&&session.isOpen()) {
                try {
                    log.info("【websocket消息】广播消息: {}",message);
                    session.getAsyncRemote().sendText(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
