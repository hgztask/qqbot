package com.example.qqbot.model.group;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.MessageUtil;
import com.example.qqbot.Util.NetworkUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.file.DataFile;
import com.example.qqbot.data.group.DataGroup;
import com.example.qqbot.data.json.DataJson;
import com.example.qqbot.model.pgr.PGRModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    private static final File BLACK_PATHF_FILE = new File("E:\\qqbot\\黑名单群聊.json");


    /**
     * 黑名单群组
     */
    private static Set<String> BLACKGROUPID = GroupReReadingModel.getFileJson(BLACK_PATHF_FILE);

    /**
     * 记录存储一直戳一戳的成员
     */
    private static final Set<String> USER_IDPOKESET = new HashSet<>();

    /**
     * 表情包api
     */
    private static final HashMap<String, String> walkImageUrlMap = new HashMap<>();

    private static final HashMap<String, String> mochaImageUrlMap = new HashMap<>();

    static {
        mochaImageUrlMap.put("抓", "https://api.xingzhige.com/API/grab/?qq=");
        mochaImageUrlMap.put("拍瓜", "https://api.xingzhige.com/API/paigua/?qq=");
        mochaImageUrlMap.put("顶球", "https://api.xingzhige.com/API/dingqiu/?qq=");
        mochaImageUrlMap.put("咬", "https://api.xingzhige.com/API/bite/?qq=");
        mochaImageUrlMap.put("看这个", "https://api.xingzhige.com/API/Lookatthis/?qq=");
        mochaImageUrlMap.put("保抱肉肉", "https://api.xingzhige.com/API/baororo/?qq=");
        mochaImageUrlMap.put("一起笑", "https://api.xingzhige.com/API/LaughTogether/?qq=");
        mochaImageUrlMap.put("招财猫", "https://api.xingzhige.com/API/FortuneCat/?qq=");
        mochaImageUrlMap.put("舞鸡腿", "https://api.xingzhige.com/API/DanceChickenLeg/?qq=");
        mochaImageUrlMap.put("打年糕", "https://api.xingzhige.com/API/pound/?qq=");
        mochaImageUrlMap.put("狠狠滴咬", "http://h.xiaocha.fun/api/yao/yao.php?QQ=");
        mochaImageUrlMap.put("滑稽锤你", "http://h.xiaocha.fun/api/chui/chui.php?QQ=");
        mochaImageUrlMap.put("发怒拍闹钟", "http://h.xiaocha.fun/api/nu/nu.php?QQ=");
        walkImageUrlMap.put("获取快手小姐姐图片", "http://api.wqwlkj.cn/wqwlapi/ks_xjj.php?type=json");
        walkImageUrlMap.put("获取cos小姐姐图片", "http://api.wqwlkj.cn/wqwlapi/hlxcos.php?type=json");
        walkImageUrlMap.put("获取快手二次元图片", "http://api.wqwlkj.cn/wqwlapi/ks_2cy.php?type=json");
    }

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
        //接受消息的qq机器人
        String self_id = dataGroup.getSelf_id();
        //消息id
        String message_id = dataGroup.getMessage_id();
        //消息的json样式
        JSONArray messageJson = dataGroup.getMessage();
        Set<String> re_reading_member_set = GroupReReadingModel.getMEMBER_SET();


        List<JSONObject> typeImageList = MessageUtil.getTypeImageList(messageJson);
        if (!(typeImageList.isEmpty())) {
            MessageUtil.downloadGroupImageThread(typeImageList, group_id, user_id);
        }
        JSONObject typeVideo = MessageUtil.getTypeVideo(messageJson);
        if (!(typeVideo.isEmpty())) {
            MessageUtil.downloadGroupVideoThread(typeVideo, group_id, user_id);
        }

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
        if (InformationUtil.isContainsMessAge(PGRModel.getPGR_CONSCIOUS_COLLOCATION(), raw_message) && !(group_id.equals("942611877"))) {
            PGRModel.consciousnessPGRTable(group_id, user_id);
            return;
        }
        if (InformationUtil.isContainsMessAge(PGRModel.getPGR_GUILDID(), raw_message)) {
            PGRModel.printPGRGuild(group_id, user_id);
            return;
        }
        if (raw_message.startsWith("添加复读机成员") && boolSupeRuser) { //需要超级用户权限
            //该关键词触发条件要优先于下面的复读机,要不然会导致复读操作
            String userATID = MessageUtil.getOneAtID(messageJson);
            if (userATID.isEmpty()) {
                return;
            }
            GroupReReadingModel.addReReadingMemberSet(dataGroup, userATID);
            return;
        }
        if (raw_message.startsWith("移除复读机成员") && boolSupeRuser) { //需要超级用户权限
            String userATID = MessageUtil.getOneAtID(messageJson);
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
            SignalUtil.sendGroupMessage(group_id, "正在请求中!");
            JSONArray ban = new JSONArray();
            for (int i = 0; i < 12; i++) {
                String url = DataFile.getRandomKey(DataFile.getYS_IMAGE_LIST());
                ban.add(DataJson.imageUrl(InformationUtil.subEqual("/", url), url, true));
            }
            JSONObject entries = SignalUtil.sendGroupForwardMsg(group_id, DataJson.nodeMerge("机器人", user_id, ban));
            if (entries.isEmpty()) {
                log.info("获取原神图出错了!");
                return;
            }
            if (entries.get("retcode", int.class) == 100) {
                log.info("获取原神图出错了!");
                SignalUtil.sendGroupMessage(group_id, "获取原神图出错了!");
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "获取成功!");
            return;
        }
        if (raw_message.startsWith("获取腿系列图")) {
            SignalUtil.sendGroupMessage(group_id, "正在请求腿系列图中!");
            JSONArray ban = new JSONArray();
            for (int i = 0; i < 20; i++) {
                String url = DataFile.getRandomKey(DataFile.getLEG_SERIESL_IST());
                ban.add(DataJson.imageUrl(InformationUtil.subEqual("/", url), url, true));
            }
            JSONObject entries = SignalUtil.sendGroupForwardMsg(group_id, DataJson.nodeMerge("机器人", dataGroup.getSelf_id(), ban));
            if (entries.isEmpty()) {
                log.info("获取腿系列图出错了!");
                return;
            }
            if (entries.get("retcode", int.class) == 100) {
                log.info("获取腿系列图出错了!");
                SignalUtil.sendGroupMessage(group_id, "获取腿系列图出错了!");
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "获取腿系列图成功!");
            return;
        }
        if (raw_message.startsWith("原神黄历")) {
            if (SignalUtil.sendGroupMessage(group_id, DataJson.imageUrl("原神黄历", "https://api.xingzhige.com/API/yshl/", false)).isEmpty()) {
                log.info("原神黄历发送失败");
                return;
            }
            log.info("原神黄历发送失败!");
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
        if (raw_message.startsWith("获取b站用户信息")) {
            String subEqual = InformationUtil.subEqual("获取b站用户信息", raw_message);
            if (subEqual.isEmpty()) {
                log.info("subEqual获取值失败,为空字符串");
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "正在获取!请稍等!");
            JSONObject uidJson = NetworkUtil.getBiBiUserinfo(subEqual);
            if (uidJson.isEmpty()) {
                log.info("biBiUserinfo获取值失败,为空字符串");
                return;
            }
            JSONArray jsonArray = new JSONArray();
            String biBiUserinfo = NetworkUtil.getBiBiUserinfo(uidJson);
            String face = uidJson.getByPath("owner.face", String.class);
            String cover = uidJson.getByPath("live_room.cover", String.class);
            jsonArray.add(DataJson.at(user_id));
            jsonArray.add(DataJson.imageUrl("头像", face, false));
            jsonArray.add(DataJson.text(biBiUserinfo));
            jsonArray.add(DataJson.imageUrl("直播间封面", cover, false));
            SignalUtil.sendGroupMessage(group_id, jsonArray.toString());
            return;
        }
        if (raw_message.startsWith("获取b站最新番剧信息")) {
            SignalUtil.sendGroupMessage(group_id, "正在获取b站最新番剧信息,请稍等!");
            JSONArray jsonArray = NetworkUtil.getTheLatestOpera();
            if (jsonArray.isEmpty()) {
                SignalUtil.sendGroupMessage(group_id, "获取b站最新番剧信息失败!");
                return;
            }
            if (SignalUtil.sendGroupForwardMsg(group_id, DataJson.nodeMerge("机器人", self_id, jsonArray)).isEmpty()) {
                log.info("发送b站最新番剧信息失败!");
                return;
            }
            log.info("发送b站最新番剧信息成功!");
            return;
        }
        if (raw_message.startsWith("获取b站新番更新时间表")) {
            SignalUtil.sendGroupMessage(group_id, "正在获取b站新番更新时间表,请稍等!");
            JSONArray jsonArray = NetworkUtil.getXinfanTimetable();
            if (jsonArray.isEmpty()) {
                SignalUtil.sendGroupMessage(group_id, "获取b站新番更新时间表失败!");
                return;
            }
            if (SignalUtil.sendGroupForwardMsg(group_id, DataJson.nodeMerge("机器人", self_id, jsonArray)).isEmpty()) {
                log.info("获取b站新番更新时间表失败!");
                return;
            }
            log.info("获取b站新番更新时间表成功!");
            return;
        }

        if (raw_message.startsWith("端口扫描")) {
            String subEqual = InformationUtil.subEqual("端口扫描", raw_message);
            if (subEqual.isEmpty()) {
                log.info("subEqual获取值失败,为空字符串");
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "正在获取!请稍等!");
            String portScan = NetworkUtil.portScan(subEqual);
            if (portScan.isEmpty()) {
                return;
            }
            SignalUtil.sendGroupMessage(group_id, DataJson.reply(message_id, user_id, portScan).toString());
        }
        if (raw_message.startsWith("历史上的今天")) {
            SignalUtil.sendGroupMessage(group_id, "正在获取历史上的今天,请稍等!");
            JSONObject json = NetworkUtil.getTodayInHistory();
            if (json.isEmpty()) {
                return;
            }
            if (SignalUtil.sendGroupMessage(group_id, json.get("day", String.class) + "\n" + json.get("content", JSONArray.class).toStringPretty()).isEmpty()) {
                log.info("历史上的今天获取失败");
                return;
            }
            log.info("历史上的今天获取成功!");
            return;
        }
        if (raw_message.startsWith("获取头像")) {
            String userATID = MessageUtil.getOneAtID(messageJson);
            if (userATID.isEmpty()) {
                return;
            }
            SignalUtil.sendGroupMessage(group_id, DataJson.imageUrl(user_id, "https://q1.qlogo.cn/g?b=qq&nk=" + userATID + "&s=640", true));
            return;
        }
        if (raw_message.startsWith("B站热搜榜")) {
            SignalUtil.sendGroupMessage(group_id, "正在获取B站热搜榜中,请稍等!");
            JSONArray jsonArray = NetworkUtil.getHotSearchListOfStationB(self_id);
            if (jsonArray.isEmpty()) {
                log.info("获取B站热搜榜失败!");
                return;
            }
            SignalUtil.sendGroupForwardMsg(group_id, jsonArray);
            SignalUtil.sendGroupMessage(group_id, DataJson.at(user_id));
            log.info("获取B站热搜榜成功!");
            return;
        }
        if (raw_message.startsWith("微博热搜")) {
            SignalUtil.sendGroupMessage(group_id, "正在获取微博热搜中,请稍等!");
            JSONArray jsonArray = NetworkUtil.weiboHotSearchList(self_id);
            if (jsonArray.isEmpty()) {
                log.info("获取微博热搜失败!");
                return;
            }
            SignalUtil.sendGroupForwardMsg(group_id, jsonArray);
            SignalUtil.sendGroupMessage(group_id, DataJson.at(user_id));
            log.info("获取微博热搜成功!");
            return;
        }
        if (raw_message.startsWith("获取每日60秒看世界")) {
            SignalUtil.sendGroupMessage(group_id, "正在获取每日60秒看世界,请稍后!");
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(DataJson.at(user_id));
            jsonArray.add(NetworkUtil.getDay60World(user_id));
            if (SignalUtil.sendGroupMessage(group_id, DataJson.text(jsonArray)).isEmpty()) {
                log.info("获取每日60秒看世界失败!");
                return;
            }
            log.info("获取每日60秒看世界成功!");
            return;
        }
        if (raw_message.startsWith("获取人生倒计时")) {
            SignalUtil.sendGroupMessage(group_id, "正在获取人生倒计时,请稍后!");
            String countdownToLife = NetworkUtil.getCountdownToLife();
            if (countdownToLife.isEmpty()) {
                log.info("获取人生倒计时失败!,字符串为空串");
                return;
            }
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(DataJson.at(user_id));
            jsonArray.add(DataJson.text(countdownToLife));
            if (SignalUtil.sendGroupMessage(group_id, DataJson.text(jsonArray)).isEmpty()) {
                log.info("获取人生倒计时失败!");
                return;
            }
            log.info("获人生倒计时成功!");
            return;
        }

        //该关键词要比下面的撤回关键词优先
        if (raw_message.startsWith("监听成员撤回消息") && boolSupeRuser) {
            List<JSONObject> atList = MessageUtil.getTypeList("at", messageJson);
            if (atList.isEmpty()) {
                return;
            }
            boolean temp = false;
            for (JSONObject v : atList) {
                String atID = v.getByPath("data.qq", String.class);
                if (atID == null || atID.isBlank()) {
                    continue;
                }
                if (ListeningGroupWithdrawalModel.addUserID(atID)) {
                    temp = true;
                }
            }
            if (temp) {
                SignalUtil.sendGroupMessage(group_id, "已登记指定监听成员撤回消息队列");
                FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(ListeningGroupWithdrawalModel.getListGroupUseID()), ListeningGroupWithdrawalModel.getLIST_GROUP_USEID_FILE_PATH());
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "监听该成员撤回消息-添加失败!");
            return;
        }
        //该关键词要比下面的撤回关键词优先
        if (raw_message.startsWith("取消监听成员撤回消息") && boolSupeRuser) {
            List<JSONObject> atList = MessageUtil.getTypeList("at", messageJson);
            if (atList.isEmpty()) {
                return;
            }
            boolean temp = false;
            for (JSONObject v : atList) {
                String atID = v.getByPath("data.qq", String.class);
                if (atID == null || atID.isBlank()) {
                    continue;
                }
                if (ListeningGroupWithdrawalModel.delUserID(atID)) {
                    temp = true;
                }
            }
            if (temp) {
                SignalUtil.sendGroupMessage(group_id, "已取消指定监听成员撤回消息队列");
                FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(ListeningGroupWithdrawalModel.getListGroupUseID()), ListeningGroupWithdrawalModel.getLIST_GROUP_USEID_FILE_PATH());
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "取消监听该成员撤回消息-取消失败!");
            return;
        }
        //该关键词要比下面的撤回关键词优先
        if (raw_message.startsWith("清空监听成员内存中的撤回消息") && boolSupeRuser) {
            List<JSONObject> atList = MessageUtil.getTypeList("at", messageJson);
            if (atList.isEmpty()) {
                return;
            }
            boolean temp = false;
            for (JSONObject v : atList) {
                String atID = v.getByPath("data.qq", String.class);
                if (atID == null || atID.isBlank()) {
                    continue;
                }
                if (ListeningGroupWithdrawalModel.delMessage(atID)) {
                    temp = true;
                }
            }
            if (temp) {
                SignalUtil.sendGroupMessage(group_id, "已清空指定监听成员内存中的撤回消息");
                FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(ListeningGroupWithdrawalModel.getListGroupUseID()), ListeningGroupWithdrawalModel.getLIST_GROUP_USEID_FILE_PATH());
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "清空监听成员内存中的撤回消息-失败!");
            return;
        }
        //该关键词要比下面的撤回关键词优先
        if (raw_message.startsWith("查询指定成员是否是监听成员") && boolSupeRuser) {
            String oneAtID = MessageUtil.getOneAtID(messageJson);
            if (oneAtID.isEmpty()) {
                return;
            }
            if (ListeningGroupWithdrawalModel.readUserID(oneAtID)) {
                SignalUtil.sendGroupMessage(group_id, "该成员已在监听状态");
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "该成员未在监听状态");
            return;
        }
        //该关键词要比上面的撤回关键词底
        if (raw_message.contains("撤回")) { //让机器人撤回消息
            String messageReplyID = MessageUtil.getReplyID(messageJson);
            if (messageReplyID.isEmpty()) {
                return;
            }
            List<JSONObject> ayTypeList = MessageUtil.getTypeList("at", messageJson);
            if (ayTypeList.isEmpty()) {
                return;
            }
            String qq = ayTypeList.get(0).getByPath("data.qq", String.class);
            if (qq == null || qq.isEmpty()) {
                return;
            }
            if (SignalUtil.deleteMsg(messageReplyID).isEmpty()) {
                log.info("撤回失败!");
                return;
            }
            log.info("撤回成功!");
            return;
        }
        if (raw_message.contains("星期四")) {
            String copywriting = NetworkUtil.getCrazyThursdayCopywriting();
            if (copywriting.isEmpty()) {
                return;
            }
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(DataJson.at(user_id));
            jsonArray.add(DataJson.text(copywriting));
            if (SignalUtil.sendGroupMessage(group_id, jsonArray).isEmpty()) {
                log.info("星期四发送失败!");
                return;
            }
            log.info("星期四发送成功!");
        }
        if (raw_message.startsWith("获取指定B站用户最新动态")) {
            String subEqual = InformationUtil.subEqual("获取指定B站用户最新动态", raw_message);
            if (subEqual.isEmpty()) {
                return;
            }
            try {
                Integer.valueOf(subEqual);
            } catch (NumberFormatException e) {
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "正在获取中,请稍后");
            JSONArray station = NetworkUtil.getAnalyzeTheDynamicApiOfStation(subEqual);
            if (station.isEmpty()) {
                return;
            }
            JSONArray jsonArray = new JSONArray();
            JSONObject item = JSONUtil.parseObj(station.get(0));
            String textContent = item.get("textContent", String.class);
            List<String> img_srcList = item.get("img_src", List.class);
            jsonArray.add(DataJson.text(textContent));
            for (String s : img_srcList) {
                jsonArray.add(DataJson.imageUrl(InformationUtil.subEqual("/", s), s, true));
            }
            SignalUtil.sendGroupForwardMsg(group_id, DataJson.nodeMerge("机器人", self_id, jsonArray));
            SignalUtil.sendGroupMessage(group_id, "获取成功!");
            return;
        }
        if (raw_message.startsWith("喜加一")) {
            SignalUtil.sendGroupMessage(group_id, "正在获取喜加一!");
            List<Map<String, String>> xijiayi = NetworkUtil.getXijiayi();
            if (xijiayi == null || xijiayi.isEmpty()) {
                SignalUtil.sendGroupMessage(group_id, "未获取到喜加一,可能是当天并未有!");
                return;
            }
            if (xijiayi.size() <= 1) {
                Map<String, String> map = xijiayi.get(0);
                SignalUtil.sendGroupMessage(group_id, CharSequenceUtil.format("""
                        今天喜加一内容:
                        游戏名:{}
                        地址:{}
                        开始时间:{}
                        结束时间:{}
                        商店:{}
                        """, map.get("gameName"), map.get("href"), map.get("startTime"), map.get("endTime"), map.get("store")));
                return;
            }
            JSONArray nodeArr = new JSONArray(xijiayi.size());
            nodeArr.add(DataJson.text("今天喜加一内容"));
            for (Map<String, String> map : xijiayi) {
                String format = CharSequenceUtil.format("""
                        游戏名:{}
                        地址:{}
                        开始时间:{}
                        结束时间:{}
                        商店:{}
                        """, map.get("gameName"), map.get("href"), map.get("startTime"), map.get("endTime"), map.get("store"));
                nodeArr.add(DataJson.text(format));
            }
            SignalUtil.sendGroupForwardMsg(group_id, DataJson.nodeMerge("机器人", self_id, nodeArr));
            return;
        }
        if (raw_message.startsWith("获取mikan最新磁力资源")) {
            SignalUtil.sendGroupMessage(group_id, "正在获取mikan最新磁力.请稍后!");
            JSONArray mikanRSSList = NetworkUtil.getMikanNewestList();
            if (mikanRSSList.isEmpty()) {
                SignalUtil.sendGroupMessage(group_id, "获取最新mikan磁力失败!");
                return;
            }
            SignalUtil.sendGroupForwardMsg(group_id, DataJson.nodeMerge("机器人", self_id, mikanRSSList));
        }
        if (raw_message.startsWith("获取萌番组最新种子资源")) {
            SignalUtil.sendGroupMessage(group_id, "正获取萌番组最新种子资源.请稍后!");
            JSONArray bangumiNewRss = NetworkUtil.getBangumiNewRss();
            if (bangumiNewRss.isEmpty()) {
                SignalUtil.sendGroupMessage(group_id, "获取萌番组最新种子资源失败!");
                return;
            }
            SignalUtil.sendGroupForwardMsg(group_id, DataJson.nodeMerge("机器人", self_id, bangumiNewRss));
            return;
        }
        if (raw_message.startsWith("mikan磁力搜索=")) {
            String key = InformationUtil.subEqual("=", raw_message);
            if (key.isEmpty() || key.length() < 1) {
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "正在搜索相关资源.请稍后!");
            JSONArray searchArr = NetworkUtil.getMikanSearch(key);
            if (searchArr.isEmpty()) {
                SignalUtil.sendGroupMessage(group_id, "未搜索到 " + key + " 相关资源!");
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "已搜索到 " + key + " 资源!");
            if (searchArr.size() == 1) {
                SignalUtil.sendGroupMessage(group_id, searchArr);
                return;
            }
            SignalUtil.sendGroupForwardMsg(group_id, DataJson.nodeMerge("机器人", self_id, searchArr));
            return;
        }
        if (raw_message.startsWith("动漫花园磁力搜索=") && !(raw_message.contains("关键词"))) {
            String key = InformationUtil.subEqual("=", raw_message);
            if (key.isEmpty() || key.length() <= 1) {
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "正在搜索相关资源.请稍后!");
            JSONArray searchArr = NetworkUtil.getDMHYSearch(key);
            if (searchArr.isEmpty()) {
                SignalUtil.sendGroupMessage(group_id, "未搜索到 " + key + " 相关资源!");
                return;
            }
            if (searchArr.size() == 1) {
                SignalUtil.sendGroupMessage(group_id, searchArr);
                return;
            }
            if (SignalUtil.sendGroupForwardMsg(group_id, DataJson.nodeMerge("机器人", self_id, searchArr)).isEmpty()) {
                SignalUtil.sendGroupMessage(group_id, "发送失败!请检查控制台内容");
                return;
            }
            return;
        }
        if (raw_message.startsWith("获取群成员列表") && boolSupeRuser) {
            List<JSONObject> dataList = SignalUtil.getGroupMemberList(group_id, true).getByPath("data", List.class);
            if (dataList == null || dataList.isEmpty()) {
                return;
            }
//            File file = FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(dataList), new File("E:\\群成员列表.json"));
//            SignalUtil.sendGroupMessage(group_id, "已保存在本地文件" + file.getName());
        }


        if (raw_message.startsWith("心碎碎")) {
            String userATID = MessageUtil.getOneAtID(messageJson);
            if (userATID.isEmpty()) {
                log.info("获取userATID的值为空字符串");
                return;
            }
            String imgeUrl = "http://h.xiaocha.fun/api/sui/sui.php?QQ=" + userATID;
            SignalUtil.sendGroupMessage(group_id, DataJson.imageUrl(InformationUtil.subEqual("/", imgeUrl), imgeUrl, true));
            return;
        }

        if (raw_message.contains("获取引用图片直链")) {
            String replyID = MessageUtil.getReplyID(messageJson);
            //根据消息ID获取原消息
            JSONObject message = SignalUtil.getMessage(replyID);
            if (message.isEmpty()) {
                log.info("getMessage方法返回的json为空的Json对象");
                return;
            }
            //获取到message内容列表
            JSONArray jsonArray = message.getByPath("data.message", JSONArray.class);
            if (jsonArray == null || jsonArray.isEmpty()) {
                log.info("byPath非Json对象或者为null");
                return;
            }
            Set<String> messageTypeList = MessageUtil.getTypeImageURLList(jsonArray);
            SignalUtil.sendGroupMessage(group_id, "引用图片直链:\n" + JSONUtil.toJsonPrettyStr(messageTypeList));
            return;
        }

        if (raw_message.startsWith("一直戳一戳") && boolSupeRuser) {
            String oneAtID = MessageUtil.getOneAtID(messageJson);
            if (oneAtID.isEmpty()) {
                return;
            }
            if (!(USER_IDPOKESET.add(user_id))) {
                SignalUtil.sendGroupMessage(group_id, "添加失败!,已经添加过了");
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "添加成功成功!");
            ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
            threadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    while (USER_IDPOKESET.contains(user_id)) {
                        SignalUtil.sendGroupMessage(group_id, DataJson.groupPoke(user_id));
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            threadExecutor.shutdown();
            return;
        }
        if (raw_message.startsWith("取消一直戳一戳") && boolSupeRuser) {
            String oneAtID = MessageUtil.getOneAtID(messageJson);
            if (oneAtID.isEmpty()) {
                return;
            }
            if (USER_IDPOKESET.remove(user_id)) {
                SignalUtil.sendGroupMessage(group_id, "取消成功!");
                return;
            }
            SignalUtil.sendGroupMessage(group_id, "取消失败!");
            return;
        }
        for (String key : mochaImageUrlMap.keySet()) {
            if (!(raw_message.startsWith(key))) {
                continue;
            }
            String atID = MessageUtil.getOneAtID(messageJson);
            if (atID.isEmpty()) {
                return;
            }
            String url = mochaImageUrlMap.get(key) + atID;
            String imageFIleNmae = InformationUtil.subEqual("/", url);
            SignalUtil.sendGroupMessage(group_id, DataJson.imageUrl(imageFIleNmae, url, true));
            return;
        }

        for (String key : walkImageUrlMap.keySet()) {
            if (!(key.equals(raw_message))) {
                continue;
            }
            SignalUtil.sendGroupMessage(group_id, "正在请求" + key + "中!");
            JSONArray ban = new JSONArray();
            for (int i = 0; i < 20; i++) {
                String wqwlkjImageUrl = NetworkUtil.getWqwlkjImageUrl(walkImageUrlMap.get(key));
                if (wqwlkjImageUrl.isEmpty()) {
                    continue;
                }
                ban.add(DataJson.imageUrl(InformationUtil.subEqual("/", wqwlkjImageUrl), wqwlkjImageUrl, true));
            }
            if (ban.isEmpty()) {
                return;
            }
            if (SignalUtil.sendGroupForwardMsg(group_id, DataJson.nodeMerge("机器人", self_id, ban)).isEmpty()) {
                log.info("发送" + key + "失败");
                return;
            }
            log.info("发送" + key + "成功");
            return;
        }
        if (raw_message.startsWith("摸")) {
            List<JSONObject> at = MessageUtil.getTypeList("at", messageJson);
            if (at.isEmpty()) {
                return;
            }
            ExecutorService threadPool = Executors.newFixedThreadPool(at.size());
            try {
                for (JSONObject jsonObject : at) {
                    String qq = jsonObject.getByPath("data.qq", String.class);
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.add(DataJson.at(user_id));
                    jsonArray.add(DataJson.imageUrl(qq, "http://api.wqwlkj.cn/wqwlapi/mo.php?qq=" + qq, true));
                    threadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            SignalUtil.sendGroupMessage(group_id, jsonArray);
                        }
                    });
                }
            } finally {
                threadPool.shutdown();
            }
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

        if (MessageUtil.isTypeRecord(dataGroup.getMessage())) {
            log.info("是语音消息!");
            return false;
        }
        this.run();
        return true;
    }
}





