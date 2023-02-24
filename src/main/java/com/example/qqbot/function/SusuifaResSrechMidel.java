package com.example.qqbot.function;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.group.DataGroup;
import com.example.qqbot.data.json.DataJson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 通过苏苏影视网站搜索资源
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/24 16:13
 */
@Slf4j
@Component
public class SusuifaResSrechMidel implements IMessageEvent {


    /**
     * 记录data资源
     */
    private static Set<Data> dataSetList = new HashSet<>(0);


    private DataGroup dataGroup;


    @lombok.Data
    class Data {
        private String name;
        private String id;

        private String content;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Data data = (Data) o;
            return Objects.equals(id, data.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }


    /**
     * 搜索
     *
     * @param raw_message 消息内容
     */
    private void srarch(String raw_message) {
        String key = InformationUtil.subEqual("=", raw_message);
        if (key.isEmpty() || key.length() <= 1) {
            return;
        }
        String group_id = dataGroup.getGroup_id();
        String self_id = dataGroup.getSelf_id();
        SignalUtil.sendGroupMessage(group_id, "正在搜索中!");
        //返回资源id、资源名
        Set<Data> dataSet = ssysSrarch(key);
        if (dataSet.isEmpty()) {
            SignalUtil.sendGroupMessage(group_id, "未搜索倒指定关键词资源:" + key);
            return;
        }
        JSONArray jsonArray = new JSONArray();
        if (dataSet.size() == 1) {
            for (Data v : dataSet) {
                jsonArray.add(DataJson.text("搜索到:\n"));
                jsonArray.add(DataJson.text("资源名:" + v.getName()));
                jsonArray.add(DataJson.text("\nid:" + v.getId()));
                jsonArray.add(DataJson.text("\n回复id获取资源"));
                SignalUtil.sendGroupMessage(group_id, jsonArray);
            }
            return;
        }
        dataSetList.addAll(dataSet);
        jsonArray.add(DataJson.text("回复格式:影视资源id=id数字,获取资源"));
        for (Data v : dataSetList) {
            JSONArray item = new JSONArray(4);
            item.add(DataJson.text("资源名:" + v.getName()));
            item.add(DataJson.text("\nid:" + v.getId()));
            jsonArray.add(item);
        }
        SignalUtil.sendGroupForwardMsg(group_id, DataJson.nodeMerge("机器人", self_id, jsonArray));
    }


    /**
     * 搜索苏苏影视资源
     * 测试过对应网站的图片没法让机器人发送,故取消图片
     *
     * @param key 关键词
     * @return 返回资源id、封面、资源名
     */
    @SneakyThrows
    public Set<Data> ssysSrarch(String key) {
        Document parse = Jsoup.connect("https://susuifa.com/?s=" + key + "&type=post").get();
        Set<Data> list = new HashSet<>();
        Elements li = null;
        try {
            li = parse.getElementsByClass("b2_gap ").get(0).select("li[class='post-list-item item-post-style-1']");
        } catch (Exception e) {
            log.info("出现错误了!" + e.getMessage());
            return list;
        }
        for (Element v : li) {
            Data data = new Data();
            String name = v.getElementsByTag("h2").get(0).text();
            String id = v.attr("id");
            id = id.replace("item-", "");
            data.setName(name);
            data.setId(id);
            list.add(data);
        }
        return list;
    }

    /**
     * 根据资源id获取对应的内容
     *
     * @param id 资源id
     * @return
     */
    @SneakyThrows
    public static JSONObject getIdContent(String id) {
        JSONObject jsonObject = new JSONObject();
        try {
            Document parse = Jsoup.connect("https://susuifa.com/" + id + ".html").get();
            Element element = parse.getElementsByClass("entry-content").get(0);
            String title = parse.getElementsByClass("entry-header").get(0).getElementsByTag("h1").text();
            //内容
            String content = element.getElementsByTag("p").text();
            jsonObject.set("title", title);
            jsonObject.set("content", content);
        } catch (IOException e) {
            log.info("出错了=" + e.getMessage());
            return jsonObject;
        }
        return jsonObject;
    }


    public void addSetContent(String id, String content) {
        for (Data v : dataSetList) {
            if (!(v.getId().equals(id))) {
                continue;
            }
            v.setContent(content);
            return;
        }
        //说明集合没有该id
        Data data = new Data();
        data.setId(id);
        data.setContent(content);
        dataSetList.add(data);
    }


    /**
     * 权重,权重高的值会先匹配
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 6;
    }

    /**
     * 接受消息
     *
     * @param jsonObject 原始消息对象
     * @param message    消息对象
     * @return 是否匹配成功
     */
    @Override
    public boolean onMessage(JSONObject jsonObject, Message message) {
        if (!("group".equals(message.getMessage_type()))) {
            return false;
        }
        this.dataGroup = BeanUtil.toBean(jsonObject, DataGroup.class);
        String raw_message = dataGroup.getRaw_message();
        String user_id = dataGroup.getUser_id();
        String group_id = dataGroup.getGroup_id();
        if ((raw_message.startsWith("影视资源搜索="))) {
            srarch(raw_message);
            return true;
        }
        if (raw_message.startsWith("影视资源id=")) {
            String id = InformationUtil.subEqual("=", raw_message);
            if (id.isEmpty()) {
                return true;
            }
            for (Data v : dataSetList) {
                if (!(id.equals(v.getId()))) {
                    continue;
                }
                String content = v.getContent();
                if (content == null || content.isEmpty()) {
                    continue;
                }
                //如果都不符合,说明集合里有对应的资源,就没必要再次请求了
                SignalUtil.sendGroupMessage(group_id, content);
                return true;
            }
            //不在集合里的情况
            SignalUtil.sendGroupMessage(group_id, "正在获取中!");
            JSONObject json = getIdContent(id);
            if (json.isEmpty()) {
                SignalUtil.sendGroupMessage(group_id, "获取失败,没有该资源!!");
                return true;
            }
            String content = json.get("content", String.class);
            SignalUtil.sendGroupMessage(group_id, String.format("%s\n%s", json.get("title", String.class), content));
            addSetContent(id, content);
            return true;
        }
        return false;
    }


}
