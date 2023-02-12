package com.example.qqbot.model;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.SignalUtil;
import com.example.qqbot.Util.UserInfoUtil;
import com.example.qqbot.data.DataGroup;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.model.LanZouYmodel.LanZuoCloudResourceSearch;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

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
     * message类
     */
    private final Map<String, Object> message;


    public GroupModel(DataGroup dataGroup, JSONObject jsonObject) {
        this.dataGroup = dataGroup;
        this.jsonObject = jsonObject;
        this.message = JSONUtil.parseObj(dataGroup.getMessage().get(0));
    }

    /**
     * 黑名单群组
     */
    private static final List<String> groupIDArray = ListUtil.toList();


    /**
     * 触发发送
     */
    private static final List<String> PGR_CONSCIOUS_COLLOCATION = ListUtil.toList("意识共鸣", "共鸣什么", "共鸣啥", "共鸣选啥", "共鸣怎么选", "意识带什么", "共鸣选哪个", "共鸣选什么", "意识推荐", "什么意识", "武器共鸣", "配队", "意识技能", "带啥意识");

    @SuppressWarnings("all")
    private static final List<String> PGR_GUILDID = ListUtil.toList("公会id", "公会ID", "工会", "公会", "招人");


    @Override
    @SuppressWarnings("all")
    public void run() {
        //获取到群聊对象里的QQ群号
        String group_id = dataGroup.getGroup_id();
        List<String> re_reading_member_set = ReReadingModel.getRE_READING_MEMBER_SET();
        //过滤
        if (groupIDArray.contains(group_id)) {
            log.info(group_id + "群触发了黑名单了");
            return;
        }
        String raw_message = dataGroup.getRaw_message();
        log.info(StrUtil.format("{}群消息={}", dataGroup.getGroup_id(), raw_message));
        //实现触发某个关键词回复
        HashMap<String, String> data = new HashMap<>();
        data.put("group_id", dataGroup.getGroup_id());
        JSONObject json = null;
        if (raw_message.contains("获取艾特信息")) {
            log.info("获取艾特信息");
            log.info(jsonObject.toStringPretty());
            return;
        } else if (raw_message.contains("菜单")) {
            SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "菜单\n蓝奏云资源搜索=要搜索的资源");
            return;
        } else if (isContainsMessAge(PGR_CONSCIOUS_COLLOCATION, raw_message)) {
            //针对于发图片的类型,目前测试需要带上file参数,这个应该是必带,试了不带发不出去,例外图片如果是网络链接的最好是图片格式的那种,以此为戒
            data.put("message", String.format("[CQ:at,qq=%s]意识推荐共鸣配队站位表\n" +
                    "可用于角色意识搭配、意识共鸣、组队推荐\n" +
                    "看不清的请放大加载原图\n[CQ:image,file=战双意识表,url=https://s1.ax1x.com/2023/01/18/pS3Iekq.png]", dataGroup.getUser_id()));
            json = SignalUtil.httpGet(SignalUtil.getGROUPENDPOINT(), data);
        } else if (isContainsMessAge(PGR_GUILDID, raw_message)) {
            data.put("message", String.format("[CQ:at,qq=%s]本群的公会\n" +
                    "公会名：桐子酱的光剑\n" +
                    "信标服公会ID：00008433\n" +
                    "星火服公会ID：00009669\n" +
                    "星火就是官服如官网下载taptap下载等，反之星标即渠道服，手机商店下载", dataGroup.getUser_id()));
            json = SignalUtil.httpGet(SignalUtil.getGROUPENDPOINT(), data);
        } else if (raw_message.contains("蓝奏云资源搜索=")) {
            if (!raw_message.startsWith("蓝奏云资源搜索=")) {
                log.info("用户触发了蓝奏云资源搜索,但是未按照格式书写!");
                return;
            }
            raw_message = raw_message.substring(8).trim(); //获取到用户要搜索的内容,并去除关键词前后空格
            if (raw_message.isEmpty() || raw_message.length() < 3) {
                log.info("用户触发了蓝奏云资源搜索，当关键词为空或者长度小于3，请正确输入内容");
                return;
            }
            if (DataUserEights.SUPERUSER.contains(dataGroup.getUser_id())) {
                log.info("超级用户执行了该模块");
                new LanZuoCloudResourceSearch(raw_message, dataGroup).run();
                return;
            }
            log.info("非超级用户尝试执行该模块但是被拒绝了");
            return;
        } else if (raw_message.startsWith("添加复读机成员") && DataUserEights.SUPERUSER.contains(dataGroup.getUser_id())) { //需要超级用户权限
            //该关键词触发条件要优先于下面的复读机,要不然会导致复读操作
            String userATID = UserInfoUtil.getUserATID(dataGroup);
            if (userATID.isEmpty()) {
                return;
            }
            ReReadingModel.addReReadingMemberSet(dataGroup, userATID);
            return;
        } else if (raw_message.startsWith("移除复读机成员") && DataUserEights.SUPERUSER.contains(dataGroup.getUser_id())) { //需要超级用户权限
            String userATID = UserInfoUtil.getUserATID(dataGroup);
            if (userATID.isEmpty()) {
                return;
            }
            ReReadingModel.removeReReadingMemberSet(dataGroup, userATID);
            return;
        } else if (raw_message.startsWith("打印复读机成员") && DataUserEights.SUPERUSER.contains(dataGroup.getUser_id())) {//需要超级用户权限
            ReReadingModel.printReReadingMemberSet(dataGroup);
            return;
        } else if (re_reading_member_set.contains(dataGroup.getUser_id())) {
            new ReReadingModel(dataGroup).run();
            return;
        } else {
            return;
        }
        //下面代码会影响后期模块调整,后面需要移除,在此基础上后面的代码不需要return
        if (json == null || json.isEmpty() || json.getByPath("retcode", int.class) != 0) {
            System.out.println("请求失败!!");
            return;
        }

    }

    private static boolean isContainsMessAge(List<String> listContent, String raw_message) {
        for (String s : listContent) {
            if (s.contains(raw_message)) {
                return true;
            }
        }
        return false;
    }

}





