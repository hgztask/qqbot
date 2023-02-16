package com.example.qqbot.data.file;

import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author byhgz
 * @version 1.0
 * @date 2023/2/16 11:28
 */
public class DataFile {
    private static final Random RANDOM = new Random();


    private static final JSONObject ysImageJsonObj = JSONUtil.readJSONObject(new File("E:\\PCwinResouceDisk\\Download\\img\\原神系列.json"), StandardCharsets.UTF_8);
    private static final List<String> ysImageList = new ArrayList<>(ysImageJsonObj.keySet());


    /**
     * 随机从集合里取出一个值
     *
     * @return
     */
    public static String getRandomKey() {
        int i = RANDOM.nextInt(ysImageList.size());
        return ysImageList.get(i);
    }



}
