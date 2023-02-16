package com.example.qqbot.data.file;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Getter;

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

    @Getter
    private static final List<String> YS_IMAGE_LIST = new ArrayList<>(ysImageJsonObj.keySet());


    private static final JSONObject LEG_SERIESOBJ = JSONUtil.readJSONObject(new File("E:\\PCwinResouceDisk\\Download\\img\\腿系列.json"), StandardCharsets.UTF_8);

    private static final JSONObject HASH_MAP_COLLECTION = JSONUtil.readJSONObject(new File("E:\\PCwinResouceDisk\\Download\\img\\hashMap集合.json"), StandardCharsets.UTF_8);

    @Getter
    private static final List<String> LEG_SERIESL_IST = new ArrayList<>(LEG_SERIESOBJ.keySet());

    @Getter
    private static final List<String> haashmapList = new ArrayList<>(HASH_MAP_COLLECTION.keySet());

    /**
     * 随机从集合里取出一个值
     *
     * @return
     */
    public static String getRandomKey(List<String> list) {
        int i = RANDOM.nextInt(list.size());
        return list.get(i);
    }


}
