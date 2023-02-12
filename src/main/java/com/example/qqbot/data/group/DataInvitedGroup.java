package com.example.qqbot.data.group;

import lombok.Data;

/**
 * 加群请求／邀请的数据层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/12 11:26
 */
@Data
public class DataInvitedGroup {

    /**
     * 上报类型
     */
    private String post_type;

    /**
     * 请求类型
     */
    private String request_type;

    /**
     * 时间戳
     */
    private long time;

    /**
     * 收到事件的机器人 QQ 号
     */
    private String self_id;

    /**
     * 请求子类型, 分别表示加群请求、邀请登录号入群
     * add、invite
     */
    private String sub_type;

    /**
     * 发送请求的QQ号
     */
    private String user_id;

    /**
     * 验证信息
     */
    private String comment;

    /**
     * 群号
     */
    private String group_id;
    /**
     * 请求 flag, 在调用处理请求的 API 时需要传入
     */
    private String flag;

}
