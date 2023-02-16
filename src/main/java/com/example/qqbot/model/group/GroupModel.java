package com.example.qqbot.model.group;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.NetworkUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.CQ.CQCode;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.file.DataFile;
import com.example.qqbot.data.group.DataGroup;
import com.example.qqbot.data.json.DataJson;
import com.example.qqbot.model.LanZouYmodel.LanZuoCloudResourceSearch;
import com.example.qqbot.model.pgr.PGRModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 群聊逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/10 17:13
 */
@Slf4j
@Component
public class GroupModel implements Runnable, IMessageEvent {
    /**
     * 群聊数据层
     */
    private DataGroup dataGroup;


    /**
     * 黑名单群聊
     */
    private static final File BLACK_PATHF_FILE = new File("E:\\黑名单群聊.json");


    /**
     * 黑名单群组
     */
    private static Set<String> BLACKGROUPID = GroupReReadingModel.getFileJson(BLACK_PATHF_FILE);


    /**
     * PGR
     */
    private static final List<String> PGR_CONSCIOUS_COLLOCATION = ListUtil.toList("意识共鸣", "共鸣什么", "共鸣啥", "共鸣选啥", "共鸣怎么选", "意识带什么",
            "共鸣选哪个", "共鸣选什么", "意识推荐", "什么意识", "武器共鸣", "配队", "意识技能", "用啥意识", "带啥意识");


    /**
     * 记录群员最后一个发言者
     * 用来判断该用户私发连续发送消息,达标则执行某些操作
     * <p>
     * <p>
     * 可以分析,一个群里只有一个连续超多少消息的,
     * 但多个群不同,所以用kv集合存储数据
     */
    @SuppressWarnings("all")
    private static final Map<String, Integer> user_idEqul = new HashMap<>(0);


    /**
     * 是否把群消息打印在控制台
     */
    private static boolean isPrintGroupMessageConsole = false;


    @Override
    @SuppressWarnings("all")
    public void run() {
        //获取到群聊对象里的QQ群号
        String group_id = dataGroup.getGroup_id();
        //发言者QQ号
        String user_id = dataGroup.getUser_id();
        //消息id
        String message_id = dataGroup.getMessage_id();
        Set<String> re_reading_member_set = GroupReReadingModel.getMEMBER_SET();

        //是否是超级用户发的消息
        boolean boolSupeRuser = DataUserEights.SUPERUSER.contains(user_id);

        String raw_message = dataGroup.getRaw_message();
        if (raw_message.startsWith("查询当前群聊黑名单状态") && boolSupeRuser) {
            printIsBlackGroup(group_id);
            return;
        }
        //过滤
        if (BLACKGROUPID.contains(group_id)) {
            log.info(group_id + "群触发了黑名单了");
            return;
        }

        if (isPrintGroupMessageConsole) {
            log.info(StrUtil.format("{}群消息={}", dataGroup.getGroup_id(), raw_message));
        }

        if (user_id.equals("2978778354")) {
            System.out.println(raw_message);
            System.out.println(dataGroup.getMessage_id());
            System.out.println(dataGroup.getMessage());
        }


        //实现触发某个关键词回复
        if (raw_message.startsWith("菜单")) {
            SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), CharSequenceUtil.format("菜单\n蓝奏云资源搜索=要搜索的资源"));
            return;
        }
        if (InformationUtil.isContainsMessAge(PGR_CONSCIOUS_COLLOCATION, raw_message)) {
            PGRModel.consciousnessPGRTable(group_id, user_id);
        }
        if (InformationUtil.isContainsMessAge(PGRModel.getPGR_GUILDID(), raw_message)) {
            PGRModel.printPGRGuild(group_id, user_id);
            return;
        }
        if (raw_message.startsWith("蓝奏云资源搜索=") && boolSupeRuser) {
            String key = InformationUtil.subEqual("=", raw_message);
            if (key.isEmpty()) {
                return;
            }
            new LanZuoCloudResourceSearch(key, dataGroup).run();
            return;
        }
        if (raw_message.startsWith("添加复读机成员") && boolSupeRuser) { //需要超级用户权限
            //该关键词触发条件要优先于下面的复读机,要不然会导致复读操作
            String userATID = InformationUtil.getUserATID(dataGroup);
            if (userATID.isEmpty()) {
                return;
            }
            GroupReReadingModel.addReReadingMemberSet(dataGroup, userATID);
            return;
        }
        if (raw_message.startsWith("移除复读机成员") && boolSupeRuser) { //需要超级用户权限
            String userATID = InformationUtil.getUserATID(dataGroup);
            if (userATID.isEmpty()) {
                return;
            }
            GroupReReadingModel.removeReReadingMemberSet(dataGroup, userATID);
            return;
        }
        if (raw_message.startsWith("添加触发复读机关键词=") && boolSupeRuser) { //需要超级用户权限
            String key = InformationUtil.subEqual("=", raw_message);
            if (key.isEmpty()) {
                return;
            }
            GroupReReadingModel.addKeySet(dataGroup, key);
            return;
        }
        if (raw_message.startsWith("移除触发复读机关键词=") && boolSupeRuser) { //需要超级用户权限
            String key = InformationUtil.subEqual("=", raw_message);
            if (key.isEmpty()) {
                return;
            }
            GroupReReadingModel.removeKeySet(dataGroup, key);
            return;
        }
        if (raw_message.startsWith("标记当前群聊监听状态") && boolSupeRuser) {
            ListeningGroupModel.addlisteninggroupGather(group_id);
            return;
        }
        if (raw_message.startsWith("标记当前群聊推送状态") && boolSupeRuser) {
            ListeningGroupModel.addPoshGroupGather(group_id);
            return;
        }
        if (raw_message.startsWith("取消当前群聊监听状态") && boolSupeRuser) {
            ListeningGroupModel.removelisteninggroupGather(group_id);
            return;
        }
        if (raw_message.startsWith("取消当前群聊推送状态") && boolSupeRuser) {
            ListeningGroupModel.removePoshGroupGather(group_id);
            return;
        }
        if (raw_message.startsWith("查询当前群聊监听状态") && boolSupeRuser) {
            ListeningGroupModel.printIslistening(group_id);
            return;
        }
        if (raw_message.startsWith("查询当前群聊推送状态") && boolSupeRuser) {
            ListeningGroupModel.printIsPush(group_id);
            return;
        }
        if (raw_message.startsWith("标记当前群聊黑名单状态") && boolSupeRuser) {
            addBlackGroup(group_id);
            return;
        }
        if (raw_message.startsWith("取消当前群聊黑名单状态") && boolSupeRuser) {
            removeBlackGroup(group_id);
            return;
        }
        if (raw_message.startsWith("同意接受推送消息")) {
            log.info("已同意来自推送消息的内容!");
            ListeningGroupModel.temp = "yes";
            return;
        }
        if (raw_message.startsWith("设置群消息打印在控制台值=") && boolSupeRuser) {
            String subEqual = InformationUtil.subEqual("=", raw_message);
            try {
                isPrintGroupMessageConsole = Boolean.valueOf(subEqual);
                log.info("已设置群消息打印在控制台值的值为" + subEqual);
                SignalUtil.sendGroupMessage(group_id, "[CQ:at,qq=" + user_id + "]已设置是否允许群消息打印在控制台值的值=" + subEqual);
            } catch (Exception e) {
                log.info("设置群消息打印在控制台值的值转换错误!");
                return;
            }
        }
        if (raw_message.startsWith("获取原神图")) {
            String url = DataFile.getRandomKey();
            JSONObject json = SignalUtil.sendGroupMessage(group_id, CQCode.image(InformationUtil.lastSubEqual("/", url), URLEncodeUtil.encode(url), false));
            System.out.println(json.toStringPretty());
            System.out.println("=====================");
            return;
        }
        if (raw_message.startsWith("翻译")) {
            String subEqual = InformationUtil.subEqual("翻译", raw_message);
            if (subEqual.isEmpty()) {
                log.info("subEqual获取值失败,为空字符串");
                return;
            }
            JSONObject translate = NetworkUtil.translate(subEqual);
            if (translate.isEmpty()) {
                log.info("translate获取值失败,为空的json对象");
                return;
            }
            String tgt = translate.getByPath("translateResult[0][0].tgt", String.class);
            if (tgt == null || tgt.isEmpty()) {
                SignalUtil.sendGroupMessage(group_id, "翻译失败!");
                return;
            }
            JSONArray reply = DataJson.reply(message_id, user_id, tgt);
            SignalUtil.sendGroupMessage(group_id, reply.toString());
            return;
        }
        if (raw_message.startsWith("解析蓝奏云直链") & boolSupeRuser) {
            String subEqual = InformationUtil.subEqual("解析蓝奏云直链", raw_message);
            if (subEqual.isEmpty()) {
                log.info("subEqual获取值失败,为空字符串");
                return;
            }
            String url = NetworkUtil.analyticStraightChainLanZouY(subEqual);
            if (url.isEmpty()) {
                SignalUtil.sendGroupMessage(group_id, "获取失败!");
                return;
            }
            JSONArray reply = DataJson.reply(message_id, user_id, url);
            SignalUtil.sendGroupMessage(group_id, reply.toString());
            return;
        }


        if (re_reading_member_set.contains(user_id) || InformationUtil.keyContains(GroupReReadingModel.getKEY_SET(), InformationUtil.filtrationCQ(raw_message))) {
            //复读模块
            GroupReReadingModel re_reading_model = GroupReReadingModel.getRE_READING_MODEL();
            re_reading_model.setDataGroup(dataGroup);
            ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
            try {
                threadExecutor.execute(re_reading_model);
            } finally {
                threadExecutor.shutdown();
            }
            return;
        }
        if (raw_message.startsWith("获取头像") && boolSupeRuser) {
            String userATID = InformationUtil.getUserATID(dataGroup);
            if (userATID.isEmpty()) {
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "[CQ:image,file=头像,url=https://q1.qlogo.cn/g?b=qq&nk=" + userATID + "&s=640]");
            return;
        }
        if (raw_message.startsWith("执行demo") && boolSupeRuser) {
            if (SignalUtil.set_group_whole_ban(group_id, true).isEmpty()) {
                log.info("测试执行群聊禁言失败!");
                return;
            }
            log.info("测试执行群聊禁言成功!");
            return;
        }
    }


    /**
     * 刷新黑名单群聊数据并重新赋值
     * 会重新读取本地指定路径的json文件重新赋值给指定集合对象
     *
     * @param user_id 接受消息者 一般是超级用户
     */
    public static void readFIlePathBlackList(String user_id) {
        BLACKGROUPID = GroupReReadingModel.getFileJson(BLACK_PATHF_FILE);
        SignalUtil.sendPrivateMessage(user_id, "已刷新黑名单群聊数据并重新赋值!");
    }

    /**
     * 添加指定群为黑名单
     *
     * @param group_id 群号
     */
    public void addBlackGroup(String group_id) {
        if (BLACKGROUPID.contains(group_id)) {
            SignalUtil.sendGroupMessage(group_id, "当前群已存在黑名单中!");
            return;
        }
        BLACKGROUPID.add(group_id);
        FileUtil.writeUtf8String(JSONUtil.parseArray(BLACKGROUPID).toStringPretty(), BLACK_PATHF_FILE);
        SignalUtil.sendGroupMessage(group_id, "当前群已添加进去黑名单!");
    }

    /**
     * 从黑名单群聊中移除指定群聊
     *
     * @param group_id 群号
     */
    public void removeBlackGroup(String group_id) {
        if (BLACKGROUPID.remove(group_id)) {
            FileUtil.writeUtf8String(JSONUtil.parseArray(BLACKGROUPID).toStringPretty(), BLACK_PATHF_FILE);
            SignalUtil.sendGroupMessage(group_id, "当前群已从黑名单移除!");
            return;
        }
        SignalUtil.sendGroupMessage(group_id, "移除失败!,当前群并未在黑名单里!");
    }

    /**
     * 打印黑名单群聊
     *
     * @param user_id 发送给用户
     */
    public static void printBlackGroup(String user_id) {
        SignalUtil.sendPrivateMessage(user_id, "黑名单群聊:" + JSONUtil.parseArray(BLACKGROUPID).toStringPretty());
    }

    /**
     * 打印当前群聊是否是黑名单群聊
     * 需要注意的是,该级别优先级要高于判断黑名单组!
     *
     * @param group_id
     * @return
     */
    public boolean printIsBlackGroup(String group_id) {
        if (BLACKGROUPID.contains(group_id)) {
            SignalUtil.sendGroupMessage(group_id, "当前群聊黑名单状态=true");
            return true;
        }
        SignalUtil.sendGroupMessage(group_id, "当前群聊黑名单状态=false");
        return false;
    }


    /**
     * 权重
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 5;
    }

    /**
     * 接受消息
     *
     * @param jsonObject
     * @param message
     * @return 是否匹配成功
     */
    @Override
    @SuppressWarnings("all")
    public boolean onMessage(JSONObject jsonObject, Message message) {
        if (!("group".equals(message.getMessage_type()))) {
            return false;
        }
        this.dataGroup = BeanUtil.toBean(jsonObject, DataGroup.class);
        String user_id = dataGroup.getUser_id();
        String group_id = dataGroup.getGroup_id();
        //群聊消息
        if ("2854196310".equals(user_id)) {
            log.info(group_id + "无法回复Q群管家的信息!");
            return false;
        }

        if (InformationUtil.isMessageTypeRecord(dataGroup.getMessage())) {
            System.out.println("是语音消息!");
            return false;
        }

        this.run();
        return true;
    }
}





