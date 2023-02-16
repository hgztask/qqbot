package com.example.qqbot.Util;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.jsoup.Connection;
import org.junit.jupiter.api.Test;

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


}
