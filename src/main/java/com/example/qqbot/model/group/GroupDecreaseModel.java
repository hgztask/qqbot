package com.example.qqbot.model.group;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.group.DataGroupDecrease;
import com.example.qqbot.face.GetTypeFace;

import java.util.HashMap;

/**
 * 群人数减少逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/10 15:14
 */
public class GroupDecreaseModel implements Runnable, GetTypeFace {

    /**
     * 群人数减少的数据对象
     */
    private DataGroupDecrease dataGroupDecrease;

    public GroupDecreaseModel(DataGroupDecrease dataGroupDecrease) {
        this.dataGroupDecrease = dataGroupDecrease;
    }

    /**
     * 判断群员状态是因为什么原因退的群
     *
     * @param content
     * @return
     */
    @Override
    public final String getType(String content) {
        return switch (content) {
            case "leave" -> "主动退群";
            case "kick" -> "成员被踢";
            case "kick_me" -> "登录号被踢";
            default -> "未知对象";
        };
    }


    @Override
    public void run() {
        /**
         * 先定义相关变量,后面拓展需求时再利用
         */

        HashMap<String, String> data = new HashMap<>();
        //被踢的QQ号
        String user_id = dataGroupDecrease.getUser_id();
        //所在的群号
        String group_id = dataGroupDecrease.getGroup_id();
        //收到事件的机器人 QQ 号
        String self_id = dataGroupDecrease.getSelf_id();
        //事件子类型, 分别表示主动退群、成员被踢、登录号被踢   leave、kick、kick_me
        String sub_type = dataGroupDecrease.getSub_type();
        //操作者 QQ 号 ( 如果是主动退群, 则和 user_id 相同 )
        String operator_id = dataGroupDecrease.getOperator_id();
        long time = dataGroupDecrease.getTime() * 1000;

        data.put("group_id", dataGroupDecrease.getGroup_id());
        data.put("message",
                String.format("%s成员退出群了:\n操作对象:%s\n事件类型:%s\n事件:%s",
                        user_id, operator_id, getType(sub_type),
                        DateUtil.date(time))
        );
        JSONObject json = SignalUtil.httpGet(SignalUtil.getGROUPENDPOINT(), data);
        if (json.isEmpty()) {
            System.out.println("发送群消息失败了" + json);
            return;
        }
        System.out.println("发送消息成功!");

    }
}


