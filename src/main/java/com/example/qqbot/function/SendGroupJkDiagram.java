package com.example.qqbot.function;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.group.DataGroup;
import com.example.qqbot.data.json.DataJson;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 群聊发送jk图
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/14 22:41
 */
@Slf4j
@Component
public class SendGroupJkDiagram implements Runnable, IMessageEvent {

    private DataGroup dataGroup;

    /**
     * 用于存储执行该功能的群聊,针对每个群聊做限制
     * 每个群聊需要等待上一次请求完成在继续请求!
     */
    private static final Set<String> SET_GROUP = new HashSet<>();

    /**
     * 权重,权重高的值会先匹配
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 1000;
    }


    /**
     * 接受消息
     *
     * @param jsonObject 原始消息对象
     * @param message    消息对象
     * @return 是否匹配成功
     */
    @Override
    @SuppressWarnings("all")
    public boolean onMessage(JSONObject jsonObject, Message message) {
        if (!"group".equals(message.getMessage_type())) {
            return false;
        }
        this.dataGroup = BeanUtil.toBean(jsonObject, DataGroup.class);
        if (!(dataGroup.getRaw_message().startsWith("获取jk图"))) {
            return false;
        }
        synchronized (this) {
            String group_id = dataGroup.getGroup_id();
            if (SET_GROUP.add(group_id)) {
                //如果添加成功,说明现在没有群聊在执行请求,则该群聊先执行
                ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
                threadExecutor.execute(this);
                threadExecutor.shutdown();
                return true;
            }
            SignalUtil.sendGroupMessage(group_id, "请稍等上一个请求完成");
        }
        return true;
    }

    /**
     * 请求完成之后获取完整的图片cq码
     *
     * @return 图片cq码
     */
    public static JSONObject getJKImageCQ() {
        Connection.Response response = SignalUtil.jsoupHttpGet("http://www.ggapi.cn/api/jkzf", false);
        JSONObject jsonnull = SignalUtil.getJSONNULL();
        if (response == null) {
            log.info("请求失败了!");
            return jsonnull;
        }
        String location = response.header("Location");
        if (location == null) {
            log.info("获取响应体的Location值失败");
            return jsonnull;
        }
        String fileName = InformationUtil.lastSubEqual("/", location);
        if (fileName.isEmpty()) {
            fileName = "jk图";
        }
        return DataJson.imageUrl(fileName, location, true);
    }

    @Override
    public void run() {
        String group_id = dataGroup.getGroup_id();
        SignalUtil.sendGroupMessage(group_id, "正在请求jk图!,请稍等!");
        JSONArray array = new JSONArray();
        for (int i = 0; i < 30; i++) {
            array.add(getJKImageCQ());
        }
        if (array.isEmpty()) {
            log.info("jsonArray CQ方法返回结果为空列表!");
            return;
        }
        SignalUtil.sendGroupForwardMsg(group_id,  DataJson.nodeText("机器人", dataGroup.getUser_id(), array));
        //执行完就移除对应的Q群群聊
        SET_GROUP.remove(group_id);
        SignalUtil.sendGroupMessage(group_id, "获取jk图-本轮请求完成!");
    }



}
