package com.example.qqbot.model.group;

import cn.hutool.json.JSONObject;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.data.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author byhgz
 * @version 1.0
 * @date 2023/2/14 16:32
 */
@Slf4j
@Component
public class FriendRecallModel implements Runnable, IMessageEvent {
    /**
     * 权重,权重高的值会先匹配
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 0;
    }

    /**
     * 接受消息
     *
     * @param jsonObject 原始消息对象
     * @param message    消息对象
     * @return 是否匹配成功
     */
    @Override
    public boolean onMessage(JSONObject jsonObject, Message message) {
        if (!("friend_recall".equals(message.getNotice_type()))) {
            return false;
        }
        //私聊消息撤回
        log.info("私聊消息撤回了" + jsonObject.toStringPretty());
        return true;
    }

    @Override
    public void run() {

    }
}
