package com.example.qqbot.model.group;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.example.qqbot.SignalUtil;
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
        boolean empty = SignalUtil.sendPrivateMessage(DataUserEights.SUPERUSER.get(0), StrUtil.format("""
                        是否是群消息={}
                        是群消息时的群号{}
                        消息类型{}
                        发送者{}
                        发送时间={}
                        消息内容={}""", dataRecall.isGroup(), dataRecall.getGroup_id(), dataRecall.getMessage_type(),
                dataRecall.getSender(), DateUtil.date(time), dataRecall.getMessage())).isEmpty();
        if (empty) {
            log.info("已将消息推送给超级用户!");
            return;
        }
        log.info("消息推送给超级用户失败!");
    }


}
