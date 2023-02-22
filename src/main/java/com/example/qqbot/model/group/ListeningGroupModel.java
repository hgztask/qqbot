package com.example.qqbot.model.group;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.MessageUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.group.DataGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 监听群消息
 * <p>
 * 功能
 * 监听指定群聊的消息,当有更新时.将对应的消息原封不动通过机器人推送到指定的推送群
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/12 22:25
 */
@Slf4j
@Component
public class ListeningGroupModel implements Runnable, IMessageEvent {
    private static final File FILE = new File("E:\\qqbot\\监听群聊、推送群聊黑名单群聊.json");
    /**
     * 监听群聊、推送群聊与黑名单群聊
     */
    private static JSONObject listeningorblackgroupid = getFileJSON(FILE);

    /**
     * 暂时用于判断是否继续推送bhVip群的消息
     * 该值需要其他人员在群里回复指定格式内容才能
     */
    public static String temp = "";

    /**
     * 一个群连续出现重复消息次数
     */
    private static int groupEqualIndex = 1;

    /**
     * 记录重复值
     */
    private static String groupEqualStr = "";


    private DataGroup dataGroup;

    @Override
    public void run() {
        String raw_message = dataGroup.getRaw_message();
        //监听的群聊
        String group_id = dataGroup.getGroup_id();
        JSONArray list = listeningorblackgroupid.get("推送群聊", JSONArray.class);
        if (list == null || list.isEmpty()) {
            return;
        }


        //这里需要增加新功能,要求截取atCQ段落的部分,其余的正常推送!
        for (Object pushGroupID : list) {
            if (SignalUtil.sendGroupMessage(String.valueOf(pushGroupID), raw_message).isEmpty()) {
                log.info("监听群聊推送消息失败!");
                return;
            }
            log.info(CharSequenceUtil.format("已将监听{}群聊的消息推送给推送群聊{}", group_id, pushGroupID));
        }
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
                log.info("检测到连续消息大于或等于2次,故不推送消息!");
                return true;
            }//反之说明消息才出现1次
        }
        //不相等就记录对应的消息,也就是每次都会刷刷新上一条消息和刷新重复次数1次,已保证下次和上次的判断
        groupEqualStr = raw_message;
        groupEqualIndex = 1;
        return false;
    }


    /**
     * 打印接受推送消息的群聊集合
     * 推荐私聊窗口
     *
     * @param user_id 用户
     */
    public static void printPushGather(String user_id) {
        List<String> list = listeningorblackgroupid.get("推送群聊", List.class);
        if (SignalUtil.sendPrivateMessage(user_id, "接受推送消息的群聊集合:" + JSONUtil.parseArray(list).toStringPretty()).isEmpty()) {
            log.info("打印接受推送消息的群聊集合-失败!");
            return;
        }
        log.info("打印接受推送消息的群聊集合-成功!");
    }

    /**
     * 打印指定内容消息
     * 判断指定群聊是否是标记了推送状态
     *
     * @param group_id 群号
     */
    public static void printIsPush(String group_id) {
        List<String> list = listeningorblackgroupid.get("推送群聊", List.class);
        if (list.contains(group_id)) {
            SignalUtil.sendGroupMessage(group_id, "当前群聊推送状态=true");
            return;
        }
        SignalUtil.sendGroupMessage(group_id, "当前群聊标记推送状态=false");
    }

    /**
     * 打印指定群聊是否是标记了推送状态并推送给指定用户
     *
     * @param group_id 判断的群号
     * @param user_id  推送的用户
     */
    public static void printIsPush(String group_id, String user_id) {
        List<String> list = listeningorblackgroupid.get("推送群聊", List.class);
        if (list.contains(group_id)) {
            SignalUtil.sendPrivateMessage(user_id, "指定的群聊推送状态=true");
            return;
        }
        SignalUtil.sendPrivateMessage(user_id, "指定的群聊推送状态=false");
    }

    /**
     * 打印指定群聊是否标记了监听状态并发送消息给指定群聊
     *
     * @param group_id 要判断的群号
     */
    public static void printIslistening(String group_id) {
        List<String> list = listeningorblackgroupid.get("监听群聊", List.class);
        if (list.contains(group_id)) {
            SignalUtil.sendGroupMessage(group_id, "当前群聊监听状态=true");
            return;
        }
        SignalUtil.sendGroupMessage(group_id, "当前群聊监听状态=false");
    }

    /**
     * 打印指定群聊是否标记了监听状态并发送消息给指定QQ号对象
     *
     * @param group_id 要判断的群号
     * @param user_id  发送给消息的QQ号
     */
    public static void printIslistening(String group_id, String user_id) {
        List<String> list = listeningorblackgroupid.get("监听群聊", List.class);
        if (list.contains(group_id)) {
            SignalUtil.sendPrivateMessage(user_id, "当前群聊监听状态=true");
            return;
        }
        SignalUtil.sendPrivateMessage(user_id, "当前群聊监听状态=false");
    }


    /**
     * 打印监听群聊的集合
     *
     * @param user_id
     */
    public static void printlisteninggroupGather(String user_id) {
        List<String> list = listeningorblackgroupid.get("监听群聊", List.class);
        JSONObject json = SignalUtil.sendPrivateMessage(user_id, "监听群聊的集合的对象:\n" + JSONUtil.parseArray(list).toStringPretty());
        if (json.isEmpty()) {
            log.info("打印监听群聊的集合-失败!");
            return;
        }
        log.info("打印监听群聊的集合-成功!");
    }


    /**
     * 往监听群聊的对象
     * 请确保机器人确实在该群
     *
     * @param group_id 群号,请确保机器人所在该群
     */
    public static void addlisteninggroupGather(String group_id) {
        JSONArray list = listeningorblackgroupid.get("监听群聊", JSONArray.class);
        if (!(list.add(group_id))) {
            log.info("往监听群聊的对象-失败!,该群已经在监听集合里了");
            SignalUtil.sendGroupMessage(group_id, "添加失败!,该群已经在监听集合里了!");
            return;
        }
        //更新本地文件和内存的json
        ListeningGroupModel.updateFileJSON();
        SignalUtil.sendGroupMessage(group_id, "已添加标记当前群为监听对象!");
    }

    /**
     * 删除监听群聊集合里指定的群聊对象
     *
     * @param group_id
     */
    public static void removelisteninggroupGather(String group_id) {
        JSONArray list = listeningorblackgroupid.get("监听群聊", JSONArray.class);
        if (!(list.remove(group_id))) {
            log.info("删除监听群聊集合里指定的群聊对象-失败!,该群并不在监听群聊集合里");
            SignalUtil.sendGroupMessage(group_id, "删除失败,该群并不在监听群聊集合里");
            return;
        }
        //更新本地文件和内存的json
        ListeningGroupModel.updateFileJSON();
        SignalUtil.sendGroupMessage(group_id, "已移除取消标记当前群聊的监听状态!");
    }


    /**
     * 添加标记指定群聊作为推送群聊
     * 添加到推送群聊集合对象
     *
     * @param group_id 群号
     */
    public static void addPoshGroupGather(String group_id) {
        JSONArray list = listeningorblackgroupid.get("推送群聊", JSONArray.class);
        boolean add = list.add(group_id);
        if (!add) {
            log.info("添加标记指定群聊作为推送群聊-添加失败!");
            SignalUtil.sendGroupMessage(group_id, "标记当前群为推送群聊失败,已经添加过了!");
            return;
        }
        log.info("添加标记指定群聊作为推送群聊-添加成功!");
        //更新本地文件和内存的json
        ListeningGroupModel.updateFileJSON();
        SignalUtil.sendGroupMessage(group_id, "已标记当前群为推送群聊");
    }

    /**
     * 取消标记指定群聊作为推送群聊
     * 从推送集合对象里移除
     *
     * @param group_id
     */
    public static void removePoshGroupGather(String group_id) {
        JSONArray list = listeningorblackgroupid.get("推送群聊", JSONArray.class);
        if (!(list.remove(group_id))) {
            log.info("取消标记指定群聊作为推送群聊-失败!");
            SignalUtil.sendGroupMessage(group_id, "取消标记当前群为推送群聊失败,当前群并不在推送集合内");
            return;
        }
        log.info("取消标记指定群聊作为推送群聊-成功!");
        //更新本地文件和内存的json
        ListeningGroupModel.updateFileJSON();
        SignalUtil.sendGroupMessage(group_id, "取消标记当前群为推送群聊成功");

    }

    /**
     * 权重,权重高的值会先匹配
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        //该类的权重要比正常群聊的权重大!
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
        if (!(("group".equals(message.getMessage_type())))) {
            //不是群聊
            return false;
        }
        this.dataGroup = BeanUtil.toBean(jsonObject, DataGroup.class);
        String user_id = dataGroup.getUser_id();
        String group_id = dataGroup.getGroup_id();
        String raw_message = dataGroup.getRaw_message();

        if (isequlContent(raw_message)) {
            //当群连续有两条消息重复时不执行下面操作(不是仅仅针对个人,两个发各自相同的内容也会判断为相同,全局性为群)
            return false;
        }
        JSONArray list = listeningorblackgroupid.get("监听群聊", JSONArray.class);
        if (list == null || list.isEmpty()) {
            //当监听群聊数组里没有内容时或者为nul,就不用监听
            return false;
        }
        if (!(list.contains(dataGroup.getGroup_id()))) {
            //不是监听群聊对象
            return false;
        }
        list = listeningorblackgroupid.get("黑名单用户", JSONArray.class);
        if (list.contains(user_id)) {
            log.info("用户触发了监听推送黑名单操作:ser_id=" + user_id);
            return false;
        }
        //判断是否有黑名单群聊
        list = listeningorblackgroupid.get("黑名单群聊", JSONArray.class);
        if (list.contains(group_id)) {
            log.info("群聊触发了监听推送黑名单操作:tgroup_id=" + group_id);
            return false;
        }
        if (raw_message.contains("口令") && "935671622".equals(group_id)) {
            log.info("该资源群触发了广告消息了,故本轮不推送给其他群,直接推送给超级用户");
            SignalUtil.sendPrivateMessage(DataUserEights.SUPERUSER.get(0), raw_message);
            return false;
        }
        if (MessageUtil.isTypeRecord(dataGroup.getMessage())) {
            log.info("检测到语音类型,故不推送");
            return false;
        }

//        if (!("935671622".equals(user_id))) {
        this.run();
//        }
        //先测试功能是否完善!
        //bhVip(group_id);
        return true;
    }


    /**
     * 如果是该群,则需要特殊点,当推送的内容不是图片时提示是否要接受消息!
     */
    private void bhVip(String group_id) {
        log.info("测试,接收到来自别的群推送消息!");
        SignalUtil.sendGroupMessage(group_id, "接收到来自别的群的推送消息,请在10秒内回复=同意接受推送消息 否则将不进行推送消息!");
        //每次执行就将该值设置为默认,然后让成员在群里发送指定确定即可!
        ListeningGroupModel.temp = "";
        System.out.println(JSONUtil.parseObj(dataGroup).toStringPretty());
        log.info("请在群里输入相关指令继续执行!,如果超时将按照不执行处理!");

        //倒计时休眠10秒,如果一直没有结果则不执行!
        for (int i = 10; i > 0; i--) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if ("yes".equals(ListeningGroupModel.temp)) {
                this.run();
                return;
            }
        }
        log.info("本次推送因无明确回复值,故结束!");
    }


    /**
     * 读取本地监听群聊、推送群聊、黑名单群聊json内容
     *
     * @param file file文件路径
     * @return jsonOBj对象
     */
    private static JSONObject getFileJSON(File file) {
        JSONObject entries;
        try {
            entries = JSONUtil.readJSONObject(file, StandardCharsets.UTF_8);
        } catch (IORuntimeException e) {
            log.info(e.getMessage());
            return new JSONObject(0);
        }
        if (entries.isEmpty()) {
            return new JSONObject(0);
        }
        return entries;
    }


    /**
     * 更新本地文件和内存的json
     */
    private static final void updateFileJSON() {
        FileUtil.writeUtf8String(listeningorblackgroupid.toStringPretty(), FILE);
        ListeningGroupModel.listeningorblackgroupid = ListeningGroupModel.getFileJSON(FILE);
    }

    private void isBlackGroup() {

    }


}






