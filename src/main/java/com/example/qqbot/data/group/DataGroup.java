package com.example.qqbot.data.group;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 群聊数据层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/10 17:08
 */

@Data
public class DataGroup {
    /**
     * 时间戳
     */
    private Integer time;

    /**
     * 收到事件的机器人 QQ 号
     */
    private String self_id;

    /**
     * 消息ID
     */
    private String message_id;

    /**
     * 发送人QQ
     */
    private String user_id;

    /**
     * 消息内容
     */
    private List<String> message;

    /**
     * 原始消息内容
     */
    private String raw_message;

    /**
     * 发送人信息 包括:
     * QQ昵称 nickname
     *QQ群昵称 card
     * QQ号 user_id
     */
    private Map<String, String> sender;

    /**
     * Q群群号
     */
    private String group_id;


    /**
     * 匿名信息
     * 如果不是匿名消息则为 null
     */
    private Object anonymous;

}
