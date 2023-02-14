package com.example.qqbot.model.group;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.DataRecall;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.group.DataGroupRecall;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    {
        System.out.println("GroupRecallModel被初始化了!!!!!!!!!!!!!!!!!!!!!");
    }

    @Override
    public void run() {
        JSONObject messageJson = SignalUtil.getMessage(dataGroupRecall.getMessage_id());
        if (messageJson.isEmpty()) {
            log.info("获取群撤回的原始消息失败,messageJson=空的json");
            return;
        }
        DataRecall dataRecall = messageJson.getByPath("data", DataRecall.class);
        if (dataRecall == null) {
            log.info("获取群撤回的data消息失败,data=null");
            return;
        }
        long time = dataRecall.getTime() * 1000;
        boolean empty = SignalUtil.sendPrivateMessage(DataUserEights.SUPERUSER.get(0), CharSequenceUtil.format("""
                        ===撤回消息记录====
                        是否是群消息={}
                        是群消息时的群号={}
                        消息类型={}
                        发送者={}
                        发送时间={}
                        消息内容={}""", dataRecall.isGroup(), dataRecall.getGroup_id(), dataRecall.getMessage_type(),
                dataRecall.getSender().toStringPretty(), DateUtil.date(time),
                dataRecall.getMessage().toStringPretty())).isEmpty();
        if (empty) {
            log.info("已将消息推送给超级用户!");
            return;
        }

        log.info("消息推送给超级用户失败!=" + dataRecall);
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
        log.info("群有人撤回了消息了=" + message);
        this.run();
        return false;
    }
}
