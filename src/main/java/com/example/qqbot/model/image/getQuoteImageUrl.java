package com.example.qqbot.model.image;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.group.DataGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 获取群里引用图片url
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/15 15:06
 */
@Slf4j
@Component
public class getQuoteImageUrl implements Runnable, IMessageEvent {

    private DataGroup dataGroup;

    /**
     * 权重,权重高的值会先匹配
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 30;
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
        if (!("group".equals(message.getMessage_type()))) {
            return false;
        }
        this.dataGroup = BeanUtil.toBean(jsonObject, DataGroup.class);

        String raw_message = dataGroup.getRaw_message();
        if (!(raw_message.contains("获取引用图片直链"))) {
            return false;
        }
        this.run();
        return true;
    }

    @Override
    public void run() {
        String group_id = dataGroup.getGroup_id();
        String replyID = InformationUtil.getMessageReplyID(dataGroup.getMessage());
        if (replyID.isEmpty()) {
            log.info("getMessageReplyID方法返回的replyID为空字符串");
            return;
        }
        //根据消息ID获取原消息
        JSONObject message = SignalUtil.getMessage(replyID);
        if (message.isEmpty()) {
            log.info("getMessage方法返回的json为空的Json对象");
            return;
        }
        //获取到message内容列表
        JSONArray byPath = message.getByPath("data.message", JSONArray.class);
        if (byPath == null || byPath.isEmpty()) {
            log.info("byPath是Json对象");
            return;
        }
        List<JSONObject> messageTypeList = InformationUtil.getMessageImageList(byPath);
        if (messageTypeList.isEmpty()) {
            log.info("messageTypeList值为空列表");
            SignalUtil.sendGroupMessage(group_id, "引用内容并未有图片资源!");
            return;
        }
        log.info("相应内容=" + messageTypeList);
        JSONObject entries = SignalUtil.sendGroupMessage(group_id, "引用图片直链:\n" + JSONUtil.toJsonPrettyStr(messageTypeList));
        if (entries.isEmpty()) {
            log.info("发送消息消相应结果为空的表");
            return;
        }
        log.info("引用图片直链响应成功");
    }

}
