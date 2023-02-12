package com.example.qqbot.data.group;

import lombok.Data;

/**
 * 群人数减少与增加的数据对象
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/10 15:15
 */
@Data
public class DataGroupDecrease {

    /**
     * 收到事件的机器人 QQ 号
     */
    private String self_id;
    /**
     * 事件子类型
     * 如果是有人退群则表示leave主动退群、kick成员被踢、kick_me登录号被踢
     * 如果是有人进群则分别表示 approve 管理员已同意入群、 invite 管理员邀请入群
     */
    private String sub_type;
    /**
     * 时间戳
     */
    private long time;
    /**
     * 群号
     */
    private String group_id;
    /**
     * 操作者 QQ 号
     * 如果是人数减少则根据是否是主动退群反之和user_id 相同 )
     */
    private String operator_id;
    /**
     * 如果人数增加 则表示进群的人QQ号
     * 反之则是离开者QQ号
     */
    private String user_id;

}
