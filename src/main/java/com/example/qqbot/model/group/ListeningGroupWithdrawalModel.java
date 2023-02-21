package com.example.qqbot.model.group;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.DataRecall;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.group.DataGroupRecall;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 监听指定群聊成员撤回内容
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/21 12:09
 */
@Slf4j
@Component
public class ListeningGroupWithdrawalModel implements Runnable, IMessageEvent {


    @Getter
    private static final File LIST_GROUP_USEID_FILE_PATH = new File("E:\\qqbot\\监听群聊成员撤回名单.json");


    @Getter
    private static final File LIST_GROUP_MESSAGE_FILE_PATH = new File("E:\\qqbot\\监听群聊成员撤回消息.json");

    /**
     * 监听的成员对象,和对应的撤回的消息
     */
    @Getter
    private static Set<String> listGroupUseID = getFileUserIDJson(LIST_GROUP_USEID_FILE_PATH);

    @Getter
    private static Map<String, Set<JSONArray>> listGroupMessage = getFileMessageJson(LIST_GROUP_MESSAGE_FILE_PATH);

    private static final String adminID = DataUserEights.SUPERUSER.get(0);

    private DataGroupRecall dataGroupRecall;


    /**
     * 读取本地监听撤回的聊天内容文件数据
     *
     * @param fIle file对象
     * @return
     */
    private static Map<String, Set<JSONArray>> getFileMessageJson(File fIle) {
        Map<String, Set<JSONArray>> map = new HashMap<>(0);
        Map jsonObject;
        try {
            jsonObject = JSONUtil.readJSONObject(fIle, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return map;
        }
        map.putAll(jsonObject);
        return map;
    }


    /**
     * 读取本地监听成员名单
     *
     * @param file file文件对象
     * @return
     */
    private static Set<String> getFileUserIDJson(File file) {
        Set<String> set = new HashSet<>(0);
        List jsonArray;
        try {
            jsonArray = JSONUtil.readJSONArray(file, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return set;
        }
        set.addAll(jsonArray);
        return set;
    }


    /**
     * 添加要监听的成员
     *
     * @param user_id 监听的成员
     * @return 是否成功
     */
    public static boolean addUserID(String user_id) {
        if (listGroupUseID.contains(user_id)) {
            return false;
        }
        return listGroupUseID.add(user_id);
    }


    /**
     * 添加撤回的内容
     *
     * @param user_id 撤回的人员
     * @param message 撤回的消息
     * @return 是否成功
     */
    private boolean addMessage(String user_id, JSONArray message) {
        if (listGroupMessage.containsKey(user_id)) {
            Set<JSONArray> arraySet = listGroupMessage.get(user_id);
            return arraySet.add(message);
        }
        HashSet<JSONArray> arrayHashSet = new HashSet<>();
        listGroupMessage.put(user_id, arrayHashSet);
        return arrayHashSet.add(message);
    }

    /**
     * 移除要监听的成员
     *
     * @param user_id 监听的成员
     * @return 是否成功
     */
    public static boolean delUserID(String user_id) {
        if (!(listGroupUseID.contains(user_id))) {
            return false;
        }
        listGroupUseID.remove(user_id);
        return true;
    }


    /**
     * 清空存储在listGroupMessage集合中的指定成员的聊天信息
     *
     * @param user_id 成员
     */
    public static boolean delMessage(String user_id) {
        if (!(listGroupMessage.containsKey(user_id))) {
            return false;
        }
        Set<JSONArray> arraySet = listGroupMessage.get(user_id);
        arraySet.clear();
        return true;
    }


    /**
     * [判断指定成员是否是要监听的成员
     *
     * @param user_id
     * @return
     */
    public static boolean readUserID(String user_id) {
        if (listGroupUseID.contains(user_id)) {
            return true;
        }
        return false;
    }


    /**
     * 权重,权重高的值会先匹配
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 1;
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
        //消息发送者QQ号,该人发的消息被撤回
        String user_id = dataGroupRecall.getUser_id();
        if (!(listGroupUseID.contains(user_id))) {
            return false;
        }
        log.info("检测到监听成员的撤回消息");
        this.run();
        return true;
    }

    @Override
    public void run() {
        String message_id = dataGroupRecall.getMessage_id();
        JSONObject message = SignalUtil.getMessage(message_id);
        if (message.isEmpty()) {
            return;
        }
        DataRecall dataRecall = message.getByPath("data", DataRecall.class);
        if (dataRecall == null) {
            log.info("获取群撤回的data消息失败,data=null");
            return;
        }
        JSONObject sender = dataRecall.getSender();
        String user_id = sender.get("user_id", String.class);
        String nickname = sender.get("nickname", String.class);
        JSONArray messageJson = dataRecall.getMessage();

        SignalUtil.sendPrivateMessage(adminID, messageJson);
        log.info(CharSequenceUtil.format("被监听撤回消息成员:{} 撤回了一条消息", nickname));
        addMessage(user_id, messageJson);
        if (FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(listGroupMessage), LIST_GROUP_MESSAGE_FILE_PATH).isFile()) {
            log.info("已保存监听的成员和撤回的数据到本地");
            return;
        }
        log.info("保存监听的成员和撤回的数据到本地-失败!");
    }
}
