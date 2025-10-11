package com.deaifish.excel2db.util;

import com.github.promeg.pinyinhelper.Pinyin;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cxx
 * @description 拼音工具类
 * @date 2025-10-11 17:03
 */
@Slf4j
public class PinYinUtil {

    /**
     * 获取拼音
     * @param chinese 中文
     * @return 拼音首字母
     */
    public static String getPinyin(String chinese) {
        StringBuilder pinyin = new StringBuilder();
        for (int i = 0; i < chinese.length(); i++) {
            String c = Pinyin.toPinyin(chinese.charAt(i));
            pinyin.append(c.charAt(0));
        }
        return pinyin.toString();
    }
}
