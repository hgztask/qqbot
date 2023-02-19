package com.example.qqbot.function;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONObject;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.NetworkUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.group.DataGroup;
import com.example.qqbot.data.json.DataJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ikun小黑子模块
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/12 13:08
 */
@Component
@Slf4j
public class IkunModel implements Runnable, IMessageEvent {

    private DataGroup dataGroup;


    /**
     * 坤坤关键词
     */
    private static final List<String> IKUNLISET = ListUtil.toList("ikun", "小黑子", "IKUN", "食不食油饼", "鸡哥",
            "坤", "你干嘛","球","太美","白带","异常","再看一眼","靠近一点","铁山靠","爆炸");


    /**
     * 权重,权重高的值会先匹配
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 6;
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
        if (!(InformationUtil.isContainsMessAge(IKUNLISET, raw_message))) {
            return false;
        }
        this.run();
        return true;
    }


    @Override
    public void run() {
        String group_id = dataGroup.getGroup_id();
        String ikunImageUrl = NetworkUtil.getWqwlkjImageUrl("https://api.wqwlkj.cn/wqwlapi/ikun.php?type=json&lx=bqb");
        if (ikunImageUrl.isEmpty()) {
            return;
        }
        String fileName = InformationUtil.subEqual("/", ikunImageUrl);
        if (SignalUtil.sendGroupMessage(group_id, DataJson.imageUrl(fileName, ikunImageUrl, true)).isEmpty()) {
            log.info("发送失败!");
            return;
        }
        log.info("发送成功!");


    }
}
