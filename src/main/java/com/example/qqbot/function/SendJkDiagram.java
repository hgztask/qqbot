package com.example.qqbot.function;

import cn.hutool.json.JSONObject;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.data.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *
 * 发送jk图
 * @author byhgz
 * @version 1.0
 * @date 2023/2/14 22:41
 */
@Slf4j
@Component
public class SendJkDiagram implements Runnable, IMessageEvent {


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
        return false;
    }

    @Override
    public void run() {

    }
}
