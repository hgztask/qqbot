package com.example.qqbot.model.group;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.data.group.DataGroup;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.model.LanZouYmodel.LanZuoCloudResourceSearch;
import com.example.qqbot.model.ReReadingModel;
import com.example.qqbot.model.pgr.PGRModel;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.qqbot.Util.InformationUtil;

/**
 * 群聊逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/10 17:13
 */
@Slf4j
public class GroupModel implements Runnable {
    /**
     * 群聊数据层
     */
    private DataGroup dataGroup;

    /**
     * 外面接收到的jsonObj对象,可能用到
     */
    private JSONObject jsonObject;

    /**
     * 黑名单群聊
     */
    private static final File BLACK_PATHF_FILE = new File("E:\\黑名单群聊.json");


    public GroupModel(DataGroup dataGroup, JSONObject jsonObject) {
        this.dataGroup = dataGroup;
        this.jsonObject = jsonObject;
    }

    /**
     * 黑名单群组
     */
    private static Set<String> BLACKGROUPID = ReReadingModel.getFileJson(BLACK_PATHF_FILE);


    /**
     * 触发发送
     */
    private static final List<String> PGR_CONSCIOUS_COLLOCATION = ListUtil.toList("意识共鸣", "共鸣什么", "共鸣啥", "共鸣选啥", "共鸣怎么选", "意识带什么", "共鸣选哪个", "共鸣选什么", "意识推荐", "什么意识", "武器共鸣", "配队", "意识技能", "带啥意识");


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


    @Override
    @SuppressWarnings("all")
    public void run() {
        //获取到群聊对象里的QQ群号
        String group_id = dataGroup.getGroup_id();
        //发言者QQ号
        String user_id = dataGroup.getUser_id();
        Set<String> re_reading_member_set = ReReadingModel.getMEMBER_SET();

        //是否是超级用户发的消息
        boolean boolSupeRuser = DataUserEights.SUPERUSER.contains(user_id);

        String raw_message = dataGroup.getRaw_message();
        if (raw_message.startsWith("查询当前群聊黑名单状态") && boolSupeRuser) {
            GroupModel.printIsBlackGroup(group_id);
            return;
        }
        //过滤
        if (BLACKGROUPID.contains(group_id)) {
            log.info(group_id + "群触发了黑名单了");
            return;
        }
        log.info(StrUtil.format("{}群消息={}", dataGroup.getGroup_id(), raw_message));
        //实现触发某个关键词回复
        if (raw_message.startsWith("菜单")) {
            SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "菜单\n蓝奏云资源搜索=要搜索的资源");
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
            ReReadingModel.addReReadingMemberSet(dataGroup, userATID);
            return;
        }
        if (raw_message.startsWith("移除复读机成员") && boolSupeRuser) { //需要超级用户权限
            String userATID = InformationUtil.getUserATID(dataGroup);
            if (userATID.isEmpty()) {
                return;
            }
            ReReadingModel.removeReReadingMemberSet(dataGroup, userATID);
            return;
        }
        if (raw_message.startsWith("添加触发复读机关键词=") && boolSupeRuser) { //需要超级用户权限
            String key = InformationUtil.subEqual("=", raw_message);
            if (key.isEmpty()) {
                return;
            }
            ReReadingModel.addKeySet(dataGroup, key);
            return;
        }
        if (raw_message.startsWith("移除触发复读机关键词=") && boolSupeRuser) { //需要超级用户权限
            String key = InformationUtil.subEqual("=", raw_message);
            if (key.isEmpty()) {
                return;
            }
            ReReadingModel.removeKeySet(dataGroup, key);
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

        if (re_reading_member_set.contains(user_id) || InformationUtil.keyContains(ReReadingModel.getKEY_SET(), filtrationCQ(raw_message))) {
            //复读模块
            ReReadingModel re_reading_model = ReReadingModel.getRE_READING_MODEL();
            re_reading_model.setDataGroup(dataGroup);
            ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
            try {
                threadExecutor.execute(re_reading_model);
            } finally {
                threadExecutor.shutdown();
            }
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
     * 过滤消息中的CQ码,如头部[CQ:我是CQ的内容]尾部
     * 过滤之后=头部尾部
     * 如果过滤之后还是跟原来的一样,就返回原来的字符串
     * 去除字符串中[CQ:....]中的CQ内容
     *
     * @param content 要过滤的字符串
     * @return 过滤之后的结果
     */
    public static String filtrationCQ(@NonNull String content) {
        String str = content.replaceAll("(\\[CQ:).*?(\\])", "");
        if (str.isEmpty()) {
            return "";
        }
        if (str.equals(content)) {
            return content;
        }
        return str;
    }

    /**
     * 刷新黑名单群聊数据并重新赋值
     * 会重新读取本地指定路径的json文件重新赋值给指定集合对象
     *
     * @param user_id 接受消息者 一般是超级用户
     */
    public static void readFIlePathBlackList(String user_id) {
        BLACKGROUPID = ReReadingModel.getFileJson(BLACK_PATHF_FILE);
        SignalUtil.sendPrivateMessage(user_id, "已刷新黑名单群聊数据并重新赋值!");
    }

    /**
     * 添加指定群为黑名单
     *
     * @param group_id 群号
     */
    public static void addBlackGroup(String group_id) {
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
    public static void removeBlackGroup(String group_id) {
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
    public static boolean printIsBlackGroup(String group_id) {
        if (BLACKGROUPID.contains(group_id)) {
            SignalUtil.sendGroupMessage(group_id, "当前群聊黑名单状态=true");
            return true;
        }
        SignalUtil.sendGroupMessage(group_id, "当前群聊黑名单状态=true");
        return false;
    }


}





