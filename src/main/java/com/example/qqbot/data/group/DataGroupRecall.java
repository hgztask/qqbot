package com.example.qqbot.data.group;

import lombok.Data;

/**
 * 群消息撤回数据层
 * 用于监听器响应的结果,非实际撤回的内容.仅作为有该撤回的动作数据
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/12 20:23
 */
@Data
public class DataGroupRecall {

    /**
     * 时间戳
     */
    private long time;
    /**
     * 收到事件的机器人 QQ 号
     */
    private String self_id;
    /**
     * 群号
     */
    private String group_id;

    /**
     * 消息发送者 QQ 号
     * 撤回这消息原本发送的人,比如我发了这条消息,但不是我撤回,我就是那个消息发送者,而撤回的对象成为操作者
     */
    private String user_id;

    /**
     * 操作者 QQ 号,可以理解为是谁是操作撤回的那人
     */
    private String operator_id;

    /**
     * 被撤回的消息id
     */
    private String message_id;
}
