package com.example.qqbot;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.data.MailingAddress;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 机器人发送消息的静态方法
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/9 21:42
 */
@Slf4j
public class SignalUtil {

    /**
     * 空的jsonOBJ对象
     */
    private static final JSONObject JSONNULL = new JSONObject(0);


    /**
     * 群消息的终结点
     */
    @Getter
    private static final String GROUPENDPOINT = "/send_group_msg";

    /**
     * get发送消息请求
     *
     * @param type      发送角色,私聊,群聊等
     * @param parameter 参数
     * @return 请求的相应结果json
     */
    public static JSONObject httpGet(String type, Map<String, String> parameter) {
        Connection data = Jsoup.connect(MailingAddress.SEND_MESSAGE + type)
                .method(Connection.Method.GET)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.78")
                .ignoreContentType(true)
                .data(parameter);
        Connection.Response execute;
        try {
            execute = data.execute();
        } catch (IOException e) {
            System.out.println("请求execute时出错" + e.getMessage());
            return JSONNULL;
        }

        if (execute.statusCode() != 200) {
            return JSONNULL;
        }
        return JSONUtil.parseObj(execute.body());
    }

    /**
     * get发起通讯消息
     *
     * @param type      通讯类型
     * @param parameter 参数
     * @return json对象
     */
    public static JSONObject httpGet(String type, String parameter) {
        Connection connection = Jsoup.connect(MailingAddress.SEND_MESSAGE + type + parameter)
                .method(Connection.Method.GET)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.78")
                .ignoreContentType(true);
        Connection.Response execute;
        try {
            execute = connection.execute();
            System.out.println(execute.url());
        } catch (IOException e) {
            System.out.println("请求execute时出错" + e.getMessage());
            return JSONNULL;
        }
        int code = execute.statusCode();
        if (code != 200) {
            return JSONNULL;
        }
        return JSONUtil.parseObj(execute.body());
    }


    /**
     * 发送消息
     * 发送http post请求
     *
     * @param type      消息通道类型,如群聊私聊等
     * @param parameter 参数
     * @return jsonOBj对象
     */
    public static JSONObject httpPost(String type, Map<String, String> parameter) {
        Connection connection = Jsoup.connect(MailingAddress.SEND_MESSAGE + type)
                .method(Connection.Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.78")
                .ignoreContentType(true)
                .data(parameter);
        Connection.Response execute;
        try {
            execute = connection.execute();
        } catch (IOException e) {
            System.out.println("请求execute时出错" + e.getMessage());
            return JSONNULL;
        }
        int code;
        try {
            code = execute.statusCode();
        } catch (Exception e) {
            System.out.println("请求状态码异常!" + e.getMessage());
            return JSONNULL;
        }
        if (code != 200) {
            return JSONNULL;
        }

        return JSONUtil.parseObj(execute.body());
    }


    /**
     * 机器人发送群消息
     *
     * @param group_id 群号
     * @param message  消息内容
     * @return json对象结果
     */
    public static JSONObject sendGroupMessage(@NonNull String group_id, @NonNull String message) {
        final Map<String, String> data = new HashMap<>();
        data.put("group_id", group_id);
        data.put("message", message);
        return SignalUtil.httpGet(GROUPENDPOINT, data);
    }

}
