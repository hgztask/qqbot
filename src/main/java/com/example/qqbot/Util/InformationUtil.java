package com.example.qqbot.Util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

    @Getter
    private static final Random RANDOM = new Random();


    /**
     * 获取指定关键词后面的内容
     * 且关键词是最靠前的那一个
     * 如果截取不到或者就是字符串开头第一个则返回空的字符串
     * 关键词和内容相同会返回空字符串
     * 获取不到会返回空字符串
     *
     * @param key 关键词
     * @param str 字符串
     * @return 截取之后的内容
     */
    @SuppressWarnings("all")
    public static String subEqual(String key, String str) {
        if (key.equals(str)) {//关键词和内容相同不用截取了
            return "";
        }
        int i = str.indexOf(key);
        if (i == -1) {//获取不到的情况
            return "";
        }

        if (i == 0) {//获取时刚好是前面内容,且要获取关键词后面的内容
            return str.substring(key.length()).trim();
        }
        //正常截取指定关键词后面的内容!
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
     * 判断是否是url链接
     *
     * @param url url链接
     * @return 真亦或者假
     */
    public static boolean isHttpUrl(String url) {
        if (HttpUtil.isHttp(url) || HttpUtil.isHttps(url)) {
            return true;
        }
        return false;
    }


    /**
     * 根据i值获取对应星期字符串内容
     *
     * @param integer
     * @return
     */
    public static String getDayWeek(Integer integer) {
        switch (integer) {
            case 1:
                return "星期一";
            case 2:
                return "星期二";
            case 3:
                return "星期三";
            case 4:
                return "星期四";
            case 5:
                return "星期五";
            case 6:
                return "星期六";
            case 7:
                return "星期日";
            default:
                return "未知周";
        }
    }


    /**
     * 根据字节大小转成字符串文件大小样式
     *
     * @param size 字节大小
     * @return 字符串样式大小
     */
    public static String getSize(long size) {
        //获取到的size为：1705230
        int GB = 1024 * 1024 * 1024;//定义GB的计算常量
        int MB = 1024 * 1024;//定义MB的计算常量
        int KB = 1024;//定义KB的计算常量
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize = "";
        if (size / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + "GB   ";
        } else if (size / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + "MB   ";
        } else if (size / KB >= 1) {
            //如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) KB) + "KB   ";
        } else {
            resultSize = size + "B   ";
        }
        return resultSize;
    }

    /**
     * 根据字节大小转成字符串文件大小样式
     *
     * @param size 字节大小
     * @return 字符串样式大小
     */
    public static String getSize(String size) {
        Long value;
        try {
            value = Long.valueOf(size);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
        return getSize(value);
    }


    /**
     * 获取指定路径jso文件的jsonArr数组集合对象
     *
     * @param file 路径
     * @return list字符串集合
     */
    public static List<String> getFIleListJson(String file) {
        ArrayList<String> list = new ArrayList<>(0);
        JSONArray jsonArray;
        try {
            jsonArray = JSONUtil.readJSONArray(new File(file), StandardCharsets.UTF_8);
        } catch (IORuntimeException e) {
            log.info("出现异常了=" + e.getMessage());
            return list;
        }
        for (Object v : jsonArray) {
            list.add(v.toString());

        }
        return list;
    }


}
