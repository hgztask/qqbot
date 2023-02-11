package com.example.qqbot;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.data.*;
import com.example.qqbot.model.GroupDecrease;
import com.example.qqbot.model.GroupIncrease;
import com.example.qqbot.model.GroupModel;
import com.example.qqbot.model.PrivateModel;
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

    /**
     * 发送消息方法
     *
     * @param json json内容
     */
    public static void sendMessage(String json) {

    }


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
                    if ("heartbeat".equals(message.getMeta_event_type())) {
                        //心跳包
                        return;
                    } else if ("private".equals(message.getMessage_type())) {
                        //如果是私聊的消息
                        //获取私聊消息数据
                        DataPrivate dataPrivate = BeanUtil.toBean(jsonObject, DataPrivate.class);
                        //执行私聊逻辑层
                        new PrivateModel(dataPrivate).run();
                    } else if ("group".equals(message.getMessage_type())) {
                        //群聊消息
                        DataGroup dataGroup = BeanUtil.toBean(jsonObject, DataGroup.class);
                        new GroupModel(dataGroup, jsonObject).run();
                        //log.info("群聊消息=" + jsonObject.toStringPretty());
                        //log.info("群聊消息 " + dataGroup.toString());
                    } else if ("group_ban".equals(message.getNotice_type())) {
                        //群接禁言事件
                    } else if ("group_recall".equals(message.getNotice_type())) {
                        //群消息撤回
                    } else if ("friend_recall".equals(message.getNotice_type())) {
                        //私聊消息撤回
                        log.info("私聊消息撤回了");
                        System.out.println(jsonObject.toStringPretty());
                    } else if ("group_increase".equals(message.getNotice_type())) {
                        //群成员增加
                        DataGroupDecrease dataGroupDecrease = BeanUtil.toBean(jsonObject, DataGroupDecrease.class);
                        new GroupIncrease(dataGroupDecrease).run();
                        log.info(dataGroupDecrease.getGroup_id() + "群的 " + dataGroupDecrease.getUser_id() + " 成员增加了");
                    } else if ("group_decrease".equals(message.getNotice_type())) {
                        //群成员减少
                        DataGroupDecrease dataGroupDecrease = BeanUtil.toBean(jsonObject, DataGroupDecrease.class);
                        new GroupDecrease(dataGroupDecrease).run();
                        log.info(dataGroupDecrease.getGroup_id() + "群的 " + dataGroupDecrease.getUser_id() + " 成员减少了");
                    } else {
                        log.info("未登记状态信息");
                        System.out.println(jsonObject.toStringPretty());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.info("[websocket] 退出连接");
                }

                @Override
                public void onError(Exception ex) {
                    log.info("[websocket] 连接错误={}", ex.getMessage());
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
