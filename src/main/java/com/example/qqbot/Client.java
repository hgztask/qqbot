package com.example.qqbot;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Event.Subject;
import com.example.qqbot.data.Message;
import com.example.qqbot.thread.ReconnectTask;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

/**
 * @author byhgz
 * @version 1.0
 * @date 2023/2/9 22:19
 */
@Slf4j
@ClientEndpoint
public class Client {

    private Session session;

    private static Client INSTANCE;

    /**
     * 唯一标识
     * 当值为false时表示当前不处于重连,为true表示当前正在处于重连中!
     */
    private volatile static boolean connecting = false;


    private Client(String url) throws Exception {
        session = ContainerProvider.getWebSocketContainer().connectToServer(this, URI.create(url));
    }

    public synchronized static boolean connect(String url) {
        try {
            INSTANCE = new Client(url);
            connecting = false;
            return true;
        } catch (Exception e) {
            log.info(e.getMessage());
            //e.printStackTrace();
            log.info("连接失败!");
            connecting = true;
            return false;
        }
    }

    /**
     * 重连方法
     */
    public synchronized static void reConnect() {
        if (!connecting) {
            connecting = true;
            if (INSTANCE != null) {
                INSTANCE.session = null;
                INSTANCE = null;
            }
            ReconnectTask.execute();
        }
    }


    @OnOpen
    public void onOpen(Session session) {
        log.info("[websocket] 连接成功");
    }

    @SuppressWarnings("all")
    @OnMessage
    public void onMessage(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        Message message = BeanUtil.toBean(jsonObject, Message.class);
        if ("heartbeat".equals(message.getMeta_event_type())) { //心跳包
            return;
        }
        Subject.change(jsonObject, message);
    }

    @OnClose
    public void onClose(Session session) {
        log.info("连接关闭");
        //执行重连
        reConnect();
        //log.info(CharSequenceUtil.format("[websocket] 退出连接 code={} websocket={} remote={}", code, reason, remote));
        //log.info("开始尝试执行重连!");
    }

    @SuppressWarnings("all")
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.info("[websocket] 连接异常={}", session);
    }


}
