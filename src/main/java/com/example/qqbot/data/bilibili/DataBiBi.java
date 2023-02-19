package com.example.qqbot.data.bilibili;

import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * b站动态内容数据层
 *
 * @author byhgz
 * @version 1.0
 * @date 2023/2/19 11:48
 */
@Data
public class DataBiBi {

    /**
     * 文本内容
     */
    private String textContent;


    /**
     * 动态中的图片
     */
    private List<String> img_src;


    public DataBiBi(String content, List<String> img_srcList) {
        this.textContent = content;
        this.img_src = img_srcList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataBiBi dataBiBi = (DataBiBi) o;
        return Objects.equals(textContent, dataBiBi.textContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(textContent);
    }
}
