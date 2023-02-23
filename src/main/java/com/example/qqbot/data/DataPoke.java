package com.example.qqbot.data;

import lombok.Data;

/**
 * 戳一戳的数据层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/12 17:05
 */
@Data
public class DataPoke {



    /**
     * 时间戳
     */
    private long time;

    /**
     * BOT QQ 号
     */
    private String self_id;

    /**
     * 群号
     */
    private String group_id;

    /**
     * 发送者 QQ 号
     */
    private String user_id;

    private String sender_id;

    /**
     * 被戳者 QQ 号
     */
    private String target_id;


}
