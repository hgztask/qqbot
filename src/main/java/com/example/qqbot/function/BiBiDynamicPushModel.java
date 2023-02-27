package com.example.qqbot.function;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.NetworkUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.DataUserEights;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.bilibili.DataBiBi;
import com.example.qqbot.data.group.DataGroup;
import com.example.qqbot.data.json.DataJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 推送b站指定用户动态信息
 * <p>
 * 当前模块还有些问题,以后有空在调试
 * <p>
 * <p>
 * 主要是实现监听b站用户动态,有更新会推送到群聊里
 * 每个群可以添加多个B站uid用户,并用多线程形式检测对应用户动态更新,如果有更新的会推送到对应群聊
 * 2023年2月19日17:33:57
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/18 16:15
 */
@Slf4j
@Component
public class BiBiDynamicPushModel implements Runnable, IMessageEvent {

    private DataGroup dataGroup;


    /**
     * 该变量控制是否循环检测uid获取动态
     */
    private static boolean startbool = false;


    /**
     * 记录需要推送的群聊
     * 格式
     * 群聊:数组uid
     */
    private static final Map<String, Set<String>> GROUP_MAP = new HashMap<>();


    /**
     * uid存储动态内容
     * 格式
     * uid:动态内容
     */
    private static final Map<String, Set<DataBiBi>> DYNAMIC_CONTENT_MAP = new HashMap<>();

    static {
        Set<String> uidSet = new HashSet<>(1);
        //uidSet.add("382651856");
        //uidSet.add("1858782574");
        //uidSet.add("6646795");
        //GROUP_MAP.put("760849278", uidSet);
    }


    /**
     * 空表
     */
    private static final Set<DataBiBi> SETNULL = new HashSet<>(0);

    /**
     * 权重,权重高的值会先匹配
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 40;
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
        String user_id = dataGroup.getUser_id();
        String group_id = dataGroup.getGroup_id();
        String raw_message = dataGroup.getRaw_message();
        List<String> superuser = DataUserEights.SUPERUSER;
        if (raw_message.startsWith("添加要推送b站动态用户=") && superuser.contains(user_id)) {
            String subEqual = InformationUtil.subEqual("=", raw_message);
            if (subEqual.isEmpty()) {
                return false;
            }
            if (add(group_id, subEqual)) {
                SignalUtil.sendGroupMessage(group_id, "添加成功");
                return true;
            }
            SignalUtil.sendGroupMessage(group_id, "添加失败!");
            return true;
        }
        if (raw_message.startsWith("移除要推送b站动态用户=") && superuser.contains(user_id)) {
            String subEqual = InformationUtil.subEqual("=", raw_message);
            if (subEqual.isEmpty()) {
                return true;
            }
            if (del(group_id, subEqual)) {
                SignalUtil.sendGroupMessage(group_id, "移除成功");
                return true;
            }
            SignalUtil.sendGroupMessage(group_id, "移除失败!");
            return true;
        }
        if (raw_message.startsWith("修改要推送b站动态用户=") && superuser.contains(user_id)) {
            return true;
        }
        if (raw_message.startsWith("查询要推送b站动态用户=") && superuser.contains(user_id)) {
            String subEqual = InformationUtil.subEqual("", raw_message);
            if (subEqual.isEmpty()) {
                return true;
            }
            if (read(group_id, subEqual)) {
                SignalUtil.sendGroupMessage(group_id, "该uid用户已在监听队列!");
                return true;
            }
            SignalUtil.sendGroupMessage(group_id, "该uid用户未在监听队列!");
            return true;
        }
        if (raw_message.startsWith("打印当前群聊监听动态uid用户") && superuser.contains(user_id)) {
            printGroupUID(group_id);
            return true;
        }

        if (GROUP_MAP.values().isEmpty()) {
            return false;
        }
        if (startbool) {
            //如果启动了就不需要再次执行启动了,除非修改了该值并且关闭了循环
            return false;
        }

        @SuppressWarnings("all")
        ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
        try {
            threadExecutor.execute(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            threadExecutor.shutdown();
        }
        return false;
    }


    /**
     * 添加需要监听的uid用户
     *
     * @param group_id 群号
     * @param uid      url用户
     * @return 是否添加成功
     */
    private boolean add(String group_id, String uid) {
        if (!(GROUP_MAP.containsKey(group_id))) {
            HashSet<String> set = new HashSet<>();
            set.add(uid);
            GROUP_MAP.put(group_id, set);
            startbool = false;
            return true;
        }
        Set<String> set = GROUP_MAP.get(group_id);
        if (set.add(uid)) {
            return true;
        }
        return false;
    }


    /**
     * 删除群聊监听推送的uid对象
     *
     * @param group_id 群号
     * @param uid      uid
     * @return 是否删除成功
     */
    private boolean del(String group_id, String uid) {
        if (!(GROUP_MAP.containsKey(group_id))) {
            return false;
        }
        Set<String> set = GROUP_MAP.get(group_id);
        if (!(set.contains(uid))) {
            return false;
        }
        return set.remove(uid);
    }

    /**
     * 查询指定uid成员是否存在于记录
     *
     * @param group_id 群号
     * @param uid      uid
     * @return 是否读取成功
     */
    private boolean read(String group_id, String uid) {
        if (!(GROUP_MAP.containsKey(group_id))) {
            return false;
        }
        Set<String> set = GROUP_MAP.get(group_id);
        return set.contains(uid);
    }


    /**
     * 打印指定群聊的uid值
     *
     * @param group_id
     */
    private void printGroupUID(String group_id) {
        Set<String> set = GROUP_MAP.get(group_id);
        if (!(GROUP_MAP.containsKey(group_id)) || set.isEmpty()) {
            SignalUtil.sendGroupMessage(group_id, "获取失败,当前群聊没有监听的uid用户");
            return;
        }
        SignalUtil.sendGroupMessage(group_id, "当前群聊监听得uid:\n" + JSONUtil.toJsonPrettyStr(set));
    }

    /**
     * 修改指定uid的值为新值
     *
     * @param group_id 群号
     * @param oldUid   旧值
     * @param newUid   新值
     * @return 是否修改成功
     */
    private boolean update(String group_id, String oldUid, String newUid) {
        if (!(GROUP_MAP.containsKey(group_id))) {
            return false;
        }
        Set<String> set = GROUP_MAP.get(group_id);
        if (!(set.contains(oldUid))) {
            return false;
        }
        if (!(set.remove(oldUid))) {
            return false;
        }
        return set.add(newUid);
    }

    /**
     * 获存在数组的动态内容
     *
     * @param uid 要查询的uid
     */
    private void getDTContent(String uid) {
        if (!(DYNAMIC_CONTENT_MAP.containsKey(uid))) {
            return;
        }
        if (DYNAMIC_CONTENT_MAP.values().isEmpty()) {
            return;
        }
        for (String v : DYNAMIC_CONTENT_MAP.keySet()) {
            //获取uid对应的动态数组
            Set<DataBiBi> dataBiBis = DYNAMIC_CONTENT_MAP.get(v);
            for (DataBiBi biBi : dataBiBis) {
                //获取动态对应的动态kv对象
                System.out.println(biBi.getTextContent());
                System.out.println(biBi.getImg_src());
            }
            System.out.println("============================================");
        }
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        startbool = true;
        Set<String> keySet = GROUP_MAP.keySet();
        while (startbool) {
            ExecutorService threadPool = Executors.newFixedThreadPool(GROUP_MAP.values().size());
            try {
                for (String gropu_id : keySet) {
                    Set<String> valueUidSet = GROUP_MAP.get(gropu_id);
                    if (valueUidSet.isEmpty()) {
                        continue;
                    }
                    if (!(startbool)) {
                        return;
                    }
                    for (String uid : valueUidSet) {
                        threadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                Set<DataBiBi> webDT = getWebDT(uid);
                                Set<DataBiBi> dt = getDT(uid);
                                //计算集合的单差集，即只返回【集合1】中有，但是【集合2】中没有的元素，例如：
                                //subtract([1,2,3,4],[2,3,4,5]) -》 [1]
                                if (webDT.isEmpty()) {
                                    log.info("webDT为空集合");
                                    return;
                                }
                                if (dt.isEmpty()) {
                                    log.info("dt为空集合");
                                    addTD(uid,webDT);
                                }
                                Collection<DataBiBi> subtract = CollUtil.subtract(webDT, dt);
                                if (subtract.isEmpty()) {
                                    //说明没有新内容
                                    return;
                                }
                                log.info("群聊" + gropu_id + "检测到b站用户" + uid + "动态更新的个数:" + subtract.size());
                                Set<DataBiBi> dataBiBiSet = new HashSet<>(subtract);
                                addTD(uid, dataBiBiSet);
                                for (DataBiBi dataBiBi : dataBiBiSet) {
                                    JSONArray item = new JSONArray();
                                    String textContent = dataBiBi.getTextContent();
                                    item.add(DataJson.text(textContent));
                                    List<String> img_src = dataBiBi.getImg_src();
                                    for (String imgUrl : img_src) {
                                        String imageFileName = InformationUtil.subEqual("/", imgUrl);
                                        item.add(DataJson.imageUrl(imageFileName, imgUrl, true));
                                    }
                                    SignalUtil.sendGroupForwardMsg(gropu_id, item);
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                threadPool.shutdown();
            }
            try {
                //休眠5分钟然后接着
                TimeUnit.MINUTES.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * 获取api中最新的动态
     *
     * @param uid
     * @return
     */
    private Set<DataBiBi> getWebDT(String uid) {
        JSONArray station = NetworkUtil.getAnalyzeTheDynamicApiOfStation(uid);
        if (station.isEmpty()) {
            return SETNULL;
        }
        HashSet<DataBiBi> dataBiBis = new HashSet<>(station.size());
        for (Object o : station) {
            dataBiBis.add(BeanUtil.toBean(o, DataBiBi.class));
        }
        if (!(addTD(uid, dataBiBis))) {
            return SETNULL;
        }
        return dataBiBis;
    }


    /**
     * 获取存储在DYNAMIC_CONTENT_MAP中对应uid的动态
     *
     * @param uid uid用户
     * @return
     */
    private Set<DataBiBi> getDT(String uid) {
        Set<DataBiBi> dataBiBis = DYNAMIC_CONTENT_MAP.get(uid);
        if (dataBiBis == null || dataBiBis.isEmpty()) {
            return SETNULL;
        }
        return dataBiBis;

    }


    /**
     * 添加uid对应的动态内容到数组里存储,用于判断
     *
     * @param uid       uid
     * @param dataBiBis 动态内容
     * @return 布尔值
     */
    private synchronized boolean addTD(String uid, Set<DataBiBi> dataBiBis) {
        if (!(DYNAMIC_CONTENT_MAP.containsKey(uid))) {
            return false;
        }
        DYNAMIC_CONTENT_MAP.put(uid, dataBiBis);
        return true;
    }


}
