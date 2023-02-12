package com.example.qqbot.model;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.example.qqbot.SignalUtil;
import com.example.qqbot.data.DataGroup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * 每天60秒看世界逻辑层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 23:28
 */
@Slf4j
public class Day60World implements Runnable {

    private static Day60World day60World = new Day60World();

    private DataGroup dataGroup;

    private Day60World() {
    }


    private void getDay60WorldContent() {
        if (SignalUtil.sendGroupMessage(dataGroup.getGroup_id(), "[CQ:image,file=每日60秒看世界,url=http://www.ggapi.cn/api/60s]").isEmpty()) {
            log.info("60秒看世界图发送成功");
            return;
        }
        log.info("60秒看世界图发送失败");
    }

    public static void start(@NonNull DataGroup dataGroup) {
        day60World.dataGroup = dataGroup;
        day60World.run();
    }


    @Override
    public void run() {
        if (dataGroup == null) {
            throw new NullPointerException("dataGroup 为null,请通过start方法设置");
        }

        Integer time = dataGroup.getTime()*1000;
        DateUtil.date(time);//



    }


}






