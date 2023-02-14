package com.example.qqbot.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.DataPrivate;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.data.Message;
import com.example.qqbot.model.group.GroupModel;
import com.example.qqbot.model.group.ListeningGroupModel;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * 私聊模块逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/10 15:29
 */
@Component
public class PrivateModel implements Runnable, IMessageEvent {
    private DataPrivate dataPrivate;


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
        if (raw_message.startsWith("打印群聊黑名单") && boolSupeRuser) {
            GroupModel.printBlackGroup(user_id);
            return;
        }

        //下面是复读私聊的
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

    /**
     * 权重
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 10;
    }

    /**
     * 接受消息
     *
     * @param jsonObject
     * @param message
     * @return 是否匹配成功
     */
    @Override
    public boolean onMessage(JSONObject jsonObject, Message message) {
        if (!("private".equals(message.getMessage_type()))) {
            //如果不是私聊消息
            return false;
        }

        //获取私聊消息数据
        dataPrivate = BeanUtil.toBean(jsonObject, DataPrivate.class);
        run();
        return true;
    }
}
