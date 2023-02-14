package com.example.qqbot.Event;

import cn.hutool.json.JSONObject;
import com.example.qqbot.data.Message;

/**
 *
 * 将大多数模块集中管理,需要时,请实现该接口和添加相应注解
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/14 15:04
 */
public interface IMessageEvent {

    /**
     * 权重,权重高的值会先匹配
     *
     * @return 权重值
     */
    int weight();

    /**
     * 接受消息
     *
     * @param jsonObject 原始消息对象
     * @param message    消息对象
     * @return 是否匹配成功
     */
    boolean onMessage(JSONObject jsonObject, Message message);
}
