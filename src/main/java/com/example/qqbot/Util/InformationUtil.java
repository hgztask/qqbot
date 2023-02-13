package com.example.qqbot.Util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.data.group.DataGroup;
import lombok.NonNull;

/**
 * 信息处理
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 22:29
 */
public class InformationUtil {


    /**
     * 负责获取被艾特方的QQ号码
     *
     * @param jsonObject json对象
     * @return 返回QQ号字符串或者空的字符串
     */
    public static @NonNull String getUserATID(@NonNull JSONObject jsonObject) {
        JSONObject byPath = jsonObject.getByPath("message[1]", JSONObject.class);
        if (byPath == null) {
            return "";
        }
        if (!("at".equals(byPath.get("type", String.class)))) {
            return "";
        }
        String qqIndex = byPath.getByPath("data.qq", String.class);
        if (qqIndex == null || qqIndex.isEmpty()) {
            return "";
        }
        return qqIndex;
    }

    /**
     * 责获取被艾特方的QQ号码
     * @param dataGroup  dataGroup对象
     * @return 返回QQ号字符串或者空的字符串
     */
    public static @NonNull String getUserATID(@NonNull DataGroup dataGroup) {
        return InformationUtil.getUserATID(JSONUtil.parseObj(dataGroup));
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



}
