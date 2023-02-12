package com.example.qqbot.model.group;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.DataRecall;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.data.group.DataGroupRecall;
import lombok.extern.slf4j.Slf4j;

/**
 * 群撤回逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/12 20:21
 */
@Slf4j
public class GroupRecallModel implements Runnable {

    private DataGroupRecall dataGroupRecall;

    public GroupRecallModel(DataGroupRecall dataGroupRecall) {
        this.dataGroupRecall = dataGroupRecall;
    }

    @Override
    public void run() {
        if ("3426359078".equals(dataGroupRecall.getUser_id())) {
            //如果是机器人的撤回消息就不用推送消息给超级用户了
            return;
        }
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


}
