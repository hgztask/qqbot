package com.example.qqbot.data;

import lombok.Data;
import lombok.Getter;

/**
 * 私聊的信息数据对象
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/10 15:25
 */
@Data
public class DataPrivate {
    /**
     * 时间戳
     */
    private Integer time;
    /**
     * 收到事件的机器人号
     */
    private String self_id;
    /**
     * 消息的ID
     */
    private String message_id;

    /**
     * 发送者的QQ号
     */
    private String user_id;

    /**
     * 消息的内容
     */
    private String message;

    /**
     * 原始消息的内容
     */
    private String raw_message;

    /**
     * 发送人信息
     */
    private Object sender;

    /**
     * 终结点
     */
    @Getter
    private static final String ENDPOINT ="/send_private_msg";

}
