package com.example.qqbot.Util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.jsoup.Connection;

/**
 * 网络api封装工具
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/16 13:41
 */
public class NetworkUtil {


    /**
     * 翻译功能
     *
     * @param content 要翻译的文本
     * @return jsonOBj对象
     */
    public static JSONObject translate(String content) {
        Connection.Response response = SignalUtil.jsoupHttpGet("http://www.ggapi.cn/api/fanyi?type=AUTO&city=" + content, false);
        if (response.statusCode() != 200) {
            return SignalUtil.getJSONNULL();
        }
        return JSONUtil.parseObj(response.body());
    }


    /**
     * 使用网络api解析蓝奏云链接,并获取直链
     *
     * @param url 蓝奏云分享链接的地址
     * @return 返回直链 字符串
     */
    public static String analyticStraightChainLanZouY(String url) {
        if (!(HttpUtil.isHttps(url) || HttpUtil.isHttp(url))) {
            return "";
        }
        //注意用的是自己的接口
        String content = HttpUtil.get("https://vip.mikuchase.ltd/?url=" + url);
        JSONObject jsonObject;
        try {
            jsonObject = JSONUtil.parseObj(content);
        } catch (Exception e) {
            return "";
        }
        Integer code = jsonObject.get("status", int.class);
        if (code == null || code == 0) {
            return "";
        }
        String downdLoadUrl = jsonObject.getByPath("info", String.class);
        if (downdLoadUrl == null) {
            return "";
        }
        return downdLoadUrl;
    }


    /**
     * 根据哔哩哔哩用户uid获取用户信息
     *
     * @param uid uid号
     * @return jsonobj对象
     */
    public static JSONObject getBiBiUserinfo(String uid) {
        JSONObject jsonnull = SignalUtil.getJSONNULL();
        Connection.Response response = SignalUtil.jsoupHttpGet("https://api.xingzhige.com/API/b_personal/?mid=" + uid, false);
        if (response.statusCode() != 200) {
            return jsonnull;
        }
        String body = response.body();
        if (!(JSONUtil.isTypeJSONObject(body))) {
            return jsonnull;
        }
        JSONObject jsonObject = JSONUtil.parseObj(body);
        Integer code = jsonObject.get("code", int.class);
        if (code == null || code != 0) {
            return jsonnull;
        }
        JSONObject data = jsonObject.get("data", JSONObject.class);
        if (data == null || data.isEmpty()) {
            return jsonnull;
        }
        return data;
    }

    /**
     * 根据getBiBiUserinfo放的json对象进行解析排版
     *
     * @param jsonObject jsonobj对象
     * @return 格式化的内容
     */
    public static String getBiBiUserinfo(JSONObject jsonObject) {
        return CharSequenceUtil.format("""
                        用户uid:{}
                        用户昵称:{}
                        用户性别:{}
                        用户头像:{}
                        用户简介:{}
                        用户等级:{}
                        用户关注:{}
                        用户粉丝:{}
                        直播房间号:{}
                        直播房间标题:{}
                        直播房间地址:{}
                        直播房间封面:{}
                        直播房间人气:{}
                        """, jsonObject.getByPath("owner.mid", int.class)
                , jsonObject.getByPath("owner.name", String.class)
                , jsonObject.getByPath("owner.sex", String.class)
                , jsonObject.getByPath("owner.face", String.class)
                , jsonObject.getByPath("owner.desc", String.class)
                , jsonObject.getByPath("owner.level", String.class)
                , jsonObject.getByPath("fans.following", String.class)
                , jsonObject.getByPath("fans.follower", String.class)
                , jsonObject.getByPath("live_room.roomid", String.class)
                , jsonObject.getByPath("live_room.title", String.class)
                , jsonObject.getByPath("live_room.url", String.class)
                , jsonObject.getByPath("live_room.cover", String.class)
                , jsonObject.getByPath("live_room.watch.num", String.class)
        );
    }


    /**
     * 端口扫描
     * 扫描域名/服务器IP开放端口列表
     *
     * @param url 要扫苗的url
     * @return 相应内容
     */
    public static String portScan(String url) {
        if (!(InformationUtil.isHttpUrl(url))) {
            return "";
        }
        Connection.Response response = SignalUtil.jsoupHttpGet("https://v.api.aa1.cn/api/api-port/go.php?ip=" + url, false);
        if (response == null || response.statusCode() != 200) {
            return "";
        }
        return response.body();
    }

    /**
     * 获取随机谜语
     * @return jsonobj对象
     */
    public static JSONObject riddle() {
        Connection.Response response = SignalUtil.jsoupHttpGet("https://v.api.aa1.cn/api/api-miyu/index.php", false);
        if (response == null || response.statusCode() != 200) {
            return SignalUtil.getJSONNULL();
        }
        String body = response.body();
        if (!(JSONUtil.isTypeJSONObject(body))) {
            return SignalUtil.getJSONNULL();
        }
        JSONObject jsonObject = JSONUtil.parseObj(body);
        String code = jsonObject.get("code", String.class);
        if (!("1".equals(code))) {
            return SignalUtil.getJSONNULL();
        }
        return jsonObject;
    }


}
