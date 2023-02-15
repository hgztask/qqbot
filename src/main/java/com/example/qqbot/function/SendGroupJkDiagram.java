package com.example.qqbot.function;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import com.example.qqbot.Event.IMessageEvent;
import com.example.qqbot.Util.InformationUtil;
import com.example.qqbot.Util.SignalUtil;
import com.example.qqbot.data.CQ.CQCode;
import com.example.qqbot.data.Message;
import com.example.qqbot.data.group.DataGroup;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.springframework.stereotype.Component;

/**
 * 群聊发送jk图
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/14 22:41
 */
@Slf4j
@Component
public class SendGroupJkDiagram implements Runnable, IMessageEvent {


    private DataGroup dataGroup;


    /**
     * 权重,权重高的值会先匹配
     *
     * @return 权重值
     */
    @Override
    public int weight() {
        return 1000;
    }


    /**
     * 判断是否在请求中!
     */
    private static boolean temp = false;

    /**
     * 接受消息
     *
     * @param jsonObject 原始消息对象
     * @param message    消息对象
     * @return 是否匹配成功
     */
    @Override
    public boolean onMessage(JSONObject jsonObject, Message message) {
        if (!"group".equals(message.getMessage_type())) {
            return false;
        }
        this.dataGroup = BeanUtil.toBean(jsonObject, DataGroup.class);
        if (!(dataGroup.getRaw_message().startsWith("获取jk图"))) {
            return false;
        }
        synchronized (SendGroupJkDiagram.class) {
            log.info("线程进入了synchronized块进行判断" + Thread.currentThread().getName());
            if (temp) {
                SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "请稍等上一个请求完成");
                return false;
            }
            log.info("线程进入了synchronized块并把temp值赋值为true" + Thread.currentThread().getName());
            temp = true;
        }
        this.run();
        return true;
    }

    /**
     * 请求完成之后获取完整的图片cq码
     *
     * @return 图片cq码
     */
    public static String getJKImageCQ() {
        Connection.Response response = SignalUtil.jsoupHttpGet("http://www.ggapi.cn/api/jkzf", false);
        if (response == null) {
            log.info("请求失败了!");
            return "";
        }
        String location = response.header("Location");
        if (location == null) {
            log.info("获取响应体的Location值失败");
            return "";
        }
        String fileName = InformationUtil.subEqual("//", location);
        if (fileName.isEmpty()) {
            fileName = "jk图";
        }
        return CQCode.image(fileName, location, true);
    }

    @Override
    public void run() {
        String strTip = "正在请求jk图!,请稍等!";
        log.info(strTip);
        SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), strTip);
        String jkImageCQ = getJKImageCQ();
        log.info("线程进入了synchronized块并把temp值赋值为false" + Thread.currentThread().getName());
        temp = false;
        if (jkImageCQ.isEmpty()) {
            log.info("getJKImageCQ方法返回结果为空字符串!");
            return;
        }
        SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), jkImageCQ);
    }
}
