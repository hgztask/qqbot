package com.example.qqbot.function;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.group.DataGroup;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author byhgz
 * @version 1.0
 * @date 2023/2/16 22:12
 */
public class RiddleModel implements Runnable, IMessageEvent {

    private DataGroup dataGroup;

    /**
     * 用于存储群聊是否使用了解谜语功能,以及是否解除谜语来
     */
    private static final Map<String, Boolean> DATA_MAP = new HashMap<>();

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
        if (!("group".equals(message.getMessage_type()))) {
            return false;
        }
        this.dataGroup = BeanUtil.toBean(jsonObject, DataGroup.class);
        String raw_message = dataGroup.getRaw_message();
        if (!(raw_message.startsWith("猜谜语"))) {
            return false;
        }

        String user_id = dataGroup.getUser_id();
        String group_id = dataGroup.getGroup_id();
        Set<String> keySet = DATA_MAP.keySet();
        if (keySet.contains(group_id)) {
            if (!(DATA_MAP.get(group_id))) {
                SignalUtil.sendGroupMessage(group_id,"当前群聊上一个还未解谜出来哟,如果需要提示,可以回复查看谜底,或者提示哟,或者回复跳过本轮解谜");
                return true;
            }



        }


        return false;
    }

    @Override
    public void run() {

    }


}
