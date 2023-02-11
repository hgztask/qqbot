package com.example.qqbot.model;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.SignalUtil;
import com.example.qqbot.data.DataGroup;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 复读逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 21:31
 */
@Slf4j
public class ReReadingModel implements Runnable {

    /**
     * 群聊数据层
     */
    private DataGroup dataGroup;


    /**
     * 指定存储复读机成员的路径
     */
    private static final File pathFile = new File("E:\\复读成员.json");
    /**
     * 指定要复读机的成员
     */
    private static final List<String> RE_READING_MEMBER_SET = getFileJson(pathFile);

    public ReReadingModel(DataGroup dataGroup) {
        this.dataGroup = dataGroup;
    }

    /**
     * 读取本地的复读机成员
     *
     * @return arrayList对象
     */
    private static ArrayList<String> getFileJson(File file) {
        if (!(file.isFile())) {
            return new ArrayList<>(0);
        }
        List jsonArray = JSONUtil.readJSONArray(file, StandardCharsets.UTF_8);
        return new ArrayList<>(jsonArray);
    }


    @Override
    public void run() {
        String raw_message = dataGroup.getRaw_message();
        String user_id = dataGroup.getUser_id();
        if (raw_message.contains("设置要复读机的成员=")) {
            //设置后面台添加设置对象
        } else if (RE_READING_MEMBER_SET.isEmpty() || !(RE_READING_MEMBER_SET.contains(user_id))) {
            return;
        }
        JSONObject json = SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), raw_message);
        if (json.isEmpty()) {
            return;
        }
        log.info("复读机请求成功!");
    }
}
