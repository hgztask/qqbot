package com.example.qqbot.model.LanZouYmodel;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.group.DataGroup;
import com.example.qqbot.data.json.DataJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 蓝奏云资源搜索模块逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 12:14
 */
@Slf4j
@Component
public class LanZuoCloudResourceSearch implements Runnable, IMessageEvent {

    private static final JSONArray readJSONArray = JSONUtil.readJSONArray(new File("E:\\72030条蓝奏云资源.json"), StandardCharsets.UTF_8);

    /**
     * 群聊数据层
     */
    private DataGroup dataGroup;


    @Override
    public void run() {
        String user_id = dataGroup.getUser_id();
        String group_id = dataGroup.getGroup_id();
        String key = InformationUtil.subEqual("=", dataGroup.getRaw_message());
        StringBuilder messageBuilder = new StringBuilder();
        int tempIndex = 0;
        int index = 0;
        SignalUtil.sendGroupMessage(group_id, "正在搜索中,请稍等...");
        Set<JSONObject> dataArraySet = new LinkedHashSet<>();
        for (int i = 0; i < readJSONArray.size(); i++) {
            String name = readJSONArray.getByPath(String.format("[%s]name", i), String.class);
            if (!(name.contains(key))) {
                continue;
            }
            //循环状态时符合条件的数量达到5个时执行分批发送并清空记录值和StringBuilder
            if (tempIndex == 5) {
                dataArraySet.add(DataJson.text(messageBuilder));
                tempIndex = 0;
                //达到5条数目时重新建立一个StringBuilder对象
                messageBuilder = new StringBuilder();
            }
            //找到符合条件的资源就加1
            tempIndex++;
            index++;
            String format = CharSequenceUtil.format("软件名={}下载地址={}\n", name, readJSONArray.getByPath(String.format("[%s]url", i), String.class));
            messageBuilder.append(format);
        }
        if (messageBuilder.isEmpty()) {
            SignalUtil.sendGroupMessage(group_id, "未匹配到内容,本轮故结束!");
            return;
        }

        dataArraySet.add(DataJson.text(messageBuilder));  //当上面for语句中的未达到限定数时,说明剩下的没有添加

        //dataArraySet.size()/200


        //这里功能先将就用,后面有需要时再写
        int size = dataArraySet.size();
        //至少要发几次
        int exceptIndex = size / 200;
        //多出来的消息
        int exceptForIndex = size % 200;
        if (exceptIndex == 0) {
            //说明只需要发1次合并聊天记录即可
            //return;
        }
        //能被整除,对应啊哦执行几次exceptIndex
        //不能被整除就在exceptIndex+1次即可


        if (size <= 200) {
            JSONArray jsonArray = DataJson.nodeMerge("机器人", user_id, dataArraySet);
            //目前已知合并转发不支持内容中的消息太大,但消息条数,测试至少可以达到78条,意思就是尽量不要单条消息字符串太多,
            SignalUtil.sendGroupForwardMsg(group_id, jsonArray);
            SignalUtil.sendGroupMessage(group_id, String.format("===========搜索完成!符合关键词资源个数%s个===========", dataArraySet.size()));
            return;
        }
        Set<JSONObject> tempSet = new LinkedHashSet<>();
        for (JSONObject jsonObject : dataArraySet) {
            tempSet.add(jsonObject);
            if (tempSet.size() == 200) {
                break;
            }
        }
        JSONArray jsonArray = DataJson.nodeMerge("机器人", user_id, tempSet);
        //目前已知合并转发不支持内容中的消息太大,但消息条数,测试至少可以达到200条,意思就是尽量不要单条消息字符串太多,
        SignalUtil.sendGroupForwardMsg(group_id, jsonArray);
        SignalUtil.sendGroupMessage(group_id, String.format("搜索完成!符合关键词资源个数%s个,数量太大故截取,剩下%s", dataArraySet.size(), tempSet.size()));


    }


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
        if (!(raw_message.startsWith("蓝奏云资源搜索="))) {
            return false;
        }
        String key = InformationUtil.subEqual("=", raw_message);
        if (key.isEmpty()) {
            return false;
        }
        this.run();
        return true;
    }
}
