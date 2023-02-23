package com.example.qqbot.data;

import lombok.Data;

/**
 * 消息对象
 * 该类主要是负责初始准太下判断是哪个触发器
 * 比如私聊触发器,群聊触发器,群人数变化等等触发器,
 * <p>
 * 只需要保留一些需要用,或者后面要用时添加上去即可
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/9 22:56
 */
@Data
@SuppressWarnings("all")
public class Message {

    /**
     * 元事件类型
     */
    private String meta_event_type;
    /**
     * 类型,比如群聊,私聊等
     * 如果是private则私聊,group群聊,heartbeat频道
     */
    private String message_type;

    /**
     * 通知类型,比如群消息撤回,有人被禁言等,群人数增加
     */
    private String notice_type;

    /**
     * 提示类型
     */
    private String sub_type;
    /**
     * 时间戳
     */
    private Integer time;

    /**
     * 收到事件的机器人QQ号
     */
    private String self_id;
    /**
     * 对方发送者QQ账号
     * 这里可以作为黑名单或者其他用途
     */
    private String user_id;

    /**
     * 群号
     * 可作为黑名单操作
     */
    private String group_id;

    /**
     *请求类型
     */
    private String request_type;

    /**
     * 上报类型
     */
    private String post_type;

}


