package com.example.qqbot.model;

import com.example.qqbot.data.DataGroup;

/**
 *
 * 晚安睡觉时的逻辑层
 * @author byhgz
 * @version 1.0
 * @date 2023/2/11 18:11
 */
public class GoodMightSleep implements Runnable{

    private DataGroup dataGroup;

    public GoodMightSleep(DataGroup dataGroup) {
        this.dataGroup = dataGroup;
    }

    @Override
    public void run() {

        Integer time = dataGroup.getTime();



    }
}
