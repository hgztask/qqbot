package com.example.qqbot.Util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.data.group.DataGroup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 信息处理
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 22:29
 */
@Slf4j
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
     *
     * @param dataGroup dataGroup对象
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


    /**
     * 获取当前时间的日,比如今天是1月5日,即获取到5
     *
     * @return 日
     */
    public static int getDateDAY_OF_MONTH() {
        return DateUtil.date().getField(DateField.DAY_OF_MONTH);
    }

    /**
     * 遍历集合对象中key与content是否是包含关系
     * 比如:集合内有key=舞台,content=物天空下起下午,舞台开始了,那么key是content的其中的一部分
     *
     * @param list    集合
     * @param content 关键词
     * @return 布尔值
     */
    public static boolean keyContains(Set<String> list, String content) {
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
     * 判断原始消息里是否有数组里符合的元素
     * 判断原始内容是否有集合的某个元素
     *
     * @param listContent 集合对象 判断的一方
     * @param raw_message 原始字符串 被判断
     * @return
     */
    @SuppressWarnings("all")
    public static boolean isContainsMessAge(List<String> listContent, String raw_message) {
        for (String s : listContent) {
            //左边的原始消息是否某个段落连续有集合内的某个关键元素
            //比如:集合内有你好,原始消息有你好世界,这里有连续的你好,就成立,反之不连续的不成立,亦或者是没有,也不成立
            if (raw_message.contains(s)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取消息中的类型参数
     * 如果获取不到返回空列表,反之对应类型
     *
     * @param jsonObject 消息的json对象
     * @return 返回类型的集合
     */
    public static List<String> getMessageType(@NonNull JSONObject jsonObject) {
        List<String> listNull = new ArrayList<>(0);
        System.out.println(jsonObject.toStringPretty());
        //获取该消息中包含的类型
        List<String> temp = jsonObject.getByPath("message", List.class);
        if (temp == null || temp.isEmpty()) {
            return listNull;
        }
        ArrayList<String> typeList = new ArrayList<>(5);
        for (String tempType : temp) {
            String type = JSONUtil.parseObj(tempType).get("type",String.class);
            if (type==null||type.isEmpty()) {
                continue;
            }
            typeList.add(type);
        }
        return typeList;
    }





}
