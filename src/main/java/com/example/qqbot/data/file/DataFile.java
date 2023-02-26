package com.example.qqbot.data.file;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.qqbot.Util.InformationUtil;
import lombok.Getter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author byhgz
 * @version 1.0
 * @date 2023/2/16 11:28
 */
public class DataFile {


    private static final JSONObject ysImageJsonObj = JSONUtil.readJSONObject(new File("E:\\PCwinResouceDisk\\Download\\img\\原神系列.json"), StandardCharsets.UTF_8);

    @Getter
    private static final List<String> YS_IMAGE_LIST = new ArrayList<>(ysImageJsonObj.keySet());


    private static final JSONObject LEG_SERIESOBJ = JSONUtil.readJSONObject(new File("E:\\PCwinResouceDisk\\Download\\img\\腿系列.json"), StandardCharsets.UTF_8);

    private static final JSONObject HASH_MAP_COLLECTION = JSONUtil.readJSONObject(new File("E:\\PCwinResouceDisk\\Download\\img\\hashMap集合.json"), StandardCharsets.UTF_8);


    @Getter
    private static final List<String> HASH_MAPGATHER = new ArrayList<>(JSONUtil.readJSONObject(new File("E:\\PCwinResouceDisk\\Download\\img\\hashMap集合.json"), StandardCharsets.UTF_8).keySet());

    @Getter
    private static final List<String> LEG_SERIESL_IST = new ArrayList<>(LEG_SERIESOBJ.keySet());

    @Getter
    private static final List<String> haashmapList = new ArrayList<>(HASH_MAP_COLLECTION.keySet());

    /**
     * 随机从集合里取出一个值
     *
     * @param list 集合
     * @return 值
     */
    public static String getRandomKey(List<String> list) {
        int i = InformationUtil.getRANDOM().nextInt(list.size());
        return list.get(i);
    }


    /**
     * 随机从集合中取出一组值
     *
     * @param list 集合
     * @param size 多少个(最大不会超过该数量)
     * @return
     */
    public static Set<String> getRandomKeyList(List<String> list, int size) {
        HashSet<String> set = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add(getRandomKey(list));
        }
        return set;
    }


}
