package com.example.qqbot;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Event.Subject;
import com.example.qqbot.data.MailingAddress;
import com.example.qqbot.data.Message;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * @author byhgz
 * @version 1.0
 * @date 2023/2/9 22:19
 */
@Slf4j
@Component
public class WebsocketClient {

    @Bean
    public WebSocketClient webSocketClient() {
        try {
            WebSocketClient webSocketClient = new WebSocketClient(new URI(MailingAddress.LISTENING_ADDRESS), new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    log.info("[websocket] 连接成功");
                }

                @SuppressWarnings("all")
                @Override
                public void onMessage(String json) {
                    JSONObject jsonObject = JSONUtil.parseObj(json);
                    Message message = BeanUtil.toBean(jsonObject, Message.class);
                    if ("heartbeat".equals(message.getMeta_event_type())) { //心跳包
                        return;
                    }
                    Subject.change(jsonObject, message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.info("[websocket] 退出连接");
                }

                @Override
                public void onError(Exception ex) {
                    log.info("[websocket] 连接错误={}", ex.getMessage());
                    throw new RuntimeException(ex);
                }
            };
            webSocketClient.connect();
            return webSocketClient;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
