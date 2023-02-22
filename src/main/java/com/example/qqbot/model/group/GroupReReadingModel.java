package com.example.qqbot.model.group;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Util.MessageUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.group.DataGroup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 群聊复读逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 21:31
 */
@Slf4j
@Component
public class GroupReReadingModel implements Runnable {

    /**
     * 群聊数据层
     */
    @Setter
    private DataGroup dataGroup;

    /**
     * 单例对象
     */
    @Getter
    private static final GroupReReadingModel RE_READING_MODEL = new GroupReReadingModel();


    /**
     * 指定存储复读机成员的路径
     */
    private static final File MEMBER_PATH_FILE = new File("E:\\qqbot\\复读成员.json");

    /**
     * 存储自动触发复读机的关键词的路径
     */
    private static final File KEYPATH_FILE = new File("E:\\qqbot\\复读关键词.json");
    /**
     * 复读机的成员集合
     */
    @Getter
    private static Set<String> MEMBER_SET = getFileJson(MEMBER_PATH_FILE);

    /**
     * 自动触发复读机的关键词集合
     */
    @Getter
    private static Set<String> KEY_SET = getFileJson(KEYPATH_FILE);


    /**
     * 记录重复值
     */
    private static String groupEqualStr = "";

    /**
     * 连续重复次数
     */
    private static int groupEqualIndex = 1;


    private GroupReReadingModel() {
    }

    /**
     * 读取本地的复读集合对象
     *
     * @return arrayList对象
     */
    public static Set<String> getFileJson(File file) {
        if (!(file.isFile())) {
            return new LinkedHashSet<>();
        }
        List jsonArray = JSONUtil.readJSONArray(file, StandardCharsets.UTF_8);
        return new LinkedHashSet<>(jsonArray);
    }

    /**
     * 判断上一条消息是否和下一条消息一样,一样就返回true
     * 反之false
     * 该方法需要加锁,避免多条线程同时进行判断!
     *
     * @param raw_message 原始消息内容
     * @return 是否一样布尔值
     */
    private synchronized static boolean isequlContent(String raw_message) {
        //这里执行群聊消息判断与上一次内容是否相等
        if (groupEqualStr.equals(raw_message)) {
            groupEqualIndex++;
            //如果相容的内容=>1次就不推送
            if (groupEqualIndex >= 2) {
                log.info("检测到连续消息大于或等于2次,故不复读消息!");
                return true;
            }//反之说明消息才出现1次
        }
        //不相等就记录对应的消息,也就是每次都会刷刷新上一条消息和刷新重复次数1次,已保证下次和上次的判断
        groupEqualStr = raw_message;
        groupEqualIndex = 1;
        return false;
    }


    /**
     * 添加要复读的人员
     */
    public static void addReReadingMemberSet(DataGroup dataGroup, String userATID) {
        if (MEMBER_SET.contains(userATID)) {
            SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "该用户已经设置过了!");
            return;
        }
        MEMBER_SET.add(userATID);
        //修改本地文件
        FileUtil.writeUtf8String(JSONUtil.parseArray(MEMBER_SET).toStringPretty(), MEMBER_PATH_FILE);
        SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), String.format("已将该成员%s设置为复读机对象", userATID));
    }


    /**
     * 添加触发复读机关键词
     *
     * @param key 关键词
     */
    public static void addKeySet(DataGroup dataGroup, String key) {
        if (KEY_SET.contains(key)) {
            SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "该关键词已经设置过了!");
            return;
        }
        KEY_SET.add(key);
        //修改本地文件
        FileUtil.writeUtf8String(JSONUtil.parseArray(KEY_SET).toStringPretty(), KEYPATH_FILE);
        SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "已添加该关键词作为复读机触发条件=" + key);
    }


    /**
     * 移除指定触发复读机关键词
     */
    public static void removeKeySet(DataGroup dataGroup, String key) {
        if (KEY_SET.remove(key)) {
            //修改本地文件
            FileUtil.writeUtf8String(JSONUtil.parseArray(KEY_SET).toStringPretty(), KEYPATH_FILE);
            SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "已将该关键词移除集合对象!");
            return;
        }
        SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "移除失败,没有该复读机关键词!");
    }


    /**
     * 打印触发复读机关键词集合
     * <p>
     * 触发在私聊窗口
     *
     * @param user_id
     */
    public static void printKeySet(String user_id) {
        String toStringPretty = JSONUtil.parseArray(KEY_SET).toStringPretty();
        if (SignalUtil.sendPrivateMessage(user_id, String.format("自动触发复读机关键词:\n%s", toStringPretty)).isEmpty()) {
            log.info("打印触发复读机关键词集合-失败!");
            return;
        }
        log.info("打印触发复读机关键词集合-成功!");
    }


    /**
     * 读取本地复读机成员对象并重新赋值给RE_READING_MEMBER_SET变量,相当于刷新了该变量的值
     * 触发地点私聊
     */
    @SuppressWarnings("all")
    public static void readfilearraysetre_reading_member_set(String user_id) {
        MEMBER_SET = getFileJson(MEMBER_PATH_FILE);
        SignalUtil.sendPrivateMessage(user_id, "已读取本地复读机成员对象并刷新值");
    }


    /**
     * 指定删除的复读机成员对象
     *
     * @param dataGroup
     */
    public static void removeReReadingMemberSet(DataGroup dataGroup, String userATID) {
        if (MEMBER_SET.remove(userATID)) {
            //修改本地文件
            FileUtil.writeUtf8String(JSONUtil.parseArray(MEMBER_SET).toStringPretty(), MEMBER_PATH_FILE);
            SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "已将该对象移除至集合对象!");
            return;
        }
        SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "移除失败,没有该复读机成员对象!");
    }

    /**
     * 打印触发复读机关键词集合里的对象
     * <p>
     * 发送到私聊窗口
     */
    public static void printReReadingMemberSet(String user_id) {
        if (SignalUtil.sendPrivateMessage(user_id, String.format("复读机对象成员:\n%s", JSONUtil.parseArray(MEMBER_SET).toStringPretty())).isEmpty()) {
            log.info("打印复读机集合里的对象模块功能消息发送失败!");
            return;
        }
        log.info("打印复读机集合里的对象模块功能消息发送成功!");
    }


    @Override
    public void run() {
        if (dataGroup == null) {
            throw new RuntimeException("dataGroup为Nul");
        }
        String raw_message = dataGroup.getRaw_message();
        String group_id = dataGroup.getGroup_id();

        if (isequlContent(raw_message)) {
            //限制复读数量,连续达到指定数不会复读
            return;
        }

        if (MessageUtil.isTypeRecord(dataGroup.getMessage())) {
            log.info("检测到语音类型,故不复读");
            return;
        }

        JSONObject json = SignalUtil.sendGroupMessage(group_id, raw_message);
        if (json.isEmpty()) {
            return;
        }
        log.info("复读机请求成功!");
        //消息id
        String message_id = json.getByPath("data.message_id", String.class);
        try {
            TimeUnit.SECONDS.sleep(115);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (message_id==null||message_id.isEmpty()) {
            log.info("获取消息id失败!");
            return;
        }
        //每次复读完成之后根据上面的倒计时撤回消息
        SignalUtil.deleteMsg(message_id);


    }

}
