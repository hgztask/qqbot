package com.example.qqbot.Event;

import cn.hutool.json.JSONObject;
import com.example.qqbot.data.Message;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author byhgz
 * @version 1.0
 * @date 2023/2/14 15:12
 */
@Component
public class Subject {

    /**
     * 容器
     */
    private static Map<String, IMessageEvent> map;
    /**
     * 有序的接口,用于后面遍历和权重相关的
     */
    @Getter
    private static List<IMessageEvent> list;

    @Autowired
    public void setMap(Map<String, IMessageEvent> map) {
        Subject.map = map;
    }

    /**
     * 被该注解修饰的方法,会在当前类实例化之后自动执行
     */
    @PostConstruct
    public void init() {
        list = new ArrayList<>(map.size());
        for (IMessageEvent value : map.values()) {
            list.add(value);
        }
        //排序权重的容器
        //降序,权限大的排前面
        list = Subject.list.stream().sorted(Comparator.comparing(IMessageEvent::weight).reversed()).collect(Collectors.toList());
    }

    /**
     * 依次遍历执行
     * @param jsonObject
     * @param message
     */
    public static void change(JSONObject jsonObject, Message message) {
        for (IMessageEvent iMessageEvent : list) {
            if (iMessageEvent.onMessage(jsonObject, message)) {
                //如果匹配上了就不用往下面匹配了
                break;
            }
        }
    }
}
