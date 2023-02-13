package com.example.qqbot.data;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.Data;

/**
 * 撤回的消息数据层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/12 21:07
 */
@Data
public class DataRecall {
    /**
     * 是否是群消息
     */
    private boolean group;

    /**
     * 是群消息时的群号(否则不存在此字段)
     */
    private String group_id;

    /**
     * 消息id
     */
    private String message_id;

    /**
     * 消息的真实id
     */
    private String real_id;

    /**
     * 群消息时为group, 私聊消息为private
     */
    private String message_type;

    /**
     * 发送者
     */
    private JSONObject sender;

    /**
     * 发送时间时的时间戳
     */
    private long time;

    /**
     * 起始消息序号
     */
    private long message_seq;


    /**
     * 消息内容
     */
    private JSONArray message;

}
