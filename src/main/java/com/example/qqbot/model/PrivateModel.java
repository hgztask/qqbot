package com.example.qqbot.model;

import cn.hutool.json.JSONObject;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.DataPrivate;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.model.group.GroupModel;
import com.example.qqbot.model.group.ListeningGroupModel;

import java.util.HashMap;

/**
 * 私聊模块逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/10 15:29
 */
public class PrivateModel implements Runnable {
    private DataPrivate dataPrivate;

    public PrivateModel(DataPrivate dataPrivate) {
        this.dataPrivate = dataPrivate;
    }

    @Override
    public void run() {
        //数组形式
        String message = dataPrivate.getMessage();
        //原始消息的内容
        String raw_message = dataPrivate.getRaw_message();
        //发言者QQ号
        String user_id = dataPrivate.getUser_id();
        boolean boolSupeRuser = DataUserEights.SUPERUSER.contains(user_id);
        if (raw_message.startsWith("刷新复读机成员") && boolSupeRuser) {
            //需要超级用户权限
            //该关键词触发条件要优先于下面的复读机,要不然会导致复读操作
            ReReadingModel.readfilearraysetre_reading_member_set(user_id);
            return;
        }
        if (raw_message.startsWith("打印触发复读机关键词") && boolSupeRuser) { //需要超级用户权限
            ReReadingModel.printKeySet(user_id);
            return;
        }
        if (raw_message.startsWith("打印复读机成员") && boolSupeRuser) {//需要超级用户权限
            ReReadingModel.printReReadingMemberSet(user_id);
            return;
        }
        if (raw_message.startsWith("刷新黑名单群聊数据") && boolSupeRuser) {//需要超级用户权限
            GroupModel.readFIlePathBlackList(user_id);
            return;
        }
        if (raw_message.startsWith("打印接受推送消息群聊集合") && boolSupeRuser) {
            ListeningGroupModel.printPushGather(user_id);
            return;
        }
        if (raw_message.startsWith("打印监听群聊集合") && boolSupeRuser) {
            ListeningGroupModel.printlisteninggroupGather(user_id);
            return;
        }
        if (raw_message.startsWith("打印指定群聊监听状态=") && boolSupeRuser) {
            String group_id = InformationUtil.subEqual("=", raw_message);
            if (group_id.isEmpty()) {
                return;
            }
            ListeningGroupModel.printIslistening(group_id, user_id);
            return;
        }
        if (raw_message.startsWith("打印指定群聊推送状态=") && boolSupeRuser) {
            String group_id = InformationUtil.subEqual("=", raw_message);
            if (group_id.isEmpty()) {
                return;
            }
            ListeningGroupModel.printIsPush(group_id, user_id);
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("user_id", dataPrivate.getUser_id());
        data.put("message", message);
        JSONObject json = SignalUtil.httpGet(SignalUtil.getPRIVATEENDPOINT(), data);
        if (json.isEmpty()) {
            System.out.println("发送消息失败!");
            return;
        }
        System.out.println("发送成功!:" + json);


    }


}
