package com.example.qqbot.model.group;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.MessageUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.DataRecall;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.group.DataGroupRecall;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 群撤回逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/12 20:21
 */
@Slf4j
@Component
public class GroupRecallModel implements Runnable, IMessageEvent {

    private DataGroupRecall dataGroupRecall;

    @Override
    public void run() {
        JSONObject jsonObject = SignalUtil.getMessage(dataGroupRecall.getMessage_id());
        if (jsonObject.isEmpty()) {
            log.info("获取群撤回的原始消息失败,messageJson=空的json");
            return;
        }
        DataRecall dataRecall = jsonObject.getByPath("data", DataRecall.class);
        if (dataRecall == null) {
            log.info("获取群撤回的data消息失败,data=null");
            return;
        }
        JSONObject sender = dataRecall.getSender();
        //撤回的人员QQ号
        String user_id = sender.get("user_id", String.class);
        if (DataUserEights.BOTUSERID.contains(user_id)) {
            log.info("机器人撤回的消息不需要推送给超级用户!");
            return;
        }
        long time = dataRecall.getTime() * 1000;
        String nickname = sender.get("nickname", String.class);
        String group_id = dataRecall.getGroup_id();
        log.info(nickname + "群成员" + dataGroupRecall.getUser_id() + "撤回了消息");

        JSONArray messageJson = dataRecall.getMessage();
        List<JSONObject> typeImageList = MessageUtil.getTypeImageList(messageJson);
        MessageUtil.downloadGroupImageThread(typeImageList, group_id, user_id);
        SignalUtil.sendPrivateMessage(CharSequenceUtil.format("""
                ===群撤回消息记录====
                群聊:{}
                发言人昵称:{}
                发言人qq号:{}
                时间:{}
                撤回消息如下:
                """, group_id, nickname, user_id, DateUtil.date(time)));
        //发送原消息到超级用户上
        JSONObject sendPrivateMessage = SignalUtil.sendPrivateMessage(messageJson);
        if (sendPrivateMessage.isEmpty()) {
            log.info("消息推送给超级用户失败!=" + dataRecall);
            return;
        }
        log.info("已将消息推送给超级用户!");
    }

    /**
     * 权重,权重高的值会先匹配
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 0;
    }

    /**
     * 接受消息
     *
     * @param jsonObject 原始消息对象
     * @param message    消息对象
     * @return 是否匹配成功
     */
    @Override
    public boolean onMessage(JSONObject jsonObject, Message message) {
        if (!("group_recall".equals(message.getNotice_type()))) {
            return false;
        }
        //群消息撤回
        this.dataGroupRecall = BeanUtil.toBean(jsonObject, DataGroupRecall.class);
        if ("3426359078".equals(dataGroupRecall.getUser_id())) {
            //如果是机器人的撤回消息就不用推送消息给超级用户了
            return false;
        }
        this.run();
        return false;
    }


}
