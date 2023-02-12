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
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
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
     * 黑名单
     */
    private static final File BLACK_PATHF_FILE = new File("E:\\黑名单群聊.json");

    public GroupModel(DataGroup dataGroup, JSONObject jsonObject) {
        this.dataGroup = dataGroup;
        this.jsonObject = jsonObject;
    }

    /**
     * 黑名单群组
     */
    private static List<String> BLACKGROUPID = ReReadingModel.getFileJson(BLACK_PATHF_FILE);


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
        List<String> re_reading_member_set = ReReadingModel.getMEMBER_SET();
        //过滤
        if (BLACKGROUPID.contains(group_id)) {
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
        } else if (raw_message.startsWith("蓝奏云资源搜索=") && DataUserEights.SUPERUSER.contains(dataGroup.getUser_id())) {
            String key = subEqual("=", raw_message);
            if (key.isEmpty()) {
                return;
            }
            new LanZuoCloudResourceSearch(key, dataGroup).run();
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
        } else if (raw_message.startsWith("添加触发复读机关键词=") && DataUserEights.SUPERUSER.contains(dataGroup.getUser_id())) { //需要超级用户权限
            String key = subEqual("=", raw_message);
            if (key.isEmpty()) {
                return;
            }
            ReReadingModel.addKeySet(dataGroup, key);
            return;
        } else if (raw_message.startsWith("移除触发复读机关键词=") && DataUserEights.SUPERUSER.contains(dataGroup.getUser_id())) { //需要超级用户权限
            String key = subEqual("=", raw_message);
            if (key.isEmpty()) {
                return;
            }
            ReReadingModel.removeKeySet(dataGroup, key);
            return;
        } else if (re_reading_member_set.contains(dataGroup.getUser_id()) || GroupModel.keyContains(ReReadingModel.getKEY_SET(), filtrationCQ(raw_message))) {
            //复读模块
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


    /**
     * 判断数组里的符合元素的对象
     *
     * @param listContent 集合对象
     * @param raw_message 关键对象
     * @return
     */
    @SuppressWarnings("all")
    private static boolean isContainsMessAge(List<String> listContent, String raw_message) {
        for (String s : listContent) {
            if (s.contains(raw_message)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取指定关键词后面的内容
     * 且关键词是最靠前的那一个
     * 如果截取不到或者就是字符串开头第一个则返回空的字符串
     *
     * @param key 关键词
     * @param str 字符串
     * @return 截取之后的内容
     */
    public static String subEqual(String key, String str) {
        int i = str.indexOf(key);
        if (i == -1 || i == 0) {
            return "";
        }
        str = str.substring(i + 1).trim();

        return str;
    }

    /**
     * 获取指定关键词后面的内容
     * 且关键词是最靠前后的那一个
     * 如果截取不到或者就是字符串开头第一个则返回空的字符串
     *
     * @param key 关键词
     * @param str 内容
     * @return 截取之后的内容
     */
    public static String lastSubEqual(String key, String str) {
        int i = str.lastIndexOf(key);
        if (i == -1 || i == 0) {
            return "";
        }
        str = str.substring(i + 1).trim();

        return str;
    }


    /**
     * 遍历集合对象中key与content是否是包含关系
     * 比如:集合内有key=舞台,content=物天空下起下午,舞台开始了,那么key是content的其中的一部分
     *
     * @param list    集合
     * @param content 关键词
     * @return 布尔值
     */
    public static boolean keyContains(List<String> list, String content) {
        if (content.isEmpty()) {
            return false;
        }
        for (String key : list) {
            if (content.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤消息中的CQ码,如头部[CQ:我是CQ的内容]尾部
     * 过滤之后=头部尾部
     * 如果过滤之后还是跟原来的一样,就返回原来的字符串
     *去除字符串中[CQ:....]中的CQ内容
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


}





