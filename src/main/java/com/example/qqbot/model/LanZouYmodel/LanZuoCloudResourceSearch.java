package com.example.qqbot.model.LanZouYmodel;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.SignalUtil;
import com.example.qqbot.data.DataGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 蓝奏云资源搜索模块逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 12:14
 */
@Slf4j
public class LanZuoCloudResourceSearch implements Runnable {

    private static final JSONArray readJSONArray = JSONUtil.readJSONArray(new File("E:\\72030条蓝奏云资源.json"), StandardCharsets.UTF_8);
    /**
     * 搜索的关键词
     */
    private final String searchKeywords;

    /**
     * 群聊数据层
     */
    private DataGroup dataGroup;


    public LanZuoCloudResourceSearch(String searchKeywords, DataGroup dataGroup) {
        this.searchKeywords = searchKeywords;
        this.dataGroup = dataGroup;
    }


    @Override
    public void run() {
        StringBuilder messageBuilder = new StringBuilder();
        int index = 0, eligibleIndex = 0;
        HashMap<String, String> sendMap = new HashMap<>();
        sendMap.put("group_id", dataGroup.getGroup_id());
        sendMap.put("message", String.valueOf(messageBuilder));
        SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "正在搜索中,请稍等...");
        for (int i = 0; i < readJSONArray.size(); i++) {
            String name = readJSONArray.getByPath(String.format("[%s]name", i), String.class);
            if (!(name.contains(searchKeywords))) {
                continue;
            }
            //循环状态时符合条件的数量达到5个时执行分批发送并清空记录值和StringBuilder
            if (index == 35) {
                startGet(messageBuilder);
                index = 0;
                //达到5条数目时重新建立一个StringBuilder对象
                messageBuilder = new StringBuilder();
            }
            //找到符合条件的资源就加1
            index++;
            //记录符合条件的总个数
            eligibleIndex++;
            String format = CharSequenceUtil.format("软件名={}下载地址={}\n", name, readJSONArray.getByPath(String.format("[%s]url", i), String.class));
            messageBuilder.append(format);
        }
        if (messageBuilder.isEmpty()) {
            SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "未匹配到内容,本轮故结束!");
        } else {
            //当messageBuilder对象还有内容时候发送消息,说明符合关键词的资源小于5个的
            messageBuilder.append(String.format("===========搜索完成!符合关键词资源个数%s个===========", eligibleIndex));
            startGet(messageBuilder);
        }
    }


    @SneakyThrows
    private void startGet(StringBuilder messageBuilder) {
        //休息一秒之后继续发送
        TimeUnit.SECONDS.sleep(1);
        HashMap<String, String> data = new HashMap<>();
        data.put("group_id", dataGroup.getGroup_id());
        data.put("message", String.valueOf(messageBuilder));
        JSONObject json = SignalUtil.httpGet(SignalUtil.getGROUPENDPOINT(), data);
        if (json.isEmpty()) {
            log.info("发送失败");
            return;
        }
        log.info("发送成功!");
    }

}
