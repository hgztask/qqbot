package com.example.qqbot.model.pgr;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONArray;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.group.DataGroup;
import com.example.qqbot.data.json.DataJson;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 相关游戏逻辑层面
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/13 20:26
 */
@Slf4j
public class PGRModel implements Runnable {

    @Setter
    private DataGroup dataGroup;

    @Getter
    private static final PGRModel pgrModel = new PGRModel();

    /**
     * 工会关键词
     */
    @Getter
    @SuppressWarnings("all")
    private static final List<String> PGR_GUILDID = ListUtil.toList("公会id", "公会ID", "工会", "公会", "招人");


    @Override
    public void run() {
        if (dataGroup == null) {
            throw new NullPointerException("dataGroup为Nll");
        }
        String raw_message = dataGroup.getRaw_message();
        String group_id = dataGroup.getGroup_id();
        String user_id = dataGroup.getUser_id();

        log.info("暂时功能还没想好");


    }

    /**
     * 打印战双意识表
     *
     * @param group_id 发送的群号
     * @param user_id  接受的QQ号,艾特对方
     *                 //https://s1.ax1x.com/2023/02/18/pSLr3xe.png
     */
    public static void consciousnessPGRTable(String group_id, String user_id) {
        //针对于发图片的类型,目前测试需要带上file参数,这个应该是必带,试了不带发不出去,例外图片如果是网络链接的最好是图片格式的那种,以此为戒
        String url="https://s1.ax1x.com/2023/02/18/pSLr3xe.png";
        JSONArray nodeJson = new JSONArray();
        nodeJson.add(DataJson.at(user_id));
        nodeJson.add(DataJson.text("意识推荐共鸣配队站位表\n"+
                "可用于角色意识搭配、意识共鸣、组队推荐\n"+
                        "看不清的请放大加载原图\n"));
        String imageFileName= InformationUtil.subEqual("/",url);
        nodeJson.add(DataJson.imageUrl(imageFileName,url,true));
        if (SignalUtil.sendGroupMessage(group_id,nodeJson).isEmpty()) {
            log.info("发送战双=意识表失败!");
            return;
        }
        log.info("发送战双=意识表成功!");
    }

    /**
     * 打印工会信息
     *
     * @param group_id
     * @param user_id
     */
    public static void printPGRGuild(String group_id, String user_id) {
        SignalUtil.sendGroupMessage(group_id, String.format("[CQ:at,qq=%s]本群的公会\n" +
                "公会名：桐子酱的光剑\n" +
                "信标服公会ID：00008433\n" +
                "星火服公会ID：00009669\n" +
                "星火就是官服如官网下载taptap下载等，反之星标即渠道服，手机商店下载", user_id));
    }

}

